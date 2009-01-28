// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_5.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_5.Way;
import org.openstreetmap.osmosis.core.mysql.common.DatabaseContext;


/**
 * Reads all ways from a database ordered by their identifier. These ways won't
 * be populated with nodes and tags.
 * 
 * @author Brett Henderson
 */
public class WayTableReader extends BaseEntityReader<EntityHistory<Way>> {
	private static final String SELECT_SQL =
		"SELECT w.id, w.version, w.timestamp, u.data_public, u.id AS user_id, u.display_name, w.visible"
		+ " FROM ways w"
		+ " LEFT OUTER JOIN users u ON w.user_id = u.id"
		+ " ORDER BY w.id, w.version";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public WayTableReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		super(loginCredentials, readAllUsers);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		return queryDbCtx.executeStreamingQuery(SELECT_SQL);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReadResult<EntityHistory<Way>> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		OsmUser user;
		int version;
		boolean visible;
		
		try {
			id = resultSet.getLong("id");
			version = resultSet.getInt("version");
			timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
			user = readUserField(
				resultSet.getBoolean("data_public"),
				resultSet.getInt("user_id"),
				resultSet.getString("display_name")
			);
			visible = resultSet.getBoolean("visible");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way fields.", e);
		}
		
		return new ReadResult<EntityHistory<Way>>(
			true,
			new EntityHistory<Way>(new Way(id, timestamp, user), version, visible)
		);
	}
}
