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
/**
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

/* File created on 24.10.2010 */
package org.openstreetmap.josm.plugins.addressEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.Visitor;

/**
 *
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */

public class AddressVisitor implements Visitor {
	private HashMap<String, StreetNode> streetDict = new HashMap<String, StreetNode>(100); 
	private List<AddressNode> unresolvedAddresses = new ArrayList<AddressNode>(100);
	
	private HashSet<String> tags = new HashSet<String>();
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Node)
	 */
	@Override
	public void visit(Node n) {
		AddressNode aNode = NodeFactory.createNode(n);
		
		if (aNode == null) return;
		
		if (!assignAddressToStreet(aNode)) {
			// Assignment failed: Street is not known (yet) -> add to 'unresolved' list 
			unresolvedAddresses.add(aNode);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Way)
	 */
	@Override
	public void visit(Way w) {
		if (w.isIncomplete()) return;
		
		StreetSegmentNode newSegment = NodeFactory.createNodeFromWay(w);
		
		if (newSegment != null) {
			String name = newSegment.getName();
			StreetNode sNode = null;
			if (streetDict.containsKey(name)) {
				sNode = streetDict.get(name);
			} else {
				sNode = new StreetNode(w);
				streetDict.put(name, sNode);
			}
			
			if (sNode != null) {
				sNode.addStreetSegment(newSegment);
			} else {
				throw new RuntimeException("Street node is null!");
			}
		}
		
		for (String key : w.keySet()) {
			if (!tags.contains(key)) {
				tags.add(key);
			}
		}
		/*
        for (Node n : w.getNodes()) {
            
        }*/
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Relation)
	 */
	@Override
	public void visit(Relation e) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Changeset)
	 */
	@Override
	public void visit(Changeset cs) {
		// TODO Auto-generated method stub

	}

	/**
	 * Gets the dictionary contains the collected streets.
	 * @return
	 */
	public HashMap<String, StreetNode> getStreetDict() {
		return streetDict;
	}
	
	public List<StreetNode> getStreetList() {
		
		ArrayList<StreetNode> sortedList = new ArrayList<StreetNode>(streetDict.values());
		Collections.sort(sortedList);
		return sortedList;
	}

	public List<AddressNode> getUnresolvedItems() {
		return unresolvedAddresses;
	}

	public HashSet<String> getTags() {
		return tags;
	}

	/**
	 * Tries to assign an address to a street.
	 * @param aNode
	 */
	private boolean assignAddressToStreet(AddressNode aNode) {
		String streetName = aNode.getStreet();
		if (streetName != null && streetDict.containsKey(streetName)) {
			StreetNode sNode = streetDict.get(streetName);
			sNode.addAddress(aNode);
			//System.out.println("Resolved address " + aNode + ": " + sNode);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Walks through the list of unassigned addresses and tries to assign them to streets.
	 */
	public void resolveAddresses() {
		List<AddressNode> resolvedAddresses = new ArrayList<AddressNode>();
		for (AddressNode node : unresolvedAddresses) {
			if (assignAddressToStreet(node)) {
				resolvedAddresses.add(node);
			}
		}
		
		System.out.println("Resolved " + resolvedAddresses.size() + " addresses");
		
		/* Remove all resolves nodes from unresolved list */
		for (AddressNode resolved : resolvedAddresses) {
			unresolvedAddresses.remove(resolved);
		}
		
		System.out.println("Still unresolved: " + unresolvedAddresses.size() + " addresses");
	}
	
	public void clearData() {
		streetDict.clear();
		unresolvedAddresses.clear();
	}
}
