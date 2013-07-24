/*
 *      RelationAction.java
 *      
 *      Copyright 2011 Hind <foxhind@gmail.com>
 *      
 */

package CommandLine;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;

public class RelationAction extends MapMode implements AWTEventListener {
	private CommandLine parentPlugin;

	public RelationAction(MapFrame mapFrame, CommandLine parentPlugin) {
		super(null, "addsegment.png", null, mapFrame, ImageProvider.getCursor("normal", null));
		this.parentPlugin = parentPlugin;
	}

        @Override
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
        parentPlugin.abortInput();
    }
}
