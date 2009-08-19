package org.openstreetmap.labelling.annealing;

public interface AbortCriterion
{
	public void setAnnealingOven(AnnealingOven oven);
	public boolean abort();
}
