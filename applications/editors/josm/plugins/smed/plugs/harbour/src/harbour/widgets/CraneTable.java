package harbour.widgets;

import java.awt.GridLayout;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import harbour.models.CraneTableModel;
import harbour.table_utils.StateRenderer;
import harbour.table_utils.TristateCBEditor;
import harbour.widgets.TristateCheckBox.State;

public class CraneTable extends JScrollPane {

	private static final long serialVersionUID = 1L;
	private JTable craneTable = null;

	/**
	 * This is the default constructor
	 */
	public CraneTable() {
		super();
		// setLayout(new GridLayout(1,0));
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(207, 66);

		this.setViewportView(getCraneTable());
	}

	/**
	 * This method initializes craneTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getCraneTable() {
		if (craneTable == null) {
			craneTable = new JTable();
			craneTable.setModel(new CraneTableModel());
			craneTable.setDefaultRenderer(State.class,new StateRenderer());
			craneTable.setDefaultEditor(State.class,new TristateCBEditor());
			
			 TableColumn column = null;
			 for (int i = 0; i < 4; i++) {
			     column = craneTable.getColumnModel().getColumn(i);
			     if (i == 0) 
			         column.setPreferredWidth(45); //first / last column is smaller
			     else if(i == 2) column.setPreferredWidth(90);
			     else if(i == 4) column.setPreferredWidth(35);
			     else column.setPreferredWidth(75);

			 }
		}

		return craneTable;
	}

}
