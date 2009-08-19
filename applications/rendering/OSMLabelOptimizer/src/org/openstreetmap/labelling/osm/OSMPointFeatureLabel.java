/**
 * 
 */
package org.openstreetmap.labelling.osm;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import org.openstreetmap.labelling.map.Feature;
import org.openstreetmap.labelling.map.Label;
import org.openstreetmap.labelling.map.LabelPosition;
import org.openstreetmap.labelling.map.LineLabelPosition;
import org.openstreetmap.labelling.map.PointLabelPosition;
import org.openstreetmap.labelling.map.Position;
import org.openstreetmap.osm.util.OSMTextElement;

/**
 * @author sebi
 *
 */
public class OSMPointFeatureLabel extends Label implements OSMLabel
{
    private Vector<OSMTextElement> textElements;
    
    /**
     * @param feature
     * @param text
     * @param positions
     */
    public OSMPointFeatureLabel(Feature feature, String text, Vector<LabelPosition> positions)
    {
        super(feature, text, positions);
        
        textElements = new Vector<OSMTextElement>();
    }
    
    public void addOSMTextElement(OSMTextElement textElement)
    {
        textElements.add(textElement);
    }
    
    public void setPointLabelPosition(Position p)
    {
        OSMPointFeature pointFeature = (OSMPointFeature)getFeature();
        Rectangle2D r = pointFeature.getBB();
        
        double x = 0;
        double y = 0;
        
        switch (p)
        {
            case BASELINE_EAST:
                x = r.getX() + r.getWidth();
                y = r.getY() + 0.5*r.getHeight();
                break;
            case TOPLINE_EAST:
                x = r.getX() + r.getWidth();
                y = r.getY() + 0.5*r.getHeight();
                break;
            case BASELINE_WEST:
                x = r.getX();
                y = r.getY() + 0.5*r.getHeight();
                break;
            case TOPLINE_WEST:
                x = r.getX();
                y = r.getY() + 0.5*r.getHeight();
                break;
            case BASELINE_NORTH_EAST:
                x = r.getX() + 0.75*r.getWidth();
                y = r.getY() + 0.25*r.getHeight();
                break;
            case TOPLINE_SOUTH_EAST:
                x = r.getX() + 0.75*r.getWidth();
                y = r.getY() + 0.75*r.getHeight();
                break;
            case BASELINE_NORTH_WEST:
                x = r.getX() + 0.25*r.getWidth();
                y = r.getY() + 0.25*r.getHeight();
                break;
            case BASELINE_HALF_NORTH:
                x = r.getX() + 0.5*r.getWidth();
                y = r.getY();
                break;
            case BASELINE_HALF_DESCENDER_NORTH:
                x = r.getX() + 0.5*r.getWidth();
                y = r.getY();
                break;
            case TOPLINE_HALF_SOUTH:
                x = r.getX() + 0.5*r.getWidth();
                y = r.getY() + r.getHeight();
                break;
            case BASELINE_ONE_THIRD_NORTH:
                x = r.getX() + 0.5*r.getWidth();
                y = r.getY();
                break;
            case BASELINE_ONE_THIRD_DESCENDER_NORTH:
                x = r.getX() + 0.5*r.getWidth();
                y = r.getY();
                break;
            case TOPLINE_ONE_THIRD_SOUTH:
                x = r.getX() + 0.5*r.getWidth();
                y = r.getY() + r.getHeight();
                break;
            case BASELINE_TWO_THIRD_NORTH:
                x = r.getX() + 0.5*r.getWidth();
                y = r.getY();
                break;
            case BASELINE_TWO_THIRD_DESCENDER_NORTH:
                x = r.getX() + 0.5*r.getWidth();
                y = r.getY();
                break;
            case TOPLINE_TWO_THIRD_SOUTH:
                x = r.getX() + 0.5*r.getWidth();
                y = r.getY() + r.getHeight();
                break;
            case X_HEIGHT_LINE_EAST:
                x = r.getX() + r.getWidth();
                y = r.getY() + r.getHeight();
                break;
            case X_HEIGHT_LINE_WEST:
                x = r.getX();
                y = r.getY() + r.getHeight();
                break;
            case HALF_X_HEIGHT_LINE_EAST:
                x = r.getX() + r.getWidth();
                y = r.getY() + r.getHeight();
                break;
            case HALF_X_HEIGHT_LINE_WEST:
                x = r.getX();
                y = r.getY() + r.getHeight();
                break;
        }
        
        for (OSMTextElement el : textElements)
        {
            el.setPosition(x, y, p);
        }
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.osm.OSMLabel#render()
     */
    public void render()
    {
        PointLabelPosition labelPos = (PointLabelPosition)getLabelPosition();
        System.out.println(labelPos.getPosition());
        System.out.println(textElements.get(0).getText());
        setPointLabelPosition(labelPos.getPosition());
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
