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
import java.awt.geom.*;
import java.util.EnumMap;

import s57.S57val.*;
import symbols.Symbols.*;

public class Facilities {
	private static final Symbol Facility = new Symbol();
	static {
		Facility.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Facility.add(new Instr(Form.FILL, new Color(0x80ffffff, true)));
		RoundRectangle2D.Double s = new RoundRectangle2D.Double(-29,-29,58,58,15,15);
		Facility.add(new Instr(Form.RSHP, s));
		Facility.add(new Instr(Form.FILL, new Color(0xa30075)));
		Facility.add(new Instr(Form.RRCT, s));
	}
	public static final Symbol Boatlift = new Symbol();//was Crane
	static {
		Boatlift.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		Boatlift.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Facilities.Facility, 1.0, 0, 0, null, null)));
		Boatlift.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Boatlift.add(new Instr(Form.ELPS, new Ellipse2D.Double(-3.7,-19.7,12,12)));
		Boatlift.add(new Instr(Form.LINE, new Line2D.Double(2.3,-7.7,2.3,-2.0)));
		Boatlift.add(new Instr(Form.EARC, new Arc2D.Double(-10.0,-1.5,20,20,75.0,-260.0,Arc2D.OPEN)));
	}
	public static final Symbol Boatyard = new Symbol();
	static {
		Boatyard.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		Boatyard.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Facilities.Facility, 1.0, 0, 0, null, null)));
		Boatyard.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		Boatyard.add(new Instr(Form.LINE, new Line2D.Double(19,19,-8,-8)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-11.3,-11.3); p.lineTo(-10.5,-17.5); p.lineTo(-14.8,-21.9); p.lineTo(-11.3,-25.4); p.lineTo(-7.4,-21.5);
		p.curveTo(1.0,-11.5,-11.5,1.0,-21.5,-7.4); p.lineTo(-25.4,-11.3); p.lineTo(-21.9,-14.8); p.lineTo(-17.5,-10.5); p.closePath();
		Boatyard.add(new Instr(Form.PGON, p));
	}
	public static final Symbol Chandler = new Symbol();
	static {
		Chandler.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		Chandler.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Facilities.Facility, 1.0, 0, 0, null, null)));
		Chandler.add(new Instr(Form.STRK, new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Chandler.add(new Instr(Form.ELPS, new Ellipse2D.Double(14,7,10,10)));
		Chandler.add(new Instr(Form.LINE, new Line2D.Double(-23.0,12.0,14.0,12.0)));
		Chandler.add(new Instr(Form.LINE, new Line2D.Double(8.0,21.0,8.0,-8.6)));
		Chandler.add(new Instr(Form.LINE, new Line2D.Double(-16.0,21.0,-16.0,-8.6)));
		Chandler.add(new Instr(Form.EARC, new Arc2D.Double(-16.0,-20.5,24,24,0.0,180.0,Arc2D.OPEN)));
	}
	public static final Symbol Fuel = new Symbol();
	static {
		Fuel.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		Fuel.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Facilities.Facility, 1.0, 0, 0, null, null)));
		Fuel.add(new Instr(Form.STRK, new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Fuel.add(new Instr(Form.FILL, new Color(0xa30075)));
		Path2D.Double p = new Path2D.Double(); p.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(-15.6,22.1); p.lineTo(-15.6,-19.4); p.quadTo(-15.5,-22.7,-12.2,-22.8); p.lineTo(0.2,-22.8); p.quadTo(3.3,-22.7,3.4,-19.4); p.lineTo(3.4,22.1); p.closePath(); 
		p.moveTo(-12.8,-19.0); p.quadTo(-12.7,-19.9,-11.8,-20.0); p.lineTo(-0.4,-20.0); p.quadTo(0.5,-19.9,0.6,-19.0); p.lineTo(0.6,-9.4);
		p.quadTo(0.5,-8.5,-0.4,-8.4); p.lineTo(-11.8,-8.4); p.quadTo(-12.7,-8.3,-12.8,-9.4); p.closePath();
		Fuel.add(new Instr(Form.PGON, p));
		p = new Path2D.Double(); p.moveTo(3.0,-3.0); p.lineTo(7.0,-3.0); p.quadTo(9.4,-2.8,9.6,-0.4); p.lineTo(9.6,18.0); p.curveTo(10.1,23.2,18.4,21.5,17.4,17.2);
		p.lineTo(14.9,3.5); p.lineTo(15.1,-10.3); p.quadTo(14.9,-11.9,13.9,-13.1); p.lineTo(7.4,-19.6); p.moveTo(15.1,-7.4); p.lineTo(12.6,-7.4); p.quadTo(11.1,-7.4,11.1,-8.9); p.lineTo(11.1,-16.0);
		Fuel.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol Laundrette = new Symbol();
	static {
		Laundrette.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		Laundrette.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Facilities.Facility, 1.0, 0, 0, null, null)));
		Laundrette.add(new Instr(Form.STRK, new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Laundrette.add(new Instr(Form.FILL, new Color(0xa30075)));
		Laundrette.add(new Instr(Form.RECT, new Rectangle2D.Double(-15,-15,30,30)));
		Laundrette.add(new Instr(Form.ELPS, new Ellipse2D.Double(-10,-10,20,20)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-15,-15); p.lineTo(-15,-20); p.lineTo(15,-20); p.lineTo(15,-15);
		p.moveTo(-10,15); p.lineTo(-10,20); p.lineTo(10,20); p.lineTo(10,15);
		Laundrette.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol PumpOut = new Symbol();
	static {
		PumpOut.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		PumpOut.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Facilities.Facility, 1.0, 0, 0, null, null)));
		PumpOut.add(new Instr(Form.STRK, new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		PumpOut.add(new Instr(Form.FILL, new Color(0xa30075)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(3.9,-3.7); p.lineTo(-7.4,-3.7); p.lineTo(-12.0,2.0); p.lineTo(-22.7,2.0);
		p.lineTo(-11.8,14.9); p.lineTo(15.1,14.9); p.lineTo(21.9,10.2); p.lineTo(21.9,3.1); p.lineTo(13.5,3.1);
		PumpOut.add(new Instr(Form.PLIN, p));
		PumpOut.add(new Instr(Form.STRK, new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		p = new Path2D.Double(); p.moveTo(-2.5,3.0); p.lineTo(-2.5,-13.8); p.lineTo(6.9,-13.8); p.lineTo(6.9,-6.7); p.lineTo(14.5,-6.7);
		PumpOut.add(new Instr(Form.PLIN, p));
		p = new Path2D.Double(); p.moveTo(9.7,2.3); p.lineTo(9.7,10.3); p.lineTo(-4.1,10.3); p.lineTo(-4.1,2.3); p.closePath();
		PumpOut.add(new Instr(Form.PGON, p));
		p = new Path2D.Double(); p.moveTo(14.1,-10.6); p.lineTo(23.1,-6.7); p.lineTo(14.1,-2.8); p.closePath();
		PumpOut.add(new Instr(Form.PGON, p));
	}
	public static final Symbol SailingClub = new Symbol();
	static {
		SailingClub.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		SailingClub.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Facilities.Facility, 1.0, 0, 0, null, null)));
		SailingClub.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		SailingClub.add(new Instr(Form.FILL, new Color(0xa30075)));
		SailingClub.add(new Instr(Form.LINE, new Line2D.Double(-5,20,-5,-20)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-5,0); p.lineTo(20,-10); p.lineTo(-5,-20); p.closePath();
		SailingClub.add(new Instr(Form.PGON, p));
	}
	public static final Symbol Shower = new Symbol();
	static {
		Shower.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		Shower.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Facilities.Facility, 1.0, 0, 0, null, null)));
		Shower.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Shower.add(new Instr(Form.FILL, new Color(0xa30075)));
		Shower.add(new Instr(Form.LINE, new Line2D.Double(-4.8,-24.5,6.2,-13.5)));
		Shower.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Shower.add(new Instr(Form.LINE, new Line2D.Double(-18.1,-17.9,-6.1,-21.3)));
		Shower.add(new Instr(Form.LINE, new Line2D.Double(-13.9,-10.2,-3.9,-17.7)));
		Shower.add(new Instr(Form.LINE, new Line2D.Double(-7.8,-4.4,-0.5,-14.3)));
		Shower.add(new Instr(Form.LINE, new Line2D.Double(-0.2,-0.2,3.1,-12.1)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(11.1,24.6); p.lineTo(11.1,-16.0); p.curveTo(11.1,-22.7,3.4,-23.6,0.8,-19.3);
		Shower.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol Slipway = new Symbol();
	static {
		Slipway.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		Slipway.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Facilities.Facility, 1.0, 0, 0, null, null)));
		Slipway.add(new Instr(Form.STRK, new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Slipway.add(new Instr(Form.FILL, new Color(0xa30075)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-24.8,0.1); p.lineTo(-24.8,18.0); p.curveTo(-21.2,18.0,-22.2,16.7,-18.6,16.7); p.curveTo(-15.0,16.7,-16.0,18.0,-12.4,18.0);
		p.curveTo(-8.8,18.0,-9.8,16.7,-6.2,16.7); p.curveTo(-2.6,16.7,-3.6,18.0,0.0,18.0); p.curveTo(3.6,18.0,2.6,16.7,6.2,16.7); p.curveTo(9.8,16.7,8.8,18.0,12.4,18.0);
		p.curveTo(16.0,18.0,15.0,16.7,18.6,16.7); p.curveTo(22.2,16.7,21.2,18.0,24.8,18.0);	p.lineTo(24.8,13.6); p.closePath();
		Slipway.add(new Instr(Form.PGON, p));
		Slipway.add(new Instr(Form.ELPS, new Ellipse2D.Double(-1.65,-1.9,8,8)));
		Slipway.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Slipway.add(new Instr(Form.LINE, new Line2D.Double(-24.5,-8.3,-3.1,-2.4)));
		Slipway.add(new Instr(Form.LINE, new Line2D.Double(9.3,1.1,22.2,4.6)));
		p = new Path2D.Double(); p.moveTo(22.9,0.6); p.lineTo(25.0,-7.4); p.lineTo(-5.1,-15.8); p.lineTo(0.3,-19.6); p.lineTo(-1.6,-20.1); p.lineTo(-7.2,-16.2);
		p.lineTo(-17.1,-18.9); p.quadTo(-16.8,-11.4,-7.7,-7.7); p.closePath();
		Slipway.add(new Instr(Form.PGON, p));
	}
	public static final Symbol Toilet = new Symbol();
	static {
		Toilet.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		Toilet.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Facilities.Facility, 1.0, 0, 0, null, null)));
		Toilet.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		Toilet.add(new Instr(Form.FILL, new Color(0xa30075)));
		Toilet.add(new Instr(Form.LINE, new Line2D.Double(0,20,0,-20)));
		Toilet.add(new Instr(Form.RSHP, new Ellipse2D.Double(-18.5,-22.5,7.4,7.4)));
		Toilet.add(new Instr(Form.RSHP, new Ellipse2D.Double(11,-22.5,7.4,7.4)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-9.8,-12.2); p.lineTo(-4.8,2.7); p.lineTo(-7.3,3.9); p.lineTo(-10.6,-7.0); p.lineTo(-11.7,-6.4); p.lineTo(-7.6,9.0);
		p.lineTo(-11.3,9.0); p.lineTo(-11.6,22.1); p.lineTo(-13.9,22.1); p.lineTo(-14.2,9.0); p.lineTo(-15.8,9.0); p.lineTo(-16.2,22.1); p.lineTo(-18.4,22.1);
		p.lineTo(-18.8,9.0); p.lineTo(-22.3,9.0); p.lineTo(-18.2,-6.4); p.lineTo(-19.1,-7.0); p.lineTo(-22.9,3.9); p.lineTo(-25.1,2.7); p.lineTo(-19.9,-12.2); p.closePath();
		Toilet.add(new Instr(Form.PGON, p));
		p = new Path2D.Double(); p.moveTo(19.2,-12.2); p.lineTo(22.3,-10.1); p.lineTo(22.3,4.4); p.lineTo(20.2,4.4); p.lineTo(20.2,-7.3); p.lineTo(18.3,-7.3); p.lineTo(18.3,22.1);
		p.lineTo(15.8,22.1); p.lineTo(15.8,4.6); p.lineTo(13.8,4.6); p.lineTo(13.4,22.1); p.lineTo(11.0,22.1); p.lineTo(11.0,-7.3); p.lineTo(9.5,-7.3); p.lineTo(9.5,4.4); p.lineTo(6.8,4.4);
		p.lineTo(6.8,-10.1); p.lineTo(9.4,-12.2); p.closePath();
		Toilet.add(new Instr(Form.PGON, p));
	}
	public static final Symbol VisitorBerth = new Symbol();
	static {
		VisitorBerth.add(new Instr(Form.BBOX, new Rectangle2D.Double(-30,-30,60,60)));
		VisitorBerth.add(new Instr(Form.FILL, new Color(0xa30075)));
		VisitorBerth.add(new Instr(Form.RSHP, new Ellipse2D.Double(-25,-25,50,50)));
		VisitorBerth.add(new Instr(Form.FILL, Color.white));
		Path2D.Double p = new Path2D.Double(); p.moveTo(7.9,-13.6); p.lineTo(14.0,-13.6); p.lineTo(3.4,13.6); p.lineTo(-3.4,13.6);
		p.lineTo(-14.0,-13.6); p.lineTo(-7.9,-13.6); p.lineTo(0.0,8.7); p.closePath();
		VisitorBerth.add(new Instr(Form.PGON, p));
	}
	public static final Symbol VisitorMooring = new Symbol();
	static {
		VisitorMooring.add(new Instr(Form.FILL, new Color(0xa30075)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-25.0,0); p.curveTo(-32.0,-21.0,-14.0,-45.5,12.7,-37.9); p.curveTo(27.5,-33.8,37.8,-15.5,32.0,0.0);
		p.lineTo(8.0,0.0); p.curveTo(8.0,-11.0,-8.0,-11.0,-8.0,0.0); p.closePath();
		VisitorMooring.add(new Instr(Form.PGON, p));
    VisitorMooring.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
    VisitorMooring.add(new Instr(Form.ELPS, new Ellipse2D.Double(-10,-10,20,20)));
    VisitorMooring.add(new Instr(Form.LINE, new Line2D.Double(-33,0,-10,0)));
    VisitorMooring.add(new Instr(Form.LINE, new Line2D.Double(10,0,40,0)));
    VisitorMooring.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
    VisitorMooring.add(new Instr(Form.ELPS, new Ellipse2D.Double(6.5,-49.5,12,12)));
    VisitorMooring.add(new Instr(Form.FILL, Color.white));
		p = new Path2D.Double(); p.moveTo(8.3,-32.8); p.lineTo(12.5,-32.8); p.lineTo(5.4,-12.9); p.lineTo(1.0,-12.9); p.lineTo(-6.1,-32.8);
		p.lineTo(-1.9,-32.8); p.lineTo(3.2,-18.1); p.closePath();
		VisitorMooring.add(new Instr(Form.PGON, p));
	}

	public static final EnumMap<CatSCF, Symbol> Cats = new EnumMap<CatSCF, Symbol>(CatSCF.class);
	static {
		Cats.put(CatSCF.SCF_BHST, Boatlift); Cats.put(CatSCF.SCF_BTYD, Boatyard); Cats.put(CatSCF.SCF_CHDR, Chandler); Cats.put(CatSCF.SCF_FUEL, Fuel); Cats.put(CatSCF.SCF_LAUN, Laundrette);
		Cats.put(CatSCF.SCF_PMPO, PumpOut); Cats.put(CatSCF.SCF_CLUB, SailingClub); Cats.put(CatSCF.SCF_SHWR, Shower); Cats.put(CatSCF.SCF_SLPW, Slipway); Cats.put(CatSCF.SCF_WC, Toilet);
		Cats.put(CatSCF.SCF_VBTH, VisitorBerth); Cats.put(CatSCF.SCF_VMOR, VisitorMooring);
	}
}
