package org.openstreetmap.josm.plugins.JunctionChecker.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author  joerg
 */
public class ChannelDiGraph extends Graph {

	private ArrayList<Channel> channels = new ArrayList<Channel>();
	private final ArrayList<LeadsTo> leadsTos = new ArrayList<LeadsTo>();
	private final HashSet<Channel> selectedChannels = new HashSet<Channel>();
	private HashSet<Channel> junctioncandidate = new HashSet<Channel>();

	public void setChannels(ArrayList<Channel> channels) {
		this.channels = channels;
	}

	/*
	 * gibt den Channel mit dem übergebendem OSMNode als FromNode zurück
	 */
	public Channel getChannelWithFromNode(OSMNode node) {
		for (int i = 0; i < channels.size(); i++) {
			if (channels.get(i).getFromNode() == node) {
				return channels.get(i);
			}
		}
		return null;
	}

	/**
	 * gibt alle CHannels des Digraphen zurück
	 * @return  Channels des Digraphen
	 */
	public ArrayList<Channel> getChannels() {
		return channels;
	}

	/**
	 * löscht den übergebenden Channel im Digraphen
	 * 
	 * @param channel
	 */
	public void removeChannel(Channel channel) {
		channels.remove(channel);
	}

	/**
	 * fügt einen Channel des ChannelDigraphen hinzu
	 * 
	 * @param channel
	 *            hinzuzufügender Channel
	 */
	public void addChannel(Channel channel) {
		this.channels.add(channel);
	}

	/**
	 * Anzahl der innerhalb des DiGraphen gespeicherten Channels
	 * 
	 * @return Anzahl der Channels
	 */
	public int numberOfChannels() {
		return channels.size();
	}

	/**
	 * gibt Channel i an der Position i in der ArrayList zurück
	 * 
	 * @param i
	 *            Position innerhalb der ArrayList
	 * @return gewünschter Channel
	 */
	public Channel getChannelAtPosition(int i) {
		return channels.get(i);
	}

	/**
	 * gibt den Channel mit der gesuchten ID zurück
	 * @param id ID des Channels
	 * @return der gesuchte Channel, wenn nicht vorhanden null
	 */
	public Channel getChannelWithID(int id) {
		for (int i = 0; i < channels.size(); i++) {
			if (channels.get(i).getNewid() == id) {
				return channels.get(i);
			}
		}
		return null;
	}

	/**
	 * gibt alle From und To OSMNodes eines Graphen zurück (nicht die
	 * ZWischenknoten)
	 * 
	 * @return alle From und To Nodes aller Channels des Digraphen
	 */
	public OSMNode[] getAllOSMNodes() {
		HashMap<Long, OSMNode> nodes = new HashMap<Long, OSMNode>();
		for (int i = 0; i < channels.size(); i++) {
			if (!nodes.containsKey(channels.get(i).getFromNode().getId())) {
				nodes.put(channels.get(i).getFromNode().getId(), channels
						.get(i).getFromNode());
			}
			if (!nodes.containsKey(channels.get(i).getToNode().getId())) {
				nodes.put(channels.get(i).getToNode().getId(), channels.get(i)
						.getToNode());
			}
		}
		OSMNode[] nodearray = new OSMNode[nodes.size()];
		return nodes.values().toArray(nodearray);
	}

	public ArrayList<LeadsTo> getLeadsTo() {
		return leadsTos;
	}

	/*
	public void setLeadsTo(ArrayList<LeadsTo> leadsTo) {
		this.leadsTos = leadsTo;
	}*/

	public void setForwardEdge(Channel fromChannel, Channel toChannel) {
		for (int i = 0; i < leadsTos.size(); i++) {
			if (leadsTos.get(i).getFromChannel() == fromChannel) {
				if (leadsTos.get(i).getToChannel() == toChannel)
					leadsTos.get(i).setForwardEdge(true);
			}
		}

	}

	/**
	 * fügt eine leadsto-relation dem digraphen und dem entsprechendem Channel hinzu
	 * @param leadsTo
	 */
	public void addLeadsTo(LeadsTo leadsTo) {
		leadsTos.add(leadsTo);
		for (int i = 0; i < channels.size(); i++) {
			if (channels.get(i).getNewid() == leadsTo.getFromChannel().getNewid()) {
				channels.get(i).addLeadsTo(leadsTo);
				return;
			}
		}
	}

	public void removeLeadsTo(LeadsTo leadsTo) {
		leadsTos.remove(leadsTo);
		for (int i = 0; i < channels.size(); i++) {
			if (channels.get(i).equals(leadsTo.getFromChannel())) {
				channels.get(i).removeLeadsTo(leadsTo);
				return;
			}
		}
	}

	/**
	 * gibt den Channel zurück, der paßt. Sind Channel doppelt vorhanden, wird
	 * nur der erste passende zurückgegeben!
	 * 
	 * @param fromChannel
	 * @param toChannel
	 * @return
	 */
	public LeadsTo getLeadsTo(Channel fromChannel, Channel toChannel) {
		for (int i = 0; i < leadsTos.size(); i++) {
			if (leadsTos.get(i).getFromChannel().getNewid() == fromChannel.getNewid()) {
				//log.trace("FromChannel mit ID gefunden: " + fromChannel.getNewid());
				if (leadsTos.get(i).getToChannel().getNewid() == toChannel.getNewid()) {
					//log.trace("Leads To gefunden: " + leadsTos.get(i).toString());
					return leadsTos.get(i);
				}
			}
		}
		return null;
	}

	/**
	 * gibt alle Channels zurück, die von diesen OSM-Knoten abgehen/hingehen
	 * @param nodes
	 * @return
	 */
	public ArrayList<Channel> getChannelsTouchingOSMNodes (ArrayList<OSMNode> nodes) {
		ArrayList<Channel> touchingChannel = new ArrayList<Channel>();
		for (int i = 0; i < nodes.size(); i++) {
			for (int j = 0; j < channels.size(); j++) {
				if (channels.get(j).getFromNode().getId() == nodes.get(i).getId()) {
					if (!touchingChannel.contains(channels.get(j))) {
						touchingChannel.add(channels.get(j));
					}
				}
				else if (channels.get(j).getToNode().getId() == nodes.get(i).getId()) {
					if (!touchingChannel.contains(channels.get(j))) {
						touchingChannel.add(channels.get(j));
					}
				}
			}
		}
		return touchingChannel;
	}

	public ArrayList<Channel> getChannelsTouchingOSMNode(long id) {
		ArrayList<Channel> returnchannels = new ArrayList<Channel>();
		for (int i = 0; i < channels.size(); i++) {
			if (channels.get(i).getFromNode().getId() == id) {
				returnchannels.add(channels.get(i));
			}
			if (channels.get(i).getToNode().getId() == id) {
				returnchannels.add(channels.get(i));
			}
		}
		return returnchannels;
	}

	/**
	 * gibt den oder die Channels twischen diesen OSM-Punkten zurück
	 * @param idfrom
	 * @param idto
	 * @return
	 */
	public ArrayList<Channel> getChannelsBetween(int idfrom, int idto) {
		ArrayList<Channel> channelsresult = new ArrayList<Channel>();
		for (int i = 0; i < channels.size(); i++) {
			if (channels.get(i).getFromNode().getId() == idfrom) {
				if (channels.get(i).getToNode().getId() == idto) {
					channelsresult.add(channels.get(i));
				}
			}
			else if (channels.get(i).getFromNode().getId() == idto) {
				if (channels.get(i).getToNode().getId() == idfrom) {
					channelsresult.add(channels.get(i));
				}
			}
		}
		return channelsresult;
	}

	public ArrayList<Channel> getChannelswithWayID(int id) {
		ArrayList<Channel> channelsresult = new ArrayList<Channel>();
		for (int i = 0; i < channels.size(); i++) {
			if (channels.get(i).getWay().getId() == id) {
				channelsresult.add(channels.get(i));
			}
		}
		return channelsresult;
	}

	public void detectSelectedChannels(double left, double top, double right, double bottom) {
		for (int i = 0; i < channels.size(); i++) {
			//log.trace(channels.get(i).getFromNode().toString());
			if ( (channels.get(i).getFromNode().getLatitude() <= top) && (channels.get(i).getFromNode().getLatitude() >= bottom)
					&& (channels.get(i).getFromNode().getLongitude() >= left) && (channels.get(i).getFromNode().getLongitude() <=right)) {
				channels.get(i).setSelected(true);
				selectedChannels.add(channels.get(i));
			}
			if ( (channels.get(i).getToNode().getLatitude() <= top) && (channels.get(i).getToNode().getLatitude() >= bottom)
					&& (channels.get(i).getToNode().getLongitude() >= left) && (channels.get(i).getToNode().getLongitude() <=right)) {
				channels.get(i).setSelected(true);
				selectedChannels.add(channels.get(i));
			}
		}
	}

	/**
	 * löscht die markierten Channels aus der Liste der markierten Channels und setzt die
	 * Eigenschaft isSelected auf false
	 */
	public void ereaseSelectedChannels() {
		for (int i = 0; i < selectedChannels.size(); i++) {
			Iterator<Channel> it = selectedChannels.iterator();
			while (it.hasNext()) {
				it.next().setSelected(false);
			}
		}
		selectedChannels.clear();
	}

	public HashSet<Channel> getSelectedChannels() {
		return selectedChannels;
	}

	public HashSet<Channel> getJunctionCandidate() {
		return junctioncandidate;
	}

	public void ereaseJunctioncandidate() {
		Iterator<Channel> it = junctioncandidate.iterator();
		while (it.hasNext()) {
			it.next().setPartOfJunction(false);
		}
		junctioncandidate.clear();
	}

	/**
	 * setzt die Channels eines Kreuzungskandidaten
	 * falls in im Hashset vorher Channels gespeichert waren, werden diese vorher gelöscht!
	 * @param junctionCandidate
	 */
	public void setJunctioncandidate(HashSet<Channel> junctionCandidate) {
		this.junctioncandidate.clear();
		this.junctioncandidate = junctionCandidate;
		Iterator<Channel> it = junctionCandidate.iterator();
		while (it.hasNext()) {
			it.next().setPartOfJunction(true);
		}
	}

	public void addJunctioncandidateChannel(Channel channel) {
		junctioncandidate.add(channel);
		channel.setPartOfJunction(true);
	}

	/*TODO: kann weg oder?
	public void ereaseChannelsSubgraph() {
		for (int i = 0; i < channels.size(); i++) {
			channels.get(i).setSubgraph(false);
		}
	}

	public void ereaseChannelsInDegree() {
		for (int i = 0; i < channels.size(); i++) {
			channels.get(i).setIndegree(0);
		}
	}

	public void ereaseChannelsOutDegree() {
		for (int i = 0; i < channels.size(); i++) {
			channels.get(i).setOutdegree(0);
		}
	}
	 */
}
