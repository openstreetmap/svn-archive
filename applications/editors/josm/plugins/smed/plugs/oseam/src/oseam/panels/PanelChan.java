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
	private PanelPort panelPort = null;
	private PanelStbd panelStbd = null;
	private PanelPrefPort panelPrefPort = null;
	private PanelPrefStbd panelPrefStbd = null;
	private PanelSafeWater panelSafeWater = null;
	
	public PanelChan() {
		super();
		panelPort = new PanelPort();
		panelPort.setBounds(new Rectangle(105, 0, 185, 160));
		panelPort.setVisible(false);
		panelStbd = new PanelStbd();
		panelStbd.setBounds(new Rectangle(105, 0, 185, 160));
		panelStbd.setVisible(false);
		panelPrefPort = new PanelPrefPort();
		panelPrefPort.setBounds(new Rectangle(105, 0, 185, 160));
		panelPrefPort.setVisible(false);
		panelPrefStbd = new PanelPrefStbd();
		panelPrefStbd.setBounds(new Rectangle(105, 0, 185, 160));
		panelPrefStbd.setVisible(false);
		panelSafeWater = new PanelSafeWater();
		panelSafeWater.setBounds(new Rectangle(105, 0, 185, 160));
		panelSafeWater.setVisible(false);
		initialize();
	}

	private void initialize() {
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
			ActionListener alCat = new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (portButton.isSelected()) {
						portButton.setEnabled(false);
						panelPort.setVisible(true);
					} else { 
						portButton.setEnabled(true);
						panelPort.setVisible(false);
					}
					if (stbdButton.isSelected()) {
						stbdButton.setEnabled(false);
						panelStbd.setVisible(true);
					} else { 
						stbdButton.setEnabled(true);
						panelStbd.setVisible(false);
					}
					if (prefPortButton.isSelected()) {
						prefPortButton.setEnabled(false);
						panelPrefPort.setVisible(true);
					} else { 
						prefPortButton.setEnabled(true);
						panelPrefPort.setVisible(false);
					}
					if (prefStbdButton.isSelected()) {
						prefStbdButton.setEnabled(false);
						panelPrefStbd.setVisible(true);
					} else { 
						prefStbdButton.setEnabled(true);
						panelPrefStbd.setVisible(false);
					}
					if (safeWaterButton.isSelected()) {
						safeWaterButton.setEnabled(false);
						panelSafeWater.setVisible(true);
					} else { 
						safeWaterButton.setEnabled(true);
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

	private JRadioButton getPortButton() {
		if (portButton == null) {
			portButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("PortButton"))));
			portButton.setBounds(new Rectangle(0, 0, 105, 32));
			portButton.setToolTipText(Messages.getString("PortTip"));
		}
		return portButton;
	}

	private JRadioButton getStbdButton() {
		if (stbdButton == null) {
			stbdButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("StbdButton"))));
			stbdButton.setBounds(new Rectangle(0, 32, 105, 32));
			stbdButton.setToolTipText(Messages.getString("StbdTip"));
		}
		return stbdButton;
	}

	private JRadioButton getPrefPortButton() {
		if (prefPortButton == null) {
			prefPortButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("PrefPortButton"))));
			prefPortButton.setBounds(new Rectangle(0, 64, 105, 32));
			prefPortButton.setToolTipText(Messages.getString("PrefPortTip"));
		}
		return prefPortButton;
	}

	private JRadioButton getPrefStbdButton() {
		if (prefStbdButton == null) {
			prefStbdButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("PrefStbdButton"))));
			prefStbdButton.setBounds(new Rectangle(0, 96, 105, 32));
			prefStbdButton.setToolTipText(Messages.getString("PrefStbdTip"));
		}
		return prefStbdButton;
	}

	private JRadioButton getSafeWaterButton() {
		if (safeWaterButton == null) {
			safeWaterButton = new JRadioButton(new ImageIcon(getClass().getResource(
					Messages.getString("SafeWaterButton"))));
			safeWaterButton.setBounds(new Rectangle(0, 128, 105, 32));
			safeWaterButton.setToolTipText(Messages.getString("SafeWaterTip"));
		}
		return safeWaterButton;
	}

}
