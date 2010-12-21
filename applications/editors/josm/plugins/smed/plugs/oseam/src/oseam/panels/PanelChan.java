package oseam.panels;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.panels.PanelPort;
import oseam.panels.PanelStbd;
import oseam.panels.PanelPrefPort;
import oseam.panels.PanelPrefStbd;
import oseam.seamarks.SeaMark;
import oseam.seamarks.SeaMark.Cat;
import oseam.seamarks.SeaMark.Col;
import oseam.seamarks.MarkLat;
import oseam.seamarks.MarkSaw;

import java.awt.event.ActionListener;

public class PanelChan extends JPanel {

	private OSeaMAction dlg;
	private boolean region;
	private ButtonGroup catButtons = null;
	public JRadioButton portButton = null;
	public JRadioButton stbdButton = null;
	public JRadioButton prefPortButton = null;
	public JRadioButton prefStbdButton = null;
	public JRadioButton safeWaterButton = null;
	private ActionListener alCat = null;
	public PanelPort panelPort = null;
	public PanelStbd panelStbd = null;
	public PanelPrefPort panelPrefPort = null;
	public PanelPrefStbd panelPrefStbd = null;
	public PanelSafeWater panelSafeWater = null;

	public PanelChan(OSeaMAction dia) {
		dlg = dia;
		panelPort = new PanelPort(dlg);
		panelPort.setBounds(new Rectangle(55, 0, 225, 160));
		panelPort.setVisible(false);
		panelStbd = new PanelStbd(dlg);
		panelStbd.setBounds(new Rectangle(55, 0, 225, 160));
		panelStbd.setVisible(false);
		panelPrefPort = new PanelPrefPort(dlg);
		panelPrefPort.setBounds(new Rectangle(55, 0, 225, 160));
		panelPrefPort.setVisible(false);
		panelPrefStbd = new PanelPrefStbd(dlg);
		panelPrefStbd.setBounds(new Rectangle(55, 0, 225, 160));
		panelPrefStbd.setVisible(false);
		panelSafeWater = new PanelSafeWater(dlg);
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
					if (!(dlg.mark instanceof MarkLat))
						dlg.mark = new MarkLat(dlg);
					dlg.mark.setCategory(Cat.PORT_HAND);
					if (dlg.mark.getRegion() == SeaMark.IALA_A) {
						dlg.mark.setColour(Col.RED);
						panelPort.regionAButton.doClick();
					} else {
						dlg.mark.setColour(Col.GREEN);
						panelPort.regionBButton.doClick();
					}
					portButton.setBorderPainted(true);
					panelPort.setVisible(true);
				} else {
					portButton.setBorderPainted(false);
					panelPort.setVisible(false);
				}
				if (stbdButton.isSelected()) {
					if (!(dlg.mark instanceof MarkLat))
						dlg.mark = new MarkLat(dlg);
					dlg.mark.setCategory(Cat.STARBOARD_HAND);
					if (dlg.mark.getRegion() == SeaMark.IALA_A) {
						dlg.mark.setColour(Col.GREEN);
						panelStbd.regionAButton.doClick();
					} else {
						dlg.mark.setColour(Col.RED);
						panelStbd.regionBButton.doClick();
					}
					stbdButton.setBorderPainted(true);
					panelStbd.setVisible(true);
				} else {
					stbdButton.setBorderPainted(false);
					panelStbd.setVisible(false);
				}
				if (prefPortButton.isSelected()) {
					if (!(dlg.mark instanceof MarkLat))
						dlg.mark = new MarkLat(dlg);
					dlg.mark.setCategory(Cat.PREF_PORT_HAND);
					if (dlg.mark.getRegion() == SeaMark.IALA_A) {
						dlg.mark.setColour(Col.RED_GREEN_RED);
						panelPrefPort.regionAButton.doClick();
					} else {
						dlg.mark.setColour(Col.GREEN_RED_GREEN);
						panelPrefPort.regionBButton.doClick();
					}
					prefPortButton.setBorderPainted(true);
					panelPrefPort.setVisible(true);
				} else {
					prefPortButton.setBorderPainted(false);
					panelPrefPort.setVisible(false);
				}
				if (prefStbdButton.isSelected()) {
					if (!(dlg.mark instanceof MarkLat))
						dlg.mark = new MarkLat(dlg);
					dlg.mark.setCategory(Cat.PREF_STARBOARD_HAND);
					if (dlg.mark.getRegion() == SeaMark.IALA_A) {
						dlg.mark.setColour(Col.GREEN_RED_GREEN);
						panelPrefStbd.regionAButton.doClick();
					} else {
						dlg.mark.setColour(Col.RED_GREEN_RED);
						panelPrefStbd.regionBButton.doClick();
					}
					prefStbdButton.setBorderPainted(true);
					panelPrefStbd.setVisible(true);
				} else {
					prefStbdButton.setBorderPainted(false);
					panelPrefStbd.setVisible(false);
				}
				if (safeWaterButton.isSelected()) {
					if (!(dlg.mark instanceof MarkSaw))
						dlg.mark = new MarkSaw(dlg);
					dlg.mark.setColour(Col.RED_WHITE);
					safeWaterButton.setBorderPainted(true);
					panelSafeWater.setVisible(true);
				} else {
					safeWaterButton.setBorderPainted(false);
					panelSafeWater.setVisible(false);
				}
				if (dlg.mark != null) dlg.mark.paintSign();
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
