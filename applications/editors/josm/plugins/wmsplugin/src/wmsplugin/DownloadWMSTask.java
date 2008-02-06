package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DownloadAction;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.Layer;

public class DownloadWMSTask extends PleaseWaitRunnable {

	private WMSLayer wmsLayer;
	private Bounds bounds;
	private double pixelPerDegree;
	
	public DownloadWMSTask(WMSLayer wmsLayer, Bounds bounds, double pixelPerDegree) {
		super(tr("Downloading " + wmsLayer.name));

		this.wmsLayer = wmsLayer;
		this.bounds = bounds;
		this.pixelPerDegree = pixelPerDegree;
	}
	
	@Override public void realRun() throws IOException {
		Main.pleaseWaitDlg.currentAction.setText(tr("Contacting WMS Server..."));
		wmsLayer.grab(bounds, pixelPerDegree);
	}

	@Override protected void cancel() {}
	@Override protected void finish() {}

	public static void download(WMSLayer wmsLayer) {
		MapView mv = Main.map.mapView;

		Bounds b = new Bounds(
			mv.getLatLon(0, mv.getHeight()),
			mv.getLatLon(mv.getWidth(), 0));

		Main.worker.execute(new DownloadWMSTask(wmsLayer, b,
			mv.getWidth() / (b.max.lon() - b.min.lon())));
	}
}
