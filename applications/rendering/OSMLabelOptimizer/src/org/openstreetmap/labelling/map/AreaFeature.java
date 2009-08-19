package org.openstreetmap.labelling.map;

import java.awt.geom.Area;

public class AreaFeature
	extends Feature
{
	private Area area;

	public AreaFeature(String id, Area area)
	{
        super(id);
		this.area = area;
	}

	public Area getArea()
	{
		return area;
	}
}
