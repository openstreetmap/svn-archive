// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.MultiChangeSinkRunnableChangeSourceManager;


/**
 * The task manager factory for a change appender.
 * 
 * @author Brett Henderson
 */
public class ChangeAppenderFactory extends TaskManagerFactory {
	private static final String ARG_SOURCE_COUNT = "sourceCount";
	private static final int DEFAULT_SOURCE_COUNT = 2;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		int sourceCount;
		
		sourceCount = getIntegerArgument(taskConfig, ARG_SOURCE_COUNT, DEFAULT_SOURCE_COUNT);
		
		return new MultiChangeSinkRunnableChangeSourceManager(
			taskConfig.getId(),
			new ChangeAppender(sourceCount),
			taskConfig.getPipeArgs()
		);
	}
}
