// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.v0_5;


import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.SinkSourceManager;

public class TransformTaskFactory extends TaskManagerFactory {

	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String configFile = getStringArgument(taskConfig, "file",
				getDefaultStringArgument(taskConfig, "transform.xml"));
		String statsFile = getStringArgument(taskConfig, "stats", null);
		return new SinkSourceManager(taskConfig.getId(),
				new TransformTask(configFile, statsFile),
				taskConfig.getPipeArgs());
	}

}
