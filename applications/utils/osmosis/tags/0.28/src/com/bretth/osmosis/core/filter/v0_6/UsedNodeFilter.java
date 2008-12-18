// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.filter.v0_6;

import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.container.v0_6.EntityProcessor;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.filter.common.IdTracker;
import com.bretth.osmosis.core.filter.common.IdTrackerFactory;
import com.bretth.osmosis.core.filter.common.IdTrackerType;
import com.bretth.osmosis.core.store.ReleasableIterator;
import com.bretth.osmosis.core.store.SimpleObjectStore;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;
import com.bretth.osmosis.core.task.v0_6.Sink;
import com.bretth.osmosis.core.task.v0_6.SinkSource;


/**
 * Restricts output of nodes to those that are used in ways.
 * 
 * @author Brett Henderson
 * @author Karl Newman
 * @author Christoph Sommer 
 */
public class UsedNodeFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private SimpleObjectStore<NodeContainer> allNodes;
	private SimpleObjectStore<WayContainer> allWays;
	private SimpleObjectStore<RelationContainer> allRelations;
	private IdTracker requiredNodes;
	
	
	/**
	 * Creates a new instance.
	 *
	 * @param idTrackerType
	 *            Defines the id tracker implementation to use.
	 */
	public UsedNodeFilter(IdTrackerType idTrackerType) {
		allNodes = new SimpleObjectStore<NodeContainer>(new SingleClassObjectSerializationFactory(NodeContainer.class), "afnd", true);
		allWays = new SimpleObjectStore<WayContainer>(new SingleClassObjectSerializationFactory(WayContainer.class), "afwy", true);
		allRelations = new SimpleObjectStore<RelationContainer>(new SingleClassObjectSerializationFactory(RelationContainer.class), "afrl", true);

		requiredNodes = IdTrackerFactory.createInstance(idTrackerType);
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
	 * {@inheritDoc}
	 */
	public void process(BoundContainer boundContainer) {
		// By default, pass it on unchanged
		sink.process(boundContainer);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer container) {
		allNodes.add(container);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer container) {
		Way way;

		// mark all nodes as required		
		way = container.getEntity();
		for (WayNode nodeReference : way.getWayNodeList()) {
			long nodeId = nodeReference.getNodeId();
			requiredNodes.set(nodeId);
		}

		allWays.add(container);

	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		allRelations.add(container);
	}


	/**
	 * {@inheritDoc}
	 */
	public void complete() {

		// send on all required nodes
		ReleasableIterator<NodeContainer> nodeIterator = allNodes.iterate();
		while (nodeIterator.hasNext()) {
			NodeContainer nodeContainer = nodeIterator.next();
			long nodeId = nodeContainer.getEntity().getId();
			if (!requiredNodes.get(nodeId)) {
				continue;
			}
			sink.process(nodeContainer);
		}
		nodeIterator.release();
		nodeIterator = null;

		// send on all ways
		ReleasableIterator<WayContainer> wayIterator = allWays.iterate();
		while (wayIterator.hasNext()) {
			sink.process(wayIterator.next());
		}
		wayIterator.release();
		wayIterator = null;

		// send on all relations
		ReleasableIterator<RelationContainer> relationIterator = allRelations.iterate();
		while (relationIterator.hasNext()) {
			sink.process(relationIterator.next());
		}
		relationIterator.release();
		relationIterator = null;

		// done
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
