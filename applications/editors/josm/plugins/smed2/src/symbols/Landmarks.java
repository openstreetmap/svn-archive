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
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import symbols.Symbols.Instr;
import symbols.Symbols.Prim;

public class Landmarks {
	public static final ArrayList<Instr> Chimney = new ArrayList<Instr>();
	static {
		Chimney.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Chimney.add(new Instr(Prim.FILL, Color.black));
		Chimney.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-10,-10,20,20)));
		Chimney.add(new Instr(Prim.LINE, new Line2D.Double(-35,0,-10,0)));
		Chimney.add(new Instr(Prim.LINE, new Line2D.Double(10,0,35,0)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-25.0,0.0); p.lineTo(-10.0,-120.0); p.lineTo(10.0,-120.0); p.lineTo(25.0,0.0);
		p.moveTo(-10.0,-128.0); p.curveTo(-13.0,-147.0,15.0,-159.0,20.0,-148.0);
		p.moveTo(16.0,-152.3); p.curveTo(58.0,-194.0,98.0,-87.0,16.0,-132.0);
		p.moveTo(11.0,-128.0); p.curveTo(13.4,-132.0,20.0,-132.0,20.0,-136.0);
		Chimney.add(new Instr(Prim.PLIN, p));
	}
	public static final ArrayList<Instr> Church = new ArrayList<Instr>();
	static {
		Church.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(10.0,-10.0); p.lineTo(37.0,-10.0); p.quadTo(48.0,-10.0,48.0,-21.0); p.lineTo(50.0,-21.0); p.lineTo(50.0,21.0);
		p.lineTo(48.0,21.0); p.quadTo(48.0,10.0,37.0,10.0); p.lineTo(10.0,10.0); p.lineTo(10.0,37.0); p.quadTo(10.0,48.0,21.0,48.0); p.lineTo(21.0,50.0);
		p.lineTo(-21.0,50.0); p.lineTo(-21.0,48.0); p.quadTo(-10.0,48.0,-10.0,37.0); p.lineTo(-10.0,10.0); p.lineTo(-37.0,10.0); p.quadTo(-48.0,10.0,-48.0,21.0);
		p.lineTo(-50.0,21.0); p.lineTo(-50.0,-21.0); p.lineTo(-48.0,-21.0); p.quadTo(-48.0,-10.0,-37.0,-10.0); p.lineTo(-10.0,-10.0); p.lineTo(-10.0,-37.0);
		p.quadTo(-10.0,-48.0,-21.0,-48.0); p.lineTo(-21.0,-50.0); p.lineTo(21.0,-50.0); p.lineTo(21.0,-48.0); p.quadTo(10.0,-48.0,10.0,-37.0); p.closePath();
		Church.add(new Instr(Prim.PGON, p));
	}
	public static final ArrayList<Instr> ChurchTower = new ArrayList<Instr>();
	static {
		ChurchTower.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		ChurchTower.add(new Instr(Prim.FILL, Color.black));
		ChurchTower.add(new Instr(Prim.RECT, new Rectangle2D.Double(-36,-36,72,72)));
		ChurchTower.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-2,-2,4,4)));
	}
	public static final ArrayList<Instr> Cross = new ArrayList<Instr>();
	public static final ArrayList<Instr> DishAerial = new ArrayList<Instr>();
	public static final ArrayList<Instr> Dome = new ArrayList<Instr>();
	static {
		Dome.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Dome.add(new Instr(Prim.FILL, Color.black));
		Dome.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-36,-36,72,72)));
		Dome.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-2,-2,4,4)));
	}
	public static final ArrayList<Instr> Flagstaff = new ArrayList<Instr>();
	public static final ArrayList<Instr> FlareStack = new ArrayList<Instr>();
	public static final ArrayList<Instr> LandTower = new ArrayList<Instr>();
	public static final ArrayList<Instr> Mast = new ArrayList<Instr>();
	static {
		Mast.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)));
		Mast.add(new Instr(Prim.FILL, Color.black));
		Mast.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-10,-10,20,20)));
		Mast.add(new Instr(Prim.LINE, new Line2D.Double(-35,0,-10,0)));
		Mast.add(new Instr(Prim.LINE, new Line2D.Double(10,0,35,0)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-25.0,0.0); p.lineTo(0.0,-150.0); p.lineTo(25.0,0.0);
		Mast.add(new Instr(Prim.PLIN, p));
	}
	public static final ArrayList<Instr> Minaret = new ArrayList<Instr>();
	public static final ArrayList<Instr> Monument = new ArrayList<Instr>();
	public static final ArrayList<Instr> RadioMast = new ArrayList<Instr>();
	static {
		RadioMast.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)));
		RadioMast.add(new Instr(Prim.FILL, Color.black));
		RadioMast.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-10,-10,20,20)));
		RadioMast.add(new Instr(Prim.LINE, new Line2D.Double(-35,0,-10,0)));
		RadioMast.add(new Instr(Prim.LINE, new Line2D.Double(10,0,35,0)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-25.0,0.0); p.lineTo(0.0,-150.0); p.lineTo(25.0,0.0);
		RadioMast.add(new Instr(Prim.PLIN, p));
		RadioMast.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)));
		RadioMast.add(new Instr(Prim.EARC, new Arc2D.Double(-30.0,-180.0,60.0,60.0,45.0,-90.0,Arc2D.OPEN)));
		RadioMast.add(new Instr(Prim.EARC, new Arc2D.Double(-45.0,-195.0,90.0,90.0,45.0,-90.0,Arc2D.OPEN)));
		RadioMast.add(new Instr(Prim.EARC, new Arc2D.Double(-30.0,-180.0,60.0,60.0,225.0,-90.0,Arc2D.OPEN)));
		RadioMast.add(new Instr(Prim.EARC, new Arc2D.Double(-45.0,-195.0,90.0,90.0,225.0,-90.0,Arc2D.OPEN)));
	}
	public static final ArrayList<Instr> Platform = new ArrayList<Instr>();
	static {
		Platform.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Platform.add(new Instr(Prim.FILL, Color.black));
		Platform.add(new Instr(Prim.RECT, new Rectangle2D.Double(-48,-48,96,96)));
		Platform.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-2,-2,4,4)));
	}
	public static final ArrayList<Instr> Spire = new ArrayList<Instr>();
	static {
		Spire.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Spire.add(new Instr(Prim.FILL, Color.black));
		Spire.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-25,-25,50,50)));
		Spire.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-2,-2,4,4)));
	}
	public static final ArrayList<Instr> Temple = new ArrayList<Instr>();
	public static final ArrayList<Instr> WaterTower = new ArrayList<Instr>();
	public static final ArrayList<Instr> WindMotor = new ArrayList<Instr>();
	public static final ArrayList<Instr> Windmill = new ArrayList<Instr>();
	public static final ArrayList<Instr> Windsock = new ArrayList<Instr>();
}
