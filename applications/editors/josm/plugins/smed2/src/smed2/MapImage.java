package smed2;

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

import seamap.MapHelper;
import seamap.Renderer;
import seamap.SeaMap.*;

public class MapImage extends ImageryLayer implements ZoomChangeListener, MapHelper {

	private Smed2Action dlg;

	double top;
	double bottom;
	double left;
	double right;
	double width;
	double height;
	int zoom;
	
	public MapImage(ImageryInfo info, Smed2Action d) {
		super(info);
		dlg = d;
		MapView.addZoomChangeListener(this);
		zoomChanged();
	}
	
	@Override
	public void destroy() {
		MapView.removeZoomChangeListener(this);
		super.destroy();
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
//		g2.setBackground(new Color(0xb5d0d0));
//		g2.clearRect(rect.x, rect.y, rect.width, rect.height);
		g2.setPaint(Color.black);
		g2.setFont(new Font("Arial", Font.BOLD, 20));
		g2.drawString(("Z" + zoom), (rect.x + rect.width - 40), (rect.y + rect.height - 10));
		Renderer.reRender(g2, zoom, Math.pow(2, (zoom-12)), dlg.map, this);
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
			zoom = ((int) Math.min(18, Math.max(9, Math.round(Math.floor(Math.log(4096 / bounds.asRect().width) / Math.log(2))))));
		}
	}

	public Point2D.Double getPoint(Snode coord) {
		return (Double) Main.map.mapView.getPoint2D(new LatLon(Math.toDegrees(coord.lat), Math.toDegrees(coord.lon)));
	}
}
