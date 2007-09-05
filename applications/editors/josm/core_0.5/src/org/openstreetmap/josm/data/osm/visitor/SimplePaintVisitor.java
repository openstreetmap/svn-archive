// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.data.osm.visitor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.tools.ColorHelper;

/**
 * A visitor that paint a simple scheme of every primitive it visits to a 
 * previous set graphic environment.
 * 
 * @author imi
 */
public class SimplePaintVisitor implements Visitor {

	public final static Color darkerblue = new Color(0,0,96);
	public final static Color darkblue = new Color(0,0,128);
	public final static Color darkgreen = new Color(0,128,0);

	/**
	 * The environment to paint to.
	 */
	protected Graphics g;
	/**
	 * MapView to get screen coordinates.
	 */
	protected NavigatableComponent nc;
	
	public boolean inactive;

	protected static final double PHI = Math.toRadians(20);

	public void visitAll(DataSet data) {
		for (final OsmPrimitive osm : data.ways)
			if (!osm.deleted && !osm.selected)
				osm.visit(this);
		for (final OsmPrimitive osm : data.nodes)
			if (!osm.deleted && !osm.selected)
				osm.visit(this);
		for (final OsmPrimitive osm : data.getSelected())
			if (!osm.deleted)
				osm.visit(this);
	}

	/**
	 * Draw a small rectangle. 
	 * White if selected (as always) or red otherwise.
	 * 
	 * @param n The node to draw.
	 */
	public void visit(Node n) {
		Color color = null;
		if (inactive)
			color = getPreferencesColor("inactive", Color.DARK_GRAY);
		else if (n.selected)
			color = getPreferencesColor("selected", Color.WHITE);
		else
			color = getPreferencesColor("node", Color.RED);
		drawNode(n, color);
	}

	/**
	 * Draw a darkblue line for all segments.
	 * @param w The way to draw.
	 */
	public void visit(Way w) {
		Color wayColor;
		if (inactive)
			wayColor = getPreferencesColor("inactive", Color.DARK_GRAY);
		else {
			wayColor = getPreferencesColor("way", darkblue);
		}

		boolean showDirectionArrow = Main.pref.getBoolean("draw.segment.direction");
		boolean showOrderNumber = Main.pref.getBoolean("draw.segment.order_number");
		int orderNumber = 0;
		Node lastN = null;
		for (Node n : w.nodes) {
			if (lastN == null) {
				lastN = n;
				continue;
			}
			orderNumber++;
			drawSegment(lastN, n, w.selected && !inactive ? getPreferencesColor("selected", Color.WHITE) : wayColor, showDirectionArrow);
			if (showOrderNumber)
				drawOrderNumber(lastN, n, orderNumber);
			lastN = n;
		}
	}

	public void visit(Relation e) {
		// relations are not drawn.
	}
	/**
	 * Draw an number of the order of the two consecutive nodes within the
	 * parents way
	 */
	protected void drawOrderNumber(Node n1, Node n2, int orderNumber) {
		int strlen = (""+orderNumber).length();
		Point p1 = nc.getPoint(n1.eastNorth);
		Point p2 = nc.getPoint(n2.eastNorth);
		int x = (p1.x+p2.x)/2 - 4*strlen;
		int y = (p1.y+p2.y)/2 + 4;

		Rectangle screen = g.getClipBounds();
		if (screen.contains(x,y)) {
			Color c = g.getColor();
			g.setColor(getPreferencesColor("background", Color.BLACK));
			g.fillRect(x-1, y-12, 8*strlen+1, 14);
			g.setColor(c);
			g.drawString(""+orderNumber, x, y);
		}
    }

	/**
	 * Draw the node as small rectangle with the given color.
	 *
	 * @param n		The node to draw.
	 * @param color The color of the node.
	 */
	public void drawNode(Node n, Color color) {
		Point p = nc.getPoint(n.eastNorth);
		g.setColor(color);
		Rectangle screen = g.getClipBounds();

		if ( screen.contains(p.x, p.y) )
			g.drawRect(p.x-1, p.y-1, 2, 2);
	}

	/**
	 * Draw a line with the given color.
	 */
	protected void drawSegment(Node n1, Node n2, Color col, boolean showDirection) {
		g.setColor(col);
		Point p1 = nc.getPoint(n1.eastNorth);
		Point p2 = nc.getPoint(n2.eastNorth);
		
		Rectangle screen = g.getClipBounds();
		Line2D line = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
		if (screen.contains(p1.x, p1.y, p2.x, p2.y) || screen.intersectsLine(line))
		{
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
	
			if (showDirection) {
				double t = Math.atan2(p2.y-p1.y, p2.x-p1.x) + Math.PI;
		        g.drawLine(p2.x,p2.y, (int)(p2.x + 10*Math.cos(t-PHI)), (int)(p2.y + 10*Math.sin(t-PHI)));
		        g.drawLine(p2.x,p2.y, (int)(p2.x + 10*Math.cos(t+PHI)), (int)(p2.y + 10*Math.sin(t+PHI)));
			}
		}
	}

	public static Color getPreferencesColor(String colName, Color def) {
		String colStr = Main.pref.get("color."+colName);
		if (colStr.equals("")) {
			Main.pref.put("color."+colName, ColorHelper.color2html(def));
			return def;
		}
		return ColorHelper.html2color(colStr);
	}


	public void setGraphics(Graphics g) {
    	this.g = g;
    }

	public void setNavigatableComponent(NavigatableComponent nc) {
    	this.nc = nc;
    }
}
