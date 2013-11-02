/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seamap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

import s57.S57val;
import s57.S57val.*;
import s57.S57att.*;
import s57.S57obj.*;
import seamap.SeaMap.*;
import symbols.Symbols.*;

public class Util {

	static final EnumMap<ColCOL, Color> bodyColours = new EnumMap<ColCOL, Color>(ColCOL.class);
	static {
		bodyColours.put(ColCOL.COL_UNK, new Color(0, true));
		bodyColours.put(ColCOL.COL_WHT, new Color(0xffffff));
		bodyColours.put(ColCOL.COL_BLK, new Color(0x000000));
		bodyColours.put(ColCOL.COL_RED, new Color(0xd40000));
		bodyColours.put(ColCOL.COL_GRN, new Color(0x00d400));
		bodyColours.put(ColCOL.COL_BLU, Color.blue);
		bodyColours.put(ColCOL.COL_YEL, new Color(0xffd400));
		bodyColours.put(ColCOL.COL_GRY, Color.gray);
		bodyColours.put(ColCOL.COL_BRN, new Color(0x8b4513));
		bodyColours.put(ColCOL.COL_AMB, new Color(0xfbf00f));
		bodyColours.put(ColCOL.COL_VIO, new Color(0xee82ee));
		bodyColours.put(ColCOL.COL_ORG, Color.orange);
		bodyColours.put(ColCOL.COL_MAG, new Color(0xf000f0));
		bodyColours.put(ColCOL.COL_PNK, Color.pink);
	}

	static final EnumMap<ColPAT, Patt> pattMap = new EnumMap<ColPAT, Patt>(ColPAT.class);
	static {
		pattMap.put(ColPAT.PAT_UNKN, Patt.Z);
		pattMap.put(ColPAT.PAT_HORI, Patt.H);
		pattMap.put(ColPAT.PAT_VERT, Patt.V);
		pattMap.put(ColPAT.PAT_DIAG, Patt.D);
		pattMap.put(ColPAT.PAT_BRDR, Patt.B);
		pattMap.put(ColPAT.PAT_SQUR, Patt.S);
		pattMap.put(ColPAT.PAT_CROS, Patt.C);
		pattMap.put(ColPAT.PAT_SALT, Patt.X);
		pattMap.put(ColPAT.PAT_STRP, Patt.H);
	}
	
	static String getName(Feature feature) {
		AttItem name = feature.atts.get(Att.OBJNAM);
		if (name == null) {
			name = feature.objs.get(feature.type).get(0).get(Att.OBJNAM);
		}
		if (name != null) return (String)name.val;
		return null;
	}

	static AttMap getAtts(Feature feature, Obj obj, int idx) {
		HashMap<Integer, AttMap> objs = feature.objs.get(obj);
		if (objs == null)
			return null;
		else
			return objs.get(idx);
	}

	public static Object getAttVal(Feature feature, Obj obj, int idx, Att att) {
		AttMap atts = null;
		HashMap<Integer, AttMap> objs = feature.objs.get(obj);
		if (objs != null)
			atts = objs.get(idx);
		if (atts == null)
			return S57val.nullVal(att);
		else {
			AttItem item = atts.get(att);
			if (item == null)
				return S57val.nullVal(att);
			return item.val;
		}
	}
	
	static Scheme getScheme(Feature feature, Obj obj) {
		ArrayList<Color> colours = new ArrayList<Color>();
		for (ColCOL col : (ArrayList<ColCOL>)getAttVal(feature, obj, 0, Att.COLOUR)) {
			colours.add(bodyColours.get(col));
		}
		ArrayList<Patt> patterns = new ArrayList<Patt>();
		for(ColPAT pat: (ArrayList<ColPAT>) getAttVal(feature, obj, 0, Att.COLPAT)) {
			patterns.add(pattMap.get(pat));
		}
		return new Scheme(patterns, colours);
	}
	
	static boolean hasObject(Feature feature, Obj obj) {
		return (feature.objs.containsKey(obj));
	}
	
	static boolean hasAttribute(Feature feature, Obj obj, Att att) {
		AttMap atts = getAtts(feature, obj, 0);
		return ((atts != null) && (atts.containsKey(att)));
	}
	
	static boolean testAttribute(Feature feature, Obj obj, Att att, Object val) {
		AttMap atts = getAtts(feature, obj, 0);
		if (atts != null) {
			AttItem item = atts.get(att);
			if (item != null) {
				switch (item.conv) {
				case S:
				case A:
					return ((String)item.val).equals(val);
				case L:
					return ((ArrayList<?>)item.val).contains(val);
				case E:
				case F:
				case I:
					return item.val.equals(val);
				}
			}
		}
		return false;
	}
	
}
