// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.sort.v0_6;

import java.util.Comparator;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;


/**
 * Compares two entities and sorts them by their identifier.
 * 
 * @author Brett Henderson
 */
public class EntityByIdComparator implements Comparator<EntityContainer> {
	
	/**
	 * {@inheritDoc}
	 */
	public int compare(EntityContainer o1, EntityContainer o2) {
		long idDiff;
        
		// Perform an identifier comparison.
		idDiff = o1.getEntity().getId() - o2.getEntity().getId();
		if (idDiff > 0) {
			return 1;
		} else if (idDiff < 0) {
			return -1;
		} else {
			return 0;
		}
	}
}
