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
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;

import symbols.Symbols.Instr;
import symbols.Symbols.Prim;

public class Topmarks {

	public static final ArrayList<Instr> TopBoard = new ArrayList<Instr>();
	static {
		TopBoard.add(new Instr(Prim.BBOX, new Rectangle(-20,80,40,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(-19.0,-2.0); p.lineTo(-19.0,-39.0); p.lineTo(19.0,-39.0); p.lineTo(19.0,-2.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		TopBoard.add(new Instr(Prim.COLR, colours));
		TopBoard.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopBoard.add(new Instr(Prim.FILL, Color.black));
		p = new Path2D.Double(); p.moveTo(-19.0,-2.0); p.lineTo(-19.0,-39.0); p.lineTo(19.0,-39.0); p.lineTo(19.0,-2.0); p.closePath();
		TopBoard.add(new Instr(Prim.PLIN, p));
	}

	public static final ArrayList<Instr> TopCan = new ArrayList<Instr>();
	static {
		TopCan.add(new Instr(Prim.BBOX, new Rectangle(-20,80,40,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(-12.0,-15.0); p.lineTo(-12.0,-48.0); p.lineTo(12.0,-48.0); p.lineTo(12.0,-15.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		TopCan.add(new Instr(Prim.COLR, colours));
		TopCan.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopCan.add(new Instr(Prim.FILL, Color.black));
		TopCan.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-15)));
		p = new Path2D.Double(); p.moveTo(-12.0,-15.0); p.lineTo(-12.0,-48.0); p.lineTo(12.0,-48.0); p.lineTo(12.0,-15.0); p.closePath();
		TopCan.add(new Instr(Prim.PLIN, p));
	}

	public static final ArrayList<Instr> TopCone = new ArrayList<Instr>();
	static {
		TopCone.add(new Instr(Prim.BBOX, new Rectangle(-20,80,40,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(-15.0,-15.0); p.lineTo(0.0,-45.0); p.lineTo(15.0,-15.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		TopCone.add(new Instr(Prim.COLR, colours));
		TopCone.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopCone.add(new Instr(Prim.FILL, Color.black));
		TopCone.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-15)));
		p = new Path2D.Double(); p.moveTo(-15.0,-15.0); p.lineTo(0.0,-45.0); p.lineTo(15.0,-15.0); p.closePath();
		TopCone.add(new Instr(Prim.PLIN, p));
	}

	public static final ArrayList<Instr> TopCross = new ArrayList<Instr>();
	static {
		TopCross.add(new Instr(Prim.BBOX, new Rectangle(-30,80,60,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(-5.0,-15.0); p.lineTo(-5.0,-32.5); p.lineTo(-22.5,-32.5);	p.lineTo(-22.5,-42.5); p.lineTo(-5.0,-42.5);
		p.lineTo(-5.0,-60.0); p.lineTo(5.0,-60.0); p.lineTo(5.0,-42.5); p.lineTo(22.5,-42.5);	p.lineTo(22.5,-32.5); p.lineTo(5.0,-32.5); p.lineTo(5.0,-15.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		TopCross.add(new Instr(Prim.COLR, colours));
		TopCross.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopCross.add(new Instr(Prim.FILL, Color.black));
		TopCross.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-15)));
		TopCross.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		p = new Path2D.Double(); p.moveTo(-5.0,-15.0); p.lineTo(-5.0,-32.5); p.lineTo(-22.5,-32.5); p.lineTo(-22.5,-42.5); p.lineTo(-5.0,-42.5); p.lineTo(-5.0,-60.0);
		p.lineTo(5.0,-60.0); p.lineTo(5.0,-42.5); p.lineTo(22.5,-42.5); p.lineTo(22.5,-32.5); p.lineTo(5.0,-32.5); p.lineTo(5.0,-15.0); p.closePath();
		TopCross.add(new Instr(Prim.PLIN, p));
	}

	public static final ArrayList<Instr> TopEast = new ArrayList<Instr>();
	static {
		TopEast.add(new Instr(Prim.BBOX, new Rectangle(-30,80,60,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(0.0,-80.0); p.lineTo(-15.0,-47.0); p.lineTo(15.0,-47.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		p = new Path2D.Double(); p.moveTo(0.0,-10.0); p.lineTo(-15.0,-43.0); p.lineTo(15.0,-43.0); p.closePath();
		colours.add(new Instr(Prim.P2, p));
		TopEast.add(new Instr(Prim.COLR, colours));
		TopEast.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopEast.add(new Instr(Prim.FILL, Color.black));
		TopEast.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-10)));
		TopEast.add(new Instr(Prim.LINE, new Line2D.Double(0,-43,0,-47)));
		TopEast.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		p = new Path2D.Double(); p.moveTo(0.0,-10.0); p.lineTo(-15.0,-43.0); p.lineTo(15.0,-43.0); p.closePath();
		p.moveTo(0.0,-80.0); p.lineTo(-15.0,-47.0);  p.lineTo(15.0,-47.0); p.closePath();
		TopEast.add(new Instr(Prim.PLIN, p));
	}

	public static final ArrayList<Instr> TopIsol = new ArrayList<Instr>();
	static {
		TopIsol.add(new Instr(Prim.BBOX, new Rectangle(-30,80,60,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(-13.0,-55.0); p.curveTo(-13.0, -72.3, 13.0, -72.3, 13.0,-55.0); p.curveTo(13.0, -37.7, -13.0, -37.7, -13.0,-55.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		p = new Path2D.Double(); p.moveTo(-13.0,-28.0); p.curveTo(-13.0, -45.3, 13.0, -45.3, 13.0,-28.0); p.curveTo(13.0, -10.7, -13.0, -10.7, -13.0,-28.0); p.closePath();
		colours.add(new Instr(Prim.P2, p));
		TopIsol.add(new Instr(Prim.COLR, colours));
		TopIsol.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopIsol.add(new Instr(Prim.FILL, Color.black));
		TopIsol.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-15)));
		TopIsol.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopIsol.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-13,-41,26,26)));
		TopIsol.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-13,-68,26,26)));
	}

	public static final ArrayList<Instr> TopMooring = new ArrayList<Instr>();
	static {
		TopMooring.add(new Instr(Prim.BBOX, new Rectangle(-30,80,60,80)));
		TopMooring.add(new Instr(Prim.STRK, new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopMooring.add(new Instr(Prim.FILL, Color.black));
		TopMooring.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-1.5,-6,3,3)));
		TopMooring.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-8.5,-25,17,17)));
	}

	public static final ArrayList<Instr> TopNorth = new ArrayList<Instr>();
	static {
		TopNorth.add(new Instr(Prim.BBOX, new Rectangle(-30,80,60,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(0.0,-78.0); p.lineTo(-15.0,-45.0); p.lineTo(15.0,-45.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		p = new Path2D.Double(); p.moveTo(-15.0,-10.0); p.lineTo(0.0,-43.0); p.lineTo(15.0,-10.0); p.closePath();
		colours.add(new Instr(Prim.P2, p));
		TopNorth.add(new Instr(Prim.COLR, colours));
		TopNorth.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopNorth.add(new Instr(Prim.FILL, Color.black));
		TopNorth.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-10)));
		TopNorth.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		p = new Path2D.Double(); p.moveTo(-15.0,-10.0); p.lineTo(0.0,-43.0); p.lineTo(15.0,-10.0); p.closePath();
		p.moveTo(0.0,-78.0); p.lineTo(-15.0,-45.0);  p.lineTo(15.0,-45.0); p.closePath();
		TopNorth.add(new Instr(Prim.PLIN, p));
	}

	public static final ArrayList<Instr> TopSouth = new ArrayList<Instr>();
	static {
		TopSouth.add(new Instr(Prim.BBOX, new Rectangle(-30,80,60,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(-15.0,-78.0); p.lineTo(0.0,-45.0);  p.lineTo(15.0,-78.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		p = new Path2D.Double(); p.moveTo(0.0,-10.0); p.lineTo(-15.0,-43.0); p.lineTo(15.0,-43.0); p.closePath();
		colours.add(new Instr(Prim.P2, p));
		TopSouth.add(new Instr(Prim.COLR, colours));
		TopSouth.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopSouth.add(new Instr(Prim.FILL, Color.black));
		TopSouth.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-10)));
		TopSouth.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		p = new Path2D.Double(); p.moveTo(0.0,-10.0); p.lineTo(-15.0,-43.0); p.lineTo(15.0,-43.0); p.closePath();
		p.moveTo(-15.0,-78.0); p.lineTo(0.0,-45.0);  p.lineTo(15.0,-78.0); p.closePath();
		TopSouth.add(new Instr(Prim.PLIN, p));
	}

	public static final ArrayList<Instr> TopSphere = new ArrayList<Instr>();
	static {
		TopSphere.add(new Instr(Prim.BBOX, new Rectangle(-30,80,60,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(-14.0,-28.0); p.curveTo(-14.0,-46.7,14.0,-46.7,14.0,-28.0); p.curveTo(14.0,-9.3,-14.0,-9.3,-14.0,-28.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		TopSphere.add(new Instr(Prim.COLR, colours));
		TopSphere.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopSphere.add(new Instr(Prim.FILL, Color.black));
		TopSphere.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-15)));
		TopSphere.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopSphere.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-14,-42,28,28)));
	}

	public static final ArrayList<Instr> TopSquare = new ArrayList<Instr>();
	static {
		TopSquare.add(new Instr(Prim.BBOX, new Rectangle(-20,80,40,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(-13.0,-1.0); p.lineTo(-13.0,-27.0); p.lineTo(13.0,-27.0); p.lineTo(13.0,-1.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		TopSquare.add(new Instr(Prim.COLR, colours));
		TopSquare.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopSquare.add(new Instr(Prim.FILL, Color.black));
		p = new Path2D.Double(); p.moveTo(-13.0,-1.0); p.lineTo(-13.0,-27.0); p.lineTo(13.0,-27.0); p.lineTo(13.0,-1.0); p.closePath();
		TopSquare.add(new Instr(Prim.PLIN, p));
	}

	public static final ArrayList<Instr> TopTriangle = new ArrayList<Instr>();
	static {
		TopTriangle.add(new Instr(Prim.BBOX, new Rectangle(-20,80,40,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(-15.0,-1.0); p.lineTo(0.0,-29.0); p.lineTo(15.0,-1.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		TopTriangle.add(new Instr(Prim.COLR, colours));
		TopTriangle.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopTriangle.add(new Instr(Prim.FILL, Color.black));
		p = new Path2D.Double(); p.moveTo(-15.0,-1.0); p.lineTo(0.0,-29.0); p.lineTo(15.0,-1.0); p.closePath();
		TopTriangle.add(new Instr(Prim.PLIN, p));
	}

	public static final ArrayList<Instr> TopWest = new ArrayList<Instr>();
	static {
		TopWest.add(new Instr(Prim.BBOX, new Rectangle(-30,80,60,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(-15.0,-78.0); p.lineTo(0.0,-45.0);  p.lineTo(15.0,-78.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		p = new Path2D.Double(); p.moveTo(-15.0,-10.0); p.lineTo(0.0,-43.0); p.lineTo(15.0,-10.0); p.closePath();
		colours.add(new Instr(Prim.P2, p));
		TopWest.add(new Instr(Prim.COLR, colours));
		TopWest.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopWest.add(new Instr(Prim.FILL, Color.black));
		TopWest.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-10)));
		TopWest.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		p = new Path2D.Double(); p.moveTo(-15.0,-10.0); p.lineTo(0.0,-43.0); p.lineTo(15.0,-10.0); p.closePath();
		p.moveTo(-15.0,-78.0); p.lineTo(0.0,-45.0);  p.lineTo(15.0,-78.0); p.closePath();
		TopWest.add(new Instr(Prim.PLIN, p));
	}

	public static final ArrayList<Instr> TopX = new ArrayList<Instr>();
	static {
		TopX.add(new Instr(Prim.BBOX, new Rectangle(-30,80,60,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(0.0,-27.7); p.lineTo(-12.4,-15.7); p.lineTo(-19.3,-22.6); p.lineTo(-7.3,-35.0); p.lineTo(-19.3,-47.3);
		p.lineTo(-12.4,-54.2); p.lineTo(0.0,-42.4); p.lineTo(12.4,-54.2); p.lineTo(19.3,-47.3); p.lineTo(7.3,-35.0); p.lineTo(19.3,-22.6); p.lineTo(12.4,-15.7); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		TopX.add(new Instr(Prim.COLR, colours));
		TopX.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopX.add(new Instr(Prim.FILL, Color.black));
		TopX.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-27)));
		TopX.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		p = new Path2D.Double(); p.moveTo(0.0,-27.7); p.lineTo(-12.4,-15.7); p.lineTo(-19.3,-22.6); p.lineTo(-7.3,-35.0); p.lineTo(-19.3,-47.3); p.lineTo(-12.4,-54.2); p.lineTo(0.0,-42.4);
		p.lineTo(12.4,-54.2); p.lineTo(19.3,-47.3); p.lineTo(7.3,-35.0); p.lineTo(19.3,-22.6); p.lineTo(12.4,-15.7); p.closePath();
		TopX.add(new Instr(Prim.PLIN, p));
	}
}
