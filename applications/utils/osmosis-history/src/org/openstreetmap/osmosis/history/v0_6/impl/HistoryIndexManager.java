package org.openstreetmap.osmosis.history.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.pgsnapshot.common.DatabaseContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Drops and creates indexes in support of bulk load activities.
 * 
 * @author Peter Koerner
 */
public class HistoryIndexManager {
	
	private final HistoryDatabaseCapabilityChecker capabilityChecker;
	private SimpleJdbcTemplate jdbcTemplate;

	public HistoryIndexManager(DatabaseContext dbCtx,
			HistoryDatabaseCapabilityChecker capabilityChecker) {
		
		jdbcTemplate = dbCtx.getSimpleJdbcTemplate();
		this.capabilityChecker = capabilityChecker;
	}

	public void prepareForLoad() {
		for(String stm : getPreparationStatements())
			jdbcTemplate.update(stm);
	}

	public void completeAfterLoad() {
		for(String stm : getCompletionStatements())
			jdbcTemplate.update(stm);
	}
	
	protected List<String> getPreparationStatements()
	{
		List<String> s = new ArrayList<String>();
		s.add("ALTER TABLE users DROP CONSTRAINT pk_users");
		s.add("ALTER TABLE nodes DROP CONSTRAINT pk_nodes");
		s.add("ALTER TABLE ways DROP CONSTRAINT pk_ways");
		s.add("ALTER TABLE way_nodes DROP CONSTRAINT pk_way_nodes");
		s.add("ALTER TABLE relations DROP CONSTRAINT pk_relations");
		s.add("ALTER TABLE relation_members DROP CONSTRAINT pk_relation_members");
		
		s.add("DROP INDEX IF EXISTS idx_nodes_geom");
		s.add("DROP INDEX IF EXISTS idx_way_nodes_node_id");
		s.add("DROP INDEX IF EXISTS idx_relation_members_member_id_and_type");
		
		if(capabilityChecker.isWayBboxSupported())
			s.add("DROP INDEX IF EXISTS idx_ways_bbox");
		
		if(capabilityChecker.isWayLinestringSupported())
			s.add("DROP INDEX IF EXISTS idx_ways_linestring");
		
		if(capabilityChecker.isWayNodesVersionSupported())
			s.add("DROP INDEX IF EXISTS way_nodes_node_version");
		
		return s;
	}
	
	protected List<String> getCompletionStatements()
	{
		List<String> s = new ArrayList<String>();
		s.add("ALTER TABLE ONLY users ADD CONSTRAINT pk_users PRIMARY KEY (id)");

		if(capabilityChecker.isWayBboxSupported())
		{
			s.add("CREATE INDEX idx_ways_bbox ON ways USING gist (bbox)");
			
			if(!capabilityChecker.isWayLinestringSupported())
				s.add("CLUSTER ways USING idx_ways_bbox");
		}
		if(capabilityChecker.isWayLinestringSupported())
		{
			s.add("CREATE INDEX idx_ways_linestring ON ways USING gist (linestring)");
			s.add("CLUSTER ways USING idx_ways_linestring");
		}
		
		if(capabilityChecker.isHistorySupported())
		{
			s.add("ALTER TABLE ONLY nodes ADD CONSTRAINT pk_nodes PRIMARY KEY (id, version)");
			s.add("ALTER TABLE ONLY relations ADD CONSTRAINT pk_relations PRIMARY KEY (id, version)");
			
			if(capabilityChecker.isMinorVersionSupported())
			{
				s.add("ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id, version, minor_version)");
				s.add("ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, way_version, minor_version, sequence_id)");
			}
			else
			{
				s.add("ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id, version)");
				s.add("ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, way_version, sequence_id)");
			}
			
			s.add("ALTER TABLE ONLY relation_members ADD CONSTRAINT pk_relation_members PRIMARY KEY (relation_id, relation_version, sequence_id)");
		}
		else
		{
			s.add("ALTER TABLE ONLY nodes ADD CONSTRAINT pk_nodes PRIMARY KEY (id)");
			s.add("ALTER TABLE ONLY ways ADD CONSTRAINT pk_ways PRIMARY KEY (id)");
			s.add("ALTER TABLE ONLY relations ADD CONSTRAINT pk_relations PRIMARY KEY (id)");
			
			s.add("ALTER TABLE ONLY way_nodes ADD CONSTRAINT pk_way_nodes PRIMARY KEY (way_id, sequence_id)");
			s.add("ALTER TABLE ONLY relation_members ADD CONSTRAINT pk_relation_members PRIMARY KEY (relation_id, sequence_id)");
		}
		
		return s;
	}
}
