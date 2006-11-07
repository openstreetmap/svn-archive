package landsat;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;

import javax.swing.JCheckBox;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DownloadAction;
import org.openstreetmap.josm.actions.DownloadAction.DownloadTask;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;

public class DownloadLandsatTask extends PleaseWaitRunnable implements DownloadTask {

	private LandsatLayer landsatLayer;
	private double minlat, minlon, maxlat, maxlon;
	private JCheckBox checkBox = new JCheckBox(tr("Landsat background images"));

	public DownloadLandsatTask(LandsatLayer landsatLayer) {
		super(tr("Downloading data"));
		this.landsatLayer = landsatLayer;
	}

	@Override public void realRun() throws IOException {
		landsatLayer.grab(minlat,minlon,maxlat,maxlon);
	}

	@Override protected void finish() {
		if (landsatLayer != null)
			Main.main.addLayer(landsatLayer);
	}

	@Override protected void cancel() {
	}


	public void download(DownloadAction action, double minlat, double minlon, double maxlat, double maxlon) {
		this.minlat=minlat;
		this.minlon=minlon;
		this.maxlat=maxlat;
		this.maxlon=maxlon;
		Main.worker.execute(this);
	}

	public JCheckBox getCheckBox() {
		return checkBox;
	}

	public void setEnabled(boolean b) {
		checkBox.setEnabled(b); 
	}

	public String getPreferencesSuffix() {
		return "landsat";
	}

}
