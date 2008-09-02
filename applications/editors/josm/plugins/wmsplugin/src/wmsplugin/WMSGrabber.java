package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.io.ProgressInputStream;
import org.openstreetmap.josm.gui.MapView;


public class WMSGrabber extends Thread implements Grabber{
	protected String baseURL;

	protected Bounds b;
	protected Projection proj;
	protected double pixelPerDegree;
	protected GeorefImage image;
	protected MapView mv;
	protected WMSLayer layer;

	WMSGrabber(String _baseURL, Bounds _b, Projection _proj,
			double _pixelPerDegree, GeorefImage _image, MapView _mv, WMSLayer _layer) {
		this.baseURL = _baseURL;
		b = _b;
		proj = _proj;
		pixelPerDegree = _pixelPerDegree;
		image = _image;
		mv = _mv;
		layer = _layer;
		this.setDaemon(true);
		this.setPriority(Thread.MIN_PRIORITY);
	}

	public void run() {
			
			int w = (int) ((b.max.lon() - b.min.lon()) * pixelPerDegree);
			int h = (int) ((b.max.lat() - b.min.lat()) * pixelPerDegree);

			try {
				URL url = getURL(
					b.min.lon(), b.min.lat(),
					b.max.lon(), b.max.lat(),
					w, h);

				image.min = proj.latlon2eastNorth(b.min);
				image.max = proj.latlon2eastNorth(b.max);

				image.image = grab(url);
				image.downloadingStarted = false;

				mv.repaint();
			}
			catch (MalformedURLException e) {
				if(layer.messageNum-- > 0)
					JOptionPane.showMessageDialog(Main.parent,tr("WMSPlugin: Illegal url.\n{0}",e.getMessage()));
			}
			catch (IOException e) {
				if(layer.messageNum-- > 0)
					JOptionPane.showMessageDialog(Main.parent,tr("WMSPlugin: IO exception.\n{0}",e.getMessage()));
			}
	}

	public static final NumberFormat
		latLonFormat = new DecimalFormat("###0.0000000",
			new DecimalFormatSymbols(Locale.US));

	protected URL getURL(double w, double s,double e,double n,
			int wi, int ht) throws MalformedURLException {
		String str = baseURL + "&bbox="
			+ latLonFormat.format(w) + ","
			+ latLonFormat.format(s) + ","
			+ latLonFormat.format(e) + ","
			+ latLonFormat.format(n)
			+ "&width=" + wi + "&height=" + ht;
		return new URL(str.replace(" ", "%20"));
	}

	protected BufferedImage grab(URL url) throws IOException {
			InputStream is = new ProgressInputStream(
				url.openConnection(), null);
			BufferedImage img;
		synchronized (layer){ //download only one tile in one moment
			if(!image.isVisible(mv)){
				return null;
			}
			img = ImageIO.read(is);
		}
			is.close();
			return img;
	}
}
