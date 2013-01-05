package smed2;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Action;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.gui.layer.ImageryLayer;

import seamap.SeaMap;
import symbols.Symbols;
import symbols.Buoys;

public class MapImage extends ImageryLayer implements ZoomChangeListener {

	private SeaMap mapdata;
	private int zoom;
	
	public MapImage(ImageryInfo info, SeaMap map) {
		super(info);
		MapView.addZoomChangeListener(this);
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
		double scale = Symbols.symbolScale[zoom]*Math.pow(2, (zoom-12));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		Symbols.drawSymbol(g2, Buoys.Pillar, scale, 768, 768, null, null);
		g2.drawString(String.valueOf(zoom), 768, 800);
	}

	@Override
	public void zoomChanged() {
		Bounds bounds = Main.map.mapView.getRealBounds();
		zoom = ((int)Math.min(18, Math.max(9, Math.round(Math.floor(Math.log(4096/bounds.asRect().width)/Math.log(2))))));
	}

}
