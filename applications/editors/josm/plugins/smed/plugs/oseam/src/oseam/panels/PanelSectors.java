package oseam.panels;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;

import oseam.seamarks.Light;

public class PanelSectors extends JFrame {
	
	public JPanel panel;
	public JButton minusButton;
	public JButton plusButton;
	public JTable table;
	
	public PanelSectors(Light light) {
		super("Sector Table");
		panel = new JPanel();
		this.setSize(700, 100);
		panel.setBounds(0, 0, 700, 512);
		this.getContentPane().add(panel);
		minusButton = new JButton(new ImageIcon(getClass().getResource("/images/MinusButton.png")));
		minusButton.setBounds(0, 0, 32, 34);
		plusButton = new JButton(new ImageIcon(getClass().getResource("/images/PlusButton.png")));
		plusButton.setBounds(0, 34, 32, 34);
		table = new JTable(light);
		JScrollPane tablePane = new JScrollPane(table);
		tablePane.setBounds(40, 0, 660, 34);
		panel.setLayout(null);
		panel.add(minusButton);
		panel.add(plusButton);
		panel.add(tablePane);
	}
}
