

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

/**
 * Filters out shapes with a score below a given value.
 */
public class ThresholdFilter implements FeatureMatcher {

  /**
   * Creates a ThresholdFilter with the given minimum score.
   * @param minScore the score below which shapes will be filtered out
   */
  public ThresholdFilter(double minScore) {
    this.minScore = minScore;
  }

  private double minScore;

  /**
   * Filters out shapes with a score below the minimum score threshold.
   * @param target ignored
   * @param candidates a Matches object created by another FeatureMatcher
   * @return the candidates having a score greater than or equal to the
   * threshold score. The scores are preserved from the original Matches
   * object.
   */
    @Override
  public Matches match(Feature target, FeatureCollection candidates) {
    Matches survivors = new Matches(candidates.getFeatureSchema());
    Matches allMatches = (Matches) candidates;
    for (int i = 0; i < allMatches.size(); i++) {
      if (allMatches.getScore(i) >= minScore) {
        survivors.add(allMatches.getFeature(i), allMatches.getScore(i));
      }
    }
    return survivors;
  }
}
