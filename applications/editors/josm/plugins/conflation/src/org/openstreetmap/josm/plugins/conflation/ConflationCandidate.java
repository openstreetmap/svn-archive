package org.openstreetmap.josm.plugins.conflation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * This class represents a potential match, i.e. a pair of primitives and
 * related information.
 */
public class ConflationCandidate {

    OsmPrimitive sourcePrimitive;
    OsmDataLayer sourceLayer;
    OsmPrimitive targetPrimitive;
    OsmDataLayer targetLayer;
    double cost;
    double distance;

    public ConflationCandidate(OsmPrimitive source, OsmDataLayer sourceLayer,
            OsmPrimitive target, OsmDataLayer targetLayer, double cost) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Invalid source or target");
        }
        this.sourcePrimitive = source;
        this.sourceLayer = sourceLayer;
        this.targetPrimitive = target;
        this.targetLayer = targetLayer;
        this.cost = cost;
        // TODO: use distance calculated in cost function, and make sure it's in meters?
        this.distance = ConflationUtils.getCenter(source).distance(ConflationUtils.getCenter(target));
    }

    public OsmPrimitive getSourcePrimitive() {
        return sourcePrimitive;
    }
    
    public OsmDataLayer getSourceLayer() {
        return sourceLayer;
    }
    
    public OsmDataLayer getTargetLayer() {
        return targetLayer;
    }

    public OsmPrimitive getTargetPrimitive() {
        return targetPrimitive;
    }

    public Object getCost() {
        return cost;
    }

    public Object getDistance() {
        return distance;
    }
}