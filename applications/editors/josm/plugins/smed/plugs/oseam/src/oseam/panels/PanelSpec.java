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

public class PanelSpec extends JPanel {

	private ButtonGroup shapeButtons = null;
	private JRadioButton pillarButton = null;
	private JRadioButton sparButton = null;
	private JRadioButton canButton = null;
	private JRadioButton coneButton = null;
	private JRadioButton sphereButton = null;
	private JRadioButton barrelButton = null;
	private JRadioButton superButton = null;
	private JRadioButton floatButton = null;
	private JRadioButton beaconButton = null;
	private JRadioButton towerButton = null;

	public PanelSpec() {
		super();
		initialize();
	}

	private void initialize() {
		this.setLayout(null);

		this.add(getPillarButton(), null);
		this.add(getSparButton(), null);
		this.add(getCanButton(), null);
		this.add(getConeButton(), null);
		this.add(getSphereButton(), null);
		this.add(getBarrelButton(), null);
		this.add(getSuperButton(), null);
		this.add(getFloatButton(), null);
		this.add(getBeaconButton(), null);
		this.add(getTowerButton(), null);
		shapeButtons = new ButtonGroup();
		shapeButtons.add(pillarButton);
		shapeButtons.add(sparButton);
		shapeButtons.add(canButton);
		shapeButtons.add(coneButton);
		shapeButtons.add(sphereButton);
		shapeButtons.add(barrelButton);
		shapeButtons.add(superButton);
		shapeButtons.add(floatButton);
		shapeButtons.add(beaconButton);
		shapeButtons.add(towerButton);
		ActionListener alShape = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				pillarButton.setEnabled(!pillarButton.isSelected());
				sparButton.setEnabled(!sparButton.isSelected());
				canButton.setEnabled(!canButton.isSelected());
				coneButton.setEnabled(!coneButton.isSelected());
				sphereButton.setEnabled(!sphereButton.isSelected());
				barrelButton.setEnabled(!barrelButton.isSelected());
				superButton.setEnabled(!superButton.isSelected());
				floatButton.setEnabled(!floatButton.isSelected());
				beaconButton.setEnabled(!beaconButton.isSelected());
				towerButton.setEnabled(!towerButton.isSelected());
			}
		};
		pillarButton.addActionListener(alShape);
		sparButton.addActionListener(alShape);
		canButton.addActionListener(alShape);
		coneButton.addActionListener(alShape);
		sphereButton.addActionListener(alShape);
		barrelButton.addActionListener(alShape);
		superButton.addActionListener(alShape);
		floatButton.addActionListener(alShape);
		beaconButton.addActionListener(alShape);
		towerButton.addActionListener(alShape);
	}

	private JRadioButton getPillarButton() {
		if (pillarButton == null) {
			pillarButton = new JRadioButton(new ImageIcon(getClass()
					.getResource(Messages.getString("PillarButton"))));
			pillarButton.setBounds(new Rectangle(105, 0, 90, 32));
		}
		return pillarButton;
	}

	private JRadioButton getSparButton() {
		if (sparButton == null) {
			sparButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("SparButton"))));
			sparButton.setBounds(new Rectangle(105, 32, 90, 32));
		}
		return sparButton;
	}

	private JRadioButton getCanButton() {
		if (canButton == null) {
			canButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("CanButton"))));
			canButton.setBounds(new Rectangle(105, 64, 90, 32));
		}
		return canButton;
	}

	private JRadioButton getConeButton() {
		if (coneButton == null) {
			coneButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("ConeButton"))));
			coneButton.setBounds(new Rectangle(105, 96, 90, 32));
		}
		return coneButton;
	}

	private JRadioButton getSphereButton() {
		if (sphereButton == null) {
			sphereButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("SphereButton"))));
			sphereButton.setBounds(new Rectangle(105, 128, 90, 32));
		}
		return sphereButton;
	}

	private JRadioButton getBarrelButton() {
		if (barrelButton == null) {
			barrelButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("BarrelButton"))));
			barrelButton.setBounds(new Rectangle(195, 0, 90, 32));
		}
		return barrelButton;
	}

	private JRadioButton getSuperButton() {
		if (superButton == null) {
			superButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("SuperButton"))));
			superButton.setBounds(new Rectangle(195, 32, 90, 32));
		}
		return superButton;
	}

	private JRadioButton getFloatButton() {
		if (floatButton == null) {
			floatButton = new JRadioButton(new ImageIcon(getClass()
					.getResource(Messages.getString("FloatButton"))));
			floatButton.setBounds(new Rectangle(195, 64, 90, 32));
		}
		return floatButton;
	}

	private JRadioButton getBeaconButton() {
		if (beaconButton == null) {
			beaconButton = new JRadioButton(new ImageIcon(getClass()
					.getResource(Messages.getString("BeaconButton"))));
			beaconButton.setBounds(new Rectangle(195, 96, 90, 32));
		}
		return beaconButton;
	}

	private JRadioButton getTowerButton() {
		if (towerButton == null) {
			towerButton = new JRadioButton(new ImageIcon(getClass()
					.getResource(Messages.getString("TowerButton"))));
			towerButton.setBounds(new Rectangle(195, 128, 90, 32));
		}
		return towerButton;
	}

}
