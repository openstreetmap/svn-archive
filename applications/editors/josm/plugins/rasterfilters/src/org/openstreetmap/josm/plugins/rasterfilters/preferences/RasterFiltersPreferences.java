package org.openstreetmap.josm.plugins.rasterfilters.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.json.JsonObject;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.preferences.SubPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.TabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.map.MapPreference;
import org.openstreetmap.josm.tools.GBC;

/**
 * This class draws subtab 'Image Filters' in the Preferences menu.
 *
 * @author Nipel-Crumple
 *
 */
public class RasterFiltersPreferences implements SubPreferenceSetting {

	private FiltersDownloader downloader = new FiltersDownloader();
	AbstractTableModel model;
	JPanel holder;

	@Override
	public void addGui(PreferenceTabbedPane gui) {

		model = new FiltersTableModel();

		if (holder == null) {
			holder = new JPanel();
			holder.setLayout(new GridBagLayout());

			holder.setBorder(new EmptyBorder(10, 10, 10, 10));

			model.addTableModelListener(new TableModelListener() {

				@Override
				public void tableChanged(TableModelEvent e) {
					int row = e.getFirstRow();
					int col = e.getColumn();
					TableModel model = (TableModel) e.getSource();

					Boolean isDownloadedUpdate = (Boolean) model.getValueAt(
							row, col);
					List<FilterInfo> filtersList = ((FiltersTableModel) model).filtersInfoList;

					filtersList.get(row).setNeedToDownload(isDownloadedUpdate);

				}
			});

			JTable table = new JTable(model);
			table.getTableHeader().setReorderingAllowed(false);
			table.getColumnModel().getColumn(3).setMaxWidth(20);
			JScrollPane pane = new JScrollPane(table);

			holder.add(pane, GBC.eol().fill(GBC.BOTH));

			GridBagConstraints c = GBC.eol();
			c.anchor = GBC.EAST;

			JButton download = new JButton("Download");
			download.addActionListener(downloader);
			holder.add(download, c);
		}

		MapPreference pref = gui.getMapPreference();
		pref.addSubTab(this, "Image Filters", holder);

	}

	@Override
	public boolean ok() {
		List<FilterInfo> filtersInfoList = ((FiltersTableModel) model).getFiltersInfoList();

		for (FilterInfo temp : filtersInfoList) {
			JsonObject meta = temp.getMeta();
			String paramName = meta.getString("name");
			paramName = "rasterfilters." + paramName;
			Main.pref.put(paramName, temp.isNeedToDownload());
		}

		return false;
	}

	@Override
	public boolean isExpert() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TabPreferenceSetting getTabPreferenceSetting(PreferenceTabbedPane gui) {
		return gui.getMapPreference();
	}

	class FiltersTableModel extends AbstractTableModel {

		String[] columnNames = { "Filter Name", "Author", "Description", "" };
		Class<?>[] columnClasses = { String.class, String.class, String.class, Boolean.class };
		List<FilterInfo> filtersInfoList;
		Object[][] data;

		public FiltersTableModel() {

			filtersInfoList = FiltersDownloader.downloadFiltersInfoList();
			data = new Object[filtersInfoList.size()][4];

			for (int i = 0; i < filtersInfoList.size(); i++) {
				data[i][0] = filtersInfoList.get(i).getName();
				data[i][1] = filtersInfoList.get(i).getOwner();
				data[i][2] = filtersInfoList.get(i).getDescription();
				data[i][3] = filtersInfoList.get(i).isNeedToDownload();
			}

		}

		@Override
		public int getRowCount() {
			return filtersInfoList.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return filtersInfoList.get(rowIndex).getName();
			case 1:
				return filtersInfoList.get(rowIndex).getOwner();
			case 2:
				return filtersInfoList.get(rowIndex).getDescription();
			case 3:
				return filtersInfoList.get(rowIndex).isNeedToDownload();
			default:
				return null;
			}
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return columnClasses[col];
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			if (col == 3) {
				return true;
			}

			return false;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (col == 3) {
				filtersInfoList.get(row).setNeedToDownload((boolean) value);
				fireTableCellUpdated(row, col);
			}
		}

		public List<FilterInfo> getFiltersInfoList() {
			return filtersInfoList;
		}
	}

}

class FilterInfo {
	private String name;
	private String description;
	private JsonObject meta;
	private boolean needToDownload;
	private String owner;

	public FilterInfo() {

	}

	public FilterInfo(String name, String description, JsonObject meta,
			boolean needToDownload) {
		this.setName(name);
		this.setDescription(description);
		this.meta = meta;
		this.setNeedToDownload(needToDownload);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JsonObject getMeta() {
		return meta;
	}

	public void setMeta(JsonObject meta) {
		this.meta = meta;
	}

	public boolean isNeedToDownload() {
		return needToDownload;
	}

	public void setNeedToDownload(boolean needToDownload) {
		this.needToDownload = needToDownload;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Override
	public String toString() {
		return "name: " + getName() + "\nDescription: " + getDescription()
				+ "\nMeta: " + getMeta();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof FilterInfo) {
			if (name.equals(((FilterInfo) o).getName()) &&
					meta.equals(((FilterInfo) o).getMeta()) &&
					description.equals(((FilterInfo) o).getDescription())) {
				return true;
			}
		}

		return false;
	}
}
