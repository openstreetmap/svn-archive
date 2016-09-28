// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tageditor.tagspec.ui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.plugins.tageditor.tagspec.TagSpecifications;

public class TagsTableModel extends AbstractTableModel {

    private static Logger logger = Logger.getLogger(TagsTableModel.class.getName());

    private ArrayList<Tag> items = null;
    private ArrayList<Tag> visibleItems = null;

    public TagsTableModel() {
        items = new ArrayList<>();
        visibleItems = new ArrayList<>();
    }

    protected void sort() {
        Collections.sort(
                items,
                new Comparator<Tag>() {
                    @Override
                    public int compare(Tag self, Tag other) {
                        int ret = self.getKey().compareToIgnoreCase(other.getKey());

                        if (ret == 0)
                            return self.getValue().compareToIgnoreCase(other.getValue());
                        else
                            return ret;
                    }
                }
        );
    }

    protected void clear() {
        items.clear();
        visibleItems.clear();
    }

    public void initFromTagSpecifications() {
        clear();
        TagSpecifications spec;

        try {
            spec = TagSpecifications.getInstance();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "failed to init TagTableModel. Exception:" + e);
            return;
        }

        items = spec.asList();
        sort();
        for (Tag item : items) {
            visibleItems.add(item);
        }
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        return visibleItems.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        Tag pair = visibleItems.get(row);
        switch(col) {
        case 0: return pair.getKey();
        case 1: return pair.getValue();
        default:
            /* should not happen */
            throw new IllegalArgumentException(tr("unexpected column number {0}", col));
        }
    }

    public void filter(String filter) {
        synchronized (this) {
            if (filter == null || filter.trim().equals("")) {
                visibleItems.clear();
                for (Tag pair: items) {
                    visibleItems.add(pair);
                }
            } else {
                visibleItems.clear();
                filter = filter.toLowerCase();
                for (Tag pair: items) {
                    if (pair.getKey().toLowerCase().trim().startsWith(filter)
                            || pair.getValue().toLowerCase().trim().startsWith(filter)) {
                        visibleItems.add(pair);
                    }
                }
            }
            fireTableDataChanged();
            fireTableStructureChanged();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Tag getVisibleItem(int row) {
        if (row < 0 || row >= visibleItems.size())
            throw new IndexOutOfBoundsException("row is out of bound: row=" + row);
        return visibleItems.get(row);
    }
}
