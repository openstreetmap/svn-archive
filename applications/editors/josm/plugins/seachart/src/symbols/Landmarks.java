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
import java.awt.geom.*;
import java.util.EnumMap;

import s57.S57val.*;
import symbols.Symbols.*;


public class Landmarks {
	private static final Symbol Base = new Symbol();
	static {
		Base.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Base.add(new Instr(Form.ELPS, new Ellipse2D.Double(-10,-10,20,20)));
		Base.add(new Instr(Form.LINE, new Line2D.Double(-35,0,-10,0)));
		Base.add(new Instr(Form.LINE, new Line2D.Double(10,0,35,0)));
	}
	
	public static final Symbol Chimney = new Symbol();
	static {
		Chimney.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Base, 1.0, 0, 0, null, null)));
		Chimney.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-25.0,0.0); p.lineTo(-10.0,-120.0); p.lineTo(10.0,-120.0); p.lineTo(25.0,0.0);
		p.moveTo(-10.0,-128.0); p.curveTo(-13.0,-147.0,15.0,-159.0,20.0,-148.0);
		p.moveTo(16.0,-152.3); p.curveTo(58.0,-194.0,98.0,-87.0,16.0,-132.0);
		p.moveTo(11.0,-128.0); p.curveTo(13.4,-132.0,20.0,-132.0,20.0,-136.0);
		Chimney.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol Church = new Symbol();
	static {
		Path2D.Double p = new Path2D.Double(); p.moveTo(10.0,-10.0); p.lineTo(37.0,-10.0); p.quadTo(48.0,-10.0,48.0,-21.0); p.lineTo(50.0,-21.0); p.lineTo(50.0,21.0);
		p.lineTo(48.0,21.0); p.quadTo(48.0,10.0,37.0,10.0); p.lineTo(10.0,10.0); p.lineTo(10.0,37.0); p.quadTo(10.0,48.0,21.0,48.0); p.lineTo(21.0,50.0);
		p.lineTo(-21.0,50.0); p.lineTo(-21.0,48.0); p.quadTo(-10.0,48.0,-10.0,37.0); p.lineTo(-10.0,10.0); p.lineTo(-37.0,10.0); p.quadTo(-48.0,10.0,-48.0,21.0);
		p.lineTo(-50.0,21.0); p.lineTo(-50.0,-21.0); p.lineTo(-48.0,-21.0); p.quadTo(-48.0,-10.0,-37.0,-10.0); p.lineTo(-10.0,-10.0); p.lineTo(-10.0,-37.0);
		p.quadTo(-10.0,-48.0,-21.0,-48.0); p.lineTo(-21.0,-50.0); p.lineTo(21.0,-50.0); p.lineTo(21.0,-48.0); p.quadTo(10.0,-48.0,10.0,-37.0); p.closePath();
		Church.add(new Instr(Form.PGON, p));
	}
	public static final Symbol ChurchTower = new Symbol();
	static {
		ChurchTower.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		ChurchTower.add(new Instr(Form.RECT, new Rectangle2D.Double(-36,-36,72,72)));
		ChurchTower.add(new Instr(Form.ELPS, new Ellipse2D.Double(-2,-2,4,4)));
	}
	public static final Symbol Cross = new Symbol();
	static {
		Cross.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Base, 1.0, 0, 0, null, null)));
		Cross.add(new Instr(Form.STRK, new BasicStroke(6, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Cross.add(new Instr(Form.LINE, new Line2D.Double(0,-10,0,-150)));
		Cross.add(new Instr(Form.LINE, new Line2D.Double(-30,-115,30,-115)));
	}
	public static final Symbol DishAerial = new Symbol();
	static {
		DishAerial.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Base, 1.0, 0, 0, null, null)));
		DishAerial.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-7.8,-6.0); p.lineTo(0.0,-62.0); p.lineTo(7.8,-6.0); p.moveTo(18.0,-109.0); p.lineTo(25.0,-113.0);
		p.moveTo(-9.5,-157.0); p.curveTo(-60.7,-125.5,-16.5,-33.9,44.9,-61.7); p.closePath();
		DishAerial.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol Dome = new Symbol();
	static {
		Dome.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Dome.add(new Instr(Form.ELPS, new Ellipse2D.Double(-36,-36,72,72)));
		Dome.add(new Instr(Form.RSHP, new Ellipse2D.Double(-4,-4,8,8)));
	}
	public static final Symbol Flagstaff = new Symbol();
	static {
		Flagstaff.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Base, 1.0, 0, 0, null, null)));
		Flagstaff.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(0.0,-10.0); p.lineTo(0.0,-150.0); p.moveTo(0.0,-140.0); p.lineTo(40.0,-140.0); p.lineTo(40.0,-100.0); p.lineTo(0.0,-100.0);
		Flagstaff.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol FlareStack = new Symbol();
	static {
		FlareStack.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Base, 1.0, 0, 0, null, null)));
		FlareStack.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-7.8,-6.0); p.lineTo(-7.8,-100.0); p.lineTo(7.8,-100.0); p.lineTo(7.8,-6.0);
		FlareStack.add(new Instr(Form.PLIN, p));
		FlareStack.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)));
		p = new Path2D.Double(); p.moveTo(21.6,-169.6); p.curveTo(-22.0,-132.4,-27.4,-103.5,3.0,-100.0); p.curveTo(39.0,-118.0,-4.0,-141.0,21.6,-169.6);
		FlareStack.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol LandTower = new Symbol();
	static {
		LandTower.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Base, 1.0, 0, 0, null, null)));
		LandTower.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		LandTower.add(new Instr(Form.LINE, new Line2D.Double(-25,0,-15,-120)));
		LandTower.add(new Instr(Form.LINE, new Line2D.Double(25,0,15,-120)));
		LandTower.add(new Instr(Form.RECT, new Rectangle2D.Double(-15,-150,30,30)));
	}
	public static final Symbol Mast = new Symbol();
	static {
		Mast.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Base, 1.0, 0, 0, null, null)));
		Mast.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(-25.0,0.0); p.lineTo(0.0,-150.0); p.lineTo(25.0,0.0);
		Mast.add(new Instr(Form.PLIN, p));
	}
	public static final Symbol Monument = new Symbol();
	static {
		Monument.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Base, 1.0, 0, 0, null, null)));
		Monument.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		Monument.add(new Instr(Form.LINE, new Line2D.Double(-25,0,-15,-105)));
		Monument.add(new Instr(Form.LINE, new Line2D.Double(25,0,15,-105)));
		Monument.add(new Instr(Form.EARC, new Arc2D.Double(-25.0,-150.0,50.0,50.0,233.0,-285.0,Arc2D.OPEN)));
	}
	public static final Symbol Platform = new Symbol();
	static {
		Platform.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Platform.add(new Instr(Form.RECT, new Rectangle2D.Double(-48,-48,96,96)));
		Platform.add(new Instr(Form.RSHP, new Ellipse2D.Double(-4,-4,8,8)));
	}
	public static final Symbol RadioTV = new Symbol();
	static {
		RadioTV.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)));
		RadioTV.add(new Instr(Form.EARC, new Arc2D.Double(-30.0,-180.0,60.0,60.0,45.0,-90.0,Arc2D.OPEN)));
		RadioTV.add(new Instr(Form.EARC, new Arc2D.Double(-45.0,-195.0,90.0,90.0,45.0,-90.0,Arc2D.OPEN)));
		RadioTV.add(new Instr(Form.EARC, new Arc2D.Double(-30.0,-180.0,60.0,60.0,225.0,-90.0,Arc2D.OPEN)));
		RadioTV.add(new Instr(Form.EARC, new Arc2D.Double(-45.0,-195.0,90.0,90.0,225.0,-90.0,Arc2D.OPEN)));
	}
	public static final Symbol Spire = new Symbol();
	static {
		Spire.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Spire.add(new Instr(Form.ELPS, new Ellipse2D.Double(-25,-25,50,50)));
		Spire.add(new Instr(Form.RSHP, new Ellipse2D.Double(-4,-4,8,8)));
	}
	public static final Symbol Minaret = new Symbol();
	static {
		Minaret.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Spire, 1.0, 0, 0, null, null)));
		Minaret.add(new Instr(Form.STRK, new BasicStroke(6, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Minaret.add(new Instr(Form.LINE, new Line2D.Double(0,-25,0,-50)));
		Minaret.add(new Instr(Form.STRK, new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		Minaret.add(new Instr(Form.EARC, new Arc2D.Double(-40.0,-110.0,80.0,60.0,180.0,180.0,Arc2D.OPEN)));
	}
	public static final Symbol Temple = new Symbol();
	static {
		Temple.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Temple.add(new Instr(Form.RECT, new Rectangle2D.Double(-25,-15,50,30)));
		Temple.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Temple.add(new Instr(Form.LINE, new Line2D.Double(-35,-21,35,21)));
		Temple.add(new Instr(Form.LINE, new Line2D.Double(-35,21,35,-21)));
	}
	public static final Symbol WaterTower = new Symbol();
	static {
		WaterTower.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Base, 1.0, 0, 0, null, null)));
		WaterTower.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		WaterTower.add(new Instr(Form.LINE, new Line2D.Double(-25,0,-15,-120)));
		WaterTower.add(new Instr(Form.LINE, new Line2D.Double(25,0,15,-120)));
		WaterTower.add(new Instr(Form.RECT, new Rectangle2D.Double(-25,-150,50,30)));
	}
	public static final Symbol WindMotor = new Symbol();
	static {
		WindMotor.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Base, 1.0, 0, 0, null, null)));
		WindMotor.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		WindMotor.add(new Instr(Form.LINE, new Line2D.Double(0,-10,0,-90)));
		WindMotor.add(new Instr(Form.LINE, new Line2D.Double(0,-90,30,-90)));
		WindMotor.add(new Instr(Form.LINE, new Line2D.Double(0,-90,-14,-116.6)));
		WindMotor.add(new Instr(Form.LINE, new Line2D.Double(0,-90,-14.3,-66.7)));
	}
	public static final Symbol Windmill = new Symbol();
	static {
		Windmill.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		Windmill.add(new Instr(Form.ELPS, new Ellipse2D.Double(-12,-12,24,24)));
		Windmill.add(new Instr(Form.LINE, new Line2D.Double(-30,-42,30,10)));
		Windmill.add(new Instr(Form.LINE, new Line2D.Double(-30,10,30,-42)));
	}
	public static final Symbol Windsock = new Symbol();
	static {
		Windsock.add(new Instr(Form.SYMB, new Symbols.SubSymbol(Landmarks.Base, 1.0, 0, 0, null, null)));
		Windsock.add(new Instr(Form.STRK, new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		Windsock.add(new Instr(Form.LINE, new Line2D.Double(0,-10,0,-100)));
		Windsock.add(new Instr(Form.STRK, new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)));
		Windsock.add(new Instr(Form.LINE, new Line2D.Double(0,-100,0,-150)));
		Windsock.add(new Instr(Form.STRK, new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
		Path2D.Double p = new Path2D.Double(); p.moveTo(0.0,-100.0); p.lineTo(10.0,-100.0); p.lineTo(10.0,-150.0); p.lineTo(0.0,-150.0);
		p.moveTo(10.0,-150.0); p.lineTo(50.0,-145.0); p.lineTo(120.0,-70.0); p.quadTo(120.0,-55.0,105.0,-55.0);
		p.lineTo(55,-95); p.lineTo(40,-102); p.lineTo(10,-100); p.moveTo(40,-102); p.lineTo(50,-120); p.moveTo(55,-95); p.lineTo(75,-97);
		Windsock.add(new Instr(Form.PLIN, p));
	}
	
	public static final EnumMap<CatLMK, Symbol> Shapes = new EnumMap<CatLMK, Symbol>(CatLMK.class);
	static {
		Shapes.put(CatLMK.LMK_CARN, Beacons.Cairn); Shapes.put(CatLMK.LMK_CHMY, Landmarks.Chimney);
		Shapes.put(CatLMK.LMK_DISH, Landmarks.DishAerial); Shapes.put(CatLMK.LMK_FLAG, Landmarks.Flagstaff); Shapes.put(CatLMK.LMK_FLAR, Landmarks.FlareStack);
		Shapes.put(CatLMK.LMK_MAST, Landmarks.Mast); Shapes.put(CatLMK.LMK_WNDS, Landmarks.Windsock); Shapes.put(CatLMK.LMK_MNMT, Landmarks.Monument);
		Shapes.put(CatLMK.LMK_CLMN, Landmarks.Monument); Shapes.put(CatLMK.LMK_MEML, Landmarks.Monument); Shapes.put(CatLMK.LMK_OBLK, Landmarks.Monument);
		Shapes.put(CatLMK.LMK_STAT, Landmarks.Monument); Shapes.put(CatLMK.LMK_CROS, Landmarks.Cross); Shapes.put(CatLMK.LMK_DOME, Landmarks.Dome);
		Shapes.put(CatLMK.LMK_RADR, Landmarks.Mast); Shapes.put(CatLMK.LMK_TOWR, Landmarks.LandTower); Shapes.put(CatLMK.LMK_WNDM, Landmarks.Windmill);
		Shapes.put(CatLMK.LMK_WNDG, Landmarks.WindMotor); Shapes.put(CatLMK.LMK_SPIR, Landmarks.Spire); Shapes.put(CatLMK.LMK_BLDR, Beacons.Cairn);
		Shapes.put(CatLMK.LMK_MNRT, Landmarks.Minaret); Shapes.put(CatLMK.LMK_WTRT, Landmarks.WaterTower);
	}

	public static final EnumMap<FncFNC, Symbol> Funcs = new EnumMap<FncFNC, Symbol>(FncFNC.class);
	static {
		Funcs.put(FncFNC.FNC_CHCH, Landmarks.Church); Funcs.put(FncFNC.FNC_CHPL, Landmarks.Church); Funcs.put(FncFNC.FNC_TMPL, Landmarks.Temple);
		Funcs.put(FncFNC.FNC_PGDA, Landmarks.Temple); Funcs.put(FncFNC.FNC_SHSH, Landmarks.Temple); Funcs.put(FncFNC.FNC_BTMP, Landmarks.Temple);
		Funcs.put(FncFNC.FNC_MOSQ, Landmarks.Minaret); Funcs.put(FncFNC.FNC_MRBT, Landmarks.Spire); Funcs.put(FncFNC.FNC_COMM, Landmarks.RadioTV);
		Funcs.put(FncFNC.FNC_TV, Landmarks.RadioTV); Funcs.put(FncFNC.FNC_RADO, Landmarks.RadioTV); Funcs.put(FncFNC.FNC_RADR, Landmarks.RadioTV);
		Funcs.put(FncFNC.FNC_LGHT, Beacons.LightMajor); Funcs.put(FncFNC.FNC_MCWV, Landmarks.RadioTV); Funcs.put(FncFNC.FNC_HBRM, Harbours.HarbourMaster);
		Funcs.put(FncFNC.FNC_CSTM, Harbours.Customs); Funcs.put(FncFNC.FNC_HLTH, Harbours.Hospital); Funcs.put(FncFNC.FNC_HOSP, Harbours.Hospital);
	}
}
