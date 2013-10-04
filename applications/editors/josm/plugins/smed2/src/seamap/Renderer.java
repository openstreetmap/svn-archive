/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seamap;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;

import s57.S57att.*;
import s57.S57obj.*;
import s57.S57val.*;
import s57.S57val;
import seamap.SeaMap;
import seamap.SeaMap.*;
import seamap.SeaMap.Area;
import symbols.Symbols;
import symbols.Symbols.*;

public class Renderer {

	public static final EnumMap<ColCOL, Color> bodyColours = new EnumMap<ColCOL, Color>(ColCOL.class);
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

	public static final EnumMap<ColPAT, Patt> pattMap = new EnumMap<ColPAT, Patt>(ColPAT.class);
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

	public static final double symbolScale[] = { 256.0, 128.0, 64.0, 32.0, 16.0, 8.0, 4.0, 2.0, 1.0, 0.61, 0.372, 0.227, 0.138, 0.0843, 0.0514, 0.0313, 0.0191, 0.0117, 0.007, 0.138 };

	public static final double textScale[] = { 256.0, 128.0, 64.0, 32.0, 16.0, 8.0, 4.0, 2.0, 1.0, 0.5556, 0.3086, 0.1714, 0.0953, 0.0529, 0.0294, 0.0163, 0.0091, 0.0050, 0.0028, 0.0163 };

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
		sScale = symbolScale[zoom] * factor;
		tScale = textScale[zoom] * factor;
		if (map != null) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			Rules.rules(map, zoom);
		}
	}

	public static AttMap getAtts(Feature feature, Obj obj, int idx) {
		HashMap<Integer, AttMap> objs = feature.objs.get(obj);
		if (objs == null)
			return null;
		else
			return objs.get(idx);
	}

	public static Object getAttVal(Feature feature, Obj obj, int idx, Att att) {
		AttMap atts = getAtts(feature, obj, idx);
		if (atts == null)
			return S57val.nullVal(att);
		else {
			AttItem item = atts.get(att);
			if (item == null)
				return S57val.nullVal(att);
			return item.val;
		}
	}

	public static void symbol(Feature feature, Symbol symbol, Obj obj, Delta delta, Scheme scheme) {
		Point2D point = helper.getPoint(feature.centre);
		if (obj == null) {
			Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), delta, scheme);
		} else {
			ArrayList<Color> colours = new ArrayList<Color>();
			for (ColCOL col : (ArrayList<ColCOL>)getAttVal(feature, obj, 0, Att.COLOUR)) {
				colours.add(bodyColours.get(col));
			}
			ArrayList<Patt> patterns = new ArrayList<Patt>();
			for(ColPAT pat: (ArrayList<ColPAT>) getAttVal(feature, obj, 0, Att.COLPAT)) {
				patterns.add(pattMap.get(pat));
			}
			Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), delta, new Scheme(patterns, colours));
		}
	}

	private static Rectangle symbolSize(Symbol symbol) {
		Symbol ssymb = symbol;
		while (ssymb != null) {
			for (Instr item : symbol) {
				if (item.type == Prim.BBOX) {
					return (Rectangle) item.params;
				}
				if (item.type == Prim.SYMB) {
					ssymb = ((SubSymbol) item.params).instr;
					break;
				}
			}
			if (ssymb == symbol)
				break;
		}
		return null;
	}

	private double mile(Feature feature) {
		return Math.pow(2, zoom) * 256 / (21600 * Math.abs(Math.cos(feature.centre.lat)));
	}
	
	public static void lineSymbols(Feature feature, Symbol prisymb, double space, Symbol secsymb, int ratio) {
		Area area;
		switch (feature.flag) {
		case LINE:
			Edge edge = map.edges.get(feature.refs);
			area = map.new Area();
			area.add(map.new Bound(map.new Side(edge, true), true));
			break;
		case AREA:
			area = map.areas.get(feature.refs);
			break;
		default:
			return;
		}
		Rectangle prect = symbolSize(prisymb);
		Rectangle srect = symbolSize(secsymb);
		if (srect == null)
			ratio = 0;
		if (prect != null) {
			double psize = Math.abs(prect.getY()) * sScale;
			double ssize = (srect != null) ? Math.abs(srect.getY()) * sScale : 0;
			Point2D prev = new Point2D.Double();
			Point2D next = new Point2D.Double();
			Point2D curr = new Point2D.Double();
			Point2D succ = new Point2D.Double();
			boolean gap = true;
			boolean piv = false;
			double len = 0;
			double angle = 0;
			int scount = ratio;
			Symbol symbol = prisymb;
			for (Bound bound : area) {
				BoundIterator bit = map.new BoundIterator(bound);
				boolean first = true;
				while (bit.hasNext()) {
					prev = next;
					next = helper.getPoint(bit.next());
					angle = Math.atan2(next.getY() - prev.getY(), next.getX() - prev.getX());
					piv = true;
					if (first) {
						curr = succ = next;
						gap = (space > 0);
						scount = ratio;
						symbol = prisymb;
						len = gap ? psize * space * 0.5 : psize;
						first = false;
					} else {
						while (curr.distance(next) >= len) {
							if (piv) {
								double rem = len;
								double s = prev.distance(next);
								double p = curr.distance(prev);
								if ((s > 0) && (p > 0)) {
									double n = curr.distance(next);
									double theta = Math.acos((s * s + p * p - n * n) / 2 / s / p);
									double phi = Math.asin(p / len * Math.sin(theta));
									rem = len * Math.sin(Math.PI - theta - phi) / Math.sin(theta);
								}
								succ = new Point2D.Double(prev.getX() + (rem * Math.cos(angle)), prev.getY() + (rem * Math.sin(angle)));
								piv = false;
							} else {
								succ = new Point2D.Double(curr.getX() + (len * Math.cos(angle)), curr.getY() + (len * Math.sin(angle)));
							}
							if (!gap) {
								Symbols.drawSymbol(g2, symbol, sScale, curr.getX(), curr.getY(), new Delta(Handle.BC, AffineTransform.getRotateInstance(Math.atan2((succ.getY() - curr.getY()), (succ.getX() - curr.getX())) + Math.toRadians(90))), null);
							}
							if (space > 0)
								gap = !gap;
							curr = succ;
							len = gap ? (psize * space) : (--scount == 0) ? ssize : psize;
							if (scount == 0) {
								symbol = secsymb;
								scount = ratio;
							} else {
								symbol = prisymb;
							}
						}
					}
				}
			}
		}
	}

	public static void lineVector(Feature feature, LineStyle style) {
		Path2D.Double p = new Path2D.Double();
		p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		Point2D point;
		switch (feature.flag) {
		case LINE:
			EdgeIterator eit = map.new EdgeIterator(map.edges.get(feature.refs), true);
			point = helper.getPoint(eit.next());
			p.moveTo(point.getX(), point.getY());
			while (eit.hasNext()) {
				point = helper.getPoint(eit.next());
				p.lineTo(point.getX(), point.getY());
			}
			break;
		case AREA:
			for (Bound bound : map.areas.get(feature.refs)) {
				BoundIterator bit = map.new BoundIterator(bound);
				point = helper.getPoint(bit.next());
				p.moveTo(point.getX(), point.getY());
				while (bit.hasNext()) {
					point = helper.getPoint(bit.next());
					p.lineTo(point.getX(), point.getY());
				}
			}
			break;
		}
		if (style.line != null) {
			if (style.dash != null) {
				float[] dash = new float[style.dash.length];
				System.arraycopy(style.dash, 0, dash, 0, style.dash.length);
				for (int i = 0; i < style.dash.length; i++) {
					dash[i] *= (float) sScale;
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

	public static void labelText(Feature feature, String str, Font font, Color colour, Delta delta) {
		Symbol label = new Symbol();
		label.add(new Instr(Prim.TEXT, new Caption(str, font, colour, (delta == null) ? new Delta(Handle.CC, null) : delta)));
		Point2D point = helper.getPoint(feature.centre);
		Symbols.drawSymbol(g2, label, tScale, point.getX(), point.getY(), null, null);
	}

	public static void lineText(Feature feature, String str, Font font, Color colour, double offset, double dy) {
		Area area;
		switch (feature.flag) {
		case LINE:
			Edge edge = map.edges.get(feature.refs);
			area = map.new Area();
			area.add(map.new Bound(map.new Side(edge, true), true));
			break;
		case AREA:
			area = map.areas.get(feature.refs);
			break;
		default:
			return;
		}
//		Rectangle prect = symbolSize(prisymb);
		if (!str.isEmpty()) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    FontRenderContext frc = g2.getFontRenderContext();
	    GlyphVector gv = font.deriveFont((float)(font.getSize()*tScale)).createGlyphVector(frc, str);
//			double psize = Math.abs(prect.getX());
			Point2D prev = new Point2D.Double();
			Point2D next = new Point2D.Double();
			Point2D curr = new Point2D.Double();
			Point2D succ = new Point2D.Double();
			boolean gap = true;
			boolean piv = false;
			double len = 0;
			double angle = 0;
			for (Bound bound : area) {
				BoundIterator bit = map.new BoundIterator(bound);
				boolean first = true;
				while (bit.hasNext()) {
					prev = next;
					next = helper.getPoint(bit.next());
					angle = Math.atan2(next.getY() - prev.getY(), next.getX() - prev.getX());
					piv = true;
					if (first) {
						curr = succ = next;
//						gap = (space > 0);
//						len = gap ? psize * space * 0.5 : psize;
						first = false;
					} else {
						while (curr.distance(next) >= len) {
							if (piv) {
								double rem = len;
								double s = prev.distance(next);
								double p = curr.distance(prev);
								if ((s > 0) && (p > 0)) {
									double n = curr.distance(next);
									double theta = Math.acos((s * s + p * p - n * n) / 2 / s / p);
									double phi = Math.asin(p / len * Math.sin(theta));
									rem = len * Math.sin(Math.PI - theta - phi) / Math.sin(theta);
								}
								succ = new Point2D.Double(prev.getX() + (rem * Math.cos(angle)), prev.getY() + (rem * Math.sin(angle)));
								piv = false;
							} else {
								succ = new Point2D.Double(curr.getX() + (len * Math.cos(angle)), curr.getY() + (len * Math.sin(angle)));
							}
							if (!gap) {
//								Symbols.drawSymbol(g2, symbol, sScale, curr.getX(), curr.getY(), new Delta(Handle.BC, AffineTransform.getRotateInstance(Math.atan2((succ.getY() - curr.getY()), (succ.getX() - curr.getX())) + Math.toRadians(90))), null);
							}
							gap = false;
							curr = succ;
//							len = psize;
						}
					}
				}
			}
		}
	}
}
