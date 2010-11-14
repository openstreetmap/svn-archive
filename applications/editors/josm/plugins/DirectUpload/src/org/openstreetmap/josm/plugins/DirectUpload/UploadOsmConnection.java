// ...

package org.openstreetmap.josm.plugins.DirectUpload;

import java.net.HttpURLConnection;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.io.OsmConnection;
import org.openstreetmap.josm.io.OsmTransferException;

/**
 * Work-around and utility class for DirectUpload.
 * 
 * @author ax
 */
public class UploadOsmConnection extends OsmConnection {

	// singleton, see http://en.wikipedia.org/wiki/Singleton_pattern#Traditional_simple_way
	private static final UploadOsmConnection INSTANCE = new UploadOsmConnection();

	// Private constructor prevents instantiation from other classes
	private UploadOsmConnection() {
	}

	public static UploadOsmConnection getInstance() {
		return UploadOsmConnection.INSTANCE;
	}

	// make protected OsmConnection::addAuth() available to others
	public void addAuthHack(HttpURLConnection connection) throws OsmTransferException {
		addAuth(connection);
	}

    /**
     * find which gpx layer holds the trace to upload. layers are tried in this order:
     * 
     * 1. selected (*not* active - think "zoom to layer"), from first to last
     * 2. not selectd - if there is only one 
     * 3. active
     *
     * @return data of the selected gpx layer, or null if there is none
     */
    GpxData autoSelectTrace() {
        if (Main.map != null && Main.map.mapView != null) {
            MapView mv = Main.map.mapView;
//            List<Layer> allLayers = new ArrayList<Layer>(mv.getAllLayersAsList());  // modifiable
            List<Layer> selectedLayers = LayerListDialog.getInstance().getModel().getSelectedLayers();
            List<GpxLayer> gpxLayersRemaining = mv.getLayersOfType(GpxLayer.class);
            gpxLayersRemaining.removeAll(selectedLayers);
            GpxLayer traceLayer = null;
            // find the first gpx layer inside selected layers
            for (Layer l : LayerListDialog.getInstance().getModel().getSelectedLayers()) {
                if (l instanceof GpxLayer) {
                    traceLayer = (GpxLayer) l;
                    break;
                }
            }
            if (traceLayer == null) {
                // if there is none, try the none selected gpx layers. if there is only one, use it.
                if (gpxLayersRemaining.size() == 1) {
                    traceLayer = gpxLayersRemaining.get(0); 
                }
                // active layer
                else if (mv.getActiveLayer() instanceof GpxLayer) {
                    traceLayer = (GpxLayer) mv.getActiveLayer(); 
                }
            }

            if (traceLayer != null) {
                GpxData data = traceLayer.data;
                return data;
            }
        }
        
        return null;
    }
}
