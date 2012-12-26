/* Copyright 2012 Malcolm Herring
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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.EnumMap;

import s57.S57val.*;

public class Symbols {

	public enum Prim {
		BBOX, STRK, COLR, FILL, LINE, RECT, RRCT, ELPS, EARC, PLIN, PGON, RSHP, TEXT, SYMB, P1, P2, H2, H3, H4, H5, V2, D2, D3, D4, B2, S2, S3, S4, C2, X2
	}

	public enum Handle {
		CC, TL, TR, TC, LC, RC, BL, BR, BC
	}

	public static final double symbolScale[] = { 256.0, 128.0, 64.0, 32.0, 16.0, 8.0, 4.0, 2.0, 1.0, 0.61, 0.372, 0.227, 0.138,
			0.0843, 0.0514, 0.0313, 0.0191, 0.0117, 0.007, 0.138 };

	public static final double textScale[] = { 256.0, 128.0, 64.0, 32.0, 16.0, 8.0, 4.0, 2.0, 1.0, 0.5556, 0.3086, 0.1714, 0.0953,
			0.0529, 0.0294, 0.0163, 0.0091, 0.0050, 0.0028, 0.0163 };

	private static final EnumMap<ColCOL, Color> bodyColours = new EnumMap<ColCOL, Color>(ColCOL.class);
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

	public static class Instr {
		Prim type;
		Object params;

		Instr(Prim itype, Object iparams) {
			type = itype;
			params = iparams;
		}
	}

	public static class Delta {
		Handle h;
		AffineTransform t;

		public Delta(Handle ih, AffineTransform it) {
			h = ih;
			t = it;
		}
	}

	public static class Scheme {
		ArrayList<ColPAT> pat;
		ArrayList<ColCOL> col;

		public Scheme(ArrayList<ColPAT> ipat, ArrayList<ColCOL> icol) {
			pat = ipat;
			col = icol;
		}
	}

	public static class Symbol {
		ArrayList<Instr> instr;
		double scale;
		double x;
		double y;
		Delta delta;
		Scheme scheme;

		public Symbol(ArrayList<Instr> iinstr, double iscale, double ix, double iy, Delta idelta, Scheme ischeme) {
			instr = iinstr;
			scale = iscale;
			x = ix;
			y = iy;
			delta = idelta;
			scheme = ischeme;
		}
	}

	public static void drawSymbol(Graphics2D g2, ArrayList<Instr> symbol, double scale, double x, double y, Delta dd, Scheme cs) {
		int pn = 0;
		int cn = 0;
		if (cs != null) {
			pn = cs.pat.size();
			cn = cs.col.size() - ((pn != 0) ? pn - 1 : 0);
		}
		AffineTransform savetr = g2.getTransform();
		g2.translate(x, y);
		g2.scale(scale, scale);
		for (Instr item : symbol) {
			switch (item.type) {
			case BBOX:
				Rectangle bbox = (Rectangle) item.params;
				double dx = 0.0;
				double dy = 0.0;
				if (dd != null) {
					switch (dd.h) {
					case CC:
						dx = bbox.x + (bbox.width / 2.0);
						dy = bbox.y - (bbox.height / 2.0);
						break;
					case TL:
						dx = bbox.x;
						dy = bbox.y;
						break;
					case TR:
						dx = bbox.x + bbox.width;
						dy = bbox.y;
						break;
					case TC:
						dx = bbox.x + (bbox.width / 2.0);
						dy = bbox.y;
						break;
					case LC:
						dx = bbox.x;
						dy = bbox.y - (bbox.height / 2.0);
						break;
					case RC:
						dx = bbox.x + bbox.width;
						dy = bbox.y - (bbox.height / 2.0);
						break;
					case BL:
						dx = bbox.x;
						dy = bbox.y - bbox.height;
						break;
					case BR:
						dx = bbox.x + bbox.width;
						dy = bbox.y - bbox.height;
						break;
					case BC:
						dx = bbox.x + (bbox.width / 2.0);
						dy = bbox.y - bbox.height;
						break;
					}
					g2.translate(dx, dy);
					g2.transform(dd.t);
				}
				break;
			case COLR:
				if ((cs != null) && (cs.col != null)) {
					for (Instr patch : (ArrayList<Instr>) item.params) {
						switch (patch.type) {
						case P1:
							if (cn > 0) {
								g2.setPaint(bodyColours.get(cs.col.get(0)));
								g2.fill((Path2D.Double) patch.params);
							}
							break;
						case P2:
							if (cn > 0) {
								if (cn > 1) {
									g2.setPaint(bodyColours.get(cs.col.get(1)));
								} else {
									g2.setPaint(bodyColours.get(cs.col.get(0)));
								}
								g2.fill((Path2D.Double) patch.params);
							}
							break;
						case H2:
							if ((cn > 1) && (cs.pat.get(0) == ColPAT.PAT_HORI)) {
								g2.setPaint(bodyColours.get(cs.col.get(cs.col.size() - pn)));
								g2.fill((Path2D.Double) patch.params);
							}
							break;
						case H3:
							if ((cn == 3) && (cs.pat.get(0) == ColPAT.PAT_HORI)) {
								g2.setPaint(bodyColours.get(cs.col.get(1)));
								g2.fill((Path2D.Double) patch.params);
							}
							break;
						case H4:
							if ((cn == 4) && (cs.pat.get(0) == ColPAT.PAT_HORI)) {
								g2.setPaint(bodyColours.get(cs.col.get(1)));
								g2.fill((Path2D.Double) patch.params);
							}
							break;
						case H5:
							if ((cn == 4) && (cs.pat.get(0) == ColPAT.PAT_HORI)) {
								g2.setPaint(bodyColours.get(cs.col.get(2)));
								g2.fill((Path2D.Double) patch.params);
							}
							break;
						case V2:
							if ((cn > 1) && (cs.pat.get(0) == ColPAT.PAT_VERT)) {
								g2.setPaint(bodyColours.get(cs.col.get(cs.col.size() - pn)));
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
				Symbol s = (Symbol) item.params;
				drawSymbol(g2, s.instr, s.scale, s.x, s.y, s.delta, s.scheme);
				break;
			case TEXT:
				break;
			}
		}
		g2.setTransform(savetr);
	}
}
