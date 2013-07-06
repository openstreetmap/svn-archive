package org.openstreetmap.josm.plugin.download_along;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class DownloadAlong extends Plugin {
	public static final String PREF_DOWNLOAD_ALONG_TRACK_DISTANCE = "downloadAlong.downloadAlongTrack.distance";
	public static final String PREF_DOWNLOAD_ALONG_TRACK_AREA = "downloadAlong.downloadAlongTrack.area";

	public static final String PREF_DOWNLOAD_ALONG_OSM = "downloadAlong.download.osm";
	public static final String PREF_DOWNLOAD_ALONG_GPS = "downloadAlong.download.gps";

	public DownloadAlong(PluginInformation info) {
		super(info);
		MainMenu.add(Main.main.menu.toolsMenu, new DownloadAlongAction());
	}
}
