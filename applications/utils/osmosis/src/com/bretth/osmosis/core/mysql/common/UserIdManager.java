// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Creates or loads the details of the Osmosis user in the database.
 * 
 * @author Brett Henderson
 */
public class UserIdManager {
	private static final String INSERT_SQL_USER =
		"INSERT INTO users (" +
		"email, active, pass_crypt," +
		" creation_time, display_name, data_public," +
		" description, home_lat, home_lon, home_zoom," +
		" nearby, pass_salt" +
		") VALUES (" +
		"'osmosis@bretth.com', 1, '00000000000000000000000000000000'," +
		" NOW(), 'Osmosis System User', 1," +
		" 'System user for the Osmosis toolset.', 0, 0, 3," +
		" 50, '00000000')";
	
	private static final String SELECT_SQL_USER =
		"SELECT id FROM users WHERE email='osmosis@bretth.com'";
	
	private static final String SELECT_LAST_INSERT_ID =
		"SELECT LAST_INSERT_ID() AS lastInsertId FROM DUAL";
	
	
	private DatabaseContext dbCtx;
	private boolean idLoaded;
	private long loadedUserId;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for all database access.
	 */
	public UserIdManager(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
		idLoaded = false;
	}
	
	
	/**
	 * Returns the id of the most recently inserted row on the current
	 * connection.
	 * 
	 * @return The newly inserted id.
	 */
	private long getLastInsertId() {
		try {
			ResultSet lastInsertQuery;
			long lastInsertId;
			
			lastInsertQuery = dbCtx.executeStreamingQuery(SELECT_LAST_INSERT_ID);
			
			lastInsertQuery.next();
			
			lastInsertId = lastInsertQuery.getLong("lastInsertId");
			
			lastInsertQuery.close();
			
			return lastInsertId;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to retrieve the id of the newly inserted user record.",
				e
			);
		}
	}
	
	
	/**
	 * Creates a new Osmosis user in the database.
	 * 
	 * @return The id of the newly created user.
	 */
	private long createNewUser() {
		dbCtx.executeStatement(INSERT_SQL_USER);
		
		return getLastInsertId();
	}
	
	
	/**
	 * Returns the id of an existing Osmosis user from the database.
	 * 
	 * @return The id of an existing user, -1 if no user exists.
	 */
	private long getExistingUser() {
		try {
			ResultSet existingUserQuery;
			long userId;
			
			existingUserQuery = dbCtx.executeStreamingQuery(SELECT_SQL_USER);
			
			if (existingUserQuery.next()) {
				userId = existingUserQuery.getLong("id");
			} else {
				userId = -1;
			}
			
			existingUserQuery.close();
			
			return userId;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to retrieve the id of an existing user record.",
				e
			);
		}
	}
	
	
	/**
	 * Returns the id of the Osmosis OSM user id in the database. It will create
	 * a new user if one doesn't exist.
	 * 
	 * @return The id of the user.
	 */
	public long getUserId() {
		if (!idLoaded) {
			long userId;
			
			// Retrieve the existing user if it exists.
			userId = getExistingUser();
			
			// If the user doesn't already exist, create a new one.
			if (userId < 0) {
				userId = createNewUser();
			}
			
			loadedUserId = userId;
			idLoaded = true;
		}
		
		return loadedUserId;
	}
}
