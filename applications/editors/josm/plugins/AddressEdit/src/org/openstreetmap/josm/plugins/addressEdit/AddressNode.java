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

import org.openstreetmap.josm.data.osm.OsmPrimitive;

public class AddressNode extends NodeEntityBase {

	private static final String MISSING_TAG = "?";

	public AddressNode(OsmPrimitive osmObject) {
		super(osmObject);
	}

	/**
	 * Checks if the underlying address node has all tags usually needed to describe an address.
	 * @return
	 */
	public boolean isComplete() {
		return 	TagUtils.hasAddrCityTag(osmObject) && TagUtils.hasAddrCountryTag(osmObject) &&
				TagUtils.hasAddrHousenumberTag(osmObject) && TagUtils.hasAddrPostcodeTag(osmObject) &&
				TagUtils.hasAddrStateTag(osmObject) && TagUtils.hasAddrStreetTag(osmObject);
	}
	
	/**
	 * Gets the name of the street associated with this address.
	 * @return
	 */
	public String getStreet() {
		if (!TagUtils.hasAddrStreetTag(osmObject)) {
			return NodeEntityBase.ANONYMOUS;
		}
		return TagUtils.getAddrStreetValue(osmObject);
	}
	
	/**
	 * Gets the name of the post code associated with this address.
	 * @return
	 */
	public String getPostCode() {
		if (!TagUtils.hasAddrPostcodeTag(osmObject)) {
			return MISSING_TAG;
		}
		return TagUtils.getAddrPostcodeValue(osmObject);
	}
	
	/**
	 * Gets the name of the house number associated with this address.
	 * @return
	 */
	public String getHouseNumber() {
		if (!TagUtils.hasAddrHousenumberTag(osmObject)) {
			return MISSING_TAG;
		}
		return TagUtils.getAddrHousenumberValue(osmObject);
	}
	
	/**
	 * Gets the name of the city associated with this address.
	 * @return
	 */
	public String getCity() {
		if (!TagUtils.hasAddrCityTag(osmObject)) {
			return MISSING_TAG;
		}
		return TagUtils.getAddrCityValue(osmObject);
	}
	
	/**
	 * Gets the name of the state associated with this address.
	 * @return
	 */
	public String getState() {
		if (!TagUtils.hasAddrStateTag(osmObject)) {
			return MISSING_TAG;
		}
		return TagUtils.getAddrStateValue(osmObject);
	}

	/**
	 * Gets the name of the state associated with this address.
	 * @return
	 */
	public String getCountry() {
		return TagUtils.getAddrCountryValue(osmObject);
	}
	
	@Override
	public String toString() {
		return AddressNode.getFormatString(this);
	}

	public static String getFormatString(AddressNode node) {
		// TODO: Add further countries here
		// DE
		return String.format("%s %s, %s-%s %s (%s)", 
				node.getStreet(),
				node.getHouseNumber(),
				node.getCountry(),
				node.getPostCode(),
				node.getCity(),
				node.getState());
	}
}
