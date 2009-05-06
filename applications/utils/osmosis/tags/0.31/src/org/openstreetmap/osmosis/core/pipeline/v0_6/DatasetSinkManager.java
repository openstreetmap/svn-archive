// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pipeline.v0_6;

import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.PassiveTaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.PipeTasks;
import org.openstreetmap.osmosis.core.task.v0_6.DatasetSink;
import org.openstreetmap.osmosis.core.task.v0_6.DatasetSource;


/**
 * A task manager implementation for task performing dataset sink.
 * 
 * @author Brett Henderson
 */
public class DatasetSinkManager extends PassiveTaskManager {
	private DatasetSink task;
	
	
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
	public DatasetSinkManager(String taskId, DatasetSink task, Map<String, String> pipeArgs) {
		super(taskId, pipeArgs);
		
		this.task = task;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(PipeTasks pipeTasks) {
		DatasetSource source;
		
		// Get the input task. A sink only has one input, this corresponds to
		// pipe index 0.
		source = (DatasetSource) getInputTask(pipeTasks, 0, DatasetSource.class);
		
		// Connect the tasks.
		source.setDatasetSink(task);
	}
}
