package org.openstreetmap.labelling.annealing;

public interface ConcreteAtom
{
	public int getPosition();
	public int getNumberOfPositions();
	public void setPosition(int pos);
}
