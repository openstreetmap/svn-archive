package com.tilusnet.josm.plugins.alignways.geometry;

public class AlignWaysGeomPoint {
    double x;
    double y;

    public AlignWaysGeomPoint(double x, double y) {
        setX(x);
        setY(y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

}
