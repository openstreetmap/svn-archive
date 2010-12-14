package harbour.models;

import harbour.widgets.TristateCheckBox;
import harbour.widgets.TristateCheckBox.State;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

public class CraneTableModel extends AbstractTableModel {

	private String[] columnNames = {
			"Kran",
			">100t",
            "50-100t",
            "25-50t",
            "<25t"};
	private Object[][] data = {
			{new ImageIcon(getClass().getResource("/images/fest.png")),  TristateCheckBox.DONT_CARE,TristateCheckBox.DONT_CARE,TristateCheckBox.DONT_CARE,TristateCheckBox.DONT_CARE},
			{new ImageIcon(getClass().getResource("/images/mobil.png")), TristateCheckBox.DONT_CARE,TristateCheckBox.DONT_CARE,TristateCheckBox.DONT_CARE,TristateCheckBox.DONT_CARE},
			{new ImageIcon(getClass().getResource("/images/Wasser.png")),TristateCheckBox.DONT_CARE,TristateCheckBox.DONT_CARE,TristateCheckBox.DONT_CARE,TristateCheckBox.DONT_CARE}
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
    	return true;
    }


    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }


}
