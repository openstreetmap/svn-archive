/**
 * 
 */
package org.openstreetmap.osm.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.openstreetmap.labelling.map.Position;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGTextElement;

/**
 * @author sebi
 *
 */
public class OSMTextElement
{
    private SVGTextElement textElement;
    private UpdateManager updateManager;
    
    public OSMTextElement(SVGTextElement textElement, UpdateManager updateManager)
    {
        this.textElement = textElement;
        this.updateManager = updateManager;
    }
    
    public String getCssString()
    {
        return textElement.getClassName().getAnimVal();
    }
    
    public Point2D getPoint()
    {
        Point2D p = new Point.Double();
        p.setLocation(Double.parseDouble(textElement.getAttribute("x")), Double.parseDouble(textElement.getAttribute("y")));
        return p;
    }
    
    public String getText()
    {
        return textElement.getTextContent();
    }
    
    public Rectangle2D getBB()
    {
        SVGRect r = textElement.getBBox();
        Rectangle2D rect = new Rectangle.Double();
        rect.setRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        return rect;
    }
    
    public void setPosition(double x, double y, Position p)
    {
        //TODO: How to get detailed font metrics?
        /*TextNode textNode = (TextNode)updateManager.getBridgeContext().getGraphicsNode(textElement);
        if (textNode.getText().length() == 0)
        {
            return;
        }
        GVTFont font = (GVTFont)textNode.getAttributedCharacterIterator().getAttribute(GVTAttributedCharacterIterator.TextAttribute.GVT_FONT);
        */
        
        Rectangle2D b = getBB();
        //System.out.println(textElement.getTextContent());
        //System.out.println("BB, x: " + b.getX() + ", y: " + b.getY() + ", w: " + b.getWidth() + ", h: " + b.getHeight());
        double h = b.getHeight();
        double w = b.getWidth();
        
        double dx = 0;
        double dy = 0;
        
        TextAnchor a = TextAnchor.START;
        
        switch (p)
        {
            case BASELINE_EAST:
                break;
            case TOPLINE_EAST:
                dy = h;
                //TODO Why is h to high????
                break;
            case BASELINE_WEST:
                a = TextAnchor.END;
                break;
            case TOPLINE_WEST:
                a = TextAnchor.END;
                dy = h;
                break;
            case BASELINE_NORTH_EAST:
                break;
            case TOPLINE_SOUTH_EAST:
                dy = h;
                break;
            case BASELINE_NORTH_WEST:
                a = TextAnchor.END;
                break;
            case BASELINE_HALF_NORTH:
                a = TextAnchor.MIDDLE;
                break;
            case BASELINE_HALF_DESCENDER_NORTH:
                a = TextAnchor.MIDDLE;
                break;
            case TOPLINE_HALF_SOUTH:
                a = TextAnchor.MIDDLE;
                dy = h;
                break;
            case BASELINE_ONE_THIRD_NORTH:
                a = TextAnchor.START;
                dx = - 1./3*w;
                break;
            case BASELINE_ONE_THIRD_DESCENDER_NORTH:
                a = TextAnchor.START;
                dx = - 1./3*w;
                break;
            case TOPLINE_ONE_THIRD_SOUTH:
                a = TextAnchor.START;
                dx = -1./3*w;
                dy = h;
                break;
            case BASELINE_TWO_THIRD_NORTH:
                a = TextAnchor.END;
                dx = 1./3*w;
                break;
            case BASELINE_TWO_THIRD_DESCENDER_NORTH:
                a = TextAnchor.END;
                dx = 1./3*w;
                break;
            case TOPLINE_TWO_THIRD_SOUTH:
                a = TextAnchor.END;
                dx = 1./3*w;
                dy = h;
                break;
            case X_HEIGHT_LINE_EAST:
                a = TextAnchor.START;
                dy = -3./4*h;
                break;
            case X_HEIGHT_LINE_WEST:
                a = TextAnchor.END;
                dy = -3./4*h;
                break;
            case HALF_X_HEIGHT_LINE_EAST:
                a = TextAnchor.START;
                dy = -0.5*h;
                break;
            case HALF_X_HEIGHT_LINE_WEST:
                a = TextAnchor.END;
                dy = -0.5*h;
                break;
        }
        System.out.println("x: " + x + " y:" + y + " dx: " + dx + " dy: " + dy + " a: " + a.getText());
        setTextElementLocation(x, y, dx, dy, a);
    }
    
    private void setTextElementLocation(final Double x, final Double y, final Double dx, final Double dy, final TextAnchor a)
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                textElement.setAttributeNS("", "x", x.toString());
                textElement.setAttributeNS("", "y", y.toString());
                textElement.setAttributeNS("", "dx", dx.toString());
                textElement.setAttributeNS("", "dy", dy.toString());
                textElement.getStyle().setProperty("text-anchor", a.getText(), "");
            }
        };
        try
        {
            updateManager.getUpdateRunnableQueue().invokeAndWait(r);
        }
        catch (InterruptedException e)
        {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
    
    private enum TextAnchor
    {   
        START("start"),
        MIDDLE("middle"),
        END("end");
        
        private final String text;
        
        TextAnchor(String text)
        {
            this.text = text;
        }
        
        public String getText()
        {
            return text;
        }
    }
}
