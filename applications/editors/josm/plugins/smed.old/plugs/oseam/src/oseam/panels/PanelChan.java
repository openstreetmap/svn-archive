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
	public JRadioButton prefStbdButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PrefStbdButton.png")));
	public JRadioButton prefPortButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PrefPortButton.png")));
	public JRadioButton safeWaterButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SafeWaterButton.png")));
	private ActionListener alCat = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			panelPort.setVisible(false);
			panelStbd.setVisible(false);
			panelSaw.setVisible(false);
			dlg.panelMain.moreButton.setVisible(false);
			dlg.panelMain.saveButton.setEnabled(false);
			topmarkButton.setVisible(false);
			lightButton.setVisible(false);
			Shp shp = dlg.panelMain.mark.getShape();
			if (portButton.isSelected()) {
				dlg.panelMain.mark.setCategory(Cat.LAM_PORT);
				if (panelPort.shapes.containsKey(shp)) {
					panelPort.shapes.get(shp).setSelected(true);
				} else {
					panelPort.shapeButtons.clearSelection();
					dlg.panelMain.mark.setShape(Shp.UNKSHP);
				}
				panelPort.alShape.actionPerformed(null);
				panelPort.setVisible(true);
				portButton.setBorderPainted(true);
			} else {
				portButton.setBorderPainted(false);
			}
			if (stbdButton.isSelected()) {
				dlg.panelMain.mark.setCategory(Cat.LAM_STBD);
				if (panelStbd.shapes.containsKey(shp)) {
					panelStbd.shapes.get(shp).setSelected(true);
				} else {
					panelStbd.shapeButtons.clearSelection();
					dlg.panelMain.mark.setShape(Shp.UNKSHP);
				}
				panelStbd.alShape.actionPerformed(null);
				panelStbd.setVisible(true);
				stbdButton.setBorderPainted(true);
			} else {
				stbdButton.setBorderPainted(false);
			}
			if (prefStbdButton.isSelected()) {
				dlg.panelMain.mark.setCategory(Cat.LAM_PSTBD);
				if (panelPort.shapes.containsKey(shp)) {
					panelPort.shapes.get(shp).setSelected(true);
				} else {
					panelPort.shapeButtons.clearSelection();
					dlg.panelMain.mark.setShape(Shp.UNKSHP);
				}
				panelPort.alShape.actionPerformed(null);
				panelPort.setVisible(true);
				prefStbdButton.setBorderPainted(true);
			} else {
				prefStbdButton.setBorderPainted(false);
			}
			if (prefPortButton.isSelected()) {
				dlg.panelMain.mark.setCategory(Cat.LAM_PPORT);
				if (panelStbd.shapes.containsKey(shp)) {
					panelStbd.shapes.get(shp).setSelected(true);
				} else {
					panelStbd.shapeButtons.clearSelection();
					dlg.panelMain.mark.setShape(Shp.UNKSHP);
				}
				panelStbd.alShape.actionPerformed(null);
				panelStbd.setVisible(true);
				prefPortButton.setBorderPainted(true);
			} else {
				prefPortButton.setBorderPainted(false);
			}
			if (safeWaterButton.isSelected()) {
				dlg.panelMain.mark.setCategory(Cat.NOCAT);
				panelSaw.setVisible(true);
				if (panelSaw.shapes.containsKey(shp)) {
					panelSaw.shapes.get(shp).setSelected(true);
				} else {
					panelSaw.shapeButtons.clearSelection();
					dlg.panelMain.mark.setShape(Shp.UNKSHP);
				}
				panelSaw.alShape.actionPerformed(null);
				panelSaw.setVisible(true);
				safeWaterButton.setBorderPainted(true);
			} else {
				safeWaterButton.setBorderPainted(false);
			}
			topmarkButton.setVisible(dlg.panelMain.mark.testValid());
			lightButton.setVisible(dlg.panelMain.mark.testValid());
			dlg.panelMain.panelMore.syncPanel();
		}
	};
	public JToggleButton topmarkButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/ChanTopButton.png")));
	private ActionListener alTop = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (topmarkButton.isSelected()) {
				if (SeaMark.GrpMAP.get(dlg.panelMain.mark.getObject()) == Grp.SAW) {
					dlg.panelMain.mark.setTopmark(Top.SPHERE);
					dlg.panelMain.mark.setTopPattern(Pat.NOPAT);
					dlg.panelMain.mark.setTopColour(Col.RED);
				} else {
					switch (dlg.panelMain.mark.getCategory()) {
					case LAM_PORT:
					case LAM_PSTBD:
						dlg.panelMain.mark.setTopmark(Top.CYL);
						switch (dlg.panelMain.mark.getRegion()) {
						case A:
							dlg.panelMain.mark.setTopPattern(Pat.NOPAT);
							dlg.panelMain.mark.setTopColour(Col.RED);
							break;
						case B:
							dlg.panelMain.mark.setTopPattern(Pat.NOPAT);
							dlg.panelMain.mark.setTopColour(Col.GREEN);
							break;
						case C:
							if (dlg.panelMain.mark.getCategory() == Cat.LAM_PORT) {
								dlg.panelMain.mark.setTopPattern(Pat.HSTRP);
								dlg.panelMain.mark.setTopColour(Col.RED);
								dlg.panelMain.mark.addTopColour(Col.WHITE);
							} else {
								dlg.panelMain.mark.setTopPattern(Pat.NOPAT);
								dlg.panelMain.mark.setTopColour(Col.RED);
							}
							break;
						}
						break;
					case LAM_STBD:
					case LAM_PPORT:
						dlg.panelMain.mark.setTopmark(Top.CONE);
						switch (dlg.panelMain.mark.getRegion()) {
						case A:
							dlg.panelMain.mark.setTopPattern(Pat.NOPAT);
							dlg.panelMain.mark.setTopColour(Col.GREEN);
							break;
						case B:
							dlg.panelMain.mark.setTopPattern(Pat.NOPAT);
							dlg.panelMain.mark.setTopColour(Col.RED);
							break;
						case C:
							if (dlg.panelMain.mark.getCategory() == Cat.LAM_STBD) {
								dlg.panelMain.mark.setTopPattern(Pat.HSTRP);
								dlg.panelMain.mark.setTopColour(Col.GREEN);
								dlg.panelMain.mark.addTopColour(Col.WHITE);
							} else {
								dlg.panelMain.mark.setTopPattern(Pat.NOPAT);
								dlg.panelMain.mark.setTopColour(Col.GREEN);
							}
							break;
						}
						break;
					}
				}
				topmarkButton.setBorderPainted(true);
			} else {
				dlg.panelMain.mark.setTopmark(Top.NOTOP);
				dlg.panelMain.mark.setTopPattern(Pat.NOPAT);
				dlg.panelMain.mark.setTopColour(Col.UNKCOL);
				topmarkButton.setBorderPainted(false);
			}
			dlg.panelMain.panelTop.syncPanel();
		}
	};
	public JToggleButton lightButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/DefLitButton.png")));
	private ActionListener alLit = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (lightButton.isSelected()) {
				if (SeaMark.GrpMAP.get(dlg.panelMain.mark.getObject()) == Grp.SAW) {
					dlg.panelMain.mark.setLightAtt(Att.CHR, 0, "LFl");
					dlg.panelMain.mark.setLightAtt(Att.COL, 0, Col.WHITE);
				} else {
					dlg.panelMain.mark.setLightAtt(Att.CHR, 0, "Fl");
					switch (dlg.panelMain.mark.getCategory()) {
					case LAM_PORT:
					case LAM_PPORT:
						switch (dlg.panelMain.mark.getRegion()) {
						case A:
						case C:
							dlg.panelMain.mark.setLightAtt(Att.COL, 0, Col.RED);
							break;
						case B:
							dlg.panelMain.mark.setLightAtt(Att.COL, 0, Col.GREEN);
							break;
						}
						break;
					case LAM_STBD:
					case LAM_PSTBD:
						switch (dlg.panelMain.mark.getRegion()) {
						case A:
						case C:
							dlg.panelMain.mark.setLightAtt(Att.COL, 0, Col.GREEN);
							break;
						case B:
							dlg.panelMain.mark.setLightAtt(Att.COL, 0, Col.RED);
							break;
						}
						break;
					}
				}
				lightButton.setBorderPainted(true);
			} else {
				dlg.panelMain.mark.clrLight();
				lightButton.setBorderPainted(false);
			}
			dlg.panelMain.panelLit.syncPanel();
		}
	};

	public PanelChan(OSeaMAction dia) {
		dlg = dia;
		setLayout(null);
		panelPort = new PanelPort(dlg);
		panelPort.setBounds(new Rectangle(55, 0, 70, 160));
		panelPort.setVisible(false);
		panelStbd = new PanelStbd(dlg);
		panelStbd.setBounds(new Rectangle(55, 0, 70, 160));
		panelStbd.setVisible(false);
		panelSaw = new PanelSaw(dlg);
		panelSaw.setBounds(new Rectangle(55, 0, 70, 160));
		panelSaw.setVisible(false);
		add(panelPort);
		add(panelStbd);
		add(panelSaw);
		add(getCatButton(portButton, 0, 0, 52, 32, "Port"));
		add(getCatButton(stbdButton, 0, 32, 52, 32, "Stbd"));
		add(getCatButton(prefStbdButton, 0, 64, 52, 32, "PrefStbd"));
		add(getCatButton(prefPortButton, 0, 96, 52, 32, "PrefPort"));
		add(getCatButton(safeWaterButton, 0, 128, 52, 32, "SafeWater"));

		topmarkButton.setBounds(new Rectangle(130, 0, 34, 32));
		topmarkButton.setToolTipText(Messages.getString("Topmark"));
		topmarkButton.setBorder(BorderFactory.createLoweredBevelBorder());
		topmarkButton.addActionListener(alTop);
		topmarkButton.setVisible(false);
		add(topmarkButton);
		lightButton.setBounds(new Rectangle(130, 32, 34, 32));
		lightButton.setToolTipText(Messages.getString("Light"));
		lightButton.setBorder(BorderFactory.createLoweredBevelBorder());
		lightButton.addActionListener(alLit);
		lightButton.setVisible(false);
		add(lightButton);
	}

	public void syncPanel() {
		panelPort.setVisible(false);
		panelStbd.setVisible(false);
		panelSaw.setVisible(false);
		if (dlg.panelMain.mark.getCategory() == Cat.LAM_PORT) {
			panelPort.setVisible(true);
			portButton.setBorderPainted(true);
		} else {
			portButton.setBorderPainted(false);
		}
		if (dlg.panelMain.mark.getCategory() == Cat.LAM_PPORT) {
			panelStbd.setVisible(true);
			prefPortButton.setBorderPainted(true);
		} else {
			prefPortButton.setBorderPainted(false);
		}
		if (dlg.panelMain.mark.getCategory() == Cat.LAM_STBD) {
			panelStbd.setVisible(true);
			stbdButton.setBorderPainted(true);
		} else {
			stbdButton.setBorderPainted(false);
		}
		if (dlg.panelMain.mark.getCategory() == Cat.LAM_PSTBD) {
			panelPort.setVisible(true);
			prefStbdButton.setBorderPainted(true);
		} else {
			prefStbdButton.setBorderPainted(false);
		}
		if (SeaMark.GrpMAP.get(dlg.panelMain.mark.getObject()) == Grp.SAW) {
			panelSaw.setVisible(true);
			safeWaterButton.setBorderPainted(true);
		} else {
			safeWaterButton.setBorderPainted(false);
		}
		topmarkButton.setBorderPainted(dlg.panelMain.mark.getTopmark() != Top.NOTOP);
		topmarkButton.setSelected(dlg.panelMain.mark.getTopmark() != Top.NOTOP);
		topmarkButton.setVisible(dlg.panelMain.mark.testValid());
		Boolean lit = (dlg.panelMain.mark.getLightAtt(Att.COL, 0) != Col.UNKCOL) && !((String)dlg.panelMain.mark.getLightAtt(Att.CHR, 0)).isEmpty();
		lightButton.setBorderPainted(lit);
		lightButton.setSelected(lit);
		lightButton.setVisible(dlg.panelMain.mark.testValid());
		panelPort.syncPanel();
		panelStbd.syncPanel();
		panelSaw.syncPanel();
		dlg.panelMain.mark.testValid();
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
