/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package symbols;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.util.ArrayList;

public class Symbols {

	public enum Prim {
		BBOX, STRK, COLR, FILL, LINE, RECT, RRCT, ELPS, EARC, PLIN, PGON, RSHP, TEXT, SYMB, P1, P2, H2, H3, H4, H5, V2, D2, D3, D4, B2, S2, S3, S4, C2, X2
	}

	public enum Patt {
		Z, H, V, D, B, S, C, X
	}

	public enum Handle {
		CC, TL, TR, TC, LC, RC, BL, BR, BC
	}

	public static class Instr {
		public Prim type;
		public Object params;

		public Instr(Prim itype, Object iparams) {
			type = itype;
			params = iparams;
		}
	}

	public static class Delta {
		public Handle h;
		public AffineTransform t;

		public Delta(Handle ih, AffineTransform it) {
			h = ih;
			t = it;
		}
	}

	public static class Scheme {
		public ArrayList<Patt> pat;
		public ArrayList<Color> col;

		public Scheme(ArrayList<Patt> ipat, ArrayList<Color> icol) {
			pat = ipat;
			col = icol;
		}
	}

	public static class Caption {
		public String string;
		public Font font;
		public Color colour;
		public Delta dd;

		public Caption(String istr, Font ifont, Color icolour, Delta idd) {
			string = istr;
			font = ifont;
			colour = icolour;
			dd = idd;
		}
	}

	public static class LineStyle {
		public Color line;
		public float width;
		public float[] dash;
		public Color fill;

		public LineStyle(Color iline, float iwidth, float[] idash, Color ifill) {
			line = iline;
			width = iwidth;
			dash = idash;
			fill = ifill;
		}
	}

	public static class Symbol extends ArrayList<Instr> {

		public Symbol() {
			super();
		}
	}
	
	public static class SubSymbol {
		public Symbol instr;
		public double scale;
		public double x;
		public double y;
		public Delta delta;
		public Scheme scheme;

		public SubSymbol(Symbol iinstr, double iscale, double ix, double iy, Delta idelta, Scheme ischeme) {
			instr = iinstr;
			scale = iscale;
			x = ix;
			y = iy;
			delta = idelta;
			scheme = ischeme;
		}
	}

	public static void drawSymbol(Graphics2D g2, Symbol symbol, double scale, double x, double y, Delta dd, Scheme cs) {
		int pn = 0;
		int cn = 0;
		if (cs != null) {
			pn = cs.pat.size();
			cn = cs.col.size() - ((pn != 0) ? pn - 1 : 0);
		}
		AffineTransform savetr = g2.getTransform();
		g2.translate(x, y);
		g2.scale(scale, scale);
		if (symbol != null) {
			for (Instr item : symbol) {
				switch (item.type) {
				case BBOX:
					Rectangle bbox = (Rectangle) item.params;
					double dx = 0.0;
					double dy = 0.0;
					if (dd != null) {
						g2.transform(dd.t);
						switch (dd.h) {
						case CC:
							dx -= bbox.x + (bbox.width / 2.0);
							dy -= bbox.y + (bbox.height / 2.0);
							break;
						case TL:
							dx -= bbox.x;
							dy -= bbox.y;
							break;
						case TR:
							dx -= bbox.x + bbox.width;
							dy -= bbox.y;
							break;
						case TC:
							dx -= bbox.x + (bbox.width / 2.0);
							dy -= bbox.y;
							break;
						case LC:
							dx -= bbox.x;
							dy -= bbox.y + (bbox.height / 2.0);
							break;
						case RC:
							dx -= bbox.x + bbox.width;
							dy -= bbox.y + (bbox.height / 2.0);
							break;
						case BL:
							dx -= bbox.x;
							dy -= bbox.y + bbox.height;
							break;
						case BR:
							dx -= bbox.x + bbox.width;
							dy -= bbox.y + bbox.height;
							break;
						case BC:
							dx -= bbox.x + (bbox.width / 2.0);
							dy -= bbox.y + bbox.height;
							break;
						}
						g2.translate(dx, dy);
					}
					break;
				case COLR:
					if ((cs != null) && (cs.col != null)) {
						for (Instr patch : (Symbol) item.params) {
							switch (patch.type) {
							case P1:
								if (cn > 0) {
									g2.setPaint(cs.col.get(0));
									g2.fill((Path2D.Double) patch.params);
								}
								break;
							case P2:
								if (cn > 0) {
									if (cn > 1) {
										g2.setPaint(cs.col.get(1));
									} else {
										g2.setPaint(cs.col.get(0));
									}
									g2.fill((Path2D.Double) patch.params);
								}
								break;
							case H2:
								if ((cn > 1) && (cs.pat.get(0) == Patt.H)) {
									g2.setPaint(cs.col.get(cs.col.size() - pn));
									g2.fill((Path2D.Double) patch.params);
								}
								break;
							case H3:
								if ((cn == 3) && (cs.pat.get(0) == Patt.H)) {
									g2.setPaint(cs.col.get(1));
									g2.fill((Path2D.Double) patch.params);
								}
								break;
							case H4:
								if ((cn == 4) && (cs.pat.get(0) == Patt.H)) {
									g2.setPaint(cs.col.get(1));
									g2.fill((Path2D.Double) patch.params);
								}
								break;
							case H5:
								if ((cn == 4) && (cs.pat.get(0) == Patt.H)) {
									g2.setPaint(cs.col.get(2));
									g2.fill((Path2D.Double) patch.params);
								}
								break;
							case V2:
								if ((cn > 1) && (cs.pat.get(0) == Patt.V)) {
									g2.setPaint(cs.col.get(cs.col.size() - pn));
									g2.fill((Path2D.Double) patch.params);
								}
								break;
							}
						}
					}
					break;
				case STRK:
					g2.setStroke((BasicStroke) item.params);
					break;
				case FILL:
					g2.setPaint((Color) item.params);
					break;
				case LINE:
					g2.draw((Line2D.Double) item.params);
					break;
				case RECT:
					g2.draw((Rectangle2D.Double) item.params);
					break;
				case RRCT:
					g2.draw((RoundRectangle2D.Double) item.params);
					break;
				case ELPS:
					g2.draw((Ellipse2D.Double) item.params);
					break;
				case EARC:
					g2.draw((Arc2D.Double) item.params);
					break;
				case PLIN:
					g2.draw((Path2D.Double) item.params);
					break;
				case PGON:
					g2.fill((Path2D.Double) item.params);
					break;
				case RSHP:
					g2.fill((RectangularShape) item.params);
					break;
				case SYMB:
					SubSymbol s = (SubSymbol) item.params;
					drawSymbol(g2, s.instr, s.scale, s.x, s.y, s.delta, s.scheme);
					break;
				case TEXT:
					Caption c = (Caption) item.params;
					g2.setPaint(c.colour);
					TextLayout layout = new TextLayout(c.string, c.font, g2.getFontRenderContext());
					Rectangle2D bb = layout.getBounds();
					dx = 0;
					dy = 0;
					if (c.dd != null) {
						if (c.dd.t != null) g2.transform(c.dd.t);
						switch (c.dd.h) {
						case CC:
							dx -= bb.getX() + (bb.getWidth() / 2.0);
							dy -= bb.getY() + (bb.getHeight() / 2.0);
							break;
						case TL:
							dx -= bb.getX();
							dy -= bb.getY();
							break;
						case TR:
							dx -= bb.getX() + bb.getWidth();
							dy -= bb.getY();
							break;
						case TC:
							dx -= bb.getX() + (bb.getWidth() / 2.0);
							dy -= bb.getY();
							break;
						case LC:
							dx -= bb.getX();
							dy -= bb.getY() + (bb.getHeight() / 2.0);
							break;
						case RC:
							dx -= bb.getX() + bb.getWidth();
							dy -= bb.getY() + (bb.getHeight() / 2.0);
							break;
						case BL:
							dx -= bb.getX();
							dy -= bb.getY() + bb.getHeight();
							break;
						case BR:
							dx -= bb.getX() + bb.getWidth();
							dy -= bb.getY() + bb.getHeight();
							break;
						case BC:
							dx -= bb.getX() + (bb.getWidth() / 2.0);
							dy -= bb.getY() + bb.getHeight();
							break;
						}
					}
					layout.draw(g2, (float)dx, (float)dy);
					break;
				}
			}
		}
		g2.setTransform(savetr);
	}
}
