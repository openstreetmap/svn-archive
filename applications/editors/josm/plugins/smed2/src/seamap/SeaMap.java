/* Copyright 2012 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seamap;

import java.util.*;

import s57.S57att;
import s57.S57att.Att;
import s57.S57obj;
import s57.S57obj.*;
import s57.S57val;
import s57.S57val.*;

public class SeaMap {

	public enum Nflag {
		ANON, ISOL, CONN
	}

	public class Snode {
		public double lat;
		public double lon;
		public Nflag flg;

		public Snode() {
			flg = Nflag.ANON;
			lat = 0;
			lon = 0;
		}

		public Snode(double ilat, double ilon) {
			flg = Nflag.ANON;
			lat = ilat;
			lon = ilon;
		}

		public Snode(double ilat, double ilon, Nflag iflg) {
			lat = ilat;
			lon = ilon;
			flg = iflg;
		}
	}

	public class Edge {
		public boolean forward;
		public long first;
		public long last;
		public ArrayList<Long> nodes;

		public Edge() {
			forward = true;
			first = 0;
			last = 0;
			nodes = new ArrayList<Long>();
		}
	}

	public class Side {
		Edge edge;
		boolean forward;

		public Side(Edge iedge, boolean ifwd) {
			edge = iedge;
			forward = ifwd;
		}
	}

	public class Bound {
		public boolean outer;
		ArrayList<Side> sides;

		public Bound() {
			outer = true;
			sides = new ArrayList<Side>();
		}

		public Bound(Side iside, boolean irole) {
			outer = irole;
			sides = new ArrayList<Side>();
			sides.add(iside);
		}
	}

	public class Area extends ArrayList<Bound> {
		public Area() {
			super();
		}
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

	public class NodeTab extends HashMap<Long, Snode> {
		public NodeTab() {
			super();
		}
	}

	public class EdgeTab extends HashMap<Long, Edge> {
		public EdgeTab() {
			super();
		}
	}

	public class AreaTab extends HashMap<Long, Area> {
		public AreaTab() {
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

	public enum Fflag {
		UNKN, POINT, LINE, AREA
	}

	public class Feature {
		public Fflag flag;
		public long refs;
		public Obj type;
		public AttMap atts;
		public ObjMap objs;
		public long area;
		public Snode centre;

		Feature() {
			flag = Fflag.UNKN;
			refs = 0;
			type = Obj.UNKOBJ;
			atts = new AttMap();
			objs = new ObjMap();
			area = 0;
			centre = new Snode();
		}
	}

	public NodeTab nodes;
	public EdgeTab edges;
	public AreaTab areas;

	public FtrMap features;
	public FtrTab index;

	private Feature feature;
	private Edge edge;
	private ArrayList<Long> outers;
	private ArrayList<Long> inners;

	public class EdgeIterator {
		Edge edge;
		boolean forward;
		ListIterator<Long> it;

		public EdgeIterator(Edge iedge, boolean dir) {
			edge = iedge;
			forward = dir;
			it = null;
		}

		public boolean hasNext() {
			return (edge != null) && ((it == null) || (forward && it.hasNext()) || (!forward && it.hasPrevious()));
		}

		public Snode next() {
			long ref = 0;
			if (forward) {
				if (it == null) {
					ref = edge.first;
					it = edge.nodes.listIterator();
				} else {
					if (it.hasNext()) {
						ref = it.next();
					} else {
						ref = edge.last;
						edge = null;
					}
				}
			} else {
				if (it == null) {
					ref = edge.last;
					it = edge.nodes.listIterator(edge.nodes.size());
				} else {
					if (it.hasPrevious()) {
						ref = it.previous();
					} else {
						ref = edge.first;
						edge = null;
					}
				}
			}
			return nodes.get(ref);
		}
	}

	public class BoundIterator {
		Bound bound;
		Side side;
		ListIterator<Side> sit;
		EdgeIterator eit;

		public BoundIterator(Bound ibound) {
			bound = ibound;
			sit = bound.sides.listIterator();
			if (sit.hasNext()) {
				side = sit.next();
				eit = new EdgeIterator(side.edge, side.forward);
			} else {
				side = null;
			}
		}

		public boolean hasNext() {
			return side != null;
		}

		public Snode next() {
			Snode node = null;
			if (side != null) {
				if (eit.hasNext()) {
					node = eit.next();
				} else {
					if (sit.hasNext()) {
						side = sit.next();
						eit = new EdgeIterator(side.edge, side.forward);
						node = eit.next();
					} else {
						side = null;
					}
				}
			}
			return node;
		}
	}
	
	public SeaMap() {
		nodes = new NodeTab();
		edges = new EdgeTab();
		areas = new AreaTab();
		feature = new Feature();
		features = new FtrMap();
		index = new FtrTab();
	}

	public void addNode(long id, double lat, double lon) {
		nodes.put(id, new Snode(Math.toRadians(lat), Math.toRadians(lon)));
		feature = new Feature();
		feature.refs = id;
		feature.flag = Fflag.POINT;
		edge = null;
	}

	public void addEdge(long id) {
		feature = new Feature();
		feature.refs = id;
		feature.flag = Fflag.LINE;
		edge = new Edge();
	}

	public void addToEdge(long node) {
		if (edge.first == 0) {
			edge.first = node;
		} else {
			if (edge.last != 0) {
				edge.nodes.add(edge.last);
			}
			edge.last = node;
		}
	}

	public void addArea(long id) {
		feature = new Feature();
		feature.refs = id;
		feature.flag = Fflag.AREA;
		outers = new ArrayList<Long>();
		inners = new ArrayList<Long>();
		edge = null;
	}

	public void addToArea(long id, boolean outer) {
		if (outer) {
			outers.add(id);
		} else {
			inners.add(id);
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
				if (attval.val != null)
					atts.put(att, new AttItem(attval.conv, attval.val));
			} else {
				if (subkeys[1].equals("type")) {
					feature.type = S57obj.enumType(val);
				} else {
					Att att = S57att.enumAttribute(subkeys[1], Obj.UNKOBJ);
					if (att != Att.UNKATT) {
						AttVal attval = S57val.convertValue(val, att);
						if (attval.val != null)
							feature.atts.put(att, new AttItem(attval.conv, attval.val));
					}
				}
			}
		}
	}

	public void tagsDone(long id) {
		if ((feature.type != Obj.UNKOBJ) && !((edge != null) && (edge.last == 0))) {
			index.put(id, feature);
			if (features.get(feature.type) == null) {
				features.put(feature.type, new ArrayList<Feature>());
			}
			features.get(feature.type).add(feature);
		}
		switch (feature.flag) {
		case POINT:
			Snode node = nodes.get(id);
			if (node.flg != Nflag.CONN) {
				node.flg = Nflag.ISOL;
			}
			break;
		case LINE:
			edges.put(id, edge);
			nodes.get(edge.first).flg = Nflag.CONN;
			nodes.get(edge.last).flg = Nflag.CONN;
			if (edge.first == edge.last) {
				feature.flag = Fflag.AREA;
				Area area = new Area();
				area.add(new Bound(new Side(edge, edge.forward), true));
				areas.put(id, area);
			}
			break;
		case AREA:
			Area area = new Area();
			for (ArrayList<Long> role = outers; role != null; role = inners) {
				while (!role.isEmpty()) {
					Edge edge = edges.get(role.remove(0));
					long node1 = edge.first;
					long node2 = edge.last;
					Bound bound = new Bound(new Side(edge, edge.forward), (role == outers));
					if (node1 != node2) {
						for (ListIterator<Long> it = role.listIterator(0); it.hasNext();) {
							Edge nedge = edges.get(it.next());
							if (nedge.first == node2) {
								bound.sides.add(new Side(nedge, true));
								it.remove();
								if (nedge.last == node2)
									break;
							} else if (nedge.last == node2) {
								bound.sides.add(new Side(nedge, false));
								it.remove();
								if (nedge.first == node2)
									break;
							}
						}
					}
					area.add(bound);
				}
				if (role == outers) {
					if (area.isEmpty()) {
						role = null;
					} else {
						areas.put(id, area);
					}
				}
			}
			break;
		}
		feature.centre = findCentroid(feature);
	}

	public double signedArea(Bound bound) {
		Snode node;
		double lat, lon, llon, llat;
		lat = lon = llon = llat = 0;
		double sigma = 0;
		BoundIterator it = new BoundIterator(bound);
		while (it.hasNext()) {
			llon = lon;
			llat = lat;
			node = it.next();
			lat = node.lat;
			lon = node.lon;
			sigma += (lon * Math.sin(llat)) - (llon * Math.sin(lat));
		}
		return sigma;
	}

	public boolean handOfArea(Bound bound) {
		return (signedArea(bound) < 0);
	}

	public double calcArea(Bound bound) {
		return Math.abs(signedArea(bound)) * 3444 * 3444 / 2.0;
	}

	public Snode findCentroid(Feature feature) {
		double lat, lon, slat, slon, sarc, llat, llon;
		lat = lon = slat = slon = sarc = llat = llon = 0;
		switch (feature.flag) {
		case POINT:
			return nodes.get(feature.refs);
		case LINE:
			Edge edge = edges.get(feature.refs);
			EdgeIterator eit = new EdgeIterator(edge, true);
			while (eit.hasNext()) {
				Snode node = eit.next();
				lat = node.lat;
				lon = node.lon;
				sarc += (Math.acos(Math.cos(lon - llon) * Math.cos(lat - llat)));
				llat = lat;
				llon = lon;
			}
			double harc = sarc / 2;
			sarc = 0;
			eit = new EdgeIterator(edge, true);
			while (eit.hasNext()) {
				Snode node = eit.next();
				lat = node.lat;
				lon = node.lon;
				sarc = (Math.acos(Math.cos(lon - llon) * Math.cos(lat - llat)));
				if (sarc > harc)
					break;
				harc -= sarc;
				llat = lat;
				llon = lon;
			}
			double frac = harc / sarc;
			return new Snode(llat + ((lat - llat) / frac), llon + ((lon - llon) / frac));
		case AREA:
			Bound bound = areas.get(feature.refs).get(0);
			BoundIterator bit = new BoundIterator(bound);
			while (bit.hasNext()) {
				llon = lon;
				llat = lat;
				Snode node = bit.next();
				lat = node.lat;
				lon = node.lon;
				double arc = (Math.acos(Math.cos(lon - llon) * Math.cos(lat - llat)));
				slat += (lat * arc);
				slon += (lon * arc);
				sarc += arc;
			}
			return new Snode((sarc > 0.0 ? slat / sarc : 0.0), (sarc > 0.0 ? slon / sarc : 0.0));
		}
		return null;
	}

}
