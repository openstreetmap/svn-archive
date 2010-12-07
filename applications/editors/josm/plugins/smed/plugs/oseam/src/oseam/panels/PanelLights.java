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

public class PanelLights extends JPanel {

	private ButtonGroup catButtons = null;
	private JRadioButton houseButton = null;
	private JRadioButton majorButton = null;
	private JRadioButton minorButton = null;
	private JRadioButton vesselButton = null;
	private JRadioButton floatButton = null;
	
	public PanelLights() {
		super();
		initialize();
	}

	private void initialize() {
         this.setLayout(null);
         this.add(getHouseButton(), null);
         this.add(getMajorButton(), null);
         this.add(getMinorButton(), null);
         this.add(getVesselButton(), null);
         this.add(getFloatButton(), null);
         catButtons = new ButtonGroup();
         catButtons.add(houseButton);
         catButtons.add(majorButton);
         catButtons.add(minorButton);
         catButtons.add(vesselButton);
         catButtons.add(floatButton);
			ActionListener alCat = new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					houseButton.setEnabled(!houseButton.isSelected());
					majorButton.setEnabled(!majorButton.isSelected());
					minorButton.setEnabled(!minorButton.isSelected());
					vesselButton.setEnabled(!vesselButton.isSelected());
					floatButton.setEnabled(!floatButton.isSelected());
				}
			};
			houseButton.addActionListener(alCat);
			majorButton.addActionListener(alCat);
			minorButton.addActionListener(alCat);
			vesselButton.addActionListener(alCat);
			floatButton.addActionListener(alCat);
	}

	private JRadioButton getHouseButton() {
		if (houseButton == null) {
			houseButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("LighthouseButton"))));
			houseButton.setBounds(new Rectangle(0, 0, 105, 32));
		}
		return houseButton;
	}

	private JRadioButton getMajorButton() {
		if (majorButton == null) {
			majorButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("LightMajorButton"))));
			majorButton.setBounds(new Rectangle(0, 32, 105, 32));
		}
		return majorButton;
	}

	private JRadioButton getMinorButton() {
		if (minorButton == null) {
			minorButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("LightMinorButton"))));
			minorButton.setBounds(new Rectangle(0, 64, 105, 32));
		}
		return minorButton;
	}

	private JRadioButton getVesselButton() {
		if (vesselButton == null) {
			vesselButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("LightVesselButton"))));
			vesselButton.setBounds(new Rectangle(0, 96, 105, 32));
		}
		return vesselButton;
	}

	private JRadioButton getFloatButton() {
		if (floatButton == null) {
			floatButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("LightFloatButton"))));
			floatButton.setBounds(new Rectangle(0, 128, 105, 32));
		}
		return floatButton;
	}

}
