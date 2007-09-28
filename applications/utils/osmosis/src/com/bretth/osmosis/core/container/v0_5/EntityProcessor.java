package com.bretth.osmosis.core.container.v0_5;


/**
 * EntityContainer implementations call implementations of this class to
 * perform entity type specific processing.
 * 
 * @author Brett Henderson
 */
public interface EntityProcessor {
	
	/**
	 * Process the node.
	 * 
	 * @param node
	 *            The node to be processed.
	 */
	public void process(NodeContainer node);
	
	/**
	 * Process the way.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	public void process(WayContainer way);
	
	/**
	 * Process the relation.
	 * 
	 * @param relation
	 *            The relation to be processed.
	 */
	public void process(RelationContainer relation);
}
