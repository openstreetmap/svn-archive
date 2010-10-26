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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.data.osm.visitor.Visitor;

/**
 *
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */

public class AddressEditContainer implements Visitor, DataSetListener, IAddressEditContainerListener {
	private HashMap<String, StreetNode> streetDict = new HashMap<String, StreetNode>(100); 
	private List<AddressNode> unresolvedAddresses = new ArrayList<AddressNode>(100);
	private List<AddressNode> incompleteAddresses = new ArrayList<AddressNode>(100);
	
	private HashSet<String> tags = new HashSet<String>();
	
	private List<IAddressEditContainerListener> listeners = new ArrayList<IAddressEditContainerListener>();
	
	/**
	 * 
	 */
	public AddressEditContainer() {
		NodeEntityBase.addChangedListener(this);
	}

	/**
	 * Adds a change listener.
	 * @param listener
	 */
	public void addChangedListener(IAddressEditContainerListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes a change listener.
	 * @param listener
	 */
	public void removeChangedListener(IAddressEditContainerListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Notifies clients that the address container changed.
	 */
	protected void fireContainerChanged() {
		for (IAddressEditContainerListener listener : listeners) {
			listener.containerChanged(this);
		}
	}
	
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
		
		if (!aNode.isComplete()) {
			incompleteAddresses.add(aNode);
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
	
	public List<AddressNode> getUnresolvedAddresses() {
		return unresolvedAddresses;
	}

	public List<AddressNode> getIncompleteAddresses() {
		return incompleteAddresses;
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
	 * Gets the number of streets in the container.
	 * @return
	 */
	public int getNumberOfStreets() {
		return streetDict != null ? streetDict.size() : 0;
	}
	
	/**
	 * Get the number of incomplete addresses.
	 * @return
	 */
	public int getNumberOfIncompleteAddresses() {
		return incompleteAddresses != null ? incompleteAddresses.size() : 0;
	}
	
	/**
	 * Gets the number of unresolved addresses.
	 * @return
	 */
	public int getNumberOfUnresolvedAddresses() {
		return unresolvedAddresses != null ? unresolvedAddresses.size() : 0;
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
		
		/* Remove all resolves nodes from unresolved list */
		for (AddressNode resolved : resolvedAddresses) {
			unresolvedAddresses.remove(resolved);
		}
	}
	
	/**
	 * Rebuilds the street and address lists. 
	 */
	public void invalidate() {
		invalidate(Main.main.getCurrentDataSet().allPrimitives());
	}
	
	public void invalidate(final Collection<? extends OsmPrimitive> osmData) {
		if (osmData == null || osmData.isEmpty())
			return;
		
		clearData();
		for (OsmPrimitive osmPrimitive : osmData) {
			osmPrimitive.visit(this);
		}
		
		resolveAddresses();
		
		Collections.sort(incompleteAddresses);
		Collections.sort(unresolvedAddresses);
		
		fireContainerChanged();
	}
	
	public void clearData() {
		streetDict.clear();
		unresolvedAddresses.clear();
		incompleteAddresses.clear();
	}
	
	/**
	 * Connects the listener to the data set and revisits the data. This method should
	 * be called immediately before an edit session starts.
	 */
	public void attachToDataSet() {
		Main.main.getCurrentDataSet().addDataSetListener(this);
		invalidate();
	}
	
	/**
	 * Disconnects the listener from the data set. This method should
	 * be called immediately after an edit session has ended.
	 */
	public void detachFromDataSet() {
		Main.main.getCurrentDataSet().removeDataSetListener(this);
	}

	@Override
	public void dataChanged(DataChangedEvent event) {
	}

	@Override
	public void nodeMoved(NodeMovedEvent event) {
				
	}

	@Override
	public void otherDatasetChange(AbstractDatasetChangedEvent event) {
	}

	@Override
	public void primtivesAdded(PrimitivesAddedEvent event) {
		invalidate();
	}

	@Override
	public void primtivesRemoved(PrimitivesRemovedEvent event) {
		invalidate();
	}

	@Override
	public void relationMembersChanged(RelationMembersChangedEvent event) {
	}

	@Override
	public void tagsChanged(TagsChangedEvent event) {
		invalidate();		
	}

	@Override
	public void wayNodesChanged(WayNodesChangedEvent event) {
	}

	@Override
	public void containerChanged(AddressEditContainer container) {
		
	}

	@Override
	public void entityChanged() {
		invalidate();		
	}
}
