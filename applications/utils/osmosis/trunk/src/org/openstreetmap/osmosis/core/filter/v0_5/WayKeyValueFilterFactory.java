package org.openstreetmap.osmosis.core.filter.v0_5;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_5.SinkSourceManager;


/**
 * Extends the basic task manager factory functionality with used-node filter task
 * specific common methods.
 * 
 * @author Brett Henderson
 * @author Christoph Sommer
 */
public class WayKeyValueFilterFactory extends TaskManagerFactory {
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String keyValueList = getStringArgument(taskConfig, "keyValueList", "highway.motorway,highway.motorway_link,highway.trunk,highway.trunk_link");
		return new SinkSourceManager(
			taskConfig.getId(),
			new WayKeyValueFilter(keyValueList),
			taskConfig.getPipeArgs()
		);
	}

}
