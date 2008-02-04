package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.actions.SaveActionBase;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.data.coor.EastNorth;

/**
 * This is a layer that grabs the current screen from an WMS server. The data
 * fetched this way is tiled and managerd to the disc to reduce server load.
 */
public class WMSLayer extends Layer {

	protected static Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(WMSPlugin.class.getResource("/images/wms_small.png")));
	protected final ArrayList<WMSImage> wmsImages;
	protected final String url;
	protected final int serializeFormatVersion = 1;
 
	public WMSLayer() {
		super("Blank Layer");
		wmsImages = new ArrayList<WMSImage>();
		url = "";
	}
	public WMSLayer(String name, String url) {
		super(name);
		// to calculate the world dimension, we assume that the projection does
		// not have problems with translating longitude to a correct scale.
		// Next to that, the projection must be linear dependend on the lat/lon
		// unprojected scale.
		if (Projection.MAX_LON != 180)
			throw new IllegalArgumentException(tr
					("Wrong longitude transformation for tile manager. "+
							"Can't operate on {0}",Main.proj));

		this.url = url;
		//wmsImage = new WMSImage(url);
		wmsImages = new ArrayList<WMSImage>();
	}

	public void grab() throws IOException
	{
		MapView mv = Main.map.mapView;
		WMSImage wmsImage = new WMSImage(url);
		wmsImage.grab(mv);
		wmsImages.add(wmsImage);
	}

	public void grab(double minlat,double minlon,double maxlat,double maxlon)
	throws IOException
	{
		MapView mv = Main.map.mapView;
		WMSImage wmsImage = new WMSImage(url);
		wmsImage.grab(mv,minlat,minlon,maxlat,maxlon);
		wmsImages.add(wmsImage);
	}

	@Override public Icon getIcon() {
		return icon;
	}

	@Override public String getToolTipText() {
		return tr("WMS layer ({0}), {1} tile(s) loaded", name, wmsImages.size());
	}

	@Override public boolean isMergable(Layer other) {
		return false;
	}

	@Override public void mergeFrom(Layer from) {
	}

	@Override public void paint(Graphics g, final MapView mv) {
		for(WMSImage wmsImage : wmsImages) {
			wmsImage.paint(g,mv);
		}
	}

	@Override public void visitBoundingBox(BoundingXYVisitor v) {
		// doesn't have a bounding box
	}

	@Override public Object getInfoComponent() {
		return getToolTipText();
	}

	@Override public Component[] getMenuEntries() {
		return new Component[]{
				new JMenuItem(new LayerListDialog.ShowHideLayerAction(this)),
				new JMenuItem(new LayerListDialog.DeleteLayerAction(this)),				
				new JMenuItem(new LoadWmsAction()),
				new JMenuItem(new SaveWmsAction()),
				new JSeparator(),
				new JMenuItem(new LayerListPopup.InfoAction(this))};
	}

	public WMSImage findImage(EastNorth eastNorth)
	{
		for(WMSImage wmsImage : wmsImages) {
			if (wmsImage.contains(eastNorth))  {
				return wmsImage;
			}
		}
		return null;
	}

	//to enable the removal of the images when the layer is removed.
	public void destroy() {
		wmsImages.clear();
	}
	
	public class SaveWmsAction extends AbstractAction {
		public SaveWmsAction() {
			super(tr("Save WMS layer to file"), ImageProvider.get("save"));
		}
		public void actionPerformed(ActionEvent ev) {
			File f = openFileDialog(false);
			try {
				FileOutputStream fos = new FileOutputStream(f);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeInt(serializeFormatVersion);
				oos.writeInt(wmsImages.size());
				for (WMSImage w : wmsImages) {
					oos.writeObject(w);
				}
				oos.close();
				fos.close();
			} catch (Exception ex) {
				ex.printStackTrace(System.out);
			}
		}
	}
	
	public class LoadWmsAction extends AbstractAction {
		public LoadWmsAction() {
			super(tr("Load WMS layer from file"), ImageProvider.get("load"));
		}
		public void actionPerformed(ActionEvent ev) {
			File f = openFileDialog(true);
			if (f == null) return;
			try {
				FileInputStream fis = new FileInputStream(f);
				ObjectInputStream ois = new ObjectInputStream(fis);
				int sfv = ois.readInt();
				if (sfv != serializeFormatVersion) {
					JOptionPane.showMessageDialog(Main.parent, 
						tr("Unsupported WMS file version; found {0}, expected {1}", sfv, serializeFormatVersion),
						tr("File Format Error"), 
						JOptionPane.ERROR_MESSAGE);
					return;
				}
				int numImg = ois.readInt();
				for (int i=0; i< numImg; i++) {
					WMSImage img = (WMSImage) ois.readObject();
					wmsImages.add(img);
				}
				ois.close();
				fis.close();
			} catch (Exception ex) {
				// FIXME be more specific
				ex.printStackTrace(System.out);
				JOptionPane.showMessageDialog(Main.parent, 
						tr("Error loading file"),
						tr("Error"), 
						JOptionPane.ERROR_MESSAGE);
					return;
			}
		}
	}
	
	protected static JFileChooser createAndOpenFileChooser(boolean open, boolean multiple) {
		String curDir = Main.pref.get("lastDirectory");
		if (curDir.equals(""))
			curDir = ".";
		JFileChooser fc = new JFileChooser(new File(curDir));
		fc.setMultiSelectionEnabled(multiple);
		for (int i = 0; i < ExtensionFileFilter.filters.length; ++i)
			fc.addChoosableFileFilter(ExtensionFileFilter.filters[i]);
		fc.setAcceptAllFileFilterUsed(true);
	
		int answer = open ? fc.showOpenDialog(Main.parent) : fc.showSaveDialog(Main.parent);
		if (answer != JFileChooser.APPROVE_OPTION)
			return null;
		
		if (!fc.getCurrentDirectory().getAbsolutePath().equals(curDir))
			Main.pref.put("lastDirectory", fc.getCurrentDirectory().getAbsolutePath());

		if (!open) {
			File file = fc.getSelectedFile();
			if (file == null || (file.exists() && JOptionPane.YES_OPTION != 
					JOptionPane.showConfirmDialog(Main.parent, tr("File exists. Overwrite?"), tr("Overwrite"), JOptionPane.YES_NO_OPTION)))
				return null;
		}
		
		return fc;
	}
	
	public static File openFileDialog(boolean open) {
		JFileChooser fc = createAndOpenFileChooser(open, false);
		if (fc == null)
			return null;

		File file = fc.getSelectedFile();

		String fn = file.getPath();
		if (fn.indexOf('.') == -1) {
			FileFilter ff = fc.getFileFilter();
			if (ff instanceof ExtensionFileFilter)
				fn = "." + ((ExtensionFileFilter)ff).defaultExtension;
			else
				fn += ".osm";
			file = new File(fn);
		}
		return file;
	}
}
