package oseam.panels;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;

public class PanelSectors extends JFrame {
	
	public JPanel panel;
	public JButton minusButton;
	public JButton plusButton;
	public JTable table;
	
	class TableModel extends AbstractTableModel {

		private String[] columns = { "Sector", "Start", "End", "Colour",
				"Character", "Group", "Period", "Height", "Range", "Visibility" };
		private Object[][] data = { { new Integer(1), "", "", "", "", "", "", "", "", "" } };

		public String getColumnName(int col) {
			return columns[col].toString();
		}

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public boolean isCellEditable(int row, int col) {
			return (col > 0);
		}

		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}
}
	
	public PanelSectors() {
		super("Sector Table");
		panel = new JPanel();
		this.setSize(700, 100);
		panel.setBounds(0, 0, 700, 512);
		this.getContentPane().add(panel);
		minusButton = new JButton(new ImageIcon(getClass().getResource("/images/MinusButton.png")));
		minusButton.setBounds(0, 0, 32, 34);
		plusButton = new JButton(new ImageIcon(getClass().getResource("/images/PlusButton.png")));
		plusButton.setBounds(0, 34, 32, 34);
		table = new JTable(new TableModel());
		JScrollPane tablePane = new JScrollPane(table);
		tablePane.setBounds(40, 0, 660, 34);
		panel.setLayout(null);
		panel.add(minusButton);
		panel.add(plusButton);
		panel.add(tablePane);
	}
}
