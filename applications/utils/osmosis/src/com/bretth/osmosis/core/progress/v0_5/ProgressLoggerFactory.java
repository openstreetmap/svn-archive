package com.bretth.osmosis.core.progress.v0_5;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.SinkSourceManager;


/**
 * The task manager factory for an entity reporter.
 * 
 * @author Brett Henderson
 */
public class ProgressLoggerFactory extends TaskManagerFactory {
	private static final String ARG_LOG_INTERVAL = "interval";
	private static final int DEFAULT_LOG_INTERVAL = 5;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		ProgressLogger task;
		int interval;
		
		// Get the task arguments.
		interval = getIntegerArgument(taskId, taskArgs, ARG_LOG_INTERVAL, DEFAULT_LOG_INTERVAL);
		
		// Build the task object.
		task = new ProgressLogger(interval * 1000);
		
		return new SinkSourceManager(taskId, task, pipeArgs);
	}
}
