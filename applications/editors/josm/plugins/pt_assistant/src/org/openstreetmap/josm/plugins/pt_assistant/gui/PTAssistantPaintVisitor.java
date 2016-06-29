package org.openstreetmap.josm.plugins.pt_assistant.gui;

import java.awt.Color;
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
				drawStop(rm.getMember(), Color.BLUE, Color.BLACK, (new Integer(stopCount)).toString());
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
			drawSegment(lastN, n, Color.BLUE);
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
		double cosT = 5 * Math.cos(t);
		double sinT = 5 * Math.sin(t);

		g.setColor(color);
		g.drawLine((int) (p1.x + cosT), (int) (p1.y - sinT), (int) (p2.x + cosT), (int) (p2.y - sinT));
		g.drawLine((int) (p1.x - cosT), (int) (p1.y + sinT), (int) (p2.x - cosT), (int) (p2.y + sinT));

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

	protected void drawStop(OsmPrimitive primitive, Color fillColor, Color outlineColor, String label) {

		// find the point to which the stop visualization will be linked:
		Node n = new Node(primitive.getBBox().getCenter());

		Point p = mv.getPoint(n);

		g.setColor(fillColor);
		// g.drawRect(p.x-10, p.y-10, 20, 20);
		g.fillOval(p.x - 5, p.y - 5, 10, 10);
		g.setColor(outlineColor);
		g.drawOval(p.x - 5, p.y - 5, 10, 10);

		g.setColor(Color.WHITE);
		g.drawString(label, p.x - 20, p.y - 20);
	}
}
