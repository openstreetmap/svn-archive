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

public class WayUtils {
	/**
	 * Checks, if two ways are connected. This is the case, if the first way
	 * shares one node with the second way.
	 * @param w1 The first way.
	 * @param w2 The second way.
	 * @return
	 */
	public static boolean areWaysConnected(Way w1, Way w2) {
		if (w1 == null || w2 == null) return false;
		
		for (Node n1 : w1.getNodes()) {
			if (w2.containsNode(n1)) {
				return true;
			}
		}
		
		return false;
	}
}
