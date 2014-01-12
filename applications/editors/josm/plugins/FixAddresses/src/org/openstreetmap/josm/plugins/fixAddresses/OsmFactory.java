/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.HashMap;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

public class OsmFactory {
	private static HashMap<String, OSMAddress> addressCache = new HashMap<String, OSMAddress>();

	/**
	 * Creates an address node from an OSM node, if possible.
	 * @param node
	 * @return
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
	 * @param way
	 * @return The new node instance or null; if given way is inappropriate.
	 */
	public static IOSMEntity createNodeFromWay(Way way) {
		if (TagUtils.hasHighwayTag(way)) {
			return new OSMStreetSegment(way);
		}

		// Check for building with address
		if (way.isClosed() && TagUtils.hasBuildingTag(way)  && TagUtils.isAddress(way)) {
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
