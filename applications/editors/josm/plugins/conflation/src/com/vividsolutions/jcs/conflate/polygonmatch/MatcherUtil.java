

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
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.util.CoordinateArrays;
import java.util.Iterator;
import java.util.List;

/**
 * Functions useful to FeatureMatchers in general.
 */
public class MatcherUtil {

  /**
   * Moves g so that the outline centre-of-mass is at (0,0).
   * @param g the Geometry to modify
   */
  public static void alignByOutlineCentreOfMass(Geometry g) {
    align(g, outlineCentreOfMass(g));
  }

  /**
   * Moves g so that c is at (0,0).
   * @param g the Geometry to modify
   * @param c the point to move to the origin
   */
  public static void align(Geometry g, Coordinate c) {
    final Coordinate move = CoordUtil.subtract(new Coordinate(0,0), c);
    g.apply(new CoordinateFilter() {
            @Override
      public void filter(Coordinate coordinate) {
        coordinate.x += move.x;
        coordinate.y += move.y;
      }
    });
  }

  /**
   * Returns the centre-of-mass of g's line segments
   * @param g the Geometry to analyze
   * @return the weighted average of the midpoints of g's line segments,
   * weighted by segment length
   */
  public static Coordinate outlineCentreOfMass(Geometry g) {
    Coordinate weightedSum = new Coordinate();
    double totalLength = 0;
    List coordArrays = CoordinateArrays.toCoordinateArrays(g, false);
    for (Iterator i = coordArrays.iterator(); i.hasNext(); ) {
      Coordinate[] coords = (Coordinate[]) i.next();
      for (int j = 1; j < coords.length; j++) {
        double length = coords[j-1].distance(coords[j]);
        totalLength += length;
        weightedSum = CoordUtil.add(weightedSum, CoordUtil.multiply(length,
            CoordUtil.average(coords[j-1], coords[j])));
      }
    }
    return CoordUtil.divide(weightedSum, totalLength);
  }

  /**
   * Returns a FeatureMatcher score based on the symmetric difference
   * @param targetArea area of the target shape
   * @param candidateArea area of the candidate shape
   * @param symDiffArea area of the symmetric difference between the two shapes
   * @return a linear function of the symmetric difference: 1 if the shapes perfectly
   * overlap; 0 if the shapes do not overlap at all
   */
  public static double toScoreFromSymDiffArea(
      double targetArea, double candidateArea, double symDiffArea) {
    return 1d - (symDiffArea / (targetArea + candidateArea));
  }

}
