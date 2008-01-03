// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pipeline.v0_5;

import java.util.Map;

import com.bretth.osmosis.core.pipeline.common.ActiveTaskManager;
import com.bretth.osmosis.core.pipeline.common.PipeTasks;
import com.bretth.osmosis.core.task.v0_5.ChangeSink;
import com.bretth.osmosis.core.task.v0_5.ChangeSource;
import com.bretth.osmosis.core.task.v0_5.MultiChangeSinkRunnableChangeSource;


/**
 * A task manager implementation for MultiChangeSinkRunnableChangeSource task implementations.
 * 
 * @author Brett Henderson
 */
public class MultiChangeSinkRunnableChangeSourceManager extends ActiveTaskManager {
	private MultiChangeSinkRunnableChangeSource task;
	
	
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
	public MultiChangeSinkRunnableChangeSourceManager(String taskId, MultiChangeSinkRunnableChangeSource task, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(PipeTasks pipeTasks) {
		// A multi sink receives multiple streams of data, so we must connect
		// them up one by one.
		for (int i = 0; i < task.getChangeSinkCount(); i++) {
			ChangeSink sink;
			ChangeSource source;
			
			// Retrieve the next sink.
			sink = task.getChangeSink(i);
			
			// Retrieve the appropriate source.
			source = (ChangeSource) getInputTask(pipeTasks, i, ChangeSource.class);
			
			// Connect the tasks.
			source.setChangeSink(sink);
		}
		
		// Register the source as an output task.
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
