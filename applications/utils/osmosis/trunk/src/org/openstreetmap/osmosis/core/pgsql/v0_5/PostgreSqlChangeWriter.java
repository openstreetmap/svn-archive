// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pgsql.v0_5;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_5.ChangeContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.pgsql.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.core.pgsql.v0_5.impl.ActionChangeWriter;
import org.openstreetmap.osmosis.core.pgsql.v0_5.impl.ChangeWriter;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_5.ChangeSink;


/**
 * A change sink writing to database tables. This aims to be suitable for
 * running at regular intervals with database overhead proportional to changeset
 * size.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlChangeWriter implements ChangeSink {
	
	private ChangeWriter changeWriter;
	private Map<ChangeAction, ActionChangeWriter> actionWriterMap;
	private SchemaVersionValidator schemaVersionValidator;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 */
	public PostgreSqlChangeWriter(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences) {
		changeWriter = new ChangeWriter(loginCredentials);
		actionWriterMap = new HashMap<ChangeAction, ActionChangeWriter>();
		actionWriterMap.put(ChangeAction.Create, new ActionChangeWriter(changeWriter, ChangeAction.Create));
		actionWriterMap.put(ChangeAction.Modify, new ActionChangeWriter(changeWriter, ChangeAction.Modify));
		actionWriterMap.put(ChangeAction.Delete, new ActionChangeWriter(changeWriter, ChangeAction.Delete));
		
		schemaVersionValidator = new SchemaVersionValidator(loginCredentials, preferences);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer change) {
		ChangeAction action;
		
		// Verify that the schema version is supported.
		schemaVersionValidator.validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
		
		action = change.getAction();
		
		if (!actionWriterMap.containsKey(action)) {
			throw new OsmosisRuntimeException("The action " + action + " is unrecognized.");
		}
		
		// Process the entity using the action writer appropriate for the change
		// action.
		change.getEntityContainer().process(actionWriterMap.get(action));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		changeWriter.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		changeWriter.release();
	}
}
