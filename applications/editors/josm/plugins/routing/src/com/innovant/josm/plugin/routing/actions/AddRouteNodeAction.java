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

import java.awt.event.MouseEvent;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;

import org.openstreetmap.josm.gui.layer.Layer;

import com.innovant.josm.plugin.routing.RoutingLayer;
import com.innovant.josm.plugin.routing.RoutingPlugin;
import com.innovant.josm.plugin.routing.gui.RoutingDialog;

/**
 * Accounts for the selection or unselection of the routing tool in the tool bar,
 * and the mouse events when this tool is selected
 * @author Juangui
 * @author Jose Vidal
 *
 */
public class AddRouteNodeAction extends MapMode {
    /**
     * Serial.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logger.
     */
    static Logger logger = Logger.getLogger(AddRouteNodeAction.class);
    /**
     * Routing Dialog.
     */
    private RoutingDialog routingDialog;

    /**
     * Constructor
     * @param mapFrame
     */
    public AddRouteNodeAction(MapFrame mapFrame) {
        // TODO Use constructor with shortcut
        super(tr("Routing"), "add",
                tr("Click to add destination."),
                mapFrame, ImageProvider.getCursor("crosshair", null));
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

    @Override public void mouseClicked(MouseEvent e) {
        // If left button is clicked
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Search for nearest highway node
            Node node = null;
            if (Main.map.mapView.getActiveLayer() instanceof RoutingLayer) {
                RoutingLayer layer = (RoutingLayer)Main.map.mapView.getActiveLayer();
                node = layer.getNearestHighwayNode(e.getPoint());
                if(node == null) {
                    logger.debug("no selected node");
                    return;
                }
                logger.debug("selected node " + node);
                layer.getRoutingModel().addNode(node);
                routingDialog.addNode(node);
            }
        }
        Main.map.repaint();
    }
    @Override public boolean layerIsSupported(Layer l) {
        return l instanceof RoutingLayer;
    }
}
