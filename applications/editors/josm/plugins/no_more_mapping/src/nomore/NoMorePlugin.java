package nomore;

import java.util.Date;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import static org.openstreetmap.josm.tools.I18n.tr;

Remove this garbage line if you really want to compile the plugin. 

/**
 * Prevent JOSM from loading.
 * 
 * @author zverik
 */
public class NoMorePlugin extends Plugin {

    public NoMorePlugin(PluginInformation info) {
        super(info);
        long startDate = Main.pref.getLong("nomoremapping.date", 0);
        long lastHash = Math.max(Main.pref.getLong("pluginmanager.lastupdate", 0) / 1000,
                Math.max(Main.pref.getLong("cache.motd.html", 0),
                Main.pref.getLong("cache.bing.attribution.xml", 0))) + Main.pref.get("osm-download.bounds", "").hashCode();
        boolean sameHash = Main.pref.getLong("nomoremapping.hash", 0) == lastHash;
        long today = new Date().getTime() / 1000;
        if( startDate == 0 || !sameHash ) {
            startDate = today;
            Main.pref.putLong("nomoremapping.date", startDate);
            Main.pref.putLong("nomoremapping.hash", lastHash);
        }
        long days = Math.max(today - startDate, 0) / (60*60*24);
        String message;
        if( days == 0 )
            message = "Make it one!";
        else if( days < 7 )
            message = "Keep going!";
        else if( days < 31 )
            message = "You're good. Keep on!";
        else
            message = "You don't use Potlach instead, do you?";
        String intro = tr("Days without mapping: {0}.", days);
        String prefs;
        try {
             prefs = Main.pref.getPreferenceFile().getCanonicalPath();
        } catch( Exception e ) {
            prefs = Main.pref.getPreferenceFile().getAbsolutePath();
        }
        String howto = days > 0 ? "" : "\n\n" + tr("(To miserably continue mapping, edit out no_more_mapping\nfrom {0})", prefs);
        JOptionPane.showMessageDialog(Main.parent, intro + " " + message + howto, "No More Mapping", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
}
