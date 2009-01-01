package wmsplugin;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.io.ProgressInputStream;
import org.openstreetmap.josm.gui.MapView;


public class WMSGrabber extends Grabber {
    protected String baseURL;

    WMSGrabber(String baseURL, Bounds b, Projection proj,
            double pixelPerDegree, GeorefImage image, MapView mv, WMSLayer layer) {
        super(b, proj, pixelPerDegree, image, mv, layer);
        this.baseURL = baseURL;
    }

    public void run() {
        attempt();
        mv.repaint();
    }

    void fetch() throws Exception{
        URL url = null;
        try {
            url = getURL(
                b.min.lon(), b.min.lat(),
                b.max.lon(), b.max.lat(),
                width(), height());

            image.min = proj.latlon2eastNorth(b.min);
            image.max = proj.latlon2eastNorth(b.max);

            if(image.isVisible(mv)) //don't download, if the image isn't visible already
                image.image = grab(url);
            image.downloadingStarted = false;
        } catch(Exception e) {
            throw new Exception(e.getMessage() + "\nImage couldn't be fetched: " + (url != null ? url.toString() : ""));
        }
    }

    public static final NumberFormat
        latLonFormat = new DecimalFormat("###0.0000000",
            new DecimalFormatSymbols(Locale.US));

    protected URL getURL(double w, double s,double e,double n,
            int wi, int ht) throws MalformedURLException {
        String str = baseURL;
        if(!str.endsWith("?"))
            str += "&";
        str += "bbox="
            + latLonFormat.format(w) + ","
            + latLonFormat.format(s) + ","
            + latLonFormat.format(e) + ","
            + latLonFormat.format(n)
            + "&width=" + wi + "&height=" + ht;
        return new URL(str.replace(" ", "%20"));
    }

    protected BufferedImage grab(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        String contentType = conn.getHeaderField("Content-Type");
        if( conn.getResponseCode() != 200
                || contentType != null && !contentType.startsWith("image") ) {
            throw new IOException(readException(conn));
        }

        InputStream is = new ProgressInputStream(conn, null);
        BufferedImage img = ImageIO.read(is);
        is.close();
        return img;
    }

    protected String readException(URLConnection conn) throws IOException {
        StringBuilder exception = new StringBuilder();
        InputStream in = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String line = null;
        while( (line = br.readLine()) != null) {
            exception.append(line);
            exception.append('\n');
        }
        return exception.toString();
    }
}
