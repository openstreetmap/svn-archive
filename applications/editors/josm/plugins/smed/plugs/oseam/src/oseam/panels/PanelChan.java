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

public class PanelChan extends JPanel {

	private ButtonGroup catButtons = null;
	private JRadioButton portButton = null;
	private JRadioButton stbdButton = null;
	private JRadioButton prefPortButton = null;
	private JRadioButton prefStbdButton = null;
	private JRadioButton safeWaterButton = null;
	public PanelChan() {
		super();
		initialize();
	}

	private void initialize() {
         this.setLayout(null);
         this.add(getPortButton(), null);
         this.add(getStbdButton(), null);
         this.add(getPrefPortButton(), null);
         this.add(getPrefStbdButton(), null);
         this.add(getSafeWaterButton(), null);
         catButtons = new ButtonGroup();
         catButtons.add(portButton);
         catButtons.add(stbdButton);
         catButtons.add(prefPortButton);
         catButtons.add(prefStbdButton);
         catButtons.add(safeWaterButton);
	}

	private JRadioButton getPortButton() {
		if (portButton == null) {
			portButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("PortButton"))));
			portButton.setBounds(new Rectangle(0, 0, 105, 40));
			portButton.setToolTipText(Messages.getString("PortTip"));
		}
		return portButton;
	}

	private JRadioButton getStbdButton() {
		if (stbdButton == null) {
			stbdButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("StbdButton"))));
			stbdButton.setBounds(new Rectangle(0, 32, 105, 40));
			stbdButton.setToolTipText(Messages.getString("StbdTip"));
		}
		return stbdButton;
	}

	private JRadioButton getPrefPortButton() {
		if (prefPortButton == null) {
			prefPortButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("PrefPortButton"))));
			prefPortButton.setBounds(new Rectangle(0, 64, 105, 40));
			prefPortButton.setToolTipText(Messages.getString("PrefPortTip"));
		}
		return prefPortButton;
	}

	private JRadioButton getPrefStbdButton() {
		if (prefStbdButton == null) {
			prefStbdButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("PrefStbdButton"))));
			prefStbdButton.setBounds(new Rectangle(0, 96, 105, 40));
			prefStbdButton.setToolTipText(Messages.getString("PrefStbdTip"));
		}
		return prefStbdButton;
	}

	private JRadioButton getSafeWaterButton() {
		if (safeWaterButton == null) {
			safeWaterButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("SafeWaterButton"))));
			safeWaterButton.setBounds(new Rectangle(0, 128, 105, 40));
			safeWaterButton.setToolTipText(Messages.getString("SafeWaterTip"));
		}
		return safeWaterButton;
	}

}
