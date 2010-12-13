package oseam.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

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
	private PanelCol panelCol = null;

	public PanelSpec() {
		super();
		panelCol = new PanelCol();
		panelCol.setBounds(new Rectangle(9, 0, 34, 160));
		initialize();
	}

	private void initialize() {
		this.setLayout(null);

		this.add(panelCol, null);
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
				pillarButton.setBorderPainted(pillarButton.isSelected());
				sparButton.setBorderPainted(sparButton.isSelected());
				canButton.setBorderPainted(canButton.isSelected());
				coneButton.setBorderPainted(coneButton.isSelected());
				sphereButton.setBorderPainted(sphereButton.isSelected());
				barrelButton.setBorderPainted(barrelButton.isSelected());
				superButton.setBorderPainted(superButton.isSelected());
				floatButton.setBorderPainted(floatButton.isSelected());
				beaconButton.setBorderPainted(beaconButton.isSelected());
				towerButton.setBorderPainted(towerButton.isSelected());
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
					.getResource("/images/PillarButton.png")));
			pillarButton.setBounds(new Rectangle(55, 0, 34, 32));
	        pillarButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			pillarButton.setToolTipText(Messages.getString("PillarTip"));
		}
		return pillarButton;
	}

	private JRadioButton getSparButton() {
		if (sparButton == null) {
			sparButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/SparButton.png")));
			sparButton.setBounds(new Rectangle(55, 32, 34, 32));
	        sparButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			sparButton.setToolTipText(Messages.getString("SparTip"));
		}
		return sparButton;
	}

	private JRadioButton getCanButton() {
		if (canButton == null) {
			canButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/CanButton.png")));
			canButton.setBounds(new Rectangle(55, 64, 34, 32));
	        canButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			canButton.setToolTipText(Messages.getString("CanTip"));
		}
		return canButton;
	}

	private JRadioButton getConeButton() {
		if (coneButton == null) {
			coneButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/ConeButton.png")));
			coneButton.setBounds(new Rectangle(55, 96, 34, 32));
	        coneButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			coneButton.setToolTipText(Messages.getString("ConeTip"));
		}
		return coneButton;
	}

	private JRadioButton getSphereButton() {
		if (sphereButton == null) {
			sphereButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/SphereButton.png")));
			sphereButton.setBounds(new Rectangle(55, 128, 34, 32));
	        sphereButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			sphereButton.setToolTipText(Messages.getString("SphereTip"));
		}
		return sphereButton;
	}

	private JRadioButton getBarrelButton() {
		if (barrelButton == null) {
			barrelButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/BarrelButton.png")));
			barrelButton.setBounds(new Rectangle(90, 0, 34, 32));
	        barrelButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			barrelButton.setToolTipText(Messages.getString("BarrelTip"));
		}
		return barrelButton;
	}

	private JRadioButton getSuperButton() {
		if (superButton == null) {
			superButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/SuperButton.png")));
			superButton.setBounds(new Rectangle(90, 32, 34, 32));
	        superButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			superButton.setToolTipText(Messages.getString("SuperTip"));
		}
		return superButton;
	}

	private JRadioButton getFloatButton() {
		if (floatButton == null) {
			floatButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/FloatButton.png")));
			floatButton.setBounds(new Rectangle(90, 64, 34, 32));
	        floatButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			floatButton.setToolTipText(Messages.getString("FloatTip"));
		}
		return floatButton;
	}

	private JRadioButton getBeaconButton() {
		if (beaconButton == null) {
			beaconButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/BeaconButton.png")));
			beaconButton.setBounds(new Rectangle(90, 96, 34, 32));
	        beaconButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			beaconButton.setToolTipText(Messages.getString("BeaconTip"));
		}
		return beaconButton;
	}

	private JRadioButton getTowerButton() {
		if (towerButton == null) {
			towerButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/TowerButton.png")));
			towerButton.setBounds(new Rectangle(90, 128, 34, 32));
	        towerButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			towerButton.setToolTipText(Messages.getString("TowerTip"));
		}
		return towerButton;
	}

}
