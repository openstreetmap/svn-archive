/**
 * 
 */
package org.openstreetmap.labelling.osm;

import java.awt.geom.Rectangle2D;
import java.util.Vector;

import org.openstreetmap.labelling.map.Label;
import org.openstreetmap.labelling.map.LabelPosition;
import org.openstreetmap.labelling.map.PointFeature;
import org.openstreetmap.osm.util.OSMGElement;
import org.openstreetmap.osm.util.OSMPointFeatureElement;
import org.openstreetmap.osm.util.OSMTextElement;

/**
 * @author sebi
 *
 */
public class OSMPointFeature extends PointFeature
{
    private OSMPointFeatureElement osmPointFeatureElement;
 
    public OSMPointFeature(String id, OSMPointFeatureElement osmPointFeatureElement)
    {
        super(id, osmPointFeatureElement.getPOICoordinate(), osmPointFeatureElement.getBB());
        this.osmPointFeatureElement = osmPointFeatureElement;
    }
    
    public void addTextElement(OSMTextElement textElement)
    {
        String text = textElement.getText();
        for (Label l : getLabels())
        {
            if (! (l instanceof OSMPointFeatureLabel))
            {
                return;
            }
            OSMPointFeatureLabel pl = (OSMPointFeatureLabel)l;
            if (pl.getText().equals(text))
            {
                pl.addOSMTextElement(textElement);
                return;
            }
        }
        OSMPointFeatureLabel l = new OSMPointFeatureLabel(this, text, new Vector<LabelPosition>());
        l.addOSMTextElement(textElement);
        addLabel(l);
    }
    
    public Rectangle2D getBB()
    {
        return osmPointFeatureElement.getBB();
    }
}
