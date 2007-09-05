package org.openstreetmap.josm.data;

import java.util.Collection;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * This is a listener for data changes. Whenever the global current dataset
 * is switched to another dataset, or whenver this dataset changes, a
 * dataChanged event is fired.
 * 
 * Note that these events get not fired immediately but are inserted in the
 * Swing-event queue and packed together. So only one selection changed event
 * are issued within one message dispatch routine.
 * 
 * @author Frederik Ramm <frederik@remote.org>
 */
public interface DataChangedListener {

	/**
	 * Informs the listener that the dataset has changed.
	 */
	public void dataChanged();
}
