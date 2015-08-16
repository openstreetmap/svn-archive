package org.openstreetmap.josm.plugins.rasterfilters.gui;

import org.openstreetmap.josm.plugins.rasterfilters.model.FilterStateModel;
/**
 * Filter state's keeper. This interface is implemented by {@link FilterGuiListeener}.
 *
 * @author Nipel-Crumple
 *
 */
public interface FilterStateOwner {

	public FilterStateModel getState();

}