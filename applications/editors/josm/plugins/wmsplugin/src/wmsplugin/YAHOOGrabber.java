package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.image.BufferedImage;
import java.awt.Image;
import java.net.URL;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.io.CacheFiles;
import org.openstreetmap.josm.gui.MapView;


public class YAHOOGrabber extends WMSGrabber {
    protected String browserCmd;

    YAHOOGrabber(String baseURL, Bounds b, Projection proj,
            double pixelPerDegree, GeorefImage image, MapView mv, WMSLayer layer, CacheFiles cache) {
        super("file:///" + WMSPlugin.getPrefsPath() + "ymap.html?"
        , b, proj, pixelPerDegree, image, mv, layer, cache);
        this.browserCmd = baseURL.replaceFirst("yahoo://", "");
    }

    protected BufferedImage grab(URL url) throws IOException {
        String urlstring = url.toExternalForm();
        // work around a problem in URL removing 2 slashes
        if(!urlstring.startsWith("file:///"))
            urlstring = urlstring.replaceFirst("file:", "file://");

        BufferedImage cached = cache.getImg(urlstring);
        if(cached != null) return cached;

        ArrayList<String> cmdParams = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(MessageFormat.format(browserCmd, urlstring));
        while( st.hasMoreTokens() )
            cmdParams.add(st.nextToken());

        System.out.println("WMS::Browsing YAHOO: " + cmdParams);
        ProcessBuilder builder = new ProcessBuilder( cmdParams);

        Process browser;
        try {
            browser = builder.start();
        } catch(IOException ioe) {
            throw new IOException( "Could not start browser. Please check that the executable path is correct.\n" + ioe.getMessage() );
        }
        
        BufferedImage img = ImageIO.read(browser.getInputStream());
        cache.saveImg(urlstring, img);
        return img;
    }
}
