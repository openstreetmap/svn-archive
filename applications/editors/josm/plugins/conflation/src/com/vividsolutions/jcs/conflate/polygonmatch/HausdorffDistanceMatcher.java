package com.vividsolutions.jcs.conflate.polygonmatch;

import com.vividsolutions.jcs.algorithm.VertexHausdorffDistance;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Uses an approximation of the Hausdorff distance.
 * @see VertexHausdorffDistance
 */
public class HausdorffDistanceMatcher extends AbstractDistanceMatcher {

    protected double distance(Geometry target, Geometry candidate) {
        return new VertexHausdorffDistance(target, candidate).distance();
    }
    
}
