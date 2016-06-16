package poly;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Poly reader/writer plugin.
 * 
 * @author zverik
 */
public class PolyPlugin extends Plugin {

    /**
     * Constructs a new {@code PolyPlugin}.
     * @param info plugin information
     */
    public PolyPlugin(PluginInformation info) {
        super(info);
        ExtensionFileFilter.addImporter(new PolyImporter());
        ExtensionFileFilter.addExporter(new PolyExporter());
        Main.main.menu.openLocation.addDownloadTaskClass(DownloadPolyTask.class);
    }
}
