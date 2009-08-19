/**
 * 
 */
package org.openstreetmap.labelling.map;

import java.awt.geom.Rectangle2D;

/**
 * @author sebi
 * 
 */
public class PointLabelPosition extends LabelPosition
{

    private Position position;

    /**
     * @param formatedText
     */
    public PointLabelPosition(Label label, Rectangle2D boundingBox, Position position)
    {
        super(label, boundingBox);
        this.position = position;
    }

    /**
     * @return the position
     */
    public Position getPosition()
    {
        return position;
    }  
}
