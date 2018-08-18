// License: WTFPL. For details, see LICENSE file.
package nomore;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.util.Date;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * Prevent JOSM from loading.
 * 
 * @author zverik
 */
public class NoMorePlugin extends Plugin {

    /**
     * Constructs a new {@code NoMorePlugin}.
     * @param info plugin information
     */
    public NoMorePlugin(PluginInformation info) {
        super(info);
        long startDate = Config.getPref().getLong("nomoremapping.date", 0);
        long lastHash = Math.max(Config.getPref().getLong("pluginmanager.lastupdate", 0) / 1000,
                Math.max(Config.getPref().getLong("cache.motd.html", 0),
                Config.getPref().getLong("cache.bing.attribution.xml", 0))) + Config.getPref().get("osm-download.bounds", "").hashCode();
        boolean sameHash = Config.getPref().getLong("nomoremapping.hash", 0) == lastHash;
        long today = new Date().getTime() / 1000;
        if (startDate == 0 || !sameHash) {
            startDate = today;
            Config.getPref().putLong("nomoremapping.date", startDate);
            Config.getPref().putLong("nomoremapping.hash", lastHash);
        }
        long days = Math.max(today - startDate, 0) / (60*60*24);
        String message;
        if (days == 0)
            message = "Make it one!";
        else if (days < 7)
            message = "Keep going!";
        else if (days < 31)
            message = "You're good. Keep on!";
        else
            message = "You don't use Potlach instead, do you?";
        String intro = tr("Days without mapping: {0}.", days);
        String prefs;
        try {
             prefs = Preferences.main().getPreferenceFile().getCanonicalPath();
        } catch (IOException e) {
            prefs = Preferences.main().getPreferenceFile().getAbsolutePath();
        }
        String howto = days > 0 ? "" : "\n\n" + tr("(To miserably continue mapping, edit out no_more_mapping\nfrom {0})", prefs);
        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), intro + " " + message + howto, 
                "No More Mapping", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
}
