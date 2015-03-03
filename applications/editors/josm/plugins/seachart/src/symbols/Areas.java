/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package symbols;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

import symbols.Symbols.*;

public class Areas {
	public static final Symbol Plane = new Symbol();
	static {
		Plane.add(new Instr(Form.BBOX, new Rectangle2D.Double(-60,-60,120,120)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(40,20); p.lineTo(50,10); p.lineTo(27.0,13.3); p.lineTo(23.7,6.8); p.lineTo(40.0,5.0); p.curveTo(55,4,55,-9,40,-10);
		p.quadTo(31,-11,30,-15); p.lineTo(-30,2); p.quadTo(-35,-12,-45,-15); p.quadTo(-56,-3,-50,15); p.lineTo(18.4,7.3); p.lineTo(21.7,14); p.lineTo(-20,20); p.closePath();
		Plane.add(new Instr(Form.PGON, p));
	}
	public static final Symbol Cable = new Symbol();
	static {
		Cable.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-60,60,60)));
		Cable.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Cable.add(new Instr(Form.FILL, new Color(0xc480ff)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(0,0); p.curveTo(-13,-13,-13,-17,0,-30); p.curveTo(13,-43,13,-47,0,-60);
		Cable.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol CableDot = new Symbol();
	static {
		CableDot.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-60,60,60)));
		CableDot.add(new Instr(Form.RSHP, new Ellipse2D.Double(-10,-40,20,20)));
	}
	public static final Symbol CableDash = new Symbol();
	static {
		CableDash.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-60,60,60)));
		CableDash.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		CableDash.add(new Instr(Form.LINE, new Line2D.Double(0,-15,0,-45)));
	}
	public static final Symbol CableFlash = new Symbol();
	static {
		CableFlash.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-60,60,60)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-30,-25); p.lineTo(-10,-40); p.lineTo(10,-26); p.lineTo(30,-35); p.lineTo(10,-20); p.lineTo(-10,-34); p.closePath();
		CableFlash.add(new Instr(Form.PGON, p));
	}
	public static final Symbol LaneArrow = new Symbol();
	static {
		LaneArrow.add(new Instr(Form.BBOX, new Rectangle2D.Double(-20,-240,40,240)));
		LaneArrow.add(new Instr(Form.STRK, new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		LaneArrow.add(new Instr(Form.FILL, Symbols.Mline));
		Path2D.Double p = new Path2D.Double(); p.moveTo(15,0); p.lineTo(15,-195); p.lineTo(40,-195);
		p.lineTo(0,-240); p.lineTo(-40,-195); p.lineTo(-15,-195); p.lineTo(-15,0); p.closePath();
		LaneArrow.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol LineAnchor = new Symbol();
	static {
		LineAnchor.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-60,60,90)));
		LineAnchor.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Harbours.Anchor, 0.5, 0, 0, null, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(-90.0))))));
	}
	public static final Symbol LinePlane = new Symbol();
	static {
		LinePlane.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-60,60,90)));
		LinePlane.add(new Instr(Form.FILL, new Color(0xc480ff)));
		LinePlane.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Areas.Plane, 0.5, 0, 0, null, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(-90.0))))));
	}
	public static final Symbol MarineFarm = new Symbol();
	static {
		MarineFarm.add(new Instr(Form.STRK, new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-23,12); p.lineTo(-23,23); p.lineTo(23,23); p.lineTo(23,12); p.moveTo(-8,15); p.lineTo(-8,23); p.moveTo(8,15); p.lineTo(8,23);
		p.moveTo(-23,-12); p.lineTo(-23,-23); p.lineTo(23,-23); p.lineTo(23,-12); p.moveTo(-8,-15); p.lineTo(-8,-23); p.moveTo(8,-15); p.lineTo(8,-23);
		p.moveTo(-21,8); p.quadTo(-1,-14,21,0); p.quadTo(-1,14,-21,-8); p.moveTo(7,6); p.quadTo(2,0,7,-6);
		MarineFarm.add(new Instr(Form.PLIN, p));
		MarineFarm.add(new Instr(Form.RSHP, new Ellipse2D.Double(9,-2,4,4)));
	}
	public static final Symbol NoWake = new Symbol();
	static {
		NoWake.add(new Instr(Form.STRK, new BasicStroke(12, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoWake.add(new Instr(Form.FILL, new Color(0xa30075)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-60,20); p.curveTo(-28,20,-32,0,0,0); p.curveTo(32,0,28,20,60,20); p.moveTo(-60,0); p.curveTo(-28,0,-32,-20,0,-20); p.curveTo(32,-20,28,0,60,0);
		NoWake.add(new Instr(Form.PLIN, p));
		NoWake.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		NoWake.add(new Instr(Form.LINE, new Line2D.Double(-60,60,60,-60)));
		NoWake.add(new Instr(Form.LINE, new Line2D.Double(-60,-60,60,60)));
	}
	public static final Symbol Pipeline = new Symbol();
	static {
		Pipeline.add(new Instr(Form.BBOX, new Rectangle2D.Double(-15,-60,30,60)));
		Pipeline.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Pipeline.add(new Instr(Form.FILL, new Color(0xc480ff)));
		Pipeline.add(new Instr(Form.LINE, new Line2D.Double(0,0,0,-50)));
		Pipeline.add(new Instr(Form.RSHP, new Ellipse2D.Double(-10,-60,20,20)));
	}
	public static final Symbol Restricted = new Symbol();
	static {
		Restricted.add(new Instr(Form.BBOX, new Rectangle2D.Double(-15,-30,30,30)));
		Restricted.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Restricted.add(new Instr(Form.LINE, new Line2D.Double(0,0,0,-30)));
		Restricted.add(new Instr(Form.LINE, new Line2D.Double(0,-15,17,-15)));
	}
	public static final Symbol Rock = new Symbol();
	static {
		Rock.add(new Instr(Form.FILL, new Color(0x80c0ff)));
		Rock.add(new Instr(Form.RSHP, new Ellipse2D.Double(-30,-30,60,60)));
		Rock.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{5,5}, 0)));
		Rock.add(new Instr(Form.FILL, Color.black));
		Rock.add(new Instr(Form.ELPS, new Ellipse2D.Double(-30,-30,60,60)));
		Rock.add(new Instr(Form.STRK, new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Rock.add(new Instr(Form.LINE, new Line2D.Double(-20,0,20,0)));
		Rock.add(new Instr(Form.LINE, new Line2D.Double(0,-20,0,20)));
	}
	public static final Symbol RockA = new Symbol();
	static {
		RockA.add(new Instr(Form.FILL, new Color(0x80c0ff)));
		RockA.add(new Instr(Form.RSHP, new Ellipse2D.Double(-30,-30,60,60)));
		RockA.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{5,5}, 0)));
		RockA.add(new Instr(Form.FILL, Color.black));
		RockA.add(new Instr(Form.ELPS, new Ellipse2D.Double(-30,-30,60,60)));
		RockA.add(new Instr(Form.STRK, new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		RockA.add(new Instr(Form.LINE, new Line2D.Double(-20,0,20,0)));
		RockA.add(new Instr(Form.LINE, new Line2D.Double(0,-20,0,20)));
		RockA.add(new Instr(Form.RSHP, new Ellipse2D.Double(-17,-17,8,8)));
		RockA.add(new Instr(Form.RSHP, new Ellipse2D.Double(-17,9,8,8)));
		RockA.add(new Instr(Form.RSHP, new Ellipse2D.Double(9,-17,8,8)));
		RockA.add(new Instr(Form.RSHP, new Ellipse2D.Double(9,9,8,8)));
	}
	public static final Symbol RockC = new Symbol();
	static {
		RockC.add(new Instr(Form.FILL, new Color(0x80c0ff)));
		RockC.add(new Instr(Form.RSHP, new Ellipse2D.Double(-30,-30,60,60)));
		RockC.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{5,5}, 0)));
		RockC.add(new Instr(Form.FILL, Color.black));
		RockC.add(new Instr(Form.ELPS, new Ellipse2D.Double(-30,-30,60,60)));
		RockC.add(new Instr(Form.STRK, new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		RockC.add(new Instr(Form.LINE, new Line2D.Double(-20,0,20,0)));
		RockC.add(new Instr(Form.LINE, new Line2D.Double(-10,17.3,10,-17.3)));
		RockC.add(new Instr(Form.LINE, new Line2D.Double(10,17.3,-10,-17.3)));
	}
	public static final Symbol Seaplane = new Symbol();
	static {
		Seaplane.add(new Instr(Form.BBOX, new Rectangle2D.Double(-60,-60,120,120)));
		Seaplane.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Seaplane.add(new Instr(Form.ELPS, new Ellipse2D.Double(-58,-58,116,116)));
		Seaplane.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Areas.Plane, 1.0, 0, 0, null, null)));
	}
	public static final Symbol WindFarm = new Symbol();
	static {
		WindFarm.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		WindFarm.add(new Instr(Form.ELPS, new Ellipse2D.Double(-100,-100,200,200)));
		WindFarm.add(new Instr(Form.LINE, new Line2D.Double(-35,50,35,50)));
		WindFarm.add(new Instr(Form.LINE, new Line2D.Double(0,50,0,-27.5)));
		WindFarm.add(new Instr(Form.LINE, new Line2D.Double(0,-27.5,30,-27.5)));
		WindFarm.add(new Instr(Form.LINE, new Line2D.Double(0,-27.5,-13.8,-3.8)));
		WindFarm.add(new Instr(Form.LINE, new Line2D.Double(0,-27.5,-13.8,-53.6)));
	}
	public static final Symbol WreckD = new Symbol();
	static {
		WreckD.add(new Instr(Form.FILL, new Color(0x80c0ff)));
		WreckD.add(new Instr(Form.RSHP, new Ellipse2D.Double(-50,-40,100,80)));
		WreckD.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{5,5}, 0)));
		WreckD.add(new Instr(Form.FILL, Color.black));
		WreckD.add(new Instr(Form.ELPS, new Ellipse2D.Double(-50,-40,100,80)));
		WreckD.add(new Instr(Form.STRK, new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		WreckD.add(new Instr(Form.LINE, new Line2D.Double(-40,0,40,0)));
		WreckD.add(new Instr(Form.LINE, new Line2D.Double(0,-30,0,30)));
		WreckD.add(new Instr(Form.LINE, new Line2D.Double(-20,-15,-20,15)));
		WreckD.add(new Instr(Form.LINE, new Line2D.Double(20,-15,20,15)));
	}
	public static final Symbol WreckND = new Symbol();
	static {
		WreckND.add(new Instr(Form.STRK, new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		WreckND.add(new Instr(Form.LINE, new Line2D.Double(-40,0,40,0)));
		WreckND.add(new Instr(Form.LINE, new Line2D.Double(0,-30,0,30)));
		WreckND.add(new Instr(Form.LINE, new Line2D.Double(-20,-15,-20,15)));
		WreckND.add(new Instr(Form.LINE, new Line2D.Double(20,-15,20,15)));
	}
	public static final Symbol WreckS = new Symbol();
	static {
		WreckS.add(new Instr(Form.STRK, new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		WreckS.add(new Instr(Form.ELPS, new Ellipse2D.Double(-6,-6,12,12)));
		WreckS.add(new Instr(Form.LINE, new Line2D.Double(-40,0,-6,0)));
		WreckS.add(new Instr(Form.LINE, new Line2D.Double(40,0,6,0)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-30,0); p.lineTo(-40,-25); p.lineTo(-0.3,-12.6); p.lineTo(13.7,-37.7); p.lineTo(16.3,-36.3);
		p.lineTo(2.7,-11.6); p.lineTo(37.5,0); p.lineTo(6,0); p.curveTo(5.6,-8,-5.6,-8,-6,0); p.closePath();
		WreckS.add(new Instr(Form.PGON, p));
	}
	public static final BufferedImage Sandwaves = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
	static {
		Graphics2D g2 = Sandwaves.createGraphics();
		g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.setBackground(new Color(0, true));
		g2.clearRect(0,  0, 100, 100);
		g2.setPaint(new Color(0xffd400));
		Path2D.Double p = new Path2D.Double(); p.moveTo(0.0,34.5); p.lineTo(03.3,30.8); p.lineTo(09.9,19.3); p.lineTo(13.2,16.0); p.lineTo(16.5,16.1); p.lineTo(18.2,19.5);
		p.lineTo(19.9,25.0); p.lineTo(21.6,30.3); p.lineTo(23.3,33.4); p.lineTo(25.0,33.3); p.lineTo(28.3,30.1); p.lineTo(31.6,25.0); p.lineTo(34.9,20.1); p.lineTo(38.2,17.2);
		p.lineTo(41.5,17.3); p.lineTo(43.2,20.3); p.lineTo(44.9,25); p.lineTo(46.6,29.6); p.lineTo(48.3,32.2); p.lineTo(50.0,32.1);
		p.moveTo(50.0,84.5); p.lineTo(53.3,80.8); p.lineTo(56.6,75.0); p.lineTo(59.9,69.3); p.lineTo(63.2,66.0); p.lineTo(66.5,66.1); p.lineTo(68.2,69.5); p.lineTo(69.9,75.0);
		p.lineTo(71.6,80.3); p.lineTo(73.3,83.4); p.lineTo(75.0,83.3); p.lineTo(78.3,80.1); p.lineTo(81.6,75.0); p.lineTo(84.9,70.1); p.lineTo(88.2,67.2); p.lineTo(91.5,67.3);
		p.lineTo(93.2,70.3); p.lineTo(94.9,75.0); p.lineTo(96.6,79.6); p.lineTo(98.3,82.2); p.lineTo(100.0,82.1);
		g2.draw(p);
	}
}
