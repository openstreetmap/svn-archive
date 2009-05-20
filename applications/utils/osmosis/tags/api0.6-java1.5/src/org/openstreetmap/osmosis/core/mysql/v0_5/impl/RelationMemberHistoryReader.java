// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.mysql.v0_5.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_5.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_5.RelationMember;
import org.openstreetmap.osmosis.core.mysql.common.BaseTableReader;
import org.openstreetmap.osmosis.core.mysql.common.DatabaseContext;


/**
 * Reads the most recent set of relation members from a database for relations
 * that have been modified within a time interval.
 * 
 * @author Brett Henderson
 */
public class RelationMemberHistoryReader extends BaseTableReader<EntityHistory<DBRelationMember>> {
	private static final String SELECT_SQL =
		"SELECT rm.id AS relation_id, rm.member_type, rm.member_id, rm.member_role, rm.version"
		+ " FROM relation_members rm"
		+ " INNER JOIN ("
		+ "   SELECT id, MAX(version) as version"
		+ "   FROM relations"
		+ "   WHERE timestamp > ? AND timestamp <= ?"
		+ "   GROUP BY id"
		+ " ) relationList ON rm.id = relationList.id AND rm.version = relationList.version";
	
	private MemberTypeParser memberTypeParser;
	
	
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
	public RelationMemberHistoryReader(
			DatabaseLoginCredentials loginCredentials, Date intervalBegin, Date intervalEnd) {
		super(loginCredentials);
		
		memberTypeParser = new MemberTypeParser();
		
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
	protected ReadResult<EntityHistory<DBRelationMember>> createNextValue(ResultSet resultSet) {
		long relationId;
		EntityType memberType;
		long memberId;
		String memberRole;
		int version;
		
		try {
			relationId = resultSet.getLong("relation_id");
			memberType = memberTypeParser.parse(resultSet.getString("member_type"));
			memberId = resultSet.getLong("member_id");
			memberRole = resultSet.getString("member_role");
			version = resultSet.getInt("version");
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to read relation member fields.", e);
		}
		
		return new ReadResult<EntityHistory<DBRelationMember>>(
			true,
			new EntityHistory<DBRelationMember>(
				new DBRelationMember(
					relationId,
					new RelationMember(
						memberId,
						memberType,
						memberRole
					)
				),
				version, true
			)
		);
	}
}
