package org.openstreetmap.josm.plugins.JunctionChecker.converting;

import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMNode;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMWay;

/**
 * erstellt einen ChannelDigraphen aus einem OSMGraphen
 * @author  Jörg Possin
 */
public class ChannelDigraphBuilder {

	private final ChannelDiGraph digraph;
	private final OSMGraph osmgraph;
	private final OSMWay[] osmways;
	private Channel newChannel = new Channel();
	private final NodesConnectionProducer ncp;
	// Variable wird für die IDs der neu erstellten Ways benötigt, die
	// Hinrichtung bekommt den des ursprungs-Way, die Rückrichtung
	// eine fiktive negative (OSM-XML-Standard für neue, noch nicht in der DB
	// gespeicherte Entities)
	private int newid = 1;
	private TurnRestrictionChecker trchecker;
	Channel backChannel;

	public ChannelDigraphBuilder(OSMGraph osmgraph) {
		//Nodesbeziehungen erstellen
		ncp = new NodesConnectionProducer(osmgraph);
		ncp.produceNodesConnections();
		digraph = new ChannelDiGraph();
		this.osmways = osmgraph.getWays();
		digraph.setBbbottom(osmgraph.getBbbottom());
		digraph.setBbleft(osmgraph.getBbleft());
		digraph.setBbright(osmgraph.getBbright());
		digraph.setBbtop(osmgraph.getBbtop());
		this.osmgraph = osmgraph;
	}

	private void setNewWayID(Channel channel) {
		channel.setNewid(newid);
		newid++;
	}

	/**
	 * Hilfemethode zur Erstellung eines Channels
	 * @param fromNode fromNode des zu erstellenden Channels
	 * @param toNode toNode  des zu erstellnenden Channels
	 * @param way ein zu dem Channel gehörender Way
	 * @return
	 */
	private Channel createChannel(OSMNode fromNode, OSMNode toNode, OSMWay way) {
		newChannel = new Channel();
		newChannel.setFromNode(fromNode);
		newChannel.setToNode(toNode);
		setNewWayID(newChannel);
		digraph.addChannel(newChannel);
		fromNode.addOutgoingChannel(newChannel);
		newChannel.addWay(way);
		return newChannel;
	}

	private void createBackChannel(OSMNode fromNode, OSMNode toNode, OSMWay way, Channel channel) {
		backChannel = new Channel();
		backChannel = createChannel(fromNode, toNode, way);
		backChannel.setBackChannelID(channel.getNewid());
		channel.setBackChannelID(backChannel.getNewid());
	}

	/**
	 * erzeugt den Digraphen
	 */
	private void buildChannels(OSMWay way, boolean oneway) {
		Channel tempChannel = new Channel();

		OSMNode lastUsedNode = way.getFromNode();
		OSMNode[] nodes = way.getNodes();
		/*
		 * log.debug("Way mit ID " + way.getId() + " , startnode: " +
		 * way.getFromNode().getId() + " , EndNode: " + way.getToNode().getId()
		 * + " wird bearbeitet.");
		 */
		// alle Knoten eines ways durchgehen
		for (int i = 1; i < nodes.length; i++) {
			// nur wenn der aktuelle Knoten mehr als einen Vorgänger/Nachfolger
			// hat, wird in die if-Abfrage gesprungen und ein neuer Channel mit
			//mit dem aktuell betrachtetem Node als Endnode erzeugt
			if (nodes[i].getPredNodeList().size() > 1 || nodes[i].getSuccNodeList().size() > 1) {
				tempChannel = createChannel(lastUsedNode, nodes[i], way);
				// bei Nichteinbahnstraße wird Channel in die andere Richtung
				// erzeugt
				if (oneway == false) {
					createBackChannel(nodes[i], lastUsedNode, way, tempChannel);
				}
				lastUsedNode = nodes[i];
			}
			// wenn der betrachtete Knoten keine Nachfolger hat, ist ein
			// Straßenende erreicht. Auch in diesem Fall muß ein Channel erzeugt werden
			else if (nodes[i].getSuccNodeList().size() == 0) {
				tempChannel = createChannel(lastUsedNode, nodes[i], way);
				// Rückrichtung wird nur erzeugt, wenn der OSM-Way keine Einbahnstraße ist
				if (oneway == false) {
					createBackChannel(nodes[i], lastUsedNode, way, tempChannel);
				}
			}
			// eine Straße besteht aus 2 Ways, obwohl eigentlich eine reicht
			// tritt z.b. bei einer brücke auf, brücke wird neuer channel
			//TODO: kann an dieser stelle das erzeugen von pseudo-channels verhindert werden?
			//      Idee: speichern eines flags, um diese erzeugten Channels zu markieren. aus diesen informationen
			//            später den CHannel löschen!!!
			else if (i == nodes.length - 1
					&& nodes[i].getSuccNodeList().size() == 1) {
				// damit ist ein Channel gefunden, und wird mit Werten gefüllt
				tempChannel = createChannel(lastUsedNode, nodes[i], way);
				// bei Nichteinbahnstraße wird Channel in die andere Richtung
				// erzeugt
				if (oneway == false) {
					createBackChannel(nodes[i], lastUsedNode, way, tempChannel);
				}
				lastUsedNode = nodes[i];

			}

		}
	}

	/**
	 * baut den ChannelDigraph
	 */
	public void buildChannelDigraph() {
		// alle Wege eines OSMGraphen durchgehen
		for (int i = 0; i < osmways.length; i++) {
			buildChannels(osmways[i], osmways[i].isOneWay());
		}
		trchecker = new TurnRestrictionChecker(osmgraph, digraph);
		trchecker.createLeadsTo();
		PseudoChannelRemover pcr = new PseudoChannelRemover(digraph); //überflüssige Channels entfernen
		pcr.removePseudoChannels();
	}

	public ChannelDiGraph getDigraph() {
		return digraph;
	}

	public void setNewid(int newid) {
		this.newid = newid;
	}

	public int getNewid() {
		return newid;
	}
}
