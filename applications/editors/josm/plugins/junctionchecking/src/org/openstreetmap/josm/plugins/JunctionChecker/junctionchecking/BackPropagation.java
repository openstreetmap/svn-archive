package org.openstreetmap.josm.plugins.JunctionChecker.junctionchecking;

import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;


/**
 * Klasse für die Backpropagation
 * @author  joerg
 */
public class BackPropagation {

	private final ChannelDiGraph digraph;

	public BackPropagation(ChannelDiGraph digraph) {
		this.digraph = digraph;
	}

	/**
	 * 
	 * @param y fromNode
	 * @param z toNode
	 */
	public void backPropagation(Channel y, Channel z, Channel zstrich) {
		for (int i = 0; i < z.getReachableNodes().size(); i++) {
			y.addReachableNode(z.getReachableNodeAt(i));
			//z.appendChannelToPath(i, z);
			y.appendPath(z.getReachableNodeAt(i), z.getPathsAt(z.getReachableNodeAt(i)));
			y.appendPath(z.getReachableNodeAt(i), y.getPathsAt(z));
		}
		for (int i = 0; i < y.getPredChannels().size(); i++) {
			if (zstrich !=
				y.getPredChannels().get(i) &&
				digraph.getLeadsTo(
						y.getPredChannels().get(i), y).
						isForwardEdge()
			) {
				backPropagation(y.getPredChannels().get(i), y, zstrich);
			}
		}
	}
}
