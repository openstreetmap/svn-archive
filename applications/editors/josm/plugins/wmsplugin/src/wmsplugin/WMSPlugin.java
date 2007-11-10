package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.actions.JosmAction;


// NW 151006 only add the landsat task when the map frame is initialised with
// data.

public class WMSPlugin extends Plugin {

	static String VERSION = "2.4";
	
	DownloadWMSTask task;
	WMSLayer wmsLayer;
	static JMenu wmsJMenu;

	static ArrayList<WMSInfo> wmsList = new ArrayList<WMSInfo>();
	
	public WMSPlugin() {
		refreshMenu();
	}

	// this parses the preferences settings. preferences for the wms plugin have to
	// look like this:
	// wmsplugin.1.name=Landsat
	// wmsplugin.1.url=http://and.so.on/
	
	public static void refreshMenu() {
		wmsList.clear();
		Map<String,String> prefs = Main.pref.getAllPrefix("wmsplugin.url.");
		
		TreeSet<String> keys = new TreeSet<String>(prefs.keySet());
		int prefid = 0;
		String name = null;
		String url = null;
		int lastid = -1;
		for (String key : keys) {
			String[] elements = key.split("\\.");
			if (elements.length != 4) continue;
			try {
				prefid = Integer.parseInt(elements[2]);
			} catch(NumberFormatException e) {
				continue;
			}
			if (prefid != lastid) {
				if ((name != null) && (url != null)) {
					wmsList.add(new WMSInfo(name, url, prefid));
				}
				name = null; url = null; lastid = prefid; 
			}
			if (elements[3].equals("name")) {
				name=prefs.get(key);
			} else if (elements[3].equals("url")) {
				url = prefs.get(key);
			}		
		}
		if ((name != null) && (url != null)) {
			wmsList.add(new WMSInfo(name, url, prefid));
		}
		
		// if no (valid) prefs are set, initialize to a sensible default.
		if (wmsList.isEmpty()) {
			WMSInfo landsatInfo = new WMSInfo("Landsat", 
					"http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&"+
					"layers=global_mosaic&styles=&srs=EPSG:4326&"+
					"format=image/jpeg", 1);
			landsatInfo.save();
			wmsList.add(landsatInfo);
			
			WMSInfo npeInfo = new WMSInfo("NPE Maps", 
					"http://nick.dev.openstreetmap.org/openpaths/freemap.php?layers=npe&", 2);
			npeInfo.save();
			wmsList.add(npeInfo);
		}
		
		JMenuBar menu = Main.main.menu;

		if (wmsJMenu == null) {
			wmsJMenu = new JMenu(tr("WMS"));
			menu.add(wmsJMenu, 5);
		} else {
			wmsJMenu.removeAll();
		}
		
		// for each configured WMSInfo, add a menu entry.
		for (final WMSInfo u : wmsList) {
			wmsJMenu.add(new JMenuItem(new WMSDownloadAction(u)));
		}
		wmsJMenu.addSeparator();
		wmsJMenu.add(new JMenuItem(new Map_Rectifier_WMSmenuAction()));
		
		wmsJMenu.addSeparator();
		wmsJMenu.add(new JMenuItem(new 
				JosmAction("Blank Layer", "blankmenu", "Open a blank WMS layer to load data from a file", 0, 0, false) {
			public void actionPerformed(ActionEvent ev) {
				Main.main.addLayer(new WMSLayer());
			}
		}));
		wmsJMenu.addSeparator();
		wmsJMenu.add(new JMenuItem(new Help_WMSmenuAction()));
		setEnabledAll(false);
	}
	
	private static void setEnabledAll(boolean isEnabled) {
		for(int i=0; i < wmsJMenu.getItemCount(); i++) {
			JMenuItem item = wmsJMenu.getItem(i);
			
			if(item != null) item.setEnabled(isEnabled);
		}
	}
	
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (oldFrame==null && newFrame!=null) { 
			setEnabledAll(true);
			Main.map.toolBarActions.addSeparator();
			Main.map.toolBarActions.add(new IconToggleButton
						(new WMSAdjustAction(Main.map)));
		} else if (oldFrame!=null && newFrame==null ) {
			setEnabledAll(false);
		}
	}
	
	public PreferenceSetting getPreferenceSetting() {
		return new WMSPreferenceEditor();
	}
}
