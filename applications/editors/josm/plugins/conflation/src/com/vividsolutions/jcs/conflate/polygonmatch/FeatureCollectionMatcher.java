

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

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Applies a FeatureMatcher to each item in a FeatureCollection
 */
public class FeatureCollectionMatcher {

  /**
   * Creates a FeatureCollectionMatcher that uses the given FeatureMatcher.
   * @param matcher typically a composite of other FeatureMatchers
   */
  public FeatureCollectionMatcher(FeatureMatcher matcher) {
    this.matcher = matcher;
  }

  private FeatureMatcher matcher;

  /**
   * For each target feature, finds matches among the candidate features.
   * @return a map of target-feature to matching-features (a Matches object)
   */
  public Map match(FeatureCollection targetFC, FeatureCollection candidateFC) {
    TreeMap map = new TreeMap();
    for (Iterator i = targetFC.iterator(); i.hasNext(); ) {
      Feature subjectFeature = (Feature) i.next();
      map.put(subjectFeature, matcher.match(subjectFeature, candidateFC));
    }
    return map;
  }
}
