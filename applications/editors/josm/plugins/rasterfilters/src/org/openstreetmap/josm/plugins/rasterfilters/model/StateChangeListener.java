package org.openstreetmap.josm.plugins.rasterfilters.model;

import java.rmi.server.UID;

public interface StateChangeListener {

	public void filterStateChanged(UID filterId, FilterStateModel filterState);

}
