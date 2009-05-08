// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform.v0_5;


import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_5.SinkSourceManager;

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
