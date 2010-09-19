package smed.list;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class JCheckBoxList extends JList {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	 public JCheckBoxList() {
		 super();
		 
		 setCellRenderer(new CellRenderer());

	 }

}

class CellRenderer implements ListCellRenderer  {

	@Override
	public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

		JCheckBox checkbox = (JCheckBox) value;

		return checkbox;
		
	}


	
}
