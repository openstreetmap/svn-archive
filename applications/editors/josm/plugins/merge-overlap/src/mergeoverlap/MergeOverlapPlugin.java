package mergeoverlap;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * A plugin merge overlapping part of selected ways to fix warns
 */
public class MergeOverlapPlugin extends Plugin {

    /**
     * Constructs a new {@code MergeOverlapPlugin}.
     * @param info plugin information
     */
    public MergeOverlapPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(MainApplication.getMenu().moreToolsMenu, new MergeOverlapAction());
    }
}
