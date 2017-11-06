// License: GPL. For details, see LICENSE file.
package com.innovant.josm.plugin.routing.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.MouseEvent;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

import com.innovant.josm.plugin.routing.RoutingLayer;
import com.innovant.josm.plugin.routing.RoutingModel;
import com.innovant.josm.plugin.routing.RoutingPlugin;

/**
 * Accounts for the selection or unselection of the remove route nodes tool in the tool bar,
 * and the mouse events when this tool is selected
 * @author Juangui
 * @author Jose Vidal
 *
 */
public class RemoveRouteNodeAction extends MapMode {

    /**
     * Square of the distance radius where route nodes can be removed
     */
    private static final int REMOVE_SQR_RADIUS = 100;

    /**
     * Logger.
     */
    static Logger logger = Logger.getLogger(RoutingLayer.class);

    public RemoveRouteNodeAction() {
        // TODO Use constructor with shortcut
        super(tr("Routing"), "remove",
                tr("Click to remove destination"),
                ImageProvider.getCursor("normal", "delete"));
    }

    @Override public void enterMode() {
        super.enterMode();
        MainApplication.getMap().mapView.addMouseListener(this);
    }

    @Override public void exitMode() {
        super.exitMode();
        MainApplication.getMap().mapView.removeMouseListener(this);
    }

    @Override public void mouseClicked(MouseEvent e) {
        // If left button is clicked
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (MainApplication.getLayerManager().getActiveLayer() instanceof RoutingLayer) {
                RoutingLayer layer = (RoutingLayer) MainApplication.getLayerManager().getActiveLayer();
                RoutingModel routingModel = layer.getRoutingModel();
                // Search for the nearest node in the list
                List<Node> nl = routingModel.getSelectedNodes();
                int index = -1;
                double dmax = REMOVE_SQR_RADIUS; // maximum distance, in pixels
                for (int i = 0; i < nl.size(); i++) {
                    Node node = nl.get(i);
                    double d = MainApplication.getMap().mapView.getPoint(node).distanceSq(e.getPoint());
                    if (d < dmax) {
                        dmax = d;
                        index = i;
                    }
                }
                // If found a close node, remove it and recalculate route
                if (index >= 0) {
                    // Remove node
                    logger.debug("Removing node " + nl.get(index));
                    routingModel.removeNode(index);
                    RoutingPlugin.getInstance().getRoutingDialog().removeNode(index);
                    MainApplication.getMap().repaint();
                } else {
                    logger.debug("Can't find a node to remove.");
                }
            }
        }
    }

    @Override public boolean layerIsSupported(Layer l) {
        return l instanceof RoutingLayer;
    }

}
