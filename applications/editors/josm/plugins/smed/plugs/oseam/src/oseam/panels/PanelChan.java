package oseam.panels;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;
import oseam.seamarks.SeaMark.*;

public class PanelChan extends JPanel {

	private OSeaMAction dlg;
	public PanelPort panelPort = null;
	public PanelStbd panelStbd = null;
	public PanelSaw panelSaw = null;
	public ButtonGroup catButtons = new ButtonGroup();
	public JRadioButton portButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PortButton.png")));
	public JRadioButton stbdButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/StbdButton.png")));
	public JRadioButton prefPortButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PrefPortButton.png")));
	public JRadioButton prefStbdButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PrefStbdButton.png")));
	public JRadioButton safeWaterButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SafeWaterButton.png")));
	private ActionListener alCat = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			panelPort.setVisible(false);
			panelStbd.setVisible(false);
			panelSaw.setVisible(false);
			dlg.panelMain.moreButton.setVisible(false);
			dlg.panelMain.saveButton.setEnabled(false);
			topmarkButton.setVisible(false);
			Shp shp = dlg.mark.getShape();
			if (portButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_PORT);
				if (panelPort.shapes.containsKey(shp)) {
					panelPort.shapes.get(shp).setSelected(true);
				} else {
					panelPort.shapeButtons.clearSelection();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				panelPort.alShape.actionPerformed(null);
				panelPort.setVisible(true);
				portButton.setBorderPainted(true);
			} else {
				portButton.setBorderPainted(false);
			}
			if (prefPortButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_PPORT);
				if (panelPort.shapes.containsKey(shp)) {
					panelPort.shapes.get(shp).setSelected(true);
				} else {
					panelPort.shapeButtons.clearSelection();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				panelPort.alShape.actionPerformed(null);
				panelPort.setVisible(true);
				prefPortButton.setBorderPainted(true);
			} else {
				prefPortButton.setBorderPainted(false);
			}
			if (stbdButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_STBD);
				if (panelStbd.shapes.containsKey(shp)) {
					panelStbd.shapes.get(shp).setSelected(true);
				} else {
					panelStbd.shapeButtons.clearSelection();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				panelStbd.alShape.actionPerformed(null);
				panelStbd.setVisible(true);
				stbdButton.setBorderPainted(true);
			} else {
				stbdButton.setBorderPainted(false);
			}
			if (prefStbdButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_PSTBD);
				if (panelStbd.shapes.containsKey(shp)) {
					panelStbd.shapes.get(shp).setSelected(true);
				} else {
					panelStbd.shapeButtons.clearSelection();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				panelStbd.alShape.actionPerformed(null);
				panelStbd.setVisible(true);
				prefStbdButton.setBorderPainted(true);
			} else {
				prefStbdButton.setBorderPainted(false);
			}
			if (safeWaterButton.isSelected()) {
				dlg.mark.setCategory(Cat.NONE);
				panelSaw.setVisible(true);
				if (panelSaw.shapes.containsKey(shp)) {
					panelSaw.shapes.get(shp).setSelected(true);
				} else {
					panelSaw.shapeButtons.clearSelection();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				panelSaw.alShape.actionPerformed(null);
				panelSaw.setVisible(true);
				safeWaterButton.setBorderPainted(true);
			} else {
				safeWaterButton.setBorderPainted(false);
			}
			if (dlg.mark.isValid()) {
				dlg.panelMain.moreButton.setVisible(true);
				dlg.panelMain.saveButton.setEnabled(true);
				topmarkButton.setVisible(true);
			}
			alTop.actionPerformed(null);
			dlg.mark.paintSign();
		}
	};
	public JToggleButton topmarkButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/ChanTopButton.png")));
	private ActionListener alTop = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (topmarkButton.isSelected()) {
				if (SeaMark.GrpMAP.get(dlg.mark.getObject()) == Grp.SAW) {
					dlg.mark.setTopmark(Top.SPHERE);
					dlg.mark.setTopPattern(Pat.NONE);
					dlg.mark.setTopColour(Col.RED);
				} else {
					switch (dlg.mark.getCategory()) {
					case LAM_PORT:
					case LAM_PPORT:
						dlg.mark.setTopmark(Top.CAN);
						switch (dlg.mark.getRegion()) {
						case A:
							dlg.mark.setTopPattern(Pat.NONE);
							dlg.mark.setTopColour(Col.RED);
							break;
						case B:
							dlg.mark.setTopPattern(Pat.NONE);
							dlg.mark.setTopColour(Col.GREEN);
							break;
						case C:
							dlg.mark.setTopPattern(Pat.HORIZ);
							dlg.mark.setTopColour(Col.RED);
							dlg.mark.addTopColour(Col.WHITE);
							break;
						}
						break;
					case LAM_STBD:
					case LAM_PSTBD:
						dlg.panelMain.panelTop.coneTopButton.doClick();
						switch (dlg.mark.getRegion()) {
						case A:
							dlg.mark.setTopPattern(Pat.NONE);
							dlg.mark.setTopColour(Col.GREEN);
							break;
						case B:
							dlg.mark.setTopPattern(Pat.NONE);
							dlg.mark.setTopColour(Col.RED);
							break;
						case C:
							dlg.mark.setTopPattern(Pat.HORIZ);
							dlg.mark.setTopColour(Col.GREEN);
							dlg.mark.addTopColour(Col.WHITE);
							break;
						}
						break;
					}
				}
				topmarkButton.setBorderPainted(true);
			} else {
				dlg.mark.setTopmark(Top.NONE);
				dlg.mark.setTopPattern(Pat.NONE);
				dlg.mark.setTopColour(Col.UNKNOWN);
				topmarkButton.setBorderPainted(false);
			}
			dlg.panelMain.panelTop.syncPanel();
			dlg.mark.paintSign();
		}
	};

	public PanelChan(OSeaMAction dia) {
		dlg = dia;
		panelPort = new PanelPort(dlg);
		panelPort.setBounds(new Rectangle(55, 0, 70, 160));
		panelPort.setVisible(false);
		panelStbd = new PanelStbd(dlg);
		panelStbd.setBounds(new Rectangle(55, 0, 70, 160));
		panelStbd.setVisible(false);
		panelSaw = new PanelSaw(dlg);
		panelSaw.setBounds(new Rectangle(55, 0, 70, 160));
		panelSaw.setVisible(false);
		this.setLayout(null);
		this.add(panelPort, null);
		this.add(panelStbd, null);
		this.add(panelSaw, null);
		this.add(getCatButton(portButton, 0, 0, 52, 32, "Port"), null);
		this.add(getCatButton(stbdButton, 0, 32, 52, 32, "Stbd"), null);
		this.add(getCatButton(prefPortButton, 0, 64, 52, 32, "PrefPort"), null);
		this.add(getCatButton(prefStbdButton, 0, 96, 52, 32, "PrefStbd"), null);
		this.add(getCatButton(safeWaterButton, 0, 128, 52, 32, "SafeWater"), null);

		topmarkButton.setBounds(new Rectangle(130, 0, 34, 32));
		topmarkButton.setBorder(BorderFactory.createLoweredBevelBorder());
		topmarkButton.addActionListener(alTop);
		topmarkButton.setVisible(false);
		this.add(topmarkButton);
	}

	public void syncPanel() {
		panelPort.setVisible(false);
		panelStbd.setVisible(false);
		panelSaw.setVisible(false);
		if (dlg.mark.getCategory() == Cat.LAM_PORT) {
			panelPort.setVisible(true);
			portButton.setBorderPainted(true);
		} else {
			portButton.setBorderPainted(false);
		}
		if (dlg.mark.getCategory() == Cat.LAM_PPORT) {
			panelPort.setVisible(true);
			prefPortButton.setBorderPainted(true);
		} else {
			prefPortButton.setBorderPainted(false);
		}
		if (dlg.mark.getCategory() == Cat.LAM_STBD) {
			panelStbd.setVisible(true);
			stbdButton.setBorderPainted(true);
		} else {
			stbdButton.setBorderPainted(false);
		}
		if (dlg.mark.getCategory() == Cat.LAM_PSTBD) {
			panelStbd.setVisible(true);
			prefStbdButton.setBorderPainted(true);
		} else {
			prefStbdButton.setBorderPainted(false);
		}
		if (SeaMark.GrpMAP.get(dlg.mark.getObject()) == Grp.SAW) {
			panelSaw.setVisible(true);
			safeWaterButton.setBorderPainted(true);
		} else {
			safeWaterButton.setBorderPainted(false);
		}
		topmarkButton.setBorderPainted(dlg.mark.hasTopmark());
		if (dlg.mark.isValid()) {
			topmarkButton.setVisible(true);
			dlg.panelMain.moreButton.setVisible(true);
			dlg.panelMain.saveButton.setEnabled(true);
		} else {
			topmarkButton.setVisible(false);
			dlg.panelMain.moreButton.setVisible(false);
			dlg.panelMain.saveButton.setEnabled(false);
		}
		panelPort.syncPanel();
		panelStbd.syncPanel();
		panelSaw.syncPanel();
	}

	private JRadioButton getCatButton(JRadioButton button, int x, int y, int w, int h, String tip) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alCat);
		catButtons.add(button);
		return button;
	}

}
