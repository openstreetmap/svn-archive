package org.openstreetmap.labelling.annealing;

import java.util.Collection;
import java.util.Random;
import java.util.Vector;

import org.openstreetmap.labelling.util.RandomSource;

public class AnnealingMaterial
{
	private AnnealingOven oven;
	private Vector<Atom> atoms;
	private Energy energy;
	private int numberOfNoChanges = 0;
    private int numberOfChanges = 0;
    private int totalNumberOfChanges = 0;

	public AnnealingMaterial(ConcreteAnnealingMaterial concreteMaterial, Energy energy)
	{
		this.energy = energy;
		Collection<ConcreteAtom> cAtoms = concreteMaterial.getConcreteAtoms();
		atoms = new Vector<Atom> (cAtoms.size());
		for (ConcreteAtom cAtom : cAtoms)
		{
			Atom atom = new Atom(cAtom, energy);
			atoms.add(atom);
		}
	}

	public void diffuse()
	{
        ++totalNumberOfChanges;
		int l = atoms.size();
		
		Random rand = RandomSource.getRandom();
		int k = rand.nextInt(l);
		Atom atom = atoms.elementAt(k);
		atom.relocate();
		if (! energy.lower())
		{
			double p = oven.getReversionProbability().getReversionProbability();
            //System.out.println("p: " + p);
			if (rand.nextDouble() < p)
			{
                //System.out.println("reverted");
				atom.revertRelocation();
				++numberOfNoChanges;
                numberOfChanges = 0;
			}
			else
			{
                //System.out.println("not reverted");
				numberOfNoChanges = 0;
                ++numberOfChanges;
			}
		}
		else
		{
            //System.out.println("better");
			numberOfNoChanges = 0;
            ++numberOfChanges;
		}

	}

	public int getNumberOfAtoms()
	{
		return atoms.size();
	}
    
    public int getTotalNumberOfChanges()
    {
        return totalNumberOfChanges;
    }

	public Energy getEnergy()
	{
		return energy;
	}

	public void randomiseAtoms()
	{
		for (Atom atom : atoms)
		{
			atom.relocate();
		}
	}

	public int getNumberOfNoChanges()
	{
		return numberOfNoChanges;
	}
    
    public int getNumberOfChanges()
    {
        return numberOfChanges;
    }

	public void setAnnealingOven(AnnealingOven oven)
	{
		this.oven = oven;
	}
}
