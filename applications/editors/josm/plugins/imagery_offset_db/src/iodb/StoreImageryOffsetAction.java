package iodb;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.gui.JosmUserIdentityManager;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Upload the current imagery offset or an calibration object information.
 * 
 * @author zverik
 */
public class StoreImageryOffsetAction extends JosmAction {

    public StoreImageryOffsetAction() {
        super(tr("Store Imagery Offset..."), "storeoffset",
                tr("Upload an offset for current imagery (or calibration object information) to a server"),
                null, false);
    }

    public void actionPerformed(ActionEvent e) {
        if( Main.map == null || Main.map.mapView == null || getCurrentDataSet() == null )
            return;

        ImageryLayer layer = ImageryOffsetTools.getTopImageryLayer();
        if( layer == null )
            return;

        String userName = JosmUserIdentityManager.getInstance().getUserName();
        if( userName == null ) {
            JOptionPane.showMessageDialog(Main.parent, tr("To store imagery offsets you must be a registered OSM user."), ImageryOffsetTools.DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
            
        // check if an object suitable for calibration is selected
        OsmPrimitive calibration = null;
        Collection<OsmPrimitive> selectedObjects = getCurrentDataSet().getSelected();
        if( selectedObjects.size() == 1 ) {
            OsmPrimitive selection = selectedObjects.iterator().next();
            if( selection instanceof Node || selection instanceof Way ) {
                boolean suitable = !selection.isNewOrUndeleted() && !selection.isDeleted() && !selection.isModified();
                if( selection instanceof Way ) {
                    for( Node n : ((Way)selection).getNodes() )
                        if( n.isNewOrUndeleted() || n.isDeleted() || n.isModified() )
                            suitable = false;
                } else if( selection.isReferredByWays(1) ) {
                    suitable = false;
                }
                if( suitable ) {
                    String[] options = new String[] {tr("Store calibration object"), tr("Store imagery offset"), tr("Cancel")};
                    int result = JOptionPane.showOptionDialog(Main.parent,
                            tr("The selected object can be used as a calibration object. What do you intend to do?"), ImageryOffsetTools.DIALOG_TITLE, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, options, options[0]);
                    if( result == 2 || result == JOptionPane.CLOSED_OPTION )
                        return;
                    if( result == 0 )
                        calibration = selection;
                } else {
                    String[] options = new String[] {tr("Store imagery offset"), tr("Cancel")};
                    int result = JOptionPane.showOptionDialog(Main.parent,
                            tr("You have an object selected and might want to use it as a calibration object.\n"
                             + "But in this case it should be uploaded to OSM server first."), ImageryOffsetTools.DIALOG_TITLE, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[1]);
                    if( result == 1 || result == JOptionPane.CLOSED_OPTION )
                        return;
                }
            }
        }

        Object message = "";
        LatLon center = ImageryOffsetTools.getMapCenter();
        ImageryOffsetBase offsetObj;
        if( calibration == null ) {
            // register imagery offset
            if( Math.abs(layer.getDx()) < 1e-8 && Math.abs(layer.getDy()) < 1e-8 ) {
                if( JOptionPane.showConfirmDialog(Main.parent,
                        tr("The topmost imagery layer has no offset. Are you sure you want to upload it?"), ImageryOffsetTools.DIALOG_TITLE, JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION )
                    return;
            }
            LatLon offset = ImageryOffsetTools.getLayerOffset(layer, center);
            offsetObj = new ImageryOffset(ImageryOffsetTools.getImageryID(layer), offset);
            message = "You are registering an imagery offset.\n"
                    + "Other users in this area will be able to use it for mapping.\n"
                    + "Please make sure it is as precise as possible, and\n"
                    + "describe a region this offset is applicable to.";
        } else {
            // register calibration object
            offsetObj = new CalibrationObject(calibration);
            message = "You are registering calibration object.\n"
                    + "It should be the most precisely positioned object,\n"
                    + "with clearly visible boundaries on various satellite imagery.\n"
                    + "Please describe a region where this object is located.";
        }
        offsetObj.setBasicInfo(center, userName, null, null);
        String description = null;
        boolean iterated = false;
        while( description == null ) {
            description = JOptionPane.showInputDialog(Main.parent, message, ImageryOffsetTools.DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE);
            if( description == null || description.length() == 0 )
                return;
            if( description.length() < 3 || description.length() > 200 ) {
                description = null;
                if( !iterated ) {
                    message = message + "\n" + tr("Description should be 3 to 200 letters long.");
                    iterated = true;
                }
            }
        }
        offsetObj.setDescription(description);

        // upload object info to server
        try {
            Map<String, String> params = new HashMap<String, String>();
            offsetObj.putServerParams(params);
            StringBuilder query = null;
            for( String key : params.keySet() ) {
                if( query == null ) {
                    query = new StringBuilder("store?");
                } else {
                    query.append('&');
                }
                query.append(key).append('=').append(URLEncoder.encode(params.get(key), "UTF8"));
            }
            Main.main.worker.submit(new SimpleOffsetQueryTask(query.toString(), tr("Uploading the new offset...")));
        } catch( UnsupportedEncodingException ex ) {
            // WTF
        }
    }

    @Override
    protected void updateEnabledState() {
        boolean state = true;
        if( Main.map == null || Main.map.mapView == null || !Main.map.isVisible() )
            state = false;
        if( ImageryOffsetTools.getTopImageryLayer() == null )
            state = false;
        setEnabled(state);
    }
}
