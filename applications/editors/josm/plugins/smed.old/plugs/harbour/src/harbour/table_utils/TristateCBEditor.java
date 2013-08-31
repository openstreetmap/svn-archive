package harbour.table_utils;

import harbour.widgets.TristateCheckBox;
import harbour.widgets.TristateCheckBox.State;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class TristateCBEditor extends AbstractCellEditor implements
		ItemListener, TableCellEditor {
	
	private TristateCheckBox box;
	private State curState;
	
	public TristateCBEditor() {
		box = new TristateCheckBox("",null,TristateCheckBox.NOT_SELECTED);
		box.setHorizontalAlignment(JLabel.CENTER);
		box.addItemListener(this);
		// curState = TristateCheckBox.NOT_SELECTED;
	}


	@Override
	public Object getCellEditorValue() {
		return curState;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int col) {

		curState	= (State) value;
		
		box.setState(curState);
		
		return box;
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		curState = box.getState();
	}

}
