package com.tilusnet.josm.plugins.alignways;

import static org.openstreetmap.josm.tools.I18n.tr;

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

    private static AlignWaysMode awMode;
    private static IconToggleButton btn;
    private static JosmAction awAction;
    private static AlignWaysDialog awDialog;
    private static IconToggleButton optBtn;

    // The major version is e.g. used to decide when to trigger What's New windows
    public static final int AlignWaysMajorVersion = 2;

    public AlignWaysPlugin(PluginInformation info) {
        super(info);
        
        // Add the action entries to the Tools Menu
        Main.main.menu.toolsMenu.addSeparator();
        awAction = new AlignWaysAction();
        MainMenu.add(Main.main.menu.toolsMenu, awAction);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            // Construct the AlignWays mode toggle button
            awMode = new AlignWaysMode(Main.map, "alignways", tr("Align Ways mode"));
            btn = new IconToggleButton(awMode);
            btn.setVisible(true);
            newFrame.addMapMode(btn);
            optBtn = newFrame.addToggleDialog(awDialog = new AlignWaysDialog(awMode));
        } else {
            awDialog = null;
            optBtn = null;
            btn = null;
            awMode = null;
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
