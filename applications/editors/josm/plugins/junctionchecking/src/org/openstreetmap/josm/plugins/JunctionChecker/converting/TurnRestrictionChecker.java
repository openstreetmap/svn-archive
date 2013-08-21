package org.openstreetmap.josm.plugins.JunctionChecker.converting;

import java.util.ArrayList;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.LeadsTo;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMRelation;

/**
 * TurnRestrictionChecke prüft einen OSMGraphen auf vorhandene
 * Abbiegerestriktionen in den Relationen und paßt die Abiegerestriktionen der
 * Channels des aus dem OSMGraphen erzeugten ChannelDigraphen dementsprechend
 * an. wird vom ChannelDigraphBuilder aufgerufen
 * 
 * @author Jörg Possin
 */
public class TurnRestrictionChecker {

	private final ArrayList<OSMRelation> turnrestrictionsrelations = new ArrayList<OSMRelation>();
	private final ChannelDiGraph channelDigraph;
	private int relationpointer;
	private LeadsTo tempLeadsTo;

	public TurnRestrictionChecker(OSMGraph graph, ChannelDiGraph channelDigraph) {
		// von den Relationen des Graphen nur die Abbiegevorschriften kopieren
		for (int i = 0; i < graph.getRelations().length; i++) {
			if (graph.getRelations()[i].hasKey("type")) {
				if (graph.getRelations()[i].getValue("type").equals(
				"restriction")) {
					turnrestrictionsrelations.add(graph.getRelations()[i]);
				}
			}
		}
		this.channelDigraph = channelDigraph;
	}

	private boolean checkForRelations(Channel channel) {
		for (int k = 0; k < turnrestrictionsrelations.size(); k++) {
			//log.trace("betrachte relation in liste an position:" + k);
			for (int i = 0; i < channel.getWays().size(); i++) {
				if (turnrestrictionsrelations.get(k).getMember("from").getId() == channel
						.getWays().get(i).getId()
						&& turnrestrictionsrelations.get(k).getMember("via")
						.getId() == channel.getToNode().getId()) {
					relationpointer = k;
					return true;
				}
			}
		}
		return false;
	}

	private void produceLeadsToFromRelation(Channel fromChannel,
			Channel toChannel) {
		if (toChannel.getWays().contains(
				turnrestrictionsrelations.get(relationpointer).getMember("to"))) {
			if (turnrestrictionsrelations.get(relationpointer).getValue(
			"restriction").startsWith("only")) {

				tempLeadsTo = new LeadsTo(fromChannel, toChannel);
				channelDigraph.addLeadsTo(tempLeadsTo);
				toChannel.addPredChannel(fromChannel);
			}
			/*
			 * der no_* Fall: wie oben, nur das hier nichts geschieht
			 */
			else if (turnrestrictionsrelations.get(relationpointer).getValue(
			"restriction").startsWith("no")) {
				for (int i = 0; i < fromChannel.getToNode()
				.getOutgoingChannels().size(); i++) {
					if (fromChannel.getToNode().getOutgoingChannels().get(i) != toChannel) {
						tempLeadsTo = new LeadsTo(fromChannel, fromChannel
								.getToNode().getOutgoingChannels().get(i));
						channelDigraph.addLeadsTo(tempLeadsTo);
						fromChannel.getToNode().getOutgoingChannels().get(i)
						.addPredChannel(fromChannel);
					}
				}
			}
		}

	}

	/**
	 * startet die LeadsTo Erstellung
	 */
	public void createLeadsTo() {
		Channel tempChannel;
		for (int i = 0; i < channelDigraph.getChannels().size(); i++) {
			tempChannel = channelDigraph.getChannelAtPosition(i);
			boolean isInRelation = checkForRelations(tempChannel);
			for (int j = 0; j < tempChannel.getToNode().getOutgoingChannels()
			.size(); j++) {
				if (isInRelation) {
					produceLeadsToFromRelation(tempChannel, tempChannel
							.getToNode().getOutgoingChannels().get(j));
					// es wird nur dann ein leadsTo erzeugt, wenn der vom
					// Endknoten des Channels
					// ausgehende Channel NICHT der Channel in Rückrichtung ist
					// Ausnahme: es gibt nur diesen einen Channel (Wegende eines
					// Ways, der an keine weitere
					// Straße trifft
				} else if (tempChannel.getBackChannelID() != tempChannel
						.getToNode().getOutgoingChannels().get(j).getNewid()
						|| tempChannel.getToNode().getOutgoingChannels().size() == 1) {
					tempLeadsTo = new LeadsTo(tempChannel, tempChannel
							.getToNode().getOutgoingChannels().get(j));
					channelDigraph.addLeadsTo(tempLeadsTo);
					tempChannel.getToNode().getOutgoingChannels().get(j)
					.addPredChannel(tempChannel);
				}
			}
		}
	}
}
