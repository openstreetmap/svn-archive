package org.openstreetmap.josm.plugins.JunctionChecker.junctionchecking;

import java.util.ArrayList;
import org.openstreetmap.josm.plugins.JunctionChecker.connectedness.BacktrackingColors;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.LeadsTo;


/**
 * erzeugt
 * @author  joerg
 */
public class JProcess {

	private ArrayList<Channel> vertices;
	private ChannelDiGraph digraph;
	private TRDFS trdfs;
	private BackPropagation backpropagation;
	private ArrayList<LeadsTo> cycleEdges;

	public JProcess(ArrayList<Channel> subgraph, ChannelDiGraph digraph) {
		this.digraph = digraph;
		this.vertices = subgraph;
		this.trdfs = new TRDFS(vertices, digraph);
		this.backpropagation = new BackPropagation(digraph);
	}

	/**
	 * ruft den TR-DFS und danach den Backpropagation-Algorithmus auf jPrepare
	 * muß vorher durchgelaufen sein (die Eingänge müssen bekannt sein)
	 * 
	 * @param entries
	 */
	public void jProcess(ArrayList<Channel> entries) {
		ArrayList<Channel> nodes = digraph.getChannels();
		// alle Knoten des Subgraphen auf unbesucht stellen und
		// die evtl. gespeicherten erreichbaren Knoten löschen
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).setVisited(BacktrackingColors.WHITE);
			nodes.get(i).ereaseReachableNodes();
		}
		// alle Kanten auf keine ForwardEdge stellen
		for (int i = 0; i < digraph.getLeadsTo().size(); i++) {
			digraph.getLeadsTo().get(i).setForwardEdge(false);
		}
		trdfs.ereaseCycleEdges();

		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getVisited() == BacktrackingColors.WHITE) {
				trdfs.trdfs(entries.get(i));
			}
		}
		cycleEdges = trdfs.getCycleEdges();
		for (int j = 0; j < cycleEdges.size(); j++) {
			backpropagation.backPropagation(cycleEdges.get(j).getFromChannel(),
					cycleEdges.get(j).getToChannel(), cycleEdges.get(j).getToChannel());
		}
	}
}