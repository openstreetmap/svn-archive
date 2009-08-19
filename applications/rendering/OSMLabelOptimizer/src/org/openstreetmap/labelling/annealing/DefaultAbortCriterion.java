package org.openstreetmap.labelling.annealing;

public class DefaultAbortCriterion
	implements AbortCriterion
{
	private AnnealingOven oven;
	private final int FACTOR = 20;
    private final int COOL_DOWN_LIMIT = 50;

	public void setAnnealingOven(AnnealingOven oven)
	{
		this.oven = oven;
	}

	public boolean abort()
	{
		int numberOfAtoms = oven.getAnnealingMaterial().getNumberOfAtoms();
		int numberOfNoChanges = oven.getAnnealingMaterial().getNumberOfNoChanges();
        int numberOfCoolDowns = oven.getAnnealingSchedule().getNumberOfCoolDowns();

		return numberOfNoChanges >= FACTOR * numberOfAtoms || numberOfCoolDowns >= COOL_DOWN_LIMIT;
	}
}
