package org.openstreetmap.labelling.map;

import java.util.Vector;

import org.openstreetmap.labelling.annealing.ConcreteAtom;

public class Label
	implements ConcreteAtom
{
	private String text;
	private Vector<LabelPosition> positions;
	private Vector<Metric> metrics;
    private Feature feature;
	private int position;

	public Label(Feature feature, String text, Vector<LabelPosition> positions)
	{
		this.text = text;
		this.positions = positions;
        this.metrics = new Vector<Metric>();
        this.feature = feature;
	}

	public String getText()
	{
		return text;
	}

	public Vector<LabelPosition> getPositions()
	{
		return positions;
	}

	public int getPosition()
	{
		return position;
	}
    
    public LabelPosition getLabelPosition()
    {
        return positions.get(position);
    }

	public void addMetric(Metric m)
	{
		metrics.addElement(m);
	}

	public int getNumberOfPositions()
	{
		return positions.size();
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	public void informMetrics()
	{
		for (Metric m : metrics)
		{
			m.recalculate(this);
		}
	}
    
    public Feature getFeature()
    {
        return feature;
    }
}

