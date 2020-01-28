// License: GPL. For details, see LICENSE file.
package controller;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.ValidateAction;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.help.HelpBrowser;
import org.openstreetmap.josm.gui.mappaint.MapPaintStyles;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.spi.preferences.MapListSetting;
import org.openstreetmap.josm.spi.preferences.Setting;
import org.openstreetmap.josm.tools.Shortcut;

import model.IndoorHelperModel;
import model.IndoorLevel;
import model.TagCatalog.IndoorObject;
import views.LevelSelectorView;
import views.ToolBoxView;

/**
 *
 * Class for the Controller which provides the communication between the
 * IndoorHelperModel and the different views.
 *
 * @author egru
 * @author rebsc
 */
public class IndoorHelperController {

    private final IndoorHelperModel model = new IndoorHelperModel();
    private ToolBoxView toolboxView;
    private String levelValue, levelNum;
    private final SpaceAction spaceAction = new SpaceAction();
    private Shortcut spaceShortcut;
    private final EnterAction enterAction = new EnterAction();
    private Shortcut enterShortcut;
    private boolean outerHelp, innerHelp, levelHelp;
    private Collection<OsmPrimitive> innerRelation;
    private LevelSelectorView selectorView;

    /**
     * The listener which provides the handling of the applyButton.
     * Gets the texts which were written by the user and writes them to the OSM-data.
     * After that it checks the tagged data.
     *
     * @author egru
     * @author rebsc
     */
    private final ActionListener toolApplyButtonListener = e -> {

        IndoorObject indoorObject = toolboxView.getSelectedObject();

        // collecting all tags
        List<Tag> tags = new ArrayList<>();
        if (!toolboxView.getLevelCheckBoxStatus() && !levelValue.equals("")) {
            tags.add(new Tag("level", levelValue));
        }
        if (!toolboxView.getLevelNameText().isEmpty() && !toolboxView.getLevelCheckBoxStatus()) {
            tags.add(new Tag("level_name", toolboxView.getLevelNameText()));
        }
        if (!toolboxView.getNameText().isEmpty()) {
            tags.add(new Tag("name", toolboxView.getNameText()));
        }
        if (!toolboxView.getRefText().isEmpty()) {
            tags.add(new Tag("ref", toolboxView.getRefText()));
        }
        if (!toolboxView.getRepeatOnText().isEmpty()) {
            tags.add(new Tag("repeat_on", toolboxView.getRepeatOnText()));
        }
        if (!toolboxView.getLevelNameText().isEmpty() && !toolboxView.getLevelCheckBoxStatus()) {
            tags.add(new Tag("level_name", toolboxView.getLevelNameText()));
        }

        // Tagging to OSM Data
        model.addTagsToOSM(indoorObject, tags);

        // Reset UI elements
        toolboxView.resetUiElements();

        // Do the validation process
        new ValidateAction().doValidate(true);

        refreshPresets();
    };

    /**
     * The listener which is called when a new item in the object list is selected.
     *
     * @author egru
     * @author rebsc
     */
    private final ItemListener toolObjectItemListener = e -> {
        if (toolboxView.getSelectedObject().equals(IndoorObject.ROOM)) {
            toolboxView.setNRUiElementsEnabled(true);
            toolboxView.setROUiElementsEnabled(false);
        } else if (toolboxView.getSelectedObject().equals(IndoorObject.STEPS)
                || toolboxView.getSelectedObject().equals(IndoorObject.ELEVATOR)) {
            toolboxView.setROUiElementsEnabled(true);
            toolboxView.setNRUiElementsEnabled(true);
        } else {
            toolboxView.setROUiElementsEnabled(false);
        }
    };

    /**
     * The listener which is called when the LevelCheckBox is selected.
     *
     * @author rebsc
     */
    private final ItemListener toolLevelCheckBoxListener = e -> toolboxView
            .setLVLUiElementsEnabled(e.getStateChange() != ItemEvent.SELECTED);

    /**
     * The listener which is called when the helpButton got pushed.
     *
     * @author rebsc
     */
    private final ActionListener toolHelpButtonListener = e -> HelpBrowser.setUrlForHelpTopic("Plugin/IndoorHelper");

    /**
     * The listener which is called when the addLevelButton got pushed.
     *
     * @author rebsc
     */
    private final ActionListener toolAddLevelButtonListener = e -> {
        if (selectorView == null) {
            selectorView = new LevelSelectorView();
            addLevelSelectorListeners();

            // Show LevelSelectorView
            selectorView.setVisible(true);
        } else {
            // Put focus back on LevelSelectorView
            selectorView.toFront();
        }
    };

    /**
     * The listener which is called when the MultiCheckBox is selected.
     *
     * @author rebsc
     */
    private final ItemListener toolMultiCheckBoxListener = e -> toolboxView
            .setMultiUiElementsEnabled(e.getStateChange() != ItemEvent.SELECTED);

    /**
     * The listener which is called when the OUTER Button got pushed.
     *
     * @author rebsc
     */
    private final ActionListener toolOuterButtonListener = e -> {
        // Select drawing action
        MainApplication.getMap().selectDrawTool(false);

        // For space shortcut to add the relation after spacebar got pushed {@link SpaceAction}
        outerHelp = true;
        innerHelp = false;
    };

    /**
     * The listener which is called when the INNER Button got pushed.
     *
     * @author rebsc
     */
    private final ActionListener toolInnerButtonListener = e -> {
        // Select drawing action
        MainApplication.getMap().selectDrawTool(false);

        // For space shortcut to edit the relation after enter got pushed {@link SpaceAction}{@link EnterAction}
        innerHelp = true;
        outerHelp = false;
    };

    /**
     * Listener for preset button 1.
     *
     * @author egru
     */
    private final ActionListener preset1Listener = e -> model.addTagsToOSM(toolboxView.getPreset1());

    /**
     * Listener for preset button 2.
     *
     * @author egru
     */
    private final ActionListener preset2Listener = e -> model.addTagsToOSM(toolboxView.getPreset2());

    /**
     * Listener for preset button 3.
     *
     * @author egru
     */
    private final ActionListener preset3Listener = e -> model.addTagsToOSM(toolboxView.getPreset3());

    /**
     * Listener for preset button 4.
     *
     * @author egru
     */
    private final ActionListener preset4Listener = e -> model.addTagsToOSM(toolboxView.getPreset4());

    /**
     * Updates the preset button from the current ranking.
     */
    private void refreshPresets() {
        toolboxView.setPresetButtons(model.getPresetRanking());
    }

    /**
     * Specific listener for the applyButton
     *
     * @author rebsc
     */
    private final ActionListener toolLevelOkButtonListener = e -> {
        levelHelp = true;

        // Get insert level number out of SelectorView
        if (!selectorView.getLevelNumber().equals("")) {
            levelNum = selectorView.getLevelNumber();

            // Unset visibility
            selectorView.dispose();
            // Select draw-action
            MainApplication.getMap().selectDrawTool(false);

        } else {
            JOptionPane.showMessageDialog(null, tr("Please insert a value."), tr("Error"), JOptionPane.ERROR_MESSAGE);
        }

        selectorView = null;
    };

    /**
     * Specific listener for the cancelButton
     *
     * @author rebsc
     */
    private final ActionListener toolLevelCancelButtonListener = e -> {
        selectorView.dispose();
        selectorView = null;
    };

    /**
     * General listener for LevelSelectorView window
     *
     * @author rebsc
     */
    class ToolSelectorWindowSListener extends WindowAdapter {

        @Override
        public void windowClosed(WindowEvent e) {
            selectorView = null;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            selectorView = null;
        }
    }

    /**
     * Constructor for the {@link IndoorHelperController} which initiates model and views.
     */
    public IndoorHelperController() {

        toolboxView = new ToolBoxView();

        setPluginPreferences(true);

        // Ui elements
        toolboxView.setAllUiElementsEnabled(true);
        toolboxView.setROUiElementsEnabled(false);

        addToolboxListeners();
        MainApplication.getMap().addToggleDialog(toolboxView);

        // Shortcuts
        spaceShortcut = Shortcut.registerShortcut("mapmode:space", "ConfirmObjectDrawing", KeyEvent.VK_SPACE, Shortcut.DIRECT);
        MainApplication.registerActionShortcut(spaceAction, spaceShortcut);

        enterShortcut = Shortcut.registerShortcut("mapmode:enter", "ConfirmMultipolygonSelection", KeyEvent.VK_ENTER, Shortcut.DIRECT);
        MainApplication.registerActionShortcut(enterAction, enterShortcut);

        // Helper
        outerHelp = false;
        innerHelp = false;
        levelHelp = false;
        innerRelation = null;
        levelValue = "";
        levelNum = "";
    }

    /**
     * Adds the button- and box-listeners to the {@link ToolBoxView}.
     */
    private void addToolboxListeners() {
        if (toolboxView != null) {
            toolboxView.setApplyButtonListener(toolApplyButtonListener);
            toolboxView.setLevelCheckBoxListener(toolLevelCheckBoxListener);
            toolboxView.setHelpButtonListener(toolHelpButtonListener);
            toolboxView.setAddLevelButtonListener(toolAddLevelButtonListener);
            toolboxView.setObjectItemListener(toolObjectItemListener);
            toolboxView.setOuterButtonListener(toolOuterButtonListener);
            toolboxView.setInnerButtonListener(toolInnerButtonListener);
            toolboxView.setMultiCheckBoxListener(toolMultiCheckBoxListener);
            toolboxView.setPreset1Listener(preset1Listener);
            toolboxView.setPreset2Listener(preset2Listener);
            toolboxView.setPreset3Listener(preset3Listener);
            toolboxView.setPreset4Listener(preset4Listener);
        }
    }

    /**
     * Adds the button-listeners to the {@link LevelSelectorView}.
     */
    private void addLevelSelectorListeners() {
        if (selectorView != null) {
            selectorView.setOkButtonListener(toolLevelOkButtonListener);
            selectorView.setCancelButtonListener(toolLevelCancelButtonListener);
            selectorView.setSelectorWindowListener(new ToolSelectorWindowSListener());
        }
    }

    /**
     * Shortcut for spacebar
     *
     * @author rebsc
     */
    private class SpaceAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (outerHelp) {

                // Create new relation and add the currently drawn object to it
                model.addRelation("outer");
                MainApplication.getMap().selectSelectTool(false);
                outerHelp = false;

                // Clear currently selection
                MainApplication.getLayerManager().getEditDataSet().clearSelection();
            } else if (innerHelp) {

                // Save new drawn relation for adding
                innerRelation = MainApplication.getLayerManager().getEditDataSet().getAllSelected();
                MainApplication.getMap().selectSelectTool(false);

                // Clear currently selection
                MainApplication.getLayerManager().getEditDataSet().clearSelection();
            } else if (levelHelp) {

                List<Tag> tags = new ArrayList<>();
                tags.add(new Tag("level", levelNum));

                // Add level tag
                model.addTagsToOSM(tags);

                // Change action
                MainApplication.getMap().selectSelectTool(false);
                levelHelp = false;
            }
        }
    }

    /**
     * Shortcut for enter
     *
     * @author rebsc
     */
    private class EnterAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (innerHelp && !outerHelp) {
                // Edit the new drawn relation member to selected relation
                model.editRelation("inner", innerRelation);
                innerHelp = false;
            } else if ((innerHelp && outerHelp) || (outerHelp && !innerHelp)) {
                JOptionPane.showMessageDialog(null,
                        tr("Please press spacebar first to add \"outer\" object to relation."), tr("Relation-Error"),
                        JOptionPane.ERROR_MESSAGE);
                innerHelp = false;
                outerHelp = false;
            }
        }
    }

    /**
     * Function which unsets the disabled state of currently hidden and/or disabled objects which have a
     * specific tag (key). Just unsets the disabled state if object has a tag-value which is part of the
     * current working level.
     *
     * @author rebsc
     * @param key specific key to unset hidden objects which contains it
     */
    public void unsetSpecificKeyFilter(String key) {

        DataSet editDataSet = OsmDataManager.getInstance().getEditDataSet();
        if (editDataSet != null) {
            Collection<OsmPrimitive> p = editDataSet.allPrimitives();
            int level = Integer.parseInt(levelValue);

            // Find all primitives with the specific tag and check if value is part of the current
            // workinglevel. After that unset the disabled status.
            for (OsmPrimitive osm : p) {
                if ((osm.isDisabledAndHidden() || osm.isDisabled()) && osm.hasKey(key)) {
                    for (Map.Entry<String, String> e : osm.getInterestingTags().entrySet()) {
                        if (e.getKey().equals(key)) {
                            // Compare values to current working level
                            if (IndoorLevel.isPartOfWorkingLevel(e.getValue(), level)) {
                                osm.unsetDisabledState();
                            } else {
                                osm.setDisabledState(true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Function which updates the current working level tag
     *
     * @param indoorLevel current working level
     */
    public void setIndoorLevel(String indoorLevel) {
        this.toolboxView.setLevelLabel(indoorLevel);
    }

    /**
     * Function which gets the current working level tag
     *
     * @param indoorLevel current working level
     */
    public void getIndoorLevel(String indoorLevel) {
        levelValue = indoorLevel;
    }

    /**
     * Forces JOSM to load the mappaint settings.
     */
    private static void updateSettings() {
        Preferences.main().init(false);
        MapPaintStyles.readFromPreferences();
    }

    /**
     * Enables or disables the preferences for the mapcss-style.
     *
     * @param enabled Activates or disables the settings.
     */
    private static void setPluginPreferences(boolean enabled) {
        Map<String, Setting<?>> settings = Preferences.main().getAllSettings();
        String sep = System.getProperty("file.separator");

        MapListSetting styleMapListSetting = (MapListSetting) settings.get("mappaint.style.entries");
        List<Map<String, String>> styleMaps = new ArrayList<>();
        if (styleMapListSetting != null) {
            styleMaps = styleMapListSetting.getValue();
        }

        List<Map<String, String>> styleMapsNew = new ArrayList<>();
        if (!styleMaps.isEmpty()) {
            styleMapsNew.addAll(styleMaps);
        }
        for (Map<String, String> map : styleMapsNew) {
            if (map.containsValue(tr("Indoor"))) {
                styleMapsNew.remove(map);
                break;
            }
        }
        Map<String, String> indoorMapPaint = new HashMap<>();
        indoorMapPaint.put("title", tr("Indoor"));
        indoorMapPaint.put("active", Boolean.toString(enabled));
        indoorMapPaint.put("url", Config.getDirs().getUserDataDirectory(true) + sep + "styles" + sep + "sit.mapcss");
        styleMapsNew.add(indoorMapPaint);
        Config.getPref().putListOfMaps("mappaint.style.entries", styleMapsNew);
        updateSettings();
    }
}
