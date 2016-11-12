// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.validation.OsmValidator;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.pt_assistant.actions.AddStopPositionAction;
import org.openstreetmap.josm.plugins.pt_assistant.actions.RepeatLastFixAction;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteSegment;
import org.openstreetmap.josm.plugins.pt_assistant.gui.PTAssistantPreferenceSetting;
import org.openstreetmap.josm.plugins.pt_assistant.validation.PTAssistantValidatorTest;

/**
 * This is the main class of the PTAssistant plugin.
 *
 * @author darya / Darya Golovko
 *
 */
public class PTAssistantPlugin extends Plugin {

    /*
     * last fix that was can be re-applied to all similar route segments, can be
     * null if unavailable
     */
    private static PTRouteSegment lastFix;

    /* item of the Tools menu for adding stop_positions */
    private JMenuItem addStopPositionMenu;

    /* item of the Tools menu for repeating the last fix */
    private static JMenuItem repeatLastFixMenu;

    /**
     * Main constructor.
     *
     * @param info
     *            Required information of the plugin. Obtained from the jar
     *            file.
     */
    public PTAssistantPlugin(PluginInformation info) {
        super(info);

        OsmValidator.addTest(PTAssistantValidatorTest.class);

        AddStopPositionAction addStopPositionAction = new AddStopPositionAction();
        addStopPositionMenu = MainMenu.add(Main.main.menu.toolsMenu, addStopPositionAction, false);
        RepeatLastFixAction repeatLastFixAction = new RepeatLastFixAction();
        repeatLastFixMenu = MainMenu.add(Main.main.menu.toolsMenu, repeatLastFixAction, false);

    }

    /**
     * Called when the JOSM map frame is created or destroyed.
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            addStopPositionMenu.setEnabled(true);
            repeatLastFixMenu.setEnabled(false);
        } else if (oldFrame != null && newFrame == null) {
            addStopPositionMenu.setEnabled(false);
            repeatLastFixMenu.setEnabled(false);
        }
    }

    /**
     * Sets up the pt_assistant tab in JOSM Preferences
     */
    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new PTAssistantPreferenceSetting();
    }

    public static PTRouteSegment getLastFix() {
        return lastFix;
    }

    /**
     * Remembers the last fix and enables/disables the Repeat last fix menu
     *
     * @param segment
     *            The last fix, call be null to disable the Repeat last fix menu
     */
    public static void setLastFix(PTRouteSegment segment) {
        lastFix = segment;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                repeatLastFixMenu.setEnabled(segment != null);
            }
        });
    }

    /**
     * Used in unit tests
     *
     * @param segment route segment
     */
    public static void setLastFixNoGui(PTRouteSegment segment) {
        lastFix = segment;
    }

}
