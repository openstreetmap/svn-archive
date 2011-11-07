package oseam.panels;

import java.awt.*;
import java.awt.event.*;
import java.util.EnumMap;

import javax.swing.*;
import javax.swing.table.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelSectors extends JFrame {

	private OSeaMAction dlg;
	private JPanel panel;
	private TableModel model;
	private JTable table;

	public JButton minusButton;
	private ActionListener alMinusButton = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if ((getSectorCount() > 1) && (table.getSelectedRow() != 0))
				deleteSector(table.getSelectedRow());
		}
	};
	public JButton plusButton;
	private ActionListener alPlusButton = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (table.getSelectedRow() < 0)
				addSector(table.getRowCount());
			else
				addSector(table.getSelectedRow()+1);
		}
	};
	public JComboBox visibilityBox;
	public EnumMap<Vis, String> visibilities = new EnumMap<Vis, String>(Vis.class);

	public PanelSectors(OSeaMAction dia) {
		super("Sector Table");
		dlg = dia;
		this.setSize(800, 100);
		this.setVisible(true);
		this.setAlwaysOnTop(true);
		this.setLocation(450, 0);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
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
		panel.setBounds(40, 0, 760, 512);
		model = new SectorTable();
		table = new JTable(model);
		table.setBounds(0, 0, 760, 34);
		panel.add(new JScrollPane(table));
		this.getContentPane().add(panel);
		
		TableColumn visColumn = table.getColumnModel().getColumn(11);
		visibilityBox = new JComboBox();
		addVisibItem(Messages.getString("NoneSpecified"), Vis.UNKNOWN);
		addVisibItem(Messages.getString("Intensified"), Vis.INTEN);
		addVisibItem(Messages.getString("Unintensified"), Vis.UNINTEN);
		addVisibItem(Messages.getString("PartiallyObscured"), Vis.PARTOBS);
		visColumn.setCellEditor(new DefaultCellEditor(visibilityBox));
	}

	private class SectorTable extends AbstractTableModel {

		private String[] headings = { "Sector", "Colour", "Character", "Group", "Sequence", "Period",
				"Start", "End", "Radius", "Height", "Range", "Visibility", "Exhibition", "Category" };

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
			return ((col > 0) && (row > 0));
		}

		public Class getColumnClass(int col) {
			switch (col) {
			case 1:
				return Color.class;
			case 11:
				return Vis.class;
			case 12:
				return Exh.class;
			case 13:
				return Lit.class;
			default:
				return String.class;
			}
		}

		public Object getValueAt(int row, int col) {
			if (col == 0)
				return row;
			else
				return dlg.mark.getLightAtt(col-1, row);
		}

		public void setValueAt(Object value, int row, int col) {
			switch (col) {
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 13:
				dlg.mark.setLightAtt(col-1, row, dlg.mark.validDecimal((String)value));
				break;
			case 11:
				for (Vis vis : visibilities.keySet()) {
					String str = visibilities.get(vis);
					if (str.equals(value))
						dlg.mark.setLightAtt(Att.VIS, row, vis);
				}
				break;
			default:
				dlg.mark.setLightAtt(col-1, row, value);
			}
		}
	}

	public int getSectorCount() {
		return model.getRowCount();
	}

	public void addSector(int idx) {
		dlg.mark.addLight(idx);
		table.setSize(760, ((table.getRowCount() * 16) + 18));
		if (table.getRowCount() > 3) {
			this.setSize(800, ((table.getRowCount() * 16) + 40));
		} else {
			this.setSize(800, 100);
		}
	}

	public void deleteSector(int idx) {
		dlg.mark.subLight(idx);
		table.setSize(760, ((table.getRowCount() * 16) + 18));
		if (table.getRowCount() > 3) {
			this.setSize(800, ((table.getRowCount() * 16) + 40));
		} else {
			this.setSize(800, 100);
		}
	}

	private void addVisibItem(String str, Vis vis) {
		visibilities.put(vis, str);
		visibilityBox.addItem(str);
	}

}
