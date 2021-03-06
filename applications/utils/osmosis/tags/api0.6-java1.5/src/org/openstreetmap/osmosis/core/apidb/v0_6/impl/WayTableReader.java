// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

/**
 * Reads all ways from a database ordered by their identifier. These ways won't be populated with
 * nodes and tags.
 * 
 * @author Brett Henderson
 */
public class WayTableReader extends BaseEntityReader<EntityHistory<Way>> {

    private static final String SELECT_SQL =
    	"SELECT w.id, w.version, w.timestamp, w.visible, u.data_public, u.id AS user_id, u.display_name, w.changeset_id"
            + " FROM ways w"
            + " LEFT OUTER JOIN changesets c ON w.changeset_id = c.id"
            + " LEFT OUTER JOIN users u ON c.user_id = u.id" + " ORDER BY w.id, w.version";

    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     * @param readAllUsers If this flag is true, all users will be read from the database regardless
     *        of their public edits flag.
     */
    public WayTableReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
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
	protected ReadResult<EntityHistory<Way>> createNextValue(ResultSet resultSet) {
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
            throw new OsmosisRuntimeException("Unable to read way fields.", e);
        }

        return new ReadResult<EntityHistory<Way>>(true, new EntityHistory<Way>(new Way(id, version, timestamp, user,
				changesetId), visible));
    }
}
