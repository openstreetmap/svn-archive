package org.openstreetmap.josm.plugins.JunctionChecker.junctionchecking;

import java.util.ArrayList;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;

/**
 * identifiziert die Ein/Ausgänge des übergebenden Teilgraphen. JPrepare setzt die Knotengrade von Vorgänger/nachfolgerknoten der übergebenden Knoten
 * @author  joerg
 */
public class JPrepare {

	private final ArrayList<Channel> entries;
	private final ArrayList<Channel> exits;
	private ArrayList<Channel> vertices;
	private final ChannelDiGraph digraph;

	public JPrepare(ChannelDiGraph digraph) {
		entries = new ArrayList<Channel>();
		exits = new ArrayList<Channel>();
		this.digraph = digraph;
	}

	public void jPrepare (ArrayList<Channel> vertices) {
		this.vertices = vertices;
		entries.clear();
		exits.clear();

		/*TODO: kann weg?
		digraph.ereaseChannelsInDegree();
		digraph.ereaseChannelsOutDegree();
		digraph.ereaseChannelsSubgraph();
		 */
		for (int i = 0; i < vertices.size(); i++) {
			vertices.get(i).setSubgraph(true);
		}
		for (int i = 0; i < vertices.size(); i++) {
			for (int j = 0; j < vertices.get(i).getPredChannels().size(); j++) {
				if (vertices.get(i).getPredChannels().get(j).isSubgraph() == false ) {
					if (!entries.contains(vertices.get(i))) {
						entries.add(vertices.get(i));
					}
				}
				else {
					vertices.get(i).countupIndegree();
					//log.trace(vertices.get(i).toString());
				}
			}
			for (int j = 0; j < vertices.get(i).getLeadsTo().size(); j++) {
				if (vertices.get(i).getLeadsTo().get(j).getToChannel().isSubgraph() == false) {
					if (!exits.contains(vertices.get(i))) {
						exits.add(vertices.get(i));
					}
				}
				else {
					vertices.get(i).countupOutdegree();
				}
			}
		}
	}

	public void resetSubgraph(){
		for (int i = 0; i < vertices.size(); i++) {
			vertices.get(i).setSubgraph(false);
			vertices.get(i).setIndegree(0);
			vertices.get(i).setOutdegree(0);
		}
	}

	/**
	 * gibt die Anzahl der gefundenen Eingänge zurück
	 * @return
	 */
	public ArrayList<Channel> getEntries() {
		return entries;
	}

	/**
	 * gibt die Anzahl der gefundenen Ausgänge zurück
	 * @return
	 */
	public ArrayList<Channel> getExits() {
		return exits;
	}
}
