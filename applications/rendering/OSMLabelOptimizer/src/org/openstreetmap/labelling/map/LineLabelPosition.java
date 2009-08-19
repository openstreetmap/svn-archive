/**
 * 
 */
package org.openstreetmap.labelling.map;

import java.awt.geom.Rectangle2D;

/**
 * @author sebi
 *
 */
public class LineLabelPosition extends LabelPosition
{
    private double relativePosition;

    /**
     * @param formatedText
     */
    public LineLabelPosition(Label label, Rectangle2D boundingBox, double relativePosition)
    {
        super(label, boundingBox);
        
        this.relativePosition = relativePosition;
    }

    /**
     * @return the relativePosition
     */
    public double getRelativePosition()
    {
        return relativePosition;
    }

}
