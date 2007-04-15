package landsat;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;

import javax.swing.JCheckBox;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DownloadAction;
import org.openstreetmap.josm.actions.DownloadAction.DownloadTask;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;

public class DownloadWMSTask extends PleaseWaitRunnable 
		implements DownloadTask {

	private WMSLayer wmsLayer;
	private double minlat, minlon, maxlat, maxlon;
	private JCheckBox checkBox;
	String name;

	public DownloadWMSTask(WMSLayer wmsLayer, String briefName,
									String detailedName) {
		super(tr("Downloading data"));
   		checkBox = new JCheckBox(tr(detailedName));
		this.wmsLayer = wmsLayer;
		this.name=briefName;
	}

	@Override public void realRun() throws IOException {
		wmsLayer.grab(minlat,minlon,maxlat,maxlon);
	}

	@Override protected void finish() {
		if (wmsLayer != null)
			Main.main.addLayer(wmsLayer);
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

	public void setCheckBoxSelected(boolean b) {
		checkBox.setSelected(b);
	}

	public String getPreferencesSuffix() {
		return name; 
	}

}
