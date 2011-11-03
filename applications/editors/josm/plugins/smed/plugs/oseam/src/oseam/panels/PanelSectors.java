package oseam.panels;

import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.*;

public class PanelSectors extends JFrame {

	private OSeaMAction dlg;
	public JPanel panel;
	public JButton minusButton;
	public JButton plusButton;
	public JTable table;
	private JScrollPane tablePane;
	private ActionListener alMinusButton;
	private ActionListener alPlusButton;

	public PanelSectors(OSeaMAction dia) {
		super("Sector Table");
		dlg = dia;
		lights = new ArrayList<Object[]>();
		lights.add(new Object[]{null, null, null, null, null, null, null, null, null, null, null, null});
		panel = new JPanel();
		this.setSize(700, 100);
		panel.setBounds(0, 0, 700, 512);
		this.getContentPane().add(panel);
		table = new JTable();
		tablePane = new JScrollPane(table);
		tablePane.setBounds(40, 0, 660, 34);
		panel.setLayout(null);
		panel.add(tablePane);

		alMinusButton = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				deleteSector(2);
			}
		};
		minusButton = new JButton(new ImageIcon(getClass().getResource("/images/MinusButton.png")));
		minusButton.setBounds(0, 0, 32, 34);
		minusButton.addActionListener(alMinusButton);
		panel.add(minusButton);

		alPlusButton = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				addSector(2);
			}
		};
		plusButton = new JButton(new ImageIcon(getClass().getResource("/images/PlusButton.png")));
		plusButton.setBounds(0, 34, 32, 34);
		plusButton.addActionListener(alPlusButton);
		panel.add(plusButton);
	}

	public int getSectorCount() {
		return getRowCount();
	}

	public void addSector(int idx) {
		lights.add(idx, new Object[]{null, null, null, null, null, null, null, null, null, null, null, null});
		tablePane.setSize(660, ((getRowCount() * 16) + 18));
		if (getRowCount() > 3) {
			this.setSize(700, ((getRowCount() * 16) + 40));
		} else {
			this.setSize(700, 100);
		}
//		light.fireTableRowsInserted(idx, idx);
	}

	public void deleteSector(int idx) {
		lights.remove(idx);
		tablePane.setSize(660, ((getRowCount() * 16) + 18));
		if (getRowCount() > 3) {
			this.setSize(700, ((getRowCount() * 16) + 40));
		} else {
			this.setSize(700, 100);
		}
//		light.fireTableRowsDeleted(idx, idx);
	}

	private String[] columns = { "Sector", "Start", "End", "Colour",
			"Character", "Group", "Period", "Height", "Range", "Visibility" };
	private ArrayList<Object[]> lights;

	private String getColumnName(int col) {
		return columns[col].toString();
	}

	private int getColumnCount() {
		return columns.length;
	}

	private int getRowCount() {
		return lights.size()-1;
	}

	private Object getValueAt(int row, int col) {
		if (col == 0) {
			return row+1;
		} else {
			return ((Object[])lights.get(row+1))[col+1];
		}
	}

	private boolean isCellEditable(int row, int col) {
		return (col > 0);
	}

	private void setValueAt(Object value, int row, int col) {
		((Object[])lights.get(row+1))[col+1] = value;
	}
	
}
