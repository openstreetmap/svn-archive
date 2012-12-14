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
import java.awt.geom.Path2D;
import java.util.ArrayList;

import symbols.Symbols.Instr;
import symbols.Symbols.Prim;

public class Topmarks {

	public static final ArrayList<Instr> TopCone = new ArrayList<Instr>();
	static {
		TopCone.add(new Instr(Prim.BBOX, new Rectangle(-20,80,40,80)));
		ArrayList<Instr> colours = new ArrayList<Instr>();
		Path2D.Double p = new Path2D.Double(); p.moveTo(0.0,0.0); p.lineTo(0.0,-15.0); p.moveTo(-15.0,-15.0); p.lineTo(0.0,-45.0); p.lineTo(15.0,-15.0); p.closePath();
		colours.add(new Instr(Prim.P1, p));
		TopCone.add(new Instr(Prim.COLR, colours));
		TopCone.add(new Instr(Prim.STRK, new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)));
		TopCone.add(new Instr(Prim.FILL, Color.black));
		p = new Path2D.Double(); p.moveTo(0.0,0.0); p.lineTo(0.0,-15.0); p.moveTo(-15.0,-15.0); p.lineTo(0.0,-45.0); p.lineTo(15.0,-15.0); p.closePath();
		TopCone.add(new Instr(Prim.PLIN, p));
	}

}
