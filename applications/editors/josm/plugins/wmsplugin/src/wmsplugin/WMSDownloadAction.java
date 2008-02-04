package wmsplugin;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView;

public class WMSDownloadAction extends JosmAction {

	private WMSInfo info;
	
	public WMSDownloadAction(WMSInfo info) {
		super(info.name, "wmsmenu", "Download WMS tile from "+info.name, 0, 0, false);
		this.info = info;
	}
	
	public void actionPerformed(ActionEvent e) {
		System.out.println(info.url);
		
		MapView mv = Main.map.mapView;
		
		DownloadWMSTask.download(info.name, info.url);
	}
};

