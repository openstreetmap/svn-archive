package wmsplugin;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.io.CacheFiles;


public class YAHOOGrabber extends WMSGrabber {
    protected String browserCmd;

    YAHOOGrabber(Bounds b, GeorefImage image, MapView mv, WMSLayer layer, CacheFiles cache) {
        super(b, image, mv, layer, cache);
        this.baseURL = "file:///" + WMSPlugin.getPrefsPath() + "ymap.html?";
        this.browserCmd = layer.baseURL.replaceFirst("yahoo://", "");
    }

    @Override
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
