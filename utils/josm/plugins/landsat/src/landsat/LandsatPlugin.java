package landsat;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.IconToggleButton;

// NW 151006 only add the landsat task when the map frame is initialised with
// data.

public class LandsatPlugin extends Plugin {

	DownloadLandsatTask task;
	LandsatLayer landsatLayer;
	boolean addedAction;

	public LandsatPlugin() {
		landsatLayer = new LandsatLayer
		("http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&"+
				"layers=global_mosaic&styles=&srs=EPSG:4326&"+
		"format=image/jpeg");
		task = new DownloadLandsatTask(landsatLayer);
		task.setEnabled(false);
		Main.main.menu.download.downloadTasks.add(task);

	}

	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if(!addedAction) {
			Main.map.toolBarActions.addSeparator();
			Main.map.toolBarActions.add(new IconToggleButton
						(new LandsatAdjustAction(Main.map)));
			addedAction=true;
		}
		if(oldFrame==null && newFrame!=null) { 
			task.setEnabled(true);
		} else if (oldFrame!=null && newFrame==null ) {
			task.setEnabled(false);
		}
	}
}
