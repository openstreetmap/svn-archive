// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.history;

/**
* Interface for the listener of the {@link StreetsideRecord} class
*
* @author nokutu
* @see StreetsideRecord
*/
@FunctionalInterface
public interface StreetsideRecordListener {

/**
* Fired when any command is undone or redone.
*/
void recordChanged();
}
