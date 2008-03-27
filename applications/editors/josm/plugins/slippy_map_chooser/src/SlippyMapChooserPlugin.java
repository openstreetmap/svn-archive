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
public class SlippyMapChooserPlugin extends Plugin implements PreferenceChangedListener{
	
	static String iPluginFolder = "";
	
	private static final String KEY_MAX_TILES_IN_DB = "slippy_map_chooser.max_tiles";
	private static final String KEY_MAX_TILES_REDUCE_BY = "slippy_map_chooser.max_tiles_reduce_by";
	
	static int MAX_TILES_IN_DB = 1000;
	static int MAX_TILES_REDUCE_BY = 100;
	
	public SlippyMapChooserPlugin(){
		// create the plugin folder 
//		iPluginFolder = getPluginDir();
//		File pluginFolder = new File(iPluginFolder);
//		if(!pluginFolder.exists())
//			pluginFolder.mkdir();
//		
//		//init the logger
//		Logger.setLogFile(iPluginFolder+"\\slippy_map_chooser.log");
		
		//Add this plugin to the preference changed listener list
		Main.pref.listener.add(this);
		
		//load prefs
		String maxTiles = Main.pref.get(KEY_MAX_TILES_IN_DB);
		if(!maxTiles.equals("")){
			preferenceChanged(KEY_MAX_TILES_IN_DB, maxTiles);
		}else{
			Main.pref.put(KEY_MAX_TILES_IN_DB, String.valueOf(MAX_TILES_IN_DB));
		}
		
		String maxTilesReduce = Main.pref.get(KEY_MAX_TILES_REDUCE_BY);
		if(!maxTilesReduce.equals("")){
			preferenceChanged(KEY_MAX_TILES_REDUCE_BY, maxTilesReduce);
		}else{
			Main.pref.put(KEY_MAX_TILES_REDUCE_BY, String.valueOf(MAX_TILES_REDUCE_BY));
		}
	}
	
	
	public void addDownloadSelection(List<DownloadSelection> list){	
		list.add(new SlippyMapChooser());		
	}


	public void preferenceChanged(String key, String newValue) {
		if(key.equals(KEY_MAX_TILES_IN_DB)){
			try{
				MAX_TILES_IN_DB = Integer.parseInt(newValue);
			}catch(Exception e){
				MAX_TILES_IN_DB = 1000;
			}
			
		}else if(key.equals(KEY_MAX_TILES_REDUCE_BY)){
			try{
				MAX_TILES_REDUCE_BY = Integer.parseInt(newValue);
			}catch(Exception e){
				MAX_TILES_REDUCE_BY = 100;
			}
		}
	}
	
	
	
	
}
