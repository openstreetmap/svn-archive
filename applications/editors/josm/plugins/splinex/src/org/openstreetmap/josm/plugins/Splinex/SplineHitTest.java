// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.Splinex;

import org.openstreetmap.josm.tools.Logging;

public class SplineHitTest {
    double x, y, dist, distSq;
    //public int chkCnt;

    public void setCoord(double x, double y, double dist) {
        this.x = x;
        this.y = y;
        this.dist = dist;
        distSq = dist * dist;
    }

    private double sqr(double x) {
        return x * x;
    }

    public boolean checkPoint(double x1, double y1) {
        return sqr(x1 - x) + sqr(y1 - y) <= distSq;
    }

    static final double pixTolerance = 1;

    public boolean checkCurve(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        return checkCurve(x1, y1, x2, y2, x3, y3, x4, y4, 32);
    }
    public boolean checkCurve(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, int depth) {
        if (Logging.isDebugEnabled()) {
            Logging.debug("checkCurve {0} {1} {2} {3} {4} {5} {6} {7}", x1, y1, x2, y2, x3, y3, x4, y4);
        }
        //chkCnt++;
        double dx = x4 - x1;
        double dy = y4 - y1;
        double dl = (x - x1) * dx + (y - y1) * dy;
        double lenSq = (dx * dx + dy * dy);

        double dp = (x - x1) * dy - (y - y1) * dx;
        boolean forcesplit = false;
        if (dl < 0) {
            double dl2 = ((x2 - x1) * dx + (y2 - y1) * dy) * 0.75;
            double dl3 = ((x3 - x1) * dx + (y3 - y1) * dy) * 0.75;
            if ((dl2 > 0 && dl3 > 0)
                    || (dl < dl2 && sqr(dl - dl2) > lenSq * distSq && dl < dl3 && sqr(dl - dl3) > lenSq * distSq))
                return false;
            forcesplit = true;
        }
        if (dl > lenSq) {
            dl = (x - x4) * dx + (y - y4) * dy;
            double dl2 = ((x2 - x4) * dx + (y2 - y4) * dy) * 0.75;
            double dl3 = ((x3 - x4) * dx + (y3 - y4) * dy) * 0.75;
            if ((dl2 < 0 && dl3 < 0)
                    || (dl > dl2 && sqr(dl - dl2) > lenSq * distSq && dl > dl3 && sqr(dl - dl3) > lenSq * distSq))
                return false;
            forcesplit = true;
        }
        double d2 = ((x2 - x1) * dy - (y2 - y1) * dx) * 0.75;
        double d3 = ((x3 - x1) * dy - (y3 - y1) * dx) * 0.75;
        if (sqr(Math.abs(d2) + Math.abs(d3)) < pixTolerance * lenSq && !forcesplit)
            return dp * dp <= distSq * lenSq;
        if (dp * dp > distSq * lenSq) {
            if ((Math.signum(d2) != Math.signum(dp) || (Math.abs(dp) > Math.abs(d2) && sqr(dp - d2) > lenSq * distSq))
             && (Math.signum(d3) != Math.signum(dp) || (Math.abs(dp) > Math.abs(d3) && sqr(dp - d3) > lenSq * distSq)))
                return false;
        }
        double x12 = (x1 + x2) / 2;
        double y12 = (y1 + y2) / 2;
        double x23 = (x2 + x3) / 2;
        double y23 = (y2 + y3) / 2;
        double x34 = (x3 + x4) / 2;
        double y34 = (y3 + y4) / 2;
        double x123 = (x12 + x23) / 2;
        double y123 = (y12 + y23) / 2;
        double x234 = (x23 + x34) / 2;
        double y234 = (y23 + y34) / 2;
        double x1234 = (x123 + x234) / 2;
        double y1234 = (y123 + y234) / 2;
        if (checkPoint(x1234, y1234))
            return true;
        if (depth <= 0)
            return false;
        return checkCurve(x1, y1, x12, y12, x123, y123, x1234, y1234, depth - 1)
            || checkCurve(x1234, y1234, x234, y234, x34, y34, x4, y4, depth - 1);
    }
}
