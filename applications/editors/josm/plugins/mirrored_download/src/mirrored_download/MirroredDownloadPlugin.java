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
    private static boolean addMeta = true;

    public static String getDownloadUrl() {
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            downloadUrl = Main.pref.get("plugin.mirrored_download.preferred-url");
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                downloadUrl = "http://overpass-api.de/api/xapi?";
            }
                
            String metaFlag = Main.pref.get("plugin.mirrored_download.preferred-meta-flag");
            addMeta = !("void".equals(metaFlag));
        }
        return downloadUrl;
    }
    
    public static boolean getAddMeta() {
        return addMeta;
    }

    public static void setDownloadUrl(String downloadUrl_) {
        downloadUrl = downloadUrl_;
    }

    public static void setAddMeta(boolean addMeta_) {
        addMeta = addMeta_;
    }

}
