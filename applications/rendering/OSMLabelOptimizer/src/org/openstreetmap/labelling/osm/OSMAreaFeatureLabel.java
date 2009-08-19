/**
 * 
 */
package org.openstreetmap.labelling.osm;

import java.awt.geom.Rectangle2D;
import java.util.Vector;

import org.openstreetmap.labelling.map.Feature;
import org.openstreetmap.labelling.map.Label;
import org.openstreetmap.labelling.map.LabelPosition;
import org.openstreetmap.osm.util.OSMTextElement;

/**
 * @author sebi
 *
 */
public class OSMAreaFeatureLabel extends Label implements OSMLabel
{
    private Vector<OSMTextElement> textElements;
    
    public OSMAreaFeatureLabel(Feature feature, String text, Vector<LabelPosition> positions)
    {
        super(feature, text, positions);
        
        textElements = new Vector<OSMTextElement>();
    }
    
    public void addOSMTextElement(OSMTextElement textElement)
    {
        textElements.add(textElement);
    }
    
    public void render()
    {
        // TODO Auto-generated method stub

    }
    
    public Rectangle2D getBB()
    {
        if (textElements.size() <= 0)
        {
            return null;
        }
        Rectangle2D rect = textElements.get(0).getBB();
        int i = 0;
        for (OSMTextElement textElement : textElements)
        {
            if (i > 0)
            {
                rect.add(textElement.getBB());
            }
            ++i;
        }
        return rect;
    }
}
