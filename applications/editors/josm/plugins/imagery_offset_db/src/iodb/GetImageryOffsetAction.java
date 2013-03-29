package iodb;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.CoordinateFormat;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Download a list of imagery offsets for the current position, let user choose which one to use.
 * 
 * @author Zverik
 * @license WTFPL
 */
public class GetImageryOffsetAction extends JosmAction implements ImageryOffsetWatcher.OffsetStateListener {
    private Icon iconOffsetOk;
    private Icon iconOffsetBad;
    
    /**
     * Initialize the action. Sets "Ctrl+Alt+I" shortcut: the only shortcut in this plugin.
     * Also registers itself with {@link ImageryOffsetWatcher}.
     */
    public GetImageryOffsetAction() {
        super(tr("Get Imagery Offset..."), "getoffset", tr("Download offsets for current imagery from a server"),
                Shortcut.registerShortcut("imageryoffset:get", tr("Imagery: {0}", tr("Get Imagery Offset...")),
                KeyEvent.VK_I, Shortcut.ALT_CTRL), true);
        iconOffsetOk = ImageProvider.get("getoffset");
        iconOffsetBad = ImageProvider.get("getoffsetnow");
        ImageryOffsetWatcher.getInstance().register(this);
    }

    /**
     * The action just executes {@link DownloadOffsetsTask}.
     */
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

    /**
     * This action is enabled when there's a map, mapView and one of the layers
     * is an imagery layer.
     */
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
    
    /**
     * Display a dialog for choosing between offsets. If there are no offsets in
     * the list, displays the relevant message instead.
     * @param offsets List of offset objects to choose from.
     */
    private void showOffsetDialog( List<ImageryOffsetBase> offsets ) {
        if( offsets.isEmpty() ) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("No data for this region. Please adjust imagery layer and upload an offset."),
                    ImageryOffsetTools.DIALOG_TITLE, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        OffsetDialog offsetDialog = new OffsetDialog(offsets);
        if( offsetDialog.showDialog() != null )
            offsetDialog.applyOffset();
    }

    /**
     * Update action icon based on an offset state.
     */
    public void offsetStateChanged( boolean isOffsetGood ) {
        putValue(Action.SMALL_ICON, isOffsetGood ? iconOffsetOk : iconOffsetBad);
    }

    /**
     * Remove offset listener.
     */
    @Override
    public void destroy() {
        ImageryOffsetWatcher.getInstance().unregister(this);
        super.destroy();
    }

    /**
     * A task that downloads offsets for a given position and imagery layer,
     * then parses resulting XML and calls
     * {@link #showOffsetDialog(java.util.List)} on success.
     */
    private class DownloadOffsetsTask extends SimpleOffsetQueryTask {
        private ImageryLayer layer;
        private List<ImageryOffsetBase> offsets;

        /**
         * Initializes query object from the parameters.
         * @param center A center point of a map view.
         * @param layer The topmost imagery layer.
         * @param imagery Imagery ID for the layer.
         */
        public DownloadOffsetsTask( LatLon center, ImageryLayer layer, String imagery ) {
            super(null, tr("Loading imagery offsets..."));
            try {
                String query = "get?lat=" + center.latToString(CoordinateFormat.DECIMAL_DEGREES)
                        + "&lon=" + center.lonToString(CoordinateFormat.DECIMAL_DEGREES)
                        + "&imagery=" + URLEncoder.encode(imagery, "UTF8");
                int radius = Main.pref.getInteger("iodb.radius", -1);
                if( radius > 0 )
                    query = query + "&radius=" + radius;
                setQuery(query);
            } catch( UnsupportedEncodingException e ) {
                throw new IllegalArgumentException(e);
            }
            this.layer = layer;
        }

        /**
         * Displays offset dialog on success.
         */
        @Override
        protected void afterFinish() {
            if( !cancelled && offsets != null )
                showOffsetDialog(offsets);
        }
        
        /**
         * Parses the response with {@link IODBReader}.
         * @param inp Response input stream.
         * @throws iodb.SimpleOffsetQueryTask.UploadException Thrown on XML parsing error.
         */
        @Override
        protected void processResponse( InputStream inp ) throws UploadException {
            offsets = null;
            try {
                offsets = new IODBReader(inp).parse();
            } catch( Exception e ) {
                throw new UploadException(tr("Error processing XML response: {0}", e.getMessage()));
            }
        }
    }
}
