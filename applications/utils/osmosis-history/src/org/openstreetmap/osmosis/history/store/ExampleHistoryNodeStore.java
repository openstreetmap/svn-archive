package org.openstreetmap.osmosis.history.store;

import java.util.Collection;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;

public class ExampleHistoryNodeStore implements HistoryNodeStore {
	private Map<Long, Map<Integer, Node>> nodes = new HashMap<Long, Map<Integer, Node>>();
	private Map<Long, Map<Date, Integer>> timstamps = new HashMap<Long, Map<Date, Integer>>();
	
	@Override
	public void complete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void release() {
		nodes.clear();
	}

	@Override
	public void addNode(Node node) {
		long nodeId = node.getId();
		int version = node.getVersion();
		
		Map<Integer, Node> versions = nodes.get(nodeId);
		if(versions == null)
			versions = new HashMap<Integer, Node>();
		
		versions.put(version, node);
		nodes.put(nodeId, versions);
		
		Map<Date, Integer> timestampindex = timstamps.get(nodeId);
		if(timestampindex == null)
			timestampindex = new HashMap<Date, Integer>();
		
		timestampindex.put(node.getTimestamp(), version);
		timstamps.put(nodeId, timestampindex);
	}

	@Override
	public Node getNode(long nodeId, int version) {
		Map<Integer, Node> versions = nodes.get(nodeId);
		
		if(versions == null)
			return null;
		
		return versions.get(version);
	}

	@Override
	public Collection<Node> getNodeVersions(long nodeId) {
		Map<Integer, Node> versions = nodes.get(nodeId);
		
		if(versions == null)
			return null;
		
		return versions.values();
	}

	@Override
	public Node findNode(long nodeId, Date date) {
		Map<Date, Integer> timestampindex = timstamps.get(nodeId);
		
		if(timestampindex == null)
			return null;
		
		Date currentDate = null;
		Integer currentVersion = null;
		
		for(Map.Entry<Date, Integer> e : timestampindex.entrySet())
		{
			Date indexDate = e.getKey();
			Integer indexVersion = e.getValue();
			
			// this node-version is older the way
			if(indexDate.compareTo(date) > 0)
				continue;
			
			// this node-version is younger the one we already have 
			if(currentDate != null && indexDate.compareTo(currentDate) < 0)
				continue;
			
			currentDate = indexDate;
			currentVersion = indexVersion;
		}
		
		if(currentVersion == null)
			return null;
		
		return getNode(nodeId, currentVersion);
	}
}
