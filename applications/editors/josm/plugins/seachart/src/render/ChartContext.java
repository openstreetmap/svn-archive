/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package render;

import java.awt.Color;
import java.awt.geom.Point2D;

import s57.S57map;
import s57.S57map.*;

public interface ChartContext {
	public enum RuleSet { ALL, BASE, SEAMARK }

	Point2D getPoint(Snode coord);
	double mile(Feature feature);
	boolean clip();
	Color background(S57map map);
	RuleSet ruleset();
}
