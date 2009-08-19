package org.openstreetmap.labelling.annealing;


public class DefaultAnnealingSchedule
	implements AnnealingSchedule
{
	private double currentTemperature;
    private AnnealingOven oven;
    
    private int numberOfCoolDowns = 0;
    
    private int oldNumberOfSuccessfulChanges = 0;

	private final double COOL_DOWN_FACTOR = 0.9;
	private final double INITIAL_TEMPERATURE = - 1/Math.log(1./3);
    private final int TIME_FACTOR = 20;
    private final int CHANGES_FACTOR = 5;

	public DefaultAnnealingSchedule()
	{
	}

	public double getInitialTemperature()
	{
		return INITIAL_TEMPERATURE;
	}

	public void resetTemperature()
	{
		currentTemperature = getInitialTemperature();
	}

	public double getCurrentTemperature()
	{
        int time = TIME_FACTOR * oven.getAnnealingMaterial().getNumberOfAtoms();
        int numberOfSuccessfulChanges = oven.getAnnealingMaterial().getNumberOfChanges();
        int totalNumberOfChanges = oven.getAnnealingMaterial().getTotalNumberOfChanges();
        if (numberOfSuccessfulChanges > oldNumberOfSuccessfulChanges)
        {
            numberOfSuccessfulChanges -= oldNumberOfSuccessfulChanges;
        }
        
        /*if (totalNumberOfChanges - numberOfCoolDowns * time <= 1000)
        {
            System.out.println("Number of changes: " + totalNumberOfChanges);
        }*/
        if (totalNumberOfChanges - numberOfCoolDowns * time >= time ||  numberOfSuccessfulChanges >= CHANGES_FACTOR * oven.getAnnealingMaterial().getNumberOfAtoms())
        {
            oldNumberOfSuccessfulChanges = numberOfSuccessfulChanges;
            coolDown();
        }
        
		return currentTemperature;
	}

	private void coolDown()
	{
        System.out.println("Cool down!");
        ++numberOfCoolDowns;
		currentTemperature = COOL_DOWN_FACTOR * currentTemperature;
	}
    
    public int getNumberOfCoolDowns()
    {
        return numberOfCoolDowns;
    }

	public void setAnnealingOven(AnnealingOven oven)
	{
        this.oven = oven;
	}
}
