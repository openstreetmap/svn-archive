package org.openstreetmap.labelling.map;

import java.awt.geom.Rectangle2D;

public class LabelPosition
{
    private Rectangle2D boundingBox;
    private Label label;

	public LabelPosition(Label label, Rectangle2D boundingBox)
	{
        this.boundingBox = boundingBox;
        this.label = label;
	}
    
    public Rectangle2D getBoundingBox()
    {
        return boundingBox;
    }
    
    public Label getLabel()
    {
        return label;
    }
}

