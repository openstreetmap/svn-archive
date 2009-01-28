// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.progress.v0_6;

import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.progress.v0_6.impl.ProgressTracker;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * Logs progress information using jdk logging at info level at regular intervals.
 * 
 * @author Brett Henderson
 */
public class EntityProgressLogger implements SinkSource {
	
	private static final Logger log = Logger.getLogger(EntityProgressLogger.class.getName());
	
	private Sink sink;
	private ProgressTracker progressTracker;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param interval
	 *            The interval between logging progress reports in milliseconds.
	 */
	public EntityProgressLogger(int interval) {
		progressTracker = new ProgressTracker(interval);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		Entity entity;
		
		entity = entityContainer.getEntity();
		
		if (progressTracker.updateRequired()) {
			log.info("Processing " + entity.getType() + " " + entity.getId() + ", " + progressTracker.getObjectsPerSecond() + " objects/second.");
		}
		
		sink.process(entityContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		log.info("Processing completion steps.");
		
		sink.complete();
		
		log.info("Processing complete.");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		sink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
