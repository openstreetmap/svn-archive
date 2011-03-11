package org.openstreetmap.osmosis.history.v0_6.impl;

import java.util.Collection;
import java.util.Iterator;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.Completable;
import org.openstreetmap.osmosis.history.common.MinorEntityProcessor;
import org.openstreetmap.osmosis.history.domain.MinorWay;
import org.openstreetmap.osmosis.history.domain.MinorWayContainer;
import org.openstreetmap.osmosis.history.store.HistoryNodeStore;

public class MinorVersionBuilder implements Completable {
	private final MinorEntityProcessor entitiyProcessor;
	private Way lastWay;

	private final HistoryNodeStore nodeStore;

	public MinorVersionBuilder(MinorEntityProcessor entitiyProcessor, HistoryNodeStore nodeStore) {
		this.entitiyProcessor = entitiyProcessor;
		this.nodeStore = nodeStore;
	}

	public void processWay(Way way) {
		if(lastWay == null)
		{
			lastWay = way;
			return;
		}
		
		processDifferences(lastWay, way);
		lastWay = way;
	}

	private void processDifferences(Way way, Way nextWay) {
		int minorVersion = 0;
		for (WayNode wayNode : way.getWayNodes()) {
			Collection<Node> nodes = nodeStore.getNodeVersions(wayNode.getNodeId());
			
			Iterator<Node> iter = nodes.iterator();
			while(iter.hasNext())
			{
				Node node = iter.next();
				
				if(node.getTimestamp().compareTo(way.getTimestamp()) <= 0)
					continue; // too young
				
				if(nextWay != null)
				{
					if(node.getTimestamp().compareTo(nextWay.getTimestamp()) > 0)
						break; // too old
				}
				
				processDifferenceNode(way, node, ++minorVersion);
			}
		}
	}

	private void processDifferenceNode(Way way, Node node, int minorVersion) {
		MinorWay minorWay = new MinorWay(way.getId(), way.getVersion(), 
				minorVersion, node.getTimestamp(), node.getUser(), 
				node.getChangesetId(), way.getTags(), way.getWayNodes());
		
		entitiyProcessor.process(new MinorWayContainer(minorWay));
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
	}

	@Override
	public void complete() {
		if(lastWay != null)
			processDifferences(lastWay, null);
	}

}
