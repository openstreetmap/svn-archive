package org.openstreetmap.labelling.annealing;


public interface AnnealingSchedule
{
	public double getInitialTemperature();
	public void resetTemperature();
	public double getCurrentTemperature();
    public int getNumberOfCoolDowns();
	public void setAnnealingOven(AnnealingOven oven);
}
