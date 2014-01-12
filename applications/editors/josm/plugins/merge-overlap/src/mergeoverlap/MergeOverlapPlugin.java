package mergeoverlap;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * A plugin merge overlapping part of selected ways to fix warns
 */
public class MergeOverlapPlugin extends Plugin {

    protected String name;

    public MergeOverlapPlugin(PluginInformation info) {
        super(info);
        name = tr("Merge overlap");
        MainMenu.add(Main.main.menu.moreToolsMenu, new MergeOverlapAction());
    }
}
