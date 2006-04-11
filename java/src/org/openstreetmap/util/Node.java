/*
 * Copyright (C) 2005 Tom Carden (tom@somethingmodern.com)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */

package org.openstreetmap.util;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import org.openstreetmap.client.Tile;
import org.openstreetmap.processing.OsmApplet;

/**
 * Minimal representation of OpenStreetMap node (lat/lon pair, with uid)
 */
public class Node extends OsmPrimitive {

	/**
	 * The coordinates of this node
	 */
	public Point coor;
	
	/**
	 * All lines in this node.
	 * Type: Line
	 */
	public Collection lines = new Vector();

	/**
	 * Create the node from projected values.
	 */
	public Node(float x, float y, Tile projection) {
		coor = new Point(x, y, projection);
	}

	/**
	 * Create the node with all information given.
	 */
	public Node(double lat, double lon, long id) {
		coor = new Point(lat, lon);
		this.id = id;
	}

	public String toString() {
		return "[Node " + id + " lat:" + coor.lat + " lon:" + coor.lon + "]";

	}

	public String key() {
		return key(id);
	}

    public static String key(long id) {
	    return "node_" + id;
	}

	/**
	 * Note, that it is almost anytime the SQUARED distance is what you need.
	 * It is faster to calculate, does not suffer as much from rounding issues and
	 * is equal good when it comes to comparing to other squared distances.
	 * 
	 * @return The squared distance to x,y.
	 */
	public float distanceSq(float x, float y) {
		final float xabs = Math.abs(coor.x-x);
		final float yabs = Math.abs(coor.y-y);
		return xabs*xabs + yabs*yabs;
	}

	public Map getMainTable(OsmApplet applet) {
		return applet.nodes;
	}

	public String getTypeName() {
		return "node";
	}

	public String getName() {
		String name = (String)tags.get("name");
		return name!=null ? name : "";
	}

	public void register() {}
	public void unregister() {}

	protected void doCopyFrom(OsmPrimitive other) {
		coor = ((Node)other).coor;
	}
}
