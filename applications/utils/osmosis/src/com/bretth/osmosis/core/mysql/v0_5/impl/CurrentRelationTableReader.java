// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.domain.v0_5.OsmUser;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.mysql.common.DatabaseContext;


/**
 * Reads current relations from a database ordered by their identifier. These relations
 * won't be populated with members and tags.
 * 
 * @author Brett Henderson
 */
public class CurrentRelationTableReader extends BaseEntityReader<Relation> {
	private static final String SELECT_SQL =
		"SELECT r.id, r.timestamp, u.data_public, u.id AS user_id, u.display_name, r.visible"
		+ " FROM current_relations r"
		+ " LEFT OUTER JOIN users u ON r.user_id = u.id"
		+ " ORDER BY r.id";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public CurrentRelationTableReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
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
	protected ReadResult<Relation> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		OsmUser user;
		boolean visible;
		
		try {
			id = resultSet.getLong("id");
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
		
		// Non-visible records will be ignored by the caller.
		return new ReadResult<Relation>(
			visible,
			new Relation(id, timestamp, user)
		);
	}
}
