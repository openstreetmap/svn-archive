package org.openstreetmap.osmosis.history.v0_6.impl;

import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.impl.DatabaseCapabilityChecker;

public class HistoryDatabaseCapabilityChecker extends DatabaseCapabilityChecker {

	private boolean initialized;
	private boolean isHistorySupported;
	private boolean isWayNodesVersionSupported;
	private DatabaseContext dbCtx;
	private boolean isMinorVersionSupported;

	public HistoryDatabaseCapabilityChecker(DatabaseContext dbCtx) {
		super(dbCtx);
		
		this.dbCtx = dbCtx;
	}
	
	private void initialize() {
		if (!initialized) {
			isHistorySupported = dbCtx.doesColumnExist("way_nodes", "way_version");
			isWayNodesVersionSupported = dbCtx.doesColumnExist("way_nodes", "node_version");
			isMinorVersionSupported = dbCtx.doesColumnExist("way", "minor_version");
			
			initialized = true;
		}
	}
	public boolean isHistorySupported() {
		initialize();
		
		return isHistorySupported;
	}

	public boolean isWayNodesVersionSupported() {
		initialize();
		
		return isWayNodesVersionSupported;
	}

	public boolean isMinorVersionSupported() {
		initialize();
		
		return isMinorVersionSupported;
	}

}
