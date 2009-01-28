// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.container.v0_5;

import org.openstreetmap.osmosis.core.domain.v0_5.Relation;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;


/**
 * Entity container implementation for relations.
 * 
 * @author Brett Henderson
 */
public class RelationContainer extends EntityContainer {
	private static final long serialVersionUID = 1L;
	
	
	private Relation relation;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param relation
	 *            The relation to wrap.
	 */
	public RelationContainer(Relation relation) {
		this.relation = relation;
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
	public RelationContainer(StoreReader sr, StoreClassRegister scr) {
		relation = new Relation(sr, scr);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		relation.store(sw, scr);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(EntityProcessor processor) {
		processor.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation getEntity() {
		return relation;
	}
}
