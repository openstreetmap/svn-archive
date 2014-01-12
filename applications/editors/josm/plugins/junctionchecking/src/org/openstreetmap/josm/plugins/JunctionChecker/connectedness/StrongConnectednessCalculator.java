package org.openstreetmap.josm.plugins.JunctionChecker.connectedness;

import java.util.ArrayList;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;

public class StrongConnectednessCalculator {

	private int index = 0;
	private final ArrayList<Channel> stack = new ArrayList<Channel>();
	private final ArrayList<ArrayList<Channel>> SCC = new ArrayList<ArrayList<Channel>>();
	private final int numberOfNodes;
	private int calculatedNodes = 0;
	private ArrayList<Channel> nsccchannels = new ArrayList<Channel>();
	private final ChannelDiGraph digraph;
	int biggestPart = 0;

	public StrongConnectednessCalculator(ChannelDiGraph digraph) {
		this.digraph = digraph;
		numberOfNodes = digraph.numberOfChannels();
	}

	private int findUncalculatedNodes() {
		for (int i = 0; i < numberOfNodes; i++) {
			if (digraph.getChannelAtPosition(i).getLowlink() == -1) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * berechnet die starken Zusammenhangskomponenten
	 */
	public void calculateSCC() {
		while (calculatedNodes != numberOfNodes) {
			//log.trace("calculatedNodes: " + calculatedNodes + ", gesamtnodes: " + numberOfNodes);
			tarjan(digraph.getChannelAtPosition(findUncalculatedNodes()));
		}
		//log.trace("Berechnung der starken Zusammenhangskomponenten beendet: \n " +numberOfNodes + " Nodes sind wie folgt aufgeteilt: ");
		for (int i = 0; i < SCC.size(); i++) {
			//log.trace("Komponente: " + i + " besteht aus " + SCC.get(i).size()+ " Knoten");
			/**
			 * for (int j = 1; j < list.getNumberOfNodes(); j++) { if
			 * (list.getAdjacencyListnodes()[j].getIndex()== -1) {
			 * System.out.println("====");
			 * System.out.println(list.getAdjacencyListnodes
			 * ()[j].getNode().toString()); } }
			 **/
		}
		findBiggestPiece();
		saveNotSCCChannel();
	}

	/**
	 * speichert alle Channel, die nicht stark zusammenhängend sind, in einer ArrayList
	 **/
	private void saveNotSCCChannel() {
		nsccchannels = new ArrayList<Channel>();
		for (int i = 0; i < SCC.size(); i++) {
			if (i != biggestPart) {
				nsccchannels.addAll(SCC.get(i));
			}
		}
		//alle Channels auf nicht zusammenhängend setzen
		for (int i = 0; i < nsccchannels.size(); i++) {
			nsccchannels.get(i).setStrongConnected(false);
		}
	}

	private void findBiggestPiece() {
		int number = 0;
		for (int i = 0; i < SCC.size(); i++) {
			if (SCC.get(i).size() > number) {
				biggestPart = i;
				number = SCC.get(i).size();
			}
		}
	}

	public String showNotstronglyConnectednessParts() {
		String s = new String();
		for (int i = 0; i < SCC.size(); i++) {
			if (i != biggestPart) {
				s += "GraphKomponente: " + i + "\n";
				for (int j = 0; j < SCC.get(i).size(); j++) {
					s += "Channel: " + SCC.get(i).get(j).getNewid();
				}
				s += "\n";
			}
		}
		return s;
	}

	/**
	 * gibt eine Arraylist mit all den Channels zurück, welche nicht
	 * im größten zusammenhägendem Teil des Channel-Digraphen sind
	 * @return
	 */
	public ArrayList<Channel> getNotConnectedChannels() {
		return nsccchannels;
	}

	private void tarjan(Channel v) {
		//log.trace("tarjan für channel aufgerufen mit id: " + v.getNewid());
		v.setIndex(index);
		v.setLowlink(index);
		index++;
		stack.add(0, v);
		//log.trace("channel "+v.getNewid() + " hat nachbarn: " + v.getLeadsTo().size());
		for (int i = 0; i < v.getLeadsTo().size(); i++) {
			//log.trace("schleifendurchlauf: " + i);
			Channel n = v.getLeadsTo().get(i).getToChannel();
			if (n.getIndex() == -1) {
				//log.trace("n hat index =-1");
				tarjan(n);
				v.setLowlink(Math.min(v.getLowlink(), n.getLowlink()));
			}
			else if (stack.contains(n)) {
				//log.trace("setze lowlink von n auf: " + v.getLowlink());
				v.setLowlink(Math.min(v.getLowlink(), n.getLowlink()));
			}
		}
		if (v.getLowlink() == v.getIndex()) {
			Channel n;
			ArrayList<Channel> component = new ArrayList<Channel>();
			do {
				n = stack.remove(0);
				component.add(n);
			} while (n != v);
			SCC.add(component);
			calculatedNodes += component.size();
		}
	}
}
