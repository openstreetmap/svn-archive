/*
 * Indoorhelper is a JOSM plug-in to support users when creating their own indoor maps.
 *  Copyright (C) 2016  Erik Gruschka
 *  Copyright (C) 2018  Rebecca Schmidt
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

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.gui.MainApplication;

import model.TagCatalog.IndoorObject;

/**
 * Class for the data model which includes indoor data and
 * the functions to handle the plug-in
 *
 * @author egru
 * @author rebsc
 */
public class IndoorHelperModel {

    private TagCatalog tags;
    private PresetCounter counter;

    /**
     * Constructor for the {@link IndoorHelperModel} which creates the {@link TagCatalog}
     * and {@link PresetCounter}.
     */
    public IndoorHelperModel() {
        this.tags = new TagCatalog();
        this.counter = new PresetCounter();
    }

    /**
     * Function to get a tag-set out of the {@link TagCatalog}.
     * ClipboardUtils.copy(Main.getLayerManager().getEditDataSet(),Main.getLayerManager().getEditDataSet().getKey());
     * @param object the {@link IndoorObject} from which you want to get the tag-set
     * @return a {@link List} of {@link Tag}s
     */
    public List<Tag> getObjectTags(TagCatalog.IndoorObject object) {
        return this.tags.getTags(object);
    }

    /**
     * Method which adds the selected tag-set to the currently selected OSM data. If OSM data is a relation add tag-set
     * directly to the relation otherwise add it to nodes and/or ways.
     *
     * @author rebsc
     * @param object the object which defines the tag-set you want to add
     * @param userTags the tags which are given by the user input
     */
    public void addTagsToOSM(IndoorObject object, List<Tag> userTags) {
        if (!MainApplication.getLayerManager().getEditDataSet().selectionEmpty() &&
                !OsmDataManager.getInstance().getInProgressSelection().isEmpty()) {

            DataSet ds = OsmDataManager.getInstance().getEditDataSet();
            List<Tag> tags = this.getObjectTags(object);
            Collection<Relation> relations = ds.getRelations();
            Relation relationToAdd = null;

            tags.addAll(userTags);

            // Increment the counter for the presets
            this.counter.count(object);

            // Put value on {@link relationToAdd} if selected object is a relation.
            relationToAdd = getRelationFromDataSet(ds, relations);

            if (relationToAdd != null) {
                //Add tags to relation
                for (Tag t : tags) {
                        UndoRedoHandler.getInstance().add(new ChangePropertyCommand(relationToAdd, t.getKey(), t.getValue()));
                }
            } else {
                //Add tags to ways or nodes
                for (Tag t : tags) {
                    UndoRedoHandler.getInstance().add(new ChangePropertyCommand(
                            OsmDataManager.getInstance().getInProgressSelection(), t.getKey(), t.getValue()));
                }
            }
        //If the selected dataset is empty
        } else if (MainApplication.getLayerManager().getEditDataSet().selectionEmpty()) {

            JOptionPane.showMessageDialog(null, tr("No data selected."), tr("Error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Method which adds a object {@link IndoorObject} to the currently selected OSM data (to nodes and/or ways).
     *
     * @param object the object which defines the tag-set you want to add
     */
    public void addTagsToOSM(IndoorObject object) {

        if (!MainApplication.getLayerManager().getEditDataSet().selectionEmpty() &&
                !OsmDataManager.getInstance().getInProgressSelection().isEmpty()) {
            List<Tag> tags = this.getObjectTags(object);

            //Increment the counter for the presets
            this.counter.count(object);

            //Add the tags to the current selection
            for (Tag t : tags) {
                UndoRedoHandler.getInstance().add(new ChangePropertyCommand(
                        OsmDataManager.getInstance().getInProgressSelection(), t.getKey(), t.getValue()));
            }
        //If the selected dataset ist empty
        } else if (MainApplication.getLayerManager().getEditDataSet().selectionEmpty()) {
            JOptionPane.showMessageDialog(null, tr("No data selected."), tr("Error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Method which adds a list of tag-sets to the currently selected OSM data. Tags directly to ways and/or nodes.
     *
     * @author rebsc
     * @param userTags the tags which are given by the user input
     */
    public void addTagsToOSM(List<Tag> userTags) {

        if (!MainApplication.getLayerManager().getEditDataSet().selectionEmpty() &&
                !OsmDataManager.getInstance().getInProgressSelection().isEmpty()) {

            //Add the tags to the current selection
            for (Tag t : userTags) {
                UndoRedoHandler.getInstance().add(new ChangePropertyCommand(
                        OsmDataManager.getInstance().getInProgressSelection(), t.getKey(), t.getValue()));
            }
        } else if (MainApplication.getLayerManager().getEditDataSet().selectionEmpty()) {
            JOptionPane.showMessageDialog(null, tr("No data selected."), tr("Error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Method which adds the relation to OSM data. Also adds the selected tag-set to relation object.
     *
     * @author rebsc
     * @param role the Multipolygon Role as String
     */
    public void addRelation(String role) {
        Relation newRelation = new Relation();
        RelationMember newMember;
        DataSet ds = OsmDataManager.getInstance().getEditDataSet();

        // Create new relation and add a new member with specific role
        if (!MainApplication.getLayerManager().getEditDataSet().selectionEmpty()) {
            for (OsmPrimitive osm : ds.getSelected()) {
                 newMember = new RelationMember(role == null ? "" : role, osm);
                 newRelation.addMember(newMember);
            }
        }
        // Add relation to OSM data
        UndoRedoHandler.getInstance().add(new AddCommand(MainApplication.getLayerManager().getEditDataSet(), newRelation));
    }

    /**
     * Method which edits the selected object to the currently selected OSM data (relations).
     *
     * @author rebsc
     * @param role The Multipolygon Role as String
     * @param innerRelation inner relation
     */
    public void editRelation(String role, Collection<OsmPrimitive> innerRelation) {

        RelationMember newMember;
        DataSet ds = OsmDataManager.getInstance().getEditDataSet();
        Collection<Relation> relations = ds.getRelations();
        Relation relation = getRelationFromDataSet(ds, relations);

        if (!MainApplication.getLayerManager().getEditDataSet().selectionEmpty() &&
                !OsmDataManager.getInstance().getInProgressSelection().isEmpty() &&
                !innerRelation.isEmpty() && getRole(ds, relations).equals("outer")) {

            //Add new relation member to selected relation
            for (OsmPrimitive osm : innerRelation) {
                 newMember = new RelationMember(role == null ? "" : role, osm);
                 relation.addMember(newMember);
            };

        //Check if dataset is not empty or if {@link innerRelation} has no value
        } else if (MainApplication.getLayerManager().getEditDataSet().selectionEmpty() || innerRelation.isEmpty()) {
            JOptionPane.showMessageDialog(null, tr("No data selected."), tr("Error"), JOptionPane.ERROR_MESSAGE);

        //If selected object is not a relation member or not a relation member with role "outer"
        } else if (!getRole(ds, relations).equals("outer")) {
            JOptionPane.showMessageDialog(null,
                    tr("No relation or no relation member with role \"outer\" selected."), tr("Error"), JOptionPane.ERROR_MESSAGE);
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

    /**
     * Function which returns the the relation (if any) of the currently selected object.
     * If not returns null.
     * @author rebsc
     * @param ds actual working dataset
     * @param relations collection of relations in the dataset
     * @return relation of currently selected dataset
     */
    private Relation getRelationFromDataSet(DataSet ds, Collection<Relation> relations) {
        for (Relation r: relations) {
            for (RelationMember rm: r.getMembers()) {
                for (OsmPrimitive osm: ds.getSelected()) {
                    if (rm.refersTo(osm)) {
                        return r;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Function which returns the relation role (if any) of the currently selected object.
     * If object is not a relation returns empty string.
     * @author rebsc
     * @param ds active dataset
     * @param relations collection of relations in the dataset
     * @return role of currently selected relation member if any
     */
    private String getRole(DataSet ds, Collection<Relation> relations) {

        if (isRelationMember(ds, relations)) {
            for (Relation r: relations) {
                for (RelationMember rm: r.getMembers()) {
                    for (OsmPrimitive osm: ds.getSelected()) {
                        if (rm.refersTo(osm)) {
                            return rm.getRole();
                        }
                    }
                }
            }
        }
        return "";
    }

    /**
     * Function which returns true if the currently selected object is a relation
     * @author rebsc
     * @param ds active dataset
     * @param relations relations
     * @return true if selected object is a relation
     */
    private boolean isRelationMember(DataSet ds, Collection<Relation> relations) {
        for (Relation r: relations) {
            for (RelationMember rm: r.getMembers()) {
                for (OsmPrimitive osm: ds.getSelected()) {
                    if (rm.refersTo(osm)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
