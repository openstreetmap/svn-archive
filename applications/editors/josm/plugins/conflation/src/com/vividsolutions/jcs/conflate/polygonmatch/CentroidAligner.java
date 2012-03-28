package com.vividsolutions.jcs.conflate.polygonmatch;

import com.vividsolutions.jts.geom.Geometry;

public class CentroidAligner extends IndependentCandidateMatcher {
    private IndependentCandidateMatcher matcher;
    public CentroidAligner(IndependentCandidateMatcher matcher) {
        this.matcher = matcher;
    }
    public double match(Geometry target, Geometry candidate) {
        return matcher.match(align(target), align(candidate));
    }
    private Geometry align(Geometry original) {
        Geometry aligned = (Geometry) original.clone();
        MatcherUtil.align(aligned, aligned.getCentroid().getCoordinate());
        return aligned;
    }
}
