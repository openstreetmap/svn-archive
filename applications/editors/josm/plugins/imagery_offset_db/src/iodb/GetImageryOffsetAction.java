package iodb;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.DownloadPrimitiveAction;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Download a list of imagery offsets for the current position, let user choose which one to use.
 * 
 * @author zverik
 */
public class GetImageryOffsetAction extends JosmAction {
    
    public GetImageryOffsetAction() {
        super(tr("Get Imagery Offset..."), "getoffset", tr("Download offsets for current imagery from a server"),
                Shortcut.registerShortcut("imageryoffset:get", tr("Imagery: {0}", tr("Get Imagery Offset...")),
                KeyEvent.VK_I, Shortcut.ALT_CTRL), true);
    }

    public void actionPerformed(ActionEvent e) {
        if( Main.map == null || Main.map.mapView == null || !Main.map.isVisible() )
            return;
        Projection proj = Main.map.mapView.getProjection();
        LatLon center = proj.eastNorth2latlon(Main.map.mapView.getCenter());
        ImageryLayer layer = ImageryOffsetTools.getTopImageryLayer();
        String imagery = ImageryOffsetTools.getImageryID(layer);
        if( imagery == null )
            return;
        
        DownloadOffsetsTask download = new DownloadOffsetsTask(center, layer, imagery);
        Main.worker.submit(download);
    }

    @Override
    protected void updateEnabledState() {
        boolean state = true;
        if( Main.map == null || Main.map.mapView == null || !Main.map.isVisible() )
            state = false;
        ImageryLayer layer = ImageryOffsetTools.getTopImageryLayer();
        if( ImageryOffsetTools.getImageryID(layer) == null )
            state = false;
        setEnabled(state);
    }
    
    private void showOffsetDialog( List<ImageryOffsetBase> offsets, ImageryLayer layer ) {
        if( offsets.isEmpty() ) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("No data for this region. Please adjust imagery layer and upload an offset."),
                    ImageryOffsetTools.DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final ImageryOffsetBase offset = new OffsetDialog(offsets).showDialog();
        if( offset != null ) {
            if( offset instanceof ImageryOffset ) {
                ImageryOffsetTools.applyLayerOffset(layer, (ImageryOffset)offset);
                Main.map.repaint();
            } else if( offset instanceof CalibrationObject ) {
                OsmPrimitive obj = ((CalibrationObject)offset).getObject();
                final List<PrimitiveId> ids = new ArrayList<PrimitiveId>(1);
                ids.add(obj);
                DownloadPrimitiveAction.processItems(false, ids, false, true);
                Main.worker.submit(new AfterCalibrationDownloadTask((CalibrationObject)offset));
            }
        }
    }

    class AfterCalibrationDownloadTask implements Runnable {
        private CalibrationObject offset;

        public AfterCalibrationDownloadTask( CalibrationObject offset ) {
            this.offset = offset;
        }

        @Override
        public void run() {
            OsmPrimitive p = getCurrentDataSet().getPrimitiveById(offset.getObject());
            if( p == null ) {
                return;
            }
            // check for last user
            if( offset.getLastUserId() > 0 ) {
                long uid = p.getUser().getId();
                Date ts = p.getTimestamp();
                if( p instanceof Way ) {
                    for( Node n : ((Way)p).getNodes() ) {
                        if( n.getTimestamp().after(ts) ) {
                            ts = n.getTimestamp();
                            uid = n.getUser().getId();
                        }
                    }
                }
                if( uid != offset.getLastUserId() ) {
                    int result = JOptionPane.showConfirmDialog(Main.parent,
                            tr("The calibration object has been changed in unknown way.\n"
                             + "It may be moved or extended, thus ceasing to be a reliable mark\n"
                             + "for imagery calibration. Do you want to notify the server of this?"),
                            ImageryOffsetTools.DIALOG_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if( result == JOptionPane.YES_OPTION ) {
                        DeprecateOffsetAction.deprecateOffset(offset);
                        return;
                    }
                }
            }
            Main.main.getCurrentDataSet().setSelected(p);
            AutoScaleAction.zoomTo(Collections.singleton(p));
            if( !Main.pref.getBoolean("iodb.calibration.message", false) ) {
                JOptionPane.showMessageDialog(Main.parent,
                        tr("An object has been selected on the map. Find the corresponding feature\n"
                         + "on the imagery layer and move that layer accordingly.\n"
                         + "DO NOT touch the selected object, so it can be used by others later."),
                        ImageryOffsetTools.DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
                Main.pref.put("iodb.calibration.message", true);
            }
        }
    }
    
    class DownloadOffsetsTask extends SimpleOffsetQueryTask {
        private ImageryLayer layer;
        private List<ImageryOffsetBase> offsets;

        public DownloadOffsetsTask( LatLon center, ImageryLayer layer, String imagery ) {
            super(null, tr("Loading imagery offsets..."));
            try {
                String query = "get?lat=" + center.lat() + "&lon=" + center.lon()
                        + "&imagery=" + URLEncoder.encode(imagery, "UTF8");
                setQuery(query);
            } catch( UnsupportedEncodingException e ) {
                throw new IllegalArgumentException(e);
            }
            this.layer = layer;
        }

        @Override
        protected void afterFinish() {
            if( !cancelled && offsets != null )
                showOffsetDialog(offsets, layer);
        }
        
        @Override
        protected void processResponse( InputStream inp ) throws UploadException {
            offsets = null;
            try {
                offsets = new IODBReader(inp).parse();
            } catch( Exception e ) {
                throw new UploadException("Error processing XML response: " + e.getMessage());
            }
        }
    }
}
