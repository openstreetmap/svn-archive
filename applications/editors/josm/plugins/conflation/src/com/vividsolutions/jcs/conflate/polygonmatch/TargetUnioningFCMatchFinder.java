/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package com.vividsolutions.jcs.conflate.polygonmatch;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.CoordinateArrays;
import java.util.*;
/**
 *  An FCMatchFinder wrapper that also treats unions of adjacent target features
 *  as themselves target features. Such unions are formed into composite target
 *  features. These composites are temporary -- before the results are returned,
 *  each composite is split into its constituent features. <P>
 *
 *  The result returned is a one-to-one mapping of target feature to matched
 *  candidate feature; the one-to-one mapping is achieved by discarding all
 *  matches except for those with the highest scores, for each feature (target
 *  and matched candidate). <P>
 *
 *  Note on composites: if a composite's top score is higher than the top score
 *  of each of its constituents, the composite match is retained and constituent
 *  matches are discarded; otherwise, the composite match is discarded and
 *  constituent matches are retained.
 */
public class TargetUnioningFCMatchFinder implements FCMatchFinder {
    private FCMatchFinder matchFinder;
    private int maxCompositeSize;
    /**
     *@param  maxCompositeSize  the maximum number of adjacent target features to
     *      try combining
     *@param  matchFinder       the FCMatchFinder to wrap
     */
    public TargetUnioningFCMatchFinder(int maxCompositeSize, FCMatchFinder matchFinder) {
        this.maxCompositeSize = maxCompositeSize;
        this.matchFinder = matchFinder;
    }
    @Override
    public Map match(
        FeatureCollection targetFC,
        FeatureCollection candidateFC,
        TaskMonitor monitor) {
        monitor.allowCancellationRequests();
        FeatureCollection compositeTargetFC = createCompositeFC(targetFC, monitor);
        Map compositeTargetFeatureToMatchesMap =
            matchFinder.match(compositeTargetFC, candidateFC, monitor);
        compositeTargetFeatureToMatchesMap =
            disambiguateCompositeTargetConstituents(
                compositeTargetFeatureToMatchesMap,
                candidateFC.getFeatureSchema(),
                monitor);
        createUnionIDs(compositeTargetFeatureToMatchesMap, monitor);                
        Map filteredTargetToMatchesMap =
            splitCompositeTargets(compositeTargetFeatureToMatchesMap, monitor);
        //Zero-score targets will have been filtered out. Put them back. [Jon Aquino]
        Map targetToMatchesMap =
            AreaFilterFCMatchFinder.blankTargetToMatchesMap(
                targetFC.getFeatures(),
                candidateFC.getFeatureSchema());
        targetToMatchesMap.putAll(filteredTargetToMatchesMap);
        return targetToMatchesMap;
    }
    private List lastTargetConstituents;
    private List lastUnionIDs;
    private void createUnionIDs(final Map compositeTargetFeatureToMatchesMap, TaskMonitor monitor) {
        monitor.report("Creating union IDs");
        ArrayList compositeTargets = new ArrayList(compositeTargetFeatureToMatchesMap.keySet());
        Collections.sort(compositeTargets, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                double s1 = ((Matches)compositeTargetFeatureToMatchesMap.get(o1)).getTopScore();
                double s2 = ((Matches)compositeTargetFeatureToMatchesMap.get(o2)).getTopScore();
                return s1 < s2 ? -1 : s1 > s2 ? 1 : 0;
            }
        });
        lastTargetConstituents = new ArrayList();
        lastUnionIDs = new ArrayList();
        int unionID = 0;
        for (int i = 0; i < compositeTargets.size(); i++) {
            monitor.report(i+1, compositeTargets.size(), "unions");
            CompositeFeature compositeTarget = (CompositeFeature) compositeTargets.get(i);
            if (compositeTarget.getFeatures().size() == 1) {
                continue;
            }
            unionID++;
            for (Iterator j = compositeTarget.getFeatures().iterator(); j.hasNext(); ) {
                Feature targetConstituent = (Feature) j.next();
                lastTargetConstituents.add(targetConstituent);
                lastUnionIDs.add(new Integer(unionID));
            }
        }
    }
    protected FeatureCollection createCompositeFC(
        FeatureCollection fc,
        TaskMonitor monitor) {
        FeatureCollection compositeFC = new FeatureDataset(fc.getFeatureSchema());
        Set composites = createCompositeSet(fc, monitor);
        add(composites, compositeFC, monitor);
        return new IndexedFeatureCollection(compositeFC);
    }
    /**
     * Returns a composite-target-to-Matches map in which each target constituent will be 
     * found in at most one composite target. Does not disambiguate composite targets
     * or matches (use DisambiguatingFCMatchFinder to do that), just composite target
     * constituents.
     */
    protected Map disambiguateCompositeTargetConstituents(
        Map compositeTargetToMatchesMap,
        FeatureSchema candidateSchema,
        TaskMonitor monitor) {
        ArrayList targetConstituentsEncountered = new ArrayList();
        ArrayList compositeTargets = new ArrayList();
        ArrayList candidates = new ArrayList();
        ArrayList scores = new ArrayList();
        SortedSet matchSet =
            DisambiguationMatch.createDisambiguationMatches(compositeTargetToMatchesMap, monitor);
        monitor.report("Discarding inferior composite matches");
        int j = 0;
        outer : for (Iterator i = matchSet.iterator(); i.hasNext();) {
            DisambiguationMatch match = (DisambiguationMatch) i.next();
            monitor.report(++j, matchSet.size(), "matches");
            for (Iterator k =
                ((CompositeFeature) match.getTarget()).getFeatures().iterator();
                k.hasNext();
                ) {
                Feature targetConstituent = (Feature) k.next();
                if (targetConstituentsEncountered.contains(targetConstituent)) {
                    continue outer;
                }
            }
            compositeTargets.add(match.getTarget());
            candidates.add(match.getCandidate());
            scores.add(new Double(match.getScore()));
            targetConstituentsEncountered.addAll(((CompositeFeature) match.getTarget()).getFeatures());
        }
        Map newMap = new HashMap();
        for (int i = 0; i < compositeTargets.size(); i++) {
            Matches matches = new Matches(candidateSchema);
            matches.add(
                (Feature) candidates.get(i),
                ((Double) scores.get(i)).doubleValue());
            newMap.put(compositeTargets.get(i), matches);
        }
        return newMap;
    }
    private List featuresWithCommonEdge(Feature feature, FeatureCollection fc) {
        ArrayList featuresWithCommonEdge = new ArrayList();
        List candidates = fc.query(feature.getGeometry().getEnvelopeInternal());
        for (Iterator i = candidates.iterator(); i.hasNext();) {
            Feature candidate = (Feature) i.next();
            if (feature == candidate
                || shareEdge(feature.getGeometry(), candidate.getGeometry())) {
                featuresWithCommonEdge.add(candidate);
            }
        }
        return featuresWithCommonEdge;
    }
    protected boolean shareEdge(Geometry a, Geometry b) {
        Set aEdges = edges(a);
        Set bEdges = edges(b);
        for (Iterator i = bEdges.iterator(); i.hasNext();) {
            Edge bEdge = (Edge) i.next();
            if (aEdges.contains(bEdge)) {
                return true;
            }
        }
        return false;
    }
    private static class Edge implements Comparable {
        private Coordinate p0, p1;
        public Edge(Coordinate a, Coordinate b) {
            if (a.compareTo(b) < 1) {
                p0 = a;
                p1 = b;
            } else {
                p0 = b;
                p1 = a;
            }
        }
        @Override
        public int compareTo(Object o) {
            Edge other = (Edge) o;
            int result = p0.compareTo(other.p0);
            if (result != 0)
                return result;
            return p1.compareTo(other.p1);
        }
    }
    private Set edges(Geometry g) {
        TreeSet edges = new TreeSet();
        for (Iterator i = CoordinateArrays.toCoordinateArrays(g, false).iterator();
            i.hasNext();
            ) {
            Coordinate[] coordinates = (Coordinate[]) i.next();
            for (int j = 1; j < coordinates.length; j++) { //1
                edges.add(new Edge(coordinates[j], coordinates[j - 1]));
            }
        }
        return edges;
    }
    /**
     *  Splits each composite target into its constituent features.
     */
    protected Map splitCompositeTargets(Map compositeToMatchesMap, TaskMonitor monitor) {
        monitor.report("Splitting composites");
        int compositesProcessed = 0;
        int totalComposites = compositeToMatchesMap.size();
        Map newMap = new HashMap();
        for (Iterator i = compositeToMatchesMap.keySet().iterator();
            i.hasNext() && !monitor.isCancelRequested();
            ) {
            CompositeFeature composite = (CompositeFeature) i.next();
            compositesProcessed++;
            monitor.report(compositesProcessed, totalComposites, "composites");
            Matches matches = (Matches) compositeToMatchesMap.get(composite);
            for (Iterator j = composite.getFeatures().iterator(); j.hasNext();) {
                Feature targetConstituent = (Feature) j.next();
                Assert.isTrue(!newMap.containsKey(targetConstituent));
                newMap.put(targetConstituent, matches.clone());
            }
        }
        return newMap;
    }
    private Set createCompositeSet(FeatureCollection fc, TaskMonitor monitor) {
        monitor.report("Creating composites of adjacent features");
        int featuresProcessed = 0;
        int totalFeatures = fc.getFeatures().size();
        //Use a Set to prevent duplicate composites [Jon Aquino]
        HashSet composites = new HashSet();
        for (Iterator i = fc.getFeatures().iterator();
            i.hasNext() && !monitor.isCancelRequested();
            ) {
            Feature feature = (Feature) i.next();
            featuresProcessed++;
            monitor.report(featuresProcessed, totalFeatures, "features");
            List featuresWithCommonEdge = featuresWithCommonEdge(feature, fc);
            for (Iterator j =
                CollectionUtil
                    .combinations(featuresWithCommonEdge, maxCompositeSize, feature)
                    .iterator();
                j.hasNext() && !monitor.isCancelRequested();
                ) {
                List combination = (List) j.next();
                composites.add(new CompositeFeature(fc.getFeatureSchema(), combination));
            }
        }
        return composites;
    }

    public static class CompositeFeature extends BasicFeature {
        private List features;
        private int hashCode;
        public CompositeFeature(FeatureSchema schema, List features) {
            super(schema);
            this.features = features;
            Geometry union = ((Feature) features.get(0)).getGeometry();
            hashCode = ((Feature) features.get(0)).hashCode();
            for (int i = 1; i < features.size(); i++) {
                Feature feature = (Feature) features.get(i);
                union = union.union(feature.getGeometry());
                hashCode = Math.min(hashCode, feature.hashCode());
            }
            setGeometry(union);
        }
        public List getFeatures() {
            return features;
        }
        @Override
        public boolean equals(Object obj) {
            Assert.isTrue(obj instanceof CompositeFeature, obj.getClass().toString());
            CompositeFeature other = (CompositeFeature) obj;
            if (features.size() != other.features.size()) {
                return false;
            }
            for (Iterator i = features.iterator(); i.hasNext();) {
                Feature myFeature = (Feature) i.next();
                if (!other.features.contains(myFeature)) {
                    return false;
                }
            }
            return true;
        }
        @Override
        public int hashCode() {
            return hashCode;
        }
    }
    private void add(Collection features, FeatureCollection fc, TaskMonitor monitor) {
        monitor.report("Building feature-collection");
        fc.addAll(features);
    }
    public Integer getUnionID(Feature target) {
        int i = lastTargetConstituents.indexOf(target);
        if (i == -1) { return null; }
        return (Integer)lastUnionIDs.get(i);
    }
}
