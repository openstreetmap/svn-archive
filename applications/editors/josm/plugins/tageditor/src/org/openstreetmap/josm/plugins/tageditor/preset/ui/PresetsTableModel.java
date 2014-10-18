// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tageditor.preset.ui;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.openstreetmap.josm.gui.tagging.TaggingPreset;

public class PresetsTableModel extends AbstractTableModel  {

    //private static final Logger logger = Logger.getLogger(PresetsTableModel.class.getName());

    private final ArrayList<TableModelListener> listeners = new ArrayList<>();
    private final ArrayList<TaggingPreset> items = new ArrayList<>();
    private final ArrayList<TaggingPreset> visibleItems = new ArrayList<>();

    protected void initModelFromPresets(Collection<TaggingPreset> presets) {
        items.clear();
        visibleItems.clear();
        items.addAll(presets);
        visibleItems.addAll(presets);
    }

    public PresetsTableModel() {
    }

    public Collection<TaggingPreset> getPresets() {
        return items;
    }

    public void setPresets(Collection<TaggingPreset> presets) {
        initModelFromPresets(presets);
        fireTableDataChanged();
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        synchronized(listeners) {
            if (l == null)
                return;
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return visibleItems.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        TaggingPreset item = visibleItems.get(rowIndex);
        switch(columnIndex) {
        case 0: return item.group;
        case 1: return item;
        default: return "unknown";
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        synchronized (listeners) {
            if (listeners.contains(l)) {
                listeners.remove(l);
            }
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        // do nothing. No editing allowed
    }

    public TaggingPreset getVisibleItem(int idx) {
        if (idx < 0 || idx >= this.visibleItems.size())
            throw new IndexOutOfBoundsException("index out of bounds. idx=" + idx);
        return visibleItems.get(idx);
    }

    public void filter(String filter) {
        synchronized(this) {
            if (filter == null || filter.trim().equals("")) {
                visibleItems.clear();
                for(TaggingPreset item: items) {
                    visibleItems.add(item);
                }
            } else {
                visibleItems.clear();
                filter = filter.toLowerCase();
                for(TaggingPreset item: items) {
                    if ((item.getName() != null && item.getName().toLowerCase().trim().contains(filter))
                            || (item.group != null && item.group.getName() != null && item.group.getName().toLowerCase().trim().contains(filter))) {
                        visibleItems.add(item);
                    }
                }
            }
            fireTableDataChanged();
            fireTableStructureChanged();
        }
    }
}
