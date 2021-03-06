// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.domain.v0_5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.util.IntAsChar;


/**
 * A data class representing a single OSM way.
 * 
 * @author Brett Henderson
 */
public class Way extends Entity implements Comparable<Way> {
	
	private List<WayNode> wayNodeList;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param user
	 *            The user that last modified this entity.
	 */
	public Way(long id, Date timestamp, OsmUser user) {
		super(id, timestamp, user);
		
		wayNodeList = new ArrayList<WayNode>();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param timestampContainer
	 *            The container holding the timestamp in an alternative
	 *            timestamp representation.
	 * @param user
	 *            The user that last modified this entity.
	 */
	public Way(long id, TimestampContainer timestampContainer, OsmUser user) {
		super(id, timestampContainer, user);
		
		wayNodeList = new ArrayList<WayNode>();
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public Way(StoreReader sr, StoreClassRegister scr) {
		super(sr, scr);
		
		int nodeCount;
		
		nodeCount = sr.readCharacter();
		
		wayNodeList = new ArrayList<WayNode>();
		for (int i = 0; i < nodeCount; i++) {
			addWayNode(new WayNode(sr, scr));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		super.store(sw, scr);
		
		sw.writeCharacter(IntAsChar.intToChar(wayNodeList.size()));
		for (WayNode wayNode : wayNodeList) {
			wayNode.store(sw, scr);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public EntityType getType() {
		return EntityType.Way;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Way) {
			return compareTo((Way) o) == 0;
		} else {
			return false;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public int hashCode() {
		/*
		 * As per the hashCode definition, this doesn't have to be unique it
		 * just has to return the same value for any two objects that compare
		 * equal. Using id will provide a good distribution of values but is
		 * simple to calculate.
		 */
		return (int) getId();
	}
	
	
	/**
	 * Compares this node list to the specified node list. The comparison is
	 * based on a direct comparison of the node ids.
	 * 
	 * @param comparisonWayNodeList
	 *            The node list to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	protected int compareWayNodes(List<WayNode> comparisonWayNodeList) {
		// The list with the most entities is considered bigger.
		if (wayNodeList.size() != comparisonWayNodeList.size()) {
			return wayNodeList.size() - comparisonWayNodeList.size();
		}
		
		// Check the individual way nodes.
		for (int i = 0; i < wayNodeList.size(); i++) {
			int result = wayNodeList.get(i).compareTo(comparisonWayNodeList.get(i));
			
			if (result != 0) {
				return result;
			}
		}
		
		// There are no differences.
		return 0;
	}


	/**
	 * Compares this way to the specified way. The way comparison is based on a
	 * comparison of id, timestamp, wayNodeList and tags in that order.
	 * 
	 * @param comparisonWay
	 *            The way to compare to.
	 * @return 0 if equal, < 0 if considered "smaller", and > 0 if considered
	 *         "bigger".
	 */
	public int compareTo(Way comparisonWay) {
		int wayNodeListResult;
		
		if (this.getId() < comparisonWay.getId()) {
			return -1;
		}
		if (this.getId() > comparisonWay.getId()) {
			return 1;
		}
		
		if (this.getTimestamp() == null && comparisonWay.getTimestamp() != null) {
			return -1;
		}
		if (this.getTimestamp() != null && comparisonWay.getTimestamp() == null) {
			return 1;
		}
		if (this.getTimestamp() != null && comparisonWay.getTimestamp() != null) {
			int result;
			
			result = this.getTimestamp().compareTo(comparisonWay.getTimestamp());
			
			if (result != 0) {
				return result;
			}
		}
		
		wayNodeListResult = compareWayNodes(
			comparisonWay.getWayNodeList()
		);
		
		if (wayNodeListResult != 0) {
			return wayNodeListResult;
		}
		
		return compareTags(comparisonWay.getTagList());
	}
	
	
	/**
	 * Returns the attached list of way nodes. The returned list is read-only.
	 * 
	 * @return The wayNodeList.
	 */
	public List<WayNode> getWayNodeList() {
		return Collections.unmodifiableList(wayNodeList);
	}
	
	
	/**
	 * Adds a new way node.
	 * 
	 * @param wayNode
	 *            The way node to add.
	 */
	public void addWayNode(WayNode wayNode) {
		wayNodeList.add(wayNode);
	}
	
	
	/**
	 * Adds all node references in the collection to the node.
	 * 
	 * @param wayNodes
	 *            The collection of node references to be added.
	 */
	public void addWayNodes(Collection<WayNode> wayNodes) {
		wayNodeList.addAll(wayNodes);
	}
}
