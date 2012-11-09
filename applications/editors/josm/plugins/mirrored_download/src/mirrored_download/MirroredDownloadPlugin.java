// License: GPL. For details, see LICENSE file.
package mirrored_download;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class MirroredDownloadPlugin extends Plugin {

    public MirroredDownloadPlugin(PluginInformation info) {
        super(info);
        MainMenu.addAfter(Main.main.menu.fileMenu, new MirroredDownloadAction(), false, Main.main.menu.download);
        MainMenu.add(Main.main.menu.editMenu, new UrlSelectionAction());
    }
    private static String downloadUrl = null;

    public static String getDownloadUrl() {
        if (downloadUrl == null)
        {
            downloadUrl = Main.pref.get("plugin.mirrored_download.preferred-url");
            if (downloadUrl == null)
                downloadUrl = "http://overpass-api.de/api/xapi?";
        }
        return downloadUrl;
    }

    public static void setDownloadUrl(String downloadUrl_) {
        downloadUrl = downloadUrl_;
    }
}
