/*
 * Indoorhelper is a JOSM plug-in to support users when creating their own indoor maps.
 *  Copyright (C) 2016  Erik Gruschka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ValidateAction;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.validation.OsmValidator;
import org.openstreetmap.josm.data.validation.tests.MapCSSTagChecker;
import org.openstreetmap.josm.gui.dialogs.FilterDialog;
import org.openstreetmap.josm.gui.dialogs.FilterTableModel;
import org.openstreetmap.josm.gui.mappaint.MapPaintStyles;

import model.IndoorHelperModel;
import model.TagCatalog.IndoorObject;
import views.FittingView;
import views.LevelSelectorView;
import views.ToolBoxView;

/**
 *
 * Class for the Controller which provides the communication between
 * the IndoorHelperModel and the different views.
 *
 * @author egru
 *
 */
public class IndoorHelperController {

    private IndoorHelperModel model;
    private ToolBoxView toolboxView;
    private FittingView fittingView;
    private LevelSelectorView selectorView;
    private String sep = System.getProperty("file.separator");


    private int lastLevelIndex;

    /**
     * Constructor for the {@link IndoorHelperController} which initiates model and views.
     *
     */
    public IndoorHelperController() {
        this.model = new IndoorHelperModel();
        this.toolboxView = new ToolBoxView();

        this.lastLevelIndex = 0;

        addToolboxListeners();
        Main.map.addToggleDialog(toolboxView);
    }

    /**
     * Adds the button- and box-listeners to the {@link ToolBoxView}.
     */
    private void addToolboxListeners() {

        if (this.toolboxView != null) {
            this.toolboxView.setPowerButtonListener(new ToolPowerButtonListener());
            this.toolboxView.setApplyButtonListener(new ToolApplyButtonListener());
            this.toolboxView.setLevelItemListener(new ToolLevelItemListener());
            this.toolboxView.setObjectItemListener(new ToolObjectItemListener());
            this.toolboxView.setPreset1Listener(new Preset1Listener());
            this.toolboxView.setPreset2Listener(new Preset2Listener());
            this.toolboxView.setPreset3Listener(new Preset3Listener());
            this.toolboxView.setPreset4Listener(new Preset4Listener());
        }
    }

    /**
     * Adds the button-listeners to the {@link LevelSelectorView}.
     */
    private void addLevelSelectorListeners() {
        if (this.selectorView != null) {
            this.selectorView.setOkButtonListener(new LevelOkButtonListener());
            this.selectorView.setCancelButtonListener(new LevelCancelButtonListener());
        }
    }

    /**
     * Adds the button-listeners to the {@link FittingView}.
     */
    private void addFittingListeners() {
        if (this.fittingView != null) {
            this.fittingView.setOkButtonListener(new FittingOkButtonListener());
        }
    }

    //********************************************************************
    //*********************   TOOLBOX LISTENERS   ************************
    //********************************************************************

    /**
     * The listener which handles the power button.
     *
     * @author egru
     *
     */
    class ToolPowerButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (toolboxView.getPowerButtonState()) {
                selectorView = new LevelSelectorView();
                addLevelSelectorListeners();
                selectorView.setVisible(true);
                setPluginPreferences(true);
            } else if (!toolboxView.getPowerButtonState()) {
                model = new IndoorHelperModel();
                selectorView.dispose();
                toolboxView.reset();
                setPluginPreferences(false);

                // Delete the indoor filters
                FilterDialog filterDialog = Main.map.getToggleDialog(FilterDialog.class);

                if (filterDialog != null) {
                    FilterTableModel filterTableModel = filterDialog.getFilterModel();

                    for (int i = filterTableModel.getRowCount()-1; i > -1; i--) {
                        if (filterTableModel.getFilter(i).text.startsWith("\"indoor:level\"=\"")) {
                            filterTableModel.removeFilter(i);
                        }
                    }
                }
            }
        }
    }

    /**
     * The listener which provides the handling of the apply button.
     * Gets the texts which were written by the user and writes them to the OSM-data.
     * After that it checks the tagged data  with the built-in validator file.
     *
     * @author egru
     */
    class ToolApplyButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            IndoorObject indoorObject = toolboxView.getSelectedObject();
            if (toolboxView.getNameText().isEmpty() && toolboxView.getRefText().isEmpty() && toolboxView.getLevelName().isEmpty()) {
                model.addTagsToOSM(indoorObject);
            } else {
                List<Tag> tags = new ArrayList<>();
                if (!toolboxView.getLevelName().isEmpty()) {
                    model.getLevelList().get(toolboxView.getSelectedLevelIndex()).setNameTag(toolboxView.getLevelName());
                }
                if (!toolboxView.getNameText().isEmpty()) {
                    tags.add(new Tag("name", toolboxView.getNameText()));
                }
                if (!toolboxView.getRefText().isEmpty()) {
                    tags.add(new Tag("ref", toolboxView.getRefText()));
                }
                model.addTagsToOSM(indoorObject, tags);
            }
            //Do the validation process
            ValidateAction validateAction = new ValidateAction();
            validateAction.doValidate(true);

            refreshPresets();
        }
    }

    /**
     * <pre>The listener which is called when a new item in the level list is selected.
     *It also sets the name-tag for a level, if the user has done an input in the textbox.
     * </pre>
     * @author egru
     *
     */
    class ToolLevelItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (!toolboxView.levelListIsEmpty()) {

                if (!toolboxView.getLevelName().isEmpty()) {
                    model.getLevelList().get(lastLevelIndex).setNameTag(toolboxView.getLevelName());
                }

                if (!model.getLevelList().get(toolboxView.getSelectedLevelIndex()).hasEmptyName()) {
                    toolboxView.setLevelName(model.getLevelList().get(toolboxView.getSelectedLevelIndex()).getName());
                } else {
                    toolboxView.setLevelName("");
                }
                model.setWorkingLevel(toolboxView.getSelectedLevelIndex());

                lastLevelIndex = toolboxView.getSelectedLevelIndex();
            }
        }
    }



    /**
     * The listener which is called when a new item in the object list is selected.
     *
     * @author egru
     *
     */
    class ToolObjectItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (toolboxView.getSelectedObject().equals(IndoorObject.ROOM)) {
                toolboxView.setTagUiElementsEnabled(true);
            } else {
                toolboxView.setTagUiElementsEnabled(false);
            }
        }
    }

    /**
     * Listener for preset button 1.
     * @author egru
     *
     */
    class Preset1Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            model.addTagsToOSM(toolboxView.getPreset1());

        }
    }

    /**
     * Listener for preset button 2.
     * @author egru
     *
     */
    class Preset2Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            model.addTagsToOSM(toolboxView.getPreset2());

        }

    }

    /**
     * Listener for preset button 3.
     * @author egru
     *
     */
    class Preset3Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            model.addTagsToOSM(toolboxView.getPreset3());

        }

    }

    /**
     * Listener for preset button 4.
     * @author egru
     *
     */
    class Preset4Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            model.addTagsToOSM(toolboxView.getPreset4());

        }

    }

    /**
     * Updates the preset button from the current ranking.
     */
    private void refreshPresets() {
        toolboxView.setPresetButtons(model.getPresetRanking());
    }


    //*******************
    // SELECTOR LISTENERS
    //*******************

    /**
     * <pre>
     * The listener which handles the click on the OK-button of the {@link LevelSelectorView}.
     * It sends the data of the view to the model and displays an error message,
     * if the level-list couldn't be created.
     * </pre>
     * @author egru
     *
     */
    class LevelOkButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean levelSuccess = model.setBuildingLevels(selectorView.getMin(), selectorView.getMax());

            if (levelSuccess) {
                toolboxView.setLevelList(model.getLevelList());                //set the levels to the ComboBox and
                model.setWorkingLevel(toolboxView.getSelectedLevelIndex());        //sets the working level in the model

                selectorView.dispose();

                fittingView = new FittingView();
                addFittingListeners();
                fittingView.setVisible(true);
            } else {

                JOptionPane.showMessageDialog(null, "Lowest Level has to be lower than the highest level",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Closes the level selection view if the user hits the cancel button.
     *
     * @author egru
     *
     */
    class LevelCancelButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            selectorView.dispose();
            toolboxView.setPowerButtonDisabled();
            setPluginPreferences(false);
        }

    }



    //*******************
    // FITTING LISTENERS
    //*******************
    /**
     * Closes the {@link FittingView} if the OK-Button is clicked.
     * Enables the UI elements of the toolbox
     *
     * @author egru
     *
     */
    class FittingOkButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            fittingView.dispose();
            toolboxView.setAllUiElementsEnabled(true);
            toolboxView.setTagUiElementsEnabled(false);
        }

    }

    /*
    HELPER METHODS
    */

    /**
     * Enables or disables the preferences for the mapcss-style and the validator.
     *
     * @param enabled Activates or disables the settings.
     */
    private void setPluginPreferences(boolean enabled) {
        Collection<Map<String, String>> validatorMaps = 
                Main.pref.getListOfStructs("validator.org.openstreetmap.josm.data.validation.tests.MapCSSTagChecker.entries",
                new ArrayList<>());
        Collection<Map<String, String>> styleMaps = 
                Main.pref.getListOfStructs("mappaint.style.entries", new ArrayList<>());

        if (enabled) {
            //set the validator active

            List<Map<String, String>> validatorMapsNew = new ArrayList<>();
            if (!validatorMaps.isEmpty()) {
                validatorMapsNew.addAll(validatorMaps);
            }

            for (Map<String, String> map : validatorMapsNew) {
                if (map.containsValue("Indoor")) {
                    validatorMapsNew.remove(map);
                    break;
                }
            }

            Map<String, String> indoorValidator = new HashMap<>();
            indoorValidator.put("title", "Indoor");
            indoorValidator.put("active", "true");
            indoorValidator.put("url", Main.pref.getUserDataDirectory()+ sep +"validator" +
                    sep + "indoorhelper.validator.mapcss");

            validatorMapsNew.add(indoorValidator);
            Main.pref.putListOfStructs("validator.org.openstreetmap.josm.data.validation.tests.MapCSSTagChecker.entries",
                    validatorMapsNew);

            //set mappaint active

            List<Map<String, String>> styleMapsNew = new ArrayList<>();
            if (!styleMaps.isEmpty()) {
                styleMapsNew.addAll(styleMaps);
            }

            for (Map<String, String> map : styleMapsNew) {
                if (map.containsValue("Indoor")) {
                    styleMapsNew.remove(map);
                    break;
                }
            }
            Map<String, String> indoorMapPaint = new HashMap<>();
            indoorMapPaint.put("title", "Indoor");
            indoorMapPaint.put("active", "true");
            indoorMapPaint.put("url", Main.pref.getUserDataDirectory() + sep + "styles"
                    + sep + "indoor.mapcss");
            styleMapsNew.add(indoorMapPaint);
            Main.pref.putListOfStructs("mappaint.style.entries", styleMapsNew);

            updateSettings();
        } else {
            //set the validator inactive


            List<Map<String, String>> validatorMapsNew = new ArrayList<>();
            if (!validatorMaps.isEmpty()) {
                validatorMapsNew.addAll(validatorMaps);
            }

            for (Map<String, String> map : validatorMapsNew) {
                if (map.containsValue("Indoor")) {
                    validatorMapsNew.remove(map);
                    break;
                }
            }
            Map<String, String> indoorValidator = new HashMap<>();
            indoorValidator.put("title", "Indoor");
            indoorValidator.put("active", "false");
            indoorValidator.put("url", Main.pref.getUserDataDirectory()+ sep +"validator" +
                    sep + "indoorhelper.validator.mapcss");

            validatorMapsNew.add(indoorValidator);
            Main.pref.putListOfStructs("validator.org.openstreetmap.josm.data.validation.tests.MapCSSTagChecker.entries",
                    validatorMapsNew);


            //set mappaint inactive


            List<Map<String, String>> styleMapsNew = new ArrayList<>();
            if (!styleMaps.isEmpty()) {
                styleMapsNew.addAll(styleMaps);
            }
            for (Map<String, String> map : styleMapsNew) {
                if (map.containsValue("Indoor")) {
                    styleMapsNew.remove(map);
                    break;
                }
            }
            Map<String, String> indoorMapPaint = new HashMap<>();
            indoorMapPaint.put("title", "Indoor");
            indoorMapPaint.put("active", "false");
            indoorMapPaint.put("url", Main.pref.getUserDataDirectory() + sep + "styles"
                    + sep + "indoor.mapcss");
            styleMapsNew.add(indoorMapPaint);
            Main.pref.putListOfStructs("mappaint.style.entries", styleMapsNew);

            updateSettings();
        }
    }

    /**
     * Forces JOSM to load the validator and mappaint settings.
     */
    private void updateSettings() {
        Main.pref.init(false);
        MapCSSTagChecker tagChecker = OsmValidator.getTest(MapCSSTagChecker.class);
            if (tagChecker != null) {
                OsmValidator.initializeTests(Collections.singleton(tagChecker));
            }

            MapPaintStyles.readFromPreferences();
    }
}


