package harbour.layers;

import static java.lang.Math.sin;
import static java.lang.Math.cos;

import harbour.panels.PanelSearchPois;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComboBox;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;

public class LayerHarbour extends OsmDataLayer implements LayerChangeListener {
	
	private JComboBox layerBox		= null;
	private PanelSearchPois panel	= null;
	private static DataSet ds		= new DataSet();
	private double radius			= -1.0;
	private LatLon lm				= null;
	private boolean isChanged		= true;
	
	public LayerHarbour(String name,PanelSearchPois panel) {
		super(ds, name, null);
		
		this.panel = panel;
		panel.setPois(ds);
		layerBox = panel.getLayerComboBox();
	}

	@Override
	public Icon getIcon() {
		return ImageProvider.get("layer", "Hbr_16x14");
	}

	@Override
	public Object getInfoComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action[] getMenuEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMergable(Layer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void mergeFrom(Layer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void paint(Graphics2D g, MapView mv, Bounds box) {
		isChanged = false;
		
		Point mp = null;
		Point mx = null;
				
		if(radius > 0.0 && lm != null) {
			LatLon lx = new LatLon(lm.lat(), lm.lon() + radius/65.0);
			
			mp = mv.getPoint(lm);
			mx = mv.getPoint(lx);
		
			int r = mx.x - mp.x;

			g.setColor(Color.RED);
			g.setStroke(new BasicStroke(3));
		
			g.drawOval(mp.x-r, mp.y-r, 2*r, 2*r);
			
			g.setStroke(new BasicStroke(1));
		}
		
		super.paint(g, mv, box);		
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if(newLayer instanceof OsmDataLayer) { 
			String n  = newLayer.getName();
			
			setName("Harbour - " + n);
			layerBox.setSelectedItem(n);
		}
		else setName("Harbour");
	}

	@Override
	public void layerAdded(Layer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layerRemoved(Layer arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setRadius(double r) {
		if(radius != r){
			radius = r;
			
			isChanged = true;
			Main.map.repaint();
		}
	}
	
	public boolean isChanged() { return isChanged; }
	public void setChanged(Boolean state) { isChanged = state; }
	
	public void setCenter(LatLon coor) {
		if(lm != coor) {
			lm = coor;
			
			isChanged = true;
			Main.map.repaint();
		}
	}

	public boolean isNodeinCircle(Node n) {
		if(radius < 0.0) return true;

		LatLon coor = n.getCoor();
		
		if(coor == null) return false;
		return coor.greatCircleDistance(lm) < radius * 1000;
	}
}
