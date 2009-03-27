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
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
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
     * Icon Start Middle End
     */
    private Icon startIcon,middleIcon,endIcon;

    /**
     * Flag that manager activation layer
     */
    private boolean layerAdded = false;

    /**
     * Default constructor
     * @param name Layer name.
     */
	public RoutingLayer(String name) {
		super(name);
		logger.debug("Init Routing Layer");
        if(startIcon == null) startIcon = ImageProvider.get("routing", "startflag");
        if(middleIcon == null) middleIcon = ImageProvider.get("routing", "middleflag");
        if(endIcon == null) endIcon = ImageProvider.get("routing", "endflag");
        this.routingModel = new RoutingModel();
        logger.debug("End init Routing Layer");
	}

	/**
	 * Getter Routing Model.
	 * @return the routingModel
	 */
	public RoutingModel getRoutingModel() {
		return this.routingModel;
	}

	/**
	 * Check if layer is load.
	 * @return <code>true</code> Layer load.
	 *         <code>false</code> Layer don't load.
	 */
	public boolean isLayerAdded() {
		return layerAdded;
	}

	/**
	 * Setter layer active.
	 */
	public void setLayerAdded() {
		layerAdded = true;
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
        components.add(new JMenuItem(new LayerListDialog.ShowHideMarkerText(this)));
        components.add(new JMenuItem(new LayerListDialog.DeleteLayerAction(this)));
        components.add(new JSeparator());
        components.add(new JMenuItem(new RenameLayerAction(associatedFile, this)));
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
		if (routingModel.getRouteEdges() != null) {
			// If there's a calculated route
	        return "Showing calculated route. You can still add route nodes and compute a new route or delete layer to start a new route";
		} else if (routingModel.getSelectedNodes() != null) {
			// If there are some route nodes but not a calculated route
	        return "Keep selecting route nodes and compute route";
		}
        return "Select as many route nodes as you want and compute route";
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
		// Get routing nodes (start, middle, end)
        List<Node> nodes = routingModel.getSelectedNodes();
        if(nodes == null || nodes.size() == 0) {
        	logger.debug("no nodes selected");
            return;
        }

        // Get path stroke color from preferences
//		Main.pref.hasKey(PreferencesKeys.KEY_ROUTE_COLOR.key);
        String colorString = Main.pref.get(PreferencesKeys.KEY_ROUTE_COLOR.key);
        if(colorString.length() == 0) {
            colorString = ColorHelper.color2html(Color.RED);
            // FIXME add after good color is found: Main.pref.put(KEY_ROUTE_COLOR, colorString);
        }
        Color color = ColorHelper.html2color(colorString);

        // Get path stroke width from preferences
        String widthString = Main.pref.get(PreferencesKeys.KEY_ROUTE_WIDTH.key);
        if(widthString.length() == 0) {
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
        Point screen = mv.getPoint(node.eastNorth);
        startIcon.paintIcon(mv, g, screen.x - startIcon.getIconWidth()/2,
        		screen.y - startIcon.getIconHeight());

        // paint middle icons
        for(int index = 1; index < nodes.size() - 1; ++index) {
            node = nodes.get(index);
            screen = mv.getPoint(node.eastNorth);
            middleIcon.paintIcon(mv, g, screen.x - startIcon.getIconWidth()/2,
            		screen.y - middleIcon.getIconHeight());
        }
        // paint end icon
        if(nodes.size() > 1) {
            node = nodes.get(nodes.size() - 1);
            screen = mv.getPoint(node.eastNorth);
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
		layerAdded = false;
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
