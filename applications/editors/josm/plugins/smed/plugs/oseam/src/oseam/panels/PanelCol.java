package oseam.panels;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;

import java.awt.event.ActionListener;

public class PanelCol extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup colourButtons = null;
	public JRadioButton offButton = null;
	public JRadioButton whiteButton = null;
	public JRadioButton redButton = null;
	public JRadioButton orangeButton = null;
	public JRadioButton amberButton = null;
	public JRadioButton yellowButton = null;
	public JRadioButton greenButton = null;
	public JRadioButton blueButton = null;
	public JRadioButton violetButton = null;
	public JRadioButton blackButton = null;
	private ActionListener alColour = null;

	public PanelCol(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getOffButton(), null);
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
		colourButtons.add(offButton);
		colourButtons.add(redButton);
		colourButtons.add(orangeButton);
		colourButtons.add(amberButton);
		colourButtons.add(yellowButton);
		colourButtons.add(greenButton);
		colourButtons.add(blueButton);
		colourButtons.add(violetButton);
		colourButtons.add(blackButton);
		alColour = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				offButton.setBorderPainted(offButton.isSelected());
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
		offButton.addActionListener(alColour);
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

	public void clearSelections() {
		colourButtons.clearSelection();
		alColour.actionPerformed(null);
	}

	private JRadioButton getOffButton() {
		if (offButton == null) {
			offButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/OffButton.png")));
			offButton.setBounds(new Rectangle(0, 0, 34, 16));
	        offButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			offButton.setToolTipText(tr("No colour"));
		}
		return offButton;
	}

	private JRadioButton getWhiteButton() {
		if (whiteButton == null) {
			whiteButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/WhiteButton.png")));
			whiteButton.setBounds(new Rectangle(0, 16, 34, 16));
	        whiteButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			whiteButton.setToolTipText(tr("White"));
		}
		return whiteButton;
	}

	private JRadioButton getRedButton() {
		if (redButton == null) {
			redButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/RedButton.png")));
			redButton.setBounds(new Rectangle(0, 32, 34, 16));
	        redButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			redButton.setToolTipText(tr("Red"));
		}
		return redButton;
	}

	private JRadioButton getOrangeButton() {
		if (orangeButton == null) {
			orangeButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/OrangeButton.png")));
			orangeButton.setBounds(new Rectangle(0, 48, 34, 16));
	        orangeButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			orangeButton.setToolTipText(tr("Orange"));
		}
		return orangeButton;
	}

	private JRadioButton getAmberButton() {
		if (amberButton == null) {
			amberButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/AmberButton.png")));
			amberButton.setBounds(new Rectangle(0, 64, 34, 16));
	        amberButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			amberButton.setToolTipText(tr("Amber"));
		}
		return amberButton;
	}

	private JRadioButton getYellowButton() {
		if (yellowButton == null) {
			yellowButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/YellowButton.png")));
			yellowButton.setBounds(new Rectangle(0, 80, 34, 16));
	        yellowButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			yellowButton.setToolTipText(tr("Yellow"));
		}
		return yellowButton;
	}

	private JRadioButton getGreenButton() {
		if (greenButton == null) {
			greenButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/GreenButton.png")));
			greenButton.setBounds(new Rectangle(0, 96, 34, 16));
	        greenButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			greenButton.setToolTipText(tr("Green"));
		}
		return greenButton;
	}

	private JRadioButton getBlueButton() {
		if (blueButton == null) {
			blueButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/BlueButton.png")));
			blueButton.setBounds(new Rectangle(0, 112, 34, 16));
	        blueButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			blueButton.setToolTipText(tr("Blue"));
		}
		return blueButton;
	}

	private JRadioButton getVioletButton() {
		if (violetButton == null) {
			violetButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/VioletButton.png")));
			violetButton.setBounds(new Rectangle(0, 128, 34, 16));
	        violetButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			violetButton.setToolTipText(tr("Violet"));
		}
		return violetButton;
	}

	private JRadioButton getBlackButton() {
		if (blackButton == null) {
			blackButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/BlackButton.png")));
			blackButton.setBounds(new Rectangle(0, 144, 34, 16));
	        blackButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			blackButton.setToolTipText(tr("Black"));
		}
		return blackButton;
	}

}
