/* Copyright 2012 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seamap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

import s57.S57att.Att;
import s57.S57obj.Obj;
import s57.S57val.Conv;

public class Map {

	public enum Fflag { NODE, WAY, AREA	}

	public class AttItem {
		Conv conv;
		Object val;
	}

	public class ObjItem {
		int idx;
		ArrayList<EnumMap<Att, AttItem>> atts;
	}

	public class Feature {
		public long id;
		public Fflag flag;
		public ArrayList<Long> refs;
		public Obj type;
		public EnumMap<Att, AttItem> atts;
		public EnumMap<Obj, ArrayList<ObjItem>> objs;
	}
	
	public class Coord {
		double lat;
		double lon;
		Coord (double ilat, double ilon) {
			lat = ilat;
			lon = ilon;
		}
	}
	
	public HashMap<Long, Coord> nodes;
	public HashMap<Long, ArrayList<Long>> ways;
	public HashMap<Long, ArrayList<Long>> mpolys;
	public ArrayList<Feature> features;
	
	public Map () {
		nodes = new HashMap<Long, Coord>();
		ways = new HashMap<Long, ArrayList<Long>>();
		mpolys = new HashMap<Long, ArrayList<Long>>();
		features = new ArrayList<Feature>();
	}
	
	public void addNode(long id, double lat, double lon) {
		nodes.put(id, new Coord(lat, lon));
	}
	
	public void addWay(long id) {
		ways.put(id, new ArrayList<Long>());
	}
	
	public void addToWay(long way, long node) {
		ways.get(way).add(node);
	}
	public void addRelation(long id) {
		mpolys.put(id, new ArrayList<Long>());
	}
	
	public void addToRelation(long rel, long way) {
		mpolys.get(rel).add(way);
	}
}
