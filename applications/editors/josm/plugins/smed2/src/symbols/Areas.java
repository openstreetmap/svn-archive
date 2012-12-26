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
import java.awt.geom.*;
import java.util.ArrayList;

import symbols.Symbols.*;

public class Areas {
	public static final ArrayList<Instr> Plane = new ArrayList<Instr>();
	static {
		Plane.add(new Instr(Prim.BBOX, new Rectangle(-60,-60,120,120)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(40,20); p.lineTo(50,10); p.lineTo(27.0,13.3); p.lineTo(23.7,6.8); p.lineTo(40.0,5.0); p.curveTo(55,4,55,-9,40,-10);
		p.quadTo(31,-11,30,-15); p.lineTo(-30,2); p.quadTo(-35,-12,-45,-15); p.quadTo(-56,-3,-50,15); p.lineTo(18.4,7.3); p.lineTo(21.7,14); p.lineTo(-20,20); p.closePath();
		Plane.add(new Instr(Prim.PGON, p));
	}
	public static final ArrayList<Instr> Cable = new ArrayList<Instr>();
	static {
		Cable.add(new Instr(Prim.BBOX, new Rectangle(-30,-60,60,60)));
		Cable.add(new Instr(Prim.STRK, new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Cable.add(new Instr(Prim.FILL, new Color(0xc480ff)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(0,0); p.curveTo(-13,-13,-13,-17,0,-30); p.curveTo(13,-43,13,-47,0,-60);
		Cable.add(new Instr(Prim.PLIN, p));
	}
	public static final ArrayList<Instr> LaneArrow = new ArrayList<Instr>();
	static {
		LaneArrow.add(new Instr(Prim.STRK, new BasicStroke(10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		LaneArrow.add(new Instr(Prim.FILL, new Color(0x80c480ff, true)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(15,0); p.lineTo(15,-195); p.lineTo(40,-195);
		p.lineTo(0,-240); p.lineTo(-40,-195); p.lineTo(-15,-195); p.lineTo(-15,0); p.closePath();
		LaneArrow.add(new Instr(Prim.PLIN, p));
	}
	public static final ArrayList<Instr> LineAnchor = new ArrayList<Instr>();
	static {
		LineAnchor.add(new Instr(Prim.FILL, new Color(0xc480ff)));
		LineAnchor.add(new Instr(Prim.SYMB, new Symbols.Symbol(Harbours.Anchor, 0.5, 0, 0, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(-90.0))), null)));
	}
	public static final ArrayList<Instr> LinePlane = new ArrayList<Instr>();
	static {
		LinePlane.add(new Instr(Prim.FILL, new Color(0xc480ff)));
		LinePlane.add(new Instr(Prim.SYMB, new Symbols.Symbol(Areas.Plane, 0.5, 0, 0, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(-90.0))), null)));
	}
	public static final ArrayList<Instr> MarineFarm = new ArrayList<Instr>();
	static {
		MarineFarm.add(new Instr(Prim.STRK, new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		MarineFarm.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-23,12); p.lineTo(-23,23); p.lineTo(23,23); p.lineTo(23,12); p.moveTo(-8,15); p.lineTo(-8,23); p.moveTo(8,15); p.lineTo(8,23);
		p.moveTo(-23,-12); p.lineTo(-23,-23); p.lineTo(23,-23); p.lineTo(23,-12); p.moveTo(-8,-15); p.lineTo(-8,-23); p.moveTo(8,-15); p.lineTo(8,-23);
		p.moveTo(-21,8); p.quadTo(-1,-14,21,0); p.quadTo(-1,14,-21,-8); p.moveTo(7,6); p.quadTo(2,0,7,-6);
		MarineFarm.add(new Instr(Prim.PLIN, p));
		MarineFarm.add(new Instr(Prim.RSHP, new Ellipse2D.Double(9,-2,4,4)));
	}
	public static final ArrayList<Instr> NoWake = new ArrayList<Instr>();
	static {
		NoWake.add(new Instr(Prim.STRK, new BasicStroke(12.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoWake.add(new Instr(Prim.FILL, new Color(0xa30075)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-60,20); p.curveTo(-28,20,-32,0,0,0); p.curveTo(32,0,28,20,60,20); p.moveTo(-60,0); p.curveTo(-28,0,-32,-20,0,-20); p.curveTo(32,-20,28,0,60,0);
		NoWake.add(new Instr(Prim.PLIN, p));
		NoWake.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoWake.add(new Instr(Prim.LINE, new Line2D.Double(-60,60,60,-60)));
		NoWake.add(new Instr(Prim.LINE, new Line2D.Double(-60,-60,60,60)));
	}
	public static final ArrayList<Instr> Pipeline = new ArrayList<Instr>();
	static {
		Pipeline.add(new Instr(Prim.BBOX, new Rectangle(-15,-60,30,60)));
		Pipeline.add(new Instr(Prim.STRK, new BasicStroke(8.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Pipeline.add(new Instr(Prim.FILL, new Color(0xc480ff)));
		Pipeline.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-50)));
		Pipeline.add(new Instr(Prim.RSHP, new Ellipse2D.Double(-10,-60,20,20)));
	}
	public static final ArrayList<Instr> Restricted = new ArrayList<Instr>();
	static {
		Restricted.add(new Instr(Prim.BBOX, new Rectangle(-15,-30,30,30)));
		Restricted.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Restricted.add(new Instr(Prim.FILL, new Color(0xc480ff)));
		Restricted.add(new Instr(Prim.LINE, new Line2D.Double(0,0,0,-30)));
		Restricted.add(new Instr(Prim.LINE, new Line2D.Double(0,-15,17,-15)));
	}
	public static final ArrayList<Instr> Rock = new ArrayList<Instr>();
	static {
		Rock.add(new Instr(Prim.FILL, new Color(0x80c0ff)));
		Rock.add(new Instr(Prim.RSHP, new Ellipse2D.Double(-30,-30,60,60)));
		Rock.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{5,5}, 0)));
		Rock.add(new Instr(Prim.FILL, Color.black));
		Rock.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-30,-30,60,60)));
		Rock.add(new Instr(Prim.STRK, new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Rock.add(new Instr(Prim.LINE, new Line2D.Double(-20,0,20,0)));
		Rock.add(new Instr(Prim.LINE, new Line2D.Double(0,-20,0,20)));
	}
	public static final ArrayList<Instr> RockA = new ArrayList<Instr>();
	static {
		RockA.add(new Instr(Prim.FILL, new Color(0x80c0ff)));
		RockA.add(new Instr(Prim.RSHP, new Ellipse2D.Double(-30,-30,60,60)));
		RockA.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{5,5}, 0)));
		RockA.add(new Instr(Prim.FILL, Color.black));
		RockA.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-30,-30,60,60)));
		RockA.add(new Instr(Prim.STRK, new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		RockA.add(new Instr(Prim.LINE, new Line2D.Double(-20,0,20,0)));
		RockA.add(new Instr(Prim.LINE, new Line2D.Double(0,-20,0,20)));
		RockA.add(new Instr(Prim.RSHP, new Ellipse2D.Double(-17,-17,8,8)));
		RockA.add(new Instr(Prim.RSHP, new Ellipse2D.Double(-17,9,8,8)));
		RockA.add(new Instr(Prim.RSHP, new Ellipse2D.Double(9,-17,8,8)));
		RockA.add(new Instr(Prim.RSHP, new Ellipse2D.Double(9,9,8,8)));
	}
	public static final ArrayList<Instr> RockC = new ArrayList<Instr>();
	static {
		RockC.add(new Instr(Prim.FILL, new Color(0x80c0ff)));
		RockC.add(new Instr(Prim.RSHP, new Ellipse2D.Double(-30,-30,60,60)));
		RockC.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{5,5}, 0)));
		RockC.add(new Instr(Prim.FILL, Color.black));
		RockC.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-30,-30,60,60)));
		RockC.add(new Instr(Prim.STRK, new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		RockC.add(new Instr(Prim.LINE, new Line2D.Double(-20,0,20,0)));
		RockC.add(new Instr(Prim.LINE, new Line2D.Double(-10,17.3,10,-17.3)));
		RockC.add(new Instr(Prim.LINE, new Line2D.Double(10,17.3,-10,-17.3)));
	}
	public static final ArrayList<Instr> Sandwaves = new ArrayList<Instr>();
	public static final ArrayList<Instr> Seaplane = new ArrayList<Instr>();
	static {
		Seaplane.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Seaplane.add(new Instr(Prim.FILL, new Color(0xa30075)));
		Seaplane.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-60,-60,120,120)));
		Seaplane.add(new Instr(Prim.SYMB, new Symbols.Symbol(Areas.Plane, 1.0, 0, 0, null, null)));
	}
	public static final ArrayList<Instr> WindFarm = new ArrayList<Instr>();
	public static final ArrayList<Instr> WreckD = new ArrayList<Instr>();
	public static final ArrayList<Instr> WreckND = new ArrayList<Instr>();
	public static final ArrayList<Instr> WreckS = new ArrayList<Instr>();
}
