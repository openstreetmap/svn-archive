// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.sort.v0_5;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.container.v0_5.EntityContainer;


/**
 * Compares two entities and sorts them first by their type (Nodes, then
 * Segments, then Ways) and then by their identifier.
 * 
 * @author Brett Henderson
 */
public class EntityByTypeThenIdComparator implements Comparator<EntityContainer> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(EntityContainer o1, EntityContainer o2) {
		int typeDiff;
		long idDiff;
		
		// Perform a type comparison.
		typeDiff = o1.getEntity().getType().compareTo(o2.getEntity().getType());
		if (typeDiff != 0) {
			return typeDiff;
		}
		
		// Perform an identifier comparison.
		idDiff = o1.getEntity().getId() - o2.getEntity().getId();
		if (idDiff > 0) {
			return 1;
		}
		if (idDiff < 0) {
			return -1;
		}
		return 0;
	}
}
