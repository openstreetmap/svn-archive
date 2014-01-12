package org.openstreetmap.josm.plugins.JunctionChecker.junctionchecking;

import java.util.ArrayList;
import org.openstreetmap.josm.plugins.JunctionChecker.connectedness.BacktrackingColors;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.LeadsTo;


/**
 * @author  Jörg Possin
 */
public class TRDFS {

	private final ArrayList<Channel> vertices;
	private Channel startNode;
	private final ArrayList<LeadsTo> cycleEdges;
	private final ChannelDiGraph digraph;

	/**
	 * 
	 * 
	 * @param adnodes
	 */
	public TRDFS(ArrayList<Channel> adnodes, ChannelDiGraph digraph) {
		this.vertices = adnodes;
		this.digraph = digraph;
		this.cycleEdges = new ArrayList<LeadsTo>();
	}


	public void trdfs(Channel startNode) {
		Channel succNode;
		startNode.setVisited(BacktrackingColors.GREY);
		startNode.addReachableNode(startNode);
		startNode.appendChannelToPath(startNode, startNode);
		for (int i = 0; i < startNode.getLeadsTo().size(); i++) {
			succNode = startNode.getLeadsTo().get(i).getToChannel();
			if (succNode.isSubgraph()) {
				if (succNode.getVisited() == BacktrackingColors.WHITE) {
					digraph.setForwardEdge(startNode, succNode);
					trdfs(succNode);
				} else if (succNode.getVisited() == BacktrackingColors.GREY) {
					cycleEdges.add(digraph.getLeadsTo(startNode,
							succNode));
				}
				for (int j = 0; j < succNode.getReachableNodes().size(); j++) {

					startNode.addReachableNode(succNode.getReachableNodeAt(j));
					succNode.appendChannelToPath(succNode.getReachableNodeAt(j), succNode.getReachableNodeAt(j));
					succNode.appendChannelToPath(succNode.getReachableNodeAt(j), succNode);
					startNode.appendPath(succNode.getReachableNodeAt(j), succNode.getPathsAt(succNode.getReachableNodeAt(j)));
				}
			}

		}
		startNode.setVisited(BacktrackingColors.BLACK);
	}

	public void ereaseCycleEdges() {
		cycleEdges.clear();
	}

	public int getCycleedgesSize() {
		return cycleEdges.size();
	}

	public LeadsTo getCycleEdgeAt(int i) {
		return cycleEdges.get(i);
	}

	public ArrayList<LeadsTo> getCycleEdges() {
		return cycleEdges;
	}
}