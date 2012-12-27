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

import symbols.Symbols.Delta;
import symbols.Symbols.Handle;
import symbols.Symbols.Instr;
import symbols.Symbols.Prim;

public class Notices {
	private static final ArrayList<Instr> Bollard = new ArrayList<Instr>();
	static {
		Bollard.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(20,21); p.lineTo(20,16.5); p.lineTo(11.6,16.5); p.quadTo(9.1,9.6,8.3,2.0); p.lineTo(-8.0,-0.3); p.quadTo(-8.6,9.0,-11.3,16.5);
		p.lineTo(-23.5,16.5); p.lineTo(-23.5,21.0); p.closePath(); p.moveTo(23.8,3.0); p.lineTo(-10.7,-1.8); p.curveTo(-13.1,-2.2,-12.8,-6.0,-10.2,-5.8); p.lineTo(23.8,-1.1);
		p.closePath(); p.moveTo(8.4,-4.3); p.curveTo(9.0,-9.3,9.0,-11.4,11.2,-13.0); p.curveTo(12.8,-15.0,12.8,-16.7,11.0,-18.6); p.curveTo(4.0,-22.2,-4.0,-22.2,-11.0,-18.6);
		p.curveTo(-12.8,-16.7,-12.8,-15.0,-11.2,-13.0); p.curveTo(-9.0,-11.3,-8.7,-9.5,-8.4,-6.5); p.closePath();
		Bollard.add(new Instr(Prim.PGON, p));
	}
	private static final ArrayList<Instr> Crossing = new ArrayList<Instr>();
	private static final ArrayList<Instr> CrossingL = new ArrayList<Instr>();
	private static final ArrayList<Instr> CrossingR = new ArrayList<Instr>();
	private static final ArrayList<Instr> Junction = new ArrayList<Instr>();
	private static final ArrayList<Instr> JunctionL = new ArrayList<Instr>();
	private static final ArrayList<Instr> JunctionR = new ArrayList<Instr>();
	private static final ArrayList<Instr> Motor = new ArrayList<Instr>();
	static {
		Motor.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-5.0,4.3); p.curveTo(-3.7,5.5,-1.8,5.7,-0.2,4.9); p.curveTo(1.3,8.7,4.6,10.9,8.4,10.9); p.curveTo(14.0,10.9,17.5,6.3,17.5,2.0);
		p.curveTo(17.5,-0.7,16.1,-3.2,14.5,-3.2); p.curveTo(12.5,-3.2,11.7,0.8,2.5,1.1); p.curveTo(2.5,-1.2,1.6,-2.2,0.6,-3.0); p.curveTo(3.2,-5.6,4.0,-12.6,-1.0,-16.1);
		p.curveTo(-5.3,-19.2,-11.6,-18.3,-13.7,-13.7); p.curveTo(-14.3,-12.2,-14.0,-11.2,-12.5,-10.6); p.curveTo(-8.6,-9.6,-5.3,-6.0,-4.0,-3.4); p.curveTo(-5.4,-2.6,-6.2,-2.0,-6.2,0.2);
		p.curveTo(-12.8,-1.0,-17.5,3.7,-17.5,9.3); p.curveTo(-17.5,14.7,-12.6,18.8,-8.0,17.6); p.curveTo(-7.0,17.2,-6.6,16.2,-7.2,14.6); p.curveTo(-7.7,12.4,-7.0,7.7,-5.0,4.3); p.closePath();
		Motor.add(new Instr(Prim.PGON, p));
	}
	private static final ArrayList<Instr> Proceed = new ArrayList<Instr>();
	private static final ArrayList<Instr> Rowboat = new ArrayList<Instr>();
	private static final ArrayList<Instr> Sailboard = new ArrayList<Instr>();
	private static final ArrayList<Instr> Sailboat = new ArrayList<Instr>();
	private static final ArrayList<Instr> Slipway = new ArrayList<Instr>();
	private static final ArrayList<Instr> Speedboat = new ArrayList<Instr>();
	private static final ArrayList<Instr> Sport = new ArrayList<Instr>();
	private static final ArrayList<Instr> Turn = new ArrayList<Instr>();
	static {
		Turn.add(new Instr(Prim.STRK, new BasicStroke(5.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Turn.add(new Instr(Prim.FILL, Color.black));
		Turn.add(new Instr(Prim.EARC, new Arc2D.Double(-9.0,-9.0,18.0,18.0,270.0,230.0,Arc2D.OPEN)));
		Turn.add(new Instr(Prim.EARC, new Arc2D.Double(-20.0,-20.0,40.0,40.0,315.0,-280.0,Arc2D.OPEN)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(21.8,-7.0); p.lineTo(18.8,-18.2); p.lineTo(10.5,-10.0); p.closePath();
		p.moveTo(-12.9,0.7); p.lineTo(-1.7,-2.3); p.lineTo(-9.9,-10.5); p.closePath();
		Turn.add(new Instr(Prim.PGON, p));
	}
	private static final ArrayList<Instr> VHF = new ArrayList<Instr>();
	private static final ArrayList<Instr> Waterbike = new ArrayList<Instr>();
	private static final ArrayList<Instr> Waterski = new ArrayList<Instr>();
	private static final ArrayList<Instr> NoticeA = new ArrayList<Instr>();
	static {
		NoticeA.add(new Instr(Prim.FILL, new Color(0xe80000)));
		NoticeA.add(new Instr(Prim.RSHP, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
		NoticeA.add(new Instr(Prim.FILL, Color.white));
		NoticeA.add(new Instr(Prim.RSHP, new Rectangle2D.Double(-21,-21,42,42)));
		NoticeA.add(new Instr(Prim.STRK, new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		NoticeA.add(new Instr(Prim.FILL, new Color(0xe80000)));
		NoticeA.add(new Instr(Prim.LINE, new Line2D.Double(-25,-25,25,25)));
		NoticeA.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeA.add(new Instr(Prim.FILL, Color.black));
		NoticeA.add(new Instr(Prim.RRCT, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
	}
	private static final ArrayList<Instr> NoticeB = new ArrayList<Instr>();
	static {
		NoticeB.add(new Instr(Prim.FILL, new Color(0xe80000)));
		NoticeB.add(new Instr(Prim.RSHP, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
		NoticeB.add(new Instr(Prim.FILL, Color.white));
		NoticeB.add(new Instr(Prim.RSHP, new Rectangle2D.Double(-21,-21,42,42)));
		NoticeB.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeB.add(new Instr(Prim.FILL, Color.black));
		NoticeB.add(new Instr(Prim.RRCT, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
	}
	private static final ArrayList<Instr> NoticeE = new ArrayList<Instr>();
	static {
		NoticeE.add(new Instr(Prim.FILL, new Color(0x0000a0)));
		NoticeE.add(new Instr(Prim.RSHP, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
		NoticeE.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE.add(new Instr(Prim.FILL, Color.black));
		NoticeE.add(new Instr(Prim.RRCT, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
	}

	public static final ArrayList<Instr> Notice = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeA1 = new ArrayList<Instr>();
	static {
		NoticeA1.add(new Instr(Prim.FILL, new Color(0xe80000)));
		NoticeA1.add(new Instr(Prim.RSHP, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
		NoticeA1.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeA1.add(new Instr(Prim.FILL, Color.white));
		NoticeA1.add(new Instr(Prim.RSHP, new Rectangle2D.Double(-30,-10,60,20)));
		NoticeA1.add(new Instr(Prim.FILL, Color.black));
		NoticeA1.add(new Instr(Prim.RRCT, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
	}
	public static final ArrayList<Instr> NoticeA1a = new ArrayList<Instr>();
	static {
		NoticeA1a.add(new Instr(Prim.FILL, new Color(0xe80000)));
		NoticeA1a.add(new Instr(Prim.RSHP, new Ellipse2D.Double(-30,-30,60,60)));
		NoticeA1a.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeA1a.add(new Instr(Prim.FILL, Color.white));
		NoticeA1a.add(new Instr(Prim.RSHP, new Rectangle2D.Double(-29,-10,58,20)));
		NoticeA1a.add(new Instr(Prim.FILL, Color.black));
		NoticeA1a.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-30,-30,60,60)));
	}
	public static final ArrayList<Instr> NoticeA2 = new ArrayList<Instr>();
	static {
		NoticeA2.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA2.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-10,23); p.lineTo(-10,0); p.lineTo(-6,0); p.lineTo(-12.5,-8); p.lineTo(-19,0); p.lineTo(-15,0); p.lineTo(-15,23);
		p.closePath(); p.moveTo(10,8); p.lineTo(10,-15); p.lineTo(6,-15); p.lineTo(12.5,-23); p.lineTo(19,-15); p.lineTo(15,-15); p.lineTo(15,8); p.closePath();
		NoticeA2.add(new Instr(Prim.PGON, p));
	}
	public static final ArrayList<Instr> NoticeA3 = new ArrayList<Instr>();
	static {
		NoticeA3.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA2, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-10,12); p.lineTo(-6,12); p.lineTo(-12.5,4); p.lineTo(-19,12);
		p.closePath(); p.moveTo(10,-3); p.lineTo(6,-3); p.lineTo(12.5,-11); p.lineTo(19,-3); p.closePath();
		NoticeA3.add(new Instr(Prim.PGON, p));
	}
	public static final ArrayList<Instr> NoticeA4 = new ArrayList<Instr>();
	static {
		NoticeA4.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA4.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-10,-15); p.lineTo(-10,8); p.lineTo(-6,8); p.lineTo(-12.5,16); p.lineTo(-19,8); p.lineTo(-15,8); p.lineTo(-15,-15);
		p.closePath(); p.moveTo(10,15); p.lineTo(10,-8); p.lineTo(6,-8); p.lineTo(12.5,-16); p.lineTo(19,-8); p.lineTo(15,-8); p.lineTo(15,15); p.closePath();
		NoticeA4.add(new Instr(Prim.PGON, p));
	}
	public static final ArrayList<Instr> NoticeA4_1 = new ArrayList<Instr>();
	static {
		NoticeA4_1.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA4, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-10,-4); p.lineTo(-6,-4); p.lineTo(-12.5,4); p.lineTo(-19,-4);
		p.closePath(); p.moveTo(10,5); p.lineTo(6,5); p.lineTo(12.5,-3); p.lineTo(19,5); p.closePath();
		NoticeA4_1.add(new Instr(Prim.PGON, p));
	}
	public static final ArrayList<Instr> NoticeA5 = new ArrayList<Instr>();
	static {
		NoticeA5.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA5.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD); p.moveTo(-5.3,14.6); p.lineTo(-5.3,4.0); p.lineTo(0.0,4.0); p.curveTo(4.2,4.0,7.4,3.5,9.4,0.0);
		p.curveTo(11.4,-2.8,11.4,-7.2,9.4,-10.5); p.curveTo(7.4,-13.6,4.2,-14.0,0.0,-14.0); p.lineTo(-11.0,-14.0); p.lineTo(-11.0,14.6); p.closePath();
		p.moveTo(-5.3,-1.0); p.lineTo(0.0,-1.0); p.curveTo(6.5,-1.0,6.5,-9.0,0.0,-9.0); p.lineTo(-5.3,-9.0); p.closePath();
		NoticeA5.add(new Instr(Prim.PGON, p));
	}
	public static final ArrayList<Instr> NoticeA5_1 = new ArrayList<Instr>();
	static {
		NoticeA5_1.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
	}
	public static final ArrayList<Instr> NoticeA6 = new ArrayList<Instr>();
	static {
		NoticeA6.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA6.add(new Instr(Prim.SYMB, new Symbols.Symbol(Harbours.Anchor, 0.4, 0, 0, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(180.0))), null)));
	}
	public static final ArrayList<Instr> NoticeA7 = new ArrayList<Instr>();
	static {
		NoticeA7.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA7.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.Bollard, 1.0, 0, 0, null, null)));
	}
	public static final ArrayList<Instr> NoticeA8 = new ArrayList<Instr>();
	static {
		NoticeA8.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA8.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.Turn, 1.0, 0, 0, null, null)));
	}
	public static final ArrayList<Instr> NoticeA9 = new ArrayList<Instr>();
	static {
		NoticeA9.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA9.add(new Instr(Prim.STRK, new BasicStroke(7.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeA9.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-23,10); p.curveTo(-11,10,-12,4,0,4); p.curveTo(12,4,11,10,23,10);
		p.moveTo(-23,-3); p.curveTo(-11,-3,-12,-9,0,-9); p.curveTo(12,-9,11,-3,23,-3);
		NoticeA9.add(new Instr(Prim.PLIN, p));
	}
	public static final ArrayList<Instr> NoticeA10a = new ArrayList<Instr>();
	static {
		NoticeA10a.add(new Instr(Prim.BBOX, new Rectangle(-30,-30,60,60)));
		NoticeA10a.add(new Instr(Prim.FILL, Color.white));
		Path2D.Double p = new Path2D.Double(); p.moveTo(0,-30); p.lineTo(30,0); p.lineTo(0,30); p.closePath();
		NoticeA10a.add(new Instr(Prim.PGON, p));
		NoticeA10a.add(new Instr(Prim.FILL, new Color(0xe80000)));
		p = new Path2D.Double(); p.moveTo(0,-30); p.lineTo(-30,0); p.lineTo(0,30); p.closePath();
		NoticeA10a.add(new Instr(Prim.PGON, p));
		NoticeA10a.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeA10a.add(new Instr(Prim.FILL, Color.black));
		p = new Path2D.Double(); p.moveTo(0,-30); p.lineTo(-30,0); p.lineTo(0,30); p.lineTo(30,0); p.closePath();
		NoticeA10a.add(new Instr(Prim.PLIN, p));
	}
	public static final ArrayList<Instr> NoticeA10b = new ArrayList<Instr>();
	static {
		NoticeA10b.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA10a, 1.0, 0, 0, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(180.0))), null)));
	}
	public static final ArrayList<Instr> NoticeA11= new ArrayList<Instr>();
	static {
		NoticeA11.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA11.add(new Instr(Prim.SYMB, new Symbols.Symbol(Notices.Motor, 1.0, 0, 0, null, null)));
	}
	public static final ArrayList<Instr> NoticeB2a = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeB2b = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeB3a = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeB3b = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeB4a = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeB4b = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeB5 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeB7 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeB8 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeC1 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeC2 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeC3 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeC5a = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeC5b = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeD1a = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeD1b = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeD2a = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeD2b = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE1 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE2 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE3 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE4a = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE4b = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_4 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_5 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_6 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_7 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_8 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_9 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_10 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_11 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_12 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_13 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_14 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE5_15 = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoticeE7_1 = new ArrayList<Instr>();
	public static final ArrayList<Instr> Notice11 = new ArrayList<Instr>();
	public static final ArrayList<Instr> Notice13 = new ArrayList<Instr>();
	public static final ArrayList<Instr> Notice14 = new ArrayList<Instr>();
}
