/* Copyright 2015 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package s57;

import java.io.BufferedReader;
import java.util.*;

import s57.S57obj.*;
import s57.S57att.*;
import s57.S57val.*;

public class S57osm { // OSM to S57 Object/Attribute and Object/Primitive conversions
	
	static class KeyVal<V> {
		Obj obj;
		Att att;
		Conv conv;
		V val;
		KeyVal(Obj o, Att a, Conv c, V v) {
			obj = o;
			att = a;
			conv = c;
			val = v;
		}
	}
	
	private static final HashMap<String, KeyVal<?>> OSMtags = new HashMap<>();
	static {
		OSMtags.put("natural=coastline", new KeyVal<>(Obj.COALNE, Att.UNKATT, null, null)); OSMtags.put("natural=water", new KeyVal<>(Obj.LAKARE, Att.UNKATT, null, null));
		OSMtags.put("water=river", new KeyVal<>(Obj.RIVERS, Att.UNKATT, null, null)); OSMtags.put("water=canal", new KeyVal<>(Obj.CANALS, Att.UNKATT, null, null));
		OSMtags.put("waterway=riverbank", new KeyVal<>(Obj.RIVERS, Att.UNKATT, null, null)); OSMtags.put("waterway=dock", new KeyVal<>(Obj.HRBBSN, Att.UNKATT, null, null));
		OSMtags.put("waterway=lock", new KeyVal<>(Obj.HRBBSN, Att.UNKATT, null, null)); OSMtags.put("landuse=basin", new KeyVal<>(Obj.LAKARE, Att.UNKATT, null, null));
		OSMtags.put("wetland=tidalflat", new KeyVal<Double>(Obj.DEPARE, Att.DRVAL2, Conv.F, (Double)0.0)); OSMtags.put("tidal=yes", new KeyVal<Double>(Obj.DEPARE, Att.DRVAL2, Conv.F, (Double)0.0));
		OSMtags.put("natural=mud", new KeyVal<>(Obj.DEPARE, Att.UNKATT, null, null)); OSMtags.put("natural=sand", new KeyVal<>(Obj.DEPARE, Att.UNKATT, null, null));
		OSMtags.put("highway=motorway", new KeyVal<>(Obj.ROADWY, Att.CATROD, Conv.E, CatROD.ROD_MWAY)); OSMtags.put("highway=trunk", new KeyVal<>(Obj.ROADWY, Att.CATROD, Conv.E, CatROD.ROD_MAJR));
		OSMtags.put("highway=primary", new KeyVal<>(Obj.ROADWY, Att.CATROD, Conv.E, CatROD.ROD_MAJR)); OSMtags.put("highway=secondary", new KeyVal<>(Obj.ROADWY, Att.CATROD, Conv.E, CatROD.ROD_MINR));
		OSMtags.put("highway=tertiary", new KeyVal<>(Obj.ROADWY, Att.CATROD, Conv.E, CatROD.ROD_MINR)); OSMtags.put("highway=residential", new KeyVal<>(Obj.ROADWY, Att.UNKATT, null, null));
		OSMtags.put("highway=unclassified", new KeyVal<>(Obj.ROADWY, Att.UNKATT, null, null)); OSMtags.put("railway=rail", new KeyVal<>(Obj.RAILWY, Att.UNKATT, null, null));
		OSMtags.put("man_made=breakwater", new KeyVal<>(Obj.SLCONS, Att.UNKATT, null, null)); OSMtags.put("man_made=groyne", new KeyVal<>(Obj.SLCONS, Att.UNKATT, null, null));
		OSMtags.put("man_made=pier", new KeyVal<>(Obj.SLCONS, Att.UNKATT, null, null)); OSMtags.put("man_made=jetty", new KeyVal<>(Obj.SLCONS, Att.UNKATT, null, null));
		OSMtags.put("landuse=industrial", new KeyVal<>(Obj.BUAARE, Att.UNKATT, null, null)); OSMtags.put("landuse=commercial", new KeyVal<>(Obj.BUAARE, Att.UNKATT, null, null));
		OSMtags.put("landuse=retail", new KeyVal<>(Obj.BUAARE, Att.UNKATT, null, null)); OSMtags.put("landuse=residential", new KeyVal<>(Obj.BUAARE, Att.UNKATT, null, null));
		OSMtags.put("place=city", new KeyVal<>(Obj.BUAARE, Att.CATBUA, Conv.E, CatBUA.BUA_CITY)); OSMtags.put("place=town", new KeyVal<>(Obj.BUAARE, Att.CATBUA, Conv.E, CatBUA.BUA_TOWN));
		OSMtags.put("place=village", new KeyVal<>(Obj.BUAARE, Att.CATBUA, Conv.E, CatBUA.BUA_VLLG));
		}
	
	public static void OSMtag(ArrayList<KeyVal<?>> osm, String key, String val) {
		KeyVal<?> kv = OSMtags.get(key + "=" + val);
		if (kv != null) {
			if (kv.conv == Conv.E) {
				ArrayList<Enum<?>> list = new ArrayList<Enum<?>>();
				list.add((Enum<?>)kv.val);
				osm.add(new KeyVal<>(kv.obj, kv.att, kv.conv, list));
			} else {
				osm.add(kv);
			}
		}
		KeyVal<?> kvl = null;
		KeyVal<?> kvd = null;
		boolean rc = false;
		boolean rcl = false;
		for (KeyVal<?> kvx : osm) {
			if (kvx.obj == Obj.LAKARE) {
				kvl = kvx;
			} else if ((kvx.obj == Obj.RIVERS) || (kvx.obj == Obj.CANALS)) {
				rc = true;
			}
			if (kvx.obj == Obj.DEPARE) {
				kvd = kvx;
			} else if ((kvx.obj == Obj.RIVERS) || (kvx.obj == Obj.CANALS) || (kvx.obj == Obj.LAKARE)) {
				rcl = true;
			}
		}
		if (rc && (kvl != null)) {
			osm.remove(kvl);
		}
		if (rcl && (kvd != null)) {
			osm.remove(kvd);
		}
		return;
	}
	
	public static void OSMmap(BufferedReader in, S57map map, boolean bb) throws Exception {
		String k = "";
		String v = "";

		double lat = 0;
		double lon = 0;
		long id = 0;

		boolean inOsm = false;
		boolean inNode = false;
		boolean inWay = false;
		boolean inRel = false;
		map.nodes.put(1l, map.new Snode());
		map.nodes.put(2l, map.new Snode());
		map.nodes.put(3l, map.new Snode());
		map.nodes.put(4l, map.new Snode());

		String ln;
		while ((ln = in.readLine()) != null) {
			if (inOsm) {
				if (ln.contains("<bounds") && !bb) {
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^minlat=.+")) {
							map.bounds.minlat = Math.toRadians(Double.parseDouble(token.split("[\"\']")[1]));
							map.nodes.get(2l).lat = map.bounds.minlat;
							map.nodes.get(3l).lat = map.bounds.minlat;
						} else if (token.matches("^minlon=.+")) {
							map.bounds.minlon = Math.toRadians(Double.parseDouble(token.split("[\"\']")[1]));
							map.nodes.get(1l).lon = map.bounds.minlon;
							map.nodes.get(2l).lon = map.bounds.minlon;
						} else if (token.matches("^maxlat=.+")) {
							map.bounds.maxlat = Math.toRadians(Double.parseDouble(token.split("[\"\']")[1]));
							map.nodes.get(1l).lat = map.bounds.maxlat;
							map.nodes.get(4l).lat = map.bounds.maxlat;
						} else if (token.matches("^maxlon=.+")) {
							map.bounds.maxlon = Math.toRadians(Double.parseDouble(token.split("[\"\']")[1]));
							map.nodes.get(3l).lon = map.bounds.maxlon;
							map.nodes.get(4l).lon = map.bounds.maxlon;
						}
					}
				} else {
					if ((inNode || inWay || inRel) && (ln.contains("<tag"))) {
						k = v = "";
						String[] token = ln.split("k=");
						k = token[1].split("[\"\']")[1];
						token = token[1].split("v=");
						v = token[1].split("[\"\']")[1];
						if (!k.isEmpty() && !v.isEmpty()) {
							map.addTag(k, v);
						}
					}
					if (inNode) {
						if (ln.contains("</node")) {
							inNode = false;
							map.tagsDone(id);
						}
					} else if (ln.contains("<node")) {
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^id=.+")) {
								id = Long.parseLong(token.split("[\"\']")[1]);
							} else if (token.matches("^lat=.+")) {
								lat = Double.parseDouble(token.split("[\"\']")[1]);
							} else if (token.matches("^lon=.+")) {
								lon = Double.parseDouble(token.split("[\"\']")[1]);
							}
						}
						map.addNode(id, lat, lon);
						if (ln.contains("/>")) {
							map.tagsDone(id);
						} else {
							inNode = true;
						}
					} else if (inWay) {
						if (ln.contains("<nd")) {
							long ref = 0;
							for (String token : ln.split("[ ]+")) {
								if (token.matches("^ref=.+")) {
									ref = Long.parseLong(token.split("[\"\']")[1]);
								}
							}
							try {
								map.addToEdge(ref);
							} catch (Exception e) {
								inWay = false;
							}
						}
						if (ln.contains("</way")) {
							inWay = false;
							map.tagsDone(id);
						}
					} else if (ln.contains("<way")) {
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^id=.+")) {
								id = Long.parseLong(token.split("[\"\']")[1]);
							}
						}
						map.addEdge(id);
						if (ln.contains("/>")) {
							map.tagsDone(0);
						} else {
							inWay = true;
						}
					} else if (ln.contains("</osm")) {
						map.mapDone();
						inOsm = false;
						break;
					} else if (inRel) {
						if (ln.contains("<member")) {
							String type = "";
							String role = "";
							long ref = 0;
							for (String token : ln.split("[ ]+")) {
								if (token.matches("^ref=.+")) {
									ref = Long.parseLong(token.split("[\"\']")[1]);
								} else if (token.matches("^type=.+")) {
									type = (token.split("[\"\']")[1]);
								} else if (token.matches("^role=.+")) {
									String str[] = token.split("[\"\']");
									if (str.length > 1) {
										role = (token.split("[\"\']")[1]);
									}
								}
							}
							if ((role.equals("outer") || role.equals("inner")) && type.equals("way"))
								map.addToArea(ref, role.equals("outer"));
						}
						if (ln.contains("</relation")) {
							inRel = false;
							map.tagsDone(id);
						}
					} else if (ln.contains("<relation")) {
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^id=.+")) {
								id = Long.parseLong(token.split("[\"\']")[1]);
							}
						}
						map.addArea(id);
						if (ln.contains("/>")) {
							map.tagsDone(id);
						} else {
							inRel = true;
						}
					}
				}
			} else if (ln.contains("<osm")) {
				inOsm = true;
			}
		}
		return;
	}
	
	public static void OSMmeta(S57map map) {
		map.addEdge(++map.xref);
		for (long ref = 0; ref <= 4; ref++) {
			map.addToEdge((ref == 0) ? 4 : ref);
		}
		map.addTag("seamark:type", "coverage");
		map.addTag("seamark:coverage:category", "coverage");
		map.tagsDone(map.xref);
	}
	
}
