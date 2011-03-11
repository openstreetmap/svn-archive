// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.history.v0_6.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsnapshot.common.SchemaVersionValidator;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.PostgreSqlVersionConstants;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.impl.CopyFileset;


/**
 * Loads a COPY fileset into a database.
 * 
 * @author Peter Koerner
 */
public class HistoryCopyFilesetLoader implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(HistoryCopyFilesetLoader.class.getName());
	
	private DatabaseLoginCredentials loginCredentials;
	private DatabasePreferences preferences;
	private CopyFileset copyFileset;

	private HistoryDatabaseCapabilityChecker capabilityChecker;

	private DatabaseContext dbCtx;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param copyFileset
	 *            The set of COPY files to be loaded into the database.
	 */
	public HistoryCopyFilesetLoader(DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
			CopyFileset copyFileset) {
		this.loginCredentials = loginCredentials;
		this.preferences = preferences;
		this.copyFileset = copyFileset;
		
		dbCtx = new DatabaseContext(loginCredentials);
		capabilityChecker = new HistoryDatabaseCapabilityChecker(dbCtx);
	}
    

    /**
     * Reads all data from the file set and send it to the database.
     */
    public void run() {
    	DatabaseContext dbCtx = new DatabaseContext(loginCredentials);
    	
    	try {
			HistoryIndexManager indexManager;
			
			dbCtx.beginTransaction();
			
			SchemaVersionValidator validator = new SchemaVersionValidator(dbCtx.getSimpleJdbcTemplate(), preferences);
			validator.validateVersion(PostgreSqlVersionConstants.SCHEMA_VERSION);
			
    		indexManager = new HistoryIndexManager(dbCtx, capabilityChecker);
    		
			// Drop all constraints and indexes.
    		LOG.fine("Dropping constraints and indexes.");
			indexManager.prepareForLoad();
    		
    		LOG.finer("Loading users.");
    		dbCtx.loadCopyFile(copyFileset.getUserFile(), "users", getUserColumns().toArray(new String[0]));
    		
    		LOG.finer("Loading nodes.");
    		dbCtx.loadCopyFile(copyFileset.getNodeFile(), "nodes", getNodeColumns().toArray(new String[0]));
    		
    		LOG.finer("Loading ways.");
    		dbCtx.loadCopyFile(copyFileset.getWayFile(), "ways", getWayColumns().toArray(new String[0]));
    		
    		LOG.finer("Loading way nodes.");
    		dbCtx.loadCopyFile(copyFileset.getWayNodeFile(), "way_nodes", getWayNodeColumns().toArray(new String[0]));
    		
    		LOG.finer("Loading relations.");
    		dbCtx.loadCopyFile(copyFileset.getRelationFile(), "relations", getRelationColumns().toArray(new String[0]));
    		
    		LOG.finer("Loading relation members.");
    		dbCtx.loadCopyFile(copyFileset.getRelationMemberFile(), "relation_members", getRelationMemberColumns().toArray(new String[0]));
    		
    		LOG.fine("Data load complete.");
    		
    		// Add all constraints and indexes.
    		LOG.fine("Creating constraints and indexes.");
    		indexManager.completeAfterLoad();
    		
    		LOG.finer("Committing changes.");
    		dbCtx.commitTransaction();
    		
    		LOG.fine("Clustering database.");
    		dbCtx.getSimpleJdbcTemplate().update("CLUSTER");
    		
    		LOG.fine("Vacuuming database.");
    		dbCtx.getSimpleJdbcTemplate().update("VACUUM ANALYZE");
    		
    		LOG.fine("Complete.");
    		
    	} finally {
    		dbCtx.release();
    	}
    }
    
    protected List<String> getUserColumns()
    {
		List<String> c = new ArrayList<String>();
		c.add("id");
		c.add("name");
		
		return c;
    }
    
    protected List<String> getEntityColumns()
    {
		List<String> c = new ArrayList<String>();
		c.add("id");
		c.add("version");
		c.add("user_id");
		c.add("tstamp");
		c.add("changeset_id");
		c.add("tags");
		
		return c;
    }

    protected List<String> getNodeColumns()
    {
    	List<String> c = getEntityColumns();
		c.add("geom");
		
		return c;
    }
    
    protected List<String> getWayColumns()
    {
    	List<String> c = getEntityColumns();
    	c.add("nodes");
    	
    	if(capabilityChecker.isWayBboxSupported())
    		c.add("bbox");
    	
    	if(capabilityChecker.isWayLinestringSupported())
    		c.add("linestring");

    	if(capabilityChecker.isMinorVersionSupported())
    		c.add("minor_version");
    	
    	return c;
    }

    protected List<String> getRelationColumns()
    {
    	return getEntityColumns();
    }

    protected List<String> getWayNodeColumns()
    {
    	List<String> c = new ArrayList<String>();
    	c.add("way_id");
    	c.add("node_id");
    	c.add("sequence_id");
    	
    	if(capabilityChecker.isHistorySupported())
    		c.add("way_version");
    	
    	if(capabilityChecker.isWayNodesVersionSupported())
    		c.add("node_version");

    	if(capabilityChecker.isMinorVersionSupported())
    		c.add("minor_version");

		return c;
    }
   
    protected List<String> getRelationMemberColumns()
    {
    	List<String> c = new ArrayList<String>();
    	c.add("relation_id");
    	c.add("member_id");
    	c.add("member_type");
    	c.add("member_role");
    	c.add("sequence_id");
		
		return c;
    }
}
