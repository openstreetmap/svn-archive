package org.openstreetmap.josm.plugins.pt_assistant.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.validation.PaintVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class PTAssistantPaintVisitor extends PaintVisitor {

	/** The graphics */
	private final Graphics g;
	/** The MapView */
	private final MapView mv;

	public PTAssistantPaintVisitor(Graphics g, MapView mv) {
		super(g, mv);
		this.g = g;
		this.mv = mv;
	}

	@Override
	public void visit(Relation r) {

		int stopCount = 1;
		for (RelationMember rm : r.getMembers()) {
			if (RouteUtils.isPTStop(rm)) {
				String label = "";
				if (r.hasKey("ref")) {
					label = label + r.get("ref");
				} else if (r.hasKey("name")) {
					label = label + r.get("name");
				}
				label = label + "." + stopCount;
				drawStop(rm.getMember(), label);
				stopCount++;
			} else if (RouteUtils.isPTWay(rm)) {
				if (rm.isWay()) {
					visit(rm.getWay());
				} else if (rm.isRelation()) {
					visit(rm.getRelation());
				} else {
					// if the relation has members that do not fit with the
					// PT_Assistant data model, do nothing
				}
			} else {
				// if the relation has members that do not fit with the
				// PT_Assistant data model, do nothing
			}
		}

	}

	@Override
	public void visit(List<Node> nodes) {
		Node lastN = null;
		for (Node n : nodes) {
			if (lastN == null) {
				lastN = n;
				continue;
			}
			drawSegment(lastN, n, new Color(208, 80, 208, 179));
			lastN = n;
		}
	}

	/**
	 * Draw a small rectangle. White if selected (as always) or red otherwise.
	 *
	 * @param n
	 *            The node to draw.
	 */
	@Override
	public void visit(Node n) {
		if (n.isDrawable() && isNodeVisible(n)) {
			drawNode(n, Color.BLUE);
		}
	}

	/**
	 * Draws a line around the segment
	 *
	 * @param p1
	 *            The first point of segment
	 * @param p2
	 *            The second point of segment
	 * @param color
	 *            The color
	 */
	protected void drawSegment(Point p1, Point p2, Color color) {

		double t = Math.atan2((double) p2.x - p1.x, (double) p2.y - p1.y);
		double cosT = 8 * Math.cos(t);
		double sinT = 8 * Math.sin(t);

		int[] xPoints = { (int) (p1.x + cosT), (int) (p2.x + cosT), (int) (p2.x - cosT), (int) (p1.x - cosT) };
		int[] yPoints = { (int) (p1.y - sinT), (int) (p2.y - sinT), (int) (p2.y + sinT), (int) (p1.y + sinT) };
		g.setColor(color);
		g.fillPolygon(xPoints, yPoints, 4);
		g.fillOval((int) (p1.x - 8), (int) (p1.y - 8), 16, 16);
		g.fillOval((int) (p2.x - 8), (int) (p2.y - 8), 16, 16);

		// g.drawLine((int) (p1.x - cosT), (int) (p1.y - sinT), (int) (p2.x +
		// cosT), (int) (p2.y - sinT));
		// g.drawLine((int) (p1.x - cosT), (int) (p1.y + sinT), (int) (p2.x -
		// cosT), (int) (p2.y + sinT));

	}

	/**
	 * Draws a circle around the node
	 * 
	 * @param n
	 *            The node
	 * @param color
	 *            The circle color
	 */
	protected void drawNode(Node n, Color color) {

		Point p = mv.getPoint(n);

		g.setColor(color);
		g.drawOval(p.x - 5, p.y - 5, 10, 10);

	}

	protected void drawStop(OsmPrimitive primitive, String label) {

		// find the point to which the stop visualization will be linked:
		Node n = new Node(primitive.getBBox().getCenter());

		Point p = mv.getPoint(n);
		
		g.setColor(Color.WHITE);
		Font stringFont = new Font("SansSerif", Font.PLAIN, 24);
		g.setFont(stringFont);
		g.drawString(label, p.x + 20, p.y - 20);

		Color fillColor = null;

		if (primitive.hasTag("bus", "yes")) {
			fillColor = Color.BLUE;
		} else if (primitive.hasTag("tram", "yes")) {
			fillColor = Color.RED;
		} // TODO: add more options
		g.setColor(fillColor);

		if (primitive.hasTag("public_transport", "stop_position")) {
			g.fillOval(p.x - 8, p.y - 8, 16, 16);
		} else {
			g.fillRect(p.x - 8, p.y - 8, 16, 16);
		}


	}

	protected Graphics getGraphics() {
		return this.g;
	}

}
