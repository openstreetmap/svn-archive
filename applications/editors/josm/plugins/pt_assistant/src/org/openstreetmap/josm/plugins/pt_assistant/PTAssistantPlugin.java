//License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant;

import org.openstreetmap.josm.data.validation.OsmValidator;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.pt_assistant.validation.DirectionTest;
import org.openstreetmap.josm.plugins.pt_assistant.validation.GapTest;
import org.openstreetmap.josm.plugins.pt_assistant.validation.PlatformsFirstTest;
import org.openstreetmap.josm.plugins.pt_assistant.validation.RoadTypeTest;

/**
 * This is the main class of the PTAssistant plugin.
 *
 * @author darya / Darya Golovko
 * 
 */
public class PTAssistantPlugin extends Plugin {

	/**
	 * Main constructor.
	 *
	 * @param info
	 *            Required information of the plugin. Obtained from the jar
	 *            file.
	 */
	public PTAssistantPlugin(PluginInformation info) {
		super(info);

		OsmValidator.addTest(PlatformsFirstTest.class);
		OsmValidator.addTest(RoadTypeTest.class);
		OsmValidator.addTest(DirectionTest.class);
		OsmValidator.addTest(GapTest.class);

	}



}
