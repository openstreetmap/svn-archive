package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;

public class WMSDownloadAction extends JosmAction {

	private WMSInfo info;
	
	public WMSDownloadAction(WMSInfo info) {
		super(info.name, "wmsmenu", tr("Download WMS tile from {0}",info.name), 0, 0, false);
		this.info = info;
	}
	
	public void actionPerformed(ActionEvent e) {
		System.out.println(info.url);
		
		DownloadWMSTask.download(getLayer(info));
	}

	public static WMSLayer getLayer(WMSInfo info) {
		// simply check if we already have a layer created. if not, create; if yes, reuse.
		for (Layer l : Main.main.map.mapView.getAllLayers()) {
			if (l instanceof WMSLayer && l.name.equals(info.name)) {
				return (WMSLayer) l;
			}
		}

		// FIXME: move this to WMSPlugin/WMSInfo/preferences.
		WMSLayer wmsLayer = new WMSLayer(info.name, info.grabber);
		Main.main.addLayer(wmsLayer);
		return wmsLayer;
	}
};

