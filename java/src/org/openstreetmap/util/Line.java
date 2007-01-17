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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.processing.OsmApplet;

/**
 * Minimal representation of OpenStreetMap line segment (node id -> node id with uid)
 */
public class Line extends OsmPrimitive {

	/**
	 * The nodes this line points from and to.
	 */
	public Node from, to;

	/**
	 * All ways this line is part of.
	 */
	public List ways = new LinkedList();
	
	/**
	 * Create a line from node "from" to node "to" without tags and with unknown id=0. 
	 */
	public Line(Node from, Node to) {
		this(from, to, 0);
	}

	/**
	 * Create a line.
	 * @param from	Node the line starts.
	 * @param to	Node the line ends in.
	 * @param id	Unique id for this line.
	 */
	public Line(Node from, Node to, long id) {
		if (from != null && to != null) {
			this.from = from;
			this.to = to;
		}
		this.id = id;
	}

	/**
	 * Turn the line segment around so that it now points in the reverse direction.
	 */
	public void reverse() {
		Node temp = from;
		from = to;
		to = temp;
	}

	/**
	 * @return The screen angle as float, if projected.
	 */
	public float angle() {
		return (float)Math.atan2(to.coor.y - from.coor.y, to.coor.x - from.coor.x);
	}

	/**
	 * @return The pixel length, if projected.
	 */
	public float length() {
		// TODO check != 0
		return from.coor.distance(to.coor);
	}

	
	/**
	 * @return The pixel distance to node c, if projected.
	 */
	public float distance(Node c) {
		return distance(c.coor.x, c.coor.y);
	}

	/**
	 * @return The pixel distance to point x,y, if projected.
	 */
	public float distance(float x, float y) {
		// project x/y onto line a->b
		// first find parameter (how far along a->b are we?
        System.err.println("from is " + from);
        System.err.println("to is " + to);
		float u = (((x - from.coor.x) * (to.coor.x - from.coor.x)) + ((y - from.coor.y) * (to.coor.y - from.coor.y)))
				/ ((to.coor.y - from.coor.y) * (to.coor.y - from.coor.y) + (to.coor.x - from.coor.x) * (to.coor.x - from.coor.x));
		float d = 0.0f;
		if (u <= 0.0f) {
			d = from.coor.distance(x, y);
		} else if (u >= 1.0f) {
			d = to.coor.distance(x, y);
		} else {
			// project x/y onto line a->b
			float px = from.coor.x + (u * (to.coor.x - from.coor.x));
			float py = from.coor.y + (u * (to.coor.y - from.coor.y));
			d = (float)Math.sqrt((x - px) * (x - px) + (y - py) * (y - py));
		}
		return d;
	}
	
	/**
	 * @return Whether the given coordinates is "over" this line segment. 
	 */
	public boolean mouseOver(float mouseX, float mouseY, float strokeWeight) {
        // If we don't know where we are, they can't be over us
        if(from == null || to == null) { return false; }
        // Figure out their distance, and if that's within the range
		return distance(mouseX, mouseY) < strokeWeight / 2.0;
	}

	public synchronized void setName(String sName) {
		tagsput("name", sName);
	}

	public synchronized String getName() {
		String name = (String)getTags().get("name");
		return name!=null ? name : "";
	}

	/**
	 * A debug string representation of this line segment.
	 */
	public String toString() {
		return "[Line " + id + " from " + from + " to " + to + "]";
	}

	public Map getMainTable(OsmApplet applet) {
		return applet.lines;
	}

	public String getTypeName() {
		return "segment";
	}

	public String key() {
		return "line_" + id;
	}

	/** 
	 * @return the id out of a key string.
	 */
	public static long getIdFromKey(String lineKey) {
		return Long.parseLong(lineKey.substring(lineKey.indexOf('_')+1));
	}

	public void register() {
		from.lines.add(this);
		to.lines.add(this);
	}

	public void unregister() {
		from.lines.remove(this);
		to.lines.remove(this);
	}

	public void doCopyFrom(OsmPrimitive other) {
		from = ((Line)other).from;
		to = ((Line)other).to;
	}
}

