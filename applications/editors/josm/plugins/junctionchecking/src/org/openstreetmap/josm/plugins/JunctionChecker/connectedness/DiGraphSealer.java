package org.openstreetmap.josm.plugins.JunctionChecker.connectedness;

import java.util.ArrayList;
import java.util.Vector;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.LeadsTo;


/**
 * Diese Klasse versiegelt einen Digraphen, also die Enden eines outgoingChannels außerhalb einer Boundingbox werden mit dem Anfang eines bel. incomingChannels außerhalb der Boundigbox verbunden unbd umgekehrt
 * @author  joerg
 */
public class DiGraphSealer {


	// vorsichtshalber auf einen hohen negativen Wert gesetzt. besser
	// automatisch setzen!
	// TODO: NewID automatisch setzen
	private int newID = 1000000;
	private ChannelDiGraph digraph;

	public DiGraphSealer(ChannelDiGraph digraph) {
		this.digraph = digraph;
	}

	public DiGraphSealer(ChannelDiGraph digraph, int newID) {
		this.digraph = digraph;
		this.newID = newID;
	}

	/**
	 * versiegelt den vorher gesetzten DiGraphen
	 */
	public void sealingGraph() {
		Vector<Integer> outgoingChannelIDs = new Vector<Integer>();
		Vector<Integer> incomingChannelIDs = new Vector<Integer>();

		for (int i = 0; i < digraph.numberOfChannels(); i++) {
			if (digraph.isInBBox(digraph.getChannelAtPosition(i).getFromNode()) == false) {
				incomingChannelIDs.add(i);
			}
			if (digraph.isInBBox(digraph.getChannelAtPosition(i).getToNode()) == false) {
				outgoingChannelIDs.add(i);
			}
		}
		int counter = 0;
		Channel tempChannel;
		LeadsTo tempLeadsTo;
		for (int i = 0; i < outgoingChannelIDs.size(); i++) {
			if (digraph.getChannelAtPosition(outgoingChannelIDs.get(i))
					.getLeadsTo().size() == 0) {

				tempChannel = new Channel(digraph.getChannelAtPosition(
						outgoingChannelIDs.get(i)).getToNode(), digraph
						.getChannelAtPosition(incomingChannelIDs.get(0))
						.getFromNode());
				//dem Channel auch den neuen Channel als Nachfolger übergeben!!!
				//sonst gibts Probleme beim JunctionCheck
				tempLeadsTo = new LeadsTo(digraph
						.getChannelAtPosition(outgoingChannelIDs.get(i)),
						tempChannel);
				digraph.getChannelAtPosition(outgoingChannelIDs.get(i))
						.addLeadsTo(tempLeadsTo);
				digraph.addLeadsTo(tempLeadsTo);
				tempLeadsTo = new LeadsTo(tempChannel, digraph
						.getChannelAtPosition(incomingChannelIDs.get(0)));
				tempChannel.addLeadsTo(tempLeadsTo);
				digraph.addLeadsTo(tempLeadsTo);
				tempChannel.addWay(digraph.getChannelAtPosition(
						outgoingChannelIDs.get(i)).getWay());
				tempChannel.setNewid(newID);
				newID++;
				digraph.addChannel(tempChannel);
				counter++;
			}
		}

		for (int i = 0; i < incomingChannelIDs.size(); i++) {
			if (digraph.getChannelAtPosition(incomingChannelIDs.get(i))
					.getPredChannels().size() == 0) {
				tempChannel = new Channel(digraph.getChannelAtPosition(
						outgoingChannelIDs.get(0)).getToNode(), digraph
						.getChannelAtPosition(incomingChannelIDs.get(i))
						.getFromNode());
				//dem Channel auch den neuen Channel als Nachfolger überegeben
				// sonst gibt es Probleme beim JuncitonCheck
				digraph
				.getChannelAtPosition(incomingChannelIDs.get(i)).addPredChannel(tempChannel);
				tempLeadsTo = new LeadsTo(tempChannel, digraph
						.getChannelAtPosition(incomingChannelIDs.get(i)));
				tempChannel.addLeadsTo(tempLeadsTo);
				digraph.addLeadsTo(tempLeadsTo);
				tempLeadsTo = new LeadsTo(digraph
						.getChannelAtPosition(outgoingChannelIDs.get(0)),
						tempChannel);
				digraph.getChannelAtPosition(outgoingChannelIDs.get(0))
						.addLeadsTo(tempLeadsTo);
				digraph.addLeadsTo(tempLeadsTo);
				tempChannel.addWay(digraph.getChannelAtPosition(
						incomingChannelIDs.get(i)).getWay());
				tempChannel.setNewid(newID);
				newID++;
				digraph.addChannel(tempChannel);
				counter++;
			}
		}
		this.deleteDuplicateChannels();
	}
	
	/*
	private void showLeadsTo() {
		for (int i = 0; i < digraph.getChannels().size(); i++) {
			log.debug("Untersuche CHannel: " + digraph.getChannelAtPosition(i).getNewid());
			for (int j = 0; j < digraph.getChannelAtPosition(i).getLeadsTo().size(); j++) {
				log.debug("LeadsTo: " + digraph.getChannelAtPosition(i).getLeadsTo().get(j).toString());
			}
		}
	}*/

	/**
	 * dirt'n'quick methode zum löschen doppelter channels TODO:
	 * versiegeln-methode überarbeiten
	 *TODO: benutze ich die überhaupt noch?
	 */
	private void deleteDuplicateChannels() {
		ArrayList<Integer> pointer = new ArrayList<Integer>();

		for (int i = 0; i < digraph.getChannels().size(); i++) {
			for (int j = i + 1; j < digraph.getChannels().size(); j++) {
				if (digraph.getChannelAtPosition(i).getFromNode() == digraph
						.getChannelAtPosition(j).getFromNode()
						&& digraph.getChannelAtPosition(i).getToNode() == digraph
								.getChannelAtPosition(j).getToNode()) {
					digraph.removeChannel(digraph.getChannelAtPosition(j));
				}
			}

		}
	}

	/*
	 * setzt Wert für IDs für neu angelegte Ways
	 */
	/**
	 * @param newID
	 */
	public void setNewID(int newID) {
		this.newID = newID;
	}

	/**
	 * @return
	 * @uml.property  name="newID"
	 */
	public int getNewID() {
		return newID;
	}

	public ChannelDiGraph getDiGraph() {
		return digraph;
	}
}
