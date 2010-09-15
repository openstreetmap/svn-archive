
// License: GPL. Copyright 2007 by Brent Easton

package org.openstreetmap.josm.plugins.duplicateway;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;

/**
 *
 * A class encapsulating a line Segment treating it as a Vector (
 * direction and length). Includes Utility routines to perform various
 * transformations and Trigonometric operations
 *
 * @author Brent Easton
 *
 */
public class JVector extends Line2D.Double {

  public static final double EARTH_CIRCUMFERENCE = 40041455;
  protected static final double PI_ON_2 = Math.PI / 2.0;
  protected Point2D.Double slopeIntercept = null;
  protected Point2D.Double rtheta = null;

  /**
   * Create new JVector joining two points
   *
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   */
  public JVector(double x1, double y1, double x2, double y2) {
    super(x1, y1, x2, y2);
  }

  /**
   * Create new JVector from another JVector
   *
   * @param v JVector to copy
   */
  public JVector(JVector v) {
    super();
    x1 = v.x1;
    x2 = v.x2;
    y1 = v.y1;
    y2 = v.y2;
  }

  /**
   * Create a new JVector based on a JOSM Segment object
   *
   * @param s
   */
  public JVector (Segment s) {
    super(s.from.eastNorth.east(), s.from.eastNorth.north(), s.to.eastNorth.east(), s.to.eastNorth.north());
  }

  /**
   * Calculate slope/intersect from cartesian co-ords
   *
   */
  protected void calculateSlopeIntercept() {
    double slope = (y2 - y1) /(x2 - x1);
    double intersect = y1 - (slope * x1);
    slopeIntercept = new Point2D.Double(slope, intersect);
  }

  /**
   * Return the slope of the JVector
   * @return slope
   */
  public double getSlope() {
    if (slopeIntercept == null) {
      calculateSlopeIntercept();
    }
    return slopeIntercept.x;
  }

  /**
   * Return the Y intercept of the JVector
   * @return intercept
   */
  public double getIntercept() {
    if (slopeIntercept == null) {
      calculateSlopeIntercept();
    }
    return slopeIntercept.y;
  }

  /**
   * Calculate the polar coordinates for this line as a ray with
   * the from point as origin
   */
  protected void calculatePolar() {
    double x = x2 - x1;
    double y = y2 - y1;
    double r = Math.sqrt(x * x + y * y);
    double theta = Math.atan2(y, x);
    rtheta = new Point2D.Double(r, theta);
  }

  /**
   * Return the length of the line segment
   * @return length
   */
  public double getLength() {
    if (rtheta == null) {
      calculatePolar();
    }
    return rtheta.x;
  }

  /**
   * Return the angle of the line segment
   * @return theta
   */
  public double getTheta() {
    if (rtheta == null) {
      calculatePolar();
    }
    return rtheta.y;
  }

  /**
   * Convert Polar co-ords to cartesian
   */
  protected void polarToCartesian() {
    double newx2 = x1 + getLength() * Math.cos(getTheta());
    double newy2 = y1 + getLength() * Math.sin(getTheta());
    x2 = newx2;
    y2 = newy2;
    slopeIntercept = null;
  }

  /**
   * Set the line segment using Polar co-ordinates
   *
   * @param r line length
   * @param theta angle
   */
  protected void setPolar(double r, double theta) {
    rtheta = new Point2D.Double(r, theta);
    polarToCartesian();
  }

  /**
   * Set the length of the line segment
   * @param l length
   */
  protected void setLength(double l) {
    rtheta.x = l;
    polarToCartesian();
  }

  /**
   * Reverse the direction of the segment
   */
  public void reverse() {
    double t = x2;
    x2 = x1;
    x1 = t;
    t = y2;
    y2 = y1;
    y1 = t;
    slopeIntercept = null;
    rtheta = null;
  }

  /**
   * Rotate the line segment about the origin
   * @param rot angle to rotate
   */
  protected void rotate(double rot) {
    if (rtheta == null) {
       calculatePolar();
    }
    rtheta.y = normalize(rtheta.y + rot);
    polarToCartesian();
  }

  /**
   * Rotate 90 degrees clockwise
   *
   */
  protected void rotate90CW() {
    rotate(-PI_ON_2);
  }

  /**
   * Rotate 90 degrees counterclockwise
   *
   */
  protected void rotate90CCW() {
    rotate(PI_ON_2);
  }

  /**
   * Normalize theta to be in the range -PI < theta < PI
   * @param theta
   * @return normalized angle
   */
  protected double normalize(double theta) {
    while (theta < -Math.PI || theta > Math.PI) {
      if (theta > Math.PI) {
        return (theta - 2 * Math.PI);
      }
      if (theta < -Math.PI) {
        return (theta + 2 * Math.PI);
      }
    }
    return theta;
  }

  /**
   * Rotate the line segment 90 degrees and set the length.
   * @param offset length
   */
  protected void rotate90(double offset) {
    rotate(PI_ON_2 * (offset < 0 ? 1 : -1));
    setLength(Math.abs(offset));
  }

  /*
   * Return the distance of the given point from this line. Offset is
   * -ve if the point is to the left of the line, or +ve if to the right
   */


  /**
   * Return the distance of the given point from this line. Offset is
   * -ve if the point is to the left of the line, or +ve if to the right
   * @param target
   */
  protected double calculateOffset(EastNorth target) {

    // Calculate the perpendicular interceptor to this point
    EastNorth intersectPoint = perpIntersect(target);
    JVector intersectRay = new JVector(intersectPoint.east(), intersectPoint.north(), target.east(), target.north());

    // Offset is equal to the length of the interceptor
    double offset = intersectRay.getLength();

    // Check the angle between this line and the interceptor to calculate left/right
    double theta = normalize(getTheta() - intersectRay.getTheta());
    if (theta < 0) {
      offset = -offset;
    }

    return offset;
  }


  /**
   * Return the Perpendicular distance between a point
   * and this line. Units is degrees.
   * @param n Node
   */
  public double perpDistance(Node n) {
    return perpDistance(n.eastNorth);
  }

  /**
   * Calculate the perpendicular distance from the supplied
   * point to this line
   * @param en point
   * @return distance
   */
  public double perpDistance(EastNorth en) {
   return ptLineDist(en.east(), en.north());
  }

  /**
   * Calculate the perpendicular distance from the given point
   * to a JOSM segment.
   * @param s
   * @param en
   * @return
   */
  public static double perpDistance(Segment s, EastNorth en) {
   return Line2D.ptSegDist(s.from.eastNorth.east(), s.from.eastNorth.north(), s.to.eastNorth.east(), s.to.eastNorth.north(), en.east(), en.north());
  }

  /**
   * Calculate the bisector between this and another Vector. A positive offset means
   * the bisector must point to the right of the Vectors.
   * @param ls Other vector to create angle to bisect
   * @param offset length of bisecing vector
   * @return the bisecting vector
   */
  public JVector bisector(JVector ls, double offset) {
    JVector newSeg = new JVector(ls);
    double newTheta = Math.PI + ls.getTheta() - getTheta();
    newSeg.setPolar(Math.abs(offset), newSeg.getTheta() - newTheta/2.0);

    double angle = normalize(getTheta() - newSeg.getTheta());
    if ((angle < 0 && offset > 0) || (angle > 0 && offset < 0)) {
      newSeg.rotate(Math.PI);
    }
    return newSeg;
  }

  /**
   * Return the Perpendicular Intersector from a point to this line
   * @param n a JOSM node
   * @return the point on our line closest to n
   */
  public EastNorth perpIntersect(Node n) {
    return perpIntersect(n.eastNorth);
  }

  /**
   * Calculate the point on our line closest to the supplied point
   * @param en point
   * @return return closest point
   */
  public EastNorth perpIntersect(EastNorth en) {

    /*
     * Calculate the coefficients for the two lines
     *  1. The segment: y = ax + b
     *  2. The perpendicular line through the new point: y = cx + d
     */
    double perpSlope = -1 / getSlope();
    double perpIntercept = en.north() - (en.east() * perpSlope);

    /*
     * Solve the simultaneous equation to calculate the intersection
     *  ax+b = cx+d
     *  ax - cx = d - b
     *  x (a-c) = d - b
     *  x = (d - b) / (a - c)
     */
    double intersectE = (perpIntercept - getIntercept()) / (getSlope() - perpSlope);
    double intersectN = intersectE * getSlope() + getIntercept();

    return new EastNorth(intersectE, intersectN);
  }
}