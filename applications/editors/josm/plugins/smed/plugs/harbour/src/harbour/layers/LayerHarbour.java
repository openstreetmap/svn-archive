package harbour.layers;

import java.awt.Graphics2D;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

public class LayerHarbour extends Layer {

	public LayerHarbour(String name) {
		super(name);
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
	public void paint(Graphics2D arg0, MapView arg1, Bounds arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor arg0) {
		// TODO Auto-generated method stub
		
	}

}
