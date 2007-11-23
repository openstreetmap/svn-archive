package com.bretth.osmosis.core.pgsql.common.v0_5;

import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.SchemaVersionValidator;
import com.bretth.osmosis.core.task.common.RunnableTask;


/**
 * A standalone OSM task with no inputs or outputs that truncates tables in a
 * mysql database. This is used for removing all existing data from tables.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlTruncator implements RunnableTask {
	
	// These SQL statements will be invoked to truncate each table.
	private static final String[] SQL_STATEMENTS = {
		"TRUNCATE api_user, node, node_current, node_tag"
	};
	
	
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
	public PostgreSqlTruncator(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
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
				schemaVersionValidator.validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
			}
			
			for (int i = 0; i < SQL_STATEMENTS.length; i++) {
				dbCtx.executeStatement(SQL_STATEMENTS[i]);
			}
			
			dbCtx.commit();
			
		} finally {
			dbCtx.release();
		}
	}
}
