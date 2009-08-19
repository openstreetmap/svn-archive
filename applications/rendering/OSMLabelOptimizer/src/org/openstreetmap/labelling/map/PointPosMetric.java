package org.openstreetmap.labelling.map;

import java.util.Collection;

public class PointPosMetric
	extends PrecalculatedMetric
{

    public PointPosMetric(Collection<PointFeature> features)
    {
        super(features);
    }
    
	public double getWeight() 
	{
		return 1;
	}

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.PrecalculatedMetric#calculateValue(org.openstreetmap.labelling.map.LabelPosition, java.util.Vector, java.util.Vector)
     */
    @Override
    protected double calculateValue(LabelPosition pos, Collection<? extends Feature> features, Collection<? extends Label> labels)
    {
        PointLabelPosition pPos = (PointLabelPosition)pos;
        return pPos.getPosition().getValue();
    }
}

