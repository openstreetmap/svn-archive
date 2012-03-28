package com.vividsolutions.jcs.conflate.polygonmatch;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.awt.geom.Point2D;

public abstract class AbstractDistanceMatcher extends IndependentCandidateMatcher {
    protected double maxDistance = 0;
    
    @Override
    public double match(Geometry target, Geometry candidate) {
        double distance = distance(target, candidate);
        if (maxDistance > 0) {
            return 1 - (distance / maxDistance); // FIXME: allow negative scores?
        } else {
            return 1
                    - (distance
                    / combinedEnvelopeDiagonalDistance(target, candidate));
        }
    }

    protected abstract double distance(Geometry target, Geometry candidate);

    private double combinedEnvelopeDiagonalDistance(
        Geometry target,
        Geometry candidate) {
        Envelope envelope = new Envelope(target.getEnvelopeInternal());
        envelope.expandToInclude(candidate.getEnvelopeInternal());
        return Point2D.distance(
            envelope.getMinX(),
            envelope.getMinY(),
            envelope.getMaxX(),
            envelope.getMaxY());
    }

    public void setMaxDistance(double maxDistance) {
        if (maxDistance < 0)
            this.maxDistance = 0;
        else
            this.maxDistance = maxDistance;
    }
}