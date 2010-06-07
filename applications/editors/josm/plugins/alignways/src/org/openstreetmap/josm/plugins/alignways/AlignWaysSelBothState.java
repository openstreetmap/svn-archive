/**
 * 
 */
package org.openstreetmap.josm.plugins.alignways;

import org.openstreetmap.josm.Main;

/**
 * @author tilusnet <tilusnet@gmail.com>
 * 
 */
public class AlignWaysSelBothState extends AlignWaysState {

	@Override
	public void leftClick(AlignWaysMode alignWaysMode) {
		// No state change, nothing to do
	}

	@Override
	public void ctrlLClick(AlignWaysMode alignWaysMode) {
		// No state change, nothing to do
	}

	@Override
	public void setHelpText() {
		Main.map.statusLine
				.setHelpText("Shift-A: Align segments; Alt-click: Clear selection");
	}

}
