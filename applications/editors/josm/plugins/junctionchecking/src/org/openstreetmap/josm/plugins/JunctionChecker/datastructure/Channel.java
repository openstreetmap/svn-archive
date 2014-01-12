package org.openstreetmap.josm.plugins.JunctionChecker.datastructure;

import java.util.ArrayList;
import java.util.HashMap;

import org.openstreetmap.josm.plugins.JunctionChecker.connectedness.BacktrackingColors;

/**
 * Diese Klasse erweitert BasicChannel um die Dinge, die für die Kreuzungsalgorithmen benötigt werden
 * @author  joerg
 */
public class Channel extends BasicChannel{

	//für den Junctioncheck
	private int indegree;
	private int outdegree;
	private boolean subgraph;
	private int visited = BacktrackingColors.WHITE;
	private final ArrayList<Channel> reachableNodes = new ArrayList<Channel>();
	private int ennr;
	private boolean isStrongConnected = true;
	private boolean isSelected = false; //wird für den eigenen Layer benötigt, um markierte Channels zu erhalten
	private boolean isPartOfJunction = false; //wird für den eigenen Layer benötigt, um Teile einer Kreuzung farbig repräsentieren zu können
	
	private final HashMap<Channel , ArrayList<Channel>> paths2 = new HashMap<Channel , ArrayList<Channel>>();


	public Channel(OSMNode fromNode, OSMNode toNode) {
		super();
		this.setFromNode(fromNode);
		this.setToNode(toNode);
		this.subgraph = false;
		this.indegree = 0;
		this.outdegree = 0;
	}

	public Channel(){
		super();
		//this.insidenodes = new ArrayList<OSMNode>();
		this.subgraph = false;
		this.indegree = 0;
		this.outdegree = 0;
	}

	/**
	 * gibt die Anzahl der auf diesen Channel verweisenden leadsTo zurück
	 * @return
	 */
	public int getIndegree() {
		return indegree;
	}

	/**
	 * gibt die Anzahl der aus diesem Channel gehenden leadsTo zurück
	 * @return
	 */
	public int getOutdegree() {
		return outdegree;
	}

	public void setIndegree(int i) {
		this.indegree = i;
	}

	public void setOutdegree(int i){
		this.outdegree = i;
	}

	/**
	 * ist dieser Channel Teil der zu untersuchenden Kreuzung?
	 */
	public boolean isSubgraph() {
		return subgraph;
	}

	public void setSubgraph(boolean subgraph) {
		this.subgraph = subgraph;
	}

	/**
	 * setzt die Farbe des Channels für den TRDFS white = unbesucht grey = besucht, aber noch nicht beendet black = besucht nund abgeschlossen
	 */
	public int getVisited() {
		return visited;
	}

	public void setVisited(int visited) {
		this.visited = visited;
	}

	/**
	 * gibt die von diesem Channel zu erreichenden anderen CHannels zurück
	 * @return
	 */
	public ArrayList<Channel> getReachableNodes() {
		return reachableNodes;
	}

	/**
	 * setzt die zu erreichenden Channels alle anderen werden gelöscht
	 * @param  reachableNodes
	 */
	public int getEnnr() {
		return ennr;
	}

	/**
	 * setzt die Anzahl der EingangsChannel
	 * @param  ennr
	 */
	public void setEnnr(int ennr) {
		this.ennr = ennr;
	}

	/**
	 * erhöht den Wert der Anzhal der EingangsledasTo um 1
	 */
	public void countupIndegree() {
		indegree++;
	}

	/**
	 * erhöht den Wert der Anzahl der AusgangsleadsTo um 1
	 */
	public void countupOutdegree() {
		outdegree++;
	}

	/**
	 * fügt einen erreichbaren Channel hinzu
	 * @param node
	 */
	public void addReachableNode(Channel node) {
		if (!reachableNodes.contains(node)) {
			reachableNodes.add(node);
			paths2.put(node, new ArrayList<Channel>());
		}
	}

	/**
	 * gibt den an der Position i gespeicherten erreichbaren Channel zurück
	 * @param i
	 * @return
	 */
	public Channel getReachableNodeAt(int i) {
		return reachableNodes.get(i);
	}

	/**
	 * löscht alle erreichbaren Channels
	 */
	public void ereaseReachableNodes() {
		reachableNodes.clear();
	}

	/**
	 * setzt Wert der erreichbaren Eingangschannel auf 0
	 */
	public void setEnnrZero() {
		ennr = 0;
	}

	/**
	 * erhöht den Wert der Eingänge um 1
	 */
	public void increaseEnnr() {
		ennr++;
	}


	/**
	 * fügt einen Pfad den Pfaden zu
	 * @param path
	 */
	public void appendPath(Channel node, ArrayList<Channel> path) {
		for (int i = 0; i < path.size(); i++) {
			if (!paths2.get(node).contains(path.get(i))) {
				paths2.get(node).add(path.get(i));
			}
		}
	}

	public void appendChannelToPath(Channel node, Channel channel) {
		if (!paths2.containsKey(node)) {
			paths2.put(node, new ArrayList<Channel>());

		}
		if (!paths2.get(node).contains(channel)) {
			paths2.get(node).add(channel);

		}
	}

	/**
	 * gibt alle Pfade zurück
	 * @return
	 */
	public ArrayList<ArrayList<Channel>> getPaths() {
		ArrayList<ArrayList<Channel>> t = new ArrayList<ArrayList<Channel>>();
		t.addAll(paths2.values());
		return t;
	}

	public ArrayList<Channel> getPathsAt(Channel node) {
		if (paths2.containsKey(node)) {
			return paths2.get(node);
		}
		else {
			//log.error("das darf nicht bei Channel: " + this.getNewid() + ", kein Node " + node.getNewid());
			return null;
		}

	}

	public boolean isStrongConnected() {
		return isStrongConnected;
	}

	public void setStrongConnected(boolean isStrongConnected) {
		this.isStrongConnected = isStrongConnected;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isPartOfJunction() {
		return isPartOfJunction;
	}

	public void setPartOfJunction(boolean isPartOfJunction) {
		this.isPartOfJunction = isPartOfJunction;
	}

	
}