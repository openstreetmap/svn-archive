package org.openstreetmap.util;

import java.util.Hashtable;
import java.util.Map;

import org.openstreetmap.processing.OsmApplet;

/**
 * Base class for Node, Line and Way (all current supported osm primitives)
 * 
 * @author Imi
 */
abstract public class OsmPrimitive implements Cloneable {
	/**
	 * The unique (among all other objects of the same type) identifier.
	 */
	public long id = 0;

	/**
	 * The tags for this object  
	 * Key: String  Value: String
	 */
	public Map tags = new Hashtable();

	/**
	 * @return the main table this primitive is associated with. E.g. node returns
	 * the applet.nodes table
	 */
	abstract public Map getMainTable(OsmApplet applet);
	
	/**
	 * @return A type identifier for this primitive. This should be used when a
	 * coding time resource identifier as example the tagname in xml or the name of the
	 * gui ressource is necesary.
	 */
	abstract public String getTypeName();

	/**
	 * @return A string specifier used in tables when this line is used as key.
	 * Consisting of "line_" and the id.
	 */
	abstract public String key();

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error("FATAL: Clonable interface ignored by VM", e);
		}
	}
	
	/**
	 * Register itself on every dependend objects (lines register on their nodes, ways
	 * on their lines etc..)
	 */
	abstract public void register();
	/**
	 * Unregister itself from dependend objects.
	 */
	abstract public void unregister();
}
