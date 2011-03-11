package org.openstreetmap.osmosis.history.domain;

import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.history.common.MinorEntityProcessor;

public class MinorWayContainer extends WayContainer {
	private static final long serialVersionUID = 1L;
	
	
	private MinorWay way;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param way
	 *            The way to wrap.
	 */
	public MinorWayContainer(MinorWay way) {
		super(null);
		this.way = way;
	}
	
	public MinorWayContainer(Way way) {
		this(new MinorWay(way));
	}
	
	public MinorWayContainer(StoreReader sr, StoreClassRegister scr) {
		super(null);
		way = new MinorWay(sr, scr);
	}
	
	public MinorWayContainer(WayContainer wayContainer) {
		this(wayContainer.getEntity());
	}


	/**
	 * {@inheritDoc}
	 */
	public void store(StoreWriter sw, StoreClassRegister scr) {
		way.store(sw, scr);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(MinorEntityProcessor processor) {
		processor.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public MinorWay getEntity() {
		return way;
	}


	/**
	 * {@inheritDoc}
	 */
	public MinorWayContainer getWriteableInstance() {
		if (way.isReadOnly()) {
			return new MinorWayContainer(way.getWriteableInstance());
		} else {
			return this;
		}
	}
}
