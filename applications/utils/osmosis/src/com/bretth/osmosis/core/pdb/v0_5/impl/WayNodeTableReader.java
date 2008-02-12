// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_5.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_5.WayNode;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBWayNode;
import com.bretth.osmosis.core.pgsql.common.BaseTableReader;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Reads all way nodes from a database ordered by the way identifier but not
 * by the sequence.
 * 
 * @author Brett Henderson
 */
public class WayNodeTableReader extends BaseTableReader<DBWayNode> {
	private static final String SELECT_SQL =
		"SELECT way_id, node_id, sequence_id"
		+ " FROM way_node"
		+ " ORDER BY way_id";
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The active connection to use for reading from the database.
	 */
	public WayNodeTableReader(DatabaseContext dbCtx) {
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
	protected ReadResult<DBWayNode> createNextValue(ResultSet resultSet) {
		long wayId;
		long nodeId;
		int sequenceId;
		
		try {
			wayId = resultSet.getLong("way_id");
			nodeId = resultSet.getLong("node_id");
			sequenceId = resultSet.getInt("sequence_id");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way node fields.", e);
		}
		
		return new ReadResult<DBWayNode>(
			true,
			new DBWayNode(wayId, new WayNode(nodeId), sequenceId)
		);
	}
}
