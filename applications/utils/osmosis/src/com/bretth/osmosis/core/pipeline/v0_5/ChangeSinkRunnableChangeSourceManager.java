// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pipeline.v0_5;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.common.ActiveTaskManager;
import com.bretth.osmosis.core.pipeline.common.PipeTasks;
import com.bretth.osmosis.core.task.v0_5.ChangeSinkRunnableChangeSource;
import com.bretth.osmosis.core.task.v0_5.ChangeSource;


/**
 * A task manager implementation for ChangeSinkRunnableChangeSource task implementations.
 * 
 * @author Brett Henderson
 */
public class ChangeSinkRunnableChangeSourceManager extends ActiveTaskManager {
	private ChangeSinkRunnableChangeSource task;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param taskId
	 *            A unique identifier for the task. This is used to produce
	 *            meaningful errors when errors occur.
	 * @param task
	 *            The task instance to be managed.
	 * @param pipeArgs
	 *            The arguments defining input and output pipes for the task,
	 *            pipes are a logical concept for identifying how the tasks are
	 *            connected together.
	 */
	public ChangeSinkRunnableChangeSourceManager(String taskId, ChangeSinkRunnableChangeSource task,
			Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(PipeTasks pipeTasks) {
		ChangeSource source;
		
		// Get the input task. A sink only has one input, this corresponds to
		// pipe index 0.
		source = (ChangeSource) getInputTask(pipeTasks, 0, ChangeSource.class);
		
		// Connect the tasks.
		source.setChangeSink(task);
		
		// Register the task as an output. A source only has one output, this
		// corresponds to pipe index 0.
		setOutputTask(pipeTasks, task, 0);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Runnable getTask() {
		return task;
	}
}
