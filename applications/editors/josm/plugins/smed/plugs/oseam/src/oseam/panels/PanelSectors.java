package oseam.panels;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.*;
import oseam.seamarks.SeaMark.Att;
import oseam.seamarks.SeaMark.*;

public class PanelSectors extends JFrame {

	private OSeaMAction dlg;
	private JPanel panel;
	private TableModel model;
	private JTable table;

	public JButton minusButton;
	private ActionListener alMinusButton = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			deleteSector(0);
		}
	};
	public JButton plusButton;
	private ActionListener alPlusButton = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			addSector(0);
		}
	};

	public PanelSectors(OSeaMAction dia) {
		super("Sector Table");
		dlg = dia;
		this.setSize(700, 100);
		this.setVisible(true);
		this.setAlwaysOnTop(true);
		this.setLocation(450, 0);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(null);
		minusButton = new JButton(new ImageIcon(getClass().getResource("/images/MinusButton.png")));
		minusButton.setBounds(0, 0, 32, 34);
		minusButton.addActionListener(alMinusButton);
		this.add(minusButton);
		plusButton = new JButton(new ImageIcon(getClass().getResource("/images/PlusButton.png")));
		plusButton.setBounds(0, 34, 32, 34);
		plusButton.addActionListener(alPlusButton);
		this.add(plusButton);
		panel = new JPanel(new BorderLayout());
		panel.setBounds(40, 0, 660, 512);
		model = new SectorTable();
		table = new JTable(model);
		table.setBounds(0, 0, 660, 34);
		panel.add(new JScrollPane(table));
		this.getContentPane().add(panel);
	}

	private class SectorTable extends AbstractTableModel {

		private String[] headings = { "Sector", "Colour", "Character", "Group", "Sequence", "Period", "Height", "Range", "Visibility", "Start", "End", "Radius" };

		public SectorTable() {
		}

		public String getColumnName(int col) {
			return headings[col];
		}

		public int getColumnCount() {
			return headings.length;
		}

		public int getRowCount() {
			return dlg.mark.getSectorCount();
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
			return dlg.mark.getLightAtt(col, row);
		}

		public void setValueAt(Object value, int row, int col) {
			dlg.mark.setLightAtt(col, row, value);
		}
	}

	public int getSectorCount() {
		return model.getRowCount();
	}

	public void addSector(int idx) {
		dlg.mark.addLightAtt(Att.COL, Col.UNKNOWN);
		table.setSize(660, ((table.getRowCount() * 16) + 18));
		if (table.getRowCount() > 3) {
			this.setSize(700, ((table.getRowCount() * 16) + 40));
		} else {
			this.setSize(700, 100);
		}
	}

	public void deleteSector(int idx) {
		dlg.mark.subLightAtt(Att.COL, 0);
		table.setSize(660, ((table.getRowCount() * 16) + 18));
		if (table.getRowCount() > 3) {
			this.setSize(700, ((table.getRowCount() * 16) + 40));
		} else {
			this.setSize(700, 100);
		}
	}

}
