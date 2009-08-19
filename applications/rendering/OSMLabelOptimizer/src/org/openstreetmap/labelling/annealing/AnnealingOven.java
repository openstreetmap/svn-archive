package org.openstreetmap.labelling.annealing;

public class AnnealingOven
{
	private AnnealingSchedule annealingSchedule;
	private ReversionProbability reversionProbability;
	private AnnealingMaterial annealingMaterial;
	private AbortCriterion abortCriterion;

	public AnnealingOven()
	{
		this(new DefaultAnnealingSchedule(), new DefaultReversionProbability(), new DefaultAbortCriterion());
	}

	public AnnealingOven(AnnealingSchedule schedule, ReversionProbability probability, AbortCriterion abortCriterion)
	{
		this.annealingSchedule = schedule;
		annealingSchedule.setAnnealingOven(this);
		this.reversionProbability = probability;
		reversionProbability.setAnnealingOven(this);
		this.abortCriterion = abortCriterion;
		abortCriterion.setAnnealingOven(this);
	}

	public void setAnnealingMaterial(AnnealingMaterial annealingMaterial)
	{
		this.annealingMaterial = annealingMaterial;
        annealingMaterial.setAnnealingOven(this);
	}

	public AnnealingMaterial getAnnealingMaterial()
	{
		return annealingMaterial;
	}

	protected AnnealingSchedule getAnnealingSchedule()
	{
		return annealingSchedule;
	}

	protected ReversionProbability getReversionProbability()
	{
		return reversionProbability;
	}

	public void anneal()
	{
		if (annealingMaterial == null)
		{
			return;
		}

		annealingMaterial.randomiseAtoms();
		annealingSchedule.resetTemperature();

		while(! abortCriterion.abort())
		{
			annealingMaterial.diffuse();
            //System.out.println(annealingMaterial.getEnergy().getDelta());
		}
	}
}
