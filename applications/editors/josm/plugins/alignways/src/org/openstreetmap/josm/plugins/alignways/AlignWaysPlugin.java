package org.openstreetmap.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * @author tilusnet <tilusnet@gmail.com>
 *
 */

public class AlignWaysPlugin extends Plugin {

    static AlignWaysMode awMode;
    private final IconToggleButton btn;
    static JMenuItem alignWaysMenuItem;
    static JosmAction awAction;

    /**
     * 
     */
    public AlignWaysPlugin(PluginInformation info) {
        super(info);
        awMode = new AlignWaysMode(Main.map, "alignways", tr("Align Ways mode"));
        btn = new IconToggleButton(awMode);
        btn.setVisible(true);
        Main.main.menu.toolsMenu.addSeparator();
        awAction = new AlignWaysAction();
        alignWaysMenuItem = MainMenu.add(Main.main.menu.toolsMenu, awAction);
        Main.main.menu.toolsMenu.addSeparator();

    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (Main.map != null) {
            Main.map.addMapMode(btn);
        }
    }

    /**
     * @return the awAction
     */
    public static JosmAction getAwAction() {
        return awAction;
    }

    /**
     * @return the awMode
     */
    public static AlignWaysMode getAwMode() {
        return awMode;
    }

}
