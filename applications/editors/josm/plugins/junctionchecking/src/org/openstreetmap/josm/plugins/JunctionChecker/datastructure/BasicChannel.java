package org.openstreetmap.josm.plugins.JunctionChecker.datastructure;

import java.util.ArrayList;

/**
 * BasicChannel hat alle Eigenschaften, die eine Channelklasse für einen DIgrpahen braucht.
 * @author  joerg
 */
public class BasicChannel {

	private OSMNode toNode;
	private OSMNode fromNode;
	private ArrayList<LeadsTo> leadsTo = new ArrayList<LeadsTo>();
	private final ArrayList<OSMWay> ways = new ArrayList<OSMWay>();
	private int newid;
	//gibt es nur, wenn ein Channelobjekt aus einer Nichteinbahnstraße erzeugt wurde (backchannelID ist dann die ID des anderen Channels)
	private int backChannelID = -100;
	private final ArrayList<Channel> predChannels = new ArrayList<Channel>();

	//werden für den Tarjan-Algorithmus gebraucht
	private int lowlink = -1;
	private int index = -1;

	/**
	 * fügt einen Vorgängerchannel hinzu
	 * @param channel der voherige Channel
	 */
	public void addPredChannel(Channel channel) {
		predChannels.add(channel);
	}

	/**
	 * setzt den ToNode
	 * @param toNode
	 */
	public void setToNode(OSMNode toNode) {
		this.toNode = toNode;
	}

	/**
	 * gbit den ToNode zurück
	 */
	public OSMNode getToNode() {
		return toNode;
	}

	/**
	 * setzt den FromNode
	 */
	public void setFromNode(OSMNode fromNode) {
		this.fromNode = fromNode;
	}

	/**
	 * gibt den FromNode zurück
	 */
	public OSMNode getFromNode() {
		return fromNode;
	}

	/**
	 * fügt eine LeadsTo-Beziehung hinzu
	 * @param leadsTo
	 */
	public void addLeadsTo(LeadsTo leadsTo) {
		this.leadsTo.add(leadsTo);
	}

	/**
	 * setzt alle leadsTo-Beziehungen (löscht alle voherigen)
	 */
	public void setLeadsTo(ArrayList<LeadsTo> leadsTo) {
		this.leadsTo = leadsTo;
	}

	/**
	 * löscht alle LeadsTo des Channels
	 */
	public void removeLeadsTo() {
		this.leadsTo.clear();
	}

	/**
	 * gibt alle leadsTo zurück
	 * @return
	 */
	public ArrayList<LeadsTo> getLeadsTo() {
		return leadsTo;
	}

	/**
	 * fügt einen Way hinzu, aus dem der Channel enstanden ist
	 * es gibt immer mind. einen Way, es können aber auch mehr sein
	 * @param way
	 */
	public void addWay(OSMWay way) {
		this.ways.add(way);
	}

	/**
	 * gibt alle Ways zurück
	 * @return
	 */
	public ArrayList<OSMWay> getWays() {
		return ways;
	}

	/**
	 * gibt nur den ersten Way der ArrayList zurück! wird bei der
	 * XML-datei-Erzeugung benutzt, um den Way, der aus dem Channel entsteht,
	 * mit Werten zu füllen dabei gehen Informationen verloren, da ein Channel
	 * aus mehr als einem Way bestehen kann
	 * 
	 * @return
	 */
	public OSMWay getWay() {
		return ways.get(0);
	}

	/**
	 * setzt die ID des Channels. es kann nicht die ID des Ways übernommen werden, da aus einem Way oftmals mehrere Channels entstehen (z.B. bei jeder Nichteinbahnstraße mind. 2)
	 */
	public void setNewid(int newid) {
		this.newid = newid;
	}

	/**
	 * gbit die NewID zurück
	 */
	public int getNewid() {
		return newid;
	}

	/**
	 * gibt alle VorgängerChannels zurück
	 * @return
	 */
	public ArrayList<Channel> getPredChannels() {
		return predChannels;
	}

	/**
	 * ToString Methode
	 */
	@Override
	public String toString() {
		String lt ="";
		for (int i = 0; i < leadsTo.size(); i++) {
			lt += leadsTo.get(i).getToChannel().getNewid() + ", ";
		}
		String predch = "";
		for (int i = 0; i < predChannels.size(); i++) {
			predch += predChannels.get(i).getNewid() + ", ";
		}
		return "[ChannelID: "+ newid + ":AnzahlPredCH: " + predChannels.size() + ":PredCh: " + predch +  ":AnzahlSuccCH: " + leadsTo.size() +":LeadsTo: " + lt+ ":backCHID: " + backChannelID + "]";
	}

	public void setBackChannelID(int backChannelID) {
		this.backChannelID = backChannelID;
	}

	public int getBackChannelID() {
		return backChannelID;
	}

	/**
	 * wandelt den Channel in einen OSMWay um
	 * dabie werden die Werte des 1. Way, der im Channel ist, übernommen, wenn ein
	 * 1. Channel existiert
	 */
	public OSMWay ToOSMWay() {
		OSMWay way = new OSMWay();
		way.addNode(this.fromNode);
		way.addNode(this.toNode);
		way.setId((long)this.newid);
		if (this.getWay() != null) {
			way.setHashmap(this.getWay().getHashmap());
		}
		return way;
	}

	public void ereasePredChannel(Channel rchannel) {
		predChannels.remove(rchannel);
	}

	public int getLowlink() {
		return lowlink;
	}

	public void setLowlink(int lowlink) {
		this.lowlink = lowlink;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void removeLeadsTo(LeadsTo leadsTo) {
		this.leadsTo.remove(leadsTo);
	}
}