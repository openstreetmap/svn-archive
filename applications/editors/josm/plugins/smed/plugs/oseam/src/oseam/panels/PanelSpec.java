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
	private JLabel colourLabel = null;
	private ButtonGroup colourButtons = null;
	private JRadioButton whiteButton = null;
	private JRadioButton redButton = null;
	private JRadioButton orangeButton = null;
	private JRadioButton amberButton = null;
	private JRadioButton yellowButton = null;
	private JRadioButton greenButton = null;
	private JRadioButton blueButton = null;
	private JRadioButton violetButton = null;
	private JRadioButton blackButton = null;

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

		colourLabel = new JLabel();
		colourLabel.setBounds(new Rectangle(2, -2, 60, 20));
		colourLabel.setText(tr("Colour:"));
		this.add(colourLabel, null);
		this.add(getWhiteButton(), null);
		this.add(getRedButton(), null);
		this.add(getOrangeButton(), null);
		this.add(getAmberButton(), null);
		this.add(getYellowButton(), null);
		this.add(getGreenButton(), null);
		this.add(getBlueButton(), null);
		this.add(getVioletButton(), null);
		this.add(getBlackButton(), null);
		colourButtons = new ButtonGroup();
		colourButtons.add(whiteButton);
		colourButtons.add(redButton);
		colourButtons.add(orangeButton);
		colourButtons.add(amberButton);
		colourButtons.add(yellowButton);
		colourButtons.add(greenButton);
		colourButtons.add(blueButton);
		colourButtons.add(violetButton);
		colourButtons.add(blackButton);
		ActionListener alColour = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				whiteButton.setBorderPainted(whiteButton.isSelected());
				redButton.setBorderPainted(redButton.isSelected());
				orangeButton.setBorderPainted(orangeButton.isSelected());
				amberButton.setBorderPainted(amberButton.isSelected());
				yellowButton.setBorderPainted(yellowButton.isSelected());
				greenButton.setBorderPainted(greenButton.isSelected());
				blueButton.setBorderPainted(blueButton.isSelected());
				violetButton.setBorderPainted(violetButton.isSelected());
				blackButton.setBorderPainted(blackButton.isSelected());
			}
		};
		whiteButton.addActionListener(alColour);
		redButton.addActionListener(alColour);
		orangeButton.addActionListener(alColour);
		amberButton.addActionListener(alColour);
		yellowButton.addActionListener(alColour);
		greenButton.addActionListener(alColour);
		blueButton.addActionListener(alColour);
		violetButton.addActionListener(alColour);
		blackButton.addActionListener(alColour);
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

	private JRadioButton getWhiteButton() {
		if (whiteButton == null) {
			whiteButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/WhiteButton.png")));
			whiteButton.setBounds(new Rectangle(0, 16, 52, 16));
	        whiteButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			whiteButton.setToolTipText(tr("White"));
		}
		return whiteButton;
	}

	private JRadioButton getRedButton() {
		if (redButton == null) {
			redButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/RedButton.png")));
			redButton.setBounds(new Rectangle(0, 32, 52, 16));
	        redButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			redButton.setToolTipText(tr("Red"));
		}
		return redButton;
	}

	private JRadioButton getOrangeButton() {
		if (orangeButton == null) {
			orangeButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/OrangeButton.png")));
			orangeButton.setBounds(new Rectangle(0, 48, 52, 16));
	        orangeButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			orangeButton.setToolTipText(tr("Orange"));
		}
		return orangeButton;
	}

	private JRadioButton getAmberButton() {
		if (amberButton == null) {
			amberButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/AmberButton.png")));
			amberButton.setBounds(new Rectangle(0, 64, 52, 16));
	        amberButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			amberButton.setToolTipText(tr("Amber"));
		}
		return amberButton;
	}

	private JRadioButton getYellowButton() {
		if (yellowButton == null) {
			yellowButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/YellowButton.png")));
			yellowButton.setBounds(new Rectangle(0, 80, 52, 16));
	        yellowButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			yellowButton.setToolTipText(tr("Yellow"));
		}
		return yellowButton;
	}

	private JRadioButton getGreenButton() {
		if (greenButton == null) {
			greenButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/GreenButton.png")));
			greenButton.setBounds(new Rectangle(0, 96, 52, 16));
	        greenButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			greenButton.setToolTipText(tr("Green"));
		}
		return greenButton;
	}

	private JRadioButton getBlueButton() {
		if (blueButton == null) {
			blueButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/BlueButton.png")));
			blueButton.setBounds(new Rectangle(0, 112, 52, 16));
	        blueButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			blueButton.setToolTipText(tr("Blue"));
		}
		return blueButton;
	}

	private JRadioButton getVioletButton() {
		if (violetButton == null) {
			violetButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/VioletButton.png")));
			violetButton.setBounds(new Rectangle(0, 128, 52, 16));
	        violetButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			violetButton.setToolTipText(tr("Violet"));
		}
		return violetButton;
	}

	private JRadioButton getBlackButton() {
		if (blackButton == null) {
			blackButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/BlackButton.png")));
			blackButton.setBounds(new Rectangle(0, 144, 52, 16));
	        blackButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			blackButton.setToolTipText(tr("Black"));
		}
		return blackButton;
	}

}
