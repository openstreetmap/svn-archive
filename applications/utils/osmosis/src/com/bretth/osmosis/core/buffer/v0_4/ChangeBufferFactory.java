package com.bretth.osmosis.core.buffer.v0_4;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_4.ChangeSinkRunnableChangeSourceManager;


/**
 * The task manager factory for a change buffer.
 * 
 * @author Brett Henderson
 */
public class ChangeBufferFactory extends TaskManagerFactory {
	private static final String ARG_BUFFER_CAPACITY = "bufferCapacity";
	private static final int DEFAULT_BUFFER_CAPACITY = 100;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		int bufferCapacity;
		
		// Get the task arguments.
		bufferCapacity = getIntegerArgument(taskId, taskArgs, ARG_BUFFER_CAPACITY, DEFAULT_BUFFER_CAPACITY);
		
		return new ChangeSinkRunnableChangeSourceManager(
			taskId,
			new ChangeBuffer(bufferCapacity),
			pipeArgs
		);
	}
}
