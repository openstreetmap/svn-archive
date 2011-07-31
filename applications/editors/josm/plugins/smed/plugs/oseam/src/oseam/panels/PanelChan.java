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
import oseam.seamarks.SeaMark.Cat;
import oseam.seamarks.SeaMark.Col;
import oseam.seamarks.SeaMark.Ent;
import oseam.seamarks.SeaMark.Shp;
import oseam.seamarks.SeaMark.Reg;

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
			Shp shp = null;
			if (dlg.mark != null)
				shp = dlg.mark.getShape();
			if (portButton.isSelected() || prefPortButton.isSelected()) {
				dlg.panelMain.panelTop.enableAll(false);
				dlg.panelMain.panelTop.noTopButton.setEnabled(true);
				dlg.panelMain.panelTop.canTopButton.setEnabled(true);
				dlg.panelMain.panelTop.panelCol.enableAll(false);
				dlg.panelMain.panelLit.panelCol.enableAll(false);
				dlg.panelMain.panelLit.panelCol.offButton.setEnabled(true);
				if (dlg.mark.getRegion() == Reg.B) {
					dlg.panelMain.panelTop.panelCol.greenButton.setEnabled(true);
					dlg.panelMain.panelTop.panelCol.greenButton.doClick();
					dlg.panelMain.panelLit.panelCol.greenButton.setEnabled(true);
					dlg.panelMain.panelLit.panelCol.greenButton.doClick();
				} else {
					dlg.panelMain.panelTop.panelCol.redButton.setEnabled(true);
					dlg.panelMain.panelTop.panelCol.redButton.doClick();
					dlg.panelMain.panelLit.panelCol.redButton.setEnabled(true);
					dlg.panelMain.panelLit.panelCol.redButton.doClick();
				}
			}
			if (portButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAT_PORT);
				if (panelPort.shapes.containsKey(shp)) {
					panelPort.shapes.get(shp).doClick();
				} else {
					panelPort.clearSelections();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				if (dlg.mark.getRegion() == Reg.B) {
					dlg.mark.setColour(Ent.BODY, Col.GREEN);
					panelPort.regionBButton.doClick();
				} else {
					dlg.mark.setColour(Ent.BODY, Col.RED);
					panelPort.regionAButton.doClick();
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
				dlg.mark.setCategory(Cat.LAT_PREF_PORT);
				if (panelPort.shapes.containsKey(shp) && (shp != Shp.PERCH)) {
					panelPort.shapes.get(shp).doClick();
				} else {
					panelPort.clearSelections();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				if (dlg.mark.getRegion() == Reg.B) {
					dlg.mark.setColour(Ent.BODY, Col.GREEN_RED_GREEN);
					panelPort.regionBButton.doClick();
				} else {
					dlg.mark.setColour(Ent.BODY, Col.RED_GREEN_RED);
					panelPort.regionAButton.doClick();
				}
				prefPortButton.setBorderPainted(true);
				panelPort.setVisible(true);
				panelPort.perchButton.setVisible(false);
				panelPort.stakeButton.setVisible(false);
				if (dlg.mark.getShape() == Shp.PERCH || dlg.mark.getShape() == Shp.STAKE) {
					dlg.mark.setShape(Shp.UNKNOWN);
					panelPort.clearSelections();
				}
			} else {
				prefPortButton.setBorderPainted(false);
				if (!portButton.isSelected())
					panelPort.setVisible(false);
			}
			if (stbdButton.isSelected() || prefStbdButton.isSelected()) {
				dlg.panelMain.panelTop.enableAll(false);
				dlg.panelMain.panelTop.noTopButton.setEnabled(true);
				dlg.panelMain.panelTop.coneTopButton.setEnabled(true);
				dlg.panelMain.panelTop.panelCol.enableAll(false);
				dlg.panelMain.panelLit.panelCol.enableAll(false);
				dlg.panelMain.panelLit.panelCol.offButton.setEnabled(true);
				if (dlg.mark.getRegion() == Reg.B) {
					dlg.panelMain.panelTop.panelCol.redButton.setEnabled(true);
					dlg.panelMain.panelTop.panelCol.redButton.doClick();
					dlg.panelMain.panelLit.panelCol.redButton.setEnabled(true);
					dlg.panelMain.panelLit.panelCol.redButton.doClick();
				} else {
					dlg.panelMain.panelTop.panelCol.greenButton.setEnabled(true);
					dlg.panelMain.panelTop.panelCol.greenButton.doClick();
					dlg.panelMain.panelLit.panelCol.greenButton.setEnabled(true);
					dlg.panelMain.panelLit.panelCol.greenButton.doClick();
				}
			}
			if (stbdButton.isSelected()) {
				dlg.mark.setCategory(Cat.LAT_STBD);
				if (panelStbd.shapes.containsKey(shp)) {
					panelStbd.shapes.get(shp).doClick();
				} else {
					panelStbd.clearSelections();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				if (dlg.mark.getRegion() == Reg.B) {
					dlg.mark.setColour(Ent.BODY, Col.RED);
					panelStbd.regionBButton.doClick();
				} else {
					dlg.mark.setColour(Ent.BODY, Col.GREEN);
					panelStbd.regionAButton.doClick();
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
				dlg.mark.setCategory(Cat.LAT_PREF_STBD);
				if (panelStbd.shapes.containsKey(shp) && (shp != Shp.PERCH)) {
					panelStbd.shapes.get(shp).doClick();
				} else {
					panelStbd.clearSelections();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				if (dlg.mark.getRegion() == Reg.B) {
					dlg.mark.setColour(Ent.BODY, Col.RED_GREEN_RED);
					panelStbd.regionBButton.doClick();
				} else {
					dlg.mark.setColour(Ent.BODY, Col.GREEN_RED_GREEN);
					panelStbd.regionAButton.doClick();
				}
				prefStbdButton.setBorderPainted(true);
				panelStbd.setVisible(true);
				panelStbd.perchButton.setVisible(false);
				panelStbd.stakeButton.setVisible(false);
				if (dlg.mark.getShape() == Shp.PERCH || dlg.mark.getShape() == Shp.STAKE) {
					dlg.mark.setShape(Shp.UNKNOWN);
					panelStbd.clearSelections();
				}
			} else {
				prefStbdButton.setBorderPainted(false);
				if (!stbdButton.isSelected())
					panelStbd.setVisible(false);
			}
			if (safeWaterButton.isSelected()) {
				if (panelSaw.shapes.containsKey(shp)) {
					panelSaw.shapes.get(shp).doClick();
				} else {
					panelSaw.clearSelections();
					dlg.mark.setShape(Shp.UNKNOWN);
				}
				dlg.panelMain.panelTop.enableAll(false);
				dlg.panelMain.panelTop.noTopButton.setEnabled(true);
				dlg.panelMain.panelTop.sphereTopButton.setEnabled(true);
				dlg.panelMain.panelTop.panelCol.enableAll(false);
				dlg.panelMain.panelTop.panelCol.redButton.setEnabled(true);
				dlg.panelMain.panelTop.panelCol.redButton.doClick();
				dlg.mark.setColour(Ent.BODY, Col.RED_WHITE);
				safeWaterButton.setBorderPainted(true);
				panelSaw.setVisible(true);
			} else {
				safeWaterButton.setBorderPainted(false);
				panelSaw.setVisible(false);
			}
		}
	};

	public PanelChan(OSeaMAction dia) {
		dlg = dia;
		panelPort = new PanelPort(dlg);
		panelPort.setBounds(new Rectangle(55, 0, 180, 160));
		panelPort.setVisible(false);
		panelStbd = new PanelStbd(dlg);
		panelStbd.setBounds(new Rectangle(55, 0, 180, 160));
		panelStbd.setVisible(false);
		panelSaw = new PanelSaw(dlg);
		panelSaw.setBounds(new Rectangle(55, 0, 180, 160));
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
