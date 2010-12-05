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

public class PanelStbd extends JPanel {

	private ButtonGroup regionButtons = null;
	private JRadioButton regionAButton = null;
	private JRadioButton regionBButton = null;
	private ButtonGroup shapeButtons = null;
	private JRadioButton pillarButton = null;
	private JRadioButton sparButton = null;
	private JRadioButton coneButton = null;
	private JRadioButton floatButton = null;
	private JRadioButton beaconButton = null;
	private JRadioButton towerButton = null;
	private JRadioButton perchButton = null;
	
	public PanelStbd() {
		super();
		initialize();
	}

	private void initialize() {
         this.setLayout(null);
         this.add(getRegionAButton(), null);
         this.add(getRegionBButton(), null);
         this.add(getPillarButton(), null);
         this.add(getSparButton(), null);
         this.add(getConeButton(), null);
         this.add(getFloatButton(), null);
         this.add(getBeaconButton(), null);
         this.add(getTowerButton(), null);
         this.add(getPerchButton(), null);
         
         regionButtons = new ButtonGroup();
         regionButtons.add(regionAButton);
         regionButtons.add(regionBButton);
			ActionListener alRegion = new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (regionAButton.isSelected()) {
						regionAButton.setEnabled(false);
					} else { 
						regionAButton.setEnabled(true);
					}
					if (regionBButton.isSelected()) {
						regionBButton.setEnabled(false);
					} else { 
						regionBButton.setEnabled(true);
					}
				}
			};
			regionAButton.addActionListener(alRegion);
			regionBButton.addActionListener(alRegion);
         
         shapeButtons = new ButtonGroup();
         shapeButtons.add(pillarButton);
         shapeButtons.add(sparButton);
         shapeButtons.add(coneButton);
         shapeButtons.add(floatButton);
         shapeButtons.add(beaconButton);
         shapeButtons.add(towerButton);
         shapeButtons.add(perchButton);
			ActionListener alShape = new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					pillarButton.setEnabled(!pillarButton.isSelected());
					sparButton.setEnabled(!sparButton.isSelected());
					coneButton.setEnabled(!coneButton.isSelected());
					floatButton.setEnabled(!floatButton.isSelected());
					beaconButton.setEnabled(!beaconButton.isSelected());
					towerButton.setEnabled(!towerButton.isSelected());
					perchButton.setEnabled(!perchButton.isSelected());
				}
			};
			pillarButton.addActionListener(alShape);
			sparButton.addActionListener(alShape);
			coneButton.addActionListener(alShape);
			floatButton.addActionListener(alShape);
			beaconButton.addActionListener(alShape);
			towerButton.addActionListener(alShape);
			perchButton.addActionListener(alShape);
	}

	private JRadioButton getRegionAButton() {
		if (regionAButton == null) {
			regionAButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/RegionAButton.png")));
			regionAButton.setBounds(new Rectangle(10, 0, 65, 30));
		}
		return regionAButton;
	}

	private JRadioButton getRegionBButton() {
		if (regionBButton == null) {
			regionBButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/RegionBButton.png")));
			regionBButton.setBounds(new Rectangle(10, 30, 65, 30));
		}
		return regionBButton;
	}

	private JRadioButton getPillarButton() {
		if (pillarButton == null) {
			pillarButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("PillarButton"))));
			pillarButton.setBounds(new Rectangle(0, 64, 90, 32));
		}
		return pillarButton;
	}

	private JRadioButton getSparButton() {
		if (sparButton == null) {
			sparButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("SparButton"))));
			sparButton.setBounds(new Rectangle(0, 96, 90, 32));
		}
		return sparButton;
	}

	private JRadioButton getConeButton() {
		if (coneButton == null) {
			coneButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("ConeButton"))));
			coneButton.setBounds(new Rectangle(0, 128, 90, 32));
		}
		return coneButton;
	}

	private JRadioButton getFloatButton() {
		if (floatButton == null) {
			floatButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("FloatButton"))));
			floatButton.setBounds(new Rectangle(90, 0, 90, 32));
		}
		return floatButton;
	}

	private JRadioButton getBeaconButton() {
		if (beaconButton == null) {
			beaconButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("BeaconButton"))));
			beaconButton.setBounds(new Rectangle(90, 32, 90, 32));
		}
		return beaconButton;
	}

	private JRadioButton getTowerButton() {
		if (towerButton == null) {
			towerButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("TowerButton"))));
			towerButton.setBounds(new Rectangle(90, 64, 90, 32));
		}
		return towerButton;
	}

	private JRadioButton getPerchButton() {
		if (perchButton == null) {
			perchButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("PerchSButton"))));
			perchButton.setBounds(new Rectangle(90, 96, 90, 32));
		}
		return perchButton;
	}


}
