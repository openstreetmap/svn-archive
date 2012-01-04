// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.licensechange;

import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.WaySegment;

public interface LicenseChangeVisitor {
    void visit(OsmPrimitive p);
    void visit(List<Node> nodes);
}
