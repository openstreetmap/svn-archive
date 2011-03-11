// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.history.v0_6;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.database.DatabaseTaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;
import org.openstreetmap.osmosis.history.store.HistoryNodeStoreType;


/**
 * The task manager factory for a database writer using the PostgreSQL COPY method.
 * 
 * @author Peter Koenrer
 */
public class PostgreSqlHistoryCopyWriterFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_NODE_STORE_TYPE = "nodeStoreType";
	private static final String DEFAULT_NODE_STORE_TYPE = "Example";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		HistoryNodeStoreType storeType;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		storeType = Enum.valueOf(
				HistoryNodeStoreType.class,
				getStringArgument(taskConfig, ARG_NODE_STORE_TYPE, DEFAULT_NODE_STORE_TYPE));
		
		return new SinkManager(
			taskConfig.getId(),
			new PostgreSqlHistoryCopyWriter(loginCredentials, preferences,	storeType),
			taskConfig.getPipeArgs()
		);
	}
}
