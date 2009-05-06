// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_5.WayNode;
import org.openstreetmap.osmosis.core.mysql.common.BaseTableReader;
import org.openstreetmap.osmosis.core.mysql.common.DatabaseContext;


/**
 * Reads the most recent set of way nodes from a database for ways that have
 * been modified within a time interval.
 * 
 * @author Brett Henderson
 */
public class WayNodeHistoryReader extends BaseTableReader<EntityHistory<DBWayNode>> {
	private static final String SELECT_SQL =
		"SELECT wn.id AS way_id, wn.node_id, wn.sequence_id, wn.version"
		+ " FROM way_nodes wn"
		+ " INNER JOIN ("
		+ "   SELECT id, MAX(version) as version"
		+ "   FROM ways"
		+ "   WHERE timestamp > ? AND timestamp <= ?"
		+ "   GROUP BY id"
		+ " ) wayList ON wn.id = wayList.id AND wn.version = wayList.version";
	
	
	private Date intervalBegin;
	private Date intervalEnd;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param intervalBegin
	 *            Marks the beginning (inclusive) of the time interval to be
	 *            checked.
	 * @param intervalEnd
	 *            Marks the end (exclusive) of the time interval to be checked.
	 */
	public WayNodeHistoryReader(DatabaseLoginCredentials loginCredentials, Date intervalBegin, Date intervalEnd) {
		super(loginCredentials);
		
		this.intervalBegin = intervalBegin;
		this.intervalEnd = intervalEnd;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ResultSet createResultSet(DatabaseContext queryDbCtx) {
		try {
			PreparedStatement statement;
			
			statement = queryDbCtx.prepareStatementForStreaming(SELECT_SQL);
			statement.setTimestamp(1, new Timestamp(intervalBegin.getTime()));
			statement.setTimestamp(2, new Timestamp(intervalEnd.getTime()));
			
			return statement.executeQuery();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to create streaming resultset.", e);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ReadResult<EntityHistory<DBWayNode>> createNextValue(ResultSet resultSet) {
		long wayId;
		long nodeId;
		int sequenceId;
		int version;
		
		try {
			wayId = resultSet.getLong("way_id");
			nodeId = resultSet.getLong("node_id");
			sequenceId = resultSet.getInt("sequence_id");
			version = resultSet.getInt("version");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read way node fields.", e);
		}
		
		return new ReadResult<EntityHistory<DBWayNode>>(
			true,
			new EntityHistory<DBWayNode>(
					new DBWayNode(wayId, new WayNode(nodeId), sequenceId), version, true)
		);
	}
}
