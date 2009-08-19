/**
 * 
 */
package org.openstreetmap.labelling.osm;

import java.util.Vector;

import org.openstreetmap.labelling.map.AreaFeature;
import org.openstreetmap.labelling.map.Label;
import org.openstreetmap.labelling.map.LabelPosition;
import org.openstreetmap.osm.util.OSMGElement;
import org.openstreetmap.osm.util.OSMTextElement;

/**
 * @author sebi
 *
 */
public class OSMAreaFeature extends AreaFeature
{
    private OSMGElement gElement;
    
    public OSMAreaFeature(String id, OSMGElement gElement)
    {
        super(id, null);
        this.gElement = gElement;
    }
    
    public void addTextElement(OSMTextElement textElement)
    {
        String text = textElement.getText();
        for (Label l : getLabels())
        {
            if (! (l instanceof OSMAreaFeatureLabel))
            {
                return;
            }
            OSMAreaFeatureLabel pl = (OSMAreaFeatureLabel)l;
            if (pl.getText().equals(text))
            {
                pl.addOSMTextElement(textElement);
                return;
            }
        }
        OSMAreaFeatureLabel l = new OSMAreaFeatureLabel(this, text, new Vector<LabelPosition>());
        l.addOSMTextElement(textElement);
        addLabel(l);
    }
}
