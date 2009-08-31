package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Mercator;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.io.CacheFiles;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.io.ProgressInputStream;


public class WMSGrabber extends Grabber {
    protected String baseURL;
    private final boolean urlWithPatterns;

    WMSGrabber(ProjectionBounds b, GeorefImage image, MapView mv, WMSLayer layer, CacheFiles cache) {
        super(b, image, mv, layer, cache);
        this.baseURL = layer.baseURL;
        /* URL containing placeholders? */
        urlWithPatterns = baseURL != null && baseURL.contains("{") && baseURL.contains("}");
    }

    public void run() {
        attempt();
        mv.repaint();
    }

    @Override
    void fetch() throws Exception{
        URL url = null;
        try {
            url = getURL(
                b.min.east(), b.min.north(),
                b.max.east(), b.max.north(),
                width(), height());

            image.min = b.min;
            image.max = b.max;

            if(image.isVisible(mv)) { //don't download, if the image isn't visible already
                image.image = grab(url);
                image.flushedResizedCachedInstance();
            }
            image.downloadingStarted = false;
        } catch(Exception e) {
        	e.printStackTrace();
            throw new Exception(e.getMessage() + "\nImage couldn't be fetched: " + (url != null ? url.toString() : ""));
        }
    }

    public static final NumberFormat latLonFormat = new DecimalFormat("###0.0000000",
            new DecimalFormatSymbols(Locale.US));

    protected URL getURL(double w, double s,double e,double n,
            int wi, int ht) throws MalformedURLException {
        String myProj = Main.proj.toCode();
        if(Main.proj instanceof Mercator) // don't use mercator code directly
        {
            LatLon sw = Main.proj.eastNorth2latlon(new EastNorth(w, s));
            LatLon ne = Main.proj.eastNorth2latlon(new EastNorth(e, n));
            myProj = "EPSG:4326";
            s = sw.lat();
            w = sw.lon();
            n = ne.lat();
            e = ne.lon();
        }

        String str = baseURL;
        String bbox = latLonFormat.format(w) + ","
                           + latLonFormat.format(s) + ","
                           + latLonFormat.format(e) + ","
                           + latLonFormat.format(n);

        if (urlWithPatterns) {
            str = str.replaceAll("\\{proj\\}", myProj)
            .replaceAll("\\{bbox\\}", bbox)
            .replaceAll("\\{width\\}", String.valueOf(wi))
            .replaceAll("\\{height\\}", String.valueOf(ht));
        } else {
            str += "bbox=" + bbox
                + getProjection(baseURL, false)
                + "&width=" + wi + "&height=" + ht;
        }
        return new URL(str.replace(" ", "%20"));
    }

    static public String getProjection(String baseURL, Boolean warn)
    {
        String projname = Main.proj.toCode();
        if(Main.proj instanceof Mercator) // don't use mercator code
            projname = "EPSG:4326";
        String res = "";
        try
        {
            Matcher m = Pattern.compile(".*srs=([a-z0-9:]+).*").matcher(baseURL.toLowerCase());
            if(m.matches())
            {
                projname = projname.toLowerCase();
                if(!projname.equals(m.group(1)) && warn)
                {
                    JOptionPane.showMessageDialog(Main.parent,
                    tr("The projection ''{0}'' in URL and current projection ''{1}'' mismatch.\n"
                    + "This may lead to wrong coordinates.",
                    m.group(1), projname),
                    tr("Warning"),
                    JOptionPane.WARNING_MESSAGE);
                }
            }
            else
                res ="&srs="+projname;
        }
        catch(Exception e)
        {
        }
        return res;
    }

    protected BufferedImage grab(URL url) throws IOException, OsmTransferException {
        BufferedImage cached = cache.getImg(url.toString());
        if(cached != null) return cached;

        System.out.println("Grabbing WMS " + url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if(layer.cookies != null && !layer.cookies.equals(""))
            conn.setRequestProperty("Cookie", layer.cookies);
        conn.setConnectTimeout(Main.pref.getInteger("wmsplugin.timeout.connect", 30) * 1000);
        conn.setReadTimeout(Main.pref.getInteger("wmsplugin.timeout.read", 30) * 1000);

        String contentType = conn.getHeaderField("Content-Type");
        if( conn.getResponseCode() != 200
                || contentType != null && !contentType.startsWith("image") ) {
            throw new IOException(readException(conn));
        }

        InputStream is = new ProgressInputStream(conn, null);
        BufferedImage img = ImageIO.read(is);
        is.close();

        cache.saveImg(url.toString(), img);
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
