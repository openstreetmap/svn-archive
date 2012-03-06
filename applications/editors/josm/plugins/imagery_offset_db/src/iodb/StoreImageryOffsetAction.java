package iodb;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Upload the current imagery offset or an calibration object information.
 * 
 * @author zverik
 */
public class StoreImageryOffsetAction extends JosmAction {

    public StoreImageryOffsetAction() {
        super(tr("Store Imagery Offset..."), "storeoffset", tr("Upload an offset for current imagery (or calibration object information) to a server"), null, false);
    }

    public void actionPerformed(ActionEvent e) {
        // todo: check that there is an imagery
        // and that it is moved
        Projection proj = Main.map.mapView.getProjection();
        LatLon center = proj.eastNorth2latlon(Main.map.mapView.getCenter());
        // todo: open an upload window
        // todo: if an object was selected, ask if the user wants to upload it
        // todo: enter all metadata (that is, a description)
        // todo: upload object info to server
    }
    
    private static void upload( ImageryOffsetBase offset ) {
        String base = Main.pref.get("iodb.server.url", "http://offsets.textual.ru/");
        Map<String, String> params = new HashMap<String, String>();
        offset.putServerParams(params);
        // todo
    }
}
