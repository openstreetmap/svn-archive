package wmsplugin;

import org.openstreetmap.josm.io.CacheFiles;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DiskAccessAction;
import org.openstreetmap.josm.actions.SaveActionBase;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.OptionPaneUtil;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This is a layer that grabs the current screen from an WMS server. The data
 * fetched this way is tiled and managerd to the disc to reduce server load.
 */
public class WMSLayer extends Layer {
	protected static final Icon icon =
		new ImageIcon(Toolkit.getDefaultToolkit().createImage(WMSPlugin.class.getResource("/images/wms_small.png")));

	public int messageNum = 5; //limit for messages per layer
	protected MapView mv;
	protected String resolution;
	protected boolean stopAfterPaint = false;
	protected int ImageSize = 500;
	protected int dax = 10;
	protected int day = 10;
	protected int minZoom = 3;
	protected double dx = 0.0;
	protected double dy = 0.0;
	protected double pixelPerDegree;
	protected GeorefImage[][] images = new GeorefImage[dax][day];
	JCheckBoxMenuItem startstop = new JCheckBoxMenuItem(tr("Automatic downloading"), true);
	protected JCheckBoxMenuItem alphaChannel = new JCheckBoxMenuItem(new ToggleAlphaAction());
	protected String baseURL;
	protected String cookies;
	protected final int serializeFormatVersion = 4;

	private ExecutorService executor = null;

	public WMSLayer() {
		this(tr("Blank Layer"), null, null);
		initializeImages();
		mv = Main.map.mapView;
	}

	public WMSLayer(String name, String baseURL, String cookies) {
		super(name);
		alphaChannel.setSelected(Main.pref.getBoolean("wmsplugin.alpha_channel"));
		background = true; /* set global background variable */
		initializeImages();
		this.baseURL = baseURL;
		this.cookies = cookies;
		WMSGrabber.getProjection(baseURL, true);
		mv = Main.map.mapView;
		resolution = mv.getDist100PixelText();
		pixelPerDegree = getPPD();

		executor = Executors.newFixedThreadPool(3);
	}

	@Override
	public void destroy() {
		try {
			executor.shutdown();
			// Might not be initalized, so catch NullPointer as well
		} catch(Exception x) {}
	}

	public double getPPD(){
		ProjectionBounds bounds = mv.getProjectionBounds();
		return mv.getWidth() / (bounds.max.east() - bounds.min.east());
	}

	public void initializeImages() {
		images = new GeorefImage[dax][day];
		for(int x = 0; x<dax; ++x) {
			for(int y = 0; y<day; ++y) {
				images[x][y]= new GeorefImage(false);
			}
		}
	}

	@Override public Icon getIcon() {
		return icon;
	}

	@Override public String getToolTipText() {
		if(startstop.isSelected())
			return tr("WMS layer ({0}), automatically downloading in zoom {1}", getName(), resolution);
		else
			return tr("WMS layer ({0}), downloading in zoom {1}", getName(), resolution);
	}

	@Override public boolean isMergable(Layer other) {
		return false;
	}

	@Override public void mergeFrom(Layer from) {
	}

	private ProjectionBounds XYtoBounds (int x, int y) {
		return new ProjectionBounds(
				new EastNorth(      x * ImageSize / pixelPerDegree,       y * ImageSize / pixelPerDegree),
				new EastNorth((x + 1) * ImageSize / pixelPerDegree, (y + 1) * ImageSize / pixelPerDegree));
	}

	private int modulo (int a, int b) {
		return a % b >= 0 ? a%b : a%b+b;
	}

	@Override public void paint(Graphics g, final MapView mv) {
		if(baseURL == null) return;

		if( !startstop.isSelected() || (pixelPerDegree / getPPD() > minZoom) ){ //don't download when it's too outzoomed
			for(int x = 0; x<dax; ++x) {
				for(int y = 0; y<day; ++y) {
					images[modulo(x,dax)][modulo(y,day)].paint(g, mv, dx, dy);
				}
			}
		} else {
			downloadAndPaintVisible(g, mv);
		}
	}

	public void displace(double dx, double dy) {
		this.dx += dx;
		this.dy += dy;
	}

	protected void downloadAndPaintVisible(Graphics g, final MapView mv){
		ProjectionBounds bounds = mv.getProjectionBounds();
		int bminx= (int)Math.floor ((bounds.min.east() * pixelPerDegree ) / ImageSize );
		int bminy= (int)Math.floor ((bounds.min.north() * pixelPerDegree ) / ImageSize );
		int bmaxx= (int)Math.ceil  ((bounds.max.east() * pixelPerDegree ) / ImageSize );
		int bmaxy= (int)Math.ceil  ((bounds.max.north() * pixelPerDegree ) / ImageSize );

		if((bmaxx - bminx > dax) || (bmaxy - bminy > day)){
			OptionPaneUtil.showMessageDialog(
					Main.parent,
					tr("The requested area is too big. Please zoom in a little, or change resolution"),
					tr("Error"),
					JOptionPane.ERROR_MESSAGE
			);
			return;
		}

		for(int x = bminx; x<bmaxx; ++x) {
			for(int y = bminy; y<bmaxy; ++y){
				GeorefImage img = images[modulo(x,dax)][modulo(y,day)];
				g.drawRect(x, y, dax, bminy);
				if(!img.paint(g, mv, dx, dy) && !img.downloadingStarted){
					img.downloadingStarted = true;
					img.image = null;
					img.flushedResizedCachedInstance();
					Grabber gr = WMSPlugin.getGrabber(XYtoBounds(x,y), img, mv, this);
					executor.submit(gr);
				}
			}
		}
	}

	@Override public void visitBoundingBox(BoundingXYVisitor v) {
		for(int x = 0; x<dax; ++x) {
			for(int y = 0; y<day; ++y)
				if(images[x][y].image!=null){
					v.visit(images[x][y].min);
					v.visit(images[x][y].max);
				}
		}
	}

	@Override public Object getInfoComponent() {
		return getToolTipText();
	}

	@Override public Component[] getMenuEntries() {
		return new Component[]{
				new JMenuItem(LayerListDialog.getInstance().createShowHideLayerAction(this)),
				new JMenuItem(LayerListDialog.getInstance().createDeleteLayerAction(this)),
				new JSeparator(),
				new JMenuItem(new LoadWmsAction()),
				new JMenuItem(new SaveWmsAction()),
				new JSeparator(),
				startstop,
				alphaChannel,
				new JMenuItem(new changeResolutionAction()),
				new JMenuItem(new reloadErrorTilesAction()),
				new JMenuItem(new downloadAction()),
				new JSeparator(),
				new JMenuItem(new LayerListPopup.InfoAction(this))
		};
	}

	public GeorefImage findImage(EastNorth eastNorth) {
		for(int x = 0; x<dax; ++x) {
			for(int y = 0; y<day; ++y)
				if(images[x][y].image!=null && images[x][y].min!=null && images[x][y].max!=null)
					if(images[x][y].contains(eastNorth, dx, dy))
						return images[x][y];
		}
		return null;
	}

	public class downloadAction extends AbstractAction {
		public downloadAction() {
			super(tr("Download visible tiles"));
		}
		public void actionPerformed(ActionEvent ev) {
			downloadAndPaintVisible(mv.getGraphics(), mv);
		}
	}

	public class changeResolutionAction extends AbstractAction {
		public changeResolutionAction() {
			super(tr("Change resolution"));
		}
		public void actionPerformed(ActionEvent ev) {
			initializeImages();
			resolution = mv.getDist100PixelText();
			pixelPerDegree = getPPD();
			mv.repaint();
		}
	}

	public class reloadErrorTilesAction extends AbstractAction {
		public reloadErrorTilesAction() {
			super(tr("Reload erroneous tiles"));
		}
		public void actionPerformed(ActionEvent ev) {
			// Delete small files, because they're probably blank tiles.
			// See https://josm.openstreetmap.de/ticket/2307
			WMSPlugin.cache.customCleanUp(CacheFiles.CLEAN_SMALL_FILES, 2048);

			for (int x = 0; x < dax; ++x) {
				for (int y = 0; y < day; ++y) {
					GeorefImage img = images[modulo(x,dax)][modulo(y,day)];
					if(img.failed){
						img.image = null;
						img.flushedResizedCachedInstance();
						img.downloadingStarted = false;
						img.failed = false;
						mv.repaint();
					}
				}
			}
		}
	}

	public class ToggleAlphaAction extends AbstractAction {
		public ToggleAlphaAction() {
			super(tr("Alpha channel"));
		}
		public void actionPerformed(ActionEvent ev) {
			JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) ev.getSource();
			boolean alphaChannel = checkbox.isSelected();
			Main.pref.put("wmsplugin.alpha_channel", alphaChannel);

			// clear all resized cached instances and repaint the layer
			for (int x = 0; x < dax; ++x) {
				for (int y = 0; y < day; ++y) {
					GeorefImage img = images[modulo(x, dax)][modulo(y, day)];
					img.flushedResizedCachedInstance();
				}
			}
			mv.repaint();
		}
	}

	public class SaveWmsAction extends AbstractAction {
		public SaveWmsAction() {
			super(tr("Save WMS layer to file"), ImageProvider.get("save"));
		}
		public void actionPerformed(ActionEvent ev) {
			File f = SaveActionBase.createAndOpenSaveFileChooser(
					tr("Save WMS layer"), ".wms");
			try
			{
				FileOutputStream fos = new FileOutputStream(f);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeInt(serializeFormatVersion);
				oos.writeInt(dax);
				oos.writeInt(day);
				oos.writeInt(ImageSize);
				oos.writeDouble(pixelPerDegree);
				oos.writeObject(getName());
				oos.writeObject(baseURL);
				oos.writeObject(images);
				oos.close();
				fos.close();
			}
			catch (Exception ex) {
				ex.printStackTrace(System.out);
			}
		}
	}

	public class LoadWmsAction extends AbstractAction {
		public LoadWmsAction() {
			super(tr("Load WMS layer from file"), ImageProvider.get("load"));
		}
		public void actionPerformed(ActionEvent ev) {
			JFileChooser fc = DiskAccessAction.createAndOpenFileChooser(true,
					false, tr("Load WMS layer"));
			if(fc == null) return;
			File f = fc.getSelectedFile();
			if (f == null) return;
			try
			{
				FileInputStream fis = new FileInputStream(f);
				ObjectInputStream ois = new ObjectInputStream(fis);
				int sfv = ois.readInt();
				if (sfv != serializeFormatVersion) {
					OptionPaneUtil.showMessageDialog(Main.parent,
							tr("Unsupported WMS file version; found {0}, expected {1}", sfv, serializeFormatVersion),
							tr("File Format Error"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				startstop.setSelected(false);
				dax = ois.readInt();
				day = ois.readInt();
				ImageSize = ois.readInt();
				pixelPerDegree = ois.readDouble();
				setName((String)ois.readObject());
				baseURL = (String) ois.readObject();
				images = (GeorefImage[][])ois.readObject();
				ois.close();
				fis.close();
				mv.repaint();
			}
			catch (Exception ex) {
				// FIXME be more specific
				ex.printStackTrace(System.out);
				OptionPaneUtil.showMessageDialog(Main.parent,
						tr("Error loading file"),
						tr("Error"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}
}
