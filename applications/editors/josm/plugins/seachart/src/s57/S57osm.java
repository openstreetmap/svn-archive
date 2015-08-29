/* Copyright 2015 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package s57;

import java.util.ArrayList;
import java.util.HashMap;

import s57.S57obj.*;
import s57.S57att.*;
import s57.S57val.*;

public class S57osm {

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
		OSMtags.put("waterway=riverbank", new KeyVal<>(Obj.RIVBNK, Att.UNKATT, null, null)); OSMtags.put("waterway=river", new KeyVal<>(Obj.RIVERS, Att.UNKATT, null, null));
		OSMtags.put("waterway=canal", new KeyVal<>(Obj.CANALS, Att.UNKATT, null, null)); OSMtags.put("waterway=dock", new KeyVal<>(Obj.HRBBSN, Att.UNKATT, null, null));
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

}
