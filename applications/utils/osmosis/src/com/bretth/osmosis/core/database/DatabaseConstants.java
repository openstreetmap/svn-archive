// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.database;


/**
 * Defines common constants shared between MySQL tasks.
 * 
 * @author Brett Henderson
 */
public interface DatabaseConstants {
	
	/**
	 * The task argument for specifying an database authorisation properties file.
	 */
	static final String TASK_ARG_AUTH_FILE = "authFile";
	
	/**
	 * The task argument for specifying the host for a database connection.
	 */
	static final String TASK_ARG_HOST = "host";
	
	/**
	 * The task argument for specifying the database instance for a database connection.
	 */
	static final String TASK_ARG_DATABASE = "database";
	
	/**
	 * The task argument for specifying the user for a database connection.
	 */
	static final String TASK_ARG_USER = "user";
	
	/**
	 * The task argument for specifying the password for a database connection.
	 */
	static final String TASK_ARG_PASSWORD = "password";
	
	/**
	 * The task argument for specifying whether schema version validation should be performed.
	 */
	static final String TASK_ARG_VALIDATE_SCHEMA_VERSION = "validateSchemaVersion";
	
	/**
	 * The task argument for forcing a utf-8 database connection.
	 */
	static final String TASK_ARG_FORCE_UTF8 = "forceUtf8";

	/**
	 * The task argument for enabling profiling on the database connection.
	 */
	static final String TASK_ARG_PROFILE_SQL = "profileSql";
	
	/**
	 * The default host for a database connection.
	 */
	static final String TASK_DEFAULT_HOST = "localhost";
	
	/**
	 * The default database for a database connection.
	 */
	static final String TASK_DEFAULT_DATABASE = "osm";
	
	/**
	 * The default user for a database connection.
	 */
	static final String TASK_DEFAULT_USER = "osm";
	
	/**
	 * The default password for a database connection.
	 */
	static final String TASK_DEFAULT_PASSWORD = "";
	
	/**
	 * The default value for whether schema version validation should be performed.
	 */
	static final boolean TASK_DEFAULT_VALIDATE_SCHEMA_VERSION = true;
	
	/**
	 * The default value for forcing a utf-8 connection.
	 */
	static final boolean TASK_DEFAULT_FORCE_UTF8 = false;
	
	/**
	 * The default value for enabling profile on a database connection.
	 */
	static final boolean TASK_DEFAULT_PROFILE_SQL = false;
}
