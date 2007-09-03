// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.actions.mapmode;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * A class implementing MapMode is able to be selected as an mode for map editing.
 * As example scrolling the map is a MapMode, connecting Nodes to new Segments
 * is another.
 * 
 * MapModes should register/deregister all necessary listener on the map's view
 * control. 
 */
abstract public class MapMode extends JosmAction implements MouseListener, MouseMotionListener {
	private final Cursor cursor;
	private Cursor oldCursor;

	/**
	 * Constructor for mapmodes without an menu
	 */
	public MapMode(String name, String iconName, String tooltip, int keystroke, MapFrame mapFrame, Cursor cursor) {
		super(name, "mapmode/"+iconName, tooltip, keystroke, 0, false);
		this.cursor = cursor;
		putValue("active", false);
	}

	/**
	 * Constructor for mapmodes with an menu (no shortcut will be registered)
	 */
	public MapMode(String name, String iconName, String tooltip, MapFrame mapFrame, Cursor cursor) {
		putValue(NAME, name);
		putValue(SMALL_ICON, ImageProvider.get("mapmode", iconName));
		putValue(SHORT_DESCRIPTION, tooltip);
		this.cursor = cursor;
	}

	public void enterMode() {
		putValue("active", true);
		oldCursor = Main.map.mapView.getCursor();
		Main.map.mapView.setCursor(cursor);
		
	}
	public void exitMode() {
		putValue("active", false);
		Main.map.mapView.setCursor(oldCursor);
	}

	/**
	 * Call selectMapMode(this) on the parent mapFrame.
	 */
	public void actionPerformed(ActionEvent e) {
		if (Main.map != null)
			Main.map.selectMapMode(this);
	}

	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
}
