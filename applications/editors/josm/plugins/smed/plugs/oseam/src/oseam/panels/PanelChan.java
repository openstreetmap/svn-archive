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
	private ButtonGroup catButtons = new ButtonGroup();
	public JRadioButton portButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PortButton.png")));
	public JRadioButton stbdButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/StbdButton.png")));
	public JRadioButton prefPortButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PrefPortButton.png")));
	public JRadioButton prefStbdButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PrefStbdButton.png")));
	public JRadioButton safeWaterButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SafeWaterButton.png")));
	private ActionListener alCat = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			Shp shp = Shp.UNKNOWN;
			if (dlg.mark != null)
				shp = dlg.mark.getShape();
			if (portButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_PORT);
				if (panelPort.shapes.containsKey(shp)) {
					panelPort.shapes.get(shp).doClick();
				} else {
					panelPort.clearSelections();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				portButton.setBorderPainted(true);
				panelPort.setVisible(true);
				panelPort.perchButton.setVisible(true);
				panelPort.stakeButton.setVisible(true);
			} else {
				portButton.setBorderPainted(false);
				panelPort.setVisible(false);
			}
			if (prefPortButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_PPORT);
				if (panelPort.shapes.containsKey(shp)) {
					panelPort.shapes.get(shp).doClick();
				} else {
					panelPort.clearSelections();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				prefPortButton.setBorderPainted(true);
				panelPort.setVisible(true);
			} else {
				prefPortButton.setBorderPainted(false);
				if (!portButton.isSelected())
					panelPort.setVisible(false);
			}
			if (stbdButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_STBD);
				if (panelStbd.shapes.containsKey(shp)) {
					panelStbd.shapes.get(shp).doClick();
				} else {
					panelStbd.clearSelections();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				stbdButton.setBorderPainted(true);
				panelStbd.setVisible(true);
				panelStbd.perchButton.setVisible(true);
				panelStbd.stakeButton.setVisible(true);
			} else {
				stbdButton.setBorderPainted(false);
				panelStbd.setVisible(false);
			}
			if (prefStbdButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_PSTBD);
				if (panelStbd.shapes.containsKey(shp)) {
					panelStbd.shapes.get(shp).doClick();
				} else {
					panelStbd.clearSelections();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				prefStbdButton.setBorderPainted(true);
				panelStbd.setVisible(true);
			} else {
				prefStbdButton.setBorderPainted(false);
				if (!stbdButton.isSelected())
					panelStbd.setVisible(false);
			}
			if (safeWaterButton.isSelected()) {
				dlg.mark.setCategory(Cat.UNKNOWN);
				if (panelSaw.shapes.containsKey(shp)) {
					panelSaw.shapes.get(shp).doClick();
				} else {
					panelSaw.clearSelections();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				safeWaterButton.setBorderPainted(true);
				panelSaw.setVisible(true);
			} else {
				safeWaterButton.setBorderPainted(false);
				panelSaw.setVisible(false);
			}
		}
	};
	public JToggleButton topmarkButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/ChanTopButton.png")));
	private ActionListener alTop = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (dlg.mark != null) {
				dlg.panelMain.panelTop.clearSelections();
				if (topmarkButton.isSelected()) {
					if (SeaMark.GrpMAP.get(dlg.mark.getObject()) == Grp.SAW) {
						dlg.panelMain.panelTop.sphereTopButton.doClick();
						dlg.panelMain.panelTop.panelPat.noneButton.doClick();
						dlg.panelMain.panelTop.panelPat.panelCol.redButton.doClick();
					} else {
						switch (dlg.mark.getCategory()) {
						case LAM_PORT:
						case LAM_PPORT:
							dlg.panelMain.panelTop.canTopButton.doClick();
							switch (dlg.mark.getRegion()) {
							case A:
								dlg.panelMain.panelTop.panelPat.noneButton.doClick();
								dlg.panelMain.panelTop.panelPat.panelCol.redButton.doClick();
								break;
							case B:
								dlg.panelMain.panelTop.panelPat.noneButton.doClick();
								dlg.panelMain.panelTop.panelPat.panelCol.greenButton.doClick();
								break;
							case C:
								dlg.panelMain.panelTop.panelPat.horizButton.doClick();
								dlg.panelMain.panelTop.panelPat.panelCol.redButton.doClick();
								dlg.panelMain.panelTop.panelPat.panelCol.addButton.doClick();
								dlg.panelMain.panelTop.panelPat.panelCol.whiteButton.doClick();
								break;
							}
							break;
						case LAM_STBD:
						case LAM_PSTBD:
							dlg.panelMain.panelTop.coneTopButton.doClick();
							switch (dlg.mark.getRegion()) {
							case A:
								dlg.panelMain.panelTop.panelPat.noneButton.doClick();
								dlg.panelMain.panelTop.panelPat.panelCol.greenButton.doClick();
								break;
							case B:
								dlg.panelMain.panelTop.panelPat.noneButton.doClick();
								dlg.panelMain.panelTop.panelPat.panelCol.redButton.doClick();
								break;
							case C:
								dlg.panelMain.panelTop.panelPat.horizButton.doClick();
								dlg.panelMain.panelTop.panelPat.panelCol.greenButton.doClick();
								dlg.panelMain.panelTop.panelPat.panelCol.addButton.doClick();
								dlg.panelMain.panelTop.panelPat.panelCol.whiteButton.doClick();
								break;
							}
							break;
						}
					}
					topmarkButton.setBorderPainted(true);
				} else {
					topmarkButton.setBorderPainted(false);
				}
			}
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

	public void clearSelections() {
		topmarkButton.setSelected(false);
		topmarkButton.setVisible(false);
		alTop.actionPerformed(null);
		catButtons.clearSelection();
		alCat.actionPerformed(null);
		panelPort.clearSelections();
		panelStbd.clearSelections();
		panelSaw.clearSelections();
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
