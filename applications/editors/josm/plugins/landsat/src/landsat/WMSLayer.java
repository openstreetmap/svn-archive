package landsat;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.data.coor.EastNorth;

/**
 * This is a layer that grabs the current screen from an WMS server. The data
 * fetched this way is tiled and managerd to the disc to reduce server load.
 */
public class WMSLayer extends Layer {

	protected static Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(LandsatPlugin.class.getResource("/images/wms.png")));

	protected final ArrayList<WMSImage> wmsImages;

	protected final String url;


	public WMSLayer(String url) {
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
		initMapView(mv);
		WMSImage wmsImage = new WMSImage(url);
		wmsImage.grab(mv,minlat,minlon,maxlat,maxlon);
		wmsImages.add(wmsImage);
	}

	protected void initMapView(MapView mv)
	{
		// If there is no data we need to initialise the centre and scale
		// of the map view, so that we can display the Landsat/OSGB image.
		// To do this, we centre the map in the centre point of the requested
		// data, and set the scale so that the requested data is just
		// completely within the visible area, even if it's a non-square area.

		if(mv.getCenter()==null)
		{
			EastNorth centre = Main.proj.latlon2eastNorth
			 			(new LatLon(minlat+(maxlat-minlat)/2 , 
						minlon+(maxlon-minlon)/2)),
				oldBottomLeft = Main.proj.latlon2eastNorth
						(new LatLon(minlat,minlon)),
				oldTopRight = Main.proj.latlon2eastNorth
						(new LatLon(maxlat,maxlon)),
				bottomLeft,
				topRight;

			if(oldTopRight.north-oldBottomLeft.north < 
					oldTopRight.east-oldBottomLeft.east)
			{	
				bottomLeft = new EastNorth 
					( oldBottomLeft.east(), centre.north() - 
							(oldTopRight.east()-oldBottomLeft.east()/2);
				topRight = new EastNorth 
					( oldTopRight.east(), centre.north() + 
							(oldTopRight.east()-oldBottomLeft.east()/2);
			}
			else
			{
				bottomLeft = new EastNorth 
					( centre.east() - 
							(oldTopRight.north()-oldBottomLeft.north()/2),
						oldBottomLeft.north() );
				topRight = new EastNorth 
					( centre.east() + 
							(oldTopRight.north()-oldBottomLeft.north()/2),
						oldTopRight.north() );
			}

			// scale is enPerPixel
			double scale = (topRight.east()-bottomLeft.east())/
									mapView.getWidth();
			mv.zoomTo(centre,scale);
		}
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
}
