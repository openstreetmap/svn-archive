package org.openstreetmap.josm.plugins.pt_assistant.data;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Representation of PTWays, which can be of OsmPrimitiveType Way or Relation
 * 
 * @author darya
 *
 */
public class PTWay extends RelationMember {

	/*
	 * Ways that belong to this PTWay. If the corresponding relation member is
	 * OsmPrimitiveType.WAY, this list size is 1. If the corresponding relation
	 * member is a nested relation, the list size is >= 1.
	 */
	private List<Way> ways = new ArrayList<Way>();

	/**
	 * 
	 * @param other
	 *            the corresponding RelationMember
	 * @throws IllegalArgumentException
	 *             if the given relation member cannot be a PTWay due to its
	 *             OsmPrimitiveType and/or role.
	 */
	public PTWay(RelationMember other) throws IllegalArgumentException {

		super(other);

		if (other.getType().equals(OsmPrimitiveType.WAY)) {
			ways.add(other.getWay());
		} else if (other.getType().equals(OsmPrimitiveType.RELATION)) {
			for (RelationMember rm : other.getRelation().getMembers()) {
				if (rm.getType().equals(OsmPrimitiveType.WAY)) {
					ways.add(rm.getWay());
				} else {
					throw new IllegalArgumentException(
							"A route relation member of OsmPrimitiveType.RELATION can only have ways as members");
				}
			}
		} else {
			// the RelationMember other cannot be a OsmPrimitiveType.NODE
			throw new IllegalArgumentException("A node cannot be used to model a public transport way");
		}

	}

	/**
	 * Returns the course of this PTWay. In most cases, this list only has 1
	 * element. In the case of nested relations in a route, the list can have
	 * multiple elements.
	 * 
	 * @return
	 */
	public List<Way> getWays() {
		return this.ways;
	}
	

	/**
	 * Determines if this PTWay is modeled by an OsmPrimitiveType.WAY
	 */
	public boolean isWay() {
		if (this.getType().equals(OsmPrimitiveType.WAY)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Determines if this PTWay is modeled by an OsmPrimitieType.RELATION (i.e. this is a nested relation)
	 */
	public boolean isRelation() {
		if (this.getType().equals(OsmPrimitiveType.RELATION)) {
			return true;
		}
		return false;
	}
	

}
