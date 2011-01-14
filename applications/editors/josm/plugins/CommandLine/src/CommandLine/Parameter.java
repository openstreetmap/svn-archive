/*
 *      Parameter.java
 *      
 *      Copyright 2011 Hind <foxhind@gmail.com>
 *      
 */
 
package CommandLine;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.Relation;

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
				out = String.valueOf(value);
				break;
			case RELAY:
				out = String.valueOf(((Relay)value).getValue());
				break;
			case NODE:
				out = String.valueOf(valueList.size()) + " " + tr("nodes");
				break;
			case WAY:
				out = String.valueOf(valueList.size()) + " " + tr("ways");
				break;
			case RELATION:
				out = String.valueOf(valueList.size()) + " " + tr("relations");
				break;
			case ANY:
				out = String.valueOf(valueList.size()) + " " + tr("OSM objects");
				break;
			case USERNAME:
				out = String.valueOf(value);
				break;
			case IMAGERYURL:
				out = String.valueOf(value);
				break;
			case IMAGERYOFFSET:
				out = String.valueOf(value);
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
