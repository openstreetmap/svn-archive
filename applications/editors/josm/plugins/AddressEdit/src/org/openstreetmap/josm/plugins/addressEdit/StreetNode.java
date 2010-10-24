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

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * This class is the container for all street segments with the same name. Every street
 * consists at least of one segment.
 * 
 * @author Oliver Wieland <oliver.wieland@online.de>
 */
public class StreetNode extends NodeEntityBase {
	private List<INodeEntity> children;
	private List<AddressNode> addresses;
			
	/**
	 * @param osmPrimitive
	 */
	public StreetNode(OsmPrimitive osmPrimitive) {
		super(osmPrimitive);
	}

	public List<INodeEntity> getChildren() {
		return children;
	}
	
	/**
	 * Adds a street segment to the street node.
	 * @param segment
	 */
	public void addStreetSegment(StreetSegmentNode segment) {
		lazyCreateChildren();
		
		children.add(segment);
	}
	
	private void lazyCreateChildren() {
		if (children == null) {
			children = new ArrayList<INodeEntity>();
		}
	}
	
	public void addAddress(AddressNode aNode) {
		lazyCreateAddresses();
		addresses.add(aNode);
	}

	private void lazyCreateAddresses() {
		if (addresses == null) {
			addresses = new ArrayList<AddressNode>();
		}
	}
	
	public List<AddressNode> getAddresses() {
		return addresses;
	}
	
	public void setAddresses(List<AddressNode> addresses) {
		this.addresses = addresses;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(getName());
		
		if (children != null) {
			sb.append(String.format(", %d segments", children.size()));
		}
		
		if (addresses != null) {
			sb.append(String.format(", %d address entries", addresses.size()));
		}
		
		return sb.toString();
	}

}
