package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.io.ProgressInputStream;
import org.openstreetmap.josm.gui.MapView;


public class YAHOOGrabber extends Thread implements Grabber{
	protected String baseURL;
	protected String browserCmd;

	protected Bounds b;
	protected Projection proj;
	protected double pixelPerDegree;
	protected GeorefImage image;
	protected MapView mv;
	protected WMSLayer layer;
	protected int width, height;

	YAHOOGrabber(String _baseURL, Bounds _b, Projection _proj,
			double _pixelPerDegree, GeorefImage _image, MapView _mv, WMSLayer _layer) {
		this.baseURL = "file://" + Main.pref.getPreferencesDir() + "plugins/wmsplugin/ymap.html?request=getmap&format=image/jpeg";
		this.browserCmd = _baseURL.replaceFirst("yahoo://", "");
		this.b = _b;
		this.proj = _proj;
		this.pixelPerDegree = _pixelPerDegree;
		this.image = _image;
		this.mv = _mv;
		this.layer = _layer;
		this.setDaemon(true);
		this.setPriority(Thread.MIN_PRIORITY);
	}

	public void run() {
			Image img;
			
			width = (int) ((b.max.lon() - b.min.lon()) * pixelPerDegree);
			height = (int) ((b.max.lat() - b.min.lat()) * pixelPerDegree);

			try {
				URL url = getURL(
					b.min.lon(), b.min.lat(),
					b.max.lon(), b.max.lat(),
					width, height);

				image.min = proj.latlon2eastNorth(b.min);
				image.max = proj.latlon2eastNorth(b.max);
				synchronized (layer) {
					if(!image.isVisible(mv)){
						image.downloadingStarted = false;
						return;
					}
					Process browser = browse(url.toString());;
					image.image =  new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					img = ImageIO.read(browser.getInputStream()).getScaledInstance(width, height, Image.SCALE_FAST);
				}
				image.image.getGraphics().drawImage(img, 0 , 0, null);

				image.downloadingStarted = false;

				mv.repaint();
			}
			catch (MalformedURLException e) {
				if(layer.messageNum-- > 0)
					JOptionPane.showMessageDialog(Main.parent,tr("WMSPlugin (YAHOOGrabber): Illegal url.\n{0}",e.getMessage()));
			}
			catch (IOException e) {
				if(layer.messageNum-- > 0)
					JOptionPane.showMessageDialog(Main.parent,tr("WMSPlugin (YAHOOGrabber): IO exception.\n{0}",e.getMessage()));
			}
			catch (NullPointerException e) {
				if(layer.messageNum-- > 0)
					JOptionPane.showMessageDialog(Main.parent,tr("WMSPlugin (YAHOOGrabber): Null pointer exception.\n{0}",e.getMessage()));
			}
	}


	protected Process browse(String url) throws IOException {
		ArrayList<String> cmdParams = new ArrayList<String>();
		
		StringTokenizer st = new StringTokenizer(tr(browserCmd, url));
		while( st.hasMoreTokens() ) 
			cmdParams.add(st.nextToken());

		System.out.println("WMS::Browsing YAHOO: " + cmdParams);
		ProcessBuilder builder = new ProcessBuilder( cmdParams);

		try {
			return builder.start();
		}
		catch(IOException ioe) {
			throw new IOException( tr("Could not start browser. Please check that the executable path is correct."));
		}
	}

	protected static final NumberFormat
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

}
