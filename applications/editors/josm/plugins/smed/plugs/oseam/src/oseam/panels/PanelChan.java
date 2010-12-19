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

public class PanelChan extends JPanel {

	private ButtonGroup catButtons = null;
	private JRadioButton portButton = null;
	private JRadioButton stbdButton = null;
	private JRadioButton prefPortButton = null;
	private JRadioButton prefStbdButton = null;
	private JRadioButton safeWaterButton = null;
	private ActionListener alCat = null;
	private PanelPort panelPort = null;
	private PanelStbd panelStbd = null;
	private PanelPrefPort panelPrefPort = null;
	private PanelPrefStbd panelPrefStbd = null;
	private PanelSafeWater panelSafeWater = null;

	public PanelChan() {
		panelPort = new PanelPort();
		panelPort.setBounds(new Rectangle(55, 0, 225, 160));
		panelPort.setVisible(false);
		panelStbd = new PanelStbd();
		panelStbd.setBounds(new Rectangle(55, 0, 225, 160));
		panelStbd.setVisible(false);
		panelPrefPort = new PanelPrefPort();
		panelPrefPort.setBounds(new Rectangle(55, 0, 225, 160));
		panelPrefPort.setVisible(false);
		panelPrefStbd = new PanelPrefStbd();
		panelPrefStbd.setBounds(new Rectangle(55, 0, 225, 160));
		panelPrefStbd.setVisible(false);
		panelSafeWater = new PanelSafeWater();
		panelSafeWater.setBounds(new Rectangle(55, 0, 225, 160));
		panelSafeWater.setVisible(false);
		this.setLayout(null);
		this.add(panelPort, null);
		this.add(panelStbd, null);
		this.add(panelPrefPort, null);
		this.add(panelPrefStbd, null);
		this.add(panelSafeWater, null);
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
		alCat = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (portButton.isSelected()) {
					portButton.setBorderPainted(true);
					panelPort.setVisible(true);
				} else {
					portButton.setBorderPainted(false);
					panelPort.setVisible(false);
				}
				if (stbdButton.isSelected()) {
					stbdButton.setBorderPainted(true);
					panelStbd.setVisible(true);
				} else {
					stbdButton.setBorderPainted(false);
					panelStbd.setVisible(false);
				}
				if (prefPortButton.isSelected()) {
					prefPortButton.setBorderPainted(true);
					panelPrefPort.setVisible(true);
				} else {
					prefPortButton.setBorderPainted(false);
					panelPrefPort.setVisible(false);
				}
				if (prefStbdButton.isSelected()) {
					prefStbdButton.setBorderPainted(true);
					panelPrefStbd.setVisible(true);
				} else {
					prefStbdButton.setBorderPainted(false);
					panelPrefStbd.setVisible(false);
				}
				if (safeWaterButton.isSelected()) {
					safeWaterButton.setBorderPainted(true);
					panelSafeWater.setVisible(true);
				} else {
					safeWaterButton.setBorderPainted(false);
					panelSafeWater.setVisible(false);
				}
			}
		};
		portButton.addActionListener(alCat);
		stbdButton.addActionListener(alCat);
		prefPortButton.addActionListener(alCat);
		prefStbdButton.addActionListener(alCat);
		safeWaterButton.addActionListener(alCat);
	}

	public void clearSelections() {
		catButtons.clearSelection();
		alCat.actionPerformed(null);
		panelPort.clearSelections();
		panelStbd.clearSelections();
		panelPrefPort.clearSelections();
		panelPrefStbd.clearSelections();
		panelSafeWater.clearSelections();
	}

	private JRadioButton getPortButton() {
		if (portButton == null) {
			portButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/PortButton.png")));
			portButton.setBounds(new Rectangle(0, 0, 52, 32));
			portButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			portButton.setToolTipText(Messages.getString("PortTip"));
		}
		return portButton;
	}

	private JRadioButton getStbdButton() {
		if (stbdButton == null) {
			stbdButton = new JRadioButton(new ImageIcon(getClass().getResource(
					"/images/StbdButton.png")));
			stbdButton.setBounds(new Rectangle(0, 32, 52, 32));
			stbdButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			stbdButton.setToolTipText(Messages.getString("StbdTip"));
		}
		return stbdButton;
	}

	private JRadioButton getPrefPortButton() {
		if (prefPortButton == null) {
			prefPortButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/PrefPortButton.png")));
			prefPortButton.setBounds(new Rectangle(0, 64, 52, 32));
			prefPortButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			prefPortButton.setToolTipText(Messages.getString("PrefPortTip"));
		}
		return prefPortButton;
	}

	private JRadioButton getPrefStbdButton() {
		if (prefStbdButton == null) {
			prefStbdButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/PrefStbdButton.png")));
			prefStbdButton.setBounds(new Rectangle(0, 96, 52, 32));
			prefStbdButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			prefStbdButton.setToolTipText(Messages.getString("PrefStbdTip"));
		}
		return prefStbdButton;
	}

	private JRadioButton getSafeWaterButton() {
		if (safeWaterButton == null) {
			safeWaterButton = new JRadioButton(new ImageIcon(getClass()
					.getResource("/images/SafeWaterButton.png")));
			safeWaterButton.setBounds(new Rectangle(0, 128, 52, 32));
	        safeWaterButton.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
			safeWaterButton.setToolTipText(Messages.getString("SafeWaterTip"));
		}
		return safeWaterButton;
	}

}
