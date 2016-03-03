/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package render;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

import s57.S57val.*;
import s57.S57map;
import s57.S57map.*;
import symbols.Areas;
import symbols.Symbols;
import symbols.Symbols.*;

public class Renderer {

	public static final double symbolScale[] = { 256.0, 128.0, 64.0, 32.0, 16.0, 8.0, 4.0, 2.0, 1.0, 0.61, 0.372, 0.227, 0.138, 0.0843, 0.0514, 0.0313, 0.0191, 0.0117, 0.007 };

	public enum LabelStyle { NONE, RRCT, RECT, ELPS, CIRC, VCLR, PCLR, HCLR }

	static ChartContext context;
	static S57map map;
	static double sScale;
	static Graphics2D g2;
	static int zoom;

	public static void reRender(Graphics2D g, Rectangle rect, int z, double factor, S57map m, ChartContext c) {
		g2 = g;
		zoom = z;
		context = c;
		map = m;
		sScale = symbolScale[zoom] * factor;
		if (map != null) {
			if (context.clip()) {
				Point2D tl = context.getPoint(map.new Snode(map.bounds.maxlat, map.bounds.minlon));
				Point2D br = context.getPoint(map.new Snode(map.bounds.minlat, map.bounds.maxlon));
				g2.clip(new Rectangle2D.Double(tl.getX(), tl.getY(), (br.getX() - tl.getX()), (br.getY() - tl.getY())));
			}
			g2.setBackground(context.background(map));
			g2.clearRect(rect.x, rect.y, rect.width, rect.height);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			g2.setStroke(new BasicStroke(0, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
			Rules.rules();
		}
	}

	public static void symbol(Symbol symbol) {
		Point2D point = context.getPoint(Rules.feature.geom.centre);
		Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), null, null);
	}
	public static void symbol(Symbol symbol, Scheme scheme) {
		Point2D point = context.getPoint(Rules.feature.geom.centre);
		Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), scheme, null);
	}
	public static void symbol(Symbol symbol, Delta delta) {
		Point2D point = context.getPoint(Rules.feature.geom.centre);
		Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), null, delta);
	}
	public static void symbol(Symbol symbol, Scheme scheme, Delta delta) {
		Point2D point = context.getPoint(Rules.feature.geom.centre);
		Symbols.drawSymbol(g2, symbol, sScale, point.getX(), point.getY(), scheme, delta);
	}
	
	public static void cluster(ArrayList<Symbol> symbols) {
		Rectangle2D.Double bbox = null;
		if (symbols.size() > 4) {
			for (Instr instr : symbols.get(0)) {
				if (instr.type == Form.BBOX) {
					bbox = (Rectangle2D.Double) instr.params;
					break;
				}
			}
			if (bbox == null) return;
		}
		switch (symbols.size()) {
		case 1:
			symbol(symbols.get(0), new Delta(Handle.CC, new AffineTransform()));
			break;
		case 2:
			symbol(symbols.get(0), new Delta(Handle.RC, new AffineTransform()));
			symbol(symbols.get(1), new Delta(Handle.LC, new AffineTransform()));
			break;
		case 3:
			symbol(symbols.get(0), new Delta(Handle.BC, new AffineTransform()));
			symbol(symbols.get(1), new Delta(Handle.TR, new AffineTransform()));
			symbol(symbols.get(2), new Delta(Handle.TL, new AffineTransform()));
			break;
		case 4:
			symbol(symbols.get(0), new Delta(Handle.BR, new AffineTransform()));
			symbol(symbols.get(1), new Delta(Handle.BL, new AffineTransform()));
			symbol(symbols.get(2), new Delta(Handle.TR, new AffineTransform()));
			symbol(symbols.get(3), new Delta(Handle.TL, new AffineTransform()));
			break;
		case 5:
			symbol(symbols.get(0), new Delta(Handle.BR, new AffineTransform()));
			symbol(symbols.get(1), new Delta(Handle.BL, new AffineTransform()));
			symbol(symbols.get(2), new Delta(Handle.TR, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(symbols.get(3), new Delta(Handle.TC, new AffineTransform()));
			symbol(symbols.get(4), new Delta(Handle.TL, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			break;
		case 6:
			symbol(symbols.get(0), new Delta(Handle.BR, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(symbols.get(1), new Delta(Handle.BC, new AffineTransform()));
			symbol(symbols.get(2), new Delta(Handle.BL, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			symbol(symbols.get(3), new Delta(Handle.TR, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(symbols.get(4), new Delta(Handle.TC, new AffineTransform()));
			symbol(symbols.get(5), new Delta(Handle.TL, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			break;
		case 7:
			symbol(symbols.get(0), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -bbox.height/2)));
			symbol(symbols.get(1), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(symbols.get(2), new Delta(Handle.CC, new AffineTransform()));
			symbol(symbols.get(3), new Delta(Handle.LC, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			symbol(symbols.get(4), new Delta(Handle.TR, AffineTransform.getTranslateInstance(-bbox.width/2, bbox.height/2)));
			symbol(symbols.get(5), new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, bbox.height/2)));
			symbol(symbols.get(6), new Delta(Handle.TL, AffineTransform.getTranslateInstance(bbox.width/2, bbox.height/2)));
			break;
		case 8:
			symbol(symbols.get(0), new Delta(Handle.BR, AffineTransform.getTranslateInstance(0, -bbox.height/2)));
			symbol(symbols.get(1), new Delta(Handle.BL, AffineTransform.getTranslateInstance(0, -bbox.height/2)));
			symbol(symbols.get(2), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(symbols.get(3), new Delta(Handle.CC, new AffineTransform()));
			symbol(symbols.get(4), new Delta(Handle.LC, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			symbol(symbols.get(5), new Delta(Handle.TR, AffineTransform.getTranslateInstance(-bbox.width/2, bbox.height/2)));
			symbol(symbols.get(6), new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, bbox.height/2)));
			symbol(symbols.get(7), new Delta(Handle.TL, AffineTransform.getTranslateInstance(bbox.width/2, bbox.height/2)));
			break;
		case 9:
			symbol(symbols.get(0), new Delta(Handle.BR, AffineTransform.getTranslateInstance(-bbox.width/2, -bbox.height/2)));
			symbol(symbols.get(1), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -bbox.height/2)));
			symbol(symbols.get(2), new Delta(Handle.BL, AffineTransform.getTranslateInstance(bbox.width/2, -bbox.height/2)));
			symbol(symbols.get(3), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-bbox.width/2, 0)));
			symbol(symbols.get(4), new Delta(Handle.CC, new AffineTransform()));
			symbol(symbols.get(5), new Delta(Handle.LC, AffineTransform.getTranslateInstance(bbox.width/2, 0)));
			symbol(symbols.get(6), new Delta(Handle.TR, AffineTransform.getTranslateInstance(-bbox.width/2, bbox.height/2)));
			symbol(symbols.get(7), new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, bbox.height/2)));
			symbol(symbols.get(8), new Delta(Handle.TL, AffineTransform.getTranslateInstance(bbox.width/2, bbox.height/2)));
			break;
		}
	}

	private static Rectangle2D.Double symbolSize(Symbol symbol) {
		Symbol ssymb = symbol;
		while (ssymb != null) {
			for (Instr item : symbol) {
				if (item.type == Form.BBOX) {
					return (Rectangle2D.Double) item.params;
				}
				if (item.type == Form.SYMB) {
					ssymb = ((SubSymbol) item.params).instr;
					break;
				}
			}
			if (ssymb == symbol)
				break;
		}
		return null;
	}

	public static void lineSymbols(Symbol prisymb, double space, Symbol secsymb, Symbol tersymb, int ratio, Color col) {
		if ((Rules.feature.geom.prim == Pflag.NOSP) || (Rules.feature.geom.prim == Pflag.POINT))
			return;
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
			GeomIterator git = map.new GeomIterator(Rules.feature.geom);
			while (git.hasComp()) {
				git.nextComp();
				boolean first = true;
				while (git.hasEdge()) {
					git.nextEdge();
					while (git.hasNode()) {
						Snode node = git.next();
						if (node == null) continue;
						prev = next;
						next = context.getPoint(node);
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
									if (trect != null)
										stflag = !stflag;
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
	}

	public static void lineVector(LineStyle style) {
		Path2D.Double p = new Path2D.Double();
		p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		Point2D point;
		GeomIterator git = map.new GeomIterator(Rules.feature.geom);
		while (git.hasComp()) {
			git.nextComp();
			boolean first = true;
			while (git.hasEdge()) {
				git.nextEdge();
				point = context.getPoint(git.next());
				if (first) {
					p.moveTo(point.getX(), point.getY());
					first = false;
				} else {
					p.lineTo(point.getX(), point.getY());
				}
				while (git.hasNode()) {
					Snode node = git.next();
					if (node == null) continue;
					point = context.getPoint(node);
					p.lineTo(point.getX(), point.getY());
				}
			}
		}
		if ((style.fill != null) && (Rules.feature.geom.prim == Pflag.AREA)) {
			g2.setPaint(style.fill);
			g2.fill(p);
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
				g2.setStroke(new BasicStroke((float) (style.width * sScale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			}
			g2.setPaint(style.line);
			g2.draw(p);
		}
	}
	
	public static void lineCircle(LineStyle style, double radius, UniHLU units) {
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
		radius *= context.mile(Rules.feature);
		Symbol circle = new Symbol();
		if (style.fill != null) {
			circle.add(new Instr(Form.FILL, style.fill));
			circle.add(new Instr(Form.RSHP, new Ellipse2D.Double(-radius,-radius,radius*2,radius*2)));
		}
		circle.add(new Instr(Form.FILL, style.line));
		circle.add(new Instr(Form.STRK, new BasicStroke(style.width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, style.dash, 0)));
		circle.add(new Instr(Form.ELPS, new Ellipse2D.Double(-radius,-radius,radius*2,radius*2)));
		Point2D point = context.getPoint(Rules.feature.geom.centre);
		Symbols.drawSymbol(g2, circle, 1, point.getX(), point.getY(), null, null);
	}

	public static void fillPattern(BufferedImage image) {
		Path2D.Double p = new Path2D.Double();
		p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		Point2D point;
		switch (Rules.feature.geom.prim) {
		case POINT:
			point = context.getPoint(Rules.feature.geom.centre);
			g2.drawImage(image, new AffineTransformOp(AffineTransform.getScaleInstance(sScale, sScale), AffineTransformOp.TYPE_NEAREST_NEIGHBOR),
					(int)(point.getX() - (50 * sScale)), (int)(point.getY() - (50 * sScale)));
			break;
		case AREA:
			GeomIterator git = map.new GeomIterator(Rules.feature.geom);
			while (git.hasComp()) {
				git.nextComp();
				while (git.hasEdge()) {
					git.nextEdge();
					point = context.getPoint(git.next());
					p.moveTo(point.getX(), point.getY());
					while (git.hasNode()) {
						Snode node = git.next();
						if (node == null) continue;
						point = context.getPoint(node);
						p.lineTo(point.getX(), point.getY());
					}
				}
			}
	    g2.setPaint(new TexturePaint(image, new Rectangle(0, 0, 1 + (int)(100 * sScale), 1 + (int)(100 * sScale))));
	    g2.fill(p);
	    break;
		default:
			break;
		}
	}
	
	public static void labelText(String str, Font font, Color tc) {
		labelText(str, font, tc, LabelStyle.NONE, null, null, null);
	}
	public static void labelText(String str, Font font, Color tc, Delta delta) {
		labelText(str, font, tc, LabelStyle.NONE, null, null, delta);
	}
	public static void labelText(String str, Font font, Color tc, LabelStyle style, Color fg) {
		labelText(str, font, tc, style, fg, null, null);
	}
	public static void labelText(String str, Font font, Color tc, LabelStyle style, Color fg, Color bg) {
		labelText(str, font, tc, style, fg, bg, null);
	}
	public static void labelText(String str, Font font, Color tc, LabelStyle style, Color fg, Delta delta) {
		labelText(str, font, tc, style, fg, null, delta);
	}
	public static void labelText(String str, Font font, Color tc, LabelStyle style, Color fg, Color bg, Delta delta) {
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
			label.add(new Instr(Form.BBOX, new Rectangle2D.Double(lx,ly,width,height)));
			label.add(new Instr(Form.FILL, bg));
			label.add(new Instr(Form.RSHP, new RoundRectangle2D.Double(lx,ly,width,height,height,height)));
			label.add(new Instr(Form.FILL, fg));
			label.add(new Instr(Form.STRK, new BasicStroke(1 + (int)(height/10), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
			label.add(new Instr(Form.RRCT, new RoundRectangle2D.Double(lx,ly,width,height,height,height)));
			break;
		case VCLR:
			width += height * 1.0;
			height *= 2.0;
	    if (width < height) width = height;
	    lx = -width / 2;
	    ly = -height / 2;
	    tx = lx + (height * 0.27);
	    ty = ly + (height * 0.25);
			label.add(new Instr(Form.BBOX, new Rectangle2D.Double(lx,ly,width,height)));
			label.add(new Instr(Form.FILL, bg));
			label.add(new Instr(Form.RSHP, new RoundRectangle2D.Double(lx,ly,width,height,height,height)));
			label.add(new Instr(Form.FILL, fg));
			int sw = 1 + (int)(height/10);
			double po = sw / 2;
			label.add(new Instr(Form.STRK, new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
			Path2D.Double p = new Path2D.Double(); p.moveTo(-height*0.2,-ly-po); p.lineTo(height*0.2,-ly-po); p.moveTo(0,-ly-po); p.lineTo(0,-ly-po-(height*0.15));
			p.moveTo(-height*0.2,ly+po); p.lineTo((height*0.2),ly+po); p.moveTo(0,ly+po); p.lineTo(0,ly+po+(height*0.15));
			label.add(new Instr(Form.PLIN, p));
			break;
		case PCLR:
			width += height * 1.0;
			height *= 2.0;
	    if (width < height) width = height;
	    lx = -width / 2;
	    ly = -height / 2;
	    tx = lx + (height * 0.27);
	    ty = ly + (height * 0.25);
			label.add(new Instr(Form.BBOX, new Rectangle2D.Double(lx,ly,width,height)));
			label.add(new Instr(Form.FILL, bg));
			label.add(new Instr(Form.RSHP, new RoundRectangle2D.Double(lx,ly,width,height,height,height)));
			label.add(new Instr(Form.FILL, fg));
			sw = 1 + (int)(height/10);
			po = sw / 2;
			label.add(new Instr(Form.STRK, new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
			p = new Path2D.Double(); p.moveTo(-height*0.2,-ly-po); p.lineTo(height*0.2,-ly-po); p.moveTo(0,-ly-po); p.lineTo(0,-ly-po-(height*0.15));
			p.moveTo(-height*0.2,ly+po); p.lineTo((height*0.2),ly+po); p.moveTo(0,ly+po); p.lineTo(0,ly+po+(height*0.15));
			label.add(new Instr(Form.PLIN, p));
			label.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Areas.CableFlash, 1, 0, 0, null, new Delta(Handle.CC, new AffineTransform(0,-1,1,0,-width/2,0)))));
			label.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Areas.CableFlash, 1, 0, 0, null, new Delta(Handle.CC, new AffineTransform(0,-1,1,0,width/2,0)))));
			break;
		case HCLR:
			width += height * 1.5;
			height *= 1.5;
	    if (width < height) width = height;
	    lx = -width / 2;
	    ly = -height / 2;
	    tx = lx + (height * 0.5);
	    ty = ly + (height * 0.17);
			label.add(new Instr(Form.BBOX, new Rectangle2D.Double(lx,ly,width,height)));
			label.add(new Instr(Form.FILL, bg));
			label.add(new Instr(Form.RSHP, new RoundRectangle2D.Double(lx,ly,width,height,height,height)));
			label.add(new Instr(Form.FILL, fg));
			sw = 1 + (int)(height/10);
			double vo = height / 4;
			label.add(new Instr(Form.STRK, new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
			p = new Path2D.Double(); p.moveTo(-width*0.4-sw,-ly-vo); p.lineTo(-width*0.4-sw,ly+vo); p.moveTo(-width*0.4-sw,0); p.lineTo(-width*0.4+sw,0);
			p.moveTo(width*0.4+sw,-ly-vo); p.lineTo(width*0.4+sw,ly+vo); p.moveTo(width*0.4-sw,0); p.lineTo(width*0.4+sw,0);
			label.add(new Instr(Form.PLIN, p));
			break;
		default:
			lx = -width / 2;
			ly = -height / 2;
			tx = lx;
			ty = ly;
			label.add(new Instr(Form.BBOX, new Rectangle2D.Double(lx,ly,width,height)));
			break;
		}
		label.add(new Instr(Form.TEXT, new Caption(str, font, tc, new Delta(Handle.TL, AffineTransform.getTranslateInstance(tx, ty)))));
		Point2D point = context.getPoint(Rules.feature.geom.centre);
		Symbols.drawSymbol(g2, label, sScale, point.getX(), point.getY(), null, delta);
	}

	public static void lineText(String str, Font font, Color colour, double offset, double dy) {
		if (!str.isEmpty()) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setPaint(colour);
	    FontRenderContext frc = g2.getFontRenderContext();
	    GlyphVector gv = font.deriveFont(font.getSize2D() * (float)sScale).createGlyphVector(frc, (" " + str));
	    GeneralPath path = new GeneralPath();
			Point2D prev = new Point2D.Double();
			Point2D next = new Point2D.Double();
			Point2D curr = new Point2D.Double();
			Point2D succ = new Point2D.Double();
			boolean piv = false;
			double angle = 0;
			int index = 0;
			double gwidth = offset * (Rules.feature.geom.length * context.mile(Rules.feature) - gv.getLogicalBounds().getWidth()) + gv.getGlyphMetrics(0).getAdvance();
			GeomIterator git = map.new GeomIterator(Rules.feature.geom);
			while (git.hasComp()) {
				git.nextComp();
				boolean first = true;
				while (git.hasEdge()) {
					git.nextEdge();
					while (git.hasNode()) {
						Snode node = git.next();
						if (node == null) continue;
						prev = next;
						next = context.getPoint(node);
						angle = Math.atan2(next.getY() - prev.getY(), next.getX() - prev.getX());
						piv = true;
						if (first) {
							curr = succ = next;
							first = false;
						} else {
							while (curr.distance(next) >= gwidth) {
								if (piv) {
									double rem = gwidth;
									double s = prev.distance(next);
									double p = curr.distance(prev);
									if ((s > 0) && (p > 0)) {
										double n = curr.distance(next);
										double theta = Math.acos((s * s + p * p - n * n) / 2 / s / p);
										double phi = Math.asin(p / gwidth * Math.sin(theta));
										rem = gwidth * Math.sin(Math.PI - theta - phi) / Math.sin(theta);
									}
									succ = new Point2D.Double(prev.getX() + (rem * Math.cos(angle)), prev.getY() + (rem * Math.sin(angle)));
									piv = false;
								} else {
									succ = new Point2D.Double(curr.getX() + (gwidth * Math.cos(angle)), curr.getY() + (gwidth * Math.sin(angle)));
								}
								Shape shape = gv.getGlyphOutline(index);
								Point2D point = gv.getGlyphPosition(index);
								AffineTransform at = AffineTransform.getTranslateInstance(curr.getX(), curr.getY());
								at.rotate(Math.atan2((succ.getY() - curr.getY()), (succ.getX() - curr.getX())));
								at.translate(-point.getX(), -point.getY() + (dy * sScale));
								path.append(at.createTransformedShape(shape), false);
								curr = succ;
								if (++index < gv.getNumGlyphs()) {
									gwidth = gv.getGlyphMetrics(index).getAdvance();
								} else {
									g2.fill(path);
									return;
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static void lightSector(Color col1, Color col2, double radius, double s1, double s2, Double dir, String str) {
		if ((zoom >= 16) && (radius > 0.2)) {
			radius /= (Math.pow(2, zoom-15));
		}
		double mid = (((s1 + s2)  / 2) + (s1 > s2 ? 180 : 0)) % 360;
		g2.setStroke(new BasicStroke((float) (3.0 * sScale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1, new float[] {20 * (float)sScale, 20 * (float)sScale}, 0));
		g2.setPaint(Color.black);
		Point2D.Double centre = (Point2D.Double) context.getPoint(Rules.feature.geom.centre);
		double radial = radius * context.mile(Rules.feature);
		if (dir != null) {
			g2.draw(new Line2D.Double(centre.x, centre.y, centre.x - radial * Math.sin(Math.toRadians(dir)), centre.y + radial * Math.cos(Math.toRadians(dir))));
		} else {
			if ((s1 != 0.0) || (s2 != 360.0)) {
				g2.draw(new Line2D.Double(centre.x, centre.y, centre.x - radial * Math.sin(Math.toRadians(s1)), centre.y + radial * Math.cos(Math.toRadians(s1))));
				g2.draw(new Line2D.Double(centre.x, centre.y, centre.x - radial * Math.sin(Math.toRadians(s2)), centre.y + radial * Math.cos(Math.toRadians(s2))));
			}
		}
		double arcWidth =  10.0 * sScale;
		g2.setStroke(new BasicStroke((float)arcWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1));
		g2.setPaint(col1);
		g2.draw(new Arc2D.Double(centre.x - radial, centre.y - radial, 2 * radial, 2 * radial, -(s1 + 90), ((s1 < s2) ? (s1 - s2) : (s1 - s2 - 360)), Arc2D.OPEN));
		if (col2 != null) {
			g2.setPaint(col2);
			g2.draw(new Arc2D.Double(centre.x - radial + arcWidth, centre.y - radial + arcWidth, 2 * (radial - arcWidth), 2 * (radial - arcWidth), -(s1 + 90), ((s1 < s2) ? (s1 - s2) : (s1 - s2 - 360)), Arc2D.OPEN));
		}
		if ((str != null) && (!str.isEmpty())) {
			FontRenderContext frc = g2.getFontRenderContext();
			Font font = new Font("Arial", Font.PLAIN, 40);
			GlyphVector gv = font.deriveFont(font.getSize2D() * (float)sScale).createGlyphVector(frc, str);
			double arc = (s2 > s1) ? (s2 - s1) : (s2 - s1 + 360);
			double awidth = (Math.toRadians(arc) * radial);
			boolean hand = ((mid > 270) || (mid < 90));
			double phi = Math.toRadians(mid);
			radial += 30 * sScale;
			AffineTransform at = AffineTransform.getTranslateInstance(-radial * Math.sin(phi) / sScale, radial * Math.cos(phi) / sScale);
			if (gv.getLogicalBounds().getWidth() < awidth) {
				at.rotate(Math.toRadians(mid + (hand ? 0 : 180)));
				Renderer.labelText(str, font, Color.black, new Delta(Handle.CC, at));
			} else if (gv.getLogicalBounds().getHeight() < awidth) {
				hand = (mid < 180);
				at.rotate(Math.toRadians(mid + (hand ? -90 : 90)));
				Renderer.labelText(str, font, Color.black, hand ? new Delta(Handle.RC, at) : new Delta(Handle.LC, at));
			}
			if (dir != null) {
				font = new Font("Arial", Font.PLAIN, 30);
				str = dir + "°";
				hand = (dir > 180);
				phi = Math.toRadians(dir + (hand ? -0.5 : 0.5));
				radial -= 70 * sScale;
				at = AffineTransform.getTranslateInstance(-radial * Math.sin(phi) / sScale, radial * Math.cos(phi) / sScale);
				at.rotate(Math.toRadians(dir + (hand ? 90 : -90)));
				Renderer.labelText(str, font, Color.black, hand ? new Delta(Handle.BR, at) : new Delta(Handle.BL, at));
			}
		}
	}
}
