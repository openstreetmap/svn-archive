/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seamap;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import s57.S57att.Att;
import s57.S57obj.Obj;
import s57.S57val.ColCOL;
import s57.S57val.*;
import s57.S57val;
import seamap.SeaMap.*;
import symbols.Symbols;
import symbols.Symbols.*;

public class Renderer {
	
	static MapHelper helper;
	static SeaMap map;
	static double sScale;
	static double tScale;
	static Graphics2D g2;
	
	public static void reRender(Graphics2D g, int zoom, double factor, SeaMap m, MapHelper h) {
		g2 = g;
		helper = h;
		map = m;
		sScale = Symbols.symbolScale[zoom]*factor;
		tScale = Symbols.textScale[zoom]*factor;
		if (map != null) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			Rules.rules(map, zoom);
		}
	}
	
	public static AttMap getAtts(Feature feature, Obj obj, int idx) {
		HashMap<Integer, AttMap> objs = feature.objs.get(obj);
		if (objs == null) return null;
		else return objs.get(idx);
	}
	
	public static Object getAttVal(Feature feature, Obj obj, int idx, Att att) {
		AttMap atts = getAtts(feature, obj, idx);
		if (atts == null) return S57val.nullVal(att);
		else {
			AttItem item = atts.get(att);
			if (item == null) return S57val.nullVal(att);
			return item.val;
		}
	}

	public static double calcArea(Feature feature) {
	  if (feature.flag == Fflag.AREA) {
			ArrayList<Long> way = map.ways.get(feature.refs);
			Coord coord = map.nodes.get(way.get(0));
	    double llat = coord.lat;
	    double llon = coord.lon;
	    double area = 0.0;
			for (long node : way) {
				coord = map.nodes.get(node);
	      area += ((llon * coord.lat) - (llat * coord.lon));
	      llat = coord.lat;
	      llon = coord.lon;
	    }
	    return Math.abs(area) / 2.0 * 60.0 * 60.0;
	  }
	  return 0.0;
	}

	public static Coord findCentroid(Feature feature) {
		Coord coord;
		ArrayList<Long> way = map.ways.get(feature.refs);
		switch (feature.flag) {
		case NODE:
			return map.nodes.get(feature.refs);
		case WAY:
			coord = map.nodes.get(way.get(1));
			break;
		case AREA:
		default:
			coord = map.nodes.get(way.get(0));
		}
    double slat = 0.0;
    double slon = 0.0;
    double sarc = 0.0;
    double llat = coord.lat;
    double llon = coord.lon;
		for (long node : way) {
			coord = map.nodes.get(node);
      double lon = coord.lon;
      double lat = coord.lat;
      double arc = Math.sqrt(Math.pow((lon-llon), 2) + Math.pow((lat-llat), 2));
      slat += (lat * arc);
      slon += (lon * arc);
      sarc += arc;
      llat = lat;
      llon = lon;
		}
		return map.new Coord((sarc > 0.0 ? slat/sarc : 0.0), (sarc > 0.0 ? slon/sarc : 0.0));
	}
	
	public static void symbol(Feature feature, ArrayList<Instr> symbol, Obj obj) {
		Point2D point = helper.getPoint(findCentroid(feature));
		ArrayList<ColCOL> colours = (ArrayList<ColCOL>) getAttVal(feature, obj, 0, Att.COLOUR);
		ArrayList<ColPAT> pattern = (ArrayList<ColPAT>) getAttVal(feature, obj, 0, Att.COLPAT);
		Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), null, new Scheme(pattern, colours));
	}
	
}
