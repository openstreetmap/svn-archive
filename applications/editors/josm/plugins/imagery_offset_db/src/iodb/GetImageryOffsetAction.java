package iodb;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.gui.jmapviewer.tilesources.AbstractTMSTileSource;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmTransferException;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.Shortcut;
import org.xml.sax.SAXException;

/**
 * Download a list of imagery offsets for the current position, let user choose which one to use.
 * 
 * @author zverik
 */
public class GetImageryOffsetAction extends JosmAction {
    
    private List<ImageryOffsetBase> offsets;
    
    private HashMap<String, String> imageryAliases;
    
    public GetImageryOffsetAction() {
        super(tr("Get Imagery Offset..."), "getoffset", tr("Download offsets for current imagery from a server"),
                Shortcut.registerShortcut("imageryoffset:get", tr("Imagery: {0}", tr("Get Imagery Offset...")), KeyEvent.VK_I, Shortcut.ALT+Shortcut.CTRL), true);
        offsets = Collections.emptyList();
    }

    public void actionPerformed(ActionEvent e) {
        Projection proj = Main.map.mapView.getProjection();
        LatLon center = proj.eastNorth2latlon(Main.map.mapView.getCenter());
        // todo: download a list of offsets for current bbox * N
        List<ImageryOffsetBase> offsets = download(center); // todo: async
        DownloadOffsets download = new DownloadOffsets();
        Future<?> future = Main.worker.submit(download);
        try {
            future.get();
        } catch( Exception ex ) {
            ex.printStackTrace();
            return;
        }
        
        // todo: show a dialog for selecting one of the offsets (without "update" flag)
        ImageryOffsetBase offset = new OffsetDialog(offsets).showDialog();
        if( offset != null ) {
            // todo: use the chosen offset
        }
    }
    
    private List<ImageryOffsetBase> download( LatLon center ) {
        String base = Main.pref.get("iodb.server.url", "http://textual.ru/iodb.php");
        String query = "?action=get&lat=" + center.getX() + "&lon=" + center.getY();
        List<ImageryOffsetBase> result = null;
        try {
            query = query + "&imagery=" + URLEncoder.encode(getImageryID(), "utf-8");
            URL url = new URL(base + query);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            int retCode = connection.getResponseCode();
            InputStream inp = connection.getInputStream();
            if( inp != null ) {
                result = new IODBReader(inp).parse();
            }
            connection.disconnect();
        } catch( MalformedURLException ex ) {
            // ?
        } catch( UnsupportedEncodingException e ) {
            // do nothing. WTF is that?
        } catch( IOException e ) {
            e.printStackTrace();
            // ?
        } catch( SAXException e ) {
            e.printStackTrace();
            // ?
        }
        if( result == null )
            result = new ArrayList<ImageryOffsetBase>();
        return result;
    }
    
    private String getImageryID() {
        List<ImageryLayer> layers = Main.map.mapView.getLayersOfType(ImageryLayer.class);
        String url = null;
        for( ImageryLayer layer : layers ) {
            if( layer.isVisible() ) {
                url = layer.getInfo().getUrl();
                break;
            }
        }
        if( url == null )
            return null;
        
        if( imageryAliases == null )
            loadImageryAliases();
        for( String substr : imageryAliases.keySet() )
            if( url.contains(substr) )
                return imageryAliases.get(substr);
        
        return url; // todo: strip parametric parts, etc
    }
    
    private void loadImageryAliases() {
        if( imageryAliases == null )
            imageryAliases = new HashMap<String, String>();
        else
            imageryAliases.clear();
        
        // { substring, alias }
        imageryAliases.put("bing", "bing");
        // todo: load from a resource?
    }
    
    // Following three methods were snatched from TMSLayer
    private double latToTileY(double lat, int zoom) {
        double l = lat / 180 * Math.PI;
        double pf = Math.log(Math.tan(l) + (1 / Math.cos(l)));
        return Math.pow(2.0, zoom - 1) * (Math.PI - pf) / Math.PI;
    }

    private double lonToTileX(double lon, int zoom) {
        return Math.pow(2.0, zoom - 3) * (lon + 180.0) / 45.0;
    }

    private int getCurrentZoom() {
        if (Main.map == null || Main.map.mapView == null) {
            return 1;
        }
        MapView mv = Main.map.mapView;
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
        double x1 = lonToTileX(topLeft.lon(), 1);
        double y1 = latToTileY(topLeft.lat(), 1);
        double x2 = lonToTileX(botRight.lon(), 1);
        double y2 = latToTileY(botRight.lat(), 1);

        int screenPixels = mv.getWidth() * mv.getHeight();
        double tilePixels = Math.abs((y2 - y1) * (x2 - x1) * 256 * 256);
        if (screenPixels == 0 || tilePixels == 0) {
            return 1;
        }
        double factor = screenPixels / tilePixels;
        double result = Math.log(factor) / Math.log(2) / 2 + 1;
        int intResult = (int) Math.floor(result);
        return intResult;
    }
    
    class DownloadOffsets extends PleaseWaitRunnable {
        
        private boolean cancelled;

        public DownloadOffsets() {
            super(tr("Downloading calibration data"));
            cancelled = false;
        }

        @Override
        protected void realRun() throws SAXException, IOException, OsmTransferException {
            // todo: open httpconnection to server and read xml
            if( cancelled )
                return;
            
        }

        @Override
        protected void finish() {
            if( cancelled )
                return;
            // todo: parse xml and return an array of ImageryOffsetBase
        }
        
        @Override
        protected void cancel() {
            cancelled = true;
        }
    }
}
