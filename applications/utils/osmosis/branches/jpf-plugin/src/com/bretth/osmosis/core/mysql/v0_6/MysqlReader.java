// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.mysql.v0_6;

import java.util.Date;

import com.bretth.osmosis.core.OsmosisConstants;
import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.DatabasePreferences;
import com.bretth.osmosis.core.domain.v0_6.Bound;
import com.bretth.osmosis.core.domain.v0_6.NodeBuilder;
import com.bretth.osmosis.core.domain.v0_6.RelationBuilder;
import com.bretth.osmosis.core.domain.v0_6.WayBuilder;
import com.bretth.osmosis.core.lifecycle.ReleasableIterator;
import com.bretth.osmosis.core.mysql.v0_6.impl.EntityHistory;
import com.bretth.osmosis.core.mysql.v0_6.impl.EntityHistoryComparator;
import com.bretth.osmosis.core.mysql.v0_6.impl.EntitySnapshotReader;
import com.bretth.osmosis.core.mysql.v0_6.impl.NodeReader;
import com.bretth.osmosis.core.mysql.v0_6.impl.RelationReader;
import com.bretth.osmosis.core.mysql.v0_6.impl.SchemaVersionValidator;
import com.bretth.osmosis.core.mysql.v0_6.impl.WayReader;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.task.v0_6.RunnableSource;
import com.bretth.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data source reading from a database.  The entire contents of the database are read.
 * 
 * @author Brett Henderson
 */
public class MysqlReader implements RunnableSource {
	private Sink sink;
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private Date snapshotInstant;
	private boolean readAllUsers;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param snapshotInstant
	 *            The state of the node table at this point in time will be
	 *            dumped. This ensures a consistent snapshot.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public MysqlReader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences, Date snapshotInstant, boolean readAllUsers) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		this.snapshotInstant = snapshotInstant;
		this.readAllUsers = readAllUsers;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
	
	
	/**
	 * Reads all nodes from the database and sends to the sink.
	 */
	private void processNodes() {
		ReleasableIterator<NodeBuilder> reader;
		
		reader = new EntitySnapshotReader<NodeBuilder>(
			new PeekableIterator<EntityHistory<NodeBuilder>>(
				new NodeReader(loginCredentials, readAllUsers)
			),
			snapshotInstant,
			new EntityHistoryComparator<NodeBuilder>()
		);
		
		try {
			while (reader.hasNext()) {
				sink.process(new NodeContainer(reader.next().buildEntity()));
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all ways from the database and sends to the sink.
	 */
	private void processWays() {
		ReleasableIterator<WayBuilder> reader;
		
		reader = new EntitySnapshotReader<WayBuilder>(
			new PeekableIterator<EntityHistory<WayBuilder>>(
				new WayReader(loginCredentials, readAllUsers)
			),
			snapshotInstant,
			new EntityHistoryComparator<WayBuilder>()
		);
		
		try {
			while (reader.hasNext()) {
				sink.process(new WayContainer(reader.next().buildEntity()));
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all relations from the database and sends to the sink.
	 */
	private void processRelations() {
		ReleasableIterator<RelationBuilder> reader;
		
		reader = new EntitySnapshotReader<RelationBuilder>(
			new PeekableIterator<EntityHistory<RelationBuilder>>(
				new RelationReader(loginCredentials, readAllUsers)
			),
			snapshotInstant,
			new EntityHistoryComparator<RelationBuilder>()
		);
		
		try {
			while (reader.hasNext()) {
				sink.process(new RelationContainer(reader.next().buildEntity()));
			}
			
		} finally {
			reader.release();
		}
	}
	
	
	/**
	 * Reads all data from the database and send it to the sink.
	 */
	public void run() {
		try {
			new SchemaVersionValidator(loginCredentials, preferences).validateVersion(MySqlVersionConstants.SCHEMA_MIGRATIONS);
			
			sink.process(new BoundContainer(new Bound("Osmosis " + OsmosisConstants.VERSION)));
			processNodes();
			processWays();
			processRelations();
			
			sink.complete();
			
		} finally {
			sink.release();
		}
	}
}
