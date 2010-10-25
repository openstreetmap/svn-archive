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

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.data.osm.Node;
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
		if (!TagUtils.hasAddrCountryTag(osmObject)) {
			return MISSING_TAG;
		}
		return TagUtils.getAddrCountryValue(osmObject);
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.addressEdit.NodeEntityBase#compareTo(org.openstreetmap.josm.plugins.addressEdit.INodeEntity)
	 */
	@Override
	public int compareTo(INodeEntity o) {
		if (o == null || !(o instanceof AddressNode)) {
			return -1;
		}
		AddressNode other = (AddressNode) o;
		
		int cc = 0;
		cc = this.getCountry().compareTo(other.getCountry());
		if ( cc  == 0) {
			cc = this.getState().compareTo(other.getState());			
			if (cc  == 0) {
				cc = this.getCity().compareTo(other.getCity());				
				if (cc  == 0) {
					cc = this.getStreet().compareTo(other.getStreet());					
					if (cc  == 0) {
						cc = this.getHouseNumber().compareTo(other.getHouseNumber());
					}
				}
			}
		}
		
		return cc;
	}
	
	/**
	 * Applies the street name from the specified street node.
	 * @param node
	 */
	public void assignStreet(StreetNode node) {
		if (node == null || !node.hasName()) return;
		
		if (!node.getName().equals(getStreet())) {
			setStreetName(node.getName());			
			node.addAddress(this);
			fireEntityChanged();
		}
	}
	
	/**
	 * Sets the street name of the address node.
	 * @param streetName
	 */
	public void setStreetName(String streetName) {
		if (streetName != null && streetName.length() == 0) return;
		
		changeAddressValue(TagUtils.ADDR_STREET_TAG, streetName);
	}

	
	/**
	 * Sets the state of the address node.
	 * @param state
	 */
	public void setState(String state) {
		if (state != null && state.length() == 0) return;
		
		changeAddressValue(TagUtils.ADDR_STATE_TAG, state);
	}
	
	/**
	 * Sets the country of the address node.
	 * @param country
	 */
	public void setCountry(String country) {
		if (country != null && country.length() == 0) return;
		
		changeAddressValue(TagUtils.ADDR_COUNTRY_TAG, country);
	}
	
	/**
	 * Sets the post code of the address node.
	 * @param postCode
	 */
	public void setPostCode(String postCode) {
		if (postCode != null && postCode.length() == 0) return;
		
		changeAddressValue(TagUtils.ADDR_POSTCODE_TAG, postCode);
	}
	
	/**
	 * Internal helper method which changes the given property and
	 * puts the appropriate command {@link src.org.openstreetmap.josm.command.Command}
	 * into the undo/redo queue.
	 * @param tag The tag to change.
	 * @param newValue The new value for the tag.
	 */
	private void changeAddressValue(String tag, String newValue) {
		Node oldNode = (Node)osmObject;
		OsmPrimitive newNode = new Node(oldNode);
		newNode.put(tag, newValue);
		Main.main.undoRedo.add( new ChangeCommand(oldNode, newNode));
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
