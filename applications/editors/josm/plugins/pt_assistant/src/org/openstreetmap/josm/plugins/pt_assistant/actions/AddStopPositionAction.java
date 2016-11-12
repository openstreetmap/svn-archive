// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JoinNodeWayAction;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Action to add stop position and split the relevant way
 *
 * @author darya
 *
 */
public class AddStopPositionAction extends JosmAction {

    /**
     *
     */
    private static final long serialVersionUID = -5140181388906670207L;

    public AddStopPositionAction() {
        super(tr("Add stop position"), new ImageProvider("presets/transport", "bus.svg"), tr("Add stop position"),
                Shortcut.registerShortcut("Add stop position", tr("Add stop position"), KeyEvent.VK_T, Shortcut.NONE),
                false, "addStopPosition", false);

    }

    /**
     * Actions that add the new node, set tags and update the map frame.
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        if (!isEnabled() || !Main.isDisplayingMapView()) {
            return;
        }

        final ActionEvent actionEventParameter = e;

        Main.map.mapView.addMouseListener(new MouseAdapter() {

            LatLon clickPosition;

            @Override
            public void mouseClicked(MouseEvent e) {

                if (clickPosition == null) {
                    clickPosition = Main.map.mapView.getLatLon(e.getX(), e.getY());

                    Layer activeLayer = Main.getLayerManager().getActiveLayer();

                    if (activeLayer instanceof OsmDataLayer) {
                        OsmDataLayer osmDataLayer = (OsmDataLayer) activeLayer;
                        Node newNode = new Node(clickPosition);
                        newNode.put("bus", "yes");
                        newNode.put("public_transport", "stop_position");
                        osmDataLayer.data.addPrimitive(newNode);
                        osmDataLayer.data.setSelected(newNode);
                        Main.map.mapView.repaint();

                        // make the stop position node part of the way:
                        JoinNodeWayAction joinNodeWayAction = JoinNodeWayAction.createJoinNodeToWayAction();
                        joinNodeWayAction.actionPerformed(actionEventParameter);

                        // split the way:
                        SplitWayAction splitWayAction = new SplitWayAction();
                        splitWayAction.actionPerformed(actionEventParameter);

                    }

                    Main.map.mapView.removeMouseListener(this);
                    Main.map.mapView.removeMouseMotionListener(this);

                }
            }
        });

    }

}
