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

import s57.S57att;
import s57.S57att.*;
import s57.S57obj;
import s57.S57obj.*;
import s57.S57val;
import s57.S57val.*;

public class Map {

	public enum Fflag {
		UNKN, NODE, WAY, AREA
	}

	public class AttItem {
		Conv conv;
		Object val;

		AttItem(Conv iconv, Object ival) {
			conv = iconv;
			val = ival;
		}
	}

	public class Feature {
		public Fflag flag;
		public ArrayList<Long> refs;
		public Obj type;
		public EnumMap<Att, AttItem> atts;
		public EnumMap<Obj, HashMap<Integer, EnumMap<Att, AttItem>>> objs;

		Feature() {
			clean();
		}
		
		void clean() {
			flag = Fflag.UNKN;
			refs = new ArrayList<Long>();
			type = Obj.UNKOBJ;
			atts = new EnumMap<Att, AttItem>(Att.class);
			objs = new EnumMap<Obj, HashMap<Integer, EnumMap<Att, AttItem>>>(Obj.class);
		}
	}

	public class Coord {
		double lat;
		double lon;

		Coord(double ilat, double ilon) {
			lat = ilat;
			lon = ilon;
		}
	}

	public HashMap<Long, Coord> nodes;
	public HashMap<Long, ArrayList<Long>> ways;
	public HashMap<Long, ArrayList<Long>> mpolys;
	public ArrayList<Feature> features;

	private Feature feature;

	public Map() {
		nodes = new HashMap<Long, Coord>();
		ways = new HashMap<Long, ArrayList<Long>>();
		mpolys = new HashMap<Long, ArrayList<Long>>();
		feature = new Feature();
		features = new ArrayList<Feature>();
		features.add(feature);
	}

	public void addNode(long id, double lat, double lon) {
		nodes.put(id, new Coord(lat, lon));
		if (feature.type == Obj.UNKOBJ) {
			feature.clean();
		} else {
			feature = new Feature();
			features.add(feature);
		}
	}

	public void addWay(long id) {
		ways.put(id, new ArrayList<Long>());
		if (feature.type == Obj.UNKOBJ) {
			feature.clean();
		} else {
			feature = new Feature();
			features.add(feature);
		}
	}

	public void addToWay(long way, long node) {
		ways.get(way).add(node);
	}

	public void addMpoly(long id) {
		mpolys.put(id, new ArrayList<Long>());
	}

	public void addToMpoly(long id, long way) {
		mpolys.get(id).add(way);
	}

	public void addTag(String key, String val) {
		String subkeys[] = key.split(":");
		if ((subkeys.length > 1) && subkeys[0].equals("seamark")) {
			Obj obj = S57obj.enumType(subkeys[1]);
			if ((subkeys.length > 2) && (obj != Obj.UNKOBJ)) {
				int idx = 0;
				Att att = Att.UNKATT;
				try {
					idx = Integer.parseInt(subkeys[2]);
					if (subkeys.length == 4) {
						att = s57.S57att.enumAttribute(subkeys[3], obj);
					}
				} catch (Exception e) {
					att = S57att.enumAttribute(subkeys[2], obj);
				}
				HashMap<Integer, EnumMap<Att, AttItem>> items = feature.objs.get(obj);
				if (items == null) {
					items = new HashMap<Integer, EnumMap<Att, AttItem>>();
					feature.objs.put(obj, items);
				}
				EnumMap<Att, AttItem> atts = items.get(idx);
				if (atts == null) {
					atts = new EnumMap<Att, AttItem>(Att.class);
				}
				AttVal attval = S57val.convertValue(val, att);
				atts.put(att, new AttItem(attval.conv, attval.val));
			} else {
				if (subkeys[1].equals("type")) {
					feature.type = S57obj.enumType(val);
				} else {
					Att att = S57att.enumAttribute(subkeys[1], Obj.UNKOBJ);
					if (att != Att.UNKATT) {
						AttVal attval = S57val.convertValue(val, att);
						feature.atts.put(att, new AttItem(attval.conv, attval.val));
					}
				}
			}
		}
	}
}
