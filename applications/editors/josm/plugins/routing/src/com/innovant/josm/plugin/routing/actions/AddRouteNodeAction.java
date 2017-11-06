// License: GPL. For details, see LICENSE file.
package com.innovant.josm.plugin.routing.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.MouseEvent;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

import com.innovant.josm.plugin.routing.RoutingLayer;
import com.innovant.josm.plugin.routing.RoutingPlugin;

/**
 * Accounts for the selection or unselection of the routing tool in the tool bar,
 * and the mouse events when this tool is selected
 * @author Juangui
 * @author Jose Vidal
 *
 */
public class AddRouteNodeAction extends MapMode {

    /**
     * Logger.
     */
    static Logger logger = Logger.getLogger(AddRouteNodeAction.class);

    /**
     * Constructor
     * @param mapFrame map frame
     */
    public AddRouteNodeAction() {
        // TODO Use constructor with shortcut
        super(tr("Routing"), "add",
                tr("Click to add destination."),
                ImageProvider.getCursor("crosshair", null));
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
            // Search for nearest highway node
            Node node = null;
            if (MainApplication.getLayerManager().getActiveLayer() instanceof RoutingLayer) {
                RoutingLayer layer = (RoutingLayer) MainApplication.getLayerManager().getActiveLayer();
                node = layer.getNearestHighwayNode(e.getPoint());
                if (node == null) {
                    logger.debug("no selected node");
                    return;
                }
                logger.debug("selected node " + node);
                layer.getRoutingModel().addNode(node);
                RoutingPlugin.getInstance().getRoutingDialog().addNode(node);
            }
        }
        MainApplication.getMap().repaint();
    }

    @Override public boolean layerIsSupported(Layer l) {
        return l instanceof RoutingLayer;
    }
}
