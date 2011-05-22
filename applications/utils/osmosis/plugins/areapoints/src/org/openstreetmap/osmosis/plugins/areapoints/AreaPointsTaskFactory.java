package org.openstreetmap.osmosis.plugins.areapoints;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

public class AreaPointsTaskFactory extends TaskManagerFactory {

	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {

		SinkSource ss = new AreaPointsTask();

		return new SinkSourceManager(taskConfig.getId(),
				ss,
				taskConfig.getPipeArgs());
	}

}
