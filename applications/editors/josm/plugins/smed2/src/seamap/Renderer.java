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
import java.awt.image.*;
import java.util.*;

import s57.S57val.*;
import seamap.SeaMap;
import seamap.SeaMap.*;
import seamap.SeaMap.Area;
import symbols.Areas;
import symbols.Harbours;
import symbols.Symbols;
import symbols.Symbols.*;

public class Renderer {

	public static final double symbolScale[] = { 256.0, 128.0, 64.0, 32.0, 16.0, 8.0, 4.0, 2.0, 1.0, 0.61, 0.372, 0.227, 0.138, 0.0843, 0.0514, 0.0313, 0.0191, 0.0117, 0.007, 0.138 };

	public static final Color Yland = new Color(0x50b0ff);
	public static final Color Mline = new Color(0xc480ff);
	public static final Color Msymb = new Color(0xa30075);
	
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
	
	public enum LabelStyle { NONE, RRCT, RECT, ELPS, CIRC, VCLR, PCLR, HCLR }

	static MapContext context;
	static SeaMap map;
	static double sScale;
	static Graphics2D g2;
	static int zoom;

	public static void reRender(Graphics2D g, int z, double factor, SeaMap m, MapContext c) {
		g2 = g;
		zoom = z;
		context = c;
		map = m;
		sScale = symbolScale[zoom] * factor;
		if (map != null) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			g2.setStroke(new BasicStroke(0, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
			Rules.rules(map, zoom);
		}
	}

	public static void symbol(Feature feature, Symbol symbol) {
		Point2D point = context.getPoint(feature.centre);
		Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), null, null);
	}
	public static void symbol(Feature feature, Symbol symbol, Scheme scheme) {
		Point2D point = context.getPoint(feature.centre);
		Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), scheme, null);
	}
	public static void symbol(Feature feature, Symbol symbol, Delta delta) {
		Point2D point = context.getPoint(feature.centre);
		Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), null, delta);
	}
	public static void symbol(Feature feature, Symbol symbol, Scheme scheme, Delta delta) {
		Point2D point = context.getPoint(feature.centre);
		Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), scheme, delta);
	}
	
	public static void cluster(Feature feature, ArrayList<Symbol> symbols) {
		Rectangle2D.Double bbox = null;
		if (symbols.size() > 4) {
			for (Instr instr : symbols.get(0)) {
				if (instr.type == Prim.BBOX) {
					bbox = (Rectangle2D.Double) instr.params;
					break;
				}
			}
			if (bbox == null) return;
		}
		switch (symbols.size()) {
		case 1:
			symbol(feature, symbols.get(0), new Delta(Handle.CC, new AffineTransform()));
			break;
		case 2:
			symbol(feature, symbols.get(0), new Delta(Handle.RC, new AffineTransform()));
			symbol(feature, symbols.get(1), new Delta(Handle.LC, new AffineTransform()));
			break;
		case 3:
			symbol(feature, symbols.get(0), new Delta(Handle.BC, new AffineTransform()));
			symbol(feature, symbols.get(1), new Delta(Handle.TR, new AffineTransform()));
			symbol(feature, symbols.get(2), new Delta(Handle.TL, new AffineTransform()));
			break;
		case 4:
			symbol(feature, symbols.get(0), new Delta(Handle.BR, new AffineTransform()));
			symbol(feature, symbols.get(1), new Delta(Handle.BL, new AffineTransform()));
			symbol(feature, symbols.get(2), new Delta(Handle.TR, new AffineTransform()));
			symbol(feature, symbols.get(3), new Delta(Handle.TL, new AffineTransform()));
			break;
		case 5:
			symbol(feature, symbols.get(0), new Delta(Handle.BR, new AffineTransform()));
			symbol(feature, symbols.get(1), new Delta(Handle.BL, new AffineTransform()));
			symbol(feature, symbols.get(2), new Delta(Handle.TR, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(feature, symbols.get(3), new Delta(Handle.TC, new AffineTransform()));
			symbol(feature, symbols.get(4), new Delta(Handle.TL, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			break;
		case 6:
			symbol(feature, symbols.get(0), new Delta(Handle.BR, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(feature, symbols.get(1), new Delta(Handle.BC, new AffineTransform()));
			symbol(feature, symbols.get(2), new Delta(Handle.BL, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			symbol(feature, symbols.get(3), new Delta(Handle.TR, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(feature, symbols.get(4), new Delta(Handle.TC, new AffineTransform()));
			symbol(feature, symbols.get(5), new Delta(Handle.TL, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			break;
		case 7:
			symbol(feature, symbols.get(0), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -bbox.height/2)));
			symbol(feature, symbols.get(1), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(feature, symbols.get(2), new Delta(Handle.CC, new AffineTransform()));
			symbol(feature, symbols.get(3), new Delta(Handle.LC, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			symbol(feature, symbols.get(4), new Delta(Handle.TR, AffineTransform.getTranslateInstance(-bbox.width/2, bbox.height/2)));
			symbol(feature, symbols.get(5), new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, bbox.height/2)));
			symbol(feature, symbols.get(6), new Delta(Handle.TL, AffineTransform.getTranslateInstance(bbox.width/2, bbox.height/2)));
			break;
		case 8:
			symbol(feature, symbols.get(0), new Delta(Handle.BR, AffineTransform.getTranslateInstance(0, -bbox.height/2)));
			symbol(feature, symbols.get(1), new Delta(Handle.BL, AffineTransform.getTranslateInstance(0, -bbox.height/2)));
			symbol(feature, symbols.get(2), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(feature, symbols.get(3), new Delta(Handle.CC, new AffineTransform()));
			symbol(feature, symbols.get(4), new Delta(Handle.LC, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			symbol(feature, symbols.get(5), new Delta(Handle.TR, AffineTransform.getTranslateInstance(-bbox.width/2, bbox.height/2)));
			symbol(feature, symbols.get(6), new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, bbox.height/2)));
			symbol(feature, symbols.get(7), new Delta(Handle.TL, AffineTransform.getTranslateInstance(bbox.width/2, bbox.height/2)));
			break;
		case 9:
			symbol(feature, symbols.get(0), new Delta(Handle.BR, AffineTransform.getTranslateInstance(-bbox.width/2, -bbox.height/2)));
			symbol(feature, symbols.get(1), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -bbox.height/2)));
			symbol(feature, symbols.get(2), new Delta(Handle.BL, AffineTransform.getTranslateInstance(bbox.width/2, -bbox.height/2)));
			symbol(feature, symbols.get(3), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(feature, symbols.get(4), new Delta(Handle.CC, new AffineTransform()));
			symbol(feature, symbols.get(5), new Delta(Handle.LC, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			symbol(feature, symbols.get(6), new Delta(Handle.TR, AffineTransform.getTranslateInstance(-bbox.width/2, bbox.height/2)));
			symbol(feature, symbols.get(7), new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, bbox.height/2)));
			symbol(feature, symbols.get(8), new Delta(Handle.TL, AffineTransform.getTranslateInstance(bbox.width/2, bbox.height/2)));
			break;
		}
	}

	private static Rectangle2D.Double symbolSize(Symbol symbol) {
		Symbol ssymb = symbol;
		while (ssymb != null) {
			for (Instr item : symbol) {
				if (item.type == Prim.BBOX) {
					return (Rectangle2D.Double) item.params;
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

	public static void lineSymbols(Feature feature, Symbol prisymb, double space, Symbol secsymb, Symbol tersymb, int ratio, Color col) {
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
		Rectangle2D.Double prect = symbolSize(prisymb);
		Rectangle2D.Double srect = symbolSize(secsymb);
		Rectangle2D.Double trect = symbolSize(tersymb);
		if (srect == null)
			ratio = 0;
		if (prect != null) {
			double psize = Math.abs(prect.getY()) * sScale;
			double ssize = (srect != null) ? Math.abs(srect.getY()) * sScale : 0;
			double tsize = (trect != null) ? Math.abs(srect.getY()) * sScale : 0;
			Point2D prev = new Point2D.Double();
			Point2D next = new Point2D.Double();
			Point2D curr = new Point2D.Double();
			Point2D succ = new Point2D.Double();
			boolean gap = true;
			boolean piv = false;
			double len = 0;
			double angle = 0;
			int stcount = ratio;
			boolean stflag = false;
			Symbol symbol = prisymb;
			for (Bound bound : area) {
				BoundIterator bit = map.new BoundIterator(bound);
				boolean first = true;
				while (bit.hasNext()) {
					prev = next;
					next = context.getPoint(bit.next());
					angle = Math.atan2(next.getY() - prev.getY(), next.getX() - prev.getX());
					piv = true;
					if (first) {
						curr = succ = next;
						gap = (space > 0);
						stcount = ratio - 1;
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
								Symbols.drawSymbol(g2, symbol, sScale, curr.getX(), curr.getY(), new Scheme(col),
										new Delta(Handle.BC, AffineTransform.getRotateInstance(Math.atan2((succ.getY() - curr.getY()), (succ.getX() - curr.getX())) + Math.toRadians(90))));
							}
							if (space > 0)
								gap = !gap;
							curr = succ;
							len = gap ? (psize * space) : (--stcount == 0) ? (stflag ? tsize : ssize) : psize;
							if (stcount == 0) {
								symbol = stflag ? tersymb : secsymb;
								if (trect != null) stflag = !stflag;
								stcount = ratio;
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
			point = context.getPoint(eit.next());
			p.moveTo(point.getX(), point.getY());
			while (eit.hasNext()) {
				point = context.getPoint(eit.next());
				p.lineTo(point.getX(), point.getY());
			}
			break;
		case AREA:
			for (Bound bound : map.areas.get(feature.refs)) {
				BoundIterator bit = map.new BoundIterator(bound);
				point = context.getPoint(bit.next());
				p.moveTo(point.getX(), point.getY());
				while (bit.hasNext()) {
					point = context.getPoint(bit.next());
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
	
	public static void lineCircle(Feature feature, LineStyle style, double radius, UniHLU units) {
		switch (units) {
		case HLU_FEET:
			radius /= 6076;
			break;
		case HLU_KMTR:
			radius /= 1.852;
			break;
		case HLU_HMTR:
			radius /= 18.52;
			break;
		case HLU_SMIL:
			radius /= 1.15078;
			break;
		case HLU_NMIL:
			break;
		default:
			radius /= 1852;
			break;
		}
		radius *= context.mile(feature);
		Symbol circle = new Symbol();
		if (style.fill != null) {
			circle.add(new Instr(Prim.FILL, style.fill));
			circle.add(new Instr(Prim.RSHP, new Ellipse2D.Double(-radius,-radius,radius*2,radius*2)));
		}
		circle.add(new Instr(Prim.FILL, style.line));
		circle.add(new Instr(Prim.STRK, new BasicStroke(style.width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, style.dash, 0)));
		circle.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-radius,-radius,radius*2,radius*2)));
		Point2D point = context.getPoint(feature.centre);
		Symbols.drawSymbol(g2, circle, 1, point.getX(), point.getY(), null, null);
	}

	
	public static void fillPattern(Feature feature, BufferedImage image) {
		Path2D.Double p = new Path2D.Double();
		p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		Point2D point;
		switch (feature.flag) {
		case POINT:
			point = context.getPoint(feature.centre);
			g2.drawImage(image, new AffineTransformOp(AffineTransform.getScaleInstance(sScale, sScale), AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
					(int)(point.getX() - (50 * sScale)), (int)(point.getY() - (50 * sScale)));
			break;
		case AREA:
			for (Bound bound : map.areas.get(feature.refs)) {
				BoundIterator bit = map.new BoundIterator(bound);
				point = context.getPoint(bit.next());
				p.moveTo(point.getX(), point.getY());
				while (bit.hasNext()) {
					point = context.getPoint(bit.next());
					p.lineTo(point.getX(), point.getY());
				}
			}
	    g2.setPaint(new TexturePaint(image, new Rectangle(0, 0, 1 + (int)(100 * sScale), 1 + (int)(100 * sScale))));
	    g2.fill(p);
	    break;
		}
	}

	public static void labelText(Feature feature, String str, Font font, LabelStyle style, Color fg) {
		labelText(feature, str, font, style, fg, null, null);
	}
	public static void labelText(Feature feature, String str, Font font, LabelStyle style, Color fg, Color bg) {
		labelText(feature, str, font, style, fg, bg, null);
	}
	public static void labelText(Feature feature, String str, Font font, LabelStyle style, Color fg, Delta delta) {
		labelText(feature, str, font, style, fg, null, delta);
	}
	public static void labelText(Feature feature, String str, Font font, LabelStyle style, Color fg, Color bg, Delta delta) {
		if (delta == null) delta = new Delta(Handle.CC);
		if (bg == null) bg = new Color(0x00000000, true);
		if ((str == null) || (str.isEmpty())) str = " ";
    FontRenderContext frc = g2.getFontRenderContext();
    GlyphVector gv = font.deriveFont((float)(font.getSize())).createGlyphVector(frc, str.equals(" ") ? "!" : str);
    Rectangle2D bounds = gv.getVisualBounds();
    double width = bounds.getWidth();
    double height = bounds.getHeight();
		Symbol label = new Symbol();
		double lx, ly, tx, ty;
		switch (style) {
		case RRCT:
			width += height * 1.0;
			height *= 1.5;
	    if (width < height) width = height;
	    lx = -width / 2;
	    ly = -height / 2;
	    tx = lx + (height * 0.34);
	    ty = ly + (height * 0.17);
			label.add(new Instr(Prim.BBOX, new Rectangle2D.Double(lx,ly,width,height)));
			label.add(new Instr(Prim.FILL, bg));
			label.add(new Instr(Prim.RSHP, new RoundRectangle2D.Double(lx,ly,width,height,height,height)));
			label.add(new Instr(Prim.FILL, fg));
			label.add(new Instr(Prim.STRK, new BasicStroke(1 + (int)(height/10), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
			label.add(new Instr(Prim.RRCT, new RoundRectangle2D.Double(lx,ly,width,height,height,height)));
			break;
		case VCLR:
			width += height * 1.0;
			height *= 2.0;
	    if (width < height) width = height;
	    lx = -width / 2;
	    ly = -height / 2;
	    tx = lx + (height * 0.27);
	    ty = ly + (height * 0.25);
			label.add(new Instr(Prim.BBOX, new Rectangle2D.Double(lx,ly,width,height)));
			label.add(new Instr(Prim.FILL, bg));
			label.add(new Instr(Prim.RSHP, new RoundRectangle2D.Double(lx,ly,width,height,height,height)));
			label.add(new Instr(Prim.FILL, fg));
			int sw = 1 + (int)(height/10);
			double po = sw / 2;
			label.add(new Instr(Prim.STRK, new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
			Path2D.Double p = new Path2D.Double(); p.moveTo(-height*0.2,-ly-po); p.lineTo(height*0.2,-ly-po); p.moveTo(0,-ly-po); p.lineTo(0,-ly-po-(height*0.15));
			p.moveTo(-height*0.2,ly+po); p.lineTo((height*0.2),ly+po); p.moveTo(0,ly+po); p.lineTo(0,ly+po+(height*0.15));
			label.add(new Instr(Prim.PLIN, p));
			break;
		case PCLR:
			width += height * 1.0;
			height *= 2.0;
	    if (width < height) width = height;
	    lx = -width / 2;
	    ly = -height / 2;
	    tx = lx + (height * 0.27);
	    ty = ly + (height * 0.25);
			label.add(new Instr(Prim.BBOX, new Rectangle2D.Double(lx,ly,width,height)));
			label.add(new Instr(Prim.FILL, bg));
			label.add(new Instr(Prim.RSHP, new RoundRectangle2D.Double(lx,ly,width,height,height,height)));
			label.add(new Instr(Prim.FILL, fg));
			sw = 1 + (int)(height/10);
			po = sw / 2;
			label.add(new Instr(Prim.STRK, new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
			p = new Path2D.Double(); p.moveTo(-height*0.2,-ly-po); p.lineTo(height*0.2,-ly-po); p.moveTo(0,-ly-po); p.lineTo(0,-ly-po-(height*0.15));
			p.moveTo(-height*0.2,ly+po); p.lineTo((height*0.2),ly+po); p.moveTo(0,ly+po); p.lineTo(0,ly+po+(height*0.15));
			label.add(new Instr(Prim.PLIN, p));
			label.add(new Instr(Prim.SYMB, new Symbols.SubSymbol(Areas.CableFlash, 1, 0, 0, null, new Delta(Handle.CC, new AffineTransform(0,-1,1,0,-width/2,0)))));
			label.add(new Instr(Prim.SYMB, new Symbols.SubSymbol(Areas.CableFlash, 1, 0, 0, null, new Delta(Handle.CC, new AffineTransform(0,-1,1,0,width/2,0)))));
			break;
		case HCLR:
			width += height * 1.5;
			height *= 1.5;
	    if (width < height) width = height;
	    lx = -width / 2;
	    ly = -height / 2;
	    tx = lx + (height * 0.5);
	    ty = ly + (height * 0.17);
			label.add(new Instr(Prim.BBOX, new Rectangle2D.Double(lx,ly,width,height)));
			label.add(new Instr(Prim.FILL, bg));
			label.add(new Instr(Prim.RSHP, new RoundRectangle2D.Double(lx,ly,width,height,height,height)));
			label.add(new Instr(Prim.FILL, fg));
			sw = 1 + (int)(height/10);
			double vo = height / 4;
			label.add(new Instr(Prim.STRK, new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
			p = new Path2D.Double(); p.moveTo(-width*0.4-sw,-ly-vo); p.lineTo(-width*0.4-sw,ly+vo); p.moveTo(-width*0.4-sw,0); p.lineTo(-width*0.4+sw,0);
			p.moveTo(width*0.4+sw,-ly-vo); p.lineTo(width*0.4+sw,ly+vo); p.moveTo(width*0.4-sw,0); p.lineTo(width*0.4+sw,0);
			label.add(new Instr(Prim.PLIN, p));
			break;
		default:
			lx = -width / 2;
			ly = -height / 2;
			tx = lx;
			ty = ly;
			label.add(new Instr(Prim.BBOX, new Rectangle2D.Double(lx,ly,width,height)));
			break;
		}
		label.add(new Instr(Prim.TEXT, new Caption(str, font, fg, new Delta(Handle.TL, AffineTransform.getTranslateInstance(tx, ty)))));
		Point2D point = context.getPoint(feature.centre);
		Symbols.drawSymbol(g2, label, sScale, point.getX(), point.getY(), null, delta);
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
	    GlyphVector gv = font.deriveFont((float)(font.getSize()*sScale)).createGlyphVector(frc, str);
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
					next = context.getPoint(bit.next());
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
