// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_5;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.database.DatabaseTaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_5.SinkManager;


/**
 * The task manager factory for a database writer.
 * 
 * @author Brett Henderson
 */
public class MysqlWriterFactory extends DatabaseTaskManagerFactory {
	private static final String ARG_LOCK_TABLES = "lockTables";
	private static final String ARG_POPULATE_CURRENT_TABLES = "populateCurrentTables";
	private static final boolean DEFAULT_LOCK_TABLES = true;
	private static final boolean DEFAULT_POPULATE_CURRENT_TABLES = true;
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		DatabaseLoginCredentials loginCredentials;
		DatabasePreferences preferences;
		boolean lockTables;
		boolean populateCurrentTables;
		
		// Get the task arguments.
		loginCredentials = getDatabaseLoginCredentials(taskConfig);
		preferences = getDatabasePreferences(taskConfig);
		lockTables = getBooleanArgument(taskConfig, ARG_LOCK_TABLES, DEFAULT_LOCK_TABLES);
		populateCurrentTables = getBooleanArgument(
				taskConfig, ARG_POPULATE_CURRENT_TABLES, DEFAULT_POPULATE_CURRENT_TABLES);
		
		return new SinkManager(
			taskConfig.getId(),
			new MysqlWriter(loginCredentials, preferences, lockTables, populateCurrentTables),
			taskConfig.getPipeArgs()
		);
	}
}
