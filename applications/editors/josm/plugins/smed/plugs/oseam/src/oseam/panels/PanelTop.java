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

import java.awt.Cursor;
import java.awt.event.ActionListener;

public class PanelTop extends JPanel {

	private PanelCol panelCol = null;
	private ButtonGroup topButtons = null;
	private JRadioButton noTopButton = null;
	private JRadioButton canTopButton = null;
	private JRadioButton coneTopButton = null;
	private JRadioButton sphereTopButton = null;
	private JRadioButton XTopButton = null;
	private JRadioButton northTopButton = null;
	private JRadioButton southTopButton = null;
	private JRadioButton eastTopButton = null;
	private JRadioButton westTopButton = null;
	private JRadioButton spheres2TopButton = null;
	private JRadioButton boardDayButton = null;
	private JRadioButton diamondDayButton = null;
	private JRadioButton triangleDayButton = null;
	private JRadioButton triangleInvDayButton = null;
	private JRadioButton squareDayButton = null;
	private JRadioButton mooringTopButton = null;

	public PanelTop() {
		super();
		panelCol = new PanelCol();
		panelCol.setBounds(new Rectangle(0, 0, 34, 160));
		initialize();
	}

	private void initialize() {
		this.setLayout(null);
		this.add(panelCol, null);
		this.add(getNoTopButton(), null);
		this.add(getCanTopButton(), null);
		this.add(getConeTopButton(), null);
		this.add(getSphereTopButton(), null);
		this.add(getXTopButton(), null);
		this.add(getNorthTopButton(), null);
		this.add(getSouthTopButton(), null);
		this.add(getEastTopButton(), null);
		this.add(getWestTopButton(), null);
		this.add(getSpheres2TopButton(), null);
		this.add(getBoardDayButton(), null);
		this.add(getDiamondDayButton(), null);
		this.add(getTriangleDayButton(), null);
		this.add(getTriangleInvDayButton(), null);
		this.add(getSquareDayButton(), null);
		this.add(getMooringTopButton(), null);
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
		ActionListener alTop = new ActionListener() {
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

	private JRadioButton getNoTopButton() {
		if (noTopButton == null) {
			noTopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/NoTopButton.png")));
			noTopButton.setBounds(new Rectangle(40, 5, 27, 27));
	        noTopButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			noTopButton.setToolTipText(Messages.getString("NoTopTip"));
		}
		return noTopButton;
	}

	private JRadioButton getCanTopButton() {
		if (canTopButton == null) {
			canTopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/CanTopButton.png")));
			canTopButton.setBounds(new Rectangle(70, 5, 27, 27));
	        canTopButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			canTopButton.setToolTipText(Messages.getString("CanTopTip"));
		}
		return canTopButton;
	}

	private JRadioButton getConeTopButton() {
		if (coneTopButton == null) {
			coneTopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/ConeTopButton.png")));
			coneTopButton.setBounds(new Rectangle(100, 5, 27, 27));
			coneTopButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			coneTopButton.setToolTipText(Messages.getString("ConeTopTip"));
		}
		return coneTopButton;
	}

	private JRadioButton getSphereTopButton() {
		if (sphereTopButton == null) {
			sphereTopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/SphereTopButton.png")));
			sphereTopButton.setBounds(new Rectangle(130, 5, 27, 27));
			sphereTopButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			sphereTopButton.setToolTipText(Messages.getString("SphereTopTip"));
		}
		return sphereTopButton;
	}

	private JRadioButton getXTopButton() {
		if (XTopButton == null) {
			XTopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/XTopButton.png")));
			XTopButton.setBounds(new Rectangle(160, 5, 27, 27));
			XTopButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			XTopButton.setToolTipText(Messages.getString("XTopTip"));
		}
		return XTopButton;
	}

	private JRadioButton getNorthTopButton() {
		if (northTopButton == null) {
			northTopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/NorthTopButton.png")));
			northTopButton.setBounds(new Rectangle(40, 35, 27, 27));
	        northTopButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			northTopButton.setToolTipText(Messages.getString("NorthTopTip"));
		}
		return northTopButton;
	}

	private JRadioButton getSouthTopButton() {
		if (southTopButton == null) {
			southTopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/SouthTopButton.png")));
			southTopButton.setBounds(new Rectangle(70, 35, 27, 27));
	        southTopButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			southTopButton.setToolTipText(Messages.getString("SouthTopTip"));
		}
		return southTopButton;
	}

	private JRadioButton getEastTopButton() {
		if (eastTopButton == null) {
			eastTopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/EastTopButton.png")));
			eastTopButton.setBounds(new Rectangle(100, 35, 27, 27));
			eastTopButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			eastTopButton.setToolTipText(Messages.getString("EastTopTip"));
		}
		return eastTopButton;
	}

	private JRadioButton getWestTopButton() {
		if (westTopButton == null) {
			westTopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/WestTopButton.png")));
			westTopButton.setBounds(new Rectangle(130, 35, 27, 27));
			westTopButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			westTopButton.setToolTipText(Messages.getString("WestTopTip"));
		}
		return westTopButton;
	}

	private JRadioButton getSpheres2TopButton() {
		if (spheres2TopButton == null) {
			spheres2TopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/Spheres2TopButton.png")));
			spheres2TopButton.setBounds(new Rectangle(160, 35, 27, 27));
			spheres2TopButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			spheres2TopButton.setToolTipText(Messages.getString("Spheres2TopTip"));
		}
		return spheres2TopButton;
	}

	private JRadioButton getBoardDayButton() {
		if (boardDayButton == null) {
			boardDayButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/BoardDayButton.png")));
			boardDayButton.setBounds(new Rectangle(40, 65, 27, 27));
			boardDayButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			boardDayButton.setToolTipText(Messages.getString("BoardDayTip"));
		}
		return boardDayButton;
	}

	private JRadioButton getDiamondDayButton() {
		if (diamondDayButton == null) {
			diamondDayButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/DiamondDayButton.png")));
			diamondDayButton.setBounds(new Rectangle(70, 65, 27, 27));
			diamondDayButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			diamondDayButton.setToolTipText(Messages.getString("DiamondDayTip"));
		}
		return diamondDayButton;
	}

	private JRadioButton getTriangleDayButton() {
		if (triangleDayButton == null) {
			triangleDayButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/TriangleDayButton.png")));
			triangleDayButton.setBounds(new Rectangle(100, 65, 27, 27));
			triangleDayButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			triangleDayButton.setToolTipText(Messages.getString("TriangleDayTip"));
		}
		return triangleDayButton;
	}

	private JRadioButton getTriangleInvDayButton() {
		if (triangleInvDayButton == null) {
			triangleInvDayButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/TriangleInvDayButton.png")));
			triangleInvDayButton.setBounds(new Rectangle(130, 65, 27, 27));
			triangleInvDayButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			triangleInvDayButton.setToolTipText(Messages.getString("TriangleInvDayTip"));
		}
		return triangleInvDayButton;
	}

	private JRadioButton getSquareDayButton() {
		if (squareDayButton == null) {
			squareDayButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/SquareDayButton.png")));
			squareDayButton.setBounds(new Rectangle(160, 65, 27, 27));
			squareDayButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			squareDayButton.setToolTipText(Messages.getString("SquareDayTip"));
		}
		return squareDayButton;
	}

	private JRadioButton getMooringTopButton() {
		if (mooringTopButton == null) {
			mooringTopButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/MooringTopButton.png")));
			mooringTopButton.setBounds(new Rectangle(40, 95, 27, 27));
			mooringTopButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			mooringTopButton.setToolTipText(Messages.getString("MooringTopTip"));
		}
		return mooringTopButton;
	}

}
