// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.history.store;


/**
 * Defines the different node store implementations capable of working with 
 * history nodes.
 * 
 * @author Brett Henderson
 */
public enum HistoryNodeStoreType {
	/**
	 * An example implementation of the HistoryNodeStore interface that can 
	 * be used during testing and for very small datasets.
	 */
	Example, 
	
	/**
	 * An implementation of the HistoryNodeStore that saves all data in a 
	 * file on disk.
	 */
	TempFile
}
