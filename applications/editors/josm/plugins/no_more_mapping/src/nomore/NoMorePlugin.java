package nomore;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Prevent JOSM from loading.
 * 
 * @author zverik
 */
public class NoMorePlugin extends Plugin {

    public NoMorePlugin(PluginInformation info) {
        super(info);
        System.exit(0);
    }
}
