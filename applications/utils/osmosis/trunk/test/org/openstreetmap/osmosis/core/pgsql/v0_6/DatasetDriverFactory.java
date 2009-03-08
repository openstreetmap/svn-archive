// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pgsql.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.DatasetSinkManager;


/**
 * The task manager factory for reading the entire contents of a dataset.
 * 
 * @author Brett Henderson
 */
public class DatasetDriverFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new DatasetSinkManager(
			taskConfig.getId(),
			new DatasetDriver(),
			taskConfig.getPipeArgs()
		);
	}
}
