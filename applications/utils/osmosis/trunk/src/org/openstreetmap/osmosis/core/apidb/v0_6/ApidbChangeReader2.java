// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6;

import java.util.Date;

import org.openstreetmap.osmosis.core.apidb.common.DatabaseContext2;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.NodeChangeReader;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.RelationChangeReader;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.apidb.v0_6.impl.WayChangeReader;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableChangeSource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;


/**
 * A change source reading from database history tables. This aims to be suitable for running at
 * regular intervals with database overhead proportional to changeset size.
 * 
 * @author Brett Henderson
 */
public class ApidbChangeReader2 implements RunnableChangeSource {

    private ChangeSink changeSink;
    private final DatabaseLoginCredentials loginCredentials;
    private final DatabasePreferences preferences;
    private final boolean readAllUsers;
    private final Date intervalBegin;
    private final Date intervalEnd;
    private final boolean fullHistory;


    /**
     * Creates a new instance.
     * 
     * @param loginCredentials Contains all information required to connect to the database.
     * @param preferences Contains preferences configuring database behaviour.
     * @param readAllUsers If this flag is true, all users will be read from the database regardless
     *        of their public edits flag.
     * @param intervalBegin Marks the beginning (inclusive) of the time interval to be checked.
     * @param intervalEnd Marks the end (exclusive) of the time interval to be checked.
     * @param fullHistory Specifies if full version history should be returned, or just a single
     *        change per entity for the interval.
     */
    public ApidbChangeReader2(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
            boolean readAllUsers, Date intervalBegin, Date intervalEnd, boolean fullHistory) {
        this.loginCredentials = loginCredentials;
        this.preferences = preferences;
        this.readAllUsers = readAllUsers;
        this.intervalBegin = intervalBegin;
        this.intervalEnd = intervalEnd;
        this.fullHistory = fullHistory;
    }

    /**
     * {@inheritDoc}
     */
    public void setChangeSink(ChangeSink changeSink) {
        this.changeSink = changeSink;
    }

    /**
     * Reads all node changes and sends them to the change sink.
     */
    private void processNodes() {
        NodeChangeReader reader = new NodeChangeReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd,
                fullHistory);

        try {
            while (reader.hasNext()) {
                changeSink.process(reader.next());
            }

        } finally {
            reader.release();
        }
    }

    /**
     * Reads all ways from the database and sends to the sink.
     */
    private void processWays() {
        WayChangeReader reader = new WayChangeReader(loginCredentials, readAllUsers, intervalBegin, intervalEnd,
                fullHistory);

        try {
            while (reader.hasNext()) {
                changeSink.process(reader.next());
            }

        } finally {
            reader.release();
        }
    }

    /**
     * Reads all relations from the database and sends to the sink.
     */
    private void processRelations() {
        RelationChangeReader reader = new RelationChangeReader(loginCredentials, readAllUsers, intervalBegin,
                intervalEnd, fullHistory);

        try {
            while (reader.hasNext()) {
                changeSink.process(reader.next());
            }

        } finally {
            reader.release();
        }
    }
    
    
    /**
     * Performs the main logic of the task.
     */
    protected void runImpl() {
    	try {
    		new SchemaVersionValidator(loginCredentials, preferences)
            	.validateVersion(ApidbVersionConstants.SCHEMA_MIGRATIONS);

		    processNodes();
		    processWays();
		    processRelations();

		    changeSink.complete();
		    
    	} finally {
    		changeSink.release();
    	}
    }
    

    /**
     * Reads all data from the file and send it to the sink.
     */
    public void run() {
    	DatabaseContext2 dbCtx = new DatabaseContext2(loginCredentials);
    	
        try {
            dbCtx.executeWithinTransaction(new TransactionCallbackWithoutResult() {

				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					runImpl();
				}});

        } finally {
            dbCtx.release();
        }
    }
}
