package org.openstreetmap.josm.plugins.tageditor.preset.ui;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.openstreetmap.josm.plugins.tageditor.preset.Group;
import org.openstreetmap.josm.plugins.tageditor.preset.Item;
import org.openstreetmap.josm.plugins.tageditor.preset.Presets;


public class PresetsTableModel extends AbstractTableModel  {

	private static final Logger logger = Logger.getLogger(PresetsTableModel.class.getName());

	private final ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
	private final ArrayList<Item> items = new ArrayList<Item>();
	private final ArrayList<Item> visibleItems = new ArrayList<Item>();
	private Presets presets = null;

	protected void initModelFromPresets(Presets presets) {
		for(Group group: presets.getGroups()) {
			for(Item item: group.getItems()) {
				items.add(item);
				visibleItems.add(item);
			}
		}
	}

	public PresetsTableModel() {
	}

	public PresetsTableModel(Presets presets) {
		setPresets(presets);
	}


	public Presets getPresets() {
		return presets;
	}

	public void setPresets(Presets presets) {
		this.presets = presets;
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
		Item item = visibleItems.get(rowIndex);
		switch(columnIndex) {
		case 0: return item.getParent();
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

	public Item getVisibleItem(int idx) {
		if (idx < 0 || idx >= this.visibleItems.size())
			throw new IndexOutOfBoundsException("index out of bounds. idx=" + idx);
		return visibleItems.get(idx);
	}

	public void filter(String filter) {
		synchronized(this) {
			if (filter == null || filter.trim().equals("")) {
				visibleItems.clear();
				for(Item item: items) {
					visibleItems.add(item);
				}
			} else {
				visibleItems.clear();
				filter = filter.toLowerCase();
				for(Item item: items) {
					if (    (item.getName() != null && item.getName().toLowerCase().trim().startsWith(filter))
							|| (item.getParent().getName() != null && item.getParent().getName().toLowerCase().trim().startsWith(filter))) {
						visibleItems.add(item);
					}
				}
			}
			fireTableDataChanged();
			fireTableStructureChanged();
		}
	}
}
