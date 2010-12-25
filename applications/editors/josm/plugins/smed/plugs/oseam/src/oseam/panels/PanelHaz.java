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
import java.util.EnumMap;
import java.util.Iterator;

public class PanelHaz extends JPanel {

	private OSeaMAction dlg;
	private ButtonGroup catButtons = new ButtonGroup();
	public JRadioButton northButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardNButton.png")));
	public JRadioButton southButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardSButton.png")));
	public JRadioButton eastButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardEButton.png")));
	public JRadioButton westButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardWButton.png")));
	public JRadioButton isolButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/IsolButton.png")));
	private ActionListener alCat = new ActionListener() {
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

	private ButtonGroup shapeButtons = new ButtonGroup();
	public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
	public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
	public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
	public JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
	private EnumMap<Shp, JRadioButton> shapes = new EnumMap<Shp, JRadioButton>(Shp.class);
	private ActionListener alShape = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			Iterator<Shp> it = shapes.keySet().iterator();
			while (it.hasNext()) {
				Shp shp = it.next();
				JRadioButton button = shapes.get(shp);
				if (button.isSelected()) {
					dlg.mark.setShape(shp);
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			if (dlg.mark != null)
				dlg.mark.paintSign();
		}
	};

	public PanelHaz(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getCatButton(northButton, 0, 0, 52, 32, "NorthTip"), null);
		this.add(getCatButton(southButton, 0, 32, 52, 32, "SouthTip"), null);
		this.add(getCatButton(eastButton, 0, 64, 52, 32, "EastTip"), null);
		this.add(getCatButton(westButton, 0, 96, 52, 32, "WestTip"), null);
		this.add(getCatButton(isolButton, 0, 128, 52, 32, "IsolTip"), null);

		this.add(getShapeButton(pillarButton, 55, 0, 34, 32, "PillarTip", Shp.PILLAR), null);
		this.add(getShapeButton(sparButton, 55, 32, 34, 32, "SparTip", Shp.SPAR), null);
		this.add(getShapeButton(floatButton, 55, 64, 34, 32, "FloatTip", Shp.FLOAT), null);
		this.add(getShapeButton(beaconButton, 55, 96, 34, 32, "BeaconTip", Shp.BEACON), null);
		this.add(getShapeButton(towerButton, 55, 128, 34, 32, "TowerTip", Shp.TOWER), null);
	}

	public void clearSelections() {
		catButtons.clearSelection();
		alCat.actionPerformed(null);
		shapeButtons.clearSelection();
		alShape.actionPerformed(null);
	}

	private JRadioButton getCatButton(JRadioButton button, int x, int y, int w, int h, String tip) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alCat);
		catButtons.add(button);
		return button;
	}

	private JRadioButton getShapeButton(JRadioButton button, int x, int y, int w, int h, String tip, Shp shp) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alShape);
		shapeButtons.add(button);
		shapes.put(shp, button);
		return button;
	}

}
