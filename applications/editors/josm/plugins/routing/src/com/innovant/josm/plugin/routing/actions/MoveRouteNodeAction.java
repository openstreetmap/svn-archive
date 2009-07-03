/*
 * Copyright (C) 2008 Innovant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, please contact:
 *
 *  Innovant
 *   juangui@gmail.com
 *   vidalfree@gmail.com
 *
 *  http://public.grupoinnovant.com/blog
 *
 */

package com.innovant.josm.plugin.routing.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;

import com.innovant.josm.plugin.routing.RoutingLayer;
import com.innovant.josm.plugin.routing.RoutingModel;
import com.innovant.josm.plugin.routing.RoutingPlugin;
import com.innovant.josm.plugin.routing.gui.RoutingDialog;

/**
 * Accounts for the selection or unselection of the routing tool in the tool bar,
 * and the mouse events when this tool is selected
 * @author Juangui
 * @author Jose Vidal
 *
 */
public class MoveRouteNodeAction extends MapMode {
    /**
     * Serial.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Square of the distance radius where route nodes can be selected for dragging
     */
    private static final int DRAG_SQR_RADIUS = 100;

    /**
     * Logger.
     */
    static Logger logger = Logger.getLogger(RoutingLayer.class);

    /**
     * Routing Dialog.
     */
    private RoutingDialog routingDialog;

    /**
     * Index of dragged node
     */
    private int index;

    /**
     * Constructor
     * @param mapFrame
     */
    public MoveRouteNodeAction(MapFrame mapFrame) {
        // TODO Use constructor with shortcut
        super(tr("Routing"), "move",
                tr("Click and drag to move destination"),
                mapFrame, ImageProvider.getCursor("normal", "move"));
        this.routingDialog = RoutingPlugin.getInstance().getRoutingDialog();
    }

    @Override public void enterMode() {
        super.enterMode();
        Main.map.mapView.addMouseListener(this);
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
    }

    @Override public void mousePressed(MouseEvent e) {
        // If left button is pressed
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (Main.map.mapView.getActiveLayer() instanceof RoutingLayer) {
                RoutingLayer layer = (RoutingLayer)Main.map.mapView.getActiveLayer();
                RoutingModel routingModel = layer.getRoutingModel();
                // Search for the nearest node in the list
                List<Node> nl = routingModel.getSelectedNodes();
                index = -1;
                double dmax = DRAG_SQR_RADIUS; // maximum distance, in pixels
                for (int i=0;i<nl.size();i++) {
                    Node node = nl.get(i);
                    double d = Main.map.mapView.getPoint(node).distanceSq(e.getPoint());
                    if (d < dmax) {
                        dmax = d;
                        index = i;
                    }
                }
                if (index>=0)
                    logger.debug("Moved from node " + nl.get(index));
            }
        }
    }

    @Override public void mouseReleased(MouseEvent e) {
        // If left button is released and a route node is being dragged
        if ((e.getButton() == MouseEvent.BUTTON1) && (index>=0)) {
            searchAndReplaceNode(e.getPoint());
        }
    }

    @Override public void mouseDragged(MouseEvent e) {
    }

    private void searchAndReplaceNode(Point point) {
        if (Main.map.mapView.getActiveLayer() instanceof RoutingLayer) {
            RoutingLayer layer = (RoutingLayer)Main.map.mapView.getActiveLayer();
            RoutingModel routingModel = layer.getRoutingModel();
            // Search for nearest highway node
            Node node = null;
            node = layer.getNearestHighwayNode(point);
            if (node == null) {
                logger.debug("Didn't found a close node to move to.");
                return;
            }
            logger.debug("Moved to node " + node);
            routingModel.removeNode(index);
            routingDialog.removeNode(index);
            routingModel.insertNode(index, node);
            routingDialog.insertNode(index, node);
            Main.map.repaint();
        }
    }
}
