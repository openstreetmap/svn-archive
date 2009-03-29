package org.openstreetmap.josm.plugins.tageditor;

import org.openstreetmap.josm.plugins.Plugin;

public class TagEditorPlugin extends Plugin {

	LaunchAction action = null;
	
	/**
	 * constructor 
	 */
	public TagEditorPlugin() {
		action = new LaunchAction();
	}
}
