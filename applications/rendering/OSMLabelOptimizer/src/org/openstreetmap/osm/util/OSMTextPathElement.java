/**
 * 
 */
package org.openstreetmap.osm.util;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.UpdateManager;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGTextPathElement;

/**
 * @author sebi
 *
 */
public class OSMTextPathElement
{
    private SVGTextPathElement textPathElement;
    private UpdateManager updateManager;
    private final static String XLINK_NS = "http://www.w3.org/1999/xlink";
    
    public OSMTextPathElement(SVGTextPathElement textPathElement, UpdateManager updateManager)
    {
        this.textPathElement = textPathElement;
        this.updateManager = updateManager;
    }
    
    public long getId()
    {
        String href = getHref();
        //System.out.println(href);
        href = href.replaceFirst("#way_normal_", "");
        //System.out.println(href);
        href = href.replaceFirst("#way_reverse_", "");
        //System.out.println(href);
        return Long.parseLong(href);
    }
    
    public String getHref()
    {
        return textPathElement.getAttributeNS(XLINK_NS, "href");
    }
    
    public String getText()
    {
        return textPathElement.getTextContent();
    }
    
    public String getFontSize()
    {
        return textPathElement.getAttributeNS(null, "font-size");
    }
    
    public void setHref(final String id)
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                textPathElement.setAttributeNS(XLINK_NS, "xlink:href", "#".concat(id));
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
    
    public double getTextLength()
    {
        return textPathElement.getComputedTextLength();
    }
    
    public SVGTextPathElement getSVGTextPathElement()
    {
        return textPathElement;
    }
    
    public void setStartOffset(final double startOffset)
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                textPathElement.setAttributeNS(null, "startOffset", (new Double(startOffset*100)).toString() + "%");       
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
    
    public Rectangle2D getBB()
    {
        if (textPathElement.getNumberOfChars() <= 0)
        {
            return new Rectangle(0, 0, -1,-1);
        }
        Rectangle2D r = new Rectangle.Double();
        SVGRect r_tmp = textPathElement.getExtentOfChar(0);
        r.setRect(r_tmp.getX(), r_tmp.getY(), r_tmp.getWidth(), r_tmp.getHeight());
        System.out.println(textPathElement.getTextContent());
        for (int i = 1; i < textPathElement.getNumberOfChars(); ++i)
        {
            SVGRect rect = textPathElement.getExtentOfChar(i);
            Rectangle r2 = new Rectangle();
            r2.setRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
            r.add(r2);
        }
        return r;
    }
}
