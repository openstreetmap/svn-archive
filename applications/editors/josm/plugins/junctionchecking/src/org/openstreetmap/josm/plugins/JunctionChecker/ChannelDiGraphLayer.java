package org.openstreetmap.josm.plugins.JunctionChecker;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.Channel;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.OSMNode;
import org.openstreetmap.josm.plugins.JunctionChecker.reader.ColorSchemeXMLReader;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Diese Klasse enthält Teile der Klasse graphviewLayer des graphview Plugins
 *
 */
public class ChannelDiGraphLayer extends Layer implements LayerChangeListener, PropertyChangeListener{

	private ChannelDiGraph digraph;
	private static final int POINTSIZE = 5;//original 3
	private static final float LINEWIDTH = 4; //original 2
	private final ColorSchemeXMLReader cXMLReader;
	private static final Shape ARROW_HEAD;

	static {
		Polygon head = new Polygon();
		head.addPoint(  0,  0);
		head.addPoint(-15, +4);
		head.addPoint(-15, -4);
		ARROW_HEAD = head;
	}

	//die Farben für die zu zeichnenden Elemente
	private Color twoWayChannelColor;
	private Color oneWayChannelColor;
	private Color toNodeColor;
	private Color fromNodeColor;
	private Color nsccChannelColor;
	private Color selectedChannelColor;
	private Color partOfJunctionColor;

	public ChannelDiGraphLayer(ColorSchemeXMLReader cXMLReader){
		super("ChannelDiGraphLayer");
		MapView.addLayerChangeListener(this);
		this.cXMLReader = cXMLReader;
		initColors();
	}

	public void initColors() {
		twoWayChannelColor = cXMLReader.getColor("TwoWayChannel");
		oneWayChannelColor = cXMLReader.getColor("OneWayChannel");
		toNodeColor = cXMLReader.getColor("ToNode");
		fromNodeColor = cXMLReader.getColor("FromNode");
		nsccChannelColor = cXMLReader.getColor("NotConnectedChannel");
		selectedChannelColor = cXMLReader.getColor("selectedChannel");
		partOfJunctionColor = cXMLReader.getColor("partOfJunction");
	}

	@Override
	public Icon getIcon() {
		return ImageProvider.get("dialogs", "junctionchecker");
	}

	@Override
	public Object getInfoComponent() {
		return null;
	}

	@Override
	public Action[] getMenuEntries() {
		Action[] action = new Action[0];
		return action;
	}

	@Override
	public String getToolTipText() {
		return tr ("Channel-Digraph created from the active OSM-Layer");
	}

	@Override
	public boolean isMergable(Layer other) {
		return false;
	}

	@Override
	public void mergeFrom(Layer from) {
	}

	@Override
	public void paint(Graphics2D g, MapView mv, Bounds box) {
		if (digraph != null) {
			for (int i = 0; i < digraph.getChannels().size(); i++) {
				paintChannel(digraph.getChannelAtPosition(i), g, mv);
			}
		}
	}

	private void paintChannel(final Channel channel, final Graphics2D g, final MapView mv) {
		Point fromPoint = getCoord(channel.getFromNode(), mv);
		g.setColor(fromNodeColor);
		g.fillOval(fromPoint.x - POINTSIZE, fromPoint.y - POINTSIZE, 2 * POINTSIZE, 2 * POINTSIZE);
		Point toPoint = getCoord(channel.getToNode(), mv);
		g.setColor(toNodeColor);
		g.fillOval(toPoint.x - POINTSIZE, toPoint.y - POINTSIZE, 2 * POINTSIZE, 2 * POINTSIZE);
		Color c;
		if (channel.isPartOfJunction()) {
			c = partOfJunctionColor;
		}
		else if (channel.isSelected() == true) {
			c = selectedChannelColor;
		}
		else if (channel.isStrongConnected() == false) {
			c= nsccChannelColor;
		}
		else if (channel.getBackChannelID() != -100) {
			c = twoWayChannelColor;
		}
		else {
			c = oneWayChannelColor;
		}
		g.setColor(c);
		g.setStroke(new BasicStroke(LINEWIDTH));
		g.draw(new Line2D.Float(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y));
		double angle = angleFromXAxis(fromPoint, toPoint);
		Shape head = ARROW_HEAD;
		head = AffineTransform.getRotateInstance(angle).createTransformedShape(head);
		head = AffineTransform.getTranslateInstance(toPoint.x, toPoint.y).createTransformedShape(head);
		g.fill(head);

	}

	private Point getCoord(OSMNode node, MapView mv) {
		return mv.getPoint(Main.getProjection().latlon2eastNorth(new LatLon(node.getLatitude(), node.getLongitude())));
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
	public void visitBoundingBox(BoundingXYVisitor v) {
	}

	public void setDiGraph(ChannelDiGraph digraph) {
		this.digraph = digraph;
	}

	public void activeLayerChange(Layer oldLayer, Layer newLayer) {

	}

	public void layerAdded(Layer newLayer) {
	}

	public void layerRemoved(Layer oldLayer) {
		if (oldLayer == this) {
			MapView.removeLayerChangeListener(this);
		}
	}

	public ChannelDiGraph getDigraph() {
		return digraph;
	}

	public void setDigraph(ChannelDiGraph digraph) {
		this.digraph = digraph;
	}

	public void propertyChange(PropertyChangeEvent evt) {
	}
}
