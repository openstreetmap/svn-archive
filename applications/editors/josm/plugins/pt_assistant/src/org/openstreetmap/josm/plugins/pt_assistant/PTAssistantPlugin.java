// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant;

import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.validation.OsmValidator;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.pt_assistant.actions.AddStopPositionAction;
import org.openstreetmap.josm.plugins.pt_assistant.actions.CreatePlatformNodeAction;
import org.openstreetmap.josm.plugins.pt_assistant.actions.EdgeSelectionAction;
import org.openstreetmap.josm.plugins.pt_assistant.actions.DoubleSplitAction;
import org.openstreetmap.josm.plugins.pt_assistant.actions.EditHighlightedRelationsAction;
import org.openstreetmap.josm.plugins.pt_assistant.actions.RepeatLastFixAction;
import org.openstreetmap.josm.plugins.pt_assistant.actions.SortPTRouteMembersAction;
import org.openstreetmap.josm.plugins.pt_assistant.actions.SplitRoundaboutAction;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTRouteSegment;
import org.openstreetmap.josm.plugins.pt_assistant.gui.PTAssistantLayerManager;
import org.openstreetmap.josm.plugins.pt_assistant.gui.PTAssistantPreferenceSetting;
import org.openstreetmap.josm.plugins.pt_assistant.validation.BicycleFootRouteValidatorTest;
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

    /* list of relation currently highlighted by the layer */
    private static List<Relation> highlightedRelations = new ArrayList<>();

    /* item of the Tools menu for repeating the last fix */
    private static JMenuItem repeatLastFixMenu;

    /* edit the currently highlighted relations */
    private static JMenuItem editHighlightedRelationsMenu;

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
        OsmValidator.addTest(BicycleFootRouteValidatorTest.class);

        DataSet.addSelectionListener(PTAssistantLayerManager.PTLM);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(PTAssistantLayerManager.PTLM);
        RepeatLastFixAction repeatLastFixAction = new RepeatLastFixAction();
        EditHighlightedRelationsAction editHighlightedRelationsAction = new EditHighlightedRelationsAction();
        repeatLastFixMenu = MainMenu.add(MainApplication.getMenu().toolsMenu, repeatLastFixAction);
        editHighlightedRelationsMenu = MainMenu.add(MainApplication.getMenu().toolsMenu, editHighlightedRelationsAction);
        MainMenu.add(MainApplication.getMenu().toolsMenu, new SplitRoundaboutAction());
        MainMenu.add(MainApplication.getMenu().toolsMenu, new SortPTRouteMembersAction());
        MainMenu.add(MainApplication.getMenu().toolsMenu, new CreatePlatformNodeAction());
    }

    /**
     * Called when the JOSM map frame is created or destroyed.
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            repeatLastFixMenu.setEnabled(false);
            editHighlightedRelationsMenu.setEnabled(false);
            MainApplication.getMap().addMapMode(new IconToggleButton(new AddStopPositionAction()));
            MainApplication.getMap().addMapMode(new IconToggleButton(new EdgeSelectionAction()));
            MainApplication.getMap().addMapMode(new IconToggleButton(new DoubleSplitAction()));
        } else if (oldFrame != null && newFrame == null) {
            repeatLastFixMenu.setEnabled(false);
            editHighlightedRelationsMenu.setEnabled(false);
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
        SwingUtilities.invokeLater(() ->
        repeatLastFixMenu.setEnabled(segment != null));
    }

    /**
     * Used in unit tests
     *
     * @param segment route segment
     */
    public static void setLastFixNoGui(PTRouteSegment segment) {
        lastFix = segment;
    }

    public static List<Relation> getHighlightedRelations() {
        return new ArrayList<>(highlightedRelations);
    }

    public static void addHighlightedRelation(Relation highlightedRelation) {
        highlightedRelations.add(highlightedRelation);
        if (!editHighlightedRelationsMenu.isEnabled()) {
            SwingUtilities.invokeLater(() ->
            editHighlightedRelationsMenu.setEnabled(true));
        }
    }

    public static void clearHighlightedRelations() {
        highlightedRelations.clear();
        SwingUtilities.invokeLater(() ->
        editHighlightedRelationsMenu.setEnabled(false));
    }
}
