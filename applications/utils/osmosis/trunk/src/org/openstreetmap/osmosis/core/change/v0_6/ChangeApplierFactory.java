// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.change.v0_6;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.MultiSinkMultiChangeSinkRunnableSourceManager;


/**
 * The task manager factory for a change applier.
 * 
 * @author Brett Henderson
 */
public class ChangeApplierFactory extends TaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		return new MultiSinkMultiChangeSinkRunnableSourceManager(
			taskConfig.getId(),
			new ChangeApplier(10),
			taskConfig.getPipeArgs()
		);
	}
}
