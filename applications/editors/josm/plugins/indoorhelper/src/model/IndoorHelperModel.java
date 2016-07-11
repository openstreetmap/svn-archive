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

package model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Filter;
import org.openstreetmap.josm.data.osm.Filter.FilterPreferenceEntry;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.gui.dialogs.FilterDialog;
import org.openstreetmap.josm.gui.dialogs.FilterTableModel;

import model.TagCatalog.IndoorObject;

/**
 * Class for the data model which includes indoor data and
 * the functions to handle the plug-in
 *
 * @author egru
 */
public class IndoorHelperModel {

    private java.util.List<IndoorLevel> levelList;
    private int workingLevel;
    private int workingIndex;
    private TagCatalog tags;
    private PresetCounter counter;

    /**
     * Constructor for the {@link IndoorHelperModel} which sets the current
     * workingLevel to 0 and creates the {@link TagCatalog}.
     */
    public IndoorHelperModel() {
        this.workingLevel = 0;
        this.levelList = new ArrayList<>();
        this.tags = new TagCatalog();
        this.counter = new PresetCounter();
    }

    /**
     * Method to create a list of levels for the current building.
     * It also creates the filters which are needed to execute the indoor mapping.
     * minLevel should be lower than maxLevel or the same.
     *
     * @param minLevel the lowest level of the building
     * @param maxLevel the highest level of the building
     * @return boolean which indicates if the creation of the levelList was successful
     */
    public boolean setBuildingLevels(int minLevel, int maxLevel) {

        if (minLevel < maxLevel) {

            for (int i = minLevel; i <= maxLevel; i++) {

                IndoorLevel level = new IndoorLevel(i);
                levelList.add(level);

                // Get the filter dialog
                FilterDialog filterDialog = Main.map.getToggleDialog(FilterDialog.class);

                if (filterDialog != null) {
                    // Create a new filter
                    //Filter filter = new Filter("\"indoor:level\"=\""+i+"\"", SearchMode.add, false, false, false);
                    FilterPreferenceEntry entry = new FilterPreferenceEntry();
                    entry.case_sensitive = false;
                    entry.enable = false;
                    entry.hiding = false;
                    entry.inverted = false;
                    entry.mapCSS_search = false;
                    entry.mode = "add";
                    entry.text = "\"indoor:level\"=\""+i+"\"";
                    Filter filter = new Filter(entry);

                    FilterTableModel filterTableModel = filterDialog.getFilterModel();

                    boolean exists = false;

                    // Search if the filter exists already.
                    for (Filter listFilter : filterTableModel.getFilters()) {
                        if (listFilter.equals(filter)) {
                            exists = true;
                        }
                    }

                    // Only add the filter if it is not already in the filter dialog.
                    if (exists == false) {
                        filterTableModel.addFilter(filter);
                    }

                } else {
                    //Show error message if filter dialog is null.
                    JOptionPane.showMessageDialog(null, "Filter Dialog is null.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            return true;

        } else if (minLevel == maxLevel) {

            IndoorLevel level = new IndoorLevel(minLevel);
            levelList.add(level);

            // Get the filter dialog
            FilterDialog filterDialog = Main.map.getToggleDialog(FilterDialog.class);

            if (filterDialog != null) {
                // Create a new filter
                //Filter filter = new Filter("\"indoor:level\"=\""+minLevel+"\"", SearchMode.add, false, false, false);

                FilterPreferenceEntry entry = new FilterPreferenceEntry();
                entry.case_sensitive = false;
                entry.enable = false;
                entry.hiding = false;
                entry.inverted = false;
                entry.mapCSS_search = false;
                entry.mode = "add";
                entry.text = "\"indoor:level\"=\""+minLevel+"\"";
                Filter filter = new Filter(entry);

                FilterTableModel filterTableModel = filterDialog.getFilterModel();

                boolean exists = false;

                // Search if the filter exists already.
                for (Filter listFilter : filterTableModel.getFilters()) {
                    if (listFilter.equals(filter)) {
                        exists = true;
                    }
                }

                // Only add the filter if it is not already in the filter dialog.
                if (exists == false) {
                    filterTableModel.addFilter(filter);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Filter Dialog is null.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            return true;
        }

        return false;
    }

    /**
     * Getter for the levelList of the model.
     *
     * @return the levelList, or null if no levelList was created yet
     */
    public java.util.List<IndoorLevel> getLevelList() {
        return this.levelList;
    }

    /**
     * Function to set the level the user wants to work on (with the level index) and activates the corresponding filter.
     *
     * @param index the index of the level the user wants to work on
     */
    public void setWorkingLevel(int index) {
        this.workingIndex = index;
        this.workingLevel = this.getLevelNumberFromIndex(index);

        FilterDialog filterDialog = Main.map.getToggleDialog(FilterDialog.class);
        FilterTableModel filterTableModel = filterDialog.getFilterModel();


        for (Filter filter : filterTableModel.getFilters()) {
            // disable the filter for the current level
            if (filter.text.equals("\"indoor:level\"=\""+workingLevel+"\"")) {
                filterTableModel.setValueAt(false, filterTableModel.getFilters().indexOf(filter), FilterTableModel.COL_ENABLED);
                filterTableModel.setValueAt(false, filterTableModel.getFilters().indexOf(filter), FilterTableModel.COL_HIDING);
            } else if (filter.text.startsWith("\"indoor:level\"=\"")) {
                filterTableModel.setValueAt(true, filterTableModel.getFilters().indexOf(filter), FilterTableModel.COL_ENABLED);
                filterTableModel.setValueAt(true, filterTableModel.getFilters().indexOf(filter), FilterTableModel.COL_HIDING);
            }
        }
    }

    /**
     * Function to get the current working level of the plug-in
     *
     * @return {@link Integer} which represents the current working level
     */
    public int getWorkingLevel() {
        return this.workingLevel;
    }

    /**
     * Method to get the index of the current working level of the plug-in.
     *
     * @return {@link Integer} which represents the index
     */
    public int getWorkingIndex() {
        return this.workingIndex;
    }

    /**
     * Returns the level number which is corresponding to a specific index.
     *
     * @param index index of the level
     * @return a level number as an {@link Integer}
     */
    public int getLevelNumberFromIndex(int index) {
        return levelList.get(index).getLevelNumber();
    }

    /**
     * Function to set the nameTag of a specific level.
     *
     * @param levelNumber number of the level
     * @param levelName tag which the user wants to set
     * @return boolean which indicates if the level was found in the levelList
     */
    public void setLevelName(int levelIndex, String levelName) {
        if ((levelName.length() > 0) && (levelName != null)) {
            levelList.get(levelIndex).setNameTag(levelName);
        }
    }

    /**
     * Function to get a tag-set out of the {@link TagCatalog}.
     *
     * @param object the {@link IndoorObject} from which you want to get the tag-set
     * @return a {@link List} of {@link Tag}s
     */
    public List<Tag> getObjectTags(TagCatalog.IndoorObject object) {
        return this.tags.getTags(object);
    }


    /**
     * Method which adds the selected tag-set to the currently selected OSM data.
     * It also adds the level tag corresponding to the current working level.
     *
     * @param object the object which defines the tag-set you want to add
     * @param userTags the tags which are given by the user input
     */
    public void addTagsToOSM(IndoorObject object, List<Tag> userTags) {
        if (!Main.getLayerManager().getEditDataSet().selectionEmpty() && !Main.main.getInProgressSelection().isEmpty()) {

            List<Tag> tags = this.getObjectTags(object);
            tags.addAll(userTags);
            tags.add(new Tag("indoor:level", Integer.toString(workingLevel)));

            if (!this.getLevelList().get(workingIndex).hasEmptyName()) {
                tags.add(this.getLevelList().get(workingIndex).getNameTag());
            }

            // Increment the counter for the presets
            this.counter.count(object);

            //Add the tags to the current selection
            for (Tag t : tags) {
                Main.main.undoRedo.add(new ChangePropertyCommand(Main.main.getInProgressSelection(), t.getKey(), t.getValue()));
            }

        } else if (Main.getLayerManager().getEditDataSet().selectionEmpty()) {

            JOptionPane.showMessageDialog(null, "No data selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Method which adds the selected tag-set to the currently selected OSM data.
     * It also adds the level tag corresponding to the current working level.
     *
     * @param object the object which defines the tag-set you want to add
     */
    public void addTagsToOSM(IndoorObject object) {

        if (!Main.getLayerManager().getEditDataSet().selectionEmpty() && !Main.main.getInProgressSelection().isEmpty()) {
            List<Tag> tags = this.getObjectTags(object);
            tags.add(new Tag("indoor:level", Integer.toString(workingLevel)));

            // Increment the counter for the presets
            this.counter.count(object);

            //Add the tags to the current selection
            for (Tag t : tags) {
                Main.main.undoRedo.add(new ChangePropertyCommand(Main.main.getInProgressSelection(), t.getKey(), t.getValue()));
            }
        } else if (Main.getLayerManager().getEditDataSet().selectionEmpty()) {
            JOptionPane.showMessageDialog(null, "No data selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Returns the current ranking of the preset counter, which includes the 4 most used items.
     *
     * @return a list of the 4 most used IndoorObjects
     */
    public List<IndoorObject> getPresetRanking() {
        return counter.getRanking();
    }
}
