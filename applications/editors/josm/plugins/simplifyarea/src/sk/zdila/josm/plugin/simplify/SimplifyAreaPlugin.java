package sk.zdila.josm.plugin.simplify;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class SimplifyAreaPlugin extends Plugin {

    public SimplifyAreaPlugin(final PluginInformation info) {
        super(info);
        MainMenu.add(Main.main.menu.toolsMenu, new SimplifyAreaAction());
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new SimplifyAreaPreferenceSetting();
    }

}
