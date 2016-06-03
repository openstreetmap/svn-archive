package org.openstreetmap.josm.plugins.pt_assistant.utils;

import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

public final class StopToWayAssigner {
	
	private StopToWayAssigner() {
		// Hide default constructor for utils classes		
	}
	
	public static Way getWay(Node stop, Relation route) {
		List<Way> ways = new LinkedList<>();
		List<RelationMember> members = route.getMembers();
		for (RelationMember member: members) {
			if (member.getType().equals(OsmPrimitiveType.WAY)) {
				ways.add(member.getWay());
			}
		}
		// TODO: algorithm with growing bounding boxes
		return null;
	}

}
