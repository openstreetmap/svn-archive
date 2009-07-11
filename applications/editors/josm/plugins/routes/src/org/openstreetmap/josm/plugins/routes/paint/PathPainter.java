package org.openstreetmap.josm.plugins.routes.paint;

import java.awt.Graphics2D;

import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.routes.ConvertedWay;

public interface PathPainter {
	
	public void drawWay(ConvertedWay way, MapView mapView, Graphics2D g);

}
