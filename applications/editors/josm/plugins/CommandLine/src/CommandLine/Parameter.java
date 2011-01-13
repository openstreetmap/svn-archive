/*
 *      Parameter.java
 *      
 *      Copyright 2010 Hind <foxhind@gmail.com>
 *      
 */
 
package commandline;

import java.util.ArrayList;
import java.util.Collection;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

public class Parameter {
	public boolean required;
	public Type type;
	public String name;
	public String description;
	private Object value;
	private ArrayList<OsmPrimitive> valueList;
	protected float maxVal;
	protected float minVal;
	protected int maxInstances;
	
	public Parameter () { required = false; maxInstances = 1; maxVal = 0; minVal = 0; value = ""; valueList = new ArrayList<OsmPrimitive>(); }
	public String getValue() {
		String out = "";
		switch (type) {
			case POINT:
				out = (String)value;
				break;
			case LENGTH:
				out = String.valueOf(value);
				break;
			case NATURAL:
				out = String.valueOf(value);
				break;
			case STRING:
				out = "\"" + String.valueOf(value) + "\"";
				break;
			case RELAY:
				out = String.valueOf(((Relay)value).getValue());
				break;
		}
		return out;
	}

	public Object getRawValue() {
		return value;
	}

	public ArrayList<OsmPrimitive> getValueList() {
		return valueList;
	}

	public void setValue(Object obj) {
		if (type == Type.RELAY && obj instanceof String && value instanceof Relay) {
			((Relay)value).setValue((String)obj);
		}
		else
			value = obj;
	}

	public Collection<OsmPrimitive> getParameterObjects() {
		ArrayList<OsmPrimitive> pObjects = new ArrayList<OsmPrimitive>();
		if (isOsm()) {
			if (maxInstances == 1) {
				pObjects.add((OsmPrimitive)value);
			}
			else {
				return valueList;
			}
		}
		return pObjects;
	}

	public boolean isOsm() {
		return type == Type.NODE || type == Type.WAY || type == Type.RELATION || type == Type.ANY;
	}
}
