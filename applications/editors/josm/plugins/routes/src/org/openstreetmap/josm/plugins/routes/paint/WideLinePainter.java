package org.openstreetmap.josm.plugins.routes.paint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.BitSet;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.routes.ConvertedWay;
import org.openstreetmap.josm.plugins.routes.RouteDefinition;
import org.openstreetmap.josm.plugins.routes.RouteLayer;

public class WideLinePainter extends AbstractLinePainter {
	
	private static final float LINE_WIDTH = 10;
	private final RouteLayer layer;
	
	public WideLinePainter(RouteLayer layer) {
		this.layer = layer;
	}

	public void drawWay(ConvertedWay way, MapView mapView, Graphics2D g) {
		List<Node> nodes = way.getNodes();
		BitSet routes = way.getRoutes();
		
		if (nodes.size() < 2) {
			return;
		}
		
		double totalWidth = LINE_WIDTH + (routes.size() - 1) * 4;
		double width = totalWidth / routes.cardinality();
		double shift = -totalWidth / 2 + width / 2;
		
		for (int k=0; k<routes.length(); k++) {
			
			if (!routes.get(k)) {
				continue;
			}
			
			RouteDefinition route = layer.getRoutes().get(k);
	
			Color color = route.getColor();
			g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
			g.setStroke(new BasicStroke((float) width));
			
			GeneralPath path = new GeneralPath();
			Point2D start = shiftPoint(mapView.getPoint(nodes.get(0).getEastNorth()), 
					mapView.getPoint(nodes.get(1).getEastNorth()), shift);
			path.moveTo((float)start.getX(), (float)start.getY());
			for (int i=1; i<nodes.size() - 1; i++) {
				Point p1 = mapView.getPoint(nodes.get(i - 1).getEastNorth());
				Point p2 = mapView.getPoint(nodes.get(i).getEastNorth());
				Point p3 = mapView.getPoint(nodes.get(i + 1).getEastNorth());


				Line2D.Double line1 = shiftLine(p1, p2, shift);
				Line2D.Double line2 = shiftLine(p2, p3, shift);

				Point2D.Double intersection = new Point2D.Double();
				getLineLineIntersection(line1, line2, intersection);
				if (!Double.isNaN(intersection.getX())  && !Double.isNaN(intersection.getY())) {
					path.lineTo((float)intersection.getX(), (float)intersection.getY());
				}
			}
			Point2D stop = shiftPoint(mapView.getPoint(nodes.get(nodes.size() - 1).getEastNorth()), 
					mapView.getPoint(nodes.get(nodes.size() - 2).getEastNorth()), -shift);
			path.lineTo((float)stop.getX(), (float)stop.getY());
			g.draw(path);
			
			shift += width;
		}
	}

}
