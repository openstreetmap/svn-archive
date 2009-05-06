// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pgsql.v0_6.impl;


/**
 * Defines all the data types supported by the action table.
 * 
 * @author Brett Henderson
 */
public enum ActionDataType {
	/**
	 * A user record.
	 */
	USER("U"),
	/**
	 * A node entity.
	 */
	NODE("N"),
	/**
	 * A way entity.
	 */
	WAY("W"),
	/**
	 * A relation entity.
	 */
	RELATION("R");
	
	
	private final String dbValue;
	
	
	private ActionDataType(String dbValue) {
		this.dbValue = dbValue;
	}
	
	
	/**
	 * Returns the database value representing this action.
	 * 
	 * @return The database value.
	 */
	public String getDatabaseValue() {
		return dbValue;
	}
}
