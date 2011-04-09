// License: GPL v2 or later. See LICENSE file for details.
package utilsplugin2;

import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import static org.openstreetmap.josm.tools.I18n.marktr;

public class UtilsPlugin2 extends Plugin {
    JMenuItem unglueRelation;
    JMenuItem addIntersections;
    JMenuItem splitObject;
    JMenuItem selectWayNodes;
    JMenuItem adjNodes;
    JMenuItem adjWays;
    JMenuItem adjWaysAll;
    JMenuItem intWays;
    JMenuItem intWaysR;

    public UtilsPlugin2(PluginInformation info) {
        super(info);
        Main.main.menu.toolsMenu.addSeparator();
        unglueRelation = MainMenu.add(Main.main.menu.toolsMenu, new UnGlueRelationAction());
        addIntersections = MainMenu.add(Main.main.menu.toolsMenu, new AddIntersectionsAction());
        splitObject = MainMenu.add(Main.main.menu.toolsMenu, new SplitObjectAction());
        Main.main.menu.toolsMenu.addSeparator();
        JMenu m1 = Main.main.menu.addMenu(marktr("Selection"), KeyEvent.VK_N, Main.main.menu.defaultMenuPos, "help");

        selectWayNodes = MainMenu.add(m1, new SelectWayNodesAction());
        adjNodes = MainMenu.add(m1, new AdjacentNodesAction());
        adjWays = MainMenu.add(m1, new AdjacentWaysAction());
        adjWaysAll = MainMenu.add(m1, new ConnectedWaysAction());
        intWays = MainMenu.add(m1, new IntersectedWaysAction());
        intWaysR = MainMenu.add(m1, new IntersectedWaysRecursiveAction());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        boolean enabled = newFrame != null;
        enabled = false;
        unglueRelation.setEnabled(enabled);
        addIntersections.setEnabled(enabled);
        splitObject.setEnabled(enabled);
        selectWayNodes.setEnabled(enabled);
        adjNodes.setEnabled(enabled);
        adjWays.setEnabled(enabled);
        adjWaysAll.setEnabled(enabled);
        intWays.setEnabled(enabled);
        intWaysR.setEnabled(enabled);
    }
}
