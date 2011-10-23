package oseam.panels;

import java.awt.event.*;
import javax.swing.*;

import oseam.seamarks.Light;

public class PanelSectors extends JFrame {

	public JPanel panel;
	public JButton minusButton;
	public JButton plusButton;
	public JTable table;
	public Light light;
	private JScrollPane tablePane;
	private ActionListener alMinusButton;
	private ActionListener alPlusButton;

	public PanelSectors(Light lit) {
		super("Sector Table");
		light = lit;
		panel = new JPanel();
		this.setSize(700, 100);
		panel.setBounds(0, 0, 700, 512);
		this.getContentPane().add(panel);
		table = new JTable(light);
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
		return light.getRowCount();
	}

	public void addSector(int idx) {
		light.addSector(idx);
		tablePane.setSize(660, ((light.getRowCount() * 16) + 18));
		if (light.getRowCount() > 3) {
			this.setSize(700, ((light.getRowCount() * 16) + 40));
		} else {
			this.setSize(700, 100);
		}
		light.fireTableRowsInserted(idx, idx);
	}

	public void deleteSector(int idx) {
		light.deleteSector(idx);
		tablePane.setSize(660, ((light.getRowCount() * 16) + 18));
		if (light.getRowCount() > 3) {
			this.setSize(700, ((light.getRowCount() * 16) + 40));
		} else {
			this.setSize(700, 100);
		}
		light.fireTableRowsDeleted(idx, idx);
	}

}
