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
import java.util.Hashtable;

/**
 * Minimal representation of OpenStreetMap line segment (node id -> node id with uid)
 */
public class Line {

	/**
	 * The nodes this line points from and to.
	 */
	public Node from, to;

	/** The additional tags (beside the name) this line segment has.  
	 * FIXME should really be a hash
	 * Imi: What does the above mean? It is a *hash* table, right?
	 * 
	 * Key: String  Value: Tag
	 */
	public Hashtable tags = new Hashtable();

	/**
	 * The unique id (among all other line segments) of this line.
	 */
	public long id;
	/**
	 * True, if the user changed the name.
	 */
	public boolean nameChanged = false;
	/**
	 * <code>true</code>, if the line is currently selected.
	 */
	public boolean selected = false;

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
	 * @param tags	Encoded string of all properties for this line segment. 
	 */
	public Line(Node from, Node to, long id) {
		if (from != null && to != null) {
			this.from = from;
			this.to = to;
		}
		if (from != null)
			from.lines.add(this);
		if (to != null)
			to.lines.add(this);
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
		return (float)Math.atan2(to.y - from.y, to.x - from.x);
	}

	/**
	 * @return The pixel length, if projected.
	 */
	public float length() {
		// TODO check != 0
		return from.distance(to);
	}

	
	/**
	 * @return The pixel distance to node c, if projected.
	 */
	public float distance(Node c) {
		return distance(c.x, c.y);
	}

	/**
	 * @return The pixel distance to point x,y, if projected.
	 */
	public float distance(float x, float y) {
		// project x/y onto line a->b
		// first find parameter (how far along a->b are we?
		float u = (((x - from.x) * (to.x - from.x)) + ((y - from.y) * (to.y - from.y)))
				/ ((to.y - from.y) * (to.y - from.y) + (to.x - from.x) * (to.x - from.x));
		float d = 0.0f;
		if (u <= 0.0f) {
			d = from.distance(x, y);
		} else if (u >= 1.0f) {
			d = to.distance(x, y);
		} else {
			// project x/y onto line a->b
			float px = from.x + (u * (to.x - from.x));
			float py = from.y + (u * (to.y - from.y));
			d = (float)Math.sqrt((x - px) * (x - px) + (y - py) * (y - py));
		}
		return d;
	}

	/**
	 * @return Whether the given coordinates is "over" this line segment. 
	 */
	public boolean mouseOver(float mouseX, float mouseY, float strokeWeight) {
		return distance(mouseX, mouseY) < strokeWeight / 2.0;
	}

	/**
	 * @return A string specifier used in tables when this line is used as key.
	 * Consisting of "line_" and the id.
	 */
	public String key() {
		return "line_" + id;
	} // key

	public synchronized void setName(String sName) {
		Tag name = (Tag)tags.get("name");
		if(name != null)
		{
			name.value = sName;
		}
		else
		{
			tags.put("name", new Tag("name", sName));
		}
	} // setName

	public synchronized String getName() {
		Tag name = (Tag)tags.get("name");
		return name!=null ? name.value : "";
	}

	/**
	 * A debug string representation of this line segment.
	 */
	public String toString() {
		return "[Line " + id + " from " + from + " to " + to + "]";
	}

}

