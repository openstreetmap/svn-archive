

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
package com.vividsolutions.jts.geom;

import java.io.Serializable;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;

/**
 * Represents a line segment defined by two {@link Coordinate}s.
 * Provides methods to compute various geometric properties
 * and relationships of line segments.
 * <p>
 * This class is designed to be easily mutable (to the extent of
 * having its contained points public).
 * This supports a common pattern of reusing a single LineSegment
 * object as a way of computing segment properties on the
 * segments defined by arrays or lists of {@link Coordinate}s.
 *
 *@version 1.7
 */
public class LineSegment
  implements Comparable<LineSegment>, Serializable
{
  private static final long serialVersionUID = 3252005833466256227L;

  public Coordinate p0, p1;

  public LineSegment(Coordinate p0, Coordinate p1) {
    this.p0 = p0;
    this.p1 = p1;
  }


  public LineSegment() {
    this(new Coordinate(), new Coordinate());
  }

  public Coordinate getCoordinate(int i)
  {
    if (i == 0) return p0;
    return p1;
  }



  /**
   * Computes the distance between this line segment and a given point.
   *
   * @return the distance from this segment to the given point
   */
  public double distance(Coordinate p)
  {
    return CGAlgorithms.distancePointLine(p, p0, p1);
  }



  /**
   * Computes the Projection Factor for the projection of the point p
   * onto this LineSegment.  The Projection Factor is the constant r
   * by which the vector for this segment must be multiplied to
   * equal the vector for the projection of <tt>p<//t> on the line
   * defined by this segment.
   * <p>
   * The projection factor returned will be in the range <tt>(-inf, +inf)</tt>.
   * 
   * @param p the point to compute the factor for
   * @return the projection factor for the point
   */
  public double projectionFactor(Coordinate p)
  {
    if (p.equals(p0)) return 0.0;
    if (p.equals(p1)) return 1.0;
    // Otherwise, use comp.graphics.algorithms Frequently Asked Questions method
    /*     	      AC dot AB
                   r = ---------
                         ||AB||^2
                r has the following meaning:
                r=0 P = A
                r=1 P = B
                r<0 P is on the backward extension of AB
                r>1 P is on the forward extension of AB
                0<r<1 P is interior to AB
        */
    double dx = p1.x - p0.x;
    double dy = p1.y - p0.y;
    double len2 = dx * dx + dy * dy;
    double r = ( (p.x - p0.x) * dx + (p.y - p0.y) * dy )
              / len2;
    return r;
  }


  /**
   * Compute the projection of a point onto the line determined
   * by this line segment.
   * <p>
   * Note that the projected point
   * may lie outside the line segment.  If this is the case,
   * the projection factor will lie outside the range [0.0, 1.0].
   */
  public Coordinate project(Coordinate p)
  {
    if (p.equals(p0) || p.equals(p1)) return new Coordinate(p);

    double r = projectionFactor(p);
    Coordinate coord = new Coordinate();
    coord.x = p0.x + r * (p1.x - p0.x);
    coord.y = p0.y + r * (p1.y - p0.y);
    return coord;
  }

  /**
   * Computes the closest point on this line segment to another point.
   * @param p the point to find the closest point to
   * @return a Coordinate which is the closest point on the line segment to the point p
   */
  public Coordinate closestPoint(Coordinate p)
  {
    double factor = projectionFactor(p);
    if (factor > 0 && factor < 1) {
      return project(p);
    }
    double dist0 = p0.distance(p);
    double dist1 = p1.distance(p);
    if (dist0 < dist1)
      return p0;
    return p1;
  }
  /**
   * Computes the closest points on two line segments.
   * 
   * @param line the segment to find the closest point to
   * @return a pair of Coordinates which are the closest points on the line segments
   */
  public Coordinate[] closestPoints(LineSegment line)
  {
    // test for intersection
    Coordinate intPt = intersection(line);
    if (intPt != null) {
      return new Coordinate[] { intPt, intPt };
    }

    /**
     *  if no intersection closest pair contains at least one endpoint.
     * Test each endpoint in turn.
     */
    Coordinate[] closestPt = new Coordinate[2];
    double minDistance = Double.MAX_VALUE;
    double dist;

    Coordinate close00 = closestPoint(line.p0);
    minDistance = close00.distance(line.p0);
    closestPt[0] = close00;
    closestPt[1] = line.p0;

    Coordinate close01 = closestPoint(line.p1);
    dist = close01.distance(line.p1);
    if (dist < minDistance) {
      minDistance = dist;
      closestPt[0] = close01;
      closestPt[1] = line.p1;
    }

    Coordinate close10 = line.closestPoint(p0);
    dist = close10.distance(p0);
    if (dist < minDistance) {
      minDistance = dist;
      closestPt[0] = p0;
      closestPt[1] = close10;
    }

    Coordinate close11 = line.closestPoint(p1);
    dist = close11.distance(p1);
    if (dist < minDistance) {
      minDistance = dist;
      closestPt[0] = p1;
      closestPt[1] = close11;
    }

    return closestPt;
  }

  /**
   * Computes an intersection point between two line segments, if there is one.
   * There may be 0, 1 or many intersection points between two segments.
   * If there are 0, null is returned. If there is 1 or more, 
   * exactly one of them is returned 
   * (chosen at the discretion of the algorithm).  
   * If more information is required about the details of the intersection,
   * the {@link RobustLineIntersector} class should be used.
   *
   * @param line a line segment
   * @return an intersection point, or <code>null</code> if there is none
   * 
   * @see RobustLineIntersector
   */
  public Coordinate intersection(LineSegment line)
  {
    LineIntersector li = new RobustLineIntersector();
    li.computeIntersection(p0, p1, line.p0, line.p1);
    if (li.hasIntersection())
      return li.getIntersection(0);
    return null;
  }
  
  /**
   *  Returns <code>true</code> if <code>other</code> has the same values for
   *  its points.
   *
   *@param  o  a <code>LineSegment</code> with which to do the comparison.
   *@return        <code>true</code> if <code>other</code> is a <code>LineSegment</code>
   *      with the same values for the x and y ordinates.
   */
  public boolean equals(Object o) {
    if (!(o instanceof LineSegment)) {
      return false;
    }
    LineSegment other = (LineSegment) o;
    return p0.equals(other.p0) && p1.equals(other.p1);
  }

  /**
   * Gets a hashcode for this object.
   * 
   * @return a hashcode for this object
   */
  public int hashCode() {
    long bits0 = java.lang.Double.doubleToLongBits(p0.x);
    bits0 ^= java.lang.Double.doubleToLongBits(p0.y) * 31;
    int hash0 = (((int) bits0) ^ ((int) (bits0  >> 32)));
    
    long bits1 = java.lang.Double.doubleToLongBits(p1.x);
    bits1 ^= java.lang.Double.doubleToLongBits(p1.y) * 31;
    int hash1 = (((int) bits1) ^ ((int) (bits1  >> 32)));

    // XOR is supposed to be a good way to combine hashcodes
    return hash0 ^ hash1;
  }

  /**
   *  Compares this object with the specified object for order.
   *  Uses the standard lexicographic ordering for the points in the LineSegment.
   *
   *@param  o  the <code>LineSegment</code> with which this <code>LineSegment</code>
   *      is being compared
   *@return    a negative integer, zero, or a positive integer as this <code>LineSegment</code>
   *      is less than, equal to, or greater than the specified <code>LineSegment</code>
   */
  public int compareTo(LineSegment o) {
    int comp0 = p0.compareTo(o.p0);
    if (comp0 != 0) return comp0;
    return p1.compareTo(o.p1);
  }

  public String toString()
  {
    return "LINESTRING( " +
        p0.x + " " + p0.y
        + ", " +
        p1.x + " " + p1.y + ")";
  }
}
