package harbour.table_utils;

import java.awt.Component;

import harbour.widgets.TristateCheckBox;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class StateRenderer extends TristateCheckBox implements
		TableCellRenderer {

	public StateRenderer() {
		super();
		setHorizontalAlignment(JLabel.CENTER);
	}

	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {
		// setState((State) table.getValueAt(row, col));
		
		setState((State) value);
		return this;
	}

}
