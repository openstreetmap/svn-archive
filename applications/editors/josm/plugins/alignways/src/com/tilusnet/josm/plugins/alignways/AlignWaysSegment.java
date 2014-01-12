/**
 * 
 */
package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.HashSet;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;

/**
 * @author tilusnet <tilusnet@gmail.com>
 * 
 */
public class AlignWaysSegment implements MapViewPaintable {

	protected WaySegment segment;
	protected MapView mapview;
	protected Color segmentColor = Color.WHITE;
	protected Collection<Node> segmentEndPoints;

	public AlignWaysSegment(MapView mapview, Point p) throws IllegalArgumentException {
		if (mapview == null)
			throw new IllegalArgumentException(tr(
					"Parameter ''{0}'' must not be null", "mapview"));
		if (p == null)
			throw new IllegalArgumentException(tr(
					"Parameter ''{0}'' must not be null", "p"));

		this.mapview = mapview;
	}

	void setSegment(WaySegment segment) {
		this.segment = segment;
		if (segment != null) {
			setSegmentEndpoints(segment);
			mapview.addTemporaryLayer(this);
		}
	}


	void setSegmentEndpoints(WaySegment segment) {
		if (segment != null) {
			Node node1 = segment.way.getNode(segment.lowerIndex);
			Node node2 = segment.way.getNode(segment.lowerIndex + 1);

			segmentEndPoints = new HashSet<Node>();
			segmentEndPoints.add(node1);
			segmentEndPoints.add(node2);

		}
	}

	protected WaySegment getNearestWaySegment(Point p) {

		return mapview.getNearestWaySegment(p, OsmPrimitive.isUsablePredicate);

	}

	public void destroy() {
		if (segment != null) {
			mapview.removeTemporaryLayer(this);
		}
	}

	public WaySegment getSegment() {
		return segment;
	}

	public Collection<Node> getSegmentEndPoints() {
		return segmentEndPoints;
	}

	@Override
	public void paint(Graphics2D g, MapView mv, Bounds bbox) {
		highlightSegment(segmentColor, g, mv);
	}

	protected void highlightSegment(Color c, Graphics2D g, MapView mv) {

		g.setColor(c);
		g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		drawSegment(g, mv);

	}

	protected void drawSegment(Graphics2D g, MapView mv) {
		Node n1 = segment.way.getNode(segment.lowerIndex);
		Node n2 = segment.way.getNode(segment.lowerIndex + 1);

		Line2D newline = new Line2D.Double(mv.getPoint(n1), mv.getPoint(n2));
		g.draw(newline);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	 @Override
	 public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((segment == null) ? 0 : segment.hashCode());
		result = prime * result
				+ ((segmentColor == null) ? 0 : segmentColor.hashCode());
		return result;
	 }

	 /*
	  * (non-Javadoc)
	  * 
	  * @see java.lang.Object#equals(java.lang.Object)
	  */
	 @Override
	 public boolean equals(Object obj) {
		 if (this == obj)
			 return true;
		 if (obj == null)
			 return false;
		 if (!(obj instanceof AlignWaysSegment))
			 return false;
		 AlignWaysSegment other = (AlignWaysSegment) obj;
		 if (segment == null) {
			 if (other.segment != null)
				 return false;
		 } else if (!segment.equals(other.segment))
			 return false;
		 /* Segment colour is ignored in comparison
        if (segmentColor == null) {
            if (other.segmentColor != null)
                return false;
        } else if (!segmentColor.equals(other.segmentColor))
            return false;
		  */
		 return true;
	 }
}
