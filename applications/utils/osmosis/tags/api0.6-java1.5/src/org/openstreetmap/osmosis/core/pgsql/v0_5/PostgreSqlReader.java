// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pgsql.v0_5;

import org.openstreetmap.osmosis.core.container.v0_5.Dataset;
import org.openstreetmap.osmosis.core.container.v0_5.DatasetReader;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.pgsql.v0_5.impl.PostgreSqlDatasetReader;
import org.openstreetmap.osmosis.core.task.v0_5.DatasetSink;
import org.openstreetmap.osmosis.core.task.v0_5.RunnableDatasetSource;


/**
 * An OSM dataset source exposing read-only access to a custom PostgreSQL database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlReader implements RunnableDatasetSource, Dataset {
	private DatasetSink datasetSink;
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public PostgreSqlReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public void setDatasetSink(DatasetSink datasetSink) {
		this.datasetSink = datasetSink;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public void run() {
		try {
			datasetSink.process(this);
			
		} finally {
			datasetSink.release();
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public DatasetReader createReader() {
		return new PostgreSqlDatasetReader(loginCredentials, preferences);
	}
}
