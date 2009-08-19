/**
 * 
 */
package org.openstreetmap.labelling.map;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * @author sebi
 *
 */
public class PointOverlapMetric 
extends PrecalculatedMetric
{
    public PointOverlapMetric(Collection<PointFeature> pointFeatures, Collection<Label> labels)
    {
        super(pointFeatures, labels);
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.Metric#getWeight()
     */
    public double getWeight()
    {
        return 10;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.PrecalculatedMetric#calculateValue(org.openstreetmap.labelling.map.LabelPosition, java.util.Vector, java.util.Vector)
     */
    @Override
    protected double calculateValue(LabelPosition pos, Collection<? extends Feature> features, Collection<? extends Label> labels)
    {
        Rectangle2D rect = pos.getBoundingBox();
        int n = 0;
        for (Feature f : features)
        {
            PointFeature point = (PointFeature)f;
            Shape shape = point.getShape();
            
            if (shape.intersects(rect))
            {
                n = n + 1;
            }
        }
        return n;
    }
}
