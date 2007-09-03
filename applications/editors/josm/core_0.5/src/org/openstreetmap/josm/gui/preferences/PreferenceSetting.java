// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.gui.preferences;


public interface PreferenceSetting {
	/**
	 * Add the GUI elements to the dialog. The elements should be initialized after
	 * the current preferences.
	 */
	void addGui(PreferenceDialog gui);

	/**
	 * Called, when OK is pressed to save the setting in the Preferences file.
	 */
	void ok();
}
