package oseam.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelStbd extends JPanel {

	private OSeaMAction dlg;
	public ButtonGroup shapeButtons = new ButtonGroup();
	public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
	public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
	public JRadioButton coneButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeButton.png")));
	public JRadioButton sphereButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
	public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
	public JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
	public JRadioButton perchButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PerchSButton.png")));
	public JRadioButton stakeButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/StakeButton.png")));
	public EnumMap<Shp, JRadioButton> shapes = new EnumMap<Shp, JRadioButton>(Shp.class);
	public EnumMap<Shp, Obj> objects = new EnumMap<Shp, Obj>(Shp.class);
	public ActionListener alShape = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Shp shp : shapes.keySet()) {
				JRadioButton button = shapes.get(shp);
				if (button.isSelected()) {
					dlg.mark.setShape(shp);
					dlg.mark.setObject(objects.get(shp));
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			if (dlg.mark.testValid()) {
				dlg.panelMain.panelChan.topmarkButton.setVisible(true);
				dlg.panelMain.panelChan.lightButton.setVisible(true);
				if (dlg.mark.getCategory() == Cat.LAM_STBD) {
					switch (dlg.mark.getRegion()) {
					case A:
						dlg.mark.setObjPattern(Pat.NONE);
						dlg.mark.setObjColour(Col.GREEN);
						break;
					case B:
						dlg.mark.setObjPattern(Pat.NONE);
						dlg.mark.setObjColour(Col.RED);
						break;
					case C:
						dlg.mark.setObjPattern(Pat.HORIZ);
						dlg.mark.setObjColour(Col.GREEN);
						dlg.mark.addObjColour(Col.WHITE);
						dlg.mark.addObjColour(Col.GREEN);
						dlg.mark.addObjColour(Col.WHITE);
						break;
					}
				} else {
					dlg.mark.setObjPattern(Pat.HORIZ);
					switch (dlg.mark.getRegion()) {
					case A:
						dlg.mark.setObjColour(Col.GREEN);
						dlg.mark.addObjColour(Col.RED);
						dlg.mark.addObjColour(Col.GREEN);
						break;
					case B:
						dlg.mark.setObjColour(Col.RED);
						dlg.mark.addObjColour(Col.GREEN);
						dlg.mark.addObjColour(Col.RED);
						break;
					case C:
						dlg.mark.setObjColour(Col.RED);
						dlg.mark.addObjColour(Col.GREEN);
						dlg.mark.addObjColour(Col.RED);
						dlg.mark.addObjColour(Col.GREEN);
						break;
					}
				}
				dlg.panelMain.panelMore.syncPanel();
			} else {
				dlg.panelMain.panelChan.topmarkButton.setVisible(false);
				dlg.panelMain.panelChan.lightButton.setVisible(false);
			}
			dlg.mark.paintSign();
		}
	};

	public PanelStbd(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getShapeButton(pillarButton, 0, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYLAT), null);
		this.add(getShapeButton(sparButton, 0, 32, 34, 32, "Spar", Shp.SPAR, Obj.BOYLAT), null);
		this.add(getShapeButton(coneButton, 0, 64, 34, 32, "Cone", Shp.CONE, Obj.BOYLAT), null);
		this.add(getShapeButton(sphereButton, 0, 96, 34, 32, "Sphere", Shp.SPHERE, Obj.BOYLAT), null);
		this.add(getShapeButton(floatButton, 0, 128, 34, 32, "Float", Shp.FLOAT, Obj.FLTLAT), null);
		this.add(getShapeButton(beaconButton, 35, 0, 34, 32, "Beacon", Shp.BEACON, Obj.BCNLAT), null);
		this.add(getShapeButton(towerButton, 35, 32, 34, 32, "TowerB", Shp.TOWER, Obj.BCNLAT), null);
		this.add(getShapeButton(perchButton, 35, 64, 34, 32, "Perch", Shp.PERCH, Obj.BCNLAT), null);
		this.add(getShapeButton(stakeButton, 35, 96, 34, 32, "Stake", Shp.STAKE, Obj.BCNLAT), null);
	}

	public void syncPanel() {
		for (Shp shp : shapes.keySet()) {
			JRadioButton button = shapes.get(shp);
			if (dlg.mark.getShape() == shp) {
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
