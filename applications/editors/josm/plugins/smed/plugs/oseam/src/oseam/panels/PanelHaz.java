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
import oseam.seamarks.MarkCard;
import oseam.seamarks.MarkIsol;
import oseam.seamarks.SeaMark.Cat;
import oseam.seamarks.SeaMark.Col;
import oseam.seamarks.SeaMark.Shp;

import java.awt.event.ActionListener;

public class PanelHaz extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup catButtons = null;
	public JRadioButton northButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardNButton.png")));
	public JRadioButton southButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardSButton.png")));
	public JRadioButton eastButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardEButton.png")));
	public JRadioButton westButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardWButton.png")));
	public JRadioButton isolButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/IsolButton.png")));
	private ActionListener alCat = null;

	private ButtonGroup shapeButtons = null;
	public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
	public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
	public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
	public JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
	private ActionListener alShape = null;

	public PanelHaz(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getButton(northButton, 0, 0, 52, 32, "NorthTip"), null);
		this.add(getButton(southButton, 0, 32, 52, 32, "SouthTip"), null);
		this.add(getButton(eastButton, 0, 64, 52, 32, "EastTip"), null);
		this.add(getButton(westButton, 0, 96, 52, 32, "WestTip"), null);
		this.add(getButton(isolButton, 0, 128, 52, 32, "IsolTip"), null);
		catButtons = new ButtonGroup();
		catButtons.add(northButton);
		catButtons.add(southButton);
		catButtons.add(eastButton);
		catButtons.add(westButton);
		catButtons.add(isolButton);
		alCat = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (northButton.isSelected()) {
					if (!(dlg.mark instanceof MarkCard)) {
						dlg.mark = new MarkCard(dlg);
						alShape.actionPerformed(null);
					}
					dlg.mark.setCategory(Cat.CARD_NORTH);
					dlg.mark.setColour(Col.BLACK_YELLOW);
					dlg.panelMain.panelTop.northTopButton.doClick();
					dlg.panelMain.panelTop.panelCol.blackButton.doClick();
					northButton.setBorderPainted(true);
				} else {
					northButton.setBorderPainted(false);
				}
				if (southButton.isSelected()) {
					if (!(dlg.mark instanceof MarkCard)) {
						dlg.mark = new MarkCard(dlg);
						alShape.actionPerformed(null);
					}
					dlg.mark.setCategory(Cat.CARD_SOUTH);
					dlg.mark.setColour(Col.YELLOW_BLACK);
					dlg.panelMain.panelTop.southTopButton.doClick();
					dlg.panelMain.panelTop.panelCol.blackButton.doClick();
					southButton.setBorderPainted(true);
				} else {
					southButton.setBorderPainted(false);
				}
				if (eastButton.isSelected()) {
					if (!(dlg.mark instanceof MarkCard)) {
						dlg.mark = new MarkCard(dlg);
						alShape.actionPerformed(null);
					}
					dlg.mark.setCategory(Cat.CARD_EAST);
					dlg.mark.setColour(Col.BLACK_YELLOW_BLACK);
					dlg.panelMain.panelTop.eastTopButton.doClick();
					dlg.panelMain.panelTop.panelCol.blackButton.doClick();
					eastButton.setBorderPainted(true);
				} else {
					eastButton.setBorderPainted(false);
				}
				if (westButton.isSelected()) {
					if (!(dlg.mark instanceof MarkCard)) {
						dlg.mark = new MarkCard(dlg);
						alShape.actionPerformed(null);
					}
					dlg.mark.setCategory(Cat.CARD_WEST);
					dlg.mark.setColour(Col.YELLOW_BLACK_YELLOW);
					dlg.panelMain.panelTop.westTopButton.doClick();
					dlg.panelMain.panelTop.panelCol.blackButton.doClick();
					westButton.setBorderPainted(true);
				} else {
					westButton.setBorderPainted(false);
				}
				if (isolButton.isSelected()) {
					if (!(dlg.mark instanceof MarkIsol)) {
						dlg.mark = new MarkIsol(dlg);
						alShape.actionPerformed(null);
					}
					dlg.mark.setColour(Col.BLACK_RED_BLACK);
					dlg.panelMain.panelTop.spheres2TopButton.doClick();
					dlg.panelMain.panelTop.panelCol.blackButton.doClick();
					isolButton.setBorderPainted(true);
				} else {
					isolButton.setBorderPainted(false);
				}
				if (dlg.mark != null)
					dlg.mark.paintSign();
			}
		};
		northButton.addActionListener(alCat);
		southButton.addActionListener(alCat);
		eastButton.addActionListener(alCat);
		westButton.addActionListener(alCat);
		isolButton.addActionListener(alCat);

		this.add(getButton(pillarButton, 55, 0, 34, 32, "PillarTip"), null);
		this.add(getButton(sparButton, 55, 32, 34, 32, "SparTip"), null);
		this.add(getButton(floatButton, 55, 64, 34, 32, "FloatTip"), null);
		this.add(getButton(beaconButton, 55, 96, 34, 32, "BeaconTip"), null);
		this.add(getButton(towerButton, 55, 128, 34, 32, "TowerTip"), null);
		shapeButtons = new ButtonGroup();
		shapeButtons.add(pillarButton);
		shapeButtons.add(sparButton);
		shapeButtons.add(floatButton);
		shapeButtons.add(beaconButton);
		shapeButtons.add(towerButton);
		alShape = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (pillarButton.isSelected()) {
					pillarButton.setBorderPainted(true);
					dlg.mark.setShape(Shp.PILLAR);
				} else {
					pillarButton.setBorderPainted(false);
				}
				if (sparButton.isSelected()) {
					sparButton.setBorderPainted(true);
					dlg.mark.setShape(Shp.SPAR);
				} else {
					sparButton.setBorderPainted(false);
				}
				if (floatButton.isSelected()) {
					floatButton.setBorderPainted(true);
					dlg.mark.setShape(Shp.FLOAT);
				} else {
					floatButton.setBorderPainted(false);
				}
				if (beaconButton.isSelected()) {
					beaconButton.setBorderPainted(true);
					dlg.mark.setShape(Shp.BEACON);
				} else {
					beaconButton.setBorderPainted(false);
				}
				if (towerButton.isSelected()) {
					towerButton.setBorderPainted(true);
					dlg.mark.setShape(Shp.TOWER);
				} else {
					towerButton.setBorderPainted(false);
				}
				if (dlg.mark != null)
					dlg.mark.paintSign();
			}
		};
		pillarButton.addActionListener(alShape);
		sparButton.addActionListener(alShape);
		floatButton.addActionListener(alShape);
		beaconButton.addActionListener(alShape);
		towerButton.addActionListener(alShape);
	}

	public void clearSelections() {
		catButtons.clearSelection();
		alCat.actionPerformed(null);
		shapeButtons.clearSelection();
		alShape.actionPerformed(null);
	}

	private JRadioButton getButton(JRadioButton button, int x, int y, int w, int h, String tip) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		return button;
	}

}
