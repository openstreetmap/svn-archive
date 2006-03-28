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
	 * Return a human readible string for this object. Do not include the type.
	 */
	abstract public String getName();
	
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

	/**
	 * Equal, if the id (and class) is equal. If both ids are 0, use the super classes
	 * equal instead.
	 * 
	 * An primitive is equal to its incomplete counter part.
	 */
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass() || id == 0 || ((OsmPrimitive)obj).id == 0)
			return super.equals(obj);
		return id == ((OsmPrimitive)obj).id;
	}

	/**
	 * Return the id as hashcode or supers hashcode if 0.
	 * 
	 * An primitive has the same hashcode as its incomplete counter part.
	 */
	public int hashCode() {
		return id == 0 ? super.hashCode() : (int)id;
	}

	/**
	 * Copy the content of the given primitive over ourself. All back references are 
	 * updated correctly.
	 * 
	 * @param other This primitive will not be touched, but its data is copied into this
	 * 		primitive.
	 */
	final public void copyFrom(OsmPrimitive other) {
		unregister();
		id = other.id;
		tags = other.tags;
		doCopyFrom(other);
		register();
	}

	/**
	 * Implementation of subclasses must copy the references of the other object to itself.
	 * They may assume that the class of the parameter match their own class.
	 * @return 
	 */
	abstract protected void doCopyFrom(OsmPrimitive other);
}
