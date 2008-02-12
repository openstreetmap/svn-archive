// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.pgsql.common.BaseTableReader;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Reads all relations from a database ordered by their identifier. These relations won't
 * be populated with members and tags.
 * 
 * @author Brett Henderson
 */
public class RelationTableReader extends BaseTableReader<Relation> {
	private static final String SELECT_SQL =
		"SELECT id, user_name, tstamp"
		+ " FROM relation"
		+ " ORDER BY id";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 */
	public RelationTableReader(DatabaseContext dbCtx) {
		super(dbCtx);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		return queryDbCtx.executeQuery(SELECT_SQL);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReadResult<Relation> createNextValue(ResultSet resultSet) {
		long id;
		Date timestamp;
		String userName;
		
		try {
			id = resultSet.getLong("id");
			userName = resultSet.getString("user_name");
			timestamp = new Date(resultSet.getTimestamp("tstamp").getTime());
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read relation fields.", e);
		}
		
		return new ReadResult<Relation>(
			true,
			new Relation(id, timestamp, userName)
		);
	}
}
