// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_5;

import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;
import com.bretth.osmosis.core.store.Storeable;
import com.bretth.osmosis.core.util.LongAsInt;


/**
 * A data class representing a reference to an OSM node.
 * 
 * @author Brett Henderson
 */
public class WayNode implements Comparable<WayNode>, Storeable {
	
	private int nodeId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param nodeId
	 *            The unique identifier of the node being referred to.
	 */
	public WayNode(long nodeId) {
		this.nodeId = LongAsInt.longToInt(nodeId);
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
	public WayNode(StoreReader sr, StoreClassRegister scr) {
		this(sr.readInteger());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		sw.writeInteger(nodeId);
	}
	
	
	/**
	 * Compares this way node to the specified way node. The way node comparison
	 * is based on a comparison of nodeId.
	 * 
	 * @param wayNode
	 *            The way node to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	public int compareTo(WayNode wayNode) {
		long result;
		
		result = this.nodeId - wayNode.nodeId;
		
		if (result > 0) {
			return 1;
		} else if (result < 0) {
			return -1;
		} else {
			return 0;
		}
	}
	
	
	/**
	 * @return The nodeId.
	 */
	public long getNodeId() {
		return nodeId;
	}
}
