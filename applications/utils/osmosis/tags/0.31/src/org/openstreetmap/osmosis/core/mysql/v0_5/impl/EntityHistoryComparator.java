// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.util.Comparator;
import java.util.Date;

import org.openstreetmap.osmosis.core.domain.v0_5.Entity;


/**
 * A comparator for ordering entity history records by timestamp.
 * 
 * @author Brett Henderson
 * @param <T>
 *            The data type to be compared.
 */
public class EntityHistoryComparator<T extends Entity> implements Comparator<EntityHistory<T>> {

	/**
	 * {@inheritDoc}
	 */
	public int compare(EntityHistory<T> o1, EntityHistory<T> o2) {
		Date t1;
		Date t2;
		
		t1 = o1.getEntity().getTimestamp();
		t2 = o2.getEntity().getTimestamp();
		
		// If both timestamps are null, they compare equal.
		if (t1 == null && t2 == null) {
			return 0;
		}
		
		// If one timestamp is null, the one with the timestamp is larger.
		if (t1 == null) {
			return -1;
		}
		if (t2 == null) {
			return 1;
		}
		
		// Otherwise we compare the timestamps themselves.
		return t1.compareTo(t2);
	}
	
}
