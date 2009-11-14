package org.openstreetmap.josm.plugins.graphview.plugin.layer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerChangeListener;
import org.openstreetmap.josm.plugins.graphview.core.graph.GraphEdge;
import org.openstreetmap.josm.plugins.graphview.core.graph.GraphNode;
import org.openstreetmap.josm.plugins.graphview.core.graph.WayGraph;
import org.openstreetmap.josm.plugins.graphview.core.graph.WayGraphObserver;
import org.openstreetmap.josm.plugins.graphview.core.property.GraphEdgeSegments;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;
import org.openstreetmap.josm.plugins.graphview.core.transition.SegmentNode;
import org.openstreetmap.josm.plugins.graphview.core.util.GraphUtil;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.ColorScheme;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.LatLonCoords;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.NodePositioner;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.NonMovingNodePositioner;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.GraphViewPreferences;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * layer for displaying the graph visualization
 */
public class GraphViewLayer extends Layer implements LayerChangeListener, WayGraphObserver {

	private static final int NODE_RADIUS = 5;

	/**
	 * offset from real position if {@link GraphViewPreferences#getSeparateDirections()} is active,
	 * causes the graph nodes for the two directions to be visually distinguishable.
	 * Positive values move "forward" direction to the right.
	 */
	private static final double DIRECTIONAL_OFFSET = 20;

	private static final double OFFSET_ANGLE = 0.5 * Math.PI;

	private static final double MIN_QUAD_DISTANCE_FOR_OFFSET = 4;

	private static final boolean CONNECT_ALL_NODE_PAIRS = false;

	/** an arrow head that points along the x-axis to (0,0) */
	private static final Shape ARROW_HEAD;

	static {

		Polygon head = new Polygon();

		head.addPoint(  0,  0);
		head.addPoint(-15, +4);
		head.addPoint(-15, -4);

		ARROW_HEAD = head;
	}

	private WayGraph wayGraph = null;

	private ColorScheme colorScheme = null;
	private NodePositioner nodePositioner = new NonMovingNodePositioner();

	public GraphViewLayer() {
		super("Graph view");
		Layer.listeners.add(this);
	}

	/** sets the WayGraph that is to be displayed by this layer, may be null */
	public void setWayGraph(WayGraph wayGraph) {
		if (this.wayGraph != null) {
			this.wayGraph.deleteObserver(this);
		}
		this.wayGraph = wayGraph;
		if (wayGraph != null) {
			wayGraph.addObserver(this);
		}
	}

	/** sets the ColorScheme that is to be used for choosing colors, may be null */
	public void setColorScheme(ColorScheme colorScheme) {
		this.colorScheme = colorScheme;
		Main.panel.repaint();
	}

	/**
	 * sets the NodePositioner that is to be used for determining node placement,
	 * null will cause a {@link NonMovingNodePositioner} to be used.
	 */
	public void setNodePositioner(NodePositioner nodePositioner) {
		this.nodePositioner = nodePositioner;
		if (nodePositioner == null) {
			this.nodePositioner = new NonMovingNodePositioner();
		}
		Main.panel.repaint();
	}

	@Override
	public Icon getIcon() {
		return ImageProvider.get("layer", "graphview");
	}

	private void paintGraphNode(final GraphNode node, final Graphics g, final MapView mv) {

		Color color = colorScheme != null ? colorScheme.getNodeColor(node) : Color.LIGHT_GRAY;

		Point p = getNodePoint(node, mv);
		g.setColor(color);
		g.fillOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS, 2 * NODE_RADIUS, 2 * NODE_RADIUS);

	}

	private void paintGraphEdge(final GraphEdge e, final Graphics2D g2D, final MapView mv) {

		if (!CONNECT_ALL_NODE_PAIRS && GraphViewPreferences.getInstance().getSeparateDirections()) {

			//don't paint edges between nodes from the same SegmentNode and simply inverted Segment
			if (e.getStartNode().getSegmentNode() == e.getTargetNode().getSegmentNode()
					&& e.getStartNode().getSegment().getNode2() == e.getTargetNode().getSegment().getNode1()
					&& e.getStartNode().getSegment().getNode1() == e.getTargetNode().getSegment().getNode2()) {
				return;
			}

		}

		/* draw line(s) */

		g2D.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		List<Segment> edgeSegments = e.getPropertyValue(GraphEdgeSegments.PROPERTY);

		if (edgeSegments.size() > 0) {

			Segment firstSegment = edgeSegments.get(0);
			Segment lastSegment = edgeSegments.get(edgeSegments.size() - 1);

			//draw segments

			for (Segment segment : edgeSegments) {

				Color color = Color.WHITE;
				if (colorScheme != null) {
					color = colorScheme.getSegmentColor(segment);
				}
				g2D.setColor(color);

				Point p1 = getNodePoint(segment.getNode1(), mv);
				Point p2 = getNodePoint(segment.getNode2(), mv);

				if (segment == firstSegment) {
					p1 = getNodePoint(e.getStartNode(), mv);
				}
				if (segment == lastSegment) {
					p2 = getNodePoint(e.getTargetNode(), mv);
				}

				g2D.draw(new Line2D.Float(p1.x, p1.y, p2.x, p2.y));

			}

		} else {

			g2D.setColor(Color.WHITE);

			Point p1 = getNodePoint(e.getStartNode(), mv);
			Point p2 = getNodePoint(e.getTargetNode(), mv);

			g2D.draw(new Line2D.Float(p1.x, p1.y, p2.x, p2.y));

		}

		/* draw arrow head (note: color of last segment is still set) */

		{
			Point p1 = getNodePoint(e.getStartNode(), mv);
			Point p2 = getNodePoint(e.getTargetNode(), mv);

			if (edgeSegments.size() > 0) {
				Segment lastSegment = edgeSegments.get(edgeSegments.size() - 1);
				p1 = getNodePoint(lastSegment.getNode1(), mv);
			}

			double angle = angleFromXAxis(p1, p2); // angle between x-axis and [p1,p2]
			Shape head = ARROW_HEAD;
			head = AffineTransform.getRotateInstance(angle).createTransformedShape(head);
			head = AffineTransform.getTranslateInstance(p2.x, p2.y).createTransformedShape(head);

			g2D.fill(head);

		}
	}

	private Point getNodePoint(GraphNode node, MapView mv) {

		Point nodePoint = getNodePoint(nodePositioner.getPosition(node), mv);

		if (GraphViewPreferences.getInstance().getSeparateDirections()
				&& !GraphUtil.isEndNode(node)) {

			SegmentNode node1 = node.getSegment().getNode1();
			SegmentNode node2 = node.getSegment().getNode2();

			Point node1Point = getNodePoint(node1, mv);
			Point node2Point = getNodePoint(node2, mv);

			double segmentX = node2Point.getX() - node1Point.getX();
			double segmentY = node2Point.getY() - node1Point.getY();

			if (segmentX*segmentX + segmentY*segmentY >= MIN_QUAD_DISTANCE_FOR_OFFSET) {

				double rotatedX = Math.cos(OFFSET_ANGLE) * segmentX - Math.sin(OFFSET_ANGLE) * segmentY;
				double rotatedY = Math.sin(OFFSET_ANGLE) * segmentX + Math.cos(OFFSET_ANGLE) * segmentY;

				double segmentLength = Math.sqrt(rotatedX * rotatedX + rotatedY * rotatedY);

				double normalizedX = rotatedX / segmentLength;
				double normalizedY = rotatedY / segmentLength;

				nodePoint.x += DIRECTIONAL_OFFSET * normalizedX;
				nodePoint.y += DIRECTIONAL_OFFSET * normalizedY;

			}

		}

		return nodePoint;
	}
	private Point getNodePoint(SegmentNode node, MapView mv) {
		LatLonCoords coords = new LatLonCoords(node.getLat(), node.getLon());
		return getNodePoint(coords, mv);
	}
	private Point getNodePoint(LatLonCoords coords, MapView mv) {
		LatLon latLon = new LatLon(coords.getLat(), coords.getLon());
		EastNorth eastNorth = Main.proj.latlon2eastNorth(latLon);
		return mv.getPoint(eastNorth);
	}

	/**
	 * calculates the angle between the x axis and a vector given by two points
	 * @param p1  first point for vector; != null
	 * @param p2  second point for vector; != null
	 * @return  angle in radians, in range [-Pi .. +Pi]
	 */
	private double angleFromXAxis(Point p1, Point p2) {
		assert p1 != null && p2 != null;

		final float vecX = p2.x - p1.x;
		final float vecY = p2.y - p1.y;

		final float vecLength = (float)Math.sqrt(vecX*vecX + vecY*vecY);

		final float dotProductVecAxis = vecX;

		float angle = (float)Math.acos(dotProductVecAxis / vecLength);

		if (p2.y < p1.y) {
			angle = -angle;
		}

		assert -Math.PI*0.5 < angle && angle <= Math.PI*0.5;

		return angle;
	}

	@Override
	public void paint(final Graphics2D g, final MapView mv, Bounds bounds) {
		if (wayGraph != null) {

			for (GraphNode n : wayGraph.getNodes()) {
				paintGraphNode(n, g, mv);
			}

			for (GraphEdge e : wayGraph.getEdges()) {
				paintGraphEdge(e, g, mv);
			}

		}

	}

	@Override
	public String getToolTipText() {
		return "routing graph calculated by the GraphView plugin";
	}

	@Override
	public void mergeFrom(Layer from) {
		throw new AssertionError("GraphView layer is not mergable");
	}

	@Override
	public boolean isMergable(Layer other) {
		return false;
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor v) {
	}

	@Override
	public Object getInfoComponent() {
		return getToolTipText();
	}

	@Override
	public Component[] getMenuEntries() {
		return new Component[] { new JMenuItem(LayerListDialog.getInstance().createShowHideLayerAction(this)),
				new JMenuItem(LayerListDialog.getInstance().createDeleteLayerAction(this)), new JSeparator(),
				new JMenuItem(new RenameLayerAction(null, this)), new JSeparator(),
				new JMenuItem(new LayerListPopup.InfoAction(this)) };
	}

	public void update(WayGraph wayGraph) {
		assert wayGraph == this.wayGraph;
		Main.panel.repaint();
	}

	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		//do nothing
	}
	public void layerAdded(Layer newLayer) {
		//do nothing
	}
	public void layerRemoved(Layer oldLayer) {
		//do nothing
	}
}
