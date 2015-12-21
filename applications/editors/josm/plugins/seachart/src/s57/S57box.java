/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package s57;

import java.util.ArrayList;

import s57.S57map.*;
import s57.S57obj.*;

public class S57box { //S57 bounding box truncation
	
	enum Ext {I, N, W, S, E }
	
	public static void bBox(S57map map) {
		/* Truncations
		 * Points: delete point features outside BB
		 * Lines: Truncate edges at BB boundaries
		 * Areas: Truncate edges of outers & inners and add new border edges. Merge inners to outer where necessary
		 * Remove nodes outside BB
		 * Remove edges that are completely outside BB
		 */
		class Land {
			long first;
			Snode start;
			Ext sbound;
			long last;
			Snode end;
			Ext ebound;
			Feature land;

			Land(Feature l) {
				land = l;
				first = last = 0;
				start = end = null;
				sbound = ebound = Ext.I;
			}
		}
		if (map.features.get(Obj.COALNE) != null) {
			ArrayList<Feature> coasts = new ArrayList<>();
			ArrayList<Land> lands = new ArrayList<>();
			if (map.features.get(Obj.LNDARE) == null) {
				map.features.put(Obj.LNDARE, new ArrayList<Feature>());
			}
			for (Feature feature : map.features.get(Obj.COALNE)) {
				Feature land = map.new Feature();
				land.id = ++map.xref;
				land.type = Obj.LNDARE;
				land.reln = Rflag.MASTER;
				land.objs.put(Obj.LNDARE, map.new ObjTab());
				land.objs.get(Obj.LNDARE).put(0, map.new AttMap());
				if (feature.geom.prim == Pflag.AREA) {
					land.geom = feature.geom;
					map.features.get(Obj.LNDARE).add(land);
				} else if (feature.geom.prim == Pflag.LINE) {
					land.geom.prim = Pflag.LINE;
					land.geom.elems.addAll(feature.geom.elems);
					coasts.add(land);
				}
			}
			while (coasts.size() > 0) {
				Feature land = coasts.remove(0);
				Edge fedge = map.edges.get(land.geom.elems.get(0).id);
				long first = fedge.first;
				long last = map.edges.get(land.geom.elems.get(land.geom.elems.size() - 1).id).last;
				if (coasts.size() > 0) {
					boolean added = true;
					while (added) {
						added = false;
						for (int i = 0; i < coasts.size(); i++) {
							Feature coast = coasts.get(i);
							Edge edge = map.edges.get(coast.geom.elems.get(0).id);
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
				lands.add(new Land(land));
			}
			ArrayList<Land> islands = new ArrayList<>();
			for (Land land : lands) {
				map.sortGeom(land.land);
				if (land.land.geom.prim == Pflag.AREA) {
					islands.add(land);
					map.features.get(Obj.LNDARE).add(land.land);
				}
			}
			for (Land island : islands) {
				lands.remove(island);
			}
			for (Land land : lands) {
				GeomIterator git = map.new GeomIterator(land.land.geom);
				Snode prev = null;
				Ext bprev = Ext.I;
				Ext ext;
				land.ebound = land.sbound = Ext.I;
				while (git.hasComp()) {
					git.nextComp();
					while (git.hasEdge()) {
						git.nextEdge();
						while (git.hasNode()) {
							long ref = git.nextRef(false);
							Snode node = map.nodes.get(ref);
							if (node == null)
								continue;
							if (land.first == 0) {
								land.first = ref;
							}
							if (prev == null) {
								prev = node;
							}
							if ((node.lat >= map.bounds.maxlat) && (node.lon < map.bounds.maxlon)) {
								ext = Ext.N;
							} else if (node.lon <= map.bounds.minlon) {
								ext = Ext.W;
							} else if (node.lat <= map.bounds.minlat) {
								ext = Ext.S;
							} else if (node.lon >= map.bounds.maxlon) {
								ext = Ext.E;
							} else {
								ext = Ext.I;
							}
							if (ext == Ext.I) {
								if (land.start == null) {
									land.start = prev;
									land.sbound = bprev;
								}
								land.end = null;
								land.ebound = Ext.I;
							} else {
								if ((land.start != null) && (land.end == null)) {
									land.end = node;
									land.ebound = ext;
								}
							}
							prev = node;
							bprev = ext;
							land.last = ref;
						}
					}
				}
			}
			islands = new ArrayList<>();
			for (Land land : lands) {
				if ((land.sbound == Ext.I) || (land.ebound == Ext.I)) {
					islands.add(land);
				}
			}
			for (Land island : islands) {
				lands.remove(island);
			}
			while (lands.size() > 0) {
				Land land = lands.get(0);
				Edge nedge = map.new Edge();
				nedge.first = land.last;
				Ext bound = land.ebound;
				Snode last = land.end;
				double delta = Math.PI;
				int idx = -1;
				Land next = null;
				while (idx < 0) {
					for (int i = 0; i < lands.size(); i++) {
						next = lands.get(i);
						if (next.sbound == bound) {
							double diff = -Math.PI;
							switch (bound) {
							case N:
								diff = last.lon - next.start.lon;
								break;
							case W:
								diff = last.lat - next.start.lat;
								break;
							case S:
								diff = next.start.lon - last.lon;
								break;
							case E:
								diff = next.start.lat - last.lat;
								break;
							default:
								continue;
							}
							if ((diff >= 0.0) && (diff < delta)) {
								delta = diff;
								idx = i;
							}
						}
					}
					if (idx < 0) {
						long ref = (long) bound.ordinal();
						last = map.nodes.get(ref);
						nedge.nodes.add(ref);
						ref = ref < 4 ? ++ref : 1;
						for (Ext e : Ext.values()) {
							if (ref == e.ordinal()) {
								bound = e;
								break;
							}
						}
					}
				}
				next = lands.get(idx);
				nedge.last = next.first;
				map.edges.put(++map.xref, nedge);
				land.land.geom.elems.add(map.new Prim(map.xref));
				if (next != land) {
					land.land.geom.elems.addAll(next.land.geom.elems);
					land.ebound = next.ebound;
					land.end = next.end;
					land.last = next.last;
					lands.remove(idx);
				}
				map.sortGeom(land.land);
				if (land.land.geom.prim == Pflag.AREA) {
					map.features.get(Obj.LNDARE).add(land.land);
					lands.remove(land);
				}
			}
		}
		return;

	}

}
