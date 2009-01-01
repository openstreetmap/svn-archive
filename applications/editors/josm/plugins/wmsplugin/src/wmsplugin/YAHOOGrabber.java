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
import org.openstreetmap.josm.gui.MapView;


public class YAHOOGrabber extends WMSGrabber{
    protected String browserCmd;

    YAHOOGrabber(String baseURL, Bounds b, Projection proj,
            double pixelPerDegree, GeorefImage image, MapView mv, WMSLayer layer) {
        super("file:///" + Main.pref.getPreferencesDir() + "plugins/wmsplugin/ymap.html?"
//                + "request=getmap&format=image/jpeg"
        , b, proj, pixelPerDegree, image, mv, layer);
        this.browserCmd = baseURL.replaceFirst("yahoo://", "");
    }

    protected BufferedImage grab(URL url) throws IOException {
        ArrayList<String> cmdParams = new ArrayList<String>();
        String urlstring = url.toExternalForm();
        // work around a problem in URL removing 2 slashes
        if(!urlstring.startsWith("file:///"))
            urlstring = urlstring.replaceFirst("file:", "file://");
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

        return ImageIO.read(browser.getInputStream());
    }
}
