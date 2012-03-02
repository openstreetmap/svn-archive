package org.openstreetmap.josm.plugins.conflation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * This class represents a potential match, i.e. a pair of primitives and
 * related information.
 */
public class ConflationCandidate {

    OsmPrimitive referenceObject;
    OsmDataLayer referenceLayer;
    OsmPrimitive subjectObject;
    OsmDataLayer subjectLayer;
    double cost;
    double distance;

    public ConflationCandidate(OsmPrimitive referenceObject, OsmDataLayer referenceLayer,
            OsmPrimitive subjectObject, OsmDataLayer subjectLayer, double cost) {
        if (referenceObject == null || subjectObject == null) {
            throw new IllegalArgumentException(tr("Invalid reference or subject"));
        }
        this.referenceObject = referenceObject;
        this.referenceLayer = referenceLayer;
        this.subjectObject = subjectObject;
        this.subjectLayer = subjectLayer;
        this.cost = cost;
        // TODO: use distance calculated in cost function, and make sure it's in meters?
        this.distance = ConflationUtils.getCenter(referenceObject).distance(ConflationUtils.getCenter(subjectObject));
    }

    public OsmPrimitive getReferenceObject() {
        return referenceObject;
    }
    
    public OsmDataLayer getReferenceLayer() {
        return referenceLayer;
    }
    
    public OsmDataLayer getSubjectLayer() {
        return subjectLayer;
    }

    public OsmPrimitive getSubjectObject() {
        return subjectObject;
    }

    public Object getCost() {
        return cost;
    }

    public Object getDistance() {
        return distance;
    }
}