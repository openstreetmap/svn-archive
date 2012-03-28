

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

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Applies a FeatureMatcher to each item in a FeatureCollection
 */
public class BasicFCMatchFinder implements FCMatchFinder {

    /**
     * Creates a FeatureCollectionMatcher that uses the given FeatureMatcher.
     * @param matcher typically a composite of other FeatureMatchers
     */
    public BasicFCMatchFinder(FeatureMatcher matcher) {
        this.matcher = matcher;
    }

    private FeatureMatcher matcher;

    @Override
    public Map match(
        FeatureCollection targetFC,
        FeatureCollection candidateFC,
        TaskMonitor monitor) {
        monitor.allowCancellationRequests();
        monitor.report("Finding matches");
        TreeMap map = new TreeMap();
        int featuresProcessed = 0;
        int totalFeatures = targetFC.size();
        for (Iterator i = targetFC.iterator();
            i.hasNext() && !monitor.isCancelRequested();
            ) {
            Feature subjectFeature = (Feature) i.next();
            featuresProcessed++;
            monitor.report(featuresProcessed, totalFeatures, "features");
            map.put(subjectFeature, matcher.match(subjectFeature, candidateFC));
        }
        return map;
    }
}
