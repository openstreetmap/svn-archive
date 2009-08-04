// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.apidb.v0_6.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;


/**
 * Reads active transaction ids from the database allowing up-to-current queries to be performed
 * when extracting changesets from the history tables.
 */
public class TransactionDao implements TransactionSnapshotLoader {
	private SimpleJdbcTemplate jdbcTemplate;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param jdbcTemplate
	 *            Used to access the database.
	 */
	public TransactionDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = new SimpleJdbcTemplate(jdbcTemplate);
	}
	
	
	/**
	 * Obtains the current database snapshot.
	 * 
	 * @return The transaction snapshot.
	 */
	public TransactionSnapshot getTransactionSnapshot() {
		String snapshotString;
		TransactionSnapshot snapshot; 
		
		snapshotString = jdbcTemplate.queryForObject("SELECT txid_current_snapshot()", String.class);
		
		snapshot = new TransactionSnapshot(snapshotString);
		
		return snapshot;
	}
}
