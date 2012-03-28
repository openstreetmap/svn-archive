

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

import com.vividsolutions.jts.geom.Geometry;

/**
 * Uses symmetric difference as the criterion for determining match scores.
 */
public class SymDiffMatcher extends IndependentCandidateMatcher {

  public SymDiffMatcher() {
  }

  /**
   * The score is a linear function of the symmetric difference: 1 if the shapes perfectly
   * overlap; 0 if the shapes do not overlap at all.
   * @param target the feature to match
   * @param candidate the feature to compare with the target
   * @return candidates with a score greater than 0 (typically all the candidates).
   */
  public double match(Geometry target, Geometry candidate) {
    Geometry targetGeom = (Geometry) target.clone();
    Geometry candidateGeom = (Geometry) candidate.clone();
    if (targetGeom.isEmpty() || candidateGeom.isEmpty()) {
      return 0; //avoid div by 0 in centre-of-mass calc [Jon Aquino]
    }
    return MatcherUtil.toScoreFromSymDiffArea(
        targetGeom.getArea(), candidateGeom.getArea(),
        targetGeom.symDifference(candidateGeom).getArea());
  }
}
