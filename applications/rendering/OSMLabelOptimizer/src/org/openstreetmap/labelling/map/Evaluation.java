package org.openstreetmap.labelling.map;

import java.util.Vector;

import org.openstreetmap.labelling.annealing.ConcreteAtom;
import org.openstreetmap.labelling.annealing.Energy;

public class Evaluation
	implements Energy
{
	private double oldOldValue = 0;
	private double oldValue = 0;
	private double value = 0;
    
    private int tmp = 0;

	public Vector<Metric> metrics;

	public Evaluation()
	{
		this.metrics = new Vector<Metric> ();
	}
    
    public void addMetric(Metric m)
    {
        metrics.add(m);
    }

	public boolean lower()
	{
		return getDelta() <= 0;
	}

	public double getDelta()
	{
		return value - oldValue;
	}

	public void atomHasChangedPosition(ConcreteAtom atom)
	{
		oldOldValue = oldValue;
		oldValue = value;

		value = 0;

		Label label = (Label)atom;
		label.informMetrics();

        /*
        double summe = 0;
        for (Metric m : metrics)
        {
            summe += m.getWeight();
        }*/
        
		for (Metric m : metrics)
		{
			value += m.getWeight()/*/summe*/ * m.getValue();
            if (tmp % 1000 == 0)
            {
                System.out.println("value_m: " + m.getValue());
            }
		}
        
        if (tmp % 1000 == 0)
        {
            System.out.println("value: " + value);
        }
        ++tmp;
	}

	public void revert()
	{
		value = oldValue;
		oldValue = oldOldValue;
        for (Metric m : metrics)
        {
            m.revert();
        }
	}
}
