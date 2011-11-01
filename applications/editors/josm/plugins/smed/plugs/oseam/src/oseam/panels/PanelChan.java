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
			Shp shp = dlg.mark.getShape();
			if (portButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_PORT);
				if (!panelPort.shapes.containsKey(shp)) {
					dlg.mark.setShape(Shp.UNKNOWN);
				}
			}
			if (prefPortButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_PPORT);
				if (!panelPort.shapes.containsKey(shp)) {
					dlg.mark.setShape(Shp.UNKNOWN);
				}
			}
			if (stbdButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_STBD);
				if (!panelStbd.shapes.containsKey(shp)) {
					dlg.mark.setShape(Shp.UNKNOWN);
				}
			}
			if (prefStbdButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAM_PSTBD);
				if (!panelStbd.shapes.containsKey(shp)) {
					dlg.mark.setShape(Shp.UNKNOWN);
				}
			}
			if (safeWaterButton.isSelected()) {
				dlg.mark.setCategory(Cat.NONE);
				if (!panelSaw.shapes.containsKey(shp)) {
					dlg.mark.setShape(Shp.UNKNOWN);
				}
			}
			syncPanel();
			dlg.mark.paintSign();
		}
	};
	public JToggleButton topmarkButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/ChanTopButton.png")));
	private ActionListener alTop = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (topmarkButton.isSelected()) {
				if (SeaMark.GrpMAP.get(dlg.mark.getObject()) == Grp.SAW) {
					dlg.mark.setTopmark(Top.SPHERE);
					dlg.mark.setPattern(Ent.TOPMARK, Pat.NONE);
					dlg.mark.setColour(Ent.TOPMARK, Col.RED);
				} else {
					switch (dlg.mark.getCategory()) {
					case LAM_PORT:
					case LAM_PPORT:
						dlg.mark.setTopmark(Top.CAN);
						switch (dlg.mark.getRegion()) {
						case A:
							dlg.mark.setPattern(Ent.TOPMARK, Pat.NONE);
							dlg.mark.setColour(Ent.TOPMARK, Col.RED);
							break;
						case B:
							dlg.mark.setPattern(Ent.TOPMARK, Pat.NONE);
							dlg.mark.setColour(Ent.TOPMARK, Col.GREEN);
							break;
						case C:
							dlg.mark.setPattern(Ent.TOPMARK, Pat.HORIZ);
							dlg.mark.setColour(Ent.TOPMARK, Col.RED);
							dlg.mark.addColour(Ent.TOPMARK, Col.WHITE);
							break;
						}
						break;
					case LAM_STBD:
					case LAM_PSTBD:
						dlg.panelMain.panelTop.coneTopButton.doClick();
						switch (dlg.mark.getRegion()) {
						case A:
							dlg.mark.setPattern(Ent.TOPMARK, Pat.NONE);
							dlg.mark.setColour(Ent.TOPMARK, Col.GREEN);
							break;
						case B:
							dlg.mark.setPattern(Ent.TOPMARK, Pat.NONE);
							dlg.mark.setColour(Ent.TOPMARK, Col.RED);
							break;
						case C:
							dlg.mark.setPattern(Ent.TOPMARK, Pat.HORIZ);
							dlg.mark.setColour(Ent.TOPMARK, Col.GREEN);
							dlg.mark.addColour(Ent.TOPMARK, Col.WHITE);
							break;
						}
						break;
					}
				}
				topmarkButton.setBorderPainted(true);
			} else {
				dlg.mark.setTopmark(Top.NONE);
				dlg.mark.setPattern(Ent.TOPMARK, Pat.NONE);
				dlg.mark.setColour(Ent.TOPMARK, Col.UNKNOWN);
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
		if ((dlg.mark.getCategory() == Cat.LAM_PORT) || (dlg.mark.getCategory() == Cat.LAM_PPORT)) {
			portButton.setBorderPainted(true);
			panelPort.setVisible(true);
			panelPort.syncPanel();
		} else {
			portButton.setBorderPainted(false);
			panelPort.setVisible(false);
		}
		if ((dlg.mark.getCategory() == Cat.LAM_STBD) || (dlg.mark.getCategory() == Cat.LAM_PSTBD)) {
			stbdButton.setBorderPainted(true);
			panelStbd.setVisible(true);
			panelStbd.syncPanel();
		} else {
			stbdButton.setBorderPainted(false);
			panelStbd.setVisible(false);
		}
		if (SeaMark.GrpMAP.get(dlg.mark.getObject()) == Grp.SAW) {
			safeWaterButton.setBorderPainted(true);
			panelSaw.setVisible(true);
		} else {
			safeWaterButton.setBorderPainted(false);
			panelSaw.setVisible(false);
		}
		if (dlg.mark.isValid()) {
			topmarkButton.setBorderPainted(dlg.mark.hasTopmark());
			topmarkButton.setVisible(true);
			dlg.panelMain.moreButton.setVisible(true);
			panelSaw.syncPanel();
		} else {
			topmarkButton.setVisible(false);
			dlg.panelMain.moreButton.setVisible(false);
		}
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
