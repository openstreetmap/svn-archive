package org.openstreetmap.josm.plugins.rasterfilters.gui;

import org.openstreetmap.josm.plugins.rasterfilters.model.FilterStateModel;

/**
 * Filter state's keeper. This interface is implemented by {@link FilterGuiListener}.
 *
 * @author Nipel-Crumple
 */
public interface FilterStateOwner {

    FilterStateModel getState();

}
