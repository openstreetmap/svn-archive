/**
 * 
 */
package org.openstreetmap.osm.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import org.apache.batik.bridge.UpdateManager;
import org.w3c.dom.svg.SVGCircleElement;
import org.w3c.dom.svg.SVGRect;

/**
 * @author sebi
 *
 */
public class OSMCircleElement implements OSMPointFeatureElement
{
    private SVGCircleElement circleElement;
    private UpdateManager updateManager;
    
    public OSMCircleElement(SVGCircleElement circleElement, UpdateManager updateManager)
    {
        this.circleElement = circleElement;
        this.updateManager = updateManager;
    }
    
    public boolean isPOISymbol()
    {
        String s = circleElement.getClassName().getAnimVal();
        return s.equals("railway-station") || s.equals("railway-halt") || s.equals("generic-poi");
    }
    
    /* (non-Javadoc)
     * @see org.openstreetmap.osm.util.OSMPointFeatureElement#getBB()
     */
    public Rectangle2D getBB()
    {
        SVGRect r = circleElement.getBBox();
        Rectangle2D rect = new Rectangle.Double();
        rect.setRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        return rect;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.osm.util.OSMPointFeatureElement#getPOICoordinate()
     */
    public Point2D getPOICoordinate()
    {
        Point2D p = new Point.Double();
        p.setLocation(Double.parseDouble(circleElement.getAttributeNS("", "cx")), Double.parseDouble(circleElement.getAttributeNS("", "cy")));
        return p;
    }

}
