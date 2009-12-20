// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.database;


/**
 * Defines common constants shared between MySQL tasks.
 * 
 * @author Brett Henderson
 */
public interface DatabaseConstants {

    /**
     * The task argument for specifying an database authorisation properties file.
     */
    String TASK_ARG_AUTH_FILE = "authFile";

    /**
     * The task argument for specifying the host for a database connection.
     */
    String TASK_ARG_HOST = "host";

    /**
     * The task argument for specifying the database instance for a database connection.
     */
    String TASK_ARG_DATABASE = "database";

    /**
     * The task argument for specifying the user for a database connection.
     */
    String TASK_ARG_USER = "user";
    
    /**
     * The task argument for specifying the database type to be used.
     */
    String TASK_ARG_DB_TYPE = "dbType";

    /**
     * The task argument for specifying the password for a database connection.
     */
    String TASK_ARG_PASSWORD = "password";

    /**
     * The task argument for specifying whether schema version validation should be performed.
     */
    String TASK_ARG_VALIDATE_SCHEMA_VERSION = "validateSchemaVersion";

    /**
     * The task argument for specifying what should occur if an invalid schema version is
     * encountered.
     */
    String TASK_ARG_ALLOW_INCORRECT_SCHEMA_VERSION = "allowIncorrectSchemaVersion";

    /**
     * The task argument for forcing a utf-8 database connection.
     */
    String TASK_ARG_FORCE_UTF8 = "forceUtf8";

    /**
     * The task argument for enabling profiling on the database connection.
     */
    String TASK_ARG_PROFILE_SQL = "profileSql";

    /**
     * The default host for a database connection.
     */
    String TASK_DEFAULT_HOST = "localhost";

    /**
     * The default database for a database connection.
     */
    String TASK_DEFAULT_DATABASE = "osm";

    /**
     * The default user for a database connection.
     */
    String TASK_DEFAULT_USER = "osm";

    /**
     * The default password for a database connection.
     */
    DatabaseType TASK_DEFAULT_DB_TYPE = DatabaseType.POSTGRESQL;

    /**
     * The default password for a database connection.
     */
    String TASK_DEFAULT_PASSWORD = "";

    /**
     * The default value for whether schema version validation should be performed.
     */
    boolean TASK_DEFAULT_VALIDATE_SCHEMA_VERSION = true;

    /**
     * The default value for whether the program should allow an incorrect schema version.
     */
    boolean TASK_ALLOW_INCORRECT_SCHEMA_VERSION = false;

    /**
     * The default value for forcing a utf-8 connection.
     */
    boolean TASK_DEFAULT_FORCE_UTF8 = false;

    /**
     * The default value for enabling profile on a database connection.
     */
    boolean TASK_DEFAULT_PROFILE_SQL = false;
}
