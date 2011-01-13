/*
 *      DummyAction.java
 *
 *      Copyright 2010 Hind <foxhind@gmail.com>
 *
 */

package commandline;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Collection;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;

public class DummyAction extends MapMode implements AWTEventListener {
	private CommandLine parentPlugin;

	public DummyAction(MapFrame mapFrame, CommandLine parentPlugin) {
		super(null, "addsegment.png", null, mapFrame, ImageProvider.getCursor("normal", null));
		this.parentPlugin = parentPlugin;
	}

	public void eventDispatched(AWTEvent arg0) {
		if (!(arg0 instanceof KeyEvent))
			return;
		KeyEvent ev = (KeyEvent) arg0;
        if (ev.getKeyCode() == KeyEvent.VK_ESCAPE && ev.getID() == KeyEvent.KEY_PRESSED) {
            ev.consume();
            cancelDrawing();
        }
    }

    public void cancelDrawing() {
        if (Main.map == null || Main.map.mapView == null)
            return;
        Main.map.statusLine.setHeading(-1);
        Main.map.statusLine.setAngle(-1);
        Main.map.mapView.repaint();
        updateStatusLine();
        parentPlugin.endInput();
    }
}
