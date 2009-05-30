package org.openstreetmap.josm.plugins.tageditor.tagspec.ui;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import org.openstreetmap.josm.plugins.tageditor.tagspec.KeyValuePair;
import org.openstreetmap.josm.plugins.tageditor.tagspec.TagSpecifications;


public class TagsTableModel extends AbstractTableModel {

	static private Logger logger = Logger.getLogger(TagsTableModel.class.getName());

	private ArrayList<KeyValuePair> items = null;
	private ArrayList<KeyValuePair> visibleItems = null;

	public TagsTableModel() {
		items = new ArrayList<KeyValuePair>();
		visibleItems = new ArrayList<KeyValuePair>();
	}

	protected void sort() {
		Collections.sort(
				items,
				new Comparator<KeyValuePair>() {
					public int compare(KeyValuePair self,
							KeyValuePair other) {
						int ret =self.getKey().compareToIgnoreCase(other.getKey());

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
		} catch(Exception e) {
			logger.log(Level.SEVERE, "failed to init TagTableModel. Exception:" + e);
			return;
		}

		items = spec.asList();
		sort();
		for(KeyValuePair item : items) {
			visibleItems.add(item);
		}
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return visibleItems.size();
	}

	public Object getValueAt(int row, int col) {
		KeyValuePair pair = visibleItems.get(row);
		switch(col) {
		case 0: return pair.getKey();
		case 1: return pair.getValue();
		default:
			/* should not happen */
			throw new IllegalArgumentException(tr("unexpected column number {0}",col));
		}
	}

	public void filter(String filter) {
		synchronized(this) {
			if (filter == null || filter.trim().equals("")) {
				visibleItems.clear();
				for(KeyValuePair pair: items) {
					visibleItems.add(pair);
				}
			} else {
				visibleItems.clear();
				filter = filter.toLowerCase();
				for(KeyValuePair pair: items) {
					if (pair.getKey().toLowerCase().trim().startsWith(filter)
							||  pair.getValue().toLowerCase().trim().startsWith(filter)) {
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

	public KeyValuePair getVisibleItem(int row) {
		if (row < 0 || row >= visibleItems.size())
			throw new IndexOutOfBoundsException("row is out of bound: row=" + row);
		return visibleItems.get(row);
	}

}