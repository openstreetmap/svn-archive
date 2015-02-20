/* Copyright 2014 Malcolm Herring
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
import s57.S57osm;
import s57.S57osm.*;

public class S57map {
	
	public class MapBounds {
		public double minlat;
		public double minlon;
		public double maxlat;
		public double maxlon;
		public MapBounds() {
			minlat = Math.toRadians(90);
			minlon = Math.toRadians(180);
			maxlat = Math.toRadians(-90);
			maxlon = Math.toRadians(-180);
		}
	}

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
		UNKN, MASTER, SLAVE
	}
	
	public class Reln {
		public long id;
		public Rflag reln;
		public Reln(long i, Rflag r) {
			id = i;
			reln = r;
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

	public class AttMap extends HashMap<Att, AttVal<?>> {
		public AttMap() {
			super();
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
	
	public class Comp {			// Composite spatial element
		public long ref;			// ID of Comp
		public int size;			// Number of Prims in this Comp
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
		public double area;						// Area of feature
		public double length;					// Length of feature
		public Snode centre;					// Centre of feature
		public Geom(Pflag p) {
			prim = p;
			elems = new ArrayList<Prim>();
			outers = inners = 0;
			refs = new ArrayList<Comp>();
			area = 0;
			length = 0;
			centre = new Snode();
		}
	}
	
	public class Feature {
		public Rflag reln;		// Relationship status
		public Geom geom;			// Geometry data
		public Obj type;			// Feature type
		public AttMap atts;		// Feature attributes
		public RelTab rels;		// Related objects
		public ObjMap objs;		// Slave object attributes

		Feature() {
			reln = Rflag.UNKN;
			geom = new Geom(Pflag.NOSP);
			type = Obj.UNKOBJ;
			atts = new AttMap();
			rels = new RelTab();
			objs = new ObjMap();
		}
	}
	
	class OSMtag {
		String key;
		String val;
		OSMtag(String k, String v) {
			key = k;
			val = v;
		}
	}
	
	public NodeTab nodes;
	public EdgeTab edges;

	public FtrMap features;
	public FtrTab index;
	
	public MapBounds bounds;

	public long cref;
	public long xref;
	private Feature feature;
	private Edge edge;

	public S57map() {
		nodes = new NodeTab();		// All nodes in map
		edges = new EdgeTab();		// All edges in map
		feature = new Feature();	// Current feature being built
		features = new FtrMap();	// All features in map, grouped by type
		index = new FtrTab();			// Feature look-up table
		bounds = new MapBounds();
		cref = 0x0000ffffffff0000L;// Compound reference generator
		xref = 0x0fff000000000000L;// Extras reference generator
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
		if (obj == Obj.BCNWTW)
			obj = Obj.BCNLAT;
		if (obj == Obj.BOYWTW)
			obj = Obj.BOYLAT;
		if (obj == Obj.C_AGGR)
			feature.reln = Rflag.UNKN;
		feature.geom = new Geom(p);
		feature.type = obj;
		if (obj != Obj.UNKOBJ) {
			index.put(id, feature);
		}
	}
	
	public void newObj(long id, int rind) {
		Rflag r = Rflag.UNKN;
		switch (rind) {
		case 1:
			r = Rflag.MASTER;
			break;
		case 2:
			r = Rflag.SLAVE;
			break;
		case 3:
			r = Rflag.UNKN;
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
			sortGeom(feature);
			for (Reln reln : feature.rels) {
				Feature rel = index.get(reln.id);
				if (cmpGeoms(feature.geom, rel.geom)) {
					switch (reln.reln) {
					case SLAVE:
						feature.reln = Rflag.MASTER;
						break;
					default:
						feature.reln = Rflag.UNKN;
						break;
					}
					rel.reln = reln.reln; 
				} else {
					reln.reln = Rflag.UNKN;
				}
			}
		}
		for (long id : index.keySet()) {
			Feature feature = index.get(id);
			if (feature.reln == Rflag.UNKN) {
				feature.reln = Rflag.MASTER;
			}
			if ((feature.type != Obj.UNKOBJ) && (feature.reln == Rflag.MASTER)) {
				if (features.get(feature.type) == null) {
					features.put(feature.type, new ArrayList<Feature>());
				}
				features.get(feature.type).add(feature);
			}
		}
		for (long id : index.keySet()) {
			Feature feature = index.get(id);
			for (Reln reln : feature.rels) {
				Feature rel = index.get(reln.id);
				if (rel.reln == Rflag.SLAVE) {
					if (feature.objs.get(rel.type) == null) {
						feature.objs.put(rel.type, new ObjTab());
					}
					ObjTab tab = feature.objs.get(rel.type);
					int ix = tab.size();
					tab.put(ix, rel.atts);
				}
			}
		}
	}

	// OSM map building methods
	
	public void addNode(long id, double lat, double lon) {
		Snode node = new Snode(Math.toRadians(lat), Math.toRadians(lon));
		nodes.put(id, node);
		feature = new Feature();
		feature.reln = Rflag.UNKN;
		feature.geom.prim = Pflag.POINT;
		feature.geom.elems.add(new Prim(id));
		edge = null;
	}

	public void addEdge(long id) {
		feature = new Feature();
		feature.reln = Rflag.UNKN;
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
		feature.reln = Rflag.UNKN;
		feature.geom.prim = Pflag.AREA;
		edge = null;
	}

	public void addToArea(long id, boolean outer) {
		feature.geom.elems.add(new Prim(id, outer));
	}

	public void addTag(String key, String val) {
		feature.reln = Rflag.MASTER;
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
				ObjTab objs = feature.objs.get(obj);
				if (objs == null) {
					objs = new ObjTab();
					feature.objs.put(obj, objs);
				}
				AttMap atts = objs.get(idx);
				if (atts == null) {
					atts = new AttMap();
					objs.put(idx, atts);
				}
				AttVal<?> attval = S57val.convertValue(val, att);
				if (attval.val != null)
					atts.put(att, attval);
			} else {
				if (subkeys[1].equals("type")) {
					obj = S57obj.enumType(val);
					feature.type = obj;
					ObjTab objs = feature.objs.get(obj);
					if (objs == null) {
						objs = new ObjTab();
						feature.objs.put(obj, objs);
					}
					AttMap atts = objs.get(0);
					if (atts == null) {
						atts = new AttMap();
						objs.put(0, atts);
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
		} else {
			KeyVal kv = S57osm.OSMtag(key, val);
			if (kv.obj != Obj.UNKOBJ) {
				if (feature.type == Obj.UNKOBJ) {
					feature.type = kv.obj;
				}
				ObjTab objs = feature.objs.get(kv.obj);
				if (objs == null) {
					objs = new ObjTab();
					feature.objs.put(kv.obj, objs);
				}
				AttMap atts = objs.get(0);
				if (atts == null) {
					atts = new AttMap();
					objs.put(0, atts);
				}
				if (kv.att != Att.UNKATT) {
					atts.put(kv.att, new AttVal(kv.att, kv.conv, kv.val));
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
			break;
		case LINE:
			edges.put(id, edge);
			nodes.get(edge.first).flg = Nflag.CONN;
			nodes.get(edge.last).flg = Nflag.CONN;
			if (edge.first == edge.last) {
				feature.geom.prim = Pflag.AREA;
			}
			break;
		case AREA:
			break;
		default:
			break;
		}
		if ((sortGeom(feature)) && (feature.type != Obj.UNKOBJ) && !((edge != null) && (edge.last == 0))) {
			index.put(id, feature);
			if (features.get(feature.type) == null) {
				features.put(feature.type, new ArrayList<Feature>());
			}
			features.get(feature.type).add(feature);
			feature.geom.centre = findCentroid(feature);
		}
	}
	
	public void mapDone() {
		ArrayList<Feature> coasts = new ArrayList<Feature>();
		ArrayList<Feature> lands = new ArrayList<Feature>();
		if (features.get(Obj.LNDARE) == null) {
			features.put(Obj.LNDARE, new ArrayList<Feature>());
		}
		for (Feature feature : features.get(Obj.COALNE)) {
			Feature land = new Feature();
			land.type = Obj.LNDARE;
			land.reln = Rflag.MASTER;
			land.objs.put(Obj.LNDARE, new ObjTab());
			if (feature.geom.prim == Pflag.AREA) {
				land.geom = feature.geom;
				features.get(Obj.LNDARE).add(land);
			} else if (feature.geom.prim == Pflag.LINE) {
				land.geom.prim = Pflag.LINE;
				for (int i = 0; i < feature.geom.elems.size(); i++) {
					land.geom.elems.add(feature.geom.elems.get(i));
				}
				coasts.add(land);
			}
		}
		while (coasts.size() > 0) {
			Feature land = coasts.remove(0);
			Edge fedge = edges.get(land.geom.elems.get(0).id);
			long first = fedge.first;
			long last = edges.get(land.geom.elems.get(land.geom.elems.size() - 1).id).last;
			if (coasts.size() > 0) {
				boolean added = true;
				while (added) {
					added = false;
					for (int i = 0; i < coasts.size(); i++) {
						Feature coast = coasts.get(i);
						Edge edge = edges.get(coast.geom.elems.get(0).id);
						if (edge.first == last) {
							land.geom.elems.add(coast.geom.elems.get(0));
							last = edge.last;
							coasts.remove(i--);
							added = true;
						} else if (edge.last == first) {
							land.geom.elems.add(0, coast.geom.elems.get(0));
							first = edge.first;
							coasts.remove(i--);
							added = true;
						}
					}
				}
			}
			lands.add(land);
		}
		for (Feature land : lands) {
			long first = edges.get(land.geom.elems.get(0).id).first;
			long last = edges.get(land.geom.elems.get(land.geom.elems.size()-1).id).last;
			Ext fext = outsideBounds(first);
			Ext lext = outsideBounds(last);
			Edge nedge = new Edge();
			nedge.first = last;
			nedge.last = first;
			switch (lext) {
			case N:
				if ((lext == fext) || (fext != Ext.N)) {
					nedge.nodes.add(1L);
					if ((fext != Ext.W)) {
						nedge.nodes.add(2L);
						if ((fext != Ext.S)) {
							nedge.nodes.add(3L);
							if ((fext != Ext.W)) {
								nedge.nodes.add(4L);
							}
						}
					}
				}
				break;
			case W:
				if ((lext == fext) || (fext != Ext.W)) {
					nedge.nodes.add(2L);
					if ((fext != Ext.S)) {
						nedge.nodes.add(3L);
						if ((fext != Ext.E)) {
							nedge.nodes.add(4L);
							if ( (fext != Ext.N)) {
								nedge.nodes.add(1L);
							}
						}
					}
				}
				break;
			case S:
				if ((lext == fext) || (fext != Ext.S)) {
					nedge.nodes.add(3L);
					if ((fext != Ext.E)) {
						nedge.nodes.add(4L);
						if ((fext != Ext.N)) {
							nedge.nodes.add(1L);
							if ((fext != Ext.W)) {
								nedge.nodes.add(2L);
							}
						}
					}
				}
				break;
			case E:
				if ((lext == fext) || (fext != Ext.E)) {
					nedge.nodes.add(4L);
					if ((fext != Ext.N)) {
						nedge.nodes.add(1L);
						if ((fext != Ext.W)) {
							nedge.nodes.add(2L);
							if ((fext != Ext.S)) {
								nedge.nodes.add(3L);
							}
						}
					}
				}
				break;
			default:
			}
			edges.put(++xref, nedge);
			land.geom.elems.add(new Prim(xref));
			sortGeom(land);
			features.get(Obj.LNDARE).add(land);
		}
		return;
	}

	// Utility methods
	
	enum Ext {I, N, W, S, E }
	class Xnode {
		double lat;
		double lon;
		Ext ext;
	}
	Ext outsideBounds(long ref) {
		Snode node = nodes.get(ref);
		if (node.lat >= bounds.maxlat) {
			return Ext.N;
		}
		if (node.lat <= bounds.minlat) {
			return Ext.S;
		}
		if (node.lon >= bounds.maxlon) {
			return Ext.E;
		}
		if (node.lon <= bounds.minlon) {
			return Ext.W;
		}
		return Ext.I;
	}
	
	public boolean sortGeom(Feature feature) {
		Geom sort = new Geom(feature.geom.prim);
		long first = 0;
		long last = 0;
		Comp comp = null;
		boolean next = true;
		feature.geom.length = 0;
		feature.geom.area = 0;
		if (feature.geom.prim == Pflag.POINT) { 
			return true;
		}	else {
			int sweep = feature.geom.elems.size();
			while (!feature.geom.elems.isEmpty()) {
				Prim prim = feature.geom.elems.remove(0);
				Edge edge = edges.get(prim.id);
				if (edge == null) {
					return false;
				}
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
					comp = new Comp(cref++, 1);
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
							feature.geom.elems.add(prim);
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
							feature.geom.elems.add(prim);
						}
					}
				}
				if (--sweep == 0) {
					next = true;
					sweep = feature.geom.elems.size();
				}
			}
			if ((sort.prim == Pflag.LINE) && (sort.outers == 1) && (sort.inners == 0) && (first == last)) {
				sort.prim = Pflag.AREA;
			}
			feature.geom = sort;
		}
		if (feature.geom.prim == Pflag.AREA) {
			ArrayList<Prim> outers = new ArrayList<Prim>();
			ArrayList<Prim> inners = new ArrayList<Prim>();
			for (Prim prim : feature.geom.elems) {
				if (prim.outer) {
					outers.add(prim);
				} else {
					inners.add(prim);
				}
			}
			ArrayList<Prim> sorting = outers.size() > 0 ? outers : inners;
			ArrayList<Prim> closed = null;
			sort = new Geom(feature.geom.prim);
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
		feature.geom.length = calcLength(feature.geom);
		feature.geom.area = calcArea(feature.geom);
		return true;
	}
	
	public boolean cmpGeoms (Geom g1, Geom g2) {
		return ((g1.prim == g2.prim) && (g1.outers == g2.outers) && (g1.inners == g2.inners) && (g1.elems.size() == g2.elems.size()));
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
		ListIterator<S57map.Prim> ite;
		ListIterator<Comp> itc;
		Comp comp;
		int ec;
		long lastref;

		public GeomIterator(Geom g) {
			geom = g;
			lastref = 0;
			ite = geom.elems.listIterator();
			itc = geom.refs.listIterator();
		}
		
		public boolean hasComp() {
			return (itc.hasNext());
		}
		
		public long nextComp() {
			comp = itc.next();
			ec = comp.size;
			lastref = 0;
			return comp.ref;
		}
		
		public boolean hasEdge() {
			return (ec > 0) && ite.hasNext();
		}
		
		public long nextEdge() {
			prim = ite.next();
			eit = new EdgeIterator(edges.get(prim.id), prim.forward);
			ec--;
			return prim.id;
		}
		
		public boolean hasNode() {
			return (eit.hasNext());
		}
		
		public long nextRef(boolean all) {
			long ref = eit.nextRef();
			if (!all && (ref == lastref)) {
				ref = eit.nextRef();
			}
			lastref = ref;
			return ref;
		}
		
		public long nextRef() {
			return nextRef(false);
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
		GeomIterator git = new GeomIterator(geom);
		if (git.hasComp()) {
			git.nextComp();
			while (git.hasEdge()) {
				git.nextEdge();
				while (git.hasNode()) {
					node = git.next();
					if (node == null) continue;
					llon = lon;
					llat = lat;
					lat = node.lat;
					lon = node.lon;
					sigma += (lon * Math.sin(llat)) - (llon * Math.sin(lat));
				}
			}
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
		boolean first = true;
		GeomIterator git = new GeomIterator(geom);
		while (git.hasComp()) {
			git.nextComp();
			while (git.hasEdge()) {
				git.nextEdge();
				while (git.hasNode()) {
					node = git.next();
					if (first) {
						first = false;
						lat = node.lat;
						lon = node.lon;
					} else if (node != null) {
						llat = lat;
						llon = lon;
						lat = node.lat;
						lon = node.lon;
						sigma += Math.acos(Math.sin(lat) * Math.sin(llat) + Math.cos(lat) * Math.cos(llat) * Math.cos(llon - lon));
					}
				}
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
			GeomIterator git = new GeomIterator(feature.geom);
			while (git.hasComp()) {
				git.nextComp();
				while (git.hasEdge()) {
					git.nextEdge();
					while (git.hasNode()) {
						Snode node = git.next();
						if (node == null) continue;
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
				}
			}
			double harc = sarc / 2;
			sarc = 0;
			first = true;
			git = new GeomIterator(feature.geom);
			while (git.hasComp()) {
				git.nextComp();
				while (git.hasEdge()) {
					git.nextEdge();
					while (git.hasNode()) {
						Snode node = git.next();
						if (node == null) continue;
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
				}
			}
			return new Snode(llat + ((lat - llat) * harc / sarc), llon + ((lon - llon) * harc / sarc));
		case AREA:
			git = new GeomIterator(feature.geom);
			while (git.hasComp()) {
				git.nextComp();
				while (git.hasEdge()) {
					git.nextEdge();
					while (git.hasNode()) {
						Snode node = git.next();
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
				}
			}
			return new Snode((sarc > 0.0 ? slat / sarc : 0.0), (sarc > 0.0 ? slon / sarc : 0.0));
		default:
		}
		return null;
	}

}
