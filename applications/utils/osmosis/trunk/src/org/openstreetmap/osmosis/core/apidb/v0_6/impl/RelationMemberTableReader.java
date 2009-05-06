// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.BaseTableReader;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

/**
 * Reads all relation members from a database ordered by the relation identifier.
 * 
 * @author Brett Henderson
 */
public class RelationMemberTableReader extends BaseTableReader<DbFeatureHistory<DbOrderedFeature<RelationMember>>> {

    private static final String SELECT_SQL =
    	"SELECT id as relation_id, version, member_type, member_id, member_role, sequence_id"
            + " FROM relation_members" + " ORDER BY id, version";

    private final MemberTypeParser memberTypeParser;

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     */
    public RelationMemberTableReader(DatabaseLoginCredentials loginCredentials) {
        super(loginCredentials);

        memberTypeParser = new MemberTypeParser();
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
    protected ReadResult<DbFeatureHistory<DbOrderedFeature<RelationMember>>> createNextValue(ResultSet resultSet) {
        long relationId;
        EntityType memberType;
        long memberId;
        String memberRole;
        int sequenceId;
        int version;

        try {
            relationId = resultSet.getLong("relation_id");
            memberType = memberTypeParser.parse(resultSet.getString("member_type"));
            memberId = resultSet.getLong("member_id");
            memberRole = resultSet.getString("member_role");
            sequenceId = resultSet.getInt("sequence_id");
            version = resultSet.getInt("version");

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to read relation member fields.", e);
        }

        return new ReadResult<DbFeatureHistory<DbOrderedFeature<RelationMember>>>(true,
                new DbFeatureHistory<DbOrderedFeature<RelationMember>>(new DbOrderedFeature<RelationMember>(relationId,
                        new RelationMember(memberId, memberType, memberRole), sequenceId), version));
    }
}
