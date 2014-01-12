package org.openstreetmap.josm.plugins.JunctionChecker.converting;

import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMNode;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMWay;

/**
 * NodesConnectionProducer erstellt die Nachbarschaftsbeziehungen der einzelnen OSMNodes eines OSM-Graphen
 * aus diesen Beziehungen läßt sich der Channel-Digraph produzieren
 * @author  joerg
 */
public class NodesConnectionProducer {
	
	private OSMGraph osmgraph;
	private OSMWay[] osmways;

	public NodesConnectionProducer(OSMGraph osmgraph) {
		this.osmgraph = osmgraph;
		osmways = this.osmgraph.getWays();
	}
	
	public void produceNodesConnections() {
		OSMNode[] waynodes;
		for (int i = 0; i < osmways.length; i++) {
			waynodes = osmways[i].getNodes();
			for (int j = 0; j < waynodes.length - 1; j++) {
				waynodes[j].addSuccNode(waynodes[j+1]);
				waynodes[j+1].addPredNode(waynodes[j]);
			}
		}
	}

}
