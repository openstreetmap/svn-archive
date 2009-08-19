/**
 * 
 */
package org.openstreetmap.labelling.osm;

import java.util.Vector;

import org.openstreetmap.labelling.map.Feature;
import org.openstreetmap.labelling.map.Label;
import org.openstreetmap.labelling.map.LabelPosition;
import org.openstreetmap.labelling.map.LineLabelPosition;
import org.openstreetmap.osm.util.OSMTextPathElement;

/**
 * @author sebi
 *
 */
public class OSMLineFeatureLabel extends Label implements OSMLabel
{
    private OSMTextPathElement textPath;
    
    public OSMLineFeatureLabel(Feature feature, String text, Vector<LabelPosition> positions, OSMTextPathElement textPath)
    {
        super(feature, text, positions);
        
        this.textPath = textPath;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.osm.OSMLabel#render()
     */
    public void render()
    {
        LineLabelPosition labelPos = (LineLabelPosition)getLabelPosition();
        textPath.setStartOffset(labelPos.getRelativePosition());
    }
    
    
}
