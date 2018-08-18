// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.commandline;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;

public class RelationAction extends MapMode implements AWTEventListener {
    private final CommandLine parentPlugin;

    public RelationAction(CommandLine parentPlugin) {
        super(null, "addsegment.png", null, ImageProvider.getCursor("normal", null));
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
        if (!MainApplication.isDisplayingMapView())
            return;
        MapFrame map = MainApplication.getMap();
        map.statusLine.setHeading(-1);
        map.statusLine.setAngle(-1);
        map.mapView.repaint();
        updateStatusLine();
        parentPlugin.abortInput();
    }
}
