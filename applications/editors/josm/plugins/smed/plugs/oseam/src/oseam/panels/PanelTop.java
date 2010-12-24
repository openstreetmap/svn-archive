package oseam.panels;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JButton;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;

import java.awt.Cursor;
import java.awt.event.ActionListener;

public class PanelTop extends JPanel {

	private OSeaMAction dlg;
	public PanelCol panelCol = null;
	private ButtonGroup topButtons = null;
	public JRadioButton noTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/NoTopButton.png")));
	public JRadioButton canTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CanTopButton.png")));
	public JRadioButton coneTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeTopButton.png")));
	public JRadioButton sphereTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereTopButton.png")));
	public JRadioButton XTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/XTopButton.png")));
	public JRadioButton northTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/NorthTopButton.png")));
	public JRadioButton southTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SouthTopButton.png")));
	public JRadioButton eastTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/EastTopButton.png")));
	public JRadioButton westTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/WestTopButton.png")));
	public JRadioButton spheres2TopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/Spheres2TopButton.png")));
	public JRadioButton boardDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BoardDayButton.png")));
	public JRadioButton diamondDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/DiamondDayButton.png")));
	public JRadioButton triangleDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TriangleDayButton.png")));
	public JRadioButton triangleInvDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TriangleInvDayButton.png")));
	public JRadioButton squareDayButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SquareDayButton.png")));
	public JRadioButton mooringTopButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/MooringTopButton.png")));
	ActionListener alTop = null;

	public PanelTop(OSeaMAction dia) {
		dlg = dia;
		panelCol = new PanelCol(dlg);
		panelCol.setBounds(new Rectangle(0, 0, 34, 160));
		this.setLayout(null);
		this.add(panelCol, null);
		this.add(getButton(noTopButton, 40, 5, 27, 27, "NoTopTip"), null);
		this.add(getButton(canTopButton, 70, 5, 27, 27, "CanTopTip"), null);
		this.add(getButton(coneTopButton, 100, 5, 27, 27, "ConeTopTip"), null);
		this.add(getButton(sphereTopButton, 130, 5, 27, 27, "SphereTopTip"), null);
		this.add(getButton(XTopButton, 160, 5, 27, 27, "XTopTip"), null);
		this.add(getButton(northTopButton, 40, 35, 27, 27, "NorthTopTip"), null);
		this.add(getButton(southTopButton, 70, 35, 27, 27, "SouthTopTip"), null);
		this.add(getButton(eastTopButton, 100, 35, 27, 27, "EastTopTip"), null);
		this.add(getButton(westTopButton, 130, 35, 27, 27, "WestTopTip"), null);
		this.add(getButton(spheres2TopButton, 160, 35, 27, 27, "Spheres2TopTip"), null);
		this.add(getButton(boardDayButton, 40, 65, 27, 27, "BoardDayTip"), null);
		this.add(getButton(diamondDayButton, 70, 65, 27, 27, "DiamondDayTip"), null);
		this.add(getButton(triangleDayButton, 100, 65, 27, 27, "TriangleDayTip"), null);
		this.add(getButton(triangleInvDayButton, 130, 65, 27, 27, "TriangleInvDayTip"), null);
		this.add(getButton(squareDayButton, 160, 65, 27, 27, "SquareDayTip"), null);
		this.add(getButton(mooringTopButton, 40, 95, 27, 27, "MooringTopTip"), null);
		topButtons = new ButtonGroup();
		topButtons.add(noTopButton);
		topButtons.add(canTopButton);
		topButtons.add(coneTopButton);
		topButtons.add(sphereTopButton);
		topButtons.add(XTopButton);
		topButtons.add(northTopButton);
		topButtons.add(southTopButton);
		topButtons.add(eastTopButton);
		topButtons.add(westTopButton);
		topButtons.add(spheres2TopButton);
		topButtons.add(boardDayButton);
		topButtons.add(diamondDayButton);
		topButtons.add(triangleDayButton);
		topButtons.add(triangleInvDayButton);
		topButtons.add(squareDayButton);
		topButtons.add(mooringTopButton);
		alTop = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				noTopButton.setBorderPainted(noTopButton.isSelected());
				canTopButton.setBorderPainted(canTopButton.isSelected());
				coneTopButton.setBorderPainted(coneTopButton.isSelected());
				sphereTopButton.setBorderPainted(sphereTopButton.isSelected());
				XTopButton.setBorderPainted(XTopButton.isSelected());
				northTopButton.setBorderPainted(northTopButton.isSelected());
				southTopButton.setBorderPainted(southTopButton.isSelected());
				eastTopButton.setBorderPainted(eastTopButton.isSelected());
				westTopButton.setBorderPainted(westTopButton.isSelected());
				spheres2TopButton.setBorderPainted(spheres2TopButton.isSelected());
				boardDayButton.setBorderPainted(boardDayButton.isSelected());
				diamondDayButton.setBorderPainted(diamondDayButton.isSelected());
				triangleDayButton.setBorderPainted(triangleDayButton.isSelected());
				triangleInvDayButton.setBorderPainted(triangleInvDayButton.isSelected());
				squareDayButton.setBorderPainted(squareDayButton.isSelected());
				mooringTopButton.setBorderPainted(mooringTopButton.isSelected());
			}
		};
		noTopButton.addActionListener(alTop);
		canTopButton.addActionListener(alTop);
		coneTopButton.addActionListener(alTop);
		sphereTopButton.addActionListener(alTop);
		XTopButton.addActionListener(alTop);
		northTopButton.addActionListener(alTop);
		southTopButton.addActionListener(alTop);
		eastTopButton.addActionListener(alTop);
		westTopButton.addActionListener(alTop);
		spheres2TopButton.addActionListener(alTop);
		boardDayButton.addActionListener(alTop);
		diamondDayButton.addActionListener(alTop);
		triangleDayButton.addActionListener(alTop);
		triangleInvDayButton.addActionListener(alTop);
		squareDayButton.addActionListener(alTop);
		mooringTopButton.addActionListener(alTop);
	}

	public void clearSelections() {
		topButtons.clearSelection();
		alTop.actionPerformed(null);
		panelCol.clearSelections();
	}

	private JRadioButton getButton(JRadioButton button, int x, int y, int w, int h, String tip) {
			button.setBounds(new Rectangle(x, y, w, h));
			button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			button.setToolTipText(Messages.getString(tip));
		return button;
	}

}
