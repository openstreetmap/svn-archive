// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.measurement;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MainApplication;

public class MeasurementMode extends MapMode {

    private static final long serialVersionUID = 3853830673475744263L;

    public MeasurementMode(String name, String desc) {
        super(name, "measurement", desc, Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        putValue("toolbar", "measurement");
    }

    @Override
    public void enterMode() {
        super.enterMode();
        MainApplication.getMap().mapView.addMouseListener(this);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        MainApplication.getMap().mapView.removeMouseListener(this);
    }

    /**
     * If user clicked with the left button, add a node at the current mouse
     * position.
     *
     * If in nodesegment mode, add the node to the line segment by splitting the
     * segment. The new created segment will be inserted in every way the segment
     * was part of.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            MeasurementPlugin.getCurrentLayer().removeLastPoint();
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            Node coor = new Node(MainApplication.getMap().mapView.getEastNorth(e.getX(), e.getY()));
            if (coor.isOutSideWorld()) {
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),tr("Can not draw outside of the world."));
                return;
            }
            MeasurementPlugin.getCurrentLayer().mouseClicked(e);
        }
    }
}
