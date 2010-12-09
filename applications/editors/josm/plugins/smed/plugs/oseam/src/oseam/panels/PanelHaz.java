package oseam.panels;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import oseam.Messages;

import java.awt.Cursor;
import java.awt.event.ActionListener;

public class PanelHaz extends JPanel {

	private ButtonGroup catButtons = null;
	private JRadioButton northButton = null;
	private JRadioButton southButton = null;
	private JRadioButton eastButton = null;
	private JRadioButton westButton = null;
	private JRadioButton isolButton = null;

	private ButtonGroup shapeButtons = null;
	private JRadioButton pillarButton = null;
	private JRadioButton sparButton = null;
	private JRadioButton floatButton = null;
	private JRadioButton beaconButton = null;
	private JRadioButton towerButton = null;

	public PanelHaz() {
		super();
		initialize();
	}

	private void initialize() {
		this.setLayout(null);
		this.add(getNothButton(), null);
		this.add(getSouthButton(), null);
		this.add(getEastButton(), null);
		this.add(getWestButton(), null);
		this.add(getIsolButton(), null);
		catButtons = new ButtonGroup();
		catButtons.add(northButton);
		catButtons.add(southButton);
		catButtons.add(eastButton);
		catButtons.add(westButton);
		catButtons.add(isolButton);
		ActionListener alCat = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				northButton.setEnabled(!northButton.isSelected());
				southButton.setEnabled(!southButton.isSelected());
				eastButton.setEnabled(!eastButton.isSelected());
				westButton.setEnabled(!westButton.isSelected());
				isolButton.setEnabled(!isolButton.isSelected());
			}
		};
		northButton.addActionListener(alCat);
		southButton.addActionListener(alCat);
		eastButton.addActionListener(alCat);
		westButton.addActionListener(alCat);
		isolButton.addActionListener(alCat);

		this.add(getPillarButton(), null);
		this.add(getSparButton(), null);
		this.add(getFloatButton(), null);
		this.add(getBeaconButton(), null);
		this.add(getTowerButton(), null);
		shapeButtons = new ButtonGroup();
		shapeButtons.add(pillarButton);
		shapeButtons.add(sparButton);
		shapeButtons.add(floatButton);
		shapeButtons.add(beaconButton);
		shapeButtons.add(towerButton);
		ActionListener alShape = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				pillarButton.setEnabled(!pillarButton.isSelected());
				sparButton.setEnabled(!sparButton.isSelected());
				floatButton.setEnabled(!floatButton.isSelected());
				beaconButton.setEnabled(!beaconButton.isSelected());
				towerButton.setEnabled(!towerButton.isSelected());
			}
		};
		pillarButton.addActionListener(alShape);
		sparButton.addActionListener(alShape);
		floatButton.addActionListener(alShape);
		beaconButton.addActionListener(alShape);
		towerButton.addActionListener(alShape);
	}

	private JRadioButton getNothButton() {
		if (northButton == null) {
			northButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/CardNButton.png")));
			northButton.setBounds(new Rectangle(0, 0, 55, 32));
			northButton.setToolTipText(Messages.getString("NorthTip"));
		}
		return northButton;
	}

	private JRadioButton getSouthButton() {
		if (southButton == null) {
			southButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/CardSButton.png")));
			southButton.setBounds(new Rectangle(0, 32, 55, 32));
			southButton.setToolTipText(Messages.getString("SouthTip"));
		}
		return southButton;
	}

	private JRadioButton getEastButton() {
		if (eastButton == null) {
			eastButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/CardEButton.png")));
			eastButton.setBounds(new Rectangle(0, 64, 55, 32));
			eastButton.setToolTipText(Messages.getString("EastTip"));
		}
		return eastButton;
	}

	private JRadioButton getWestButton() {
		if (westButton == null) {
			westButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/CardWButton.png")));
			westButton.setBounds(new Rectangle(0, 96, 55, 32));
			westButton.setToolTipText(Messages.getString("WestTip"));
		}
		return westButton;
	}

	private JRadioButton getIsolButton() {
		if (isolButton == null) {
			isolButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/IsolButton.png")));
			isolButton.setBounds(new Rectangle(0, 128, 55, 32));
			isolButton.setToolTipText(Messages.getString("IsolTip"));
		}
		return isolButton;
	}

	private JRadioButton getPillarButton() {
		if (pillarButton == null) {
			pillarButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/PillarButton.png")));
			pillarButton.setBounds(new Rectangle(55, 0, 90, 32));
			pillarButton.setToolTipText(Messages.getString("PillarTip"));
		}
		return pillarButton;
	}

	private JRadioButton getSparButton() {
		if (sparButton == null) {
			sparButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/SparButton.png")));
			sparButton.setBounds(new Rectangle(55, 32, 90, 32));
			sparButton.setToolTipText(Messages.getString("SparTip"));
		}
		return sparButton;
	}

	private JRadioButton getFloatButton() {
		if (floatButton == null) {
			floatButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/FloatButton.png")));
			floatButton.setBounds(new Rectangle(55, 64, 90, 32));
			floatButton.setToolTipText(Messages.getString("FloatTip"));
		}
		return floatButton;
	}

	private JRadioButton getBeaconButton() {
		if (beaconButton == null) {
			beaconButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/BeaconButton.png")));
			beaconButton.setBounds(new Rectangle(55, 96, 90, 32));
			beaconButton.setToolTipText(Messages.getString("BeaconTip"));
		}
		return beaconButton;
	}

	private JRadioButton getTowerButton() {
		if (towerButton == null) {
			towerButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/TowerButton.png")));
			towerButton.setBounds(new Rectangle(55, 128, 90, 32));
			towerButton.setToolTipText(Messages.getString("TowerTip"));
		}
		return towerButton;
	}

}
