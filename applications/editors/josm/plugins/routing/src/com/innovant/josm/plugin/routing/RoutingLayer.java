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

package com.innovant.josm.plugin.routing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.ImageProvider;

import com.innovant.josm.jrt.core.PreferencesKeys;
import com.innovant.josm.jrt.osm.OsmEdge;


/**
 * A JOSM layer that encapsulates the representation of the shortest path.
 * @author juangui
 * @author Jose Vidal
 */
public class RoutingLayer extends Layer {

    /**
     * Logger
     */
    static Logger logger = Logger.getLogger(RoutingLayer.class);

    /**
     * Constant
     */
    private static final double ARROW_PHI = Math.toRadians(20);

    /**
     * Routing Model
     */
    private RoutingModel routingModel;

    /**
     * Start, Middle and End icons
     */
    private Icon startIcon,middleIcon,endIcon;

    /**
     * Associated OSM layer
     */
    private OsmDataLayer dataLayer;

    /**
     * Default constructor
     * @param name Layer name.
     */
    public RoutingLayer(String name, OsmDataLayer dataLayer) {
        super(name);
        logger.debug("Creating Routing Layer...");
        if(startIcon == null) startIcon = ImageProvider.get("routing", "startflag");
        if(middleIcon == null) middleIcon = ImageProvider.get("routing", "middleflag");
        if(endIcon == null) endIcon = ImageProvider.get("routing", "endflag");
        this.dataLayer = dataLayer;
        this.routingModel = new RoutingModel(dataLayer.data);
        logger.debug("Routing Layer created.");
    }

    /**
     * Getter Routing Model.
     * @return the routingModel
     */
    public RoutingModel getRoutingModel() {
        return this.routingModel;
    }

    /**
     * Gets associated data layer
     * @return OsmDataLayer associated to the RoutingLayer
     */
    public OsmDataLayer getDataLayer() {
        return dataLayer;
    }

    /**
     * Gets nearest node belonging to a highway tagged way
     * @param p Point on the screen
     * @return The nearest highway node, in the range of the snap distance
     */
    public final Node getNearestHighwayNode(Point p) {
        Node nearest = null;
        double minDist = 0;
        for (Way w : dataLayer.data.ways) {
            if (w.deleted || w.incomplete || w.get("highway")==null) continue;
            for (Node n : w.nodes) {
                if (n.deleted || n.incomplete) continue;

                Point P = Main.map.mapView.getPoint(n.getEastNorth());
                double dist = p.distanceSq(P);
                if (dist < NavigatableComponent.snapDistance) {
                    if ((nearest == null) || (dist < minDist)) {
                        nearest = n;
                        minDist = dist;
                    }
                }
            }
        }
        return nearest;
    }

    /*
     * (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#getIcon()
     */
    @Override
    public Icon getIcon() {
        Icon icon = ImageProvider.get("layer", "routing_small");
        return icon;
    }

    /*
     * (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#getInfoComponent()
     */
    @Override
    public Object getInfoComponent() {
        String info = "<html>"
                        + "<body>"
                            +"Graph Vertex: "+this.routingModel.routingGraph.getVertexCount()+"<br/>"
                            +"Graph Edges: "+this.routingModel.routingGraph.getEdgeCount()+"<br/>"
                        + "</body>"
                    + "</html>";
        return info;
    }

    /*
     * (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#getMenuEntries()
     */
    @Override
    public Component[] getMenuEntries() {
        Collection<Component> components = new ArrayList<Component>();
        components.add(new JMenuItem(new LayerListDialog.ShowHideLayerAction(this)));
//        components.add(new JMenuItem(new LayerListDialog.ShowHideMarkerText(this)));
        components.add(new JMenuItem(new LayerListDialog.DeleteLayerAction(this)));
        components.add(new JSeparator());
        components.add(new JMenuItem(new RenameLayerAction(getAssociatedFile(), this)));
        components.add(new JSeparator());
        components.add(new JMenuItem(new LayerListPopup.InfoAction(this)));
        return components.toArray(new Component[0]);
    }

    /*
     * (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#getToolTipText()
     */
    @Override
    public String getToolTipText() {
        String tooltip = this.routingModel.routingGraph.getVertexCount() + " vertices, "
                + this.routingModel.routingGraph.getEdgeCount() + " edges";
        return tooltip;
    }

    /*
     * (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#isMergable(org.openstreetmap.josm.gui.layer.Layer)
     */
    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#mergeFrom(org.openstreetmap.josm.gui.layer.Layer)
     */
    @Override
    public void mergeFrom(Layer from) {
        // This layer is not mergable, so do nothing
    }

    /*
     * (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#paint(java.awt.Graphics, org.openstreetmap.josm.gui.MapView)
     */
    @Override
    public void paint(Graphics g, MapView mv) {
        boolean isActiveLayer = (mv.getActiveLayer().equals(this));
        // Get routing nodes (start, middle, end)
        List<Node> nodes = routingModel.getSelectedNodes();
        if(nodes == null || nodes.size() == 0) {
            logger.debug("no nodes selected");
            return;
        }

        // Get path stroke color from preferences
        // Color is different for active and inactive layers
        String colorString;
        if (isActiveLayer) {
            if (Main.pref.hasKey(PreferencesKeys.KEY_ACTIVE_ROUTE_COLOR.key))
                    colorString = Main.pref.get(PreferencesKeys.KEY_ACTIVE_ROUTE_COLOR.key);
            else {
                colorString = ColorHelper.color2html(Color.RED);
                Main.pref.put(PreferencesKeys.KEY_ACTIVE_ROUTE_COLOR.key, colorString);
            }
        } else {
            if (Main.pref.hasKey(PreferencesKeys.KEY_INACTIVE_ROUTE_COLOR.key))
                colorString = Main.pref.get(PreferencesKeys.KEY_INACTIVE_ROUTE_COLOR.key);
            else {
                colorString = ColorHelper.color2html(Color.decode("#dd2222"));
                Main.pref.put(PreferencesKeys.KEY_INACTIVE_ROUTE_COLOR.key, colorString);
            }
        }
        Color color = ColorHelper.html2color(colorString);

        // Get path stroke width from preferences
        String widthString = Main.pref.get(PreferencesKeys.KEY_ROUTE_WIDTH.key);
        if (widthString.length() == 0) {
            widthString = "8";
            // FIXME add after good width is found: Main.pref.put(KEY_ROUTE_WIDTH, widthString);
        }
        int width = Integer.parseInt(widthString);

        // Paint routing path
        List<OsmEdge> routeEdges = routingModel.getRouteEdges();
        if(routeEdges != null) {
            for(OsmEdge edge : routeEdges) {
                drawEdge(g, mv, edge, color, width, true);
            }
        }

        // paint start icon
        Node node = nodes.get(0);
        Point screen = mv.getPoint(node.getEastNorth());
        startIcon.paintIcon(mv, g, screen.x - startIcon.getIconWidth()/2,
                screen.y - startIcon.getIconHeight());

        // paint middle icons
        for(int index = 1; index < nodes.size() - 1; ++index) {
            node = nodes.get(index);
            screen = mv.getPoint(node.getEastNorth());
            middleIcon.paintIcon(mv, g, screen.x - startIcon.getIconWidth()/2,
                    screen.y - middleIcon.getIconHeight());
        }
        // paint end icon
        if(nodes.size() > 1) {
            node = nodes.get(nodes.size() - 1);
            screen = mv.getPoint(node.getEastNorth());
            endIcon.paintIcon(mv, g, screen.x - startIcon.getIconWidth()/2,
                    screen.y - endIcon.getIconHeight());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#visitBoundingBox(org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor)
     */
    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        for (Node node : routingModel.getSelectedNodes()) {
            v.visit(node);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#destroy()
     */
    @Override
    public void destroy() {
        routingModel.reset();
//      layerAdded = false;
    }

    /**
     * Draw a line with the given color.
     */
    private void drawEdge(Graphics g, MapView mv, OsmEdge edge, Color col, int width,
            boolean showDirection) {
        g.setColor(col);
        Point from;
        Point to;
        from = mv.getPoint(edge.fromEastNorth());
        to = mv.getPoint(edge.toEastNorth());

            Graphics2D g2d = (Graphics2D)g;
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(width)); // thickness
            g.drawLine(from.x, from.y, to.x, to.y);
            if (showDirection) {
                double t = Math.atan2(to.y-from.y, to.x-from.x) + Math.PI;
                g.drawLine(to.x,to.y, (int)(to.x + 10*Math.cos(t-ARROW_PHI)), (int)(to.y + 10*Math.sin(t-ARROW_PHI)));
                g.drawLine(to.x,to.y, (int)(to.x + 10*Math.cos(t+ARROW_PHI)), (int)(to.y + 10*Math.sin(t+ARROW_PHI)));
            }
            g2d.setStroke(oldStroke);
    }

}
