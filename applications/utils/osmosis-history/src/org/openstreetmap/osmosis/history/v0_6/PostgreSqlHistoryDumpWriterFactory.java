// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.history.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;
import org.openstreetmap.osmosis.history.store.HistoryNodeStoreType;


/**
 * The task manager factory for a database dump writer.
 * 
 * @author Peter Koerner
 */
public class PostgreSqlHistoryDumpWriterFactory extends TaskManagerFactory {
	private static final String ARG_ENABLE_BBOX_BUILDER = "enableBboxBuilder";
	private static final String ARG_ENABLE_LINESTRING_BUILDER = "enableLinestringBuilder";
	private static final String ARG_ENABLE_WAYNODE_VERSION_BUILDER = "enableWayNodeVersionBuilder";
	private static final String ARG_ENABLE_MINOR_VERSION_BUILDER = "enableMinorVersionBuilder";
	private static final String ARG_FILE_NAME = "directory";
	private static final String ARG_NODE_STORE_TYPE = "nodeStoreType";
	
	private static final boolean DEFAULT_ENABLE_BBOX_BUILDER = false;
	private static final boolean DEFAULT_ENABLE_LINESTRING_BUILDER = false;
	private static final boolean DEFAULT_ENABLE_WAYNODE_VERSION_BUILDER = false;
	private static final boolean DEFAULT_ENABLE_MINOR_VERSION_BUILDER = false;
	private static final String DEFAULT_FILE_PREFIX = "pgimport";
	private static final String DEFAULT_NODE_STORE_TYPE = "Example";
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String filePrefixString;
		File filePrefix;
		boolean enableBboxBuilder;
		boolean enableLinestringBuilder;
		boolean enableWayNodeVersionBuilder;
		boolean enableMinorVersionBuilder;
		HistoryNodeStoreType storeType;
		
		// Get the task arguments.
		filePrefixString = getStringArgument(
				taskConfig, ARG_FILE_NAME, DEFAULT_FILE_PREFIX);
		enableBboxBuilder = getBooleanArgument(
				taskConfig, ARG_ENABLE_BBOX_BUILDER, DEFAULT_ENABLE_BBOX_BUILDER);
		enableLinestringBuilder = getBooleanArgument(
				taskConfig, ARG_ENABLE_LINESTRING_BUILDER, DEFAULT_ENABLE_LINESTRING_BUILDER);
		enableWayNodeVersionBuilder = getBooleanArgument(
				taskConfig, ARG_ENABLE_WAYNODE_VERSION_BUILDER, DEFAULT_ENABLE_WAYNODE_VERSION_BUILDER);
		enableMinorVersionBuilder = getBooleanArgument(
				taskConfig, ARG_ENABLE_MINOR_VERSION_BUILDER, DEFAULT_ENABLE_MINOR_VERSION_BUILDER);
		storeType = Enum.valueOf(
				HistoryNodeStoreType.class,
				getStringArgument(taskConfig, ARG_NODE_STORE_TYPE, DEFAULT_NODE_STORE_TYPE));
		
		// Create a file object representing the directory from the file name provided.
		filePrefix = new File(filePrefixString);
		
		return new SinkManager(
			taskConfig.getId(),
			new PostgreSqlHistoryDumpWriter(
						filePrefix, 
						enableBboxBuilder, 
						enableLinestringBuilder, 
						enableWayNodeVersionBuilder, 
						enableMinorVersionBuilder, 
						storeType),
			taskConfig.getPipeArgs()
		);
	}
}
