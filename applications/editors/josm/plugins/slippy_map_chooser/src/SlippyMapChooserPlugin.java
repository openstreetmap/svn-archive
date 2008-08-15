// This code has been adapted and copied from code that has been written by Immanuel Scholz and others for JOSM.
// License: GPL. Copyright 2007 by Tim Haussmann

import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;
import org.openstreetmap.josm.gui.download.DownloadSelection;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * @author Tim Haussmann
 */
public class SlippyMapChooserPlugin extends Plugin implements PreferenceChangedListener {

	static String iPluginFolder = "";

	private static final String KEY_MAX_TILES_IN_MEMORY = "slippy_map_chooser.max_tiles";
	private static final String KEY_ENABLE_FILE_CACHE = "slippy_map_chooser.file_cache";

	static int MAX_TILES_IN_MEMORY = 200;
	static boolean ENABLE_FILE_CACHE = true;

	public SlippyMapChooserPlugin() {
		// create the plugin folder
		// iPluginFolder = getPluginDir();
		// File pluginFolder = new File(iPluginFolder);
		// if(!pluginFolder.exists())
		// pluginFolder.mkdir();
		//		
		// //init the logger
		// Logger.setLogFile(iPluginFolder+"\\slippy_map_chooser.log");

		// Add this plugin to the preference changed listener list
		Main.pref.listener.add(this);

		// load prefs
		String maxTiles = Main.pref.get(KEY_MAX_TILES_IN_MEMORY);
		if (!maxTiles.equals("")) {
			preferenceChanged(KEY_MAX_TILES_IN_MEMORY, maxTiles);
		} else {
			Main.pref.put(KEY_MAX_TILES_IN_MEMORY, Integer.toString(MAX_TILES_IN_MEMORY));
		}
		String enableFileCache = Main.pref.get(KEY_ENABLE_FILE_CACHE);
		if (!enableFileCache.equals("")) {
			preferenceChanged(KEY_ENABLE_FILE_CACHE, enableFileCache);
		} else {
			Main.pref.put(KEY_ENABLE_FILE_CACHE, Boolean.toString(ENABLE_FILE_CACHE));
		}
	}

	public void addDownloadSelection(List<DownloadSelection> list) {
		list.add(new SlippyMapChooser());
	}

	public void preferenceChanged(String key, String newValue) {
		if (KEY_MAX_TILES_IN_MEMORY.equals(key)) {
			try {
				MAX_TILES_IN_MEMORY = Integer.parseInt(newValue);
			} catch (Exception e) {
				MAX_TILES_IN_MEMORY = 1000;
			}
		} else if (KEY_ENABLE_FILE_CACHE.equals(key)) {
			try {
				ENABLE_FILE_CACHE = Boolean.parseBoolean(newValue);
			} catch (Exception e) {
				MAX_TILES_IN_MEMORY = 1000;
			}
		}
	}

}
