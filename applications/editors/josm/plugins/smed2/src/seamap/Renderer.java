/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seamap;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import s57.S57att.Att;
import s57.S57obj.Obj;
import s57.S57val.*;
import s57.S57val;
import seamap.SeaMap;
import seamap.SeaMap.*;
import symbols.Symbols;
import symbols.Symbols.*;

public class Renderer {
	
	static MapHelper helper;
	static SeaMap map;
	static double sScale;
	static double tScale;
	static Graphics2D g2;
	static int zoom;
	
	public static void reRender(Graphics2D g, int z, double factor, SeaMap m, MapHelper h) {
		g2 = g;
		zoom = z;
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
	    double llon = coord.lon;
	    double llat = coord.lat;
	    double sigma = 0.0;
			for (long node : way) {
				coord = map.nodes.get(node);
				double lat = coord.lat;
				double lon = coord.lon;
				sigma += (lon * Math.sin(llat)) - (llon * Math.sin(lat));
				llon = lon;
				llat = lat;
	    }
	    return Math.abs(sigma) * 3444 * 3444 / 2.0;
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
      double arc = (Math.acos(Math.cos(lon-llon) * Math.cos(lat-llat)));
      slat += (lat * arc);
      slon += (lon * arc);
      sarc += arc;
      llat = lat;
      llon = lon;
		}
		return map.new Coord((sarc > 0.0 ? slat/sarc : 0.0), (sarc > 0.0 ? slon/sarc : 0.0));
	}
	
	public static void symbol(Feature feature, Symbol symbol, Obj obj, Delta delta) {
		Point2D point = helper.getPoint(findCentroid(feature));
		if (obj == null) {
			Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), delta, null);
		} else {
			ArrayList<ColCOL> colours = (ArrayList<ColCOL>) getAttVal(feature, obj, 0, Att.COLOUR);
			ArrayList<ColPAT> pattern = (ArrayList<ColPAT>) getAttVal(feature, obj, 0, Att.COLPAT);
			Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), delta, new Scheme(pattern, colours));
		}
	}
	
	public static void lineSymbols(Feature feature, Symbol prisymb, double space, Symbol secsymb, int ratio) {
		if (feature.flag != Fflag.NODE) {
			ArrayList<Long> way = map.ways.get(feature.refs);
			for (long node : way) {
				Point2D point = helper.getPoint(map.nodes.get(node));
				
			}
		}
	}
	
	public static void lineVector (Feature feature, LineStyle style) {
		if (feature.flag != Fflag.NODE) {
			Long mpoly = map.outers.get(feature.refs);
			ArrayList<Long> ways = new ArrayList<Long>();
			if (mpoly != null) {
				ways.addAll(map.mpolys.get(mpoly));
			} else {
				ways.add(feature.refs);
			}
			Path2D.Double p = new Path2D.Double();
			p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
			for (long way : ways) {
				boolean first = true;
				for (long node : map.ways.get(way)) {
					Point2D point = helper.getPoint(map.nodes.get(node));
					if (first) {
						p.moveTo(point.getX(), point.getY());
						first = false;
					} else {
						p.lineTo(point.getX(), point.getY());
					}
				}
			}
			if (style.line != null) {
				if (style.dash != null) {
					float[] dash = new float[style.dash.length];
					System.arraycopy(style.dash, 0, dash, 0, style.dash.length);
					for (int i = 0; i < style.dash.length; i++) {
						dash[i] *= (float) (sScale);
					}
					g2.setStroke(new BasicStroke((float) (style.width * sScale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1, dash, 0));
				} else {
					g2.setStroke(new BasicStroke((float) (style.width * sScale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
				}
				g2.setPaint(style.line);
				g2.draw(p);
			}
			if (style.fill != null) {
				g2.setPaint(style.fill);
				g2.fill(p);
			}
		}
	}
	
	public static void labelText (Feature feature, String str, TextStyle style, Delta delta) {
		
	}
	
	public static void lineText (Feature feature, String str, TextStyle style, double offset, Delta delta) {
		
	}
}
