package com.bretth.osmosis.core.mysql.v0_4;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.task.common.RunnableTask;


/**
 * A standalone OSM task with no inputs or outputs that truncates tables in a
 * mysql database. This is used for removing all existing data from tables.
 * 
 * @author Brett Henderson
 */
public class MysqlTruncator implements RunnableTask {
	
	// These SQL statements will be invoked to truncate each table.
	private static final String INVOKE_TRUNCATE_NODE = "TRUNCATE nodes";
	private static final String INVOKE_TRUNCATE_SEGMENT = "TRUNCATE segments";
	private static final String INVOKE_TRUNCATE_WAY = "TRUNCATE ways";
	private static final String INVOKE_TRUNCATE_WAY_TAG = "TRUNCATE way_tags";
	private static final String INVOKE_TRUNCATE_WAY_SEGMENT = "TRUNCATE way_segments";
	private static final String INVOKE_TRUNCATE_NODE_CURRENT = "TRUNCATE current_nodes";
	private static final String INVOKE_TRUNCATE_SEGMENT_CURRENT = "TRUNCATE current_segments";
	private static final String INVOKE_TRUNCATE_WAY_CURRENT = "TRUNCATE current_ways";
	private static final String INVOKE_TRUNCATE_WAY_TAG_CURRENT = "TRUNCATE current_way_tags";
	private static final String INVOKE_TRUNCATE_WAY_SEGMENT_CURRENT = "TRUNCATE current_way_segments";
	
	
	private DatabaseContext dbCtx;
	private DatabasePreferences preferences;
	private SchemaVersionValidator schemaVersionValidator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public MysqlTruncator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		this.preferences = preferences;
		
		dbCtx = new DatabaseContext(loginCredentials);
		
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials);
	}
	
	
	/**
	 * Truncates all data from the database.
	 */
	public void run() {
		try {
			if (preferences.getValidateSchemaVersion()) {
				schemaVersionValidator.validateVersion(MySqlVersionConstants.SCHEMA_VERSION);
			}
			
			dbCtx.executeStatement(INVOKE_TRUNCATE_WAY_TAG_CURRENT);
			dbCtx.executeStatement(INVOKE_TRUNCATE_WAY_SEGMENT_CURRENT);
			dbCtx.executeStatement(INVOKE_TRUNCATE_WAY_CURRENT);
			dbCtx.executeStatement(INVOKE_TRUNCATE_SEGMENT_CURRENT);
			dbCtx.executeStatement(INVOKE_TRUNCATE_NODE_CURRENT);
			dbCtx.executeStatement(INVOKE_TRUNCATE_WAY_TAG);
			dbCtx.executeStatement(INVOKE_TRUNCATE_WAY_SEGMENT);
			dbCtx.executeStatement(INVOKE_TRUNCATE_WAY);
			dbCtx.executeStatement(INVOKE_TRUNCATE_SEGMENT);
			dbCtx.executeStatement(INVOKE_TRUNCATE_NODE);
			
		} finally {
			dbCtx.release();
		}
	}
}
