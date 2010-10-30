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
package org.openstreetmap.josm.plugins.addressEdit;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

public class NodeFactory {
	/**
	 * Creates an address node from an OSM node, if possible.
	 * @param osm
	 * @return
	 */
	public static AddressNode createNode(Node osm) {
		if (TagUtils.isAddress(osm)) {
			return new AddressNode(osm);
		}
		
		return null;
	}
	
	/**
	 * Creates an node entity from an OSM way, if possible.
	 * @param way
	 * @return The new node instance or null; if given way is inappropriate.
	 */
	public static StreetSegmentNode createNodeFromWay(Way way) {
		if (TagUtils.hasHighwayTag(way)) {
			return new StreetSegmentNode(way);
		}
		
		return null;
	}
}
