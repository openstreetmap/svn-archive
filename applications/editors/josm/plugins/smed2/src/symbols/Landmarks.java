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
	private static final ArrayList<Instr> Base = new ArrayList<Instr>();
	static {
		Base.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Base.add(new Instr(Prim.FILL, Color.black));
		Base.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-10,-10,20,20)));
		Base.add(new Instr(Prim.LINE, new Line2D.Double(-35,0,-10,0)));
		Base.add(new Instr(Prim.LINE, new Line2D.Double(10,0,35,0)));
	}
	
	public static final ArrayList<Instr> Chimney = new ArrayList<Instr>();
	static {
		Chimney.add(new Instr(Prim.SYMB, Landmarks.Base));
		Base.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Chimney.add(new Instr(Prim.FILL, Color.black));
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
	static {
		Cross.add(new Instr(Prim.SYMB, Landmarks.Base));
		Cross.add(new Instr(Prim.STRK, new BasicStroke(6.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Cross.add(new Instr(Prim.FILL, Color.black));
		Cross.add(new Instr(Prim.LINE, new Line2D.Double(0,-10,0,-150)));
		Cross.add(new Instr(Prim.LINE, new Line2D.Double(-30,-115,30,-115)));
	}
	public static final ArrayList<Instr> DishAerial = new ArrayList<Instr>();
	static {
		DishAerial.add(new Instr(Prim.SYMB, Landmarks.Base));
		DishAerial.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)));
		DishAerial.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-7.8,-6.0); p.lineTo(0.0,-62.0); p.lineTo(7.8,-6.0); p.moveTo(18.0,-109.0); p.lineTo(25.0,-113.0);
		p.moveTo(-9.5,-157.0); p.curveTo(-60.7,-125.5,-16.5,-33.9,44.9,-61.7); p.closePath();
		DishAerial.add(new Instr(Prim.PLIN, p));
	}
	public static final ArrayList<Instr> Dome = new ArrayList<Instr>();
	static {
		Dome.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Dome.add(new Instr(Prim.FILL, Color.black));
		Dome.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-36,-36,72,72)));
		Dome.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-2,-2,4,4)));
	}
	public static final ArrayList<Instr> Flagstaff = new ArrayList<Instr>();
	static {
		Flagstaff.add(new Instr(Prim.SYMB, Landmarks.Base));
		Flagstaff.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Flagstaff.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(0.0,-10.0); p.lineTo(0.0,-150.0); p.moveTo(0.0,-140.0); p.lineTo(40.0,-140.0); p.lineTo(40.0,-100.0); p.lineTo(0.0,-100.0);
		Flagstaff.add(new Instr(Prim.PLIN, p));
	}
	public static final ArrayList<Instr> FlareStack = new ArrayList<Instr>();
	static {
		FlareStack.add(new Instr(Prim.SYMB, Landmarks.Base));
		FlareStack.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		FlareStack.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-7.8,-6.0); p.lineTo(-7.8,-100.0); p.lineTo(7.8,-100.0); p.lineTo(7.8,-6.0);
		FlareStack.add(new Instr(Prim.PLIN, p));
		FlareStack.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		p = new Path2D.Double(); p.moveTo(21.6,-169.6); p.curveTo(-22.0,-132.4,-27.4,-103.5,3.0,-100.0); p.curveTo(39.0,-118.0,-4.0,-141.0,21.6,-169.6);
		FlareStack.add(new Instr(Prim.PLIN, p));
	}
	public static final ArrayList<Instr> LandTower = new ArrayList<Instr>();
	static {
		LandTower.add(new Instr(Prim.SYMB, Landmarks.Base));
		LandTower.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		LandTower.add(new Instr(Prim.FILL, Color.black));
		LandTower.add(new Instr(Prim.LINE, new Line2D.Double(-25,0,-15,-120)));
		LandTower.add(new Instr(Prim.LINE, new Line2D.Double(25,0,15,-120)));
		LandTower.add(new Instr(Prim.RECT, new Rectangle2D.Double(-15,-150,30,30)));
	}
	public static final ArrayList<Instr> Mast = new ArrayList<Instr>();
	static {
		Mast.add(new Instr(Prim.SYMB, Landmarks.Base));
		Mast.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)));
		Mast.add(new Instr(Prim.FILL, Color.black));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-25.0,0.0); p.lineTo(0.0,-150.0); p.lineTo(25.0,0.0);
		Mast.add(new Instr(Prim.PLIN, p));
	}
	public static final ArrayList<Instr> RadioMast = new ArrayList<Instr>();
	static {
		RadioMast.add(new Instr(Prim.SYMB, Landmarks.Mast));
		RadioMast.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)));
		RadioMast.add(new Instr(Prim.FILL, Color.black));
		RadioMast.add(new Instr(Prim.EARC, new Arc2D.Double(-30.0,-180.0,60.0,60.0,45.0,-90.0,Arc2D.OPEN)));
		RadioMast.add(new Instr(Prim.EARC, new Arc2D.Double(-45.0,-195.0,90.0,90.0,45.0,-90.0,Arc2D.OPEN)));
		RadioMast.add(new Instr(Prim.EARC, new Arc2D.Double(-30.0,-180.0,60.0,60.0,225.0,-90.0,Arc2D.OPEN)));
		RadioMast.add(new Instr(Prim.EARC, new Arc2D.Double(-45.0,-195.0,90.0,90.0,225.0,-90.0,Arc2D.OPEN)));
	}
	public static final ArrayList<Instr> Monument = new ArrayList<Instr>();
	static {
		Monument.add(new Instr(Prim.SYMB, Landmarks.Base));
		Monument.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		Monument.add(new Instr(Prim.FILL, Color.black));
		Monument.add(new Instr(Prim.LINE, new Line2D.Double(-25,0,-15,-105)));
		Monument.add(new Instr(Prim.LINE, new Line2D.Double(25,0,15,-105)));
		Monument.add(new Instr(Prim.EARC, new Arc2D.Double(-25.0,-150.0,50.0,50.0,233.0,-285.0,Arc2D.OPEN)));
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
	public static final ArrayList<Instr> Minaret = new ArrayList<Instr>();
	static {
		Minaret.add(new Instr(Prim.SYMB, Landmarks.Spire));
		Minaret.add(new Instr(Prim.STRK, new BasicStroke(6.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Minaret.add(new Instr(Prim.LINE, new Line2D.Double(0,-25,0,-50)));
		Minaret.add(new Instr(Prim.STRK, new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		Minaret.add(new Instr(Prim.EARC, new Arc2D.Double(-40.0,-110.0,80.0,60.0,180.0,180.0,Arc2D.OPEN)));
	}
	public static final ArrayList<Instr> Temple = new ArrayList<Instr>();
	static {
		Temple.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Temple.add(new Instr(Prim.RECT, new Rectangle2D.Double(-25,-15,50,30)));
		Temple.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Temple.add(new Instr(Prim.LINE, new Line2D.Double(-35,-21,35,21)));
		Temple.add(new Instr(Prim.LINE, new Line2D.Double(-35,21,35,-21)));
	}
	public static final ArrayList<Instr> WaterTower = new ArrayList<Instr>();
	static {
		WaterTower.add(new Instr(Prim.SYMB, Landmarks.Base));
		WaterTower.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		WaterTower.add(new Instr(Prim.FILL, Color.black));
		WaterTower.add(new Instr(Prim.LINE, new Line2D.Double(-25,0,-15,-120)));
		WaterTower.add(new Instr(Prim.LINE, new Line2D.Double(25,0,15,-120)));
		WaterTower.add(new Instr(Prim.RECT, new Rectangle2D.Double(-25,-150,50,30)));
	}
	public static final ArrayList<Instr> WindMotor = new ArrayList<Instr>();
	static {
		WindMotor.add(new Instr(Prim.SYMB, Landmarks.Base));
		WindMotor.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		WindMotor.add(new Instr(Prim.FILL, Color.black));
		WindMotor.add(new Instr(Prim.LINE, new Line2D.Double(0,-10,0,-90)));
		WindMotor.add(new Instr(Prim.LINE, new Line2D.Double(0,-90,30,-90)));
		WindMotor.add(new Instr(Prim.LINE, new Line2D.Double(0,-90,-14,-116.6)));
		WindMotor.add(new Instr(Prim.LINE, new Line2D.Double(0,-90,-14.3,-66.7)));
	}
	public static final ArrayList<Instr> Windmill = new ArrayList<Instr>();
	static {
		Windmill.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Windmill.add(new Instr(Prim.FILL, Color.black));
		Windmill.add(new Instr(Prim.ELPS, new Ellipse2D.Double(-12,-12,24,24)));
		Windmill.add(new Instr(Prim.LINE, new Line2D.Double(-30,-42,30,10)));
		Windmill.add(new Instr(Prim.LINE, new Line2D.Double(-30,10,30,-42)));
	}
	public static final ArrayList<Instr> Windsock = new ArrayList<Instr>();
	static {
		Windsock.add(new Instr(Prim.SYMB, Landmarks.Base));
		Windsock.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		Windsock.add(new Instr(Prim.FILL, Color.black));
		Windsock.add(new Instr(Prim.LINE, new Line2D.Double(0,-10,0,-100)));
		Windsock.add(new Instr(Prim.STRK, new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		Windsock.add(new Instr(Prim.LINE, new Line2D.Double(0,-100,0,-150)));
		Windsock.add(new Instr(Prim.STRK, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(0.0,-100.0); p.lineTo(10.0,-100.0); p.lineTo(10.0,-150.0); p.lineTo(0.0,-150.0);
		p.moveTo(10.0,-150.0); p.lineTo(50.0,-145.0); p.lineTo(120.0,-70.0); p.quadTo(120.0,-55.0,105.0,-55.0);
		p.lineTo(55,-95); p.lineTo(40,-102); p.lineTo(10,-100); p.moveTo(40,-102); p.lineTo(50,-120); p.moveTo(55,-95); p.lineTo(75,-97);
		Windsock.add(new Instr(Prim.PLIN, p));
	}
}
