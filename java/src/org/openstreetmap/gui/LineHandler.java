package org.openstreetmap.gui;

import org.openstreetmap.util.Line;

/**
 * Handles line segment editing
 *  
 * @author Imi
 */
public class LineHandler extends GuiHandler {

	public LineHandler(Line line) {
		super(line);
		updateBasic();
	}

	public void ok() {
		super.ok();
	}
}
