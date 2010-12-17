// License: GPL v2 or later. See LICENSE file for details.
package utilsplugin2;

import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class UtilsPlugin2 extends Plugin {
    JMenuItem unglueRelation;
    JMenuItem addIntersections;
    JMenuItem splitObject;
    JMenuItem selectWayNodes;

    public UtilsPlugin2(PluginInformation info) {
        super(info);
        Main.main.menu.toolsMenu.addSeparator();
        unglueRelation = MainMenu.add(Main.main.menu.toolsMenu, new UnGlueRelationAction());
        addIntersections = MainMenu.add(Main.main.menu.toolsMenu, new AddIntersectionsAction());
        splitObject = MainMenu.add(Main.main.menu.toolsMenu, new SplitObjectAction());
        selectWayNodes = MainMenu.add(Main.main.menu.toolsMenu, new SelectWayNodesAction());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        boolean enabled = newFrame != null;
        unglueRelation.setEnabled(enabled);
        addIntersections.setEnabled(enabled);
        splitObject.setEnabled(enabled);
        selectWayNodes.setEnabled(enabled);
    }
}
