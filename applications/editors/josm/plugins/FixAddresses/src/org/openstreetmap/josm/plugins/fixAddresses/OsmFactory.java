// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.HashMap;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

/**
 * OSM entities factory.
 */
public final class OsmFactory {
    private static HashMap<String, OSMAddress> addressCache = new HashMap<>();

    private OsmFactory() {
        // Hide default constructor for utilities classes
    }

    /**
     * Creates an address node from an OSM node, if possible.
     * @param node OSM node
     * @return created address
     */
    public static OSMAddress createNode(Node node) {
        if (TagUtils.isAddress(node)) {
            String aid = "" + node.getId();

            OSMAddress aNode = lookup(aid);
            if (aNode == null) {
                aNode = new OSMAddress(node);
                addressCache.put(aid, aNode);
            } else {
                aNode.setOsmObject(node);
            }
            return aNode;
        }

        return null;
    }

    /**
     * Creates an node entity from an OSM way, if possible.
     * @param way OSM way
     * @return The new node instance or null; if given way is inappropriate.
     */
    public static IOSMEntity createNodeFromWay(Way way) {
        if (TagUtils.hasHighwayTag(way)) {
            return new OSMStreetSegment(way);
        }

        // Check for building with address
        if (way.isClosed() && TagUtils.hasBuildingTag(way) && TagUtils.isAddress(way)) {
            String aid = "" + way.getId();

            OSMAddress aNode = lookup(aid);
            if (aNode == null) {
                aNode = new OSMAddress(way);
                addressCache.put(aid, aNode);
            } else {
                aNode.setOsmObject(way);
            }

            return aNode;
        }
        return null;
    }

    private static OSMAddress lookup(String aid) {
        if (addressCache.containsKey(aid)) {
            return addressCache.get(aid);
        }
        return null;
    }
}
