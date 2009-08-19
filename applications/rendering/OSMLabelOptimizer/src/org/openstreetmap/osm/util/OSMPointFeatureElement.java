/**
 * 
 */
package org.openstreetmap.osm.util;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author sebi
 *
 */
public interface OSMPointFeatureElement
{
    public Rectangle2D getBB();
    public Point2D getPOICoordinate();
}
