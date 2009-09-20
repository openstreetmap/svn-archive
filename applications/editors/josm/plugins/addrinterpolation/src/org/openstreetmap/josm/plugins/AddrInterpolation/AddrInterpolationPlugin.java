// License: GPL. Copyright 2009 by Mike Nice and others
package org.openstreetmap.josm.plugins.AddrInterpolation;


import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;

public class AddrInterpolationPlugin extends Plugin {

	AddrInterpolationAction action = null;

	/**
	 * constructor
	 */
	public AddrInterpolationPlugin() {
		action = new AddrInterpolationAction();
		Main.main.menu.toolsMenu.add(action);
	}


}
