package org.openstreetmap.labelling.annealing;

public interface Energy
{
	public boolean lower();
	public double getDelta();
	public void atomHasChangedPosition(ConcreteAtom atom);
	public void revert();
}
