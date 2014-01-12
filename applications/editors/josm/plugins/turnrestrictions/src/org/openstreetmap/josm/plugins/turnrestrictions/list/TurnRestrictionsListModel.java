package org.openstreetmap.josm.plugins.turnrestrictions.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;

import org.openstreetmap.josm.data.osm.NameFormatter;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.DefaultNameFormatter;

/**
 * This is a list model for a list of turn restrictions.
 * 
 */
public class TurnRestrictionsListModel extends AbstractListModel {
    private final ArrayList<Relation> turnrestrictions = new ArrayList<Relation>();
    private DefaultListSelectionModel selectionModel;

    /**
     * Creates the model
     * 
     * @param selectionModel the selection model used in the turn restriction list
     */
    public TurnRestrictionsListModel(DefaultListSelectionModel selectionModel) {
        this.selectionModel = selectionModel;
    }

    /**
     * Replies the turn restriction at position {@code idx} in the list.
     * 
     * @param idx the index 
     * @return the turn restriction at position {@code idx} in the list.
     */
    public Relation getTurnRestriction(int idx) {
        return turnrestrictions.get(idx);
    }

    /**
     * Sorts the turn restrictions in this model 
     */
    public void sort() {
        Collections.sort(
                turnrestrictions,
                new Comparator<Relation>() {
                    NameFormatter formatter = DefaultNameFormatter.getInstance();

                    public int compare(Relation r1, Relation r2) {
                        return r1.getDisplayName(formatter).compareTo(r2.getDisplayName(formatter));
                    }
                }
        );
    }

    protected boolean isValid(Relation r) {
        return !r.isDeleted() && r.isVisible() && !r.isIncomplete();
    }
    
    /**
     * Replies true if the primitive {@code primitive} represents
     * an OSM turn restriction.  
     * 
     * @param primitive the primitive 
     * @return true if the primitive {@code primitive} represents
     * an OSM turn restriction; false, otherwise
     */
    protected boolean isTurnRestriction(OsmPrimitive primitive) {
        if (primitive == null) return false;
        if (! (primitive instanceof Relation)) return false;
        String type = primitive.get("type");
        if (type == null || ! type.equals("restriction")) return false;
        return true;
    }
    
    /**
     * Populates the model with the turn restrictions in {@code turnrestrictions}.
     * 
     * @param turnrestrictions the turn restrictions 
     */
    public void setTurnRestrictions(Collection<Relation> turnrestrictions) {
        List<Relation> sel =  getSelectedTurnRestrictions();
        this.turnrestrictions.clear();
        if (turnrestrictions == null) {
            selectionModel.clearSelection();
            fireContentsChanged(this,0,getSize());
            return;
        }
        for (Relation r: turnrestrictions) {
            if (isValid(r) && isTurnRestriction(r)) {
                this.turnrestrictions.add(r);
            }
        }
        sort();
        fireIntervalAdded(this, 0, getSize());
        setSelectedTurnRestrictions(sel);
    }

    /**
     * Add all turn restrictions in <code>addedPrimitives</code> to the model for the
     * relation list dialog
     *
     * @param addedPrimitives the collection of added primitives. May include nodes,
     * ways, and relations.
     */
    public void addTurnRestrictions(Collection<? extends OsmPrimitive> addedPrimitives) {
        boolean added = false;
        for (OsmPrimitive p: addedPrimitives) {
            if (! isTurnRestriction(p)) {
                continue;
            }

            Relation r = (Relation)p;
            if (!isValid(r)) continue;
            if (turnrestrictions.contains(r)) {
                continue;
            }
            turnrestrictions.add(r);
            added = true;
        }
        if (added) {
            List<Relation> sel = getSelectedTurnRestrictions();
            sort();
            fireIntervalAdded(this, 0, getSize());
            setSelectedTurnRestrictions(sel);
        }
    }

    /**
     * Removes all turn restrictions in <code>removedPrimitives</code> from the model
     *
     * @param removedPrimitives the removed primitives. May include nodes, ways,
     *   and relations
     */
    public void removeTurnRestrictions(Collection<? extends OsmPrimitive> removedPrimitives) {
        if (removedPrimitives == null) return;
        Set<Relation> removedTurnRestrictions = new HashSet<Relation>();
        for (OsmPrimitive p: removedPrimitives) {
            if (!isTurnRestriction(p)) continue;
            removedTurnRestrictions.add((Relation)p);
        }
        if (removedTurnRestrictions.isEmpty())return;
        int size = turnrestrictions.size();
        turnrestrictions.removeAll(removedTurnRestrictions);
        if (size != turnrestrictions.size()) {
            List<Relation> sel = getSelectedTurnRestrictions();
            sort();
            fireContentsChanged(this, 0, getSize());
            setSelectedTurnRestrictions(sel);
        }
    }

    public Object getElementAt(int index) {
        return turnrestrictions.get(index);
    }

    public int getSize() {
        return turnrestrictions.size();
    }

    /**
     * Replies the list of selected, non-new relations. Empty list,
     * if there are no selected, non-new relations.
     *
     * @return the list of selected, non-new relations.
     */
    public List<Relation> getSelectedNonNewRelations() {
        ArrayList<Relation> ret = new ArrayList<Relation>();
        for (int i=0; i<getSize();i++) {
            if (!selectionModel.isSelectedIndex(i)) {
                continue;
            }
            if (turnrestrictions.get(i).isNew()) {
                continue;
            }
            ret.add(turnrestrictions.get(i));
        }
        return ret;
    }

    /**
     * Replies the list of selected turn restrictions. Empty list,
     * if there are no selected turn restrictions.
     *
     * @return the list of selected turn restrictions
     */
    public List<Relation> getSelectedTurnRestrictions() {
        ArrayList<Relation> ret = new ArrayList<Relation>();
        for (int i=0; i<getSize();i++) {
            if (!selectionModel.isSelectedIndex(i)) {
                continue;
            }
            ret.add(turnrestrictions.get(i));
        }
        return ret;
    }

    /**
     * Sets the selected turn restrictions
     *
     * @return sel the list of selected turn restrictions
     */
    public void setSelectedTurnRestrictions(List<Relation> sel) {
        selectionModel.clearSelection();
        if (sel == null || sel.isEmpty())
            return;
        for (Relation r: sel) {
            int i = turnrestrictions.indexOf(r);
            if (i<0) {
                continue;
            }
            selectionModel.addSelectionInterval(i,i);
        }
    }

    /**
     * Returns the index of a turn restriction 
     *
     * @return index of relation (-1, if not found)
     */
    public int getTurnRestrictionIndex(Relation tr) {
        int i = turnrestrictions.indexOf(tr);
        if (i<0)return -1;
        return i;
    }
}
