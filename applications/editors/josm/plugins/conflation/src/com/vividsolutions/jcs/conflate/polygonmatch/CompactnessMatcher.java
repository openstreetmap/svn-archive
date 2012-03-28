

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
import com.vividsolutions.jts.util.Assert;

/**
 * Uses (4 x pi x Area) / (Perimeter^2) as a shape characteristic. The
 * maximum value is 1 (for circles).
 */
public class CompactnessMatcher extends IndependentCandidateMatcher {

  public CompactnessMatcher() {
  }

  /**
   * @return 1 - the difference between the values of the shape
   * characteristic, defined above.
   */
  public double match(Geometry target, Geometry candidate) {
    double score = 1 - Math.abs(characteristic(target)
                              - characteristic(candidate));
    Assert.isTrue(score >= 0);
    Assert.isTrue(score <= 1);
    return score;
  }

  protected double characteristic(Geometry g) {
    return 4 * Math.PI * g.getArea() / Math.pow(g.getLength(), 2);
  }
}
