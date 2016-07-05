//License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant;

import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.validation.OsmValidator;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.pt_assistant.actions.AddStopPositionAction;
import org.openstreetmap.josm.plugins.pt_assistant.validation.PTAssistantValidatorTest;

/**
 * This is the main class of the PTAssistant plugin.
 *
 * @author darya / Darya Golovko
 * 
 */
public class PTAssistantPlugin extends Plugin {

	private JMenuItem addStopPositionMenu;

	/**
	 * Main constructor.
	 *
	 * @param info
	 *            Required information of the plugin. Obtained from the jar
	 *            file.
	 */
	public PTAssistantPlugin(PluginInformation info) {
		super(info);

		OsmValidator.addTest(PTAssistantValidatorTest.class);
		
		AddStopPositionAction addStopPositionAction = new AddStopPositionAction();
		addStopPositionMenu = MainMenu.add(Main.main.menu.toolsMenu, addStopPositionAction, false);

	}

	/**
	 * Called when the JOSM map frame is created or destroyed.
	 */
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (oldFrame == null && newFrame != null) {
			addStopPositionMenu.setEnabled(true);
		} else if (oldFrame != null && newFrame == null) {
			addStopPositionMenu.setEnabled(false);
		}
	}

}
