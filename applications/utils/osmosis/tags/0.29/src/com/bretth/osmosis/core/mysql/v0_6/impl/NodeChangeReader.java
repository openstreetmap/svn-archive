// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6.impl;

import java.util.Date;

import com.bretth.osmosis.core.container.v0_6.ChangeContainer;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.mysql.common.EntityHistory;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.task.common.ChangeAction;


/**
 * Reads the set of node changes from a database that have occurred within a
 * time interval.
 * 
 * @author Brett Henderson
 */
public class NodeChangeReader {
	
	private PeekableIterator<EntityHistory<Node>> nodeHistoryReader;
	private ChangeContainer nextValue;
	private Date intervalBegin;
	
	
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
		this.intervalBegin = intervalBegin;
		
		nodeHistoryReader = new PeekableIterator<EntityHistory<Node>>(
			new NodeHistoryReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd)
		);
	}
	
	
	/**
	 * Reads the history of the next entity and builds a change object.
	 */
	private ChangeContainer readChange() {
		boolean createdPreviously;
		EntityHistory<Node> mostRecentHistory;
		NodeContainer nodeContainer;
		
		// Read the entire node history, if any nodes exist prior to the
		// interval beginning, the node already existed and therefore cannot
		// be a create.
		createdPreviously = false;
		do {
			mostRecentHistory = nodeHistoryReader.next();
			if (mostRecentHistory.getEntity().getTimestamp().compareTo(intervalBegin) < 0) {
				createdPreviously = true;
			}
		} while (nodeHistoryReader.hasNext() &&
				(nodeHistoryReader.peekNext().getEntity().getId() == mostRecentHistory.getEntity().getId()));
		
		// The node in the result must be wrapped in a container.
		nodeContainer = new NodeContainer(mostRecentHistory.getEntity());
		
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
	 * transactions and should always be called in a finally block whenever this
	 * class is used.
	 */
	public void release() {
		nextValue = null;
		
		nodeHistoryReader.release();
	}
}
