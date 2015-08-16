package org.openstreetmap.josm.plugins.rasterfilters.model;

import java.rmi.server.UID;

/**
 * Interface that notifies about filter's state is changed.
 * This interface is implemented by {@link FiltersManager}.
 * @author Nipel-Crumple
 *
 */
public interface StateChangeListener {

	public void filterStateChanged(UID filterId, FilterStateModel filterState);

}
