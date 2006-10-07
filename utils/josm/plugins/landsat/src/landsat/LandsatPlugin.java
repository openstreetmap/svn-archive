package landsat;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;

public class LandsatPlugin extends Plugin {

	public LandsatPlugin() {
		Main.main.downloadAction.downloadTasks.add(new DownloadLandsatTask());
	}
}
