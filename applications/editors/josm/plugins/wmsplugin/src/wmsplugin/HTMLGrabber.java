package wmsplugin;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.io.CacheFiles;

public class HTMLGrabber extends WMSGrabber {
    HTMLGrabber(ProjectionBounds b, GeorefImage image, MapView mv, WMSLayer layer, CacheFiles cache) {
        super(b, image, mv, layer, cache);
        this.baseURL = layer.baseURL.replaceFirst("html:", "");
    }

    @Override
    protected BufferedImage grab(URL url) throws IOException {
        String urlstring = url.toExternalForm();

        System.out.println("Grabbing HTML " + url);

        ArrayList<String> cmdParams = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(MessageFormat.format(
        Main.pref.get("wmsplugin.browser", "webkit-image {0}"), urlstring));
        while( st.hasMoreTokens() )
            cmdParams.add(st.nextToken());

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
