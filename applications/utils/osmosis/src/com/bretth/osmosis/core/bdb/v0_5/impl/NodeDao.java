// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.bdb.v0_5.impl;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.bdb.common.StoreableTupleBinding;
import com.bretth.osmosis.core.bdb.common.UnsignedIntegerLongIndexElement;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.mysql.common.TileCalculator;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;


/**
 * Performs all node-specific db operations.
 * 
 * @author Brett Henderson
 */
public class NodeDao {
	private Transaction txn;
	private Database dbNode;
	private Database dbTileNode;
	private TupleBinding idBinding;
	private StoreableTupleBinding<Node> nodeBinding;
	private StoreableTupleBinding<UnsignedIntegerLongIndexElement> uintLongBinding;
	private DatabaseEntry keyEntry;
	private DatabaseEntry dataEntry;
	private TileCalculator tileCalculator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param transaction
	 *            The active transaction.
	 * @param dbNode
	 *            The node database.
	 * @param dbTileNode
	 *            The tile-node database.
	 */
	public NodeDao(Transaction transaction, Database dbNode, Database dbTileNode) {
		this.txn = transaction;
		this.dbNode = dbNode;
		this.dbTileNode = dbTileNode;
		
		idBinding = TupleBinding.getPrimitiveBinding(Long.class);
		nodeBinding = new StoreableTupleBinding<Node>();
		uintLongBinding = new StoreableTupleBinding<UnsignedIntegerLongIndexElement>();
		keyEntry = new DatabaseEntry();
		dataEntry = new DatabaseEntry();
		
		tileCalculator = new TileCalculator();
	}
	
	
	/**
	 * Writes the node to the node database.
	 * 
	 * @param node
	 *            The node to be written.
	 */
	public void putNode(Node node) {
		// Write the node object to the node database.
		idBinding.objectToEntry(node.getId(), keyEntry);
		nodeBinding.objectToEntry(node, dataEntry);
		
		try {
			dbNode.put(txn, keyEntry, dataEntry);
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to write node " + node.getId() + ".", e);
		}
		
		// Write the tile to node index element to the tile-node database.
		uintLongBinding.objectToEntry(
			new UnsignedIntegerLongIndexElement(
				(int) tileCalculator.calculateTile(node.getLatitude(), node.getLongitude()),
				node.getId()
			),
			keyEntry
		);
		dataEntry.setSize(0);
		
		try {
			dbTileNode.put(txn, keyEntry, dataEntry);
		} catch (DatabaseException e) {
			throw new OsmosisRuntimeException("Unable to write tile-node " + node.getId() + ".", e);
		}
	}
}
