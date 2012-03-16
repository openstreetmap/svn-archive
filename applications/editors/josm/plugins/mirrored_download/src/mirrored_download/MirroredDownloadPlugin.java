package mirrored_download;

import javax.swing.JMenu;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class MirroredDownloadPlugin extends Plugin {

    static JMenu jMenu;

    public MirroredDownloadPlugin(PluginInformation info) {
        super(info);
        MainMenu.addAfter(Main.main.menu.fileMenu, new DownloadAction2(), false, Main.main.menu.download);
        MainMenu.add(Main.main.menu.editMenu, new UrlSelectionAction());
    }
    private static String downloadUrl = "http://overpass.osm.rambler.ru/cgi/xapi?";//"http://overpass-api.de/api/xapi?";

    public static String getDownloadUrl() {
        return downloadUrl;
    }

    public static void setDownloadUrl(String downloadUrl_) {
        downloadUrl = downloadUrl_;
    }
}
