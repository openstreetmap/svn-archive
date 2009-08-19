/**
 * 
 */
package org.openstreetmap.labelling.map;

import java.util.Collection;

/**
 * @author sebi
 *
 */
public class CenterednessLineLabelMetric extends PrecalculatedMetric
{
    /**
     * 
     */
    public CenterednessLineLabelMetric(Collection<LineFeature> lineFeatures)
    {
        super(lineFeatures);
    }


    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.Metric#getWeight()
     */
    public double getWeight()
    {
        return 3;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.PrecalculatedMetric#calculateValue(org.openstreetmap.labelling.map.LabelPosition, java.util.Vector, java.util.Vector)
     */
    @Override
    protected double calculateValue(LabelPosition pos, Collection<? extends Feature> features, Collection<? extends Label> labels)
    {
        LineLabelPosition linePos = (LineLabelPosition)pos;
        int numberLabels = pos.getLabel().getFeature().getLabels().size();
        int numberThis = pos.getLabel().getFeature().getLabels().indexOf(pos.getLabel());
        //double val = Math.abs(2*linePos.getRelativePosition() - 1);
        // optimal position: opt=(2*numerThis + 1)/(2*numberLabels)
        // for measuring the quality take a line through (opt, 0) and (1,1) if opt <= 0.5
        // and a line through (opt, 0) and (0,-1) if opt > 0.5
        double opt = (2*numberThis + 1.0)/(2*numberLabels);
        double val = 0;
        if (opt <= 0.5)
        {
            val = Math.abs(1/(1-opt)*(linePos.getRelativePosition() - 1) + 1);
        }
        else
        {
            val = Math.abs(1/opt*linePos.getRelativePosition() - 1);
        }

        return val;
    }
}
