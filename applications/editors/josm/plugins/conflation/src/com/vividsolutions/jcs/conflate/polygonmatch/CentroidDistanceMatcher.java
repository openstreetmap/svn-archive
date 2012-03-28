package com.vividsolutions.jcs.conflate.polygonmatch;

import com.vividsolutions.jts.geom.Geometry;

public class CentroidDistanceMatcher extends AbstractDistanceMatcher {
    protected double distance(Geometry target, Geometry candidate) {
        return target.getCentroid().distance(
            candidate.getCentroid());
    }
}
