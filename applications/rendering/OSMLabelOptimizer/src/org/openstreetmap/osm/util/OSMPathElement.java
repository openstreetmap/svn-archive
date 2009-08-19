/**
 * 
 */
package org.openstreetmap.osm.util;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.apache.batik.bridge.UpdateManager;
import org.w3c.dom.svg.SVGPathElement;
import org.w3c.dom.svg.SVGPathSeg;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicAbs;
import org.w3c.dom.svg.SVGPathSegLinetoAbs;
import org.w3c.dom.svg.SVGPathSegList;
import org.w3c.dom.svg.SVGPathSegMovetoAbs;

/**
 * @author sebi
 *
 */
public class OSMPathElement
{
    private SVGPathElement path;
    private UpdateManager updateManager;
    
    public OSMPathElement(SVGPathElement path, UpdateManager updateManager)
    {
        this.path = path;
        this.updateManager = updateManager;
    }
    
    public String getD()
    {
        return path.getAttributeNS(null, "d");
    }
    
    public Point2D getStart()
    {
        SVGPathSegList sl = path.getNormalizedPathSegList();
        SVGPathSegMovetoAbs ss = (SVGPathSegMovetoAbs)sl.getItem(0);
        Point2D p = new Point.Double();
        p.setLocation(ss.getX(), ss.getY());
        return p;
    }
    
    public Point2D getEnd()
    {
        SVGPathSegList sl = path.getNormalizedPathSegList();
        SVGPathSeg ss = sl.getItem(sl.getNumberOfItems() - 1);
        Point2D p = new Point.Double();
        switch(ss.getPathSegType())
        {
            case SVGPathSeg.PATHSEG_LINETO_ABS:
                SVGPathSegLinetoAbs ss_tmp = (SVGPathSegLinetoAbs)ss;
                p.setLocation(ss_tmp.getX(), ss_tmp.getY());
                return p;
            case SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS:
                SVGPathSegCurvetoCubicAbs ss_tmp2 = (SVGPathSegCurvetoCubicAbs)ss;
                p.setLocation(ss_tmp2.getX(), ss_tmp2.getY());
                return p;
            case SVGPathSeg.PATHSEG_CLOSEPATH:
                return getStart();
        }
        return null;
    }
    
    public Vector<Line2D> getLines()
    {
        Vector<Line2D> v = new Vector<Line2D>();
        Point2D p1 = new Point.Double();
        Point2D p2 = null;
        
        SVGPathSegList sl = path.getNormalizedPathSegList();
        SVGPathSegMovetoAbs ss = (SVGPathSegMovetoAbs)sl.getItem(0);
        p1.setLocation(ss.getX(), ss.getY());
        Point2D p = p1;
        
        for (int i = 1; i < sl.getNumberOfItems(); ++i)
        {
            SVGPathSeg ss_tmp = sl.getItem(i);
            p2 = new Point();
            switch(ss_tmp.getPathSegType())
            {
                case SVGPathSeg.PATHSEG_LINETO_ABS:
                    SVGPathSegLinetoAbs ss_tmp2 = (SVGPathSegLinetoAbs)ss_tmp;
                    p2.setLocation(ss_tmp2.getX(), ss_tmp2.getY());
                    break;
                case SVGPathSeg.PATHSEG_CLOSEPATH:
                    p2.setLocation(p.getX(), p.getY());
                    break;
                default:
                    System.out.println("Strange path segment when creating lines from path.");
                    return null;
            }
            Line2D l = new Line2D.Double();
            l.setLine(p1, p2);
            v.add(l);
            p1 = p2;
            p2 = null;
        }
        
        return v;
    }
    
    public void addPath(final OSMPathElement newPath)
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                String d = path.getAttributeNS(null, "d");
                String da = newPath.getD();
                if (d != "")
                {
                    int n = da.indexOf("L");
                    da = da.substring(n);
                }
                path.setAttributeNS(null, "d", d.concat(da));
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
}
