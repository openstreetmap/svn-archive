/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstreetmap.josm.plugins.conflation;

import java.util.HashSet;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.TagCollection;

/**
 * Model for the conflation results table.
 */
class MatchTableModel extends AbstractTableModel implements ConflationListChangedListener {

    private ConflationCandidateList candidates = null;
    private final static String[] columnNames = {"Mine", "Theirs", "Distance (m)", "Cost", "Tags"};

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        if (candidates == null)
            return 0;
        return candidates.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        if (candidates == null)
            return null;
        
        ConflationCandidate c = candidates.get(row);
        if (col == 0) {
            return c.getSourcePrimitive();
        } else if (col == 1) {
            return c.getTargetPrimitive();
        } else if (col == 2) {
            return c.getDistance();
        } else if (col == 3) {
            return c.getCost();
        }
        if (col == 4) {
            HashSet<OsmPrimitive> set = new HashSet<OsmPrimitive>();
            set.add(c.getSourcePrimitive());
            set.add(c.getTargetPrimitive());
            TagCollection tags = TagCollection.unionOfAllPrimitives(set);
            Set<String> keys = tags.getKeysWithMultipleValues();
            if (keys.isEmpty()) {
                return "No conflicts!";
            } else {
                return "Conflicts!";
            }

        } else {
            return 0;
        }
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /**
     * @return the candidates
     */
    public ConflationCandidateList getCandidates() {
        return candidates;
    }

    /**
     * @param candidates the candidates to set
     */
    public void setCandidates(ConflationCandidateList candidates) {
        this.candidates = candidates;
        fireTableDataChanged();
    }

    @Override
    public void conflationListChanged(ConflationCandidateList list) {
        fireTableDataChanged();
    }
}
