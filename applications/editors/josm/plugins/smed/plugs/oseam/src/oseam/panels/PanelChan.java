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
import oseam.seamarks.SeaMark.Obj;
import oseam.seamarks.MarkLat;
import oseam.seamarks.MarkSaw;

import java.awt.event.ActionListener;

public class PanelChan extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup catButtons = new ButtonGroup();
	public JRadioButton portButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PortButton.png")));
	public JRadioButton stbdButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/StbdButton.png")));
	public JRadioButton prefPortButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PrefPortButton.png")));
	public JRadioButton prefStbdButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PrefStbdButton.png")));
	public JRadioButton safeWaterButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SafeWaterButton.png")));
	private ActionListener alCat = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (portButton.isSelected()) {
				if (!(dlg.mark instanceof MarkLat) || (dlg.mark.getCategory() != Cat.LAT_PORT))
					dlg.mark = new MarkLat(dlg);
				dlg.mark.setCategory(Cat.LAT_PORT);
				if (dlg.mark.getRegion() == SeaMark.IALA_A) {
					dlg.mark.setColour(Obj.BODY, Col.RED);
					panelPort.regionAButton.doClick();
				} else {
					dlg.mark.setColour(Obj.BODY, Col.GREEN);
					panelPort.regionBButton.doClick();
				}
				portButton.setBorderPainted(true);
				panelPort.setVisible(true);
			} else {
				portButton.setBorderPainted(false);
				panelPort.setVisible(false);
				panelPort.clearSelections();
			}
			if (stbdButton.isSelected()) {
				if (!(dlg.mark instanceof MarkLat) || (dlg.mark.getCategory() != Cat.LAT_STBD))
					dlg.mark = new MarkLat(dlg);
				dlg.mark.setCategory(Cat.LAT_STBD);
				if (dlg.mark.getRegion() == SeaMark.IALA_A) {
					dlg.mark.setColour(Obj.BODY, Col.GREEN);
					panelStbd.regionAButton.doClick();
				} else {
					dlg.mark.setColour(Obj.BODY, Col.RED);
					panelStbd.regionBButton.doClick();
				}
				stbdButton.setBorderPainted(true);
				panelStbd.setVisible(true);
			} else {
				stbdButton.setBorderPainted(false);
				panelStbd.setVisible(false);
				panelStbd.clearSelections();
			}
			if (prefPortButton.isSelected()) {
				if (!(dlg.mark instanceof MarkLat) || (dlg.mark.getCategory() != Cat.LAT_PREF_PORT))
					dlg.mark = new MarkLat(dlg);
				dlg.mark.setCategory(Cat.LAT_PREF_PORT);
				if (dlg.mark.getRegion() == SeaMark.IALA_A) {
					dlg.mark.setColour(Obj.BODY, Col.RED_GREEN_RED);
					panelPrefPort.regionAButton.doClick();
				} else {
					dlg.mark.setColour(Obj.BODY, Col.GREEN_RED_GREEN);
					panelPrefPort.regionBButton.doClick();
				}
				prefPortButton.setBorderPainted(true);
				panelPrefPort.setVisible(true);
			} else {
				prefPortButton.setBorderPainted(false);
				panelPrefPort.setVisible(false);
				panelPrefPort.clearSelections();
			}
			if (prefStbdButton.isSelected()) {
				if (!(dlg.mark instanceof MarkLat) || (dlg.mark.getCategory() != Cat.LAT_PREF_STBD))
					dlg.mark = new MarkLat(dlg);
				dlg.mark.setCategory(Cat.LAT_PREF_STBD);
				if (dlg.mark.getRegion() == SeaMark.IALA_A) {
					dlg.mark.setColour(Obj.BODY, Col.GREEN_RED_GREEN);
					panelPrefStbd.regionAButton.doClick();
				} else {
					dlg.mark.setColour(Obj.BODY, Col.RED_GREEN_RED);
					panelPrefStbd.regionBButton.doClick();
				}
				prefStbdButton.setBorderPainted(true);
				panelPrefStbd.setVisible(true);
			} else {
				prefStbdButton.setBorderPainted(false);
				panelPrefStbd.setVisible(false);
				panelPrefStbd.clearSelections();
			}
			if (safeWaterButton.isSelected()) {
				if (!(dlg.mark instanceof MarkSaw))
					dlg.mark = new MarkSaw(dlg);
				dlg.mark.setColour(Obj.BODY, Col.RED_WHITE);
				safeWaterButton.setBorderPainted(true);
				panelSafeWater.setVisible(true);
			} else {
				safeWaterButton.setBorderPainted(false);
				panelSafeWater.setVisible(false);
				panelSafeWater.clearSelections();
			}
			if (dlg.mark != null)
				dlg.mark.paintSign();
		}
	};

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
		this.add(getCatButton(portButton, 0, 0, 52, 32, "PortTip"), null);
		this.add(getCatButton(stbdButton, 0, 32, 52, 32, "StbdTip"), null);
		this.add(getCatButton(prefPortButton, 0, 64, 52, 32, "PrefPortTip"), null);
		this.add(getCatButton(prefStbdButton, 0, 96, 52, 32, "PrefStbdTip"), null);
		this.add(getCatButton(safeWaterButton, 0, 128, 52, 32, "SafeWaterTip"), null);
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

	private JRadioButton getCatButton(JRadioButton button, int x, int y, int w, int h, String tip) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alCat);
		catButtons.add(button);
		return button;
	}

}
