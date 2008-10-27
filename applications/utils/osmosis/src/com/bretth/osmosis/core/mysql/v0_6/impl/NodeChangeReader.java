// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.container.v0_6.ChangeContainer;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.PersistentIterator;
import com.bretth.osmosis.core.store.SingleClassObjectSerializationFactory;
import com.bretth.osmosis.core.task.common.ChangeAction;


/**
 * Reads the set of node changes from a database that have occurred within a
 * time interval.
 * 
 * @author Brett Henderson
 */
public class NodeChangeReader {
	
	private PeekableIterator<EntityHistory<Node>> nodeHistoryReader;
	private PeekableIterator<DbFeatureHistory<DbFeature<Tag>>> nodeTagHistoryReader;
	private ChangeContainer nextValue;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public NodeChangeReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers, Date intervalBegin, Date intervalEnd) {
		nodeHistoryReader =
			new PeekableIterator<EntityHistory<Node>>(
				new PersistentIterator<EntityHistory<Node>>(
					new SingleClassObjectSerializationFactory(EntityHistory.class),
					new NodeHistoryReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd),
					"nod",
					true
				)
			);
		nodeTagHistoryReader =
			new PeekableIterator<DbFeatureHistory<DbFeature<Tag>>>(
				new PersistentIterator<DbFeatureHistory<DbFeature<Tag>>>(
					new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
					new EntityTagHistoryReader(loginCredentials, "nodes", "node_tags", intervalBegin, intervalEnd),
					"nodtag",
					true
				)
			);
	}
	
	
	/**
	 * Consolides the output of all history readers so that nodes are fully
	 * populated.
	 * 
	 * @return A node history record where the node is fully populated with nodes
	 *         and tags.
	 */
	private EntityHistory<Node> readNextNodeHistory() {
		EntityHistory<Node> nodeHistory;
		Node node;
		
		nodeHistory = nodeHistoryReader.next();
		node = nodeHistory.getEntity();
		
		// Add all applicable tags to the node.
		while (nodeTagHistoryReader.hasNext() &&
				nodeTagHistoryReader.peekNext().getDbFeature().getEntityId() == node.getId() &&
				nodeTagHistoryReader.peekNext().getVersion() == node.getVersion()) {
			node.addTag(nodeTagHistoryReader.next().getDbFeature().getFeature());
		}
		
		return nodeHistory;
	}
	
	
	/**
	 * Reads the history of the next entity and builds a change object.
	 */
	private ChangeContainer readChange() {
		boolean createdPreviously;
		EntityHistory<Node> mostRecentHistory;
		Node node;
		NodeContainer nodeContainer;
		
		// Check the first node, if it has a version greater than 1 the node
		// existed prior to the interval beginning and therefore cannot be a
		// create.
		mostRecentHistory = readNextNodeHistory();
		node = mostRecentHistory.getEntity();
		createdPreviously = (node.getVersion() > 1);
		
		while (nodeHistoryReader.hasNext() &&
				(nodeHistoryReader.peekNext().getEntity().getId() == node.getId())) {
			mostRecentHistory = readNextNodeHistory();
		}
		
		// The node in the result must be wrapped in a container.
		nodeContainer = new NodeContainer(node);
		
		// The entity has been modified if it is visible and was created previously.
		// It is a create if it is visible and was NOT created previously.
		// It is a delete if it is NOT visible and was created previously.
		// No action if it is NOT visible and was NOT created previously.
		if (mostRecentHistory.isVisible() && createdPreviously) {
			return new ChangeContainer(nodeContainer, ChangeAction.Modify);
		} else if (mostRecentHistory.isVisible() && !createdPreviously) {
			return new ChangeContainer(nodeContainer, ChangeAction.Create);
		} else if (!mostRecentHistory.isVisible() && createdPreviously) {
			return new ChangeContainer(nodeContainer, ChangeAction.Delete);
		} else {
			return null;
		}
	}
	
	
	/**
	 * Indicates if there is any more data available to be read.
	 * 
	 * @return True if more data is available, false otherwise.
	 */
	public boolean hasNext() {
		while (nextValue == null && nodeHistoryReader.hasNext()) {
			nextValue = readChange();
		}
		
		return (nextValue != null);
	}
	
	
	/**
	 * Returns the next available entity and advances to the next record.
	 * 
	 * @return The next available entity.
	 */
	public ChangeContainer next() {
		ChangeContainer result;
		
		if (!hasNext()) {
			throw new OsmosisRuntimeException("No records are available, call hasNext first.");
		}
		
		result = nextValue;
		nextValue = null;
		
		return result;
	}
	
	
	/**
	 * Releases all database resources. This method is guaranteed not to throw
	 * transactions and should alnodes be called in a finally block whenever this
	 * class is used.
	 */
	public void release() {
		nextValue = null;
		
		nodeHistoryReader.release();
		nodeTagHistoryReader.release();
	}
}
