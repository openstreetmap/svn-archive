

/*
 * The Java Conflation Suite (JCS) is a library of Java classes that
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
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.CoordinateArrays;
import java.util.*;

/**
 *  An FCMatchFinder wrapper that also treats pairs of adjacent target features
 *  as themselves target features. Such pairs are formed into composite target
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
public class CombinatorialFCMatchFinder implements FCMatchFinder {

  private FCMatchFinder matchFinder;

  private int maxCompositeSize;

  /**
   *@param  maxCompositeSize  the maximum number of adjacent target features to
   *      try combining
   *@param  matchFinder       the FCMatchFinder to wrap
   */
  public CombinatorialFCMatchFinder(int maxCompositeSize, FCMatchFinder matchFinder) {
    this.maxCompositeSize = maxCompositeSize;
    this.matchFinder = new OneToOneFCMatchFinder(matchFinder);
  }

  public Map match(IndexedFeatureCollection targetFC, IndexedFeatureCollection candidateFC,
      TaskMonitor monitor) {
    monitor.allowCancellationRequests();
    FeatureCollection compositeTargetFC = new FeatureDataset(targetFC.getFeatureSchema());
    CollectionMap constituentToCompositesMap = new CollectionMap();
    createComposites(targetFC, constituentToCompositesMap, compositeTargetFC, monitor);
    Map targetFeatureToMatchesMap = matchFinder.match(
        new IndexedFeatureCollection(compositeTargetFC),
        candidateFC, monitor);
    deleteInferiorComposites(targetFeatureToMatchesMap, constituentToCompositesMap, monitor);
    return splitComposites(targetFeatureToMatchesMap, monitor);
  }

  protected void createComposites(FeatureCollection fc, CollectionMap constituentToCompositesMap, FeatureCollection compositeFC, TaskMonitor monitor) {
    Assert.isTrue(constituentToCompositesMap.isEmpty());
    Assert.isTrue(compositeFC.isEmpty());
    Set composites = createCompositeSet(fc, monitor);
    add(composites, constituentToCompositesMap, monitor);
    add(composites, compositeFC, monitor);
  }

  /**
   *  Removes from the compositeToMatchesMap any composites sharing constituents
   *  with other composites but having a lower match score than any of the other
   *  composites.
   */
  protected void deleteInferiorComposites(Map compositeToMatchesMap, CollectionMap constituentToCompositesMap, TaskMonitor monitor) {
    monitor.report("Discarding inferior composites");
    int featuresProcessed = 0;
    int totalFeatures = constituentToCompositesMap.size();
    for (Iterator i = constituentToCompositesMap.keySet().iterator(); i.hasNext() && ! monitor.isCancelRequested(); ) {
      Feature constituent = (Feature) i.next();
      featuresProcessed++;
      monitor.report(featuresProcessed, totalFeatures, "features");
      Collection composites = constituentToCompositesMap.getItems(constituent);
      Assert.isTrue(!composites.isEmpty());
      double bestScore = -1;
      CompositeFeature bestComposite = null;
      Matches bestMatches = null;
      for (Iterator j = composites.iterator(); j.hasNext(); ) {
        CompositeFeature composite = (CompositeFeature) j.next();
        Matches matches = (Matches) compositeToMatchesMap.get(composite);
        if (matches == null) {
          continue;
        }
        if (matches.getTopScore() > bestScore) {
          bestScore = matches.getTopScore();
          bestComposite = composite;
          bestMatches = matches;
        }
      }
      CollectionUtil.removeKeys(composites, compositeToMatchesMap);
      if (bestMatches == null) {
        continue;
      }
      compositeToMatchesMap.put(bestComposite, bestMatches);
    }
  }

  protected List featuresWithCommonEdge(Feature feature, FeatureCollection fc) {
    ArrayList featuresWithCommonEdge = new ArrayList();
    List candidates = fc.query(feature.getGeometry().getEnvelopeInternal());
    for (Iterator i = candidates.iterator(); i.hasNext(); ) {
      Feature candidate = (Feature) i.next();
      if (feature == candidate || shareEdge(feature.getGeometry(), candidate.getGeometry())) {
        featuresWithCommonEdge.add(candidate);
      }
    }
    return featuresWithCommonEdge;
  }

  protected boolean shareEdge(Geometry a, Geometry b) {
    Set aEdges = edges(a);
    Set bEdges = edges(b);
    for (Iterator i = bEdges.iterator(); i.hasNext(); ) {
      Edge bEdge = (Edge) i.next();
      if (aEdges.contains(bEdge)) { return true; }
    }
    return false;
  }

    @Override
    public Map match(FeatureCollection targetFC, FeatureCollection candidateFC, TaskMonitor monitor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

  private static class Edge implements Comparable {
    private Coordinate p0, p1;
    public Edge(Coordinate a, Coordinate b) {
      if (a.compareTo(b) < 1) {
        p0 = a;
        p1 = b;
      }
      else {
        p0 = b;
        p1 = a;
      }
    }
        @Override
    public int compareTo(Object o) {
      Edge other = (Edge) o;
      int result = p0.compareTo(other.p0);
      if (result != 0) return result;
      return p1.compareTo(other.p1);
    }
  }

  private Set edges(Geometry g) {
    TreeSet edges = new TreeSet();
    for (Iterator i = CoordinateArrays.toCoordinateArrays(g, false).iterator(); i.hasNext(); ) {
      Coordinate[] coordinates = (Coordinate[]) i.next();
      for (int j = 1; j < coordinates.length; j++) { //1
        edges.add(new Edge(coordinates[j], coordinates[j-1]));
      }
    }
    return edges;
  }

  /**
   *  Splits each composite target into its constituent features.
   */
  protected Map splitComposites(Map compositeToMatchesMap, TaskMonitor monitor) {
    monitor.report("Splitting composites");
    int compositesProcessed = 0;
    int totalComposites = compositeToMatchesMap.size();
    Map newMap = new HashMap();
    for (Iterator i = compositeToMatchesMap.keySet().iterator(); i.hasNext() && ! monitor.isCancelRequested(); ) {
      CompositeFeature composite = (CompositeFeature) i.next();
      compositesProcessed++;
      monitor.report(compositesProcessed, totalComposites, "composites");
      Matches matches = (Matches) compositeToMatchesMap.get(composite);
      //Because we use OneToOneFCMatchFinder, all targets will be associated
      //with one and only one match.
      Assert.isTrue(1 == matches.size());
      for (Iterator j = composite.getFeatures().iterator(); j.hasNext(); ) {
        Feature constituent = (Feature) j.next();
        Assert.isTrue(!newMap.containsKey(constituent));
        Matches matchesCopy = new Matches(matches.getFeatureSchema());
        matchesCopy.add(matches.getTopMatch(), matches.getTopScore());
        newMap.put(constituent, matchesCopy);
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
    for (Iterator i = fc.getFeatures().iterator(); i.hasNext() && !monitor.isCancelRequested(); ) {
      Feature feature = (Feature) i.next();
      featuresProcessed++;
      monitor.report(featuresProcessed, totalFeatures, "features");
      List featuresWithCommonEdge = featuresWithCommonEdge(feature, fc);
      for (Iterator j = CollectionUtil.combinations(
          featuresWithCommonEdge, maxCompositeSize, feature).iterator(); j.hasNext() && !monitor.isCancelRequested(); ) {
        List combination = (List) j.next();
        composites.add(new CompositeFeature(fc.getFeatureSchema(), combination));
      }
    }
    return composites;
  }

  private void add(Set composites, CollectionMap constituentToCompositesMap, TaskMonitor monitor) {
    monitor.report("Creating feature-to-composite map");
    int compositesProcessed = 0;
    int totalComposites = composites.size();
    for (Iterator i = composites.iterator(); i.hasNext() && !monitor.isCancelRequested(); ) {
      CompositeFeature composite = (CompositeFeature) i.next();
      compositesProcessed++;
      monitor.report(compositesProcessed, totalComposites, "composites");
      for (Iterator j = composite.getFeatures().iterator(); j.hasNext() && !monitor.isCancelRequested(); ) {
        Feature constituent = (Feature) j.next();
        constituentToCompositesMap.addItem(constituent, composite);
      }
    }
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
      for (Iterator i = features.iterator(); i.hasNext(); ) {
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

}
