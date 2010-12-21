package harbour.models;

import harbour.widgets.TristateCheckBox;

import javax.swing.table.AbstractTableModel;

public class SearchTableModel extends AbstractTableModel {
	
	private String[] columnNames = {
			"Key",
            "Value",
            "Show"};
	private Object[][] data = {
			{"amenity","bank",			new Boolean(false) },
			{"amenity","cafe",			new Boolean(false) },
			{"amenity","hospital",		new Boolean(false) },
			{"amenity","parking",		new Boolean(false) },
			{"amenity","bank",			new Boolean(false) },
			{"amenity","pharmacy",		new Boolean(false) },
			{"amenity","police",		new Boolean(false) },
			{"amenity","post_box",		new Boolean(false) },
			{"amenity","post_office",	new Boolean(false) },
			{"amenity","restaurant",	new Boolean(false) },
			{"amenity","telephone",		new Boolean(false) },
			{"amenity","toilets",		new Boolean(false) },
			{"information","board",		new Boolean(false) },
			{"information","citymap",	new Boolean(false) },
			{"information","office",	new Boolean(false) },
	};

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return data.length;
	}

	public String getColumnName(int col) {
        return columnNames[col];
    }
	
	@Override
	public Object getValueAt(int row, int col) {
		return data[row][col];
	}
	
    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }


    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col == 2) 	return true;
        else 			return false;
    }


    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
    
    public Boolean isWanted(int i)	{ return (Boolean) data[i][2]; }
    public String getKey(int i)		{ return (String) data[i][0]; }
    public String getValue(int i)	{ return (String) data[i][1]; }
}
