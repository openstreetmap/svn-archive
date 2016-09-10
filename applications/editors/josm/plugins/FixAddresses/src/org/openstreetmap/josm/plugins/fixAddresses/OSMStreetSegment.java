// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Represents a single segment of a street. In many cases a segment may represent the complete street, but
 * sometimes a street is separated into many segments, e. g. due to different speed limits, bridges, etc..
 *
 * @author Oliver Wieland &lt;oliver.wieland@online.de>
 *
 */
public class OSMStreetSegment extends OSMEntityBase {

    public OSMStreetSegment(OsmPrimitive osmObject) {
        super(osmObject);
    }

    @Override
    public List<IOSMEntity> getChildren() {
        return null;
    }
}
