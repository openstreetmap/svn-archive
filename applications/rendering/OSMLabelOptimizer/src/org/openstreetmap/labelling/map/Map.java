package org.openstreetmap.labelling.map;

import java.util.Collection;
import java.util.HashSet;

import org.openstreetmap.labelling.annealing.ConcreteAnnealingMaterial;
import org.openstreetmap.labelling.annealing.ConcreteAtom;

public class Map
	implements ConcreteAnnealingMaterial
{
	private Collection<Feature> features;
    private Collection<ConcreteAtom> atoms;

    public Map()
    {
        features = new HashSet<Feature>();
        atoms = new HashSet<ConcreteAtom>();
    }
    
	public void addFeature(Feature feature)
	{
		features.add(feature);
        for (Label l : feature.getLabels())
        {
            atoms.add(l);
        }
	}
    
    public Collection<Feature> getFeatures()
    {
        return features;
    }

	public Collection<ConcreteAtom> getConcreteAtoms()
	{
		return atoms;
	}

}
