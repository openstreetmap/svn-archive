package com.bretth.osmosis.core.migrate;

import com.bretth.osmosis.core.container.v0_5.BoundContainer;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.migrate.impl.EntityContainerMigrater;


/**
 * A task for converting 0.5 data into 0.6 format.  This isn't a true migration but okay for most uses.
 * 
 * @author Brett Henderson
 */
public class MigrateV05ToV06 implements Sink05Source06, EntityProcessor {
	
	private com.bretth.osmosis.core.task.v0_6.Sink sink;
	private EntityContainerMigrater migrater;
	
	
	public MigrateV05ToV06() {
		migrater = new EntityContainerMigrater();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(EntityContainer entityContainer) {
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(BoundContainer bound) {
		sink.process(migrater.migrate(bound));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(NodeContainer node) {
		sink.process(migrater.migrate(node));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(WayContainer way) {
		sink.process(migrater.migrate(way));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(RelationContainer relation) {
		sink.process(migrater.migrate(relation));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		sink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		sink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSink(com.bretth.osmosis.core.task.v0_6.Sink sink) {
		this.sink = sink;
	}
}
