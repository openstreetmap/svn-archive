// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.history.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.impl.DirectoryCopyFileset;
import org.openstreetmap.osmosis.history.store.HistoryNodeStoreType;
import org.openstreetmap.osmosis.history.v0_6.impl.HistoryCopyFilesetBuilder;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data sink for storing all data to database dump files. This task is
 * intended for populating an empty database.
 * 
 * @author Peter Koerner
 */
public class PostgreSqlHistoryDumpWriter implements Sink {
	
	private HistoryCopyFilesetBuilder copyFilesetBuilder;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param filePrefix
	 *            The prefix to prepend to all generated file names.
	 * @param enableBboxBuilder
	 *            If true, the way bbox geometry is built during processing
	 *            instead of relying on the database to build them after import.
	 *            This increases processing but is faster than relying on the
	 *            database.
	 * @param enableLinestringBuilder
	 *            If true, the way linestring geometry is built during
	 *            processing instead of relying on the database to build them
	 *            after import. This increases processing but is faster than
	 *            relying on the database.
	 * @param storeType
	 *            The node location storage type used by the geometry builders.
	 */
	public PostgreSqlHistoryDumpWriter(
			File filePrefix, 
			boolean enableBboxBuilder,
			boolean enableLinestringBuilder, 
			boolean enableWayNodeVersionBuilder, 
			boolean enableMinorVersionBuilder, 
			HistoryNodeStoreType storeType) {
		
		DirectoryCopyFileset copyFileset;
		
		copyFileset = new DirectoryCopyFileset(filePrefix);
		
		copyFilesetBuilder =
			new HistoryCopyFilesetBuilder(
					copyFileset, 
					enableBboxBuilder, 
					enableLinestringBuilder, 
					enableWayNodeVersionBuilder, 
					enableMinorVersionBuilder, 
					storeType);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		copyFilesetBuilder.process(entityContainer);
	}
	
	
	/**
	 * Writes any buffered data to the database and commits. 
	 */
	public void complete() {
		copyFilesetBuilder.complete();
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		copyFilesetBuilder.release();
	}
}
