package com.tilusnet.josm.plugins.alignways;

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
    static AlignWaysDialog awDialog;
    static IconToggleButton optBtn;

    // The major version is e.g. used to decide when to trigger What's New windows
    public static final int AlignWaysMajorVersion = 2;

    public AlignWaysPlugin(PluginInformation info) {
        super(info);
        awMode = new AlignWaysMode(Main.map, "alignways", tr("Align Ways mode"));
        btn = new IconToggleButton(awMode);
        btn.setVisible(true);
        Main.main.menu.toolsMenu.addSeparator();
        awAction = new AlignWaysAction();
        alignWaysMenuItem = MainMenu.add(Main.main.menu.toolsMenu, awAction);
        awDialog = new AlignWaysDialog(awMode);
        // Prevent user clicking on the Windows menu entry while panel is meaningless
        awDialog.getWindowMenuItem().setEnabled(false);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if(newFrame != null) {
            optBtn = newFrame.addToggleDialog(AlignWaysPlugin.getAwDialog());
        }
        if (Main.map != null) {
            Main.map.addMapMode(btn);
            // Re-enable menu item in Windows menu
            awDialog.getWindowMenuItem().setEnabled(true);
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

    /**
     * @return the awDialog
     */
    public static AlignWaysDialog getAwDialog() {
        return awDialog;
    }

    /**
     * @return the optBtn
     */
    public static IconToggleButton getOptBtn() {
        return optBtn;
    }

}
