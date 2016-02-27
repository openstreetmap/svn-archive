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
	
	static Ext getExt(S57map map, double lat, double lon) {
		if ((lat >= map.bounds.maxlat) && (lon < map.bounds.maxlon)) {
			return Ext.N;
		} else if (lon <= map.bounds.minlon) {
			return Ext.W;
		} else if (lat <= map.bounds.minlat) {
			return Ext.S;
		} else if (lon >= map.bounds.maxlon) {
			return Ext.E;
		}		return Ext.I;
	}
	
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
				land.first = map.edges.get(land.land.geom.elems.get(0).id).first;
				land.start = map.nodes.get(land.first);
				land.sbound = getExt(map, land.start.lat, land.start.lon);
				land.last = map.edges.get(land.land.geom.elems.get(land.land.geom.comps.get(0).size - 1).id).last;
				land.end = map.nodes.get(land.last);
				land.ebound = getExt(map, land.end.lat, land.end.lon);
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
			for (Land land : lands) {
				Edge nedge = map.new Edge();
				nedge.first = land.last;
				nedge.last = land.first;
				Ext bound = land.ebound;
				while (bound != land.sbound) {
					switch (bound) {
					case N:
						nedge.nodes.add(1l);
						bound = Ext.W;
						break;
					case W:
						nedge.nodes.add(2l);
						bound = Ext.S;
						break;
					case S:
						nedge.nodes.add(3l);
						bound = Ext.E;
						break;
					case E:
						nedge.nodes.add(4l);
						bound = Ext.N;
						break;
					default:
						continue;
					}
				}
				map.edges.put(++map.xref, nedge);
				land.land.geom.elems.add(map.new Prim(map.xref));
				land.land.geom.comps.get(0).size++;
				land.land.geom.prim = Pflag.AREA;
				map.features.get(Obj.LNDARE).add(land.land);
			}
		}
		return;

	}

}
