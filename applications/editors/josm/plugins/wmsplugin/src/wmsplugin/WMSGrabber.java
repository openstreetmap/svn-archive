package wmsplugin;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.io.ProgressInputStream;

public class WMSGrabber implements Grabber {
	public String baseURL;

	public WMSGrabber(String baseURL) {
		this.baseURL = baseURL;
	}

	public GeorefImage grab(Bounds b, Projection proj,
			double pixelPerDegree) throws IOException {
		int w = (int) ((b.max.lon() - b.min.lon()) * pixelPerDegree);
		int h = (int) ((b.max.lat() - b.min.lat()) * pixelPerDegree);

		try {
			URL url = getURL(
				b.min.lon(), b.min.lat(),
				b.max.lon(), b.max.lat(),
				w, h);

			BufferedImage img = grab(url);

			return new GeorefImage(img,
				proj.latlon2eastNorth(b.min),
				proj.latlon2eastNorth(b.max));
		} catch (MalformedURLException e) {
			throw (IOException) new IOException(
				"WMSGrabber: Illegal url.").initCause(e);
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
			url.openConnection(), Main.pleaseWaitDlg);
		BufferedImage img = ImageIO.read(is);
		is.close();
		return img;
	}
}
