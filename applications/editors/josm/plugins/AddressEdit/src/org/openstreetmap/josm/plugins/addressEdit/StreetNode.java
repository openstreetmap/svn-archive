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

public class StreetNode {
	private OsmPrimitive osmPrimitive;
	private List<StreetNode> children;
	private List<OsmPrimitive> addresses;
		
	/**
	 * @param osmPrimitive
	 */
	public StreetNode(OsmPrimitive osmPrimitive) {
		super();
		this.osmPrimitive = osmPrimitive;
	}

	public List<StreetNode> getChildren() {
		return children;
	}
	
	public void AddAddress(OsmPrimitive address) {
		LazyCreateAddresses();
		addresses.add(address);
	}

	private void LazyCreateAddresses() {
		if (addresses == null) {
			addresses = new ArrayList<OsmPrimitive>();
		}
	}
	
	public List<OsmPrimitive> getAddresses() {
		return addresses;
	}
	public void setAddresses(List<OsmPrimitive> addresses) {
		this.addresses = addresses;
	}
	
	
}
