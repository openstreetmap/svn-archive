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
	public static final ArrayList<Instr> Cable = new ArrayList<Instr>();
	static {
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
		LineAnchor.add(new Instr(Prim.SYMB, new Symbols.Symbol(Harbours.Anchor, 0.5, 0, 0, new Delta(Handle.TC, AffineTransform.getRotateInstance(Math.toRadians(-90.0))), null)));
	}
	public static final ArrayList<Instr> LinePlane = new ArrayList<Instr>();
	public static final ArrayList<Instr> MarineFarm = new ArrayList<Instr>();
	public static final ArrayList<Instr> NoWake = new ArrayList<Instr>();
	public static final ArrayList<Instr> Pipeline = new ArrayList<Instr>();
	public static final ArrayList<Instr> Restricted = new ArrayList<Instr>();
	public static final ArrayList<Instr> Rock = new ArrayList<Instr>();
	public static final ArrayList<Instr> RockA = new ArrayList<Instr>();
	public static final ArrayList<Instr> RockC = new ArrayList<Instr>();
	public static final ArrayList<Instr> Sandwaves = new ArrayList<Instr>();
	public static final ArrayList<Instr> Seaplane = new ArrayList<Instr>();
	public static final ArrayList<Instr> WindFarm = new ArrayList<Instr>();
	public static final ArrayList<Instr> WreckD = new ArrayList<Instr>();
	public static final ArrayList<Instr> WreckND = new ArrayList<Instr>();
	public static final ArrayList<Instr> WreckS = new ArrayList<Instr>();
}
