/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seachart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import javax.swing.Action;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.gui.layer.ImageryLayer;

import render.ChartContext;
import render.Renderer;
import render.Rules.RuleSet;
import s57.S57map.*;

public class ChartImage extends ImageryLayer implements ZoomChangeListener, ChartContext {

	double top;
	double bottom;
	double left;
	double right;
	double width;
	double height;
	int zoom;
	
	public ChartImage(ImageryInfo info) {
		super(info);
		MapView.addZoomChangeListener(this);
		zoomChanged();
	}
	
	@Override
	public Action[] getMenuEntries() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return null;
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor arg0) {
	}

	@Override
	public void paint(Graphics2D g2, MapView mv, Bounds bb) {
		Rectangle rect = Main.map.mapView.getBounds();
		Renderer.reRender(g2, RuleSet.ALL, rect, zoom, Math.pow(2, (zoom-12)), SeachartAction.map, this);
		g2.setPaint(Color.black);
		g2.setFont(new Font("Arial", Font.BOLD, 20));
		g2.drawString(("Z" + zoom), (rect.x + rect.width - 40), (rect.y + rect.height - 10));
	}

	@Override
	public void zoomChanged() {
		if ((Main.map != null) && (Main.map.mapView != null)) {
			Bounds bounds = Main.map.mapView.getRealBounds();
			top = bounds.getMax().lat();
			bottom = bounds.getMin().lat();
			left = bounds.getMin().lon();
			right = bounds.getMax().lon();
			width = Main.map.mapView.getBounds().getWidth();
			height = Main.map.mapView.getBounds().getHeight();
			zoom = ((int) Math.min(18, Math.max(9, Math.round(Math.floor(Math.log(1024 / bounds.asRect().height) / Math.log(2))))));
		}
	}

	public Point2D.Double getPoint(Snode coord) {
		return (Double) Main.map.mapView.getPoint2D(new LatLon(Math.toDegrees(coord.lat), Math.toDegrees(coord.lon)));
	}

	public double mile(Feature feature) {
		return 185000 / Main.map.mapView.getDist100Pixel();
	}
}
