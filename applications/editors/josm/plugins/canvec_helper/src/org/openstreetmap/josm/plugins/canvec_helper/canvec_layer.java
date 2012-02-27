package org.openstreetmap.josm.plugins.canvec_helper;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
//import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.io.MirroredInputStream;
import static org.openstreetmap.josm.tools.I18n.tr;

// most of the layout was copied from the openstreetbugs plugin to get things started
public class canvec_layer extends Layer implements MouseListener {
	private Icon layerIcon = null;
	private int max_zoom = 4;
	public canvec_helper plugin_self;
	private ArrayList<CanVecTile> tiles = new ArrayList<CanVecTile>();
	public ArrayList<CanVecTile> downloadable = new ArrayList<CanVecTile>();
	public ArrayList<CanVecTile> openable = new ArrayList<CanVecTile>();

	public canvec_layer(String name,canvec_helper self){
		super(name);
		plugin_self = self;
		this.setBackgroundLayer(true);
/*		for (int i = 0; i < 119; i++) {
			CanVecTile tile = new CanVecTile(i,"",0,"",plugin_self);
			if (tile.isValid()) tiles.add(tile);
		}*/
		layerIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(getClass().getResource("/images/layericon.png")));
		try {
			long start = System.currentTimeMillis();
			Pattern p = Pattern.compile("(\\d\\d\\d)([A-Z]\\d\\d).*");
			MirroredInputStream index = new MirroredInputStream("http://ftp2.cits.rncan.gc.ca/osm/pub/ZippedOsm.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(index));
			String line;
			int last_cell = -1;
			ArrayList<String> list = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				Matcher m = p.matcher(line);
				if (m.find()) {
					int cell = Integer.parseInt(m.group(1));
					if (cell == last_cell) {
						list.add(m.group(0));
					} else if (last_cell != -1) {
						CanVecTile tile = new CanVecTile(last_cell,"",0,"",this,list);
						if (tile.isValid()) tiles.add(tile);
						list = new ArrayList<String>();
						list.add(m.group(0));
					}
					last_cell = cell;
				} else System.out.print("bad line '" + line + "'\n");
			}
			CanVecTile tile = new CanVecTile(last_cell,"",0,"",this,list);
			if (tile.isValid()) tiles.add(tile);

			long end = System.currentTimeMillis();
			System.out.println((end-start)+"ms spent");
		} catch (IOException e) {
			System.out.println("exception getting index");
		}
	}
	public Action[] getMenuEntries() {
		return new Action[]{
			LayerListDialog.getInstance().createShowHideLayerAction(),
			LayerListDialog.getInstance().createDeleteLayerAction(),
			SeparatorLayerAction.INSTANCE,
			new LayerListPopup.InfoAction(this),
			new MaxZoomAction(this),
			new DownloadCanvecAction(this),
			new OpenOsmAction(this)};
	}
	public class MaxZoomAction extends AbstractAction implements LayerAction {
		private canvec_layer parent;
		public MaxZoomAction(canvec_layer parent) {
			this.parent = parent;
		}
		public void actionPerformed(ActionEvent e) {}
		public boolean supportLayers(List<Layer> layers) {
			return false;
		}
		public Component createMenuComponent() {
			JMenu max_zoom = new JMenu("max zoom");
			max_zoom.add(new JMenuItem(new SetMaxZoom(parent,1)));
			max_zoom.add(new JMenuItem(new SetMaxZoom(parent,2)));
			max_zoom.add(new JMenuItem(new SetMaxZoom(parent,3)));
			max_zoom.add(new JMenuItem(new SetMaxZoom(parent,4)));
			return max_zoom;
		}
	}
	private class AllowDownload extends AbstractAction {
		CanVecTile tile;
		public AllowDownload(CanVecTile tile) {
			super(tile.tileid);
			this.tile = tile;
		}
		public void actionPerformed(ActionEvent arg0) {
			tile.can_download = true;
		}
	}
	private class OpenOsmAction extends AbstractAction implements LayerAction {
		private canvec_layer layer;
		public OpenOsmAction(canvec_layer layer) {
			this.layer = layer;
		}
		public void actionPerformed(ActionEvent e) {}
		public Component createMenuComponent() {
			JMenu OpenOsm = new JMenu("Open tile");
			for (int i = 0; i < layer.openable.size(); i++) {
				OpenOsm.add(new JMenuItem(new DoOpenOsm(layer.openable.get(i))));
			}
			return OpenOsm;
		}
		public boolean supportLayers(List<Layer> layers) {
			return false;
		}
	}
	private class DoOpenOsm extends AbstractAction {
		CanVecTile tile;
		public DoOpenOsm(CanVecTile tile) {
			super(tile.tileid);
			this.tile = tile;
		}
		public void actionPerformed(ActionEvent e) {
			tile.load_raw_osm();
		}
	}
	private class DownloadCanvecAction extends AbstractAction implements LayerAction {
		private canvec_layer parent;
		public DownloadCanvecAction(canvec_layer parent) {
			this.parent = parent;
		}
		public void actionPerformed(ActionEvent e) {}
		public boolean supportLayers(List<Layer> layers) {
			return false;
		}
		public Component createMenuComponent() {
			JMenu downloadCanvec = new JMenu("Download zip's");
			for (int i = 0; i < parent.downloadable.size(); i++) {
				downloadCanvec.add(new JMenuItem(new AllowDownload(parent.downloadable.get(i))));
			}
			return downloadCanvec;
		}
	}
	public void setMaxZoom(int max_zoom) {
		this.max_zoom = max_zoom;
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
	public Icon getIcon() { return layerIcon; }
	public void paint(Graphics2D g, MapView mv, Bounds bounds) {
		//long start = System.currentTimeMillis();
		//System.out.println("painting the area covered by "+bounds.toString());
		downloadable = new ArrayList<CanVecTile>();
		openable = new ArrayList<CanVecTile>();
		// loop over each canvec tile in the db and check bounds.intersects(Bounds)
		g.setColor(Color.red);
		for (int i = 0; i < tiles.size(); i++) {
			CanVecTile tile = tiles.get(i);
			tile.paint(g,mv,bounds,max_zoom);
		}
		long end = System.currentTimeMillis();
		//System.out.println((end-start)+"ms spent");
	}
	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {
		System.out.println("click!");
	}
}
