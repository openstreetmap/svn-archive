/**
 * 
 */
package org.openstreetmap.osm.util;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDefsElement;
import org.w3c.dom.svg.SVGPathElement;

/**
 * @author sebi
 *
 */
public class OSMDefsElement
{
    private Document doc;
    private SVGDefsElement defsElement;
    private UpdateManager updateManager;
    
    public OSMDefsElement(SVGDefsElement defsElement, UpdateManager updateManager)
    {
        this.defsElement = defsElement;
        this.doc = defsElement.getOwnerDocument();
        this.updateManager = updateManager;
    }
    
    public OSMPathElement addNewPathElement(final String id)
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                Element pathElement = doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "path");
                pathElement.setAttributeNS(null, "id", id);
                pathElement.setAttributeNS(null, "d", "");
                defsElement.appendChild(pathElement);
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
        SVGPathElement pathElement = (SVGPathElement)doc.getElementById(id);
        return new OSMPathElement(pathElement, updateManager);
    }
}
