package landsat;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.gui.MapFrame;

// NW 151006 only add the landsat task when the map frame is initialised with
// data.

public class LandsatPlugin extends Plugin {

	DownloadLandsatTask task;

	public LandsatPlugin() {
		task = new DownloadLandsatTask();
		task.setEnabled(false);
		Main.main.menu.download.downloadTasks.add(task);
	}

	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if(oldFrame==null && newFrame!=null) { 
			task.setEnabled(true);
		} else if (oldFrame!=null && newFrame==null ) {
			task.setEnabled(false);
		}
	}
}
