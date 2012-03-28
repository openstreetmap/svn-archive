

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
 * Re-scales the scores output from another FeatureMatcher
 */
public class ScoreStretcher implements FeatureMatcher {

  /**
   * Creates a StretchFilter with the given control points.
   * @param minScore the score that will be warped to 0
   * @param maxScore the score that will be warped to 1
   */
  public ScoreStretcher(double minScore, double maxScore) {
    this.minScore = minScore;
    this.maxScore = maxScore;
  }

  private double minScore;
  private double maxScore;

  /**
   * Scales the scores so that #minScore becomes 0 and #maxScore
   * becomes 1. Scores outside of 0 and 1 get set to 0 and 1 respectively.
   * @param target ignored
   * @param candidates a Matches object created by another FeatureMatcher
   * @return the scaled scores
   */
    @Override
  public Matches match(Feature target, FeatureCollection candidates) {
    Matches oldMatches = (Matches) candidates;
    Matches newMatches = new Matches(candidates.getFeatureSchema());
    for (int i = 0; i < oldMatches.size(); i++) {
      newMatches.add(oldMatches.getFeature(i), convert(oldMatches.getScore(i)));
    }
    return newMatches;
  }

  private double convert(double oldScore) {
    //y = m x + b; v = m u + b
    double x = minScore, y = 0, u = maxScore, v = 1;
    double m = (y - v) / (x - u);
    double b = y - (m * x);
    return Math.min(1, Math.max(0, (m * oldScore) + b));
  }
}
