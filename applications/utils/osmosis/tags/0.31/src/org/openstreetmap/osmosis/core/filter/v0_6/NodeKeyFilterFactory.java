// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.filter.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;


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
