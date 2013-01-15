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
import s57.S57att.Att;
import s57.S57obj;
import s57.S57obj.*;
import s57.S57val;
import s57.S57val.*;

public class SeaMap {

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

	public class AttMap extends EnumMap<Att, AttItem> {
		public AttMap() {
			super(Att.class);
		}
	}
	
	public class ObjTab extends HashMap<Integer, AttMap> {
		public ObjTab() {
			super();
		}
	}
	
	public class ObjMap extends EnumMap<Obj, ObjTab> {
		public ObjMap() {
			super(Obj.class);
		}
	}
	
	public class NodeTab extends HashMap<Long, Coord> {
		public NodeTab() {
			super();
		}
	}
	
	public class WayTab extends HashMap<Long, ArrayList<Long>> {
		public WayTab() {
			super();
		}
	}
	
	public class MpolyTab extends HashMap<Long, Long> {
		public MpolyTab() {
			super();
		}
	}
	
	public class FtrMap extends EnumMap<Obj, ArrayList<Feature>> {
		public FtrMap() {
			super(Obj.class);
		}
	}
	
	public class FtrTab extends HashMap<Long, Feature> {
		public FtrTab() {
			super();
		}
	}
	
	public class Feature {
		public Fflag flag;
		public long refs;
		public Obj type;
		public AttMap atts;
		public ObjMap objs;

		Feature() {
			flag = Fflag.UNKN;
			refs = 0;
			type = Obj.UNKOBJ;
			atts = new AttMap();
			objs = new ObjMap();
		}
	}

	public class Coord {
		public double lat;
		public double lon;

		public Coord(double ilat, double ilon) {
			lat = ilat;
			lon = ilon;
		}
	}

	public NodeTab nodes;
	public WayTab ways;
	public WayTab mpolys;
	public MpolyTab outers;
	public FtrMap features;
	public FtrTab index;

	private Feature feature;
	private ArrayList<Long> list;
	private long mpid;

	public SeaMap() {
		nodes = new NodeTab();
		ways = new WayTab();
		mpolys = new WayTab();
		outers = new MpolyTab();
		feature = new Feature();
		features = new FtrMap();
		index = new FtrTab();
	}

	public void addNode(long id, double lat, double lon) {
		nodes.put(id, new Coord(Math.toRadians(lat), Math.toRadians(lon)));
		feature = new Feature();
		feature.refs = id;
		feature.flag = Fflag.NODE;
	}

	public void moveNode(long id, double lat, double lon) {
		nodes.put(id, new Coord(Math.toRadians(lat), Math.toRadians(lon)));
	}

	public void addWay(long id) {
		list = new ArrayList<Long>();
		ways.put(id, list);
		feature = new Feature();
		feature.refs = id;
		feature.flag = Fflag.WAY;
	}

	public void addMpoly(long id) {
		list = new ArrayList<Long>();
		mpolys.put(id, list);
		mpid = id;
	}

	public void addToWay(long node) {
		list.add(node);
	}

	public void addToMpoly(long way, boolean outer) {
		if (outer) {
			list.add(0, way);
			outers.put(way, mpid);
		} else {
			list.add(way);
		}
	}

	public void tagsDone(long id) {
		if ((feature.type != Obj.UNKOBJ) && !((feature.flag == Fflag.WAY) && (list.size() < 2))) {
			index.put(id, feature);
			if ((feature.flag == Fflag.WAY) && (list.size() > 0) && (list.get(0).equals(list.get(list.size() - 1)))) {
				feature.flag = Fflag.AREA;
			}
			if (features.get(feature.type) == null) {
				features.put(feature.type, new ArrayList<Feature>());
			}
			features.get(feature.type).add(feature);
		}
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
				ObjTab items = feature.objs.get(obj);
				if (items == null) {
					items = new ObjTab();
					feature.objs.put(obj, items);
				}
				AttMap atts = items.get(idx);
				if (atts == null) {
					atts = new AttMap();
					items.put(idx, atts);
				}
				AttVal attval = S57val.convertValue(val, att);
				if (attval.val != null) atts.put(att, new AttItem(attval.conv, attval.val));
			} else {
				if (subkeys[1].equals("type")) {
					feature.type = S57obj.enumType(val);
				} else {
					Att att = S57att.enumAttribute(subkeys[1], Obj.UNKOBJ);
					if (att != Att.UNKATT) {
						AttVal attval = S57val.convertValue(val, att);
						if (attval.val != null) feature.atts.put(att, new AttItem(attval.conv, attval.val));
					}
				}
			}
		}
	}
}
