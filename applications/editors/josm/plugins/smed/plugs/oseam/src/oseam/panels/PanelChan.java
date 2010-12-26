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
import oseam.panels.PanelSaw;
import oseam.seamarks.SeaMark;
import oseam.seamarks.SeaMark.Cat;
import oseam.seamarks.SeaMark.Col;
import oseam.seamarks.SeaMark.Obj;
import oseam.seamarks.MarkLat;
import oseam.seamarks.MarkSaw;

import java.awt.event.ActionListener;

public class PanelChan extends JPanel {

	private OSeaMAction dlg;
	public PanelPort panelPort = null;
	public PanelStbd panelStbd = null;
	public PanelSaw panelSaw = null;
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
				panelPort.perchButton.setVisible(true);
			} else {
				portButton.setBorderPainted(false);
				panelPort.setVisible(false);
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
				panelStbd.perchButton.setVisible(true);
			} else {
				stbdButton.setBorderPainted(false);
				panelStbd.setVisible(false);
			}
			if (prefPortButton.isSelected()) {
				if (!(dlg.mark instanceof MarkLat) || (dlg.mark.getCategory() != Cat.LAT_PREF_PORT))
					dlg.mark = new MarkLat(dlg);
				dlg.mark.setCategory(Cat.LAT_PREF_PORT);
				if (dlg.mark.getRegion() == SeaMark.IALA_A) {
					dlg.mark.setColour(Obj.BODY, Col.RED_GREEN_RED);
					panelPort.regionAButton.doClick();
				} else {
					dlg.mark.setColour(Obj.BODY, Col.GREEN_RED_GREEN);
					panelPort.regionBButton.doClick();
				}
				prefPortButton.setBorderPainted(true);
				panelPort.setVisible(true);
				panelPort.perchButton.setVisible(false);
			} else {
				prefPortButton.setBorderPainted(false);
				if (!portButton.isSelected()) panelPort.setVisible(false);
			}
			if (prefStbdButton.isSelected()) {
				if (!(dlg.mark instanceof MarkLat) || (dlg.mark.getCategory() != Cat.LAT_PREF_STBD))
					dlg.mark = new MarkLat(dlg);
				dlg.mark.setCategory(Cat.LAT_PREF_STBD);
				if (dlg.mark.getRegion() == SeaMark.IALA_A) {
					dlg.mark.setColour(Obj.BODY, Col.GREEN_RED_GREEN);
					panelStbd.regionAButton.doClick();
				} else {
					dlg.mark.setColour(Obj.BODY, Col.RED_GREEN_RED);
					panelStbd.regionBButton.doClick();
				}
				prefStbdButton.setBorderPainted(true);
				panelStbd.setVisible(true);
				panelStbd.perchButton.setVisible(false);
			} else {
				prefStbdButton.setBorderPainted(false);
				if (!stbdButton.isSelected()) panelStbd.setVisible(false);
			}
			if (safeWaterButton.isSelected()) {
				if (!(dlg.mark instanceof MarkSaw))
					dlg.mark = new MarkSaw(dlg);
				dlg.mark.setColour(Obj.BODY, Col.RED_WHITE);
				safeWaterButton.setBorderPainted(true);
				panelSaw.setVisible(true);
			} else {
				safeWaterButton.setBorderPainted(false);
				panelSaw.setVisible(false);
			}
			if (dlg.mark != null)
				dlg.mark.paintSign();
		}
	};

	public PanelChan(OSeaMAction dia) {
		dlg = dia;
		panelPort = new PanelPort(dlg);
		panelPort.setBounds(new Rectangle(55, 0, 225, 160));
		panelPort.setVisible(false);
		panelStbd = new PanelStbd(dlg);
		panelStbd.setBounds(new Rectangle(55, 0, 225, 160));
		panelStbd.setVisible(false);
		panelSaw = new PanelSaw(dlg);
		panelSaw.setBounds(new Rectangle(55, 0, 225, 160));
		panelSaw.setVisible(false);
		this.setLayout(null);
		this.add(panelPort, null);
		this.add(panelStbd, null);
		this.add(panelSaw, null);
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
		panelSaw.clearSelections();
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
