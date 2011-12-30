package com.tilusnet.josm.plugins.alignways.geometry;


/**
 * @author tilusnet <tilusnet@gmail.com>
 *
 */
public class AlignWaysGeomLine {

	double coef_a, coef_b, coef_c;

    public enum IntersectionStatus {
        UNDEFINED,
        INTERSECT_POINT,
        LINES_PARALLEL,
        LINES_OVERLAP
    }

    IntersectionStatus isectStat = IntersectionStatus.UNDEFINED;



    /**
     * Constructor defining the line with the coordinates of two points.
     * @param x1 x coordinate of point 1.
     * @param y1 y coordinate of point 1.
     * @param x2 x coordinate of point 2.
     * @param y2 y coordinate of point 2.
     */
    public AlignWaysGeomLine(double x1, double y1, double x2, double y2) {
        // ax + by + c = 0
        //
        //     y2 - y1
        // a = ------- (the slope);  b = -1; c = y1 - a*x1
        //     x2 - x1
        //
        // See conversion guidelines: http://www.webmath.com/equline1.html
        if (x1 == x2) {
            // Vertical line (would result div by zero if equation applied)
            coef_a = 1;
            coef_b = 0;
            coef_c = -x1;
        } else {
            coef_a = ((y2 - y1)/(x2 - x1));
            coef_b = -1;
            coef_c = y1 - coef_a * x1;
        }
    }

    /**
     * Constructor defining the line with the 3 coefficients of its equation, i.e. ax + by + c = 0.
     * @param a Coefficient a.
     * @param b Coefficient b.
     * @param c Coefficient c.
     */
    public AlignWaysGeomLine(double a, double b, double c) {
        coef_a = a;
        coef_b = b;
        coef_c = c;
    }

    /**
     * Constructor defining the line with the slope m and the y-intercept b, i.e. y = mx + b;
     * @param m Slope.
     * @param b Y-intercept.
     */
    public AlignWaysGeomLine(double m, double b) {
        coef_a = m;
        coef_b = -1;
        coef_c = b;
    }

    public AlignWaysGeomLine(AlignWaysGeomLine line) {
        this(line.coef_a, line.coef_b, line.coef_c);
    }

    /**
     * Returns the intersection point the line with another line.
     * If the lines are parallel or overlap, returns null.
     * Use getIntersectionStatus() to determine the case.
     * @param other_line The other line.
     * @return The intersection point of the lines.
     */
    public AlignWaysGeomPoint getIntersection(AlignWaysGeomLine other_line) {
        AlignWaysGeomPoint result = null;

        // Use Cramer-rule, i.e.:
        // - if (det1 != 0), there is an intersection in a point
        // - if (det1 == 0, det2 == 0, det3 == 0), the lines overlap
        // - if (det1 == 0) and any of det2 or det3 != 0, the lines are parallel

        // See: http://www.mathwizz.com/algebra/help/help21.htm
        //  and http://en.wikipedia.org/wiki/Cramers_rule


        double det1 = (coef_a * other_line.coef_b) - (other_line.coef_a * coef_b);
        double det2 = (-coef_c * other_line.coef_b) - (-other_line.coef_c * coef_b);
        double det3 = (coef_a * -other_line.coef_c) - (other_line.coef_a * -coef_c);

        if (Math.abs(det1) < 0.01) {
            if ((Math.abs(det2) < 0.01) && (Math.abs(det3) < 0.01)) {
                // Lines overlap
                isectStat = IntersectionStatus.LINES_OVERLAP;
            } else {
                // Lines are parallel
                isectStat = IntersectionStatus.LINES_PARALLEL;
            }
        } else {
            // Lines intersect in a point
            result = new AlignWaysGeomPoint(det2/det1, det3/det1);
            isectStat = IntersectionStatus.INTERSECT_POINT;
        }

        return result;
    }


    /**
     * Return the last result of getIntersection(), therefore use in conjuction with getIntersection().
     * If getIntersection() was never  called before, it returns IntersectionStatus.UNDEFINED.
     * @return The last result of getIntersection().
     */
    public IntersectionStatus getIntersectionStatus() {
        return isectStat;
    }

    /**
     * Get the Y coordinate on the line of a point with the given X coordinate.
     * 
     * @param X The x-coordinate of the given point.
     * @return The calculated y-coordinate or Double.NaN if the line is vertical.
     */
    public Double getYonLine(double X) {

        Double Y = new Double((-coef_a*X - coef_c)/coef_b);

        if (Y.isInfinite() || Y.isNaN())
            // Vertical line
            return Double.NaN;
        else
            return Y;

    }


    /**
     * Get the X coordinate on the line of a point with the given Y coordinate.
     * 
     * @param Y The y-coordinate of the given point.
     * @return The calculated x-coordinate or Double.NaN if the line is horizontal.
     */
    public Double getXonLine(double Y) {

        Double X = new Double((-coef_b*Y - coef_c)/coef_a);

        if (X.isInfinite() || X.isNaN())
            // Horizontal line
            return Double.NaN;
        else
            return X;

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AlignWaysGeomLine))
            return false;
        AlignWaysGeomLine other = (AlignWaysGeomLine) obj;

        if (Math.abs(this.coef_a - other.coef_a) < 0.01 &&
                Math.abs(this.coef_b - other.coef_b) < 0.01 &&
                Math.abs(this.coef_c - other.coef_c) < 0.01)
            return true;
        else
            return false;
    }

}
