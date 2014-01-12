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

import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Represents a single segment of a street. In many cases a segment may represent the complete street, but
 * sometimes a street is separated into many segments, e. g. due to different speed limits, bridges, etc..
 *
 * @author Oliver Wieland <oliver.wieland@online.de>
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
