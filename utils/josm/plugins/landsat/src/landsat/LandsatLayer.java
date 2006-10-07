package landsat;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerList;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * This is a layer that grabs the current screen from an WMS server. The data
 * fetched this way is tiled and managerd to the disc to reduce server load.
 */
public class LandsatLayer extends Layer {

	private static Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(LandsatPlugin.class.getResource("/images/wms.png")));

	private final LandsatImage landsatImage;

	private final String url;


	public LandsatLayer(String url) {
		super(url.indexOf('/') != -1 ? url.substring(url.indexOf('/')+1) : url);

		// to calculate the world dimension, we assume that the projection does
		// not have problems with translating longitude to a correct scale.
		// Next to that, the projection must be linear dependend on the lat/lon
		// unprojected scale.
		if (Projection.MAX_LON != 180)
			throw new IllegalArgumentException(tr
					("Wrong longitude transformation for tile manager. "+
							"Can't operate on {0}",Main.proj));

		this.url = url;
		landsatImage = new LandsatImage(url);
	}

	public void grab() throws IOException
	{
		MapView mv = Main.map.mapView;
		landsatImage.grab(mv);
	}

	public void grab(double minlat,double minlon,double maxlat,double maxlon)
	throws IOException
	{
		MapView mv = Main.map.mapView;
		landsatImage.grab(mv,minlat,minlon,maxlat,maxlon);
	}

	@Override public Icon getIcon() {
		return icon;
	}

	@Override public String getToolTipText() {
		return tr("WMS layer: {0}", url);
	}

	@Override public boolean isMergable(Layer other) {
		return false;
	}

	@Override public void mergeFrom(Layer from) {
	}

	@Override public void paint(Graphics g, final MapView mv) {
		landsatImage.paint(g,mv);
	}

	@Override public void visitBoundingBox(BoundingXYVisitor v) {
		// doesn't have a bounding box
	}

	@Override public Object getInfoComponent() {
		return getToolTipText();
	}

	@Override public Component[] getMenuEntries() {
		return new Component[]{
				new JMenuItem(new LayerList.ShowHideLayerAction(this)),
				new JMenuItem(new LayerList.DeleteLayerAction(this)),
				new JSeparator(),
				new JMenuItem(new LayerListPopup.InfoAction(this))};
	}
}
