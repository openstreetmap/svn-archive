// License: GPL. For details, see LICENSE file.
package render;

import java.awt.Color;
import java.awt.geom.Point2D;

import s57.S57map;
import s57.S57map.Feature;
import s57.S57map.Snode;

/**
 * @author Malcolm Herring
 */
public interface ChartContext {
    enum RuleSet { ALL, BASE, SEAMARK }

    Point2D getPoint(Snode coord);

    double mile(Feature feature);

    boolean clip();

    Color background(S57map map);

    RuleSet ruleset();
}
