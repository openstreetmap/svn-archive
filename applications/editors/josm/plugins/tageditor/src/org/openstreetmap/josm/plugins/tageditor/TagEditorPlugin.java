package org.openstreetmap.josm.plugins.tageditor;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class TagEditorPlugin extends Plugin {

	LaunchAction action = null;
	
	/**
	 * constructor 
	 */
	public TagEditorPlugin(PluginInformation info) {
		super(info);
		action = new LaunchAction();
	}
}
