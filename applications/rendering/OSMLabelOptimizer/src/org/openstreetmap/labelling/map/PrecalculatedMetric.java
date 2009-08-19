/**
 * 
 */
package org.openstreetmap.labelling.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

/**
 * @author sebi
 *
 */
public abstract class PrecalculatedMetric implements Metric
{
    private double oldValue;
    private double value;
    private boolean revertable;
    private java.util.Map<Label, LabelPosition> currentPositions;
    private java.util.Map<LabelPosition, Double> possibleValues;
    private Label lastChanged;
    private LabelPosition oldPosition;

    /**
     * 
     */
    public PrecalculatedMetric()
    {
        oldValue = 0;
        value = 0;
        revertable = false;
        currentPositions = new HashMap<Label, LabelPosition> ();
        possibleValues = new HashMap<LabelPosition, Double> ();
    }
    
    public PrecalculatedMetric(Collection<? extends Feature> features)
    {
        this(features, null);
    }
    
    public PrecalculatedMetric(Collection<? extends Feature> features, Collection<? extends Label> labels)
    {
        this();
        Vector<Label> labelsToRead = new Vector<Label> ();
        if (labels == null)
        {
            for (Feature f : features)
            {
                labelsToRead.addAll(f.getLabels());
            }
        }
        else
        {
            labelsToRead.addAll(labels);
        }
        
        for (Label l : labelsToRead)
        {
            l.addMetric(this);
            LabelPosition activePos = l.getLabelPosition();
            currentPositions.put(l, activePos);

            for (LabelPosition pos : l.getPositions())
            {
                double val = calculateValue(pos, features, labels);
                possibleValues.put(pos, val);

                if (activePos == pos)
                {
                    setValue(getValue() + val);
                }
            }
        }
    }
    
    protected abstract double calculateValue(LabelPosition pos, Collection<? extends Feature> features, Collection<? extends Label> labels);
    
    /* (non-Javadoc)
     * @see org.openstreetmap.labelling.map.Metric#recalculate(org.openstreetmap.labelling.map.Label)
     */
    public void recalculate(Label changedLabel)
    {
        revertable = true;
        oldValue = value;
        lastChanged = changedLabel;
        LabelPosition newPos = changedLabel.getLabelPosition();
        LabelPosition pos = currentPositions.get(changedLabel);
        oldPosition = pos;
        
        Double newVal = possibleValues.get(newPos);
        Double val = possibleValues.get(pos);
        
        value = value - val;
        value = value + newVal;
        
        currentPositions.put(changedLabel, newPos);
    }
    
    public void revert()
    {
        if (revertable)
        {
            value = oldValue;
            revertable = false;
            currentPositions.put(lastChanged, oldPosition);
        }
    }

    /**
     * @return the currentPositions
     */
    public java.util.Map<Label, LabelPosition> getCurrentPositions()
    {
        return currentPositions;
    }

    /**
     * @param currentPositions the currentPositions to set
     */
    public void setCurrentPositions(
            java.util.Map<Label, LabelPosition> currentPositions)
    {
        this.currentPositions = currentPositions;
    }

    /**
     * @return the possibleValues
     */
    public java.util.Map<LabelPosition, Double> getPossibleValues()
    {
        return possibleValues;
    }

    /**
     * @param possibleValues the possibleValues to set
     */
    public void setPossibleValues(
            java.util.Map<LabelPosition, Double> possibleValues)
    {
        this.possibleValues = possibleValues;
    }

    /**
     * @return the value
     */
    public double getValue()
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(double value)
    {
        this.value = value;
    }

}
