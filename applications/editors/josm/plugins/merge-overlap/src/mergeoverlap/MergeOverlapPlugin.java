package mergeoverlap;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * A plugin merge overlapping part of selected ways to fix warns
 */
public class MergeOverlapPlugin extends Plugin {

    protected String name;

        public MergeOverlapPlugin(PluginInformation info) {
        super(info);
        name = tr("Merge overlap", null);
        JMenu toolsMenu = null;
        for (int i = 0; i < Main.main.menu.getMenuCount() && toolsMenu == null; i++) {
            JMenu menu = Main.main.menu.getMenu(i);
            String name = menu.getText();
            if (name != null && name.equals(tr("Tools", null))) {
                toolsMenu = menu;
            }
        }

        if (toolsMenu == null) {
            toolsMenu = new JMenu(name);
            toolsMenu.add(new JMenuItem(new MergeOverlapAction()));
            Main.main.menu.add(toolsMenu, 2);
        } else {
            toolsMenu.addSeparator();
            toolsMenu.add(new JMenuItem(new MergeOverlapAction()));
        }
    }
}
