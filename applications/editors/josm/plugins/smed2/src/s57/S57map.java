/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package s57;

import java.util.*;

import s57.S57obj;
import s57.S57obj.*;
import s57.S57att;
import s57.S57att.*;
import s57.S57val;
import s57.S57val.*;

public class S57map {

	public enum Nflag {
		ANON,	// Edge inner nodes
		ISOL,	// Node not part of Edge
		CONN,	// Edge first and last nodes
		DPTH	// Sounding nodes
	}

	public class Snode {	// All coordinates in map
		public double lat;	// Latitude
		public double lon;	// Longitude
		public Nflag flg;		// Role of node

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

	public class Dnode extends Snode {	// All depth soundings
		public double val;	// Sounding value

		public Dnode() {
			flg = Nflag.DPTH;
			lat = 0;
			lon = 0;
			val = 0;
		}
		public Dnode(double ilat, double ilon, double ival) {
			flg = Nflag.DPTH;
			lat = ilat;
			lon = ilon;
			val = ival;
		}
	}
	
	public class Edge {		// A polyline segment
		public long first;	// First CONN node
		public long last;		// Last CONN node
		public ArrayList<Long> nodes; // Inner ANON nodes

		public Edge() {
			first = 0;
			last = 0;
			nodes = new ArrayList<Long>();
		}
	}
	
	public enum Rflag {
		UNKN, AGGR, MASTER, SLAVE, PEER
	}
	
	public class Reln {
		public long id;
		public Rflag reln;
		public Reln(long i, Rflag r) {
			id = i;
			reln = r;
		}
	}

	public class AttMap extends HashMap<Att, AttVal<?>> {
		public AttMap() {
			super();
		}
	}

	public class RelTab extends ArrayList<Reln> {
		public RelTab() {
			super();
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

	public class Prim {				// Spatial element
		public long id;					// Snode ID for POINTs, Edge ID for LINEs & AREAs)
		public boolean forward;	// Direction of vector used (LINEs & AREAs)
		public boolean outer;		// Exterior/Interior boundary (AREAs)
		public Prim() {
			id = 0; forward = true; outer = true;
		}
		public Prim(long i) {
			id = i; forward = true; outer = true;
		}
		public Prim(long i, boolean o) {
			id = i; forward = true; outer = o;
		}
		public Prim(long i, boolean f, boolean o) {
			id = i; forward = f; outer = o;
		}
	}
	
	public class Comp {
		public long ref;
		public int size;
		public Comp(long r, int s) {
			ref = r;
			size = s;
		}
	}
	
	public enum Pflag {
		NOSP, POINT, LINE, AREA
	}
	
	public class Geom {							// Geometric structure of feature
		public Pflag prim;						// Geometry type
		public ArrayList<Prim> elems;	// Ordered list of elements
		public int outers;						// Number of outers
		public int inners;						// Number of inners
		public ArrayList<Comp> refs;	// Ordered list of compounds
		public Geom(Pflag p) {
			prim = p;
			elems = new ArrayList<Prim>();
			outers = inners = 0;
			refs = new ArrayList<Comp>();
		}
	}
	
	public class Feature {
		public Rflag reln;		// Relationship status
		public Geom geom;			// Geometry data
		public Obj type;			// Feature type
		public AttMap atts;		// Feature attributes
		public RelTab rels;		// Related objects
		public ObjMap objs;		// Slave objects
		public double area;		// Area of feature
		public double length;	// Length of feature
		public Snode centre;	// Centre of feature

		Feature() {
			reln = Rflag.UNKN;
			geom = new Geom(Pflag.NOSP);
			type = Obj.C_AGGR;
			atts = new AttMap();
			rels = new RelTab();
			objs = new ObjMap();
			area = 0;
			length = 0;
			centre = new Snode();
		}
	}

	public NodeTab nodes;
	public EdgeTab edges;

	public FtrMap features;
	public FtrTab index;

	private Feature feature;
	private Edge edge;
	private long ref;

	public S57map() {
		nodes = new NodeTab();		// All nodes in map
		edges = new EdgeTab();		// All edges in map
		feature = new Feature();	// Current feature being built
		features = new FtrMap();	// All features in map, grouped by type
		index = new FtrTab();			// Feature look-up table
		ref = 0x0000ffffffff0000L;// Compound reference generator
	}

	// S57 map building methods
	
	public void newNode(long id, double lat, double lon, Nflag flag) {
		nodes.put(id, new Snode(Math.toRadians(lat), Math.toRadians(lon), flag));
		if (flag == Nflag.ANON) {
			edge.nodes.add(id);
		}
	}

	public void newNode(long id, double lat, double lon, double depth) {
		nodes.put(id, new Dnode(Math.toRadians(lat), Math.toRadians(lon), depth));
	}

	public void newFeature(long id, Pflag p, long objl) {
		feature = new Feature();
		Obj obj = S57obj.decodeType(objl);
		if (obj == Obj.C_AGGR) {
			feature.reln = Rflag.AGGR;
		}
		feature.geom = new Geom(p);
		feature.type = obj;
		index.put(id, feature);
	}
	
	public void newObj(long id, int rind) {
		Rflag r = Rflag.AGGR;
		switch (rind) {
		case 1:
			r = Rflag.MASTER;
			break;
		case 2:
			r = Rflag.SLAVE;
			break;
		case 3:
			r = Rflag.PEER;
			break;
		}
		feature.rels.add(new Reln(id, r));
	}
	
	public void endFeature() {
		
	}
	
	public void newAtt(long attl, String atvl) {
		Att att = S57att.decodeAttribute(attl);
		AttVal<?> val = S57val.decodeValue(atvl, att);
		feature.atts.put(att, val);
	}

	public void newPrim(long id, long ornt, long usag) {
		feature.geom.elems.add(new Prim(id, (ornt != 2), (usag != 2)));
	}

	public void addConn(long id, int topi) {
		if (topi == 1) {
			edge.first = id;
		} else {
			edge.last = id;
		}
	}

	public void newEdge(long id) {
		edge = new Edge();
		edges.put(id, edge);
	}

	public void endFile() {
		for (long id : index.keySet()) {
			Feature feature = index.get(id);
			for (Reln rel : feature.rels) {
				Feature reln = index.get(rel.id);
				reln.reln = rel.reln;
				if (feature.reln == Rflag.UNKN) {
					switch (rel.reln) {
					case MASTER:
						feature.reln = Rflag.AGGR;
						break;
					case SLAVE:
						feature.reln = Rflag.MASTER;
					case PEER:
						feature.reln = Rflag.PEER;
						break;
					default:
						break;
					}
				}
				ObjTab tab = feature.objs.get(reln.type);
				if (tab == null) {
					tab = new ObjTab();
					feature.objs.put(reln.type, tab);
				}
				tab.put(tab.size(), reln.atts);
			}
		}
		for (long id : index.keySet()) {
			Feature feature = index.get(id);
			if (feature.reln == Rflag.UNKN) {
				feature.reln = Rflag.MASTER;
			}
			if ((feature.type != Obj.UNKOBJ) && ((feature.reln == Rflag.MASTER) || (feature.reln == Rflag.PEER))) {
				if (features.get(feature.type) == null) {
					features.put(feature.type, new ArrayList<Feature>());
				}
				features.get(feature.type).add(feature);
			}
		}
		sortGeom();
	}

	// OSM map building methods
	
	public void addNode(long id, double lat, double lon) {
		Snode node = new Snode(Math.toRadians(lat), Math.toRadians(lon));
		nodes.put(id, node);
		feature = new Feature();
		feature.reln = Rflag.AGGR;
		feature.geom.prim = Pflag.POINT;
		feature.geom.elems.add(new Prim(id));
		edge = null;
	}

	public void addEdge(long id) {
		feature = new Feature();
		feature.reln = Rflag.AGGR;
		feature.geom.prim = Pflag.LINE;
		feature.geom.elems.add(new Prim(id));
		edge = new Edge();
	}

	public void addToEdge(long node) {
		if (edge.first == 0) {
			edge.first = node;
			nodes.get(node).flg = Nflag.CONN;
		} else {
			if (edge.last != 0) {
				edge.nodes.add(edge.last);
			}
			edge.last = node;
		}
	}

	public void addArea(long id) {
		feature = new Feature();
		feature.reln = Rflag.AGGR;
		feature.geom.prim = Pflag.AREA;
		feature.geom.elems.add(new Prim(id));
		edge = null;
	}

	public void addToArea(long id, boolean outer) {
		feature.geom.elems.add(new Prim(id, outer));
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
				AttVal<?> attval = S57val.convertValue(val, att);
				if (attval.val != null)
					atts.put(att, attval);
			} else {
				if (subkeys[1].equals("type")) {
					feature.type = S57obj.enumType(val);
					if (feature.objs.get(feature.type) == null) {
						feature.objs.put(feature.type, new ObjTab());
					}
				} else {
					Att att = S57att.enumAttribute(subkeys[1], Obj.UNKOBJ);
					if (att != Att.UNKATT) {
						AttVal<?> attval = S57val.convertValue(val, att);
						if (attval.val != null)
							feature.atts.put(att, attval);
					}
				}
			}
		}
	}

	public void tagsDone(long id) {
		switch (feature.geom.prim) {
		case POINT:
			Snode node = nodes.get(id);
			if (node.flg != Nflag.CONN) {
				node.flg = Nflag.ISOL;
			}
			feature.length = 0;
			feature.area = 0;
			break;
		case LINE:
			edges.put(id, edge);
			nodes.get(edge.first).flg = Nflag.CONN;
			nodes.get(edge.last).flg = Nflag.CONN;
			feature.length = calcLength(feature.geom);
			if (edge.first == edge.last) {
				feature.geom.prim = Pflag.AREA;
				feature.area = calcArea(feature.geom);
			} else {
				feature.area = 0;
			}
			break;
		case AREA:
			break;
		default:
			break;
		}
		if ((feature.type != Obj.UNKOBJ) && !((edge != null) && (edge.last == 0))) {
			index.put(id, feature);
			if (features.get(feature.type) == null) {
				features.put(feature.type, new ArrayList<Feature>());
			}
			feature.centre = findCentroid(feature);
			features.get(feature.type).add(feature);
		}
	}

	// Utility methods
	
	public void sortGeom() {
		for (long id : index.keySet()) {
			feature = index.get(id);
			Geom geom = feature.geom;
			Geom sort = new Geom(geom.prim);
			long first = 0;
			long last = 0;
			Comp comp = null;
			boolean next = true;
			if ((geom.prim == Pflag.LINE) || (geom.prim == Pflag.AREA)) {
				int sweep = geom.elems.size();
				while (!geom.elems.isEmpty()) {
					Prim prim = geom.elems.remove(0);
					Edge edge = edges.get(prim.id);
					if (next == true) {
						next = false;
						if (prim.forward) {
							first = edge.first;
							last = edge.last;
						} else {
							first = edge.last;
							last = edge.first;
						}
						sort.elems.add(prim);
						if (prim.outer) {
							sort.outers++;
						} else {
							sort.inners++;
						}
						comp = new Comp(ref++, 1);
						sort.refs.add(comp);
					} else {
						if (prim.forward) {
							if (edge.first == last) {
								sort.elems.add(prim);
								last = edge.last;
								comp.size++;
							} else if (edge.last == first) {
								sort.elems.add(0, prim);
								first = edge.first;
								comp.size++;
							} else {
								geom.elems.add(prim);
							}
						} else {
							if (edge.last == last) {
								sort.elems.add(prim);
								last = edge.first;
								comp.size++;
							} else if (edge.first == first) {
								sort.elems.add(0, prim);
								first = edge.last;
								comp.size++;
							} else {
								geom.elems.add(prim);
							}
						}
					}
					if (--sweep == 0) {
						next = true;
						sweep = geom.elems.size();
					}
				}
				feature.geom = sort;
			} 
			if (geom.prim == Pflag.AREA) {
				ArrayList<Prim> outers = new ArrayList<Prim>();
				ArrayList<Prim> inners = new ArrayList<Prim>();
				for (Prim prim : feature.geom.elems) {
					if (prim.outer) {
						outers.add(prim);
					} else {
						inners.add(prim);
					}
				}
				ArrayList<Prim> sorting = outers;
				ArrayList<Prim> closed = null;
				sort = new Geom(geom.prim);
				sort.outers = feature.geom.outers;
				sort.inners = feature.geom.inners;
				sort.refs = feature.geom.refs;
				next = true;
				while (!sorting.isEmpty()) {
					Prim prim = sorting.remove(0);
					Edge edge = edges.get(prim.id);
					if (next == true) {
						next = false;
						closed = new ArrayList<Prim>();
						closed.add(prim);
						if (prim.forward) {
							first = edge.first;
							last = edge.last;
						} else {
							first = edge.last;
							last = edge.first;
						}
					} else {
						if (prim.forward) {
							if (edge.first == last) {
								last = edge.last;
								closed.add(prim);
							} else {
								sorting.add(0, prim);
								next = true;
							}
						} else {
							if (edge.last == last) {
								last = edge.first;
								closed.add(prim);
							} else {
								sorting.add(0, prim);
								next = true;
							}
						}
					}
					if (first == last) {
						sort.elems.addAll(closed);
						next = true;
					}
					if (sorting.isEmpty() && sorting == outers) {
						sorting = inners;
						next = true;
					}
				}
				feature.geom = sort;
			}
		}
	}
	
	public class EdgeIterator {
		Edge edge;
		boolean forward;
		ListIterator<Long> it;

		public EdgeIterator(Edge e, boolean dir) {
			edge = e;
			forward = dir;
			it = null;
		}

		public boolean hasNext() {
			return (edge != null);
		}

		public long nextRef() {
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
			return ref;
		}
		
		public Snode next() {
			return nodes.get(nextRef());
		}
	}

	public class GeomIterator {
		Geom geom;
		Prim prim;
		EdgeIterator eit;
		ListIterator<S57map.Prim> it;
		int cc, ec;
		Comp comp;
		
		public GeomIterator(Geom g) {
			geom = g;
			eit = null;
			cc = ec = 0;
			comp = null;
			if ((geom.prim != Pflag.NOSP) && (geom.prim != Pflag.POINT)) {
				it = geom.elems.listIterator();
			} else {
				it = null;
			}
		}
		
		public boolean hasMore() {
			return (cc < geom.refs.size());
		}
		
		public long getMore() {
			comp = geom.refs.get(cc++);
			ec = 0;
			return comp.ref;
		}
		
		public boolean hasNext() {
			if (eit == null) {
				return (ec < comp.size);
			} else {
				return (eit.hasNext());
			}
		}
		
		public long nextRef() {
			if ((eit == null) && (ec < comp.size)) {
				prim = geom.elems.get(ec++);
				eit = new EdgeIterator(edges.get(prim.id), prim.forward);
			}
			long ref = eit.nextRef();
			if (!eit.hasNext()) {
				eit = null;
			}
			return ref;
		}
		
		public Snode next() {
			return nodes.get(nextRef());
		}
	}
	
	double signedArea(Geom geom) {
		Snode node;
		double lat, lon, llon, llat;
		lat = lon = llon = llat = 0;
		double sigma = 0;
		GeomIterator it = new GeomIterator(geom);
		it.getMore();
		while (it.hasNext()) {
			llon = lon;
			llat = lat;
			node = it.next();
			lat = node.lat;
			lon = node.lon;
			sigma += (lon * Math.sin(llat)) - (llon * Math.sin(lat));
		}
		return sigma / 2.0;
	}

	public boolean handOfArea(Geom geom) {
		return (signedArea(geom) < 0);
	}

	public double calcArea(Geom geom) {
		return Math.abs(signedArea(geom)) * 3444 * 3444;
	}

	public double calcLength(Geom geom) {
		Snode node;
		double lat, lon, llon, llat;
		lat = lon = llon = llat = 0;
		double sigma = 0;
		GeomIterator it = new GeomIterator(geom);
		it.getMore();
		if (it.hasNext()) {
			node = it.next();
			lat = node.lat;
			lon = node.lon;
			while (it.hasNext()) {
				llon = lon;
				llat = lat;
				node = it.next();
				lat = node.lat;
				lon = node.lon;
				sigma += Math.acos(Math.sin(lat) * Math.sin(llat) + Math.cos(lat) * Math.cos(llat) * Math.cos(llon - lon));
			}
		}
		return sigma * 3444;
	}

	public Snode findCentroid(Feature feature) {
		double lat, lon, slat, slon, llat, llon;
		llat = llon = lat = lon = slat = slon = 0;
		double sarc = 0;
		boolean first = true;
		switch (feature.geom.prim) {
		case POINT:
			return nodes.get(feature.geom.elems.get(0).id);
		case LINE:
			GeomIterator it = new GeomIterator(feature.geom);
			it.getMore();
			while (it.hasNext()) {
				Snode node = it.next();
				lat = node.lat;
				lon = node.lon;
				if (first) {
					first = false;
				} else {
					sarc += (Math.acos(Math.cos(lon - llon) * Math.cos(lat - llat)));
				}
				llat = lat;
				llon = lon;
			}
			double harc = sarc / 2;
			sarc = 0;
			first = true;
			it = new GeomIterator(feature.geom);
			while (it.hasNext()) {
				it.getMore();
				Snode node = it.next();
				lat = node.lat;
				lon = node.lon;
				if (first) {
					first = false;
				} else {
					sarc = (Math.acos(Math.cos(lon - llon) * Math.cos(lat - llat)));
					if (sarc > harc)
						break;
				}
				harc -= sarc;
				llat = lat;
				llon = lon;
			}
			return new Snode(llat + ((lat - llat) * harc / sarc), llon + ((lon - llon) * harc / sarc));
		case AREA:
			GeomIterator bit = new GeomIterator(feature.geom);
			bit.getMore();
			while (bit.hasNext()) {
				Snode node = bit.next();
				lat = node.lat;
				lon = node.lon;
				if (first) {
					first = false;
				} else {
					double arc = (Math.acos(Math.cos(lon - llon) * Math.cos(lat - llat)));
					slat += ((lat + llat) / 2 * arc);
					slon += ((lon + llon) / 2 * arc);
					sarc += arc;
				}
				llon = lon;
				llat = lat;
			}
			return new Snode((sarc > 0.0 ? slat / sarc : 0.0), (sarc > 0.0 ? slon / sarc : 0.0));
		default:
		}
		return null;
	}

}
