package org.openstreetmap.labelling.annealing;

import java.util.Random;

import org.openstreetmap.labelling.util.RandomSource;

public class Atom
{
	private Energy energy;
	private ConcreteAtom concreteAtom;
	private int oldPos = 0;

	public Atom(ConcreteAtom concreteAtom, Energy energy)
	{
		this.concreteAtom = concreteAtom;
		this.energy = energy;
	}

	public void relocate()
	{
		oldPos = concreteAtom.getPosition();
		int numOfPos = concreteAtom.getNumberOfPositions();
		Random rand = RandomSource.getRandom();
        if (numOfPos > 0)
        {
            int pos = rand.nextInt(numOfPos);
            concreteAtom.setPosition(pos);
            energy.atomHasChangedPosition(concreteAtom);
        }
	}
	
	public void revertRelocation()
	{
		concreteAtom.setPosition(oldPos);
		energy.revert();
	}
}
