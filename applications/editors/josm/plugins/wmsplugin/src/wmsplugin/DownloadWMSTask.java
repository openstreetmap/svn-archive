package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DownloadAction;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.Layer;

public class DownloadWMSTask extends PleaseWaitRunnable {

	private WMSLayer wmsLayer;
	private Bounds bounds;
	
	public DownloadWMSTask(WMSLayer wmsLayer, Bounds bounds) {
		super(tr("Downloading " + wmsLayer.name));

		this.wmsLayer = wmsLayer;
		this.bounds = bounds;
	}
	
	@Override public void realRun() throws IOException {
		Main.pleaseWaitDlg.currentAction.setText(tr("Contacting WMS Server..."));
		wmsLayer.grab(
			bounds.min.lat(), bounds.min.lon(),
			bounds.max.lat(), bounds.max.lon());
	}

	@Override protected void cancel() {}
	@Override protected void finish() {}

	public static void download(String name, String wmsurl,
			double minlat, double minlon, double maxlat, double maxlon) {
		WMSLayer wmsLayer = null;

		// simply check if we already have a layer created. if not, create; if yes, reuse.
		for (Layer l : Main.main.map.mapView.getAllLayers()) {
			if (l instanceof WMSLayer && l.name.equals(name)) {
				wmsLayer = (WMSLayer) l;
			}
		}

		if (wmsLayer == null) {
			if (wmsurl.matches("(?i).*layers=npeoocmap.*") || wmsurl.matches("(?i).*layers=npe.*") ){
				//then we use the OSGBLayer
				wmsLayer= new OSGBLayer(name, wmsurl);
			} else {
				wmsLayer = new WMSLayer(name, wmsurl); 
			}
			Main.main.addLayer(wmsLayer);
		} 

		Main.worker.execute(new DownloadWMSTask(wmsLayer,
			new Bounds(new LatLon(minlat, minlon), new LatLon(maxlat, maxlon))));
	}
}
