package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

public class Utils {
	private static final Set<String> ROAD_HIGHWAY_VALUES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
	    "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link", "secondary", "secondary_link",
	    "tertiary", "residential", "unclassified", "road", "living_street", "service", "track", "pedestrian", "raceway",
	    "services")));
	
	public static boolean isRoad(Way w) {
		return ROAD_HIGHWAY_VALUES.contains(w.get("highway"));
	}
	
	public static Node getMemberNode(Relation r, String role) {
		return getMember(r, role, OsmPrimitiveType.NODE).getNode();
	}
	
	public static RelationMember getMember(Relation r, String role, OsmPrimitiveType type) {
		final List<RelationMember> candidates = getMembers(r, role, type);
		
		if (candidates.size() == 0) {
			throw new IllegalStateException("No member with given role and type.");
		} else if (candidates.size() > 1) {
			throw new IllegalStateException(candidates.size() + " members with given role and type.");
		}
		
		return candidates.get(0);
	}
	
	public static List<RelationMember> getMembers(Relation r, String role, OsmPrimitiveType type) {
		final List<RelationMember> result = new ArrayList<RelationMember>();
		
		for (RelationMember m : r.getMembers()) {
			if (m.getRole().equals(role) && m.getType() == type) {
				result.add(m);
			}
		}
		
		return result;
	}
	
	public static List<Way> getMemberWays(Relation r, String role) {
		final List<Way> result = new ArrayList<Way>();
		
		for (RelationMember m : getMembers(r, role, OsmPrimitiveType.WAY)) {
			result.add(m.getWay());
		}
		
		return result;
	}
	
	public static List<Node> getMemberNodes(Relation r, String role) {
		final List<Node> result = new ArrayList<Node>();
		
		for (RelationMember m : getMembers(r, role, OsmPrimitiveType.NODE)) {
			result.add(m.getNode());
		}
		
		return result;
	}
	
	public static Node getOppositeEnd(Way w, Node n) {
		final boolean first = n.equals(w.firstNode());
		final boolean last = n.equals(w.lastNode());
		
		if (first && last) {
			throw new IllegalArgumentException("Way starts as well as ends at the given node.");
		} else if (first) {
			return w.lastNode();
		} else if (last) {
			return w.firstNode();
		} else {
			throw new IllegalArgumentException("Way neither starts nor ends at given node.");
		}
	}
}
