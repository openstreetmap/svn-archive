package oseam.panels;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.*;
import oseam.seamarks.SeaMark.*;

public class PanelSectors extends JPanel {

	private OSeaMAction dlg;
	private TableModel model;
	private JTable table;
	private JFrame frame;

	// public JPanel panel;
	// public JButton minusButton;
	// public JButton plusButton;
	// private JScrollPane tablePane;
	// private ActionListener alMinusButton;
	// private ActionListener alPlusButton;

	public PanelSectors(OSeaMAction dia) {
		dlg = dia;
		model = new SectorTable();
		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		frame = new JFrame("Sector Table");
		frame.getContentPane().add(new JScrollPane(table));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private class SectorTable extends AbstractTableModel {

		private String[] headings = { "Sector", "Colour", "Character", "Group", "Period", "Height", "Range", "Visibility", "Start", "End" };
		
		public SectorTable() {
	}

	public String getColumnName(int col) {
		return headings[col];
	}

	public int getColumnCount() {
		return headings.length;
	}

	public int getRowCount() {
		return dlg.mark.getSectorCount() - 1;
	}

	public boolean isCellEditable(int row, int col) {
		return (col > 0);
	}

	public Class getColumnClass(int col) {
		switch (col) {
		case 1:
			return Col.class;
		case 7:
			return Vis.class;
		default:
			return String.class;
		}
	}
	
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 1:
			return (dlg.mark.getColour(Ent.LIGHT, row));
		case 2:
			return (dlg.mark.getLightChar(row));
		case 3:
			return (dlg.mark.getLightGroup(row));
		case 4:
			return (dlg.mark.getLightPeriod(row));
		case 5:
			return (dlg.mark.getLightHeight(row));
		case 6:
			return (dlg.mark.getLightRange(row));
		case 7:
			return (dlg.mark.getVisibility(row));
		case 8:
			return (dlg.mark.getLightSector1(row));
		case 9:
			return (dlg.mark.getLightSector2(row));
		default:
			return null;
		}
	}

	public void setValueAt(Object value, int row, int col) {
		switch (col) {
		case 1:
			dlg.mark.setColour(Ent.LIGHT, row, (SeaMark.Col)value);
		case 2:
			dlg.mark.setLightChar(row, (SeaMark.Chr)value);
		case 7:
			dlg.mark.setVisibility(row, (SeaMark.Vis)value);
		default:
			dlg.mark.setLightSector2(row, (String)value);
		}
	}
}
	/*
	 * panel = new JPanel();
	 * this.setSize(700, 100);
	 * panel.setBounds(0, 0, 700, 512);
	 * this.getContentPane().add(panel);
	 * table = new JTable();
	 * tablePane = new JScrollPane(table);
	 * tablePane.setBounds(40, 0, 660, 34);
	 * panel.setLayout(null);
	 * panel.add(tablePane);
	 * 
	 * alMinusButton = new ActionListener() {
	 *  public void actionPerformed(java.awt.event.ActionEvent e) { deleteSector(2); } };
	 * minusButton = new JButton(new
	 * ImageIcon(getClass().getResource("/images/MinusButton.png")));
	 * minusButton.setBounds(0, 0, 32, 34);
	 * minusButton.addActionListener(alMinusButton); panel.add(minusButton);
	 * 
	 * alPlusButton = new ActionListener() { public void
	 * actionPerformed(java.awt.event.ActionEvent e) { addSector(2); } };
	 * plusButton = new JButton(new
	 * ImageIcon(getClass().getResource("/images/PlusButton.png")));
	 * plusButton.setBounds(0, 34, 32, 34);
	 * plusButton.addActionListener(alPlusButton); panel.add(plusButton); }
	 * 
	 * public int getSectorCount() { return getRowCount(); }
	 * 
	 * public void addSector(int idx) { lights.add(idx, new Object[]{null, null,
	 * null, null, null, null, null, null, null, null, null, null});
	 * tablePane.setSize(660, ((getRowCount() * 16) + 18)); if (getRowCount() > 3)
	 * { this.setSize(700, ((getRowCount() * 16) + 40)); } else {
	 * this.setSize(700, 100); } // light.fireTableRowsInserted(idx, idx); }
	 * 
	 * public void deleteSector(int idx) { lights.remove(idx);
	 * tablePane.setSize(660, ((getRowCount() * 16) + 18)); if (getRowCount() > 3)
	 * { this.setSize(700, ((getRowCount() * 16) + 40)); } else {
	 * this.setSize(700, 100); } // light.fireTableRowsDeleted(idx, idx); }
	 * 
	 * private ArrayList<Object[]> lights;
	 * 
	 */
}
