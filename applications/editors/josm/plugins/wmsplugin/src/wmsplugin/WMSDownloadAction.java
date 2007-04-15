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
		
		// store the download task with the "info" object. if we create a new
		// download task here every time, then different layers are displayed even
		// for images from the same server, and we don't want that.
		
		if (info.downloadTask == null)
			info.downloadTask = new DownloadWMSTask(info.name, info.url);
		
		MapView mv = Main.map.mapView;
		
		info.downloadTask.download(null,
				mv.getLatLon(0, mv.getHeight()).lat(),
				mv.getLatLon(0, mv.getHeight()).lon(),
				mv.getLatLon(mv.getWidth(), 0).lat(),
				mv.getLatLon(mv.getWidth(), 0).lon());			
	}
};

