package org.openstreetmap.labelling.map;

public interface Metric
{
	public double getWeight();
	public double getValue();
	public void recalculate(Label changedLabel);
    public void revert();
}
