package org.openstreetmap.josm.plugins.validator;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.WaySegment;

public interface ValidatorVisitor {
    void visit(OsmPrimitive p);

    void visit(WaySegment ws);
}
