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
import java.io.IOException;
import java.util.*;

import s57.S57map.Feature;
import s57.S57obj.*;
import s57.S57att.*;
import s57.S57val.*;

public class S57osm { // OSM to S57 Object/Attribute and Object/Primitive conversions
	
	enum Prims { N, P, L, A, PA, PL, LA, PLA }
	private static final EnumMap<Obj, Prims> S57prims = new EnumMap<Obj, Prims>(Obj.class);
	static {
		S57prims.put(Obj.UNKOBJ, Prims.PLA); S57prims.put(Obj.M_COVR, Prims.A); S57prims.put(Obj.M_NSYS, Prims.A); S57prims.put(Obj.AIRARE, Prims.PA);
		S57prims.put(Obj.ACHBRT, Prims.PA); S57prims.put(Obj.ACHARE, Prims.PA); S57prims.put(Obj.BCNCAR, Prims.P); S57prims.put(Obj.BCNISD, Prims.P);
		S57prims.put(Obj.BCNLAT, Prims.P); S57prims.put(Obj.BCNSAW, Prims.P); S57prims.put(Obj.BCNSPP, Prims.P); S57prims.put(Obj.BERTHS, Prims.PLA);
		S57prims.put(Obj.BRIDGE, Prims.PLA); S57prims.put(Obj.BUISGL, Prims.PA); S57prims.put(Obj.BUAARE, Prims.PA); S57prims.put(Obj.BOYCAR, Prims.P);
		S57prims.put(Obj.BOYINB, Prims.P); S57prims.put(Obj.BOYISD, Prims.P); S57prims.put(Obj.BOYLAT, Prims.P); S57prims.put(Obj.BOYSAW, Prims.P);
		S57prims.put(Obj.BOYSPP, Prims.P); S57prims.put(Obj.CBLARE, Prims.A); S57prims.put(Obj.CBLOHD, Prims.L); S57prims.put(Obj.CBLSUB, Prims.L);
		S57prims.put(Obj.CANALS, Prims.A); S57prims.put(Obj.CTSARE, Prims.PA); S57prims.put(Obj.CAUSWY, Prims.LA); S57prims.put(Obj.CTNARE, Prims.PA);
		S57prims.put(Obj.CHKPNT, Prims.PA); S57prims.put(Obj.CGUSTA, Prims.P); S57prims.put(Obj.COALNE, Prims.L); S57prims.put(Obj.CONZNE, Prims.A);
		S57prims.put(Obj.COSARE, Prims.A); S57prims.put(Obj.CTRPNT, Prims.P); S57prims.put(Obj.CONVYR, Prims.LA); S57prims.put(Obj.CRANES, Prims.PA);
		S57prims.put(Obj.CURENT, Prims.P); S57prims.put(Obj.CUSZNE, Prims.A); S57prims.put(Obj.DAMCON, Prims.LA); S57prims.put(Obj.DAYMAR, Prims.P);
		S57prims.put(Obj.DWRTCL, Prims.L); S57prims.put(Obj.DWRTPT, Prims.A); S57prims.put(Obj.DEPARE, Prims.A); S57prims.put(Obj.DEPCNT, Prims.L);
		S57prims.put(Obj.DISMAR, Prims.P); S57prims.put(Obj.DOCARE, Prims.A); S57prims.put(Obj.DRGARE, Prims.A); S57prims.put(Obj.DRYDOC, Prims.A);
		S57prims.put(Obj.DMPGRD, Prims.PA); S57prims.put(Obj.DYKCON, Prims.L); S57prims.put(Obj.EXEZNE, Prims.A); S57prims.put(Obj.FAIRWY, Prims.A);
		S57prims.put(Obj.FNCLNE, Prims.L); S57prims.put(Obj.FERYRT, Prims.LA); S57prims.put(Obj.FSHZNE, Prims.A); S57prims.put(Obj.FSHFAC, Prims.PLA);
		S57prims.put(Obj.FSHGRD, Prims.A); S57prims.put(Obj.FLODOC, Prims.A); S57prims.put(Obj.FOGSIG, Prims.P); S57prims.put(Obj.FORSTC, Prims.PLA);
		S57prims.put(Obj.FRPARE, Prims.A); S57prims.put(Obj.GATCON, Prims.PLA); S57prims.put(Obj.GRIDRN, Prims.PA); S57prims.put(Obj.HRBARE, Prims.A);
		S57prims.put(Obj.HRBFAC, Prims.PA); S57prims.put(Obj.HULKES, Prims.PA); S57prims.put(Obj.ICEARE, Prims.A); S57prims.put(Obj.ICNARE, Prims.PA);
		S57prims.put(Obj.ISTZNE, Prims.A); S57prims.put(Obj.LAKARE, Prims.A); S57prims.put(Obj.LNDARE, Prims.PLA); S57prims.put(Obj.LNDELV, Prims.PL);
		S57prims.put(Obj.LNDRGN, Prims.PA); S57prims.put(Obj.LNDMRK, Prims.PLA); S57prims.put(Obj.LIGHTS, Prims.P); S57prims.put(Obj.LITFLT, Prims.P);
		S57prims.put(Obj.LITVES, Prims.P); S57prims.put(Obj.LOCMAG, Prims.PLA); S57prims.put(Obj.LOKBSN, Prims.A); S57prims.put(Obj.LOGPON, Prims.PA);
		S57prims.put(Obj.MAGVAR, Prims.PLA); S57prims.put(Obj.MARCUL, Prims.PLA); S57prims.put(Obj.MIPARE, Prims.PA); S57prims.put(Obj.MORFAC, Prims.PLA);
		S57prims.put(Obj.MPAARE, Prims.PA); S57prims.put(Obj.NAVLNE, Prims.L); S57prims.put(Obj.OBSTRN, Prims.PLA); S57prims.put(Obj.OFSPLF, Prims.PA);
		S57prims.put(Obj.OSPARE, Prims.A); S57prims.put(Obj.OILBAR, Prims.L); S57prims.put(Obj.PILPNT, Prims.P); S57prims.put(Obj.PILBOP, Prims.PA);
		S57prims.put(Obj.PIPARE, Prims.PA); S57prims.put(Obj.PIPOHD, Prims.L); S57prims.put(Obj.PIPSOL, Prims.PL); S57prims.put(Obj.PONTON, Prims.LA);
		S57prims.put(Obj.PRCARE, Prims.PA); S57prims.put(Obj.PRDARE, Prims.PA); S57prims.put(Obj.PYLONS, Prims.PA); S57prims.put(Obj.RADLNE, Prims.L);
		S57prims.put(Obj.RADRNG, Prims.A); S57prims.put(Obj.RADRFL, Prims.P); S57prims.put(Obj.RADSTA, Prims.P); S57prims.put(Obj.RTPBCN, Prims.P);
		S57prims.put(Obj.RDOCAL, Prims.PL); S57prims.put(Obj.RDOSTA, Prims.P); S57prims.put(Obj.RAILWY, Prims.L); S57prims.put(Obj.RAPIDS, Prims.PLA);
		S57prims.put(Obj.RCRTCL, Prims.L); S57prims.put(Obj.RECTRC, Prims.LA); S57prims.put(Obj.RCTLPT, Prims.PA); S57prims.put(Obj.RSCSTA, Prims.P);
		S57prims.put(Obj.RESARE, Prims.A); S57prims.put(Obj.RETRFL, Prims.P); S57prims.put(Obj.RIVERS, Prims.LA); S57prims.put(Obj.ROADWY, Prims.PLA);
		S57prims.put(Obj.RUNWAY, Prims.PLA); S57prims.put(Obj.SNDWAV, Prims.PLA); S57prims.put(Obj.SEAARE, Prims.PA); S57prims.put(Obj.SPLARE, Prims.PA);
		S57prims.put(Obj.SBDARE, Prims.PLA); S57prims.put(Obj.SLCONS, Prims.PLA); S57prims.put(Obj.SISTAT, Prims.P); S57prims.put(Obj.SISTAW, Prims.P);
		S57prims.put(Obj.SILTNK, Prims.PA); S57prims.put(Obj.SLOTOP, Prims.L); S57prims.put(Obj.SLOGRD, Prims.PA); S57prims.put(Obj.SMCFAC, Prims.PA);
		S57prims.put(Obj.SOUNDG, Prims.P); S57prims.put(Obj.SPRING, Prims.P); S57prims.put(Obj.STSLNE, Prims.L); S57prims.put(Obj.SUBTLN, Prims.A);
		S57prims.put(Obj.SWPARE, Prims.A); S57prims.put(Obj.TESARE, Prims.A); S57prims.put(Obj.TS_PRH, Prims.PA); S57prims.put(Obj.TS_PNH, Prims.PA);
		S57prims.put(Obj.TS_PAD, Prims.PA); S57prims.put(Obj.TS_TIS, Prims.PA); S57prims.put(Obj.T_HMON, Prims.PA); S57prims.put(Obj.T_NHMN, Prims.PA);
		S57prims.put(Obj.T_TIMS, Prims.PA); S57prims.put(Obj.TIDEWY, Prims.LA); S57prims.put(Obj.TOPMAR, Prims.P); S57prims.put(Obj.TSELNE, Prims.LA);
		S57prims.put(Obj.TSSBND, Prims.L); S57prims.put(Obj.TSSCRS, Prims.A); S57prims.put(Obj.TSSLPT, Prims.A); S57prims.put(Obj.TSSRON, Prims.A);
		S57prims.put(Obj.TSEZNE, Prims.A); S57prims.put(Obj.TUNNEL, Prims.LA); S57prims.put(Obj.TWRTPT, Prims.A); S57prims.put(Obj.UWTROC, Prims.P);
		S57prims.put(Obj.UNSARE, Prims.A); S57prims.put(Obj.VEGATN, Prims.PLA); S57prims.put(Obj.WATTUR, Prims.PLA); S57prims.put(Obj.WATFAL, Prims.PL);
		S57prims.put(Obj.WEDKLP, Prims.PA); S57prims.put(Obj.WRECKS, Prims.PA); S57prims.put(Obj.TS_FEB, Prims.PA);
		S57prims.put(Obj.NOTMRK, Prims.P); S57prims.put(Obj.WTWAXS, Prims.L); S57prims.put(Obj.WTWPRF, Prims.L); S57prims.put(Obj.BUNSTA, Prims.PA);
		S57prims.put(Obj.COMARE, Prims.A); S57prims.put(Obj.HRBBSN, Prims.A); S57prims.put(Obj.LKBSPT, Prims.A); S57prims.put(Obj.PRTARE, Prims.A);
		S57prims.put(Obj.REFDMP, Prims.P); S57prims.put(Obj.TERMNL, Prims.PA); S57prims.put(Obj.TRNBSN, Prims.PA); S57prims.put(Obj.WTWARE, Prims.A);
		S57prims.put(Obj.WTWGAG, Prims.PA); S57prims.put(Obj.TISDGE, Prims.N); S57prims.put(Obj.VEHTRF, Prims.PA); S57prims.put(Obj.EXCNST, Prims.PA);
		S57prims.put(Obj.LG_SDM, Prims.A); S57prims.put(Obj.LG_VSP, Prims.A);
	}

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
	
	private static final HashMap<String, KeyVal<?>> OSMtags = new HashMap<String, KeyVal<?>>();
	static {
		OSMtags.put("natural=coastline", new KeyVal<>(Obj.COALNE, Att.UNKATT, null, null)); OSMtags.put("natural=water", new KeyVal<>(Obj.LAKARE, Att.UNKATT, null, null));
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
	}
	
	public static KeyVal<?> OSMtag(String key, String val) {
		KeyVal<?> kv = OSMtags.get(key + "=" + val);
		if (kv != null) {
			if (kv.conv == Conv.E) {
				ArrayList<Enum<?>> list = new ArrayList<Enum<?>>();
				list.add((Enum<?>)kv.val);
				return new KeyVal<>(kv.obj, kv.att, kv.conv, list);
			}
			return kv;
		}
		return new KeyVal<>(Obj.UNKOBJ, Att.UNKATT, null, null);
	}
	
	public static void OSMmap(BufferedReader in, S57map map) throws IOException {
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
				if (ln.contains("<bounds")) {
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
							map.addToEdge(ref);
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
	
	public static void OSMgeom(Feature feature, S57map map) {
		switch (S57prims.get(feature.type)) {
		case N:
		case P:
		case L:
		case A:
		case PA:
		case PL:
		case LA:
		case PLA:
		}
	}


}
