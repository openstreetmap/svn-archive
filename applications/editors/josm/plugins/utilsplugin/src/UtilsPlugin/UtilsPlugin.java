package UtilsPlugin;

import static org.openstreetmap.josm.tools.I18n.tr;


import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BoxLayout;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.actions.JosmAction;

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
