/**
 * 
 */
package org.openstreetmap.labelling.osm;

import org.openstreetmap.labelling.map.LineFeature;
import org.openstreetmap.osm.util.OSMPathElement;
import org.openstreetmap.osm.util.OSMTextPathElement;

/**
 * @author sebi
 *
 */
public class OSMLineFeature extends LineFeature
{
    private OSMTextPathElement textPathElement;
    private OSMPathElement pathElement;
    
    public OSMLineFeature(OSMTextPathElement textPathElement, OSMPathElement pathElement)
    {
        super(textPathElement.getHref(), pathElement.getLines(), null);
        
        this.textPathElement = textPathElement;
        this.pathElement = pathElement;
    }
    
    public double getTextLength()
    {
        return textPathElement.getTextLength();
    }
    
    public String getText()
    {
        return textPathElement.getText();
    }
}
