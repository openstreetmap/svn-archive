package org.openstreetmap.labelling.map;

import java.util.Vector;

public abstract class Feature
{
	private Vector<Label> labels;
    private String id;

    public Feature(String id)
    {
        this.id = id;
        labels = new Vector<Label>();
    }
    
	public void addLabel(Label label)
	{
		labels.addElement(label);
	}

	public Vector<Label> getLabels()
	{
		return labels;
	}

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }
}
