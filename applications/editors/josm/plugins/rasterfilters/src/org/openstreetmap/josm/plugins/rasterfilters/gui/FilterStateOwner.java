package org.openstreetmap.josm.plugins.rasterfilters.gui;

import org.openstreetmap.josm.plugins.rasterfilters.model.FilterStateModel;

public interface FilterStateOwner {
	
	public FilterStateModel getState();
	
}