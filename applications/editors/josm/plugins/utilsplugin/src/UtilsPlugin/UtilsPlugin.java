package UtilsPlugin;

import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;

public class UtilsPlugin extends Plugin {
    JMenuItem SimplifyWay;
    JMenuItem JoinAreas;
    JumpToAction JumpToAct = new JumpToAction();

    public UtilsPlugin() {
        SimplifyWay = MainMenu.add(Main.main.menu.toolsMenu, new SimplifyWayAction());
        JoinAreas = MainMenu.add(Main.main.menu.toolsMenu, new JoinAreasAction());
        SimplifyWay.setEnabled(false);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            SimplifyWay.setEnabled(true);
            JoinAreas.setEnabled(true);
            newFrame.statusLine.addMouseListener(JumpToAct);
        }
    }
}
