package oseam.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelPort extends JPanel {

	private OSeaMAction dlg;
	public ButtonGroup shapeButtons = new ButtonGroup();
	public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
	public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
	public JRadioButton canButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CanButton.png")));
	public JRadioButton sphereButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
	public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
	public JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
	public JRadioButton perchButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PerchPButton.png")));
	public JRadioButton stakeButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/StakeButton.png")));
	public EnumMap<Shp, JRadioButton> shapes = new EnumMap<Shp, JRadioButton>(Shp.class);
	public EnumMap<Shp, Obj> objects = new EnumMap<Shp, Obj>(Shp.class);
	public ActionListener alShape = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Shp shp : shapes.keySet()) {
				JRadioButton button = shapes.get(shp);
				if (button.isSelected()) {
					dlg.panelMain.mark.setShape(shp);
					dlg.panelMain.mark.setObject(objects.get(shp));
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			if (dlg.panelMain.mark.testValid()) {
				dlg.panelMain.panelChan.topmarkButton.setVisible(true);
				dlg.panelMain.panelChan.lightButton.setVisible(true);
				if (dlg.panelMain.mark.getCategory() == Cat.LAM_PORT) {
					switch (dlg.panelMain.mark.getRegion()) {
					case A:
						dlg.panelMain.mark.setObjPattern(Pat.NOPAT);
						dlg.panelMain.mark.setObjColour(Col.RED);
						break;
					case B:
						dlg.panelMain.mark.setObjPattern(Pat.NOPAT);
						dlg.panelMain.mark.setObjColour(Col.GREEN);
						break;
					case C:
						dlg.panelMain.mark.setObjPattern(Pat.HORIZ);
						dlg.panelMain.mark.setObjColour(Col.RED);
						dlg.panelMain.mark.addObjColour(Col.WHITE);
						dlg.panelMain.mark.addObjColour(Col.RED);
						dlg.panelMain.mark.addObjColour(Col.WHITE);
						break;
					}
				} else {
					dlg.panelMain.mark.setObjPattern(Pat.HORIZ);
					switch (dlg.panelMain.mark.getRegion()) {
					case A:
						dlg.panelMain.mark.setObjColour(Col.RED);
						dlg.panelMain.mark.addObjColour(Col.GREEN);
						dlg.panelMain.mark.addObjColour(Col.RED);
						break;
					case B:
						dlg.panelMain.mark.setObjColour(Col.GREEN);
						dlg.panelMain.mark.addObjColour(Col.RED);
						dlg.panelMain.mark.addObjColour(Col.GREEN);
						break;
					case C:
						dlg.panelMain.mark.setObjColour(Col.RED);
						dlg.panelMain.mark.addObjColour(Col.GREEN);
						dlg.panelMain.mark.addObjColour(Col.RED);
						dlg.panelMain.mark.addObjColour(Col.GREEN);
						break;
					}
				}
				dlg.panelMain.panelMore.syncPanel();
			} else {
				dlg.panelMain.panelChan.topmarkButton.setVisible(false);
				dlg.panelMain.panelChan.lightButton.setVisible(false);
			}
		}
	};

	public PanelPort(OSeaMAction dia) {
		dlg = dia;
		setLayout(null);
		add(getShapeButton(pillarButton, 0, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYLAT));
		add(getShapeButton(sparButton, 0, 32, 34, 32, "Spar", Shp.SPAR, Obj.BOYLAT));
		add(getShapeButton(canButton, 0, 64, 34, 32, "Can", Shp.CAN, Obj.BOYLAT));
		add(getShapeButton(sphereButton, 0, 96, 34, 32, "Sphere", Shp.SPHERI, Obj.BOYLAT));
		add(getShapeButton(floatButton, 0, 128, 34, 32, "Float", Shp.FLOAT, Obj.FLTLAT));
		add(getShapeButton(beaconButton, 35, 0, 34, 32, "Beacon", Shp.BEACON, Obj.BCNLAT));
		add(getShapeButton(towerButton, 35, 32, 34, 32, "TowerB", Shp.TOWER, Obj.BCNLAT));
		add(getShapeButton(perchButton, 35, 64, 34, 32, "Perch", Shp.PERCH, Obj.BCNLAT));
		add(getShapeButton(stakeButton, 35, 96, 34, 32, "Stake", Shp.STAKE, Obj.BCNLAT));
	}

	public void syncPanel() {
		for (Shp shp : shapes.keySet()) {
			JRadioButton button = shapes.get(shp);
			if (dlg.panelMain.mark.getShape() == shp) {
				button.setBorderPainted(true);
			} else
				button.setBorderPainted(false);
		}
	}

	private JRadioButton getShapeButton(JRadioButton button, int x, int y, int w, int h, String tip, Shp shp, Obj obj) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alShape);
		shapeButtons.add(button);
		shapes.put(shp, button);
		objects.put(shp, obj);
		return button;
	}

}
