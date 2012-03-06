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
    
    public GetImageryOffsetAction() {
        super(tr("Get Imagery Offset..."), "getoffset", tr("Download offsets for current imagery from a server"),
                Shortcut.registerShortcut("imageryoffset:get", tr("Imagery: {0}", tr("Get Imagery Offset...")), KeyEvent.VK_I, Shortcut.ALT+Shortcut.CTRL), true);
        offsets = Collections.emptyList();
    }

    public void actionPerformed(ActionEvent e) {
        Projection proj = Main.map.mapView.getProjection();
        LatLon center = proj.eastNorth2latlon(Main.map.mapView.getCenter());
        ImageryLayer layer = ImageryOffsetTools.getTopImageryLayer();
        String imagery = ImageryOffsetTools.getImageryID(layer);
        if( imagery == null )
            return;
        
        List<ImageryOffsetBase> offsets = download(center, imagery); // todo: async
        /*DownloadOffsets download = new DownloadOffsets();
        Future<?> future = Main.worker.submit(download);
        try {
            future.get();
        } catch( Exception ex ) {
            ex.printStackTrace();
            return;
        }*/
        
        // todo: show a dialog for selecting one of the offsets (without "update" flag)
        ImageryOffsetBase offset = new OffsetDialog(offsets).showDialog();
        if( offset != null ) {
            // todo: use the chosen offset
            if( offset instanceof ImageryOffset ) {
                ImageryOffsetTools.applyLayerOffset(layer, (ImageryOffset)offset);
            } else if( offset instanceof CalibrationObject ) {
                // todo: select object
            }
        }
    }
    
    private List<ImageryOffsetBase> download( LatLon center, String imagery ) {
        String base = Main.pref.get("iodb.server.url", "http://offsets.textual.ru/");
        String query = "get?lat=" + center.getX() + "&lon=" + center.getY();
        List<ImageryOffsetBase> result = null;
        try {
            query = query + "&imagery=" + URLEncoder.encode(imagery, "utf-8");
            URL url = new URL(base + query);
            System.out.println("url=" + url);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            int retCode = connection.getResponseCode();
            InputStream inp = connection.getInputStream();
            if( inp != null ) {
                result = new IODBReader(inp).parse();
                System.out.println("result.size() = " + result.size());
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
