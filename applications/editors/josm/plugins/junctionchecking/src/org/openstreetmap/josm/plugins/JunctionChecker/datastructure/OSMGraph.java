package org.openstreetmap.josm.plugins.JunctionChecker.datastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;


/**
 * @author  joerg
 */
public class OSMGraph extends Graph{

	private final HashMap<Long, OSMWay> ways = new HashMap<Long, OSMWay>();
	private HashMap<Long, OSMRelation> relations = new HashMap<Long, OSMRelation>();
	private final HashMap<Long, OSMNode> nodes = new HashMap<Long, OSMNode>();

	public void addNode(OSMNode node) {
		nodes.put(node.getId(), node);
	}

	/**
	 * gibt den Knoten mit der gesuchten OSM-ID zurück
	 * @param id OSM-iD des Knotens!
	 * @return
	 */
	public OSMNode getNode(long id) {
		return nodes.get(id);
	}

	public void removeWay(OSMWay way) {
		ways.remove(way);
	}

	public OSMNode[] getNodes(){
		OSMNode[] nodearray= new OSMNode[nodes.size()];
		return nodes.values().toArray(nodearray);
	}

	public void addWay(OSMWay way) {
		ways.put(way.getId(), way);
	}

	public OSMWay getWay(long id) {
		return ways.get(id);
	}

	public OSMRelation getRelation(int id) {
		return relations.get(id);
	}

	public  HashMap<Long, OSMRelation> getRelationsAshashmap() {
		return relations;
	}

	public void setRelations( HashMap<Long, OSMRelation> relations) {
		this.relations = relations;
	}

	public OSMWay[] getWays() {
		OSMWay[] wayarray= new OSMWay[ways.size()];
		return ways.values().toArray(wayarray);
	}

	public void addRelation(OSMRelation relation) {
		relations.put(relation.getId(), relation);
	}

	public OSMRelation[] getRelations(){
		OSMRelation[] relationarray = new OSMRelation[relations.size()];
		return relations.values().toArray(relationarray);
	}

	public Collection<OSMRelation> getRelationsCollection() {
		return relations.values();
	}

	public boolean hasNode(Long id) {
		return nodes.containsKey(id);
	}

	public ArrayList<Long> getIDsfromWay(int id) {
		OSMWay w = ways.get(id);
		ArrayList<Long> ids  = new ArrayList<Long>();
		ids.add(w.getToNode().getId());
		ids.add(w.getFromNode().getId());
		return ids;
	}

	public void addNode(Node node) {
		OSMNode OSMnode = new OSMNode();
		OSMnode.setId(node.getId());
		OSMnode.setLatitude(node.getBBox().getTopLeft().lat());
		OSMnode.setLongitude(node.getBBox().getTopLeft().lon());
		OSMnode.setHashmap(new HashMap<String, String>(node.getKeys()));
		nodes.put(OSMnode.getId(), OSMnode);
	}

	public void addWay(Way way) {
		OSMWay osmway = new OSMWay();
		osmway.setId(way.getId());
		Iterator<Node> it = way.getNodes().iterator();
		while (it.hasNext()) {
			osmway.addNode(getNode(it.next().getId()));
		}
		osmway.setHashmap(new HashMap<String, String>(way.getKeys()));
		ways.put(osmway.getId(), osmway);
	}

	public void addRelation(Relation relation) {
		OSMRelation osmrelation = new OSMRelation();
		osmrelation.setId(relation.getId());
		osmrelation.setHashmap(new HashMap<String, String>(relation.getKeys()));
		RelationMember rmember;
		for (int i = 0; i < relation.getMembers().size(); i++) {
			rmember = relation.getMember(i);
			if (rmember.getMember() instanceof Node) {
				osmrelation.addMember(getNode(rmember.getMember().getId()), rmember.getRole());
			}
			else if (rmember.getMember() instanceof Way) {
				osmrelation.addMember(getWay(rmember.getMember().getId()), rmember.getRole());
			}
		}
		relations.put(osmrelation.getId(), osmrelation);
	}
}
