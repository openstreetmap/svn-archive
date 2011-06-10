package oseam.seamarks;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class Light extends AbstractTableModel {

	private String[] columns = { "Sector", "Start", "End", "Colour",
			"Character", "Group", "Period", "Height", "Range", "Visibility" };
	private ArrayList<Object[]> lights;
	
	public Light() {
		super();
		lights = new ArrayList<Object[]>();
		lights.add(new Object[12]);
	}

	public String getColumnName(int col) {
		return columns[col].toString();
	}

	public int getColumnCount() {
		return columns.length;
	}

	public int getRowCount() {
		return lights.size()-1;
	}

	public Object getValueAt(int row, int col) {
		if (col == 0) {
			return row+1;
		} else {
			return ((Object[])lights.get(row+1))[col+1];
		}
	}

	public boolean isCellEditable(int row, int col) {
		return (col > 0);
	}

	public void setValueAt(Object value, int row, int col) {
//		lights.set(row, value)[col] = value;
		fireTableCellUpdated(row, col);
	}
	public void addSector(int idx) {
		lights.add(idx, new Object[12]);
	}
	
	public void deleteSector(int idx) {
		lights.remove(idx);
	}
	
}

