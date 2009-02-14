// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_6;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.mysql.common.DatabaseContext;
import org.openstreetmap.osmosis.core.mysql.v0_6.impl.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.task.common.RunnableTask;


/**
 * A standalone OSM task with no inputs or outputs that truncates tables in a
 * mysql database. This is used for removing all existing data from tables.
 * 
 * @author Brett Henderson
 */
public class MysqlTruncator implements RunnableTask {
	
	// These SQL statements will be invoked to truncate each table.
	private static final String[] SQL_STATEMENTS = {
		"TRUNCATE current_relation_members",
		"TRUNCATE current_relation_tags",
		"TRUNCATE current_relations",
		"TRUNCATE current_way_nodes",
		"TRUNCATE current_way_tags",
		"TRUNCATE current_ways",
		"TRUNCATE current_node_tags",
		"TRUNCATE current_nodes",
		"TRUNCATE relation_members",
		"TRUNCATE relation_tags",
		"TRUNCATE relations",
		"TRUNCATE way_nodes",
		"TRUNCATE way_tags",
		"TRUNCATE ways",
		"TRUNCATE node_tags",
		"TRUNCATE nodes",
		"TRUNCATE changeset_tags",
		"TRUNCATE changesets",
		"TRUNCATE users"
	};
	
	
	private DatabaseContext dbCtx;
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
		dbCtx = new DatabaseContext(loginCredentials);
		
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials, preferences);
	}
	
	
	/**
	 * Truncates all data from the database.
	 */
	public void run() {
		try {
			schemaVersionValidator.validateVersion(MySqlVersionConstants.SCHEMA_MIGRATIONS);
			
			for (int i = 0; i < SQL_STATEMENTS.length; i++) {
				dbCtx.executeStatement(SQL_STATEMENTS[i]);
			}
			
		} finally {
			dbCtx.release();
		}
	}
}
