// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.v0_5;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.domain.v0_5.EntityType;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.domain.v0_5.RelationMember;
import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.filter.common.IdTracker;
import com.bretth.osmosis.core.filter.common.IdTrackerFactory;
import com.bretth.osmosis.core.filter.common.IdTrackerType;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.store.SimpleObjectStore;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;
import com.bretth.osmosis.core.task.v0_5.Sink;
import com.bretth.osmosis.core.task.v0_5.SinkSource;


/**
 * A base class for all tasks filter entities within an area.
 * 
 * @author Brett Henderson
 * @author Karl Newman
 */
public abstract class AreaFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private IdTracker availableNodes;
	private IdTracker requiredNodes; // Nodes needed to make complete Ways
	private SimpleObjectStore<NodeContainer> allNodes;
	private boolean completeWays;
	private IdTracker availableWays;
	private SimpleObjectStore<WayContainer> allWays;
	private IdTracker availableRelations;
	private boolean completeRelations;
	private SimpleObjectStore<RelationContainer> allRelations;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param idTrackerType
	 *            Defines the id tracker implementation to use.
	 * @param completeWays
	 *            Include all nodes for ways which have at least one node inside
	 *            the filtered area.
	 * @param completeRelations
	 *            Include all relations referenced by other relations which have
	 *            members inside the filtered area.
	 */
	public AreaFilter(IdTrackerType idTrackerType, boolean completeWays, boolean completeRelations) {
		this.completeWays = completeWays;
		this.completeRelations = completeRelations;
		
		availableNodes = IdTrackerFactory.createInstance(idTrackerType);
		if (completeWays) {
			requiredNodes = IdTrackerFactory.createInstance(idTrackerType);
			allNodes = new SimpleObjectStore<NodeContainer>(new SingleClassObjectSerializationFactory(NodeContainer.class), "afnd", true);
			allWays = new SimpleObjectStore<WayContainer>(new SingleClassObjectSerializationFactory(WayContainer.class), "afwy", true);
		}
		availableWays = IdTrackerFactory.createInstance(idTrackerType);
		availableRelations = IdTrackerFactory.createInstance(idTrackerType);
		if (this.completeRelations || this.completeWays) {
			allRelations = new SimpleObjectStore<RelationContainer>(new SingleClassObjectSerializationFactory(RelationContainer.class), "afrl", true);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// Ask the entity container to invoke the appropriate processing method
		// for the entity type.
		entityContainer.process(this);
	}
	
	
	/**
	 * Indicates if the node lies within the area required.
	 * 
	 * @param node
	 *            The node to be checked.
	 * @return True if the node lies within the area.
	 */
	protected abstract boolean isNodeWithinArea(Node node);
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer container) {
		Node node;
		long nodeId;
		
		node = container.getEntity();
		nodeId = node.getId();
		
		// If complete ways are desired, stuff the node into a file
		if (completeWays) {
			allNodes.add(container);
		}
		// Only add the node if it lies within the box boundaries.
		if (isNodeWithinArea(node)) {
			availableNodes.set(nodeId);
			if (!completeWays) { // just pass it on immediately
				sink.process(container);
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer container) {
		Way way;
		
		way = container.getEntity();

		// First look through all the nodes to see if any are within the filtered area
		for (WayNode nodeReference : way.getWayNodeList()) {
			if (availableNodes.get(nodeReference.getNodeId())) {
				availableWays.set(way.getId());
				break;
			}
		}
		
		// If the way has at least one node in the filtered area
		if (availableWays.get(way.getId())) {
			// If complete ways are desired, mark all its nodes as required, then stuff the way into
			// a file until all the ways are read
			if (completeWays) {
				for (WayNode nodeReference : way.getWayNodeList()) {
					requiredNodes.set(nodeReference.getNodeId());
				}
				allWays.add(container);
			} else { // just filter it on the available nodes and pass it on
				emitFilteredWay(way);
			}
		} 
	}
	
	/**
	 * Construct a new way composed of only those nodes which are available and send it on to the
	 * sink.
	 * @param way
	 *            Complete way entity from which to filter out nodes which are not available.
	 */
	private void emitFilteredWay(Way way) {
		Way filteredWay;
		
		// Create a new way object to contain only available nodes.
		filteredWay = new Way(way.getId(), way.getTimestamp(), way.getUser());
		
		// Only add node references for nodes that are available.
		for (WayNode nodeReference : way.getWayNodeList()) {
			long nodeId;
			
			nodeId = nodeReference.getNodeId();
			
			if (availableNodes.get(nodeId)) {
				filteredWay.addWayNode(nodeReference);
			}
		}
		
		// Only add ways that contain nodes.
		if (filteredWay.getWayNodeList().size() > 0) {
			// Add all tags to the filtered way.
			for (Tag tag : way.getTagList()) {
				filteredWay.addTag(tag);
			}
			
			sink.process(new WayContainer(filteredWay));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		Relation relation;
		
		relation = container.getEntity();

		// If complete relations are wanted, then mark each one as available and stuff them
		// into a file for later retrieval. This has a potential referential integrity problem
		// in that if it is marked as available and referenced by another relation, but later
		// when it is read back, none of its members are available, the filtered relation will
		// be empty so it won't get passed on to the sink.
		if (completeRelations) {
			availableRelations.set(relation.getId());
		} else { // examine the relation to see if it should be passed on
			// First look through all the members to see if any are within the filtered area
			for (RelationMember member : relation.getMemberList()) {
				long memberId;
				EntityType memberType;
				
				memberId = member.getMemberId();
				memberType = member.getMemberType();
				
				if ((EntityType.Node.equals(memberType) && availableNodes.get(memberId))
					|| (EntityType.Way.equals(memberType) && availableWays.get(memberId)) 
					|| (EntityType.Relation.equals(memberType) && availableRelations.get(memberId))) {
						availableRelations.set(relation.getId());
						break;
				}
			}
		}
		
		// if the relation is of interest
		if (availableRelations.get(relation.getId())) {
			// if the ways and nodes are being saved, or if complete relations are required 
			if (completeWays || completeRelations) {
				allRelations.add(container);
			} else { // just filter it on available members and pass it on to the sink
				emitFilteredRelation(relation);
			}
		}
	}


	/**
	 * Construct a new relation composed of only those entities which are available and send it on 
	 * to the sink.
     * @param relation
     *            Complete relation from which to filter out members which are not available.
     */
    private void emitFilteredRelation(Relation relation) {
	    Relation filteredRelation;
	    // Create a new relation object to contain only items within the bounding box.
		filteredRelation = new Relation(relation.getId(), relation.getTimestamp(), relation.getUser());
		
		// Only add members for entities that are available.
		for (RelationMember member : relation.getMemberList()) {
			long memberId;
			EntityType memberType;
			
			memberId = member.getMemberId();
			memberType = member.getMemberType();
			
			if (EntityType.Node.equals(memberType)) {
				if (availableNodes.get(memberId)) {
					filteredRelation.addMember(member);
				}
			} else if (EntityType.Way.equals(memberType)) {
				if (availableWays.get(memberId)) {
					filteredRelation.addMember(member);
				}
			} else if (EntityType.Relation.equals(memberType)) {
				if (availableRelations.get(memberId)) {
					filteredRelation.addMember(member);
				}
			} else {
				throw new OsmosisRuntimeException("Unsupported member type + " + memberType + " for relation " + relation.getId() + ".");
			}
		}
		
		// Only add relations that contain entities.
		if (filteredRelation.getMemberList().size() > 0) {
			// Add all tags to the filtered relation.
			for (Tag tag : relation.getTagList()) {
				filteredRelation.addTag(tag);
			}
			
			sink.process(new RelationContainer(filteredRelation));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		ReleasableIterator<NodeContainer> nodeIterator;
		ReleasableIterator<WayContainer> wayIterator;
		ReleasableIterator<RelationContainer> relationIterator;
		NodeContainer nodeContainer;
		long nodeId;
		if (completeWays) {
			// first send on all the nodes
			nodeIterator = allNodes.iterate();
			while (nodeIterator.hasNext()) {
				nodeContainer = nodeIterator.next();
				nodeId = nodeContainer.getEntity().getId();
				// Send on all the stored nodes which are available (in the filtered area) or
				// required (to make a way complete) 
				if (availableNodes.get(nodeId) || requiredNodes.get(nodeId)) {
					availableNodes.set(nodeId); // make sure to mark it as available for ways
					sink.process(nodeContainer);
				}
			}
			// 
			nodeIterator.release();
			nodeIterator = null;
			// next send on all the ways
			wayIterator = allWays.iterate();
			while (wayIterator.hasNext()) {
				emitFilteredWay(wayIterator.next().getEntity());
			}
			wayIterator.release();
			wayIterator = null;
		}
		if (completeWays || completeRelations) {
			relationIterator = allRelations.iterate();
			while (relationIterator.hasNext()) {
				emitFilteredRelation(relationIterator.next().getEntity());
			}
		}
		sink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (allNodes != null) {
			allNodes.release();
		}
		if (allWays != null) {
			allWays.release();			
		}
		if (allRelations != null) {
			allRelations.release();
		}
		sink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
