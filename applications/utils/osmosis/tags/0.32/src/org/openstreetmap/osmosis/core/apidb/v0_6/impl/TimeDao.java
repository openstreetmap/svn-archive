// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import java.util.Date;

import org.springframework.jdbc.core.JdbcTemplate;


/**
 * A DAO providing access to the system time on the database server. This avoids relying on the
 * clock of this system which may be different.
 */
public class TimeDao implements SystemTimeLoader {
	
	private JdbcTemplate jdbcTemplate;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param jdbcTemplate
	 *            Used to access the database.
	 */
	public TimeDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	
	/**
	 * Gets the system time of the database server.
	 * 
	 * @return The timestamp.
	 */
	public Date getSystemTime() {
		return (Date) jdbcTemplate.queryForObject("SELECT now() AS SystemTime", Date.class);
	}
}
