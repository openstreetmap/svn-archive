// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.pgsql.common.BaseDao;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;


/**
 * Performs all action db operations.
 * 
 * @author Brett Henderson
 */
public class ActionDao extends BaseDao {
	private static final String SQL_INSERT = "INSERT INTO actions(data_type, action, id) VALUES(?, ?, ?)";
	private static final String SQL_TRUNCATE = "TRUNCATE actions";
	
	private boolean enabled;
	private PreparedStatement insertStatement;
	private PreparedStatement truncateStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public ActionDao(DatabaseContext dbCtx) {
		this(dbCtx, true);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 * @param enabled
	 *            Action records will only be written if this is set to true.
	 */
	public ActionDao(DatabaseContext dbCtx, boolean enabled) {
		super(dbCtx);
		
		this.enabled = enabled;
	}
	
	
	/**
	 * Adds the specified action to the database.
	 * 
	 * @param dataType The type of data being represented by this action. 
	 * @param action The action being performed on the data.
	 * @param id The identifier of the data. 
	 */
	public void addAction(ActionDataType dataType, ChangesetAction action, long id) {
		if (enabled) {
			int prmIndex;
			
			if (insertStatement == null) {
				insertStatement = prepareStatement(SQL_INSERT);
			}
			
			prmIndex = 1;
			
			try {
				insertStatement.setString(prmIndex++, dataType.getDatabaseValue());
				insertStatement.setString(prmIndex++, action.getDatabaseValue());
				insertStatement.setLong(prmIndex++, id);
				
				insertStatement.executeUpdate();
				
			} catch (SQLException e) {
				throw new OsmosisRuntimeException(
					"Unable to insert action with type=" + dataType + ", action=" + action + " and id=" + id + ".", e);
			}
		}
	}
	
	
	/**
	 * Removes all action records.
	 */
	public void truncate() {
		if (truncateStatement == null) {
			truncateStatement = prepareStatement(SQL_TRUNCATE);
		}
		
		try {
			truncateStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Truncate failed for users.",
				e
			);
		}
	}
}
