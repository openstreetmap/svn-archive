/**
 * License: GPL. For details, see LICENSE file.
 */
package org.openstreetmap.josm.plugins.pdfimport;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;

public class PreferenceSettings implements SubPreferenceSetting {
	
	private PreferenceSettingsGui guiSetting = new PreferenceSettingsGui();
	private PreferenceSettingsProcessing processingSettings = new PreferenceSettingsProcessing();
	
	@Override
	public TabPreferenceSetting getTabPreferenceSetting(PreferenceTabbedPane gui)
	{
	    return gui.getPluginPreference();
	}

	@Override
	public void addGui(PreferenceTabbedPane gui) {
		Container container = Box.createVerticalBox();
		container.add(guiSetting.getGui());
		container.add(processingSettings.getGui());
		container.add(new Box.Filler(new Dimension(), new Dimension(), new Dimension(0,Integer.MAX_VALUE)));
		synchronized (gui.getPluginPreference().getTabPane()) {
			gui.getPluginPreference().addSubTab(this, "PdfImport", new JScrollPane(container));
		}
		return;
	}

	@Override
	public boolean ok() {
		guiSetting.save();
		processingSettings.save();
		return false;
	}

	@Override
	public boolean isExpert() {
		return false;
	}
}