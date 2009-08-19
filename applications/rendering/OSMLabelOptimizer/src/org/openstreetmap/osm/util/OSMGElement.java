package org.openstreetmap.osm.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.bridge.UpdateManager;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGGElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGTransform;
import org.w3c.dom.svg.SVGTransformList;
import org.w3c.dom.svg.SVGUseElement;

/**
 * @author sebi
 *
 */
public class OSMGElement implements OSMPointFeatureElement
{
    private SVGGElement gElement;
    private UpdateManager updateManager;
    
    public OSMGElement(SVGGElement gElement, UpdateManager updateManager)
    {
        this.gElement = gElement;
        this.updateManager = updateManager;
    }
    
    public boolean isPOISymbol()
    {
        boolean is_symbol = gElement.getChildNodes().getLength() == 3;
        is_symbol = is_symbol && gElement.getChildNodes().item(1) instanceof SVGUseElement;
        is_symbol = is_symbol && gElement.getTransform().getAnimVal().getNumberOfItems() == 3;
        is_symbol = is_symbol && gElement.getTransform().getAnimVal().getItem(0).getType() == SVGTransform.SVG_TRANSFORM_TRANSLATE;
        return is_symbol;
    }
    
    public Point2D getPOICoordinate()
    {
        Point2D p = new Point.Double();
        SVGTransformList l = gElement.getTransform().getAnimVal();
        if (l.getNumberOfItems() > 0 && l.getItem(0).getType() == SVGTransform.SVG_TRANSFORM_TRANSLATE)
        {
            // this does not work, because it's not exact enough
            //p.setLocation(l.getItem(0).getMatrix().getE(), l.getItem(0).getMatrix().getF());
            // this is a better solution:
            Pattern pat = Pattern.compile("^translate\\((-?\\d*\\.\\d*),(-?\\d*\\.\\d*)\\).*$");
            System.out.println(gElement.getAttributeNS("", "transform"));
            Matcher m = pat.matcher(gElement.getAttributeNS("", "transform"));
            m.matches();
            //System.out.println("x: " + m.group(1) + " y: " + m.group(2));
            p.setLocation(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
        }
        return p;
    }
    
    public Rectangle2D getBB()
    {
        SVGRect rect = gElement.getBBox();
        SVGMatrix m = gElement.getTransformToElement((SVGElement)gElement.getParentNode());
        //System.out.println(m.getA() + " " + m.getB() + " " + m.getC() + " " + m.getD() + " " + m.getE() + " " + m.getF());
        AffineTransform af = new AffineTransform(m.getA(), m.getB(), m.getC(), m.getD(), m.getE(), m.getF());
        Rectangle2D rect2 = new Rectangle.Double();
        rect2.setRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        Shape s = af.createTransformedShape(rect2);
        rect2 = s.getBounds2D();
        return rect2;
    }
}
