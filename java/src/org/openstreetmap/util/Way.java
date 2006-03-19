package org.openstreetmap.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openstreetmap.processing.OsmApplet;

/**
 * Representation of OSM way.
 * @author Imi
 */
public class Way extends OsmPrimitive {

	/**
	 * The lines this way consist of
	 * Type: Line
	 */
	private List lines = new ArrayList();

	public String key() {
		return "way_" + id;
	}
	
	public Way(long id) {
		this.id = id;
	}
	
	public String getName() {
		return (String)tags.get("name");
	}
	
	public Line getNameLineSegment() {
		String n = (String)tags.get("name_segment");
		if (n != null) {
			try {
				long id = Long.parseLong(n);
				for (Iterator it = lines.iterator(); it.hasNext();) {
					Line l = (Line)it.next();
					if (l.id == id)
						return l;
				}
			} catch (NumberFormatException nfe) {
			}
		}
		if (lines.isEmpty())
			return null;
		return (Line)lines.get(lines.size()/2);
	}
	
	public int size() {return lines.size();}
	public Line get(int i) {return (Line)lines.get(i);}
	public void remove(Line line) {
		lines.remove(line);
		line.ways.remove(this);
	}
	public void add(Line line) {
		lines.add(line);
		line.ways.add(this);
	}
	/**
	 * This way is going to be destroyed. Deregister from all lines.
	 */
	public void removeAll() {
		for (Iterator it = lines.iterator(); it.hasNext();)
			((Line)it.next()).ways.remove(this);
		lines.clear();
	}
	
	public String toString() {
		return "[Way "+id+" with "+lines+"]";
	}

	public Map getMainTable(OsmApplet applet) {
		return applet.ways;
	}

	public String getTypeName() {
		return "way";
	}

	public void addAll(Collection c) {
		for (Iterator it = c.iterator(); it.hasNext();)
			add((Line)it.next());
	}

	protected void finalize() throws Throwable {
		super.finalize();
		removeAll(); // to be extra sure.
	}

	public Object clone() {
		Way way = (Way)super.clone();
		for (Iterator it = way.lines.iterator(); it.hasNext();)
			((Line)it.next()).ways.add(way);
		return way;
	}
}
