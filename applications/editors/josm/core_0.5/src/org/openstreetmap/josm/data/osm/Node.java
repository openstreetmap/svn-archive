// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.data.osm;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.visitor.Visitor;


/**
 * One node data, consisting of one world coordinate waypoint.
 *
 * @author imi
 */
public final class Node extends OsmPrimitive {
	
	public LatLon coor;
	public volatile EastNorth eastNorth;

	/**
	 * Create an incomplete Node object
	 */
	public Node(long id) {
		this.id = id;
		incomplete = true;
	}
	
	/**
	 * Create an identical clone of the argument (including the id)
	 */
	public Node(Node clone) {
		cloneFrom(clone);
	}

	public Node(LatLon latlon) {
		this.coor = latlon;
		eastNorth = Main.proj.latlon2eastNorth(latlon);
	}

	@Override public void visit(Visitor visitor) {
		visitor.visit(this);
	}
	
	@Override public void cloneFrom(OsmPrimitive osm) {
		super.cloneFrom(osm);
		coor = ((Node)osm).coor;
		eastNorth = ((Node)osm).eastNorth;
	}

	@Override public String toString() {
		return "{Node id="+id+",lat="+coor.lat()+",lon="+coor.lon()+"}";
	}

	@Override public boolean realEqual(OsmPrimitive osm, boolean semanticOnly) {
		return osm instanceof Node ? super.realEqual(osm, semanticOnly) && coor.equals(((Node)osm).coor) : false;
    }

	public int compareTo(OsmPrimitive o) {
	    return o instanceof Node ? Long.valueOf(id).compareTo(o.id) : 1;
    }
}
