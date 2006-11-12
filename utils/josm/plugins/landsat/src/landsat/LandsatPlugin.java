package landsat;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.IconToggleButton;

// NW 151006 only add the landsat task when the map frame is initialised with
// data.

public class LandsatPlugin extends Plugin {

	DownloadWMSTask task, npeTask;
	WMSLayer landsatLayer;
	OSGBLayer npeLayer;

	public LandsatPlugin() {
		landsatLayer = new WMSLayer
		("http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&"+
				"layers=global_mosaic&styles=&srs=EPSG:4326&"+
		"format=image/jpeg");
		npeLayer = new OSGBLayer
			("http://nick.dev.openstreetmap.org/openpaths/"+
			 "freemap.php?layers=npe&");
		task = new DownloadWMSTask(landsatLayer, "landsat",
											"Landsat images");
		npeTask = new DownloadWMSTask(npeLayer, "npe", 
			"New Popular Edition maps - " +
			"see npemap.org.uk for conditions of use");
		task.setEnabled(false);
		npeTask.setEnabled(false);
		Main.main.menu.download.downloadTasks.add(task);
		Main.main.menu.download.downloadTasks.add(npeTask);
	}

	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if(oldFrame==null && newFrame!=null) { 
			task.setEnabled(true);
			npeTask.setEnabled(true);

			/* re. bug report (Andy Robinson) 08/11/06...
			 * Neither of these seem to work
			task.setCheckBoxSelected(false);
			Main.pref.put("download.landsat",false);
			*/

			Main.map.toolBarActions.addSeparator();
			IconToggleButton button = new IconToggleButton
						(new WMSAdjustAction(Main.map));
			Main.map.toolBarActions.add(button);
			Main.map.toolGroup.add(button);
		} else if (oldFrame!=null && newFrame==null ) {
			task.setEnabled(false);
			npeTask.setEnabled(false);
		}
	}
}
