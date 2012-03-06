


/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.geomgraph;

/**
 * @version 1.7
 */
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Utility functions for working with quadrants, which are numbered as follows:
 * <pre>
 * 1 | 0
 * --+--
 * 2 | 3
 * <pre>
 *
 * @version 1.7
 */
public class Quadrant 
{
	public static final int NE = 0;
	public static final int NW = 1;
	public static final int SW = 2;
	public static final int SE = 3;
	
  /**
   * Returns the quadrant of a directed line segment (specified as x and y
   * displacements, which cannot both be 0).
   * 
   * @throws IllegalArgumentException if the displacements are both 0
   */
  public static int quadrant(double dx, double dy)
  {
    if (dx == 0.0 && dy == 0.0)
      throw new IllegalArgumentException("Cannot compute the quadrant for point ( "+ dx + ", " + dy + " )" );
    if (dx >= 0.0) {
      if (dy >= 0.0)
        return NE;
      else
        return SE;
    }
    else {
    	if (dy >= 0.0)
    		return NW;
    	else
    		return SW;
    }
  }

  /**
   * Returns the quadrant of a directed line segment from p0 to p1.
   * 
   * @throws IllegalArgumentException if the points are equal
   */
  public static int quadrant(Coordinate p0, Coordinate p1)
  {
    if (p1.x == p0.x && p1.y == p0.y)
      throw new IllegalArgumentException("Cannot compute the quadrant for two identical points " + p0);
    
    if (p1.x >= p0.x) {
      if (p1.y >= p0.y)
        return NE;
      else
        return SE;
    }
    else {
    	if (p1.y >= p0.y)
    		return NW;
    	else
    		return SW;
    }
  }
    
}
