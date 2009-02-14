// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.progress.v0_5;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_5.SinkSourceManager;


/**
 * The task manager factory for an entity progress logger.
 * 
 * @author Brett Henderson
 */
public class EntityProgressLoggerFactory extends TaskManagerFactory {
	private static final String ARG_LOG_INTERVAL = "interval";
	private static final int DEFAULT_LOG_INTERVAL = 5;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		EntityProgressLogger task;
		int interval;
		
		// Get the task arguments.
		interval = getIntegerArgument(taskConfig, ARG_LOG_INTERVAL, DEFAULT_LOG_INTERVAL);
		
		// Build the task object.
		task = new EntityProgressLogger(interval * 1000);
		
		return new SinkSourceManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}
}
