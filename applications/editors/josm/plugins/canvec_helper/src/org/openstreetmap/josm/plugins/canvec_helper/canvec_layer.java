package org.openstreetmap.josm.plugins.canvec_helper;

import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.gui.layer.Layer;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Point;
import java.awt.Color;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.Icon;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.MapView;
import java.util.ArrayList;
import java.util.zip.ZipException;

// most of the layout was copied from the openstreetbugs plugin to get things started
public class canvec_layer extends Layer implements MouseListener {
	canvec_helper plugin_self;
	private ArrayList<CanVecTile> tiles = new ArrayList<CanVecTile>();
	public canvec_layer(String name,canvec_helper self){
		super(name);
		plugin_self = self;
		this.setBackgroundLayer(true);
		for (int i = 0; i < 119; i++) {
			CanVecTile tile = new CanVecTile(i,"",0,"",plugin_self);
			if (tile.isValid()) tiles.add(tile);
		}
	}
	public Action[] getMenuEntries() {
		return new Action[]{
			LayerListDialog.getInstance().createShowHideLayerAction(),
			LayerListDialog.getInstance().createDeleteLayerAction(),
			SeparatorLayerAction.INSTANCE,
			new LayerListPopup.InfoAction(this)};
	}
	public Object getInfoComponent() {
		return getToolTipText();
	}
	public String getToolTipText() {
		return tr("canvec tile helper");
	}
	public void visitBoundingBox(BoundingXYVisitor v) {}
	public boolean isMergable(Layer other) {
		return false;
	}
	public void mergeFrom(Layer from) {}
	public Icon getIcon() {
		return null; // FIXME
	}
	public void paint(Graphics2D g, MapView mv, Bounds bounds) {
		long start = System.currentTimeMillis();
		System.out.println("painting the area covered by "+bounds.toString());
		// loop over each canvec tile in the db and check bounds.intersects(Bounds)
		g.setColor(Color.red);
		for (int i = 0; i < tiles.size(); i++) {
			CanVecTile tile = tiles.get(i);
			tile.paint(g,mv,bounds);
		}
		long end = System.currentTimeMillis();
		System.out.println((end-start)+"ms spent");
	}
	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {
		System.out.println("click!");
	}
}
