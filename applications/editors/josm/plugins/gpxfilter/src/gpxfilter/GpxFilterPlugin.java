package gpxfilter;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class GpxFilterPlugin extends Plugin {

    public GpxFilterPlugin(PluginInformation info) {
        super(info);
        JMenu historyMenu = Main.main.menu.addMenu(marktr("GPX"), KeyEvent.VK_R,
                Main.main.menu.defaultMenuPos,null);
        //MainMenu.add(historyMenu, new ObjectsHistoryAction());
        MainMenu.add(historyMenu, new AddEGpxLayerAction());
    }

}
