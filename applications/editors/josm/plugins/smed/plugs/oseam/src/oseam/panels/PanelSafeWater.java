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

public class PanelSafeWater extends JPanel {

	private ButtonGroup shapeButtons = null;
	private JRadioButton pillarButton = null;
	private JRadioButton sparButton = null;
	private JRadioButton sphereButton = null;
	private JRadioButton barrelButton = null;
	private JRadioButton floatButton = null;
	
	public PanelSafeWater() {
		super();
		initialize();
	}

	private void initialize() {
         this.setLayout(null);
         this.add(getPillarButton(), null);
         this.add(getSparButton(), null);
         this.add(getSphereButton(), null);
         this.add(getBarrelButton(), null);
         this.add(getFloatButton(), null);
         
         shapeButtons = new ButtonGroup();
         shapeButtons.add(pillarButton);
         shapeButtons.add(sparButton);
         shapeButtons.add(sphereButton);
         shapeButtons.add(barrelButton);
         shapeButtons.add(floatButton);
			ActionListener alShape = new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					pillarButton.setEnabled(!pillarButton.isSelected());
					sparButton.setEnabled(!sparButton.isSelected());
					sphereButton.setEnabled(!sphereButton.isSelected());
					barrelButton.setEnabled(!barrelButton.isSelected());
					floatButton.setEnabled(!floatButton.isSelected());
				}
			};
			pillarButton.addActionListener(alShape);
			sparButton.addActionListener(alShape);
			sphereButton.addActionListener(alShape);
			barrelButton.addActionListener(alShape);
			floatButton.addActionListener(alShape);
	}

	private JRadioButton getPillarButton() {
		if (pillarButton == null) {
			pillarButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("PillarButton"))));
			pillarButton.setBounds(new Rectangle(0, 0, 90, 32));
		}
		return pillarButton;
	}

	private JRadioButton getSparButton() {
		if (sparButton == null) {
			sparButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("SparButton"))));
			sparButton.setBounds(new Rectangle(0, 32, 90, 32));
		}
		return sparButton;
	}

	private JRadioButton getSphereButton() {
		if (sphereButton == null) {
			sphereButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("SphereButton"))));
			sphereButton.setBounds(new Rectangle(0, 64, 90, 32));
		}
		return sphereButton;
	}

	private JRadioButton getBarrelButton() {
		if (barrelButton == null) {
			barrelButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("BarrelButton"))));
			barrelButton.setBounds(new Rectangle(0, 96, 90, 32));
		}
		return barrelButton;
	}

	private JRadioButton getFloatButton() {
		if (floatButton == null) {
			floatButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("FloatButton"))));
			floatButton.setBounds(new Rectangle(0, 128, 90, 32));
		}
		return floatButton;
	}

}
