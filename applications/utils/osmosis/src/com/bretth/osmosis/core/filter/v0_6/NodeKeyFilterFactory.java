package com.bretth.osmosis.core.filter.v0_6;

import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_6.SinkSourceManager;


/**
 * Extends the basic task manager factory functionality with used-node filter task
 * specific common methods.
 *
 * @author Brett Henderson
 * @author Christoph Sommer
 */
public class NodeKeyFilterFactory extends TaskManagerFactory {
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String keyList = getStringArgument(taskConfig, "keyList");
		return new SinkSourceManager(
			taskConfig.getId(),
			new NodeKeyFilter(keyList),
			taskConfig.getPipeArgs()
		);
	}

}
