// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.history.v0_6;

import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.history.store.HistoryNodeStoreType;
import org.openstreetmap.osmosis.history.v0_6.impl.HistoryCopyFilesetBuilder;
import org.openstreetmap.osmosis.history.v0_6.impl.HistoryCopyFilesetLoader;
import org.openstreetmap.osmosis.history.v0_6.impl.HistoryDatabaseCapabilityChecker;
import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.impl.TempCopyFileset;


/**
 * An OSM data sink for storing all data to a database using the COPY command.
 * This task is intended for writing to an empty database.
 * 
 * @author Peter Koerner
 */
public class PostgreSqlHistoryCopyWriter implements Sink {
	
	private static final Logger LOG = Logger.getLogger(PostgreSqlHistoryCopyWriter.class.getName());
	
	private HistoryCopyFilesetBuilder copyFilesetBuilder;
	private HistoryCopyFilesetLoader copyFilesetLoader;
	private TempCopyFileset copyFileset;
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private HistoryNodeStoreType storeType;
	private boolean enableBboxBuilder;
	private boolean enableLinestringBuilder;
	private boolean enableWayNodeVersionBuilder;
	private boolean initialized;

	private boolean enableMinorVersionBuilder;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param storeType
	 *            The node location storage type used by the geometry builders.
	 */
	public PostgreSqlHistoryCopyWriter(
			DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
			HistoryNodeStoreType storeType) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		this.storeType = storeType;
		
		copyFileset = new TempCopyFileset();
	}
	
	
	private void initialize() {
		if (!initialized) {
			DatabaseContext dbCtx;
			HistoryDatabaseCapabilityChecker capabilityChecker;
			
			LOG.fine("Initializing the database and temporary processing files.");
			
			dbCtx = new DatabaseContext(loginCredentials);
			try {
				capabilityChecker = new HistoryDatabaseCapabilityChecker(dbCtx);

				boolean isHistoryCompatible = capabilityChecker.isHistorySupported();
				
				if(!isHistoryCompatible)
				{
					throw new OsmosisRuntimeException(
							"The database " + loginCredentials.getDatabase() + 
							" is not compatible with the history dump tasks. "+
							"Did you load the history schema sql files?");
				}
				
				enableBboxBuilder = capabilityChecker.isWayBboxSupported();
				enableLinestringBuilder = capabilityChecker.isWayLinestringSupported();
				enableWayNodeVersionBuilder = capabilityChecker.isWayNodesVersionSupported();
				enableMinorVersionBuilder = capabilityChecker.isMinorVersionSupported();
			} finally {
				dbCtx.release();
			}

			copyFilesetBuilder =
				new HistoryCopyFilesetBuilder(
						copyFileset, 
						enableBboxBuilder, 
						enableLinestringBuilder, 
						enableWayNodeVersionBuilder, 
						enableMinorVersionBuilder, 
						storeType);
			
			copyFilesetLoader = new HistoryCopyFilesetLoader(loginCredentials, preferences, copyFileset);
			
			LOG.fine("Processing input data, building geometries and creating database load files.");
			
			initialized = true;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		initialize();
		
		copyFilesetBuilder.process(entityContainer);
	}
	
	
	/**
	 * Writes any buffered data to the files, then loads the files into the database. 
	 */
	public void complete() {
		initialize();
		
		copyFilesetBuilder.complete();
		
		LOG.fine("All data has been received, beginning database load.");
		copyFilesetLoader.run();
		
		LOG.fine("Processing complete.");
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		if(copyFilesetBuilder != null)
			copyFilesetBuilder.release();
		
		if(copyFileset != null)
			copyFileset.release();
		
		initialized = false;
	}
}
