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
package org.openstreetmap.josm.plugins.fixAddresses;

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
 * Provides a container serving streets and unresolved/incomplete addresses.
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */

public class AddressEditContainer implements Visitor, DataSetListener, IAddressEditContainerListener {
	private HashMap<String, StreetNode> streetDict = new HashMap<String, StreetNode>(100); 
	private List<AddressNode> unresolvedAddresses = new ArrayList<AddressNode>(100);
	private List<AddressNode> incompleteAddresses = new ArrayList<AddressNode>(100);
	
	private HashMap<String, StreetNode> shadowStreetDict = new HashMap<String, StreetNode>(100); 
	private List<AddressNode> shadowUnresolvedAddresses = new ArrayList<AddressNode>(100);
	private List<AddressNode> shadowIncompleteAddresses = new ArrayList<AddressNode>(100);
	
	private HashMap<String, AddressNode> addressCache = new HashMap<String, AddressNode>();
	private HashSet<Node> visitedNodes = new HashSet<Node>();
	private HashSet<Way> visitedWays = new HashSet<Way>();
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
		List<IAddressEditContainerListener> shadowListeners = 
			new ArrayList<IAddressEditContainerListener>(listeners);
		
		for (IAddressEditContainerListener listener : shadowListeners) {
			listener.containerChanged(this);
		}
	}
	
	private void markNodeAsVisited(Node n) {
		visitedNodes.add(n);
	}
	
	private boolean hasBeenVisited(Node n) {
		return visitedNodes.contains(n);
	}
	
	private void markWayAsVisited(Way w) {
		visitedWays.add(w);
	}
	
	private boolean hasBeenVisited(Way w) {
		return visitedWays.contains(w);
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Node)
	 */
	@Override
	public void visit(Node n) {
		if (hasBeenVisited(n)) {
			return;
		}

		// Address nodes are recycled in order to keep instance variables like guessed names
		String aid = "" + n.getId();
		AddressNode aNode = null;
		if (!addressCache.containsKey(aid)) {
			aNode = NodeFactory.createNode(n);
			if (aNode != null) {
				addressCache.put(aid, aNode);
			}
		} else {
			aNode = addressCache.get(aid);
			aNode.setOsmObject(n);
		}
		
		if (aNode != null) {
			addAndClassifyAddress(aNode);
		} else {
			// check, if node is referred by a way
			for (OsmPrimitive osm : n.getReferrers()) {
				if (osm instanceof Way) {
					Way w = (Way) osm;
					if (!hasBeenVisited(w)) {
						createNodeFromWay(w);
					}
				}
			}
			
		}
		markNodeAsVisited(n);
	}

	private void addAndClassifyAddress(AddressNode aNode) {
		if (!assignAddressToStreet(aNode)) {
			// Assignment failed: Street is not known (yet) -> add to 'unresolved' list 
			shadowUnresolvedAddresses.add(aNode);
		}

		if (!aNode.isComplete()) {
			shadowIncompleteAddresses.add(aNode);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Way)
	 */
	@Override
	public void visit(Way w) {
		// This doesn't matter, we just need the street name 
		//if (w.isIncomplete()) return;
		
		createNodeFromWay(w);
		/*
        for (Node n : w.getNodes()) {
            
        }*/
	}

	/**
	 * Creates the node from an OSM way instance.
	 *
	 * @param w the w
	 */
	private void createNodeFromWay(Way w) {
		INodeEntity ne = NodeFactory.createNodeFromWay(w);
		
		processNode(ne, w);
		
		markWayAsVisited(w);
		
		// Look also into nodes for addresses (unlikely, but at least they
		// get marked as visited).
		for (Node n : w.getNodes()) {
			visit(n);
		}
		
		for (String key : w.keySet()) {
			if (!tags.contains(key)) {
				tags.add(key);
			}
		}
	}

	/**
	 * Process a entity node.
	 *
	 * @param ne the ne
	 * @param w the w
	 */
	private void processNode(INodeEntity ne, Way w) {
		if (ne != null) {
			if (ne instanceof StreetSegmentNode) {
				StreetSegmentNode newSegment = (StreetSegmentNode) ne;

				if (newSegment != null) {
					String name = newSegment.getName();
					if (StringUtils.isNullOrEmpty(name)) return;

					StreetNode sNode = null;
					if (shadowStreetDict.containsKey(name)) { // street exists?
						sNode = shadowStreetDict.get(name);
					} else { // new street name -> add to dict
						sNode = new StreetNode(w);
						shadowStreetDict.put(name, sNode);
					}

					if (sNode != null) {
						// TODO: Check if segment really belongs to the street, even if the
						// names are the same. Then the streets should be split up...
						sNode.addStreetSegment(newSegment);
					} else {
						throw new RuntimeException("Street node is null!");
					}
				}
			}

			// Node is an address 
			if (ne instanceof AddressNode) {
				AddressNode aNode = (AddressNode) ne;
				addAndClassifyAddress(aNode);			
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Relation)
	 */
	@Override
	public void visit(Relation e) {
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.osm.visitor.Visitor#visit(org.openstreetmap.josm.data.osm.Changeset)
	 */
	@Override
	public void visit(Changeset cs) {
	}

	/**
	 * Gets the dictionary contains the collected streets.
	 * @return
	 */
	public HashMap<String, StreetNode> getStreetDict() {
		return streetDict;
	}
	
	/**
	 * Gets the unresolved (addresses without valid street name) addresses.
	 *
	 * @return the unresolved addresses
	 */
	public List<AddressNode> getUnresolvedAddresses() {
		return unresolvedAddresses;
	}

	/**
	 * Gets the list with incomplete addresses.
	 *
	 * @return the incomplete addresses
	 */
	public List<AddressNode> getIncompleteAddresses() {
		return incompleteAddresses;
	}

	/**
	 * Gets the street list.
	 *
	 * @return the street list
	 */
	public List<StreetNode> getStreetList() {		
		ArrayList<StreetNode> sortedList = new ArrayList<StreetNode>(streetDict.values());
		Collections.sort(sortedList);
		return sortedList;
	}

	/**
	 * Gets all addresses without valid street.
	 * @return
	 */
	public List<AddressNode> getUnresolvedItems() {
		return unresolvedAddresses;
	}

	/**
	 * Gets the tags used in the data layer.
	 * @return
	 */
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
	 * Gets the number of guessed tags.
	 * @return
	 */
	public int getNumberOfGuesses() {
		int sum = 0;
				
		for (AddressNode aNode : getAllAddressesToFix()) {
			if (aNode.hasGuesses()) {
				sum++;
			}
		}
		return sum;
	}
	
	/**
	 * Gets all (incomplete and/or unresolved) address nodes to fix.
	 * @return
	 */
	public List<AddressNode> getAllAddressesToFix() {
		List<AddressNode> all = new ArrayList<AddressNode>(incompleteAddresses);

		for (AddressNode aNode : unresolvedAddresses) {
			if (!all.contains(aNode)) {
				all.add(aNode);
			}
		}
		
		return all; 
	}

	/**
	 * Tries to assign an address to a street.
	 * @param aNode
	 */
	private boolean assignAddressToStreet(AddressNode aNode) {
		String streetName = aNode.getStreet();
		
		if (streetName != null && shadowStreetDict.containsKey(streetName)) {
			StreetNode sNode = shadowStreetDict.get(streetName);
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
		for (AddressNode node : shadowUnresolvedAddresses) {
			if (assignAddressToStreet(node)) {
				resolvedAddresses.add(node);
			}
		}
		
		/* Remove all resolves nodes from unresolved list */
		for (AddressNode resolved : resolvedAddresses) {
			shadowUnresolvedAddresses.remove(resolved);
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

		synchronized (this) {
			clearData();
			for (OsmPrimitive osmPrimitive : osmData) {
				osmPrimitive.visit(this);
			}

			resolveAddresses();
			// sort lists
			Collections.sort(shadowIncompleteAddresses);
			Collections.sort(shadowUnresolvedAddresses);

			// put results from shadow copy into real lists
			incompleteAddresses = new ArrayList<AddressNode>(shadowIncompleteAddresses);
			unresolvedAddresses = new ArrayList<AddressNode>(shadowUnresolvedAddresses);
			streetDict = new HashMap<String, StreetNode>(shadowStreetDict);
			// remove temp data
			shadowStreetDict.clear();
			shadowUnresolvedAddresses.clear();
			shadowIncompleteAddresses.clear();
			// update clients
			fireContainerChanged();
		}
	}
	
	public void clearData() {
		shadowStreetDict.clear();
		shadowUnresolvedAddresses.clear();
		shadowIncompleteAddresses.clear();
		visitedNodes.clear();
		visitedWays.clear();
	}
	
	/**
	 * Connects the listener to the data set and revisits the data. This method should
	 * be called immediately before an edit session starts.
	 */
	public void attachToDataSet(Collection<? extends OsmPrimitive> dataToExamine) {		
		Main.main.getCurrentDataSet().addDataSetListener(this);
		if (dataToExamine != null && dataToExamine.size() > 0) {
			invalidate(dataToExamine); // use given data set (usually the current selection)
		} else {
			invalidate(); // use current data set
		}
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
	public void entityChanged(INodeEntity entity) {
		invalidate();		
	}
}
