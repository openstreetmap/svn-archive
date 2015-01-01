/* Copyright 2014 Malcolm Herring
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
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.EnumMap;

import s57.S57val.*;
import symbols.Symbols.*;

public class Notices {
	private static final Symbol Bollard = new Symbol();
	static {
		Path2D.Double p = new Path2D.Double(); p.moveTo(20,21); p.lineTo(20,16.5); p.lineTo(11.6,16.5); p.quadTo(9.1,9.6,8.3,2.0); p.lineTo(-8.0,-0.3); p.quadTo(-8.6,9.0,-11.3,16.5);
		p.lineTo(-23.5,16.5); p.lineTo(-23.5,21.0); p.closePath(); p.moveTo(23.8,3.0); p.lineTo(-10.7,-1.8); p.curveTo(-13.1,-2.2,-12.8,-6.0,-10.2,-5.8); p.lineTo(23.8,-1.1);
		p.closePath(); p.moveTo(8.4,-4.3); p.curveTo(9.0,-9.3,9.0,-11.4,11.2,-13.0); p.curveTo(12.8,-15.0,12.8,-16.7,11.0,-18.6); p.curveTo(4.0,-22.2,-4.0,-22.2,-11.0,-18.6);
		p.curveTo(-12.8,-16.7,-12.8,-15.0,-11.2,-13.0); p.curveTo(-9.0,-11.3,-8.7,-9.5,-8.4,-6.5); p.closePath();
		Bollard.add(new Instr(Form.PGON, p));
	}
	private static final Symbol Motor = new Symbol();
	static {
		Path2D.Double p = new Path2D.Double(); p.moveTo(-5.0,4.3); p.curveTo(-3.7,5.5,-1.8,5.7,-0.2,4.9); p.curveTo(1.3,8.7,4.6,10.9,8.4,10.9); p.curveTo(14.0,10.9,17.5,6.3,17.5,2.0);
		p.curveTo(17.5,-0.7,16.1,-3.2,14.5,-3.2); p.curveTo(12.5,-3.2,11.7,0.8,2.5,1.1); p.curveTo(2.5,-1.2,1.6,-2.2,0.6,-3.0); p.curveTo(3.2,-5.6,4.0,-12.6,-1.0,-16.1);
		p.curveTo(-5.3,-19.2,-11.6,-18.3,-13.7,-13.7); p.curveTo(-14.3,-12.2,-14.0,-11.2,-12.5,-10.6); p.curveTo(-8.6,-9.6,-5.3,-6.0,-4.0,-3.4); p.curveTo(-5.4,-2.6,-6.2,-2.0,-6.2,0.2);
		p.curveTo(-12.8,-1.0,-17.5,3.7,-17.5,9.3); p.curveTo(-17.5,14.7,-12.6,18.8,-8.0,17.6); p.curveTo(-7.0,17.2,-6.6,16.2,-7.2,14.6); p.curveTo(-7.7,12.4,-7.0,7.7,-5.0,4.3); p.closePath();
		Motor.add(new Instr(Form.PGON, p));
	}
	private static final Symbol Rowboat = new Symbol();
	static {
		Path2D.Double p = new Path2D.Double(); p.moveTo(-17.5,-2.0); p.lineTo(17.5,-2.0); p.lineTo(15.0,6.0); p.lineTo(-11.0,6.0); p.closePath();
		Rowboat.add(new Instr(Form.PGON, p));
		Rowboat.add(new Instr(Form.RSHP, new Ellipse2D.Double(-6,-17.5,6,6)));
		Rowboat.add(new Instr(Form.STRK, new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Rowboat.add(new Instr(Form.LINE, new Line2D.Double(-5.5,-9,-8,0)));
		Rowboat.add(new Instr(Form.LINE, new Line2D.Double(-5.0,10.0,-7.5,14.0)));
		Rowboat.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Rowboat.add(new Instr(Form.LINE, new Line2D.Double(-5.5,-9,7,-6.5)));
		Rowboat.add(new Instr(Form.LINE, new Line2D.Double(7.3,-7.8,-5.0,10.0)));
	}
	private static final Symbol Sailboard = new Symbol();
	static {
		Path2D.Double p = new Path2D.Double(); p.moveTo(-6.0,19.0); p.quadTo(-4.0,-5,1.5,-20.0); p.quadTo(14,-7,15.5,6.5); p.quadTo(7,17,-6.0,19.0); p.closePath();
		Sailboard.add(new Instr(Form.PGON, p));
		Sailboard.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Sailboard.add(new Instr(Form.LINE, new Line2D.Double(-2,20,-10,20)));
		Sailboard.add(new Instr(Form.LINE, new Line2D.Double(-13,2.5,-3,2.5)));
		Sailboard.add(new Instr(Form.RSHP, new Ellipse2D.Double(-15,-4,5,5)));
		Sailboard.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		p = new Path2D.Double(); p.moveTo(-13,2.5); p.lineTo(-12,6.0); p.lineTo(-12,9.5);
		Sailboard.add(new Instr(Form.PLIN, p));
		Sailboard.add(new Instr(Form.STRK, new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		p = new Path2D.Double(); p.moveTo(-12,9.5); p.lineTo(-7.5,13.0); p.lineTo(-6.0,19.0);
		Sailboard.add(new Instr(Form.PLIN, p));
	}
	private static final Symbol Sailboat = new Symbol();
	static {
		Path2D.Double p = new Path2D.Double(); p.moveTo(3.75,-20.5); p.lineTo(3.75,8.5); p.lineTo(-19.5,8.5); p.closePath();
		Sailboat.add(new Instr(Form.PGON, p));
		p = new Path2D.Double(); p.moveTo(-19.5,12.0); p.lineTo(19.5,12.0); p.lineTo(13.0,20.5); p.lineTo(-16.0,20.5); p.closePath();
		Sailboat.add(new Instr(Form.PGON, p));
	}
	private static final Symbol Slipway = new Symbol();
	static {
		Path2D.Double p = new Path2D.Double(); p.moveTo(-17,-5.5); p.lineTo(-13.5,0); p.lineTo(4,-1.5); p.quadTo(18,-5,20,-13.5); p.closePath();
		p.moveTo(-14,7); p.lineTo(-14,11); p.lineTo(20,11); p.lineTo(20,2); p.closePath();
		Slipway.add(new Instr(Form.PGON, p));
		Slipway.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Slipway.add(new Instr(Form.LINE, new Line2D.Double(-14,3,20,-2.5)));
		Slipway.add(new Instr(Form.STRK, new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Slipway.add(new Instr(Form.ELPS, new Ellipse2D.Double(1,1.5,3,3)));
		p = new Path2D.Double(); p.moveTo(-21,8.5); p.curveTo(-17.5, 5, -17.5, 12, -13, 7.2);
		Slipway.add(new Instr(Form.PLIN, p));
	}
	private static final Symbol Speedboat = new Symbol();
	static {
		Speedboat.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Speedboat.add(new Instr(Form.LINE, new Line2D.Double(-21,0,-17,-1)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-17.5,8.5); p.curveTo(-10.5,13,-2.5,2,4,6); p.curveTo(12,2,11.5,9.5,20,6);
		Speedboat.add(new Instr(Form.PLIN, p));
		p = new Path2D.Double(); p.moveTo(-18.5,1.5); p.lineTo(-16,6); p.curveTo(-9, 9.0, -3.5, -2.0, 4.5, 3.5); p.lineTo(14.5,0); p.quadTo(19, -3, 19.5, -9);
		p.lineTo(9.5,-6); p.lineTo(6.5,-8); p.lineTo(2.5,-4); p.closePath();
		Speedboat.add(new Instr(Form.PGON, p));
		Speedboat.add(new Instr(Form.RSHP, new Ellipse2D.Double(-1.5,-13,5,5)));
		Speedboat.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Speedboat.add(new Instr(Form.LINE, new Line2D.Double(-2,-7,-5,0)));
		Speedboat.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Speedboat.add(new Instr(Form.LINE, new Line2D.Double(-2,-7,5,-5)));
	}
	private static final Symbol Turn = new Symbol();
	static {
		Turn.add(new Instr(Form.STRK, new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Turn.add(new Instr(Form.EARC, new Arc2D.Double(-9.0,-9.0,18.0,18.0,270.0,230.0,Arc2D.OPEN)));
		Turn.add(new Instr(Form.EARC, new Arc2D.Double(-20.0,-20.0,40.0,40.0,315.0,-280.0,Arc2D.OPEN)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(21.8,-7.0); p.lineTo(18.8,-18.2); p.lineTo(10.5,-10.0); p.closePath();
		p.moveTo(-12.9,0.7); p.lineTo(-1.7,-2.3); p.lineTo(-9.9,-10.5); p.closePath();
		Turn.add(new Instr(Form.PGON, p));
	}
	private static final Symbol Waterbike = new Symbol();
	static {
		Path2D.Double p = new Path2D.Double(); p.moveTo(-17.5,13); p.curveTo(-10.5,17.5,-2.5,6.5,4,10.5); p.curveTo(12,6.5,11.5,14,20,10.5);
		Waterbike.add(new Instr(Form.PLIN, p));
		p = new Path2D.Double(); p.moveTo(-16.5,9.5); p.lineTo(-16,10.5); p.curveTo(-9, 13.5, -3.5, 2.5, 4.5, 8); p.quadTo(15, 4, 19.5, -4); p.closePath();
		p.moveTo(19.5,-5); p.lineTo(1, -5); p.lineTo(-4.5, -10); p.lineTo(-5.5, -10); p.lineTo(2, -2); p.lineTo(-15, 4); p.lineTo(-16, 8); p.closePath();
		Waterbike.add(new Instr(Form.PGON, p));
		Waterbike.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		p = new Path2D.Double(); p.moveTo(-7,1); p.lineTo(-7.5,-1.5); p.lineTo(-12.5,-3.5); p.lineTo(-11.5,-10.5);
		Waterbike.add(new Instr(Form.PLIN, p));
		Waterbike.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Waterbike.add(new Instr(Form.LINE, new Line2D.Double(-11.5,-10.5,-3,-8.5)));
		Waterbike.add(new Instr(Form.RSHP, new Ellipse2D.Double(-11.5,-18,5,5)));
	}
	private static final Symbol Waterski = new Symbol();
	static {
		Waterski.add(new Instr(Form.RSHP, new Ellipse2D.Double(12,-18,6,6)));
		Waterski.add(new Instr(Form.STRK, new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Waterski.add(new Instr(Form.LINE, new Line2D.Double(-18,-6,0,-6)));
		Waterski.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(6.5,17.5); p.lineTo(-13,14.5); p.curveTo(-15,14.25,-16.0,13.6,-17.5,12.0);
		Waterski.add(new Instr(Form.PLIN, p));
		Waterski.add(new Instr(Form.STRK, new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		p = new Path2D.Double(); p.moveTo(-1.5,-4.0); p.lineTo(14,-7.5); p.lineTo(9.5,3.5); p.lineTo(2.0,6.0); p.lineTo(-4.4,15.8);
		Waterski.add(new Instr(Form.PLIN, p));
	}
	private static final Symbol NoticeA = new Symbol();
	static {
		NoticeA.add(new Instr(Form.FILL, new Color(0xe80000)));
		NoticeA.add(new Instr(Form.RSHP, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
		NoticeA.add(new Instr(Form.FILL, Color.white));
		NoticeA.add(new Instr(Form.RSHP, new Rectangle2D.Double(-21,-21,42,42)));
		NoticeA.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		NoticeA.add(new Instr(Form.FILL, new Color(0xe80000)));
		NoticeA.add(new Instr(Form.LINE, new Line2D.Double(-25,-25,25,25)));
		NoticeA.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeA.add(new Instr(Form.FILL, Color.black));
		NoticeA.add(new Instr(Form.RRCT, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
	}
	private static final Symbol NoticeB = new Symbol();
	static {
		NoticeB.add(new Instr(Form.FILL, new Color(0xe80000)));
		NoticeB.add(new Instr(Form.RSHP, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
		NoticeB.add(new Instr(Form.FILL, Color.white));
		NoticeB.add(new Instr(Form.RSHP, new Rectangle2D.Double(-21,-21,42,42)));
		NoticeB.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeB.add(new Instr(Form.FILL, Color.black));
		NoticeB.add(new Instr(Form.RRCT, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
	}
	private static final Symbol NoticeE = new Symbol();
	static {
		NoticeE.add(new Instr(Form.FILL, new Color(0x0000a0)));
		NoticeE.add(new Instr(Form.RSHP, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
		NoticeE.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE.add(new Instr(Form.FILL, Color.black));
		NoticeE.add(new Instr(Form.RRCT, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
		NoticeE.add(new Instr(Form.FILL, Color.white));
	}
	public static final Symbol Notice = new Symbol();
	static {
		Notice.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		Notice.add(new Instr(Form.FILL, new Color(0xe80000)));
		Notice.add(new Instr(Form.RSHP, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
		Notice.add(new Instr(Form.FILL, new Color(0x0000a0)));
		Notice.add(new Instr(Form.RSHP, new Rectangle2D.Double(-21,-21,42,42)));
		Notice.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Notice.add(new Instr(Form.FILL, Color.black));
		Notice.add(new Instr(Form.RRCT, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
	}
	public static final Symbol NoticeA1 = new Symbol();
	static {
		NoticeA1.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA1.add(new Instr(Form.FILL, new Color(0xe80000)));
		NoticeA1.add(new Instr(Form.RSHP, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
		NoticeA1.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeA1.add(new Instr(Form.FILL, Color.white));
		NoticeA1.add(new Instr(Form.RSHP, new Rectangle2D.Double(-30,-10,60,20)));
		NoticeA1.add(new Instr(Form.FILL, Color.black));
		NoticeA1.add(new Instr(Form.RRCT, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
	}
	public static final Symbol NoticeA1a = new Symbol();
	static {
		NoticeA1a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA1a.add(new Instr(Form.FILL, new Color(0xe80000)));
		NoticeA1a.add(new Instr(Form.RSHP, new Ellipse2D.Double(-30,-30,60,60)));
		NoticeA1a.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeA1a.add(new Instr(Form.FILL, Color.white));
		NoticeA1a.add(new Instr(Form.RSHP, new Rectangle2D.Double(-29,-10,58,20)));
		NoticeA1a.add(new Instr(Form.FILL, Color.black));
		NoticeA1a.add(new Instr(Form.ELPS, new Ellipse2D.Double(-30,-30,60,60)));
	}
	public static final Symbol NoticeA2 = new Symbol();
	static {
		NoticeA2.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA2.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA2.add(new Instr(Form.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-10,23); p.lineTo(-10,0); p.lineTo(-6,0); p.lineTo(-12.5,-8); p.lineTo(-19,0); p.lineTo(-15,0); p.lineTo(-15,23);
		p.closePath(); p.moveTo(10,8); p.lineTo(10,-15); p.lineTo(6,-15); p.lineTo(12.5,-23); p.lineTo(19,-15); p.lineTo(15,-15); p.lineTo(15,8); p.closePath();
		NoticeA2.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeA3 = new Symbol();
	static {
		NoticeA3.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA3.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA2, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-10,12); p.lineTo(-6,12); p.lineTo(-12.5,4); p.lineTo(-19,12);
		p.closePath(); p.moveTo(10,-3); p.lineTo(6,-3); p.lineTo(12.5,-11); p.lineTo(19,-3); p.closePath();
		NoticeA3.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeA4 = new Symbol();
	static {
		NoticeA4.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA4.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA4.add(new Instr(Form.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-10,-15); p.lineTo(-10,8); p.lineTo(-6,8); p.lineTo(-12.5,16); p.lineTo(-19,8); p.lineTo(-15,8); p.lineTo(-15,-15);
		p.closePath(); p.moveTo(10,15); p.lineTo(10,-8); p.lineTo(6,-8); p.lineTo(12.5,-16); p.lineTo(19,-8); p.lineTo(15,-8); p.lineTo(15,15); p.closePath();
		NoticeA4.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeA4_1 = new Symbol();
	static {
		NoticeA4_1.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA4_1.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA4, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-10,-4); p.lineTo(-6,-4); p.lineTo(-12.5,4); p.lineTo(-19,-4);
		p.closePath(); p.moveTo(10,5); p.lineTo(6,5); p.lineTo(12.5,-3); p.lineTo(19,5); p.closePath();
		NoticeA4_1.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeA5 = new Symbol();
	static {
		NoticeA5.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA5.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD); p.moveTo(-5.3,14.6); p.lineTo(-5.3,4.0); p.lineTo(0.0,4.0); p.curveTo(4.2,4.0,7.4,3.5,9.4,0.0);
		p.curveTo(11.4,-2.8,11.4,-7.2,9.4,-10.5); p.curveTo(7.4,-13.6,4.2,-14.0,0.0,-14.0); p.lineTo(-11.0,-14.0); p.lineTo(-11.0,14.6); p.closePath();
		p.moveTo(-5.3,-1.0); p.lineTo(0.0,-1.0); p.curveTo(6.5,-1.0,6.5,-9.0,0.0,-9.0); p.lineTo(-5.3,-9.0); p.closePath();
		NoticeA5.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeA5_1 = new Symbol();
	static {
		NoticeA5_1.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA5_1.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
	}
	public static final Symbol NoticeA6 = new Symbol();
	static {
		NoticeA6.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA6.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA6.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Harbours.Anchor, 0.4, 0, 0, new Scheme(Color.black), new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(180.0))))));
	}
	public static final Symbol NoticeA7 = new Symbol();
	static {
		NoticeA7.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA7.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA7.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Bollard, 1.0, 0, 0, new Scheme(Color.black), null)));
	}
	public static final Symbol NoticeA8 = new Symbol();
	static {
		NoticeA8.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA8.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA8.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Turn, 1.0, 0, 0, new Scheme(Color.black), null)));
	}
	public static final Symbol NoticeA9 = new Symbol();
	static {
		NoticeA9.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA9.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA9.add(new Instr(Form.STRK, new BasicStroke(7, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeA9.add(new Instr(Form.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-23,10); p.curveTo(-11,10,-12,4,0,4); p.curveTo(12,4,11,10,23,10);
		p.moveTo(-23,-3); p.curveTo(-11,-3,-12,-9,0,-9); p.curveTo(12,-9,11,-3,23,-3);
		NoticeA9.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol NoticeA10a = new Symbol();
	static {
		NoticeA10a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA10a.add(new Instr(Form.FILL, Color.white));
		Path2D.Double p = new Path2D.Double(); p.moveTo(0,-30); p.lineTo(30,0); p.lineTo(0,30); p.closePath();
		NoticeA10a.add(new Instr(Form.PGON, p));
		NoticeA10a.add(new Instr(Form.FILL, new Color(0xe80000)));
		p = new Path2D.Double(); p.moveTo(0,-30); p.lineTo(-30,0); p.lineTo(0,30); p.closePath();
		NoticeA10a.add(new Instr(Form.PGON, p));
		NoticeA10a.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeA10a.add(new Instr(Form.FILL, Color.black));
		p = new Path2D.Double(); p.moveTo(0,-30); p.lineTo(-30,0); p.lineTo(0,30); p.lineTo(30,0); p.closePath();
		NoticeA10a.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol NoticeA10b = new Symbol();
	static {
		NoticeA10b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA10b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA10a, 1.0, 0, 0, null, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(180.0))))));
	}
	public static final Symbol NoticeA12 = new Symbol();
	static {
		NoticeA12.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA12.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA12.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Motor, 1.0, 0, 0, new Scheme(Color.black), null)));
	}
	public static final Symbol NoticeA13 = new Symbol();
	static {
		NoticeA13.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA13.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA13.add(new Instr(Form.TEXT, new Caption("SPORT", new Font("Arial", Font.BOLD, 15), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 5)))));
	}
	public static final Symbol NoticeA14 = new Symbol();
	static {
		NoticeA14.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA14.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA14.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Waterski, 1.0, 0, 0, new Scheme(Color.black), null)));
	}
	public static final Symbol NoticeA15 = new Symbol();
	static {
		NoticeA15.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA15.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA15.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Sailboat, 1.0, 0, 0, new Scheme(Color.black), null)));
	}
	public static final Symbol NoticeA16 = new Symbol();
	static {
		NoticeA16.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA16.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA16.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Rowboat, 1.0, 0, 0, new Scheme(Color.black), null)));
	}
	public static final Symbol NoticeA17 = new Symbol();
	static {
		NoticeA17.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA17.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA17.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Sailboard, 1.0, 0, 0, new Scheme(Color.black), null)));
	}
	public static final Symbol NoticeA18 = new Symbol();
	static {
		NoticeA18.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA18.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA18.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Speedboat, 1.0, 0, 0, new Scheme(Color.black), null)));
	}
	public static final Symbol NoticeA19 = new Symbol();
	static {
		NoticeA19.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA19.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA19.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Slipway, 1.0, 0, 0, new Scheme(Color.black), null)));
	}
	public static final Symbol NoticeA20 = new Symbol();
	static {
		NoticeA20.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeA20.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeA, 1.0, 0, 0, null, null)));
		NoticeA20.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Waterbike, 1.0, 0, 0, new Scheme(Color.black), null)));
	}
	public static final Symbol NoticeB1a = new Symbol();
	static {
		NoticeB1a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB1a.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(21,8); p.lineTo(-8,8); p.lineTo(-8,18); p.lineTo(-21,0);
		p.lineTo(-8,-18); p.lineTo(-8,-8); p.lineTo(21,-8); p.closePath();
		NoticeB1a.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeB1b = new Symbol();
	static {
		NoticeB1b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB1b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-21,8); p.lineTo(8,8); p.lineTo(8,18); p.lineTo(21,0);
		p.lineTo(8,-18); p.lineTo(8,-8); p.lineTo(-21,-8); p.closePath();
		NoticeB1b.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeB2a = new Symbol();
	static {
		NoticeB2a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB2a.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		NoticeB2a.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(18,21); p.lineTo(18,10); p.lineTo(-15,-10); p.lineTo(-15,-15);
		NoticeB2a.add(new Instr(Form.PLIN, p));
		p = new Path2D.Double(); p.moveTo(-15,-21); p.lineTo(-21,-15); p.lineTo(-9,-15); p.closePath();
		NoticeB2a.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeB2b = new Symbol();
	static {
		NoticeB2b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB2b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		NoticeB2b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		NoticeB2b.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-18,21); p.lineTo(-18,10); p.lineTo(15,-10); p.lineTo(15,-15);
		NoticeB2b.add(new Instr(Form.PLIN, p));
		p = new Path2D.Double(); p.moveTo(15,-21); p.lineTo(21,-15); p.lineTo(9,-15); p.closePath();
		NoticeB2b.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeB3a = new Symbol();
	static {
		NoticeB3a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB3a.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		NoticeB3a.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeB3a.add(new Instr(Form.LINE, new Line2D.Double(-15,21,-15,-15)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-15,-21); p.lineTo(-21,-15); p.lineTo(-9,-15); p.closePath();
		NoticeB3a.add(new Instr(Form.PGON, p));
		NoticeB3a.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 5.5f, 2.4f }, 0.0f)));
		NoticeB3a.add(new Instr(Form.LINE, new Line2D.Double(15,-21,15,15)));
		p = new Path2D.Double(); p.moveTo(15,21); p.lineTo(21,15); p.lineTo(9,15); p.closePath();
		NoticeB3a.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeB3b = new Symbol();
	static {
		NoticeB3b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB3b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		NoticeB3b.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeB3b.add(new Instr(Form.LINE, new Line2D.Double(15,21,15,-15)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(15,-21); p.lineTo(21,-15); p.lineTo(9,-15); p.closePath();
		NoticeB3b.add(new Instr(Form.PGON, p));
		NoticeB3b.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 5.5f, 2.4f }, 0.0f)));
		NoticeB3b.add(new Instr(Form.LINE, new Line2D.Double(-15,-21,-15,15)));
		p = new Path2D.Double(); p.moveTo(-15,21); p.lineTo(-21,15); p.lineTo(-9,15); p.closePath();
		NoticeB3b.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeB4a = new Symbol();
	static {
		NoticeB4a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB4a.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB2a, 1.0, 0, 0, null, null)));
		NoticeB4a.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 5.5f, 2.4f }, 0.0f)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(18,-21); p.lineTo(18,-10); p.lineTo(-15,10); p.lineTo(-15,15);
		NoticeB4a.add(new Instr(Form.PLIN, p));
		p = new Path2D.Double(); p.moveTo(-15,21); p.lineTo(-21,15); p.lineTo(-9,15); p.closePath();
		NoticeB4a.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeB4b = new Symbol();
	static {
		NoticeB4b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB4b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB2b, 1.0, 0, 0, null, null)));
		NoticeB4b.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 5.5f, 2.4f }, 0.0f)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-18,-21); p.lineTo(-18,-10); p.lineTo(15,10); p.lineTo(15,15);
		NoticeB4b.add(new Instr(Form.PLIN, p));
		p = new Path2D.Double(); p.moveTo(15,21); p.lineTo(21,15); p.lineTo(9,15); p.closePath();
		NoticeB4b.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeB5 = new Symbol();
	static {
		NoticeB5.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB5.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		NoticeB5.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		NoticeB5.add(new Instr(Form.LINE, new Line2D.Double(15,0,-15,0)));
	}
	public static final Symbol NoticeB6 = new Symbol();
	static {
		NoticeB6.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB6.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
	}
	public static final Symbol NoticeB7 = new Symbol();
	static {
		NoticeB7.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB7.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		NoticeB7.add(new Instr(Form.RSHP, new Ellipse2D.Double(-10,-10,20,20)));
	}
	public static final Symbol NoticeB8 = new Symbol();
	static {
		NoticeB8.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB8.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		NoticeB8.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		NoticeB8.add(new Instr(Form.LINE, new Line2D.Double(0,15,0,-15)));
	}
	public static final Symbol NoticeB9a = new Symbol();
	static {
		NoticeB9a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB9a.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		NoticeB9a.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeB9a.add(new Instr(Form.LINE, new Line2D.Double(-21,0,21,0)));
		NoticeB9a.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeB9a.add(new Instr(Form.LINE, new Line2D.Double(0,21,0,0)));
	}
	public static final Symbol NoticeB9b = new Symbol();
	static {
		NoticeB9b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB9b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		NoticeB9b.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeB9b.add(new Instr(Form.LINE, new Line2D.Double(-21,0,21,0)));
		NoticeB9b.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeB9b.add(new Instr(Form.LINE, new Line2D.Double(0,21,0,-21)));
	}
	public static final Symbol NoticeB11 = new Symbol();
	static {
		NoticeB11.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeB11.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		NoticeB11.add(new Instr(Form.TEXT, new Caption("VHF", new Font("Arial", Font.BOLD, 20), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 0)))));
	}
	public static final Symbol NoticeC1 = new Symbol();
	static {
		NoticeC1.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeC1.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-15,21); p.lineTo(0,12); p.lineTo(15,21); p.closePath();
		NoticeC1.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeC2 = new Symbol();
	static {
		NoticeC2.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeC2.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-15,-21); p.lineTo(0,-12); p.lineTo(15,-21); p.closePath();
		NoticeC2.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeC3 = new Symbol();
	static {
		NoticeC3.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeC3.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(21,-15); p.lineTo(12,0); p.lineTo(21,15); p.closePath();
		p.moveTo(-21,-15); p.lineTo(-12,0); p.lineTo(-21,15); p.closePath();
		NoticeC3.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeC4 = new Symbol();
	static {
		NoticeC4.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeC4.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
	}
	public static final Symbol NoticeC5a = new Symbol();
	static {
		NoticeC5a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeC5a.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-21,-21); p.lineTo(10,-21); p.lineTo(21,0);	p.lineTo(10,21);	p.lineTo(-21,21); p.closePath();
		NoticeC5a.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeC5b = new Symbol();
	static {
		NoticeC5b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeC5b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeB, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(21,-21); p.lineTo(-10,-21); p.lineTo(-21,0);	p.lineTo(-10,21);	p.lineTo(21,21); p.closePath();
		NoticeC5b.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeD1a = new Symbol();
	static {
		NoticeD1a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeD1a.add(new Instr(Form.FILL, Color.yellow));
		Path2D.Double p = new Path2D.Double(); p.moveTo(0,-30); p.lineTo(-30,0); p.lineTo(0,30); p.lineTo(30,0); p.closePath();
		NoticeD1a.add(new Instr(Form.PGON, p));
		NoticeD1a.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeD1a.add(new Instr(Form.FILL, Color.black));
		NoticeD1a.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol NoticeD1b = new Symbol();
	static {
		NoticeD1b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeD1b.add(new Instr(Form.FILL, Color.yellow));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-30,0); p.lineTo(-15,25); p.lineTo(15,-25); p.lineTo(30,0);	p.lineTo(15,25); p.lineTo(-15,-25); p.closePath();
		NoticeD1b.add(new Instr(Form.PGON, p));
		NoticeD1b.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeD1b.add(new Instr(Form.FILL, Color.black));
		NoticeD1b.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol NoticeD2a = new Symbol();
	static {
		NoticeD2a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeD2a.add(new Instr(Form.FILL, Color.white));
		Path2D.Double p = new Path2D.Double(); p.moveTo(0,-30); p.lineTo(-30,0); p.lineTo(0,30); p.closePath();
		NoticeD2a.add(new Instr(Form.PGON, p));
		NoticeD2a.add(new Instr(Form.FILL, new Color(0x00e800)));
		p = new Path2D.Double(); p.moveTo(0,-30); p.lineTo(30,0); p.lineTo(0,30); p.closePath();
		NoticeD2a.add(new Instr(Form.PGON, p));
		NoticeD2a.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeD2a.add(new Instr(Form.FILL, Color.black));
		p = new Path2D.Double(); p.moveTo(0,-30); p.lineTo(-30,0); p.lineTo(0,30); p.lineTo(30,0); p.closePath();
		NoticeD2a.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol NoticeD2b = new Symbol();
	static {
		NoticeD2b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeD2b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeD2a, 1.0, 0, 0, null, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(180.0))))));
	}
	public static final Symbol NoticeD3a = new Symbol();
	static {
		NoticeD3a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeD3a.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(28,10); p.lineTo(-10,10); p.lineTo(-10,20); p.lineTo(-28,0);
		p.lineTo(-10,-20); p.lineTo(-10,-10); p.lineTo(28,-10); p.closePath();
		NoticeD3a.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeD3b = new Symbol();
	static {
		NoticeD3b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeD3b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-28,10); p.lineTo(10,10); p.lineTo(10,20); p.lineTo(28,0);
		p.lineTo(10,-20); p.lineTo(10,-10); p.lineTo(-28,-10); p.closePath();
		NoticeD3b.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE1 = new Symbol();
	static {
		NoticeE1.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE1.add(new Instr(Form.FILL, new Color(0x00e800)));
		NoticeE1.add(new Instr(Form.RSHP, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
		NoticeE1.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE1.add(new Instr(Form.FILL, Color.white));
		NoticeE1.add(new Instr(Form.RSHP, new Rectangle2D.Double(-10,-30,20,60)));
		NoticeE1.add(new Instr(Form.FILL, Color.black));
		NoticeE1.add(new Instr(Form.RRCT, new RoundRectangle2D.Double(-30,-30,60,60,4,4)));
	}
	public static final Symbol NoticeE2 = new Symbol();
	static {
		NoticeE2.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE2.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(5,-25); p.lineTo(-10,-1); p.lineTo(10,-1); p.lineTo(-10,20);
		p.lineTo(-7,20); p.lineTo(-12,25); p.lineTo(-16,20); p.lineTo(-13,20); p.lineTo(4,1); p.lineTo(-14,1);
		p.lineTo(2,-25); p.closePath();
		NoticeE2.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE3 = new Symbol();
	static {
		NoticeE3.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE3.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE3.add(new Instr(Form.STRK, new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE3.add(new Instr(Form.LINE, new Line2D.Double(25,-20,25,20)));
		NoticeE3.add(new Instr(Form.LINE, new Line2D.Double(-25,-20,-25,20)));
		NoticeE3.add(new Instr(Form.LINE, new Line2D.Double(-15,-15,-15,20)));
		NoticeE3.add(new Instr(Form.LINE, new Line2D.Double(-5,-15,-5,20)));
		NoticeE3.add(new Instr(Form.LINE, new Line2D.Double(5,-15,5,20)));
		NoticeE3.add(new Instr(Form.LINE, new Line2D.Double(15,-15,15,20)));
		NoticeE3.add(new Instr(Form.STRK, new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE3.add(new Instr(Form.LINE, new Line2D.Double(-26,18.5,26,18.5)));
		NoticeE3.add(new Instr(Form.LINE, new Line2D.Double(-26,-15,26,-15)));
	}
	public static final Symbol NoticeE4a = new Symbol();
	static {
		NoticeE4a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE4a.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-20,-10); p.lineTo(-5,-10); p.lineTo(-5,-20); p.lineTo(5,-20); p.lineTo(5,-10);
		p.lineTo(20,-10); p.lineTo(15,0); p.lineTo(-15,0); p.closePath();
		p.moveTo(-25,5); p.lineTo(25,5); p.lineTo(25,10); p.lineTo(-25,10); p.closePath();
		NoticeE4a.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE4b = new Symbol();
	static {
		NoticeE4b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE4b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-20,0); p.lineTo(-5,0); p.lineTo(-5,-10); p.lineTo(5,-10); p.lineTo(5,0);
		p.lineTo(20,0); p.lineTo(15,10); p.lineTo(-15,10); p.closePath();
		NoticeE4b.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5 = new Symbol();
	static {
		NoticeE5.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD); p.moveTo(-5.3,14.6); p.lineTo(-5.3,4.0); p.lineTo(0.0,4.0); p.curveTo(4.2,4.0,7.4,3.5,9.4,0.0);
		p.curveTo(11.4,-2.8,11.4,-7.2,9.4,-10.5); p.curveTo(7.4,-13.6,4.2,-14.0,0.0,-14.0); p.lineTo(-11.0,-14.0); p.lineTo(-11.0,14.6); p.closePath();
		p.moveTo(-5.3,-1.0); p.lineTo(0.0,-1.0); p.curveTo(6.5,-1.0,6.5,-9.0,0.0,-9.0); p.lineTo(-5.3,-9.0); p.closePath();
		NoticeE5.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_1 = new Symbol();
	static {
		NoticeE5_1.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_1.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
	}
	public static final Symbol NoticeE5_2 = new Symbol();
	static {
		NoticeE5_2.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_2.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
	}
	public static final Symbol NoticeE5_3 = new Symbol();
	static {
		NoticeE5_3.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_3.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
	}
	public static final Symbol NoticeE5_4 = new Symbol();
	static {
		NoticeE5_4.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_4.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,25); p.lineTo(0,-28); p.lineTo(28,25); p.closePath();
		NoticeE5_4.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_5 = new Symbol();
	static {
		NoticeE5_5.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_5.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,25); p.lineTo(0,-28); p.lineTo(28,25); p.closePath();
		p.moveTo(0,24); p.lineTo(-15,2); p.lineTo(15,2); p.closePath();
		NoticeE5_5.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_6 = new Symbol();
	static {
		NoticeE5_6.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_6.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,25); p.lineTo(0,-28); p.lineTo(28,25); p.closePath();
		p.moveTo(0,7); p.lineTo(-10,-8); p.lineTo(10,-8); p.closePath();
		p.moveTo(0,24); p.lineTo(-10,9); p.lineTo(10,9); p.closePath();
		NoticeE5_6.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_7 = new Symbol();
	static {
		NoticeE5_7.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_7.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,25); p.lineTo(0,-28); p.lineTo(28,25); p.closePath();
		p.moveTo(0,-1); p.lineTo(-8,-11); p.lineTo(8,-11); p.closePath();
		p.moveTo(0,11); p.lineTo(-8,1); p.lineTo(8,1); p.closePath();
		p.moveTo(0,23); p.lineTo(-8,13); p.lineTo(8,13); p.closePath();
		NoticeE5_7.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_8 = new Symbol();
	static {
		NoticeE5_8.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_8.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,-25); p.lineTo(0,28); p.lineTo(28,-25); p.closePath();
		NoticeE5_8.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_9 = new Symbol();
	static {
		NoticeE5_9.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_9.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,-25); p.lineTo(0,28); p.lineTo(28,-25); p.closePath();
		p.moveTo(0,8); p.lineTo(-15,-14); p.lineTo(15,-14); p.closePath();
		NoticeE5_9.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_10 = new Symbol();
	static {
		NoticeE5_10.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_10.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,-25); p.lineTo(0,28); p.lineTo(28,-25); p.closePath();
		p.moveTo(0,-5); p.lineTo(-10,-20); p.lineTo(10,-20); p.closePath();
		p.moveTo(0,15); p.lineTo(-10,0); p.lineTo(10,0); p.closePath();
		NoticeE5_10.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_11 = new Symbol();
	static {
		NoticeE5_11.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_11.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,-25); p.lineTo(0,28); p.lineTo(28,-25); p.closePath();
		p.moveTo(0,-12); p.lineTo(-8,-22); p.lineTo(8,-22); p.closePath();
		p.moveTo(0,3); p.lineTo(-8,-7); p.lineTo(8,-7); p.closePath();
		p.moveTo(0,18); p.lineTo(-8,8); p.lineTo(8,8); p.closePath();
		NoticeE5_11.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_12 = new Symbol();
	static {
		NoticeE5_12.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_12.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,0); p.lineTo(0,28); p.lineTo(28,0); p.lineTo(0,-28); p.closePath();
		NoticeE5_12.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_13 = new Symbol();
	static {
		NoticeE5_13.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_13.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,0); p.lineTo(0,28); p.lineTo(28,0); p.lineTo(0,-28); p.closePath();
		p.moveTo(0,15); p.lineTo(-15,-7); p.lineTo(15,-7); p.closePath();
		NoticeE5_13.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_14 = new Symbol();
	static {
		NoticeE5_14.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_14.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,0); p.lineTo(0,28); p.lineTo(28,0); p.lineTo(0,-28); p.closePath();
		p.moveTo(0,0); p.lineTo(-10,-15); p.lineTo(10,-15); p.closePath();
		p.moveTo(0,20); p.lineTo(-10,5); p.lineTo(10,5); p.closePath();
		NoticeE5_14.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE5_15 = new Symbol();
	static {
		NoticeE5_15.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE5_15.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-28,0); p.lineTo(0,28); p.lineTo(28,0); p.lineTo(0,-28); p.closePath();
		p.moveTo(0,-7); p.lineTo(-8,-17); p.lineTo(8,-17); p.closePath();
		p.moveTo(0,8); p.lineTo(-8,-2); p.lineTo(8,-2); p.closePath();
		p.moveTo(0,23); p.lineTo(-8,13); p.lineTo(8,13); p.closePath();
		NoticeE5_15.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE6 = new Symbol();
	static {
		NoticeE6.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE6.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE6.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Harbours.Anchor, 0.4, 0, 0, new Scheme(Color.white), null)));
	}
	public static final Symbol NoticeE7 = new Symbol();
	static {
		NoticeE7.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE7.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE7.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Bollard, 1.0, 0, 0, new Scheme(Color.white), null)));
	}
	public static final Symbol NoticeE7_1 = new Symbol();
	static {
		NoticeE7_1.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE7_1.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE7_1.add(new Instr(Form.STRK, new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE7_1.add(new Instr(Form.LINE, new Line2D.Double(20,25,20,-10)));
		NoticeE7_1.add(new Instr(Form.LINE, new Line2D.Double(22,-8,-15,-20)));
		NoticeE7_1.add(new Instr(Form.STRK, new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE7_1.add(new Instr(Form.LINE, new Line2D.Double(20,8,0,-15)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-17,2); p.quadTo(-5,0,7,2); p.lineTo(9,12); p.lineTo(7,20); p.lineTo(6,20); p.lineTo(6,23); p.lineTo(3,23); p.lineTo(3,20);
		p.quadTo(-5,22,-13,20); p.lineTo(-13,23); p.lineTo(-16,23); p.lineTo(-16,20); p.lineTo(-17,20); p.lineTo(-19,12); p.closePath();
		p.moveTo(-15,4); p.quadTo(-3,2,5,4); p.lineTo(6,11); p.quadTo(-5,9,-16,11); p.closePath();
		NoticeE7_1.add(new Instr(Form.PGON, p));
		NoticeE7_1.add(new Instr(Form.FILL, new Color(0x0000a0)));
		NoticeE7_1.add(new Instr(Form.RSHP, new Ellipse2D.Double(-16,13,4,4)));
		NoticeE7_1.add(new Instr(Form.RSHP, new Ellipse2D.Double(2,13,4,4)));
	}
	public static final Symbol NoticeE8 = new Symbol();
	static {
		NoticeE8.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE8.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE8.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Turn, 1.0, 0, 0, new Scheme(Color.white), null)));
	}
	public static final Symbol NoticeE9a = new Symbol();
	static {
		NoticeE9a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE9a.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE9a.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9a.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-29)));
		NoticeE9a.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9a.add(new Instr(Form.LINE, new Line2D.Double(-29,0,29,0)));
	}
	public static final Symbol NoticeE9b = new Symbol();
	static {
		NoticeE9b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE9b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE9b.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9b.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-29)));
		NoticeE9b.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9b.add(new Instr(Form.LINE, new Line2D.Double(-2,0,29,0)));
	}
	public static final Symbol NoticeE9c = new Symbol();
	static {
		NoticeE9c.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE9c.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE9c.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9c.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-29)));
		NoticeE9c.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9c.add(new Instr(Form.LINE, new Line2D.Double(2,0,-29,0)));
	}
	public static final Symbol NoticeE9d = new Symbol();
	static {
		NoticeE9d.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE9d.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE9d.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9d.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-4)));
		NoticeE9d.add(new Instr(Form.LINE, new Line2D.Double(-4,0,29,0)));
		NoticeE9d.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9d.add(new Instr(Form.LINE, new Line2D.Double(0,-29,0,2)));
	}
	public static final Symbol NoticeE9e = new Symbol();
	static {
		NoticeE9e.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE9e.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE9e.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9e.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-4)));
		NoticeE9e.add(new Instr(Form.LINE, new Line2D.Double(4,0,-29,0)));
		NoticeE9e.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9e.add(new Instr(Form.LINE, new Line2D.Double(0,-29,0,2)));
	}
	public static final Symbol NoticeE9f = new Symbol();
	static {
		NoticeE9f.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE9f.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE9f.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9f.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-4)));
		NoticeE9f.add(new Instr(Form.LINE, new Line2D.Double(-4,0,29,0)));
		NoticeE9f.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9f.add(new Instr(Form.LINE, new Line2D.Double(2,0,-29,0)));
	}
	public static final Symbol NoticeE9g = new Symbol();
	static {
		NoticeE9g.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE9g.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE9g.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9g.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-4)));
		NoticeE9g.add(new Instr(Form.LINE, new Line2D.Double(4,0,-29,0)));
		NoticeE9g.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9g.add(new Instr(Form.LINE, new Line2D.Double(-2,0,29,0)));
	}
	public static final Symbol NoticeE9h = new Symbol();
	static {
		NoticeE9h.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE9h.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE9h.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9h.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-4)));
		NoticeE9h.add(new Instr(Form.LINE, new Line2D.Double(-4,0,29,0)));
		NoticeE9h.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9h.add(new Instr(Form.LINE, new Line2D.Double(0,-29,0,2)));
		NoticeE9h.add(new Instr(Form.LINE, new Line2D.Double(2,0,-29,0)));
	}
	public static final Symbol NoticeE9i = new Symbol();
	static {
		NoticeE9i.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE9i.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE9i.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9i.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-4)));
		NoticeE9i.add(new Instr(Form.LINE, new Line2D.Double(4,0,-29,0)));
		NoticeE9i.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE9i.add(new Instr(Form.LINE, new Line2D.Double(0,-29,0,2)));
		NoticeE9i.add(new Instr(Form.LINE, new Line2D.Double(-2,0,29,0)));
	}
	public static final Symbol NoticeE10a = new Symbol();
	static {
		NoticeE10a.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE10a.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE10a.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE10a.add(new Instr(Form.LINE, new Line2D.Double(-29,0,29,0)));
		NoticeE10a.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE10a.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-29)));
	}
	public static final Symbol NoticeE10b = new Symbol();
	static {
		NoticeE10b.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE10b.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE10b.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE10b.add(new Instr(Form.LINE, new Line2D.Double(-29,0,29,0)));
		NoticeE10b.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE10b.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-2)));
	}
	public static final Symbol NoticeE10c = new Symbol();
	static {
		NoticeE10c.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE10c.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE10c.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE10c.add(new Instr(Form.LINE, new Line2D.Double(0,-29,0,4)));
		NoticeE10c.add(new Instr(Form.LINE, new Line2D.Double(-4,0,29,0)));
		NoticeE10c.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE10c.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-2)));
	}
	public static final Symbol NoticeE10d = new Symbol();
	static {
		NoticeE10d.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE10d.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE10d.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE10d.add(new Instr(Form.LINE, new Line2D.Double(0,-29,0,4)));
		NoticeE10d.add(new Instr(Form.LINE, new Line2D.Double(4,0,-29,0)));
		NoticeE10d.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE10d.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-2)));
	}
	public static final Symbol NoticeE10e = new Symbol();
	static {
	NoticeE10e.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
	NoticeE10e.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
	NoticeE10e.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
	NoticeE10e.add(new Instr(Form.LINE, new Line2D.Double(0,-29,0,4)));
	NoticeE10e.add(new Instr(Form.LINE, new Line2D.Double(-4,0,29,0)));
	NoticeE10e.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
	NoticeE10e.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-2)));
	NoticeE10e.add(new Instr(Form.LINE, new Line2D.Double(2,0,-29,0)));
	}
	public static final Symbol NoticeE10f = new Symbol();
	static {
		NoticeE10f.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE10f.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE10f.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE10f.add(new Instr(Form.LINE, new Line2D.Double(0,-29,0,4)));
		NoticeE10f.add(new Instr(Form.LINE, new Line2D.Double(4,0,-29,0)));
		NoticeE10f.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoticeE10f.add(new Instr(Form.LINE, new Line2D.Double(0,29,0,-2)));
		NoticeE10f.add(new Instr(Form.LINE, new Line2D.Double(-2,0,29,0)));
	}
	public static final Symbol NoticeE11 = new Symbol();
	static {
		NoticeE11.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE11.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE11.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		NoticeE11.add(new Instr(Form.LINE, new Line2D.Double(-27,-27,27,27)));
	}
	public static final Symbol NoticeE13 = new Symbol();
	static {
		NoticeE13.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE13.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-4,-16); p.lineTo(9,-16); p.lineTo(9,-14); p.lineTo(3.5,-14); p.lineTo(3.5,-7); p.lineTo(5,-7);
		p.lineTo(5,1); p.lineTo(6.5,1); p.lineTo(6.5,5); p.lineTo(17.5,5); p.lineTo(17.5,1); p.lineTo(19,1); p.lineTo(19,15); p.lineTo(17.5,15);
		p.lineTo(17.5,10); p.lineTo(17.5,10); p.lineTo(6.5,10); p.lineTo(6.5,13); p.lineTo(-2,13); p.lineTo(-2,10); p.lineTo(-9,10);
		p.quadTo(-13.5,10,-13.5,16); p.lineTo(-19,16); p.quadTo(-19,5,-9,5); p.lineTo(-2,5); p.lineTo(-2,1); p.lineTo(0,1); p.lineTo(0,-7);
		p.lineTo(1.5,-7); p.lineTo(1.5,-14); p.lineTo(-4,-14); p.closePath();
		NoticeE13.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE14 = new Symbol();
	static {
		NoticeE14.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE14.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-18,-18); p.lineTo(-11,-7); p.lineTo(-9,-10); p.lineTo(-14,-18); p.closePath();
		p.moveTo(9.5,7); p.lineTo(22.5,9); p.lineTo(21.5,5.5); p.lineTo(12,4); p.closePath();
		p.moveTo(-19,-16.5); p.lineTo(-13,-6.5); p.quadTo(-15.5,-2,-12.5,0); p.lineTo(4,11); p.quadTo(7,13,10,9); p.lineTo(21.5,11);
		p.curveTo(15.5,23,1,18.5,-9,12); p.curveTo(-18,6,-28.5,-7,-19,-16.5); p.closePath();
		NoticeE14.add(new Instr(Form.PGON, p));
	}
	public static final Symbol NoticeE15 = new Symbol();
	static {
		NoticeE15.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE15.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE15.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Motor, 1.0, 0, 0, new Scheme(Color.white), null)));
	}
	public static final Symbol NoticeE16 = new Symbol();
	static {
		NoticeE16.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE16.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE16.add(new Instr(Form.TEXT, new Caption("SPORT", new Font("Arial", Font.BOLD, 15), Color.white, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 5)))));
	}
	public static final Symbol NoticeE17 = new Symbol();
	static {
		NoticeE17.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE17.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE17.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Waterski, 1.0, 0, 0, new Scheme(Color.white), null)));
	}
	public static final Symbol NoticeE18 = new Symbol();
	static {
		NoticeE18.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE18.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE18.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Sailboat, 1.0, 0, 0, new Scheme(Color.white), null)));
	}
	public static final Symbol NoticeE19 = new Symbol();
	static {
		NoticeE19.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE19.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE19.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Rowboat, 1.0, 0, 0, new Scheme(Color.white), null)));
	}
	public static final Symbol NoticeE20 = new Symbol();
	static {
		NoticeE20.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE20.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE20.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Sailboard, 1.0, 0, 0, new Scheme(Color.white), null)));
	}
	public static final Symbol NoticeE21 = new Symbol();
	static {
		NoticeE21.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE21.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE21.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Speedboat, 1.0, 0, 0, new Scheme(Color.white), null)));
	}
	public static final Symbol NoticeE22 = new Symbol();
	static {
		NoticeE22.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE22.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE22.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Slipway, 1.0, 0, 0, new Scheme(Color.white), null)));
	}
	public static final Symbol NoticeE23 = new Symbol();
	static {
		NoticeE23.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE23.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE23.add(new Instr(Form.TEXT, new Caption("VHF", new Font("Arial", Font.BOLD, 20), Color.white, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 0)))));
	}
	public static final Symbol NoticeE24 = new Symbol();
	static {
		NoticeE24.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		NoticeE24.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.NoticeE, 1.0, 0, 0, null, null)));
		NoticeE24.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Notices.Waterbike, 1.0, 0, 0, new Scheme(Color.white), null)));
	}

	public static final EnumMap<CatNMK, Symbol> NmkCevni = new EnumMap<CatNMK, Symbol>(CatNMK.class);
	static {
		NmkCevni.put(CatNMK.NMK_UNKN, Notice); NmkCevni.put(CatNMK.NMK_NENT, NoticeA1); NmkCevni.put(CatNMK.NMK_CLSA, NoticeA1a); NmkCevni.put(CatNMK.NMK_NOVK, NoticeA2);
		NmkCevni.put(CatNMK.NMK_NCOV, NoticeA3); NmkCevni.put(CatNMK.NMK_NPAS, NoticeA4); NmkCevni.put(CatNMK.NMK_NCPS, NoticeA4_1); NmkCevni.put(CatNMK.NMK_NBRT, NoticeA5);
		NmkCevni.put(CatNMK.NMK_NBLL, NoticeA5_1); NmkCevni.put(CatNMK.NMK_NANK, NoticeA6); NmkCevni.put(CatNMK.NMK_NMOR, NoticeA7); NmkCevni.put(CatNMK.NMK_NTRN, NoticeA8);
		NmkCevni.put(CatNMK.NMK_NWSH, NoticeA9); NmkCevni.put(CatNMK.NMK_NPSL, NoticeA10a); NmkCevni.put(CatNMK.NMK_NPSR, NoticeA10b); NmkCevni.put(CatNMK.NMK_NMTC, NoticeA12);
		NmkCevni.put(CatNMK.NMK_NSPC, NoticeA13); NmkCevni.put(CatNMK.NMK_NWSK, NoticeA14); NmkCevni.put(CatNMK.NMK_NSLC, NoticeA15); NmkCevni.put(CatNMK.NMK_NUPC, NoticeA16);
		NmkCevni.put(CatNMK.NMK_NSLB, NoticeA17); NmkCevni.put(CatNMK.NMK_NWBK, NoticeA20); NmkCevni.put(CatNMK.NMK_NHSC, NoticeA18); NmkCevni.put(CatNMK.NMK_NLBG, NoticeA19);
		NmkCevni.put(CatNMK.NMK_MVTL, NoticeB1a); NmkCevni.put(CatNMK.NMK_MVTR, NoticeB1b); NmkCevni.put(CatNMK.NMK_MVTP, NoticeB2a); NmkCevni.put(CatNMK.NMK_MVTS, NoticeB2b);
		NmkCevni.put(CatNMK.NMK_KPTP, NoticeB3a); NmkCevni.put(CatNMK.NMK_KPTS, NoticeB3b); NmkCevni.put(CatNMK.NMK_CSTP, NoticeB4a); NmkCevni.put(CatNMK.NMK_CSTS, NoticeB4b);
		NmkCevni.put(CatNMK.NMK_STOP, NoticeB5); NmkCevni.put(CatNMK.NMK_SPDL, NoticeB6); NmkCevni.put(CatNMK.NMK_SHRN, NoticeB7); NmkCevni.put(CatNMK.NMK_KPLO, NoticeB8);
		NmkCevni.put(CatNMK.NMK_GWJN, NoticeB9a); NmkCevni.put(CatNMK.NMK_GWCS, NoticeB9b); NmkCevni.put(CatNMK.NMK_MKRC, NoticeB11);
		NmkCevni.put(CatNMK.NMK_LMDP, NoticeC1); NmkCevni.put(CatNMK.NMK_LMHR, NoticeC2);	NmkCevni.put(CatNMK.NMK_LMWD, NoticeC3); NmkCevni.put(CatNMK.NMK_NAVR, NoticeC4);
		NmkCevni.put(CatNMK.NMK_CHDL, NoticeC5a); NmkCevni.put(CatNMK.NMK_CHDR, NoticeC5b);
		NmkCevni.put(CatNMK.NMK_CHTW, NoticeD1a); NmkCevni.put(CatNMK.NMK_CHOW, NoticeD1b); NmkCevni.put(CatNMK.NMK_OPTR, NoticeD2a); NmkCevni.put(CatNMK.NMK_OPTL, NoticeD2b);
		NmkCevni.put(CatNMK.NMK_PRTL, NoticeD3a); NmkCevni.put(CatNMK.NMK_PRTR, NoticeD3b);
		NmkCevni.put(CatNMK.NMK_ENTP, NoticeE1); NmkCevni.put(CatNMK.NMK_OVHC, NoticeE2);	NmkCevni.put(CatNMK.NMK_WEIR, NoticeE3); NmkCevni.put(CatNMK.NMK_FERN, NoticeE4a);
		NmkCevni.put(CatNMK.NMK_FERI, NoticeE4b); NmkCevni.put(CatNMK.NMK_BRTP, NoticeE5);	NmkCevni.put(CatNMK.NMK_BTLL, NoticeE5_1); NmkCevni.put(CatNMK.NMK_BTLS, NoticeE5_2);
		NmkCevni.put(CatNMK.NMK_BTRL, NoticeE5_3); NmkCevni.put(CatNMK.NMK_BTUP, NoticeE5_4);	NmkCevni.put(CatNMK.NMK_BTP1, NoticeE5_5); NmkCevni.put(CatNMK.NMK_BTP2, NoticeE5_6);
		NmkCevni.put(CatNMK.NMK_BTP3, NoticeE5_7); NmkCevni.put(CatNMK.NMK_BTUN, NoticeE5_8);	NmkCevni.put(CatNMK.NMK_BTN1, NoticeE5_9); NmkCevni.put(CatNMK.NMK_BTN2, NoticeE5_10);
		NmkCevni.put(CatNMK.NMK_BTN3, NoticeE5_11); NmkCevni.put(CatNMK.NMK_BTUM, NoticeE5_12);	NmkCevni.put(CatNMK.NMK_BTU1, NoticeE5_13); NmkCevni.put(CatNMK.NMK_BTU2, NoticeE5_14);
		NmkCevni.put(CatNMK.NMK_BTU3, NoticeE5_15); NmkCevni.put(CatNMK.NMK_ANKP, NoticeE6);	NmkCevni.put(CatNMK.NMK_MORP, NoticeE7); NmkCevni.put(CatNMK.NMK_VLBT, NoticeE7_1);
		NmkCevni.put(CatNMK.NMK_TRNA, NoticeE8); NmkCevni.put(CatNMK.NMK_SWWC, NoticeE9a);	NmkCevni.put(CatNMK.NMK_SWWR, NoticeE9b); NmkCevni.put(CatNMK.NMK_SWWL, NoticeE9c);
		NmkCevni.put(CatNMK.NMK_WRSA, NoticeE9d); NmkCevni.put(CatNMK.NMK_WLSA, NoticeE9e);	NmkCevni.put(CatNMK.NMK_WRSL, NoticeE9f); NmkCevni.put(CatNMK.NMK_WLSR, NoticeE9g);
		NmkCevni.put(CatNMK.NMK_WRAL, NoticeE9h); NmkCevni.put(CatNMK.NMK_WLAR, NoticeE9i);	NmkCevni.put(CatNMK.NMK_MWWC, NoticeE10a); NmkCevni.put(CatNMK.NMK_MWWJ, NoticeE10b);
		NmkCevni.put(CatNMK.NMK_MWAR, NoticeE10c); NmkCevni.put(CatNMK.NMK_MWAL, NoticeE10d);	NmkCevni.put(CatNMK.NMK_WARL, NoticeE10e); NmkCevni.put(CatNMK.NMK_WALR, NoticeE10f);
		NmkCevni.put(CatNMK.NMK_PEND, NoticeE11); NmkCevni.put(CatNMK.NMK_DWTR, NoticeE13);	NmkCevni.put(CatNMK.NMK_TELE, NoticeE14); NmkCevni.put(CatNMK.NMK_MTCP, NoticeE15);
		NmkCevni.put(CatNMK.NMK_SPCP, NoticeE16); NmkCevni.put(CatNMK.NMK_WSKP, NoticeE17);	NmkCevni.put(CatNMK.NMK_SLCP, NoticeE18); NmkCevni.put(CatNMK.NMK_UPCP, NoticeE19);
		NmkCevni.put(CatNMK.NMK_SLBP, NoticeE20); NmkCevni.put(CatNMK.NMK_RADI, NoticeE23);	NmkCevni.put(CatNMK.NMK_WTBP, NoticeE24); NmkCevni.put(CatNMK.NMK_HSCP, NoticeE21);
		NmkCevni.put(CatNMK.NMK_LBGP, NoticeE22);
  };

	public static final Symbol NoticeBnank = new Symbol();
	static {
		NoticeBnank.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		Symbol colours = new Symbol();
		Path2D.Double p = new Path2D.Double(); p.moveTo(0.0,-80.0); p.lineTo(-15.0,-47.0); p.lineTo(15.0,-47.0); p.closePath();
		colours.add(new Instr(Form.P1, p));
		p = new Path2D.Double(); p.moveTo(0.0,-10.0); p.lineTo(-15.0,-43.0); p.lineTo(15.0,-43.0); p.closePath();
		colours.add(new Instr(Form.P2, p));
		NoticeBnank.add(new Instr(Form.COLR, colours));
	}
	public static final Symbol NoticeBlmhr = new Symbol();
	public static final Symbol NoticeBwral = new Symbol();
	public static final Symbol NoticeBwrar = new Symbol();
	public static final Symbol NoticeBktpm = new Symbol();
	public static final Symbol NoticeBktsm = new Symbol();
	public static final Symbol NoticeBktmr = new Symbol();
	public static final Symbol NoticeBcrtp = new Symbol();
	public static final Symbol NoticeBcrts = new Symbol();
	public static final Symbol NoticeBtrbm = new Symbol();
	public static final Symbol NoticeBrspd = new Symbol();
	public static final EnumMap<CatNMK, Symbol> NmkBniwr = new EnumMap<CatNMK, Symbol>(CatNMK.class);
	static {
		NmkBniwr.put(CatNMK.NMK_NANK, NoticeBnank); NmkBniwr.put(CatNMK.NMK_LMHR, NoticeBlmhr); NmkBniwr.put(CatNMK.NMK_OPTR, NoticeD2a); NmkBniwr.put(CatNMK.NMK_OPTL, NoticeD2b);
		NmkBniwr.put(CatNMK.NMK_WRAL, NoticeBwral); NmkBniwr.put(CatNMK.NMK_WLAR, NoticeBwrar); NmkBniwr.put(CatNMK.NMK_KTPM, NoticeBktpm); NmkBniwr.put(CatNMK.NMK_KTSM, NoticeBktsm);
		NmkBniwr.put(CatNMK.NMK_KTMR, NoticeBktmr); NmkBniwr.put(CatNMK.NMK_CRTP, NoticeBcrtp); NmkBniwr.put(CatNMK.NMK_CRTS, NoticeBcrts); NmkBniwr.put(CatNMK.NMK_TRBM, NoticeBtrbm);
		NmkBniwr.put(CatNMK.NMK_RSPD, NoticeBrspd);
	}

	public static final EnumMap<CatNMK, Symbol> NmkPpwbc = new EnumMap<CatNMK, Symbol>(CatNMK.class);
	static {
		NmkPpwbc.put(CatNMK.NMK_WRAL, Notice); NmkPpwbc.put(CatNMK.NMK_WLAR, Notice); NmkPpwbc.put(CatNMK.NMK_KTPM, Notice); NmkPpwbc.put(CatNMK.NMK_KTSM, Notice);
		NmkPpwbc.put(CatNMK.NMK_KTMR, Notice); NmkPpwbc.put(CatNMK.NMK_CRTP, Notice); NmkPpwbc.put(CatNMK.NMK_CRTS, Notice);
	}
	
	public static Scheme getScheme(MarSYS sys, BnkWTW bank) {
		ArrayList<Color> colours = new ArrayList<Color>();
		Scheme scheme = new Scheme(null, colours);
		switch (sys) {
		case SYS_BNWR:
			switch (bank) {
			case BWW_LEFT:
				colours.add(Color.white);
				colours.add(new Color(0xf00000));
				break;
			case BWW_RGHT:
				colours.add(Color.white);
				colours.add(new Color(0x00a000));
				break;
			default:
				colours.add(new Color(0xff8040));
				colours.add(Color.black);
				break;
			}
			break;
		case SYS_PPWB:
			switch (bank) {
			case BWW_LEFT:
				colours.add(Color.white);
				colours.add(new Color(0xd40000));
				break;
			case BWW_RGHT:
				colours.add(Color.white);
				colours.add(new Color(0x00d400));
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		return scheme;
	}
	
	public static Symbol getNotice(CatNMK cat, MarSYS sys) {
		Symbol symbol = null;
		switch (sys) {
		case SYS_CEVN:
			symbol = NmkCevni.get(cat);
			break;
		case SYS_BNWR:
			symbol = NmkBniwr.get(cat);
			break;
		case SYS_PPWB:
			symbol = NmkPpwbc.get(cat);
			break;
		default:
			break;
		}
		return symbol;
	}
}
