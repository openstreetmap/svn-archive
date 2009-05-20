// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;

/**
 * Reads current relations from a database ordered by their identifier. These relations won't be
 * populated with members and tags.
 * 
 * @author Brett Henderson
 */
public class CurrentRelationTableReader extends BaseEntityReader<Relation> {

    private static final String SELECT_SQL =
    	"SELECT r.id, r.version, r.timestamp, r.visible, u.data_public, u.id AS user_id, u.display_name, r.changeset_id"
            + " FROM current_relations r"
            + " LEFT OUTER JOIN changesets c ON r.changeset_id = c.id"
            + " LEFT OUTER JOIN users u ON c.user_id = u.id" + " ORDER BY r.id";

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     * @param readAllUsers If this flag is true, all users will be read from the database regardless
     *        of their public edits flag.
     */
    public CurrentRelationTableReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
        super(loginCredentials, readAllUsers);
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
        int version;
        Date timestamp;
        boolean visible;
        OsmUser user;
        long changesetId;

        try {
            id = resultSet.getLong("id");
            version = resultSet.getInt("version");
            timestamp = new Date(resultSet.getTimestamp("timestamp").getTime());
            visible = resultSet.getBoolean("visible");
            user = readUserField(resultSet.getBoolean("data_public"), resultSet.getInt("user_id"), resultSet
                    .getString("display_name"));
            changesetId = resultSet.getLong("changeset_id");

        } catch (SQLException e) {
            throw new OsmosisRuntimeException("Unable to read relation fields.", e);
        }

        // Non-visible records will be ignored by the caller.
        return new ReadResult<Relation>(visible, new Relation(id, version, timestamp, user, changesetId));
    }
}
