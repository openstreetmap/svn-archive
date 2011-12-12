package oseam.panels;

import java.awt.event.*;
import java.awt.*;

import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;
import oseam.seamarks.SeaMark.*;

public class PanelHaz extends JPanel {

	private OSeaMAction dlg;
	public ButtonGroup catButtons = new ButtonGroup();
	public JRadioButton northButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardNButton.png")));
	public JRadioButton southButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardSButton.png")));
	public JRadioButton eastButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardEButton.png")));
	public JRadioButton westButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CardWButton.png")));
	public JRadioButton isolButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/IsolButton.png")));
	private ActionListener alCat = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.panelMain.mark.setObjPattern(Pat.HORIZ);
			if (northButton.isSelected()) {
				dlg.panelMain.mark.setCategory(Cat.CAM_NORTH);
				dlg.panelMain.mark.setObjColour(Col.BLACK);
				dlg.panelMain.mark.addObjColour(Col.YELLOW);
				northButton.setBorderPainted(true);
			} else {
				northButton.setBorderPainted(false);
			}
			if (southButton.isSelected()) {
				dlg.panelMain.mark.setCategory(Cat.CAM_SOUTH);
				dlg.panelMain.mark.setObjColour(Col.YELLOW);
				dlg.panelMain.mark.addObjColour(Col.BLACK);
				southButton.setBorderPainted(true);
			} else {
				southButton.setBorderPainted(false);
			}
			if (eastButton.isSelected()) {
				dlg.panelMain.mark.setCategory(Cat.CAM_EAST);
				dlg.panelMain.mark.setObjColour(Col.BLACK);
				dlg.panelMain.mark.addObjColour(Col.YELLOW);
				dlg.panelMain.mark.addObjColour(Col.BLACK);
				eastButton.setBorderPainted(true);
			} else {
				eastButton.setBorderPainted(false);
			}
			if (westButton.isSelected()) {
				dlg.panelMain.mark.setCategory(Cat.CAM_WEST);
				dlg.panelMain.mark.setObjColour(Col.YELLOW);
				dlg.panelMain.mark.addObjColour(Col.BLACK);
				dlg.panelMain.mark.addObjColour(Col.YELLOW);
				westButton.setBorderPainted(true);
			} else {
				westButton.setBorderPainted(false);
			}
			if (isolButton.isSelected()) {
				dlg.panelMain.mark.setCategory(Cat.NONE);
				dlg.panelMain.mark.setObjColour(Col.BLACK);
				dlg.panelMain.mark.addObjColour(Col.RED);
				dlg.panelMain.mark.addObjColour(Col.BLACK);
				isolButton.setBorderPainted(true);
			} else {
				isolButton.setBorderPainted(false);
			}
			alTop.actionPerformed(null);
			alLit.actionPerformed(null);
			dlg.panelMain.panelMore.syncPanel();
		}
	};
	private ButtonGroup shapeButtons = new ButtonGroup();
	public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
	public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
	public JRadioButton canButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CanButton.png")));
	public JRadioButton coneButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeButton.png")));
	public JRadioButton sphereButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereButton.png")));
	public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
	public JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
	public EnumMap<Shp, JRadioButton> shapes = new EnumMap<Shp, JRadioButton>(Shp.class);
	public EnumMap<Shp, Obj> carObjects = new EnumMap<Shp, Obj>(Shp.class);
	public EnumMap<Shp, Obj> isdObjects = new EnumMap<Shp, Obj>(Shp.class);
	private ActionListener alShape = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Shp shp : shapes.keySet()) {
				JRadioButton button = shapes.get(shp);
				if (button.isSelected()) {
					dlg.panelMain.mark.setShape(shp);
					if (isolButton.isSelected())
						dlg.panelMain.mark.setObject(isdObjects.get(shp));
					else
						dlg.panelMain.mark.setObject(carObjects.get(shp));
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			topmarkButton.setVisible(dlg.panelMain.mark.testValid());
			lightButton.setVisible(dlg.panelMain.mark.testValid());
		}
	};
	public JToggleButton topmarkButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/HazTopButton.png")));
	private ActionListener alTop = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (topmarkButton.isSelected()) {
				dlg.panelMain.mark.setTopPattern(Pat.NONE);
				dlg.panelMain.mark.setTopColour(Col.BLACK);
				switch (dlg.panelMain.mark.getCategory()) {
				case CAM_NORTH:
					dlg.panelMain.mark.setTopmark(Top.NORTH);
					break;
				case CAM_SOUTH:
					dlg.panelMain.mark.setTopmark(Top.SOUTH);
					break;
				case CAM_EAST:
					dlg.panelMain.mark.setTopmark(Top.EAST);
					break;
				case CAM_WEST:
					dlg.panelMain.mark.setTopmark(Top.WEST);
					break;
				default:
					dlg.panelMain.mark.setTopmark(Top.SPHERES2);
					break;
				}
				topmarkButton.setBorderPainted(true);
			} else {
				dlg.panelMain.mark.setTopmark(Top.NONE);
				dlg.panelMain.mark.setTopPattern(Pat.NONE);
				dlg.panelMain.mark.setTopColour(Col.UNKNOWN);
				topmarkButton.setBorderPainted(false);
			}
			dlg.panelMain.panelTop.syncPanel();
		}
	};
	public JToggleButton lightButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/DefLitButton.png")));
	private ActionListener alLit = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (lightButton.isSelected()) {
				dlg.panelMain.mark.setLightAtt(Att.COL, 0, Col.WHITE);
				switch (dlg.panelMain.mark.getCategory()) {
				case CAM_NORTH:
					dlg.panelMain.mark.setLightAtt(Att.CHR, 0, "Q");
					dlg.panelMain.mark.setLightAtt(Att.GRP, 0, "");
					break;
				case CAM_SOUTH:
					dlg.panelMain.mark.setLightAtt(Att.CHR, 0, "Q+LFl");
					dlg.panelMain.mark.setLightAtt(Att.GRP, 0, "6");
					break;
				case CAM_EAST:
					dlg.panelMain.mark.setLightAtt(Att.CHR, 0, "Q");
					dlg.panelMain.mark.setLightAtt(Att.GRP, 0, "3");
					break;
				case CAM_WEST:
					dlg.panelMain.mark.setLightAtt(Att.CHR, 0, "Q");
					dlg.panelMain.mark.setLightAtt(Att.GRP, 0, "9");
					break;
				default:
					dlg.panelMain.mark.setLightAtt(Att.CHR, 0, "Fl");
					dlg.panelMain.mark.setLightAtt(Att.GRP, 0, "2");
					break;
				}
				lightButton.setBorderPainted(true);
			} else {
				dlg.panelMain.mark.clrLight();
				lightButton.setBorderPainted(false);
			}
			dlg.panelMain.panelLit.syncPanel();
		}
	};

	public PanelHaz(OSeaMAction dia) {
		dlg = dia;
		setLayout(null);
		add(getCatButton(northButton, 0, 0, 52, 32, "North"));
		add(getCatButton(southButton, 0, 32, 52, 32, "South"));
		add(getCatButton(eastButton, 0, 64, 52, 32, "East"));
		add(getCatButton(westButton, 0, 96, 52, 32, "West"));
		add(getCatButton(isolButton, 0, 128, 52, 32, "Isol"));

		add(getShapeButton(pillarButton, 55, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYCAR, Obj.BOYISD));
		add(getShapeButton(sparButton, 55, 32, 34, 32, "Spar", Shp.SPAR, Obj.BOYCAR, Obj.BOYISD));
		add(getShapeButton(canButton, 55, 64, 34, 32, "Can", Shp.CAN, Obj.BOYCAR, Obj.BOYISD));
		add(getShapeButton(coneButton, 55, 96, 34, 32, "Cone", Shp.CONE, Obj.BOYCAR, Obj.BOYISD));
		add(getShapeButton(sphereButton, 55, 128, 34, 32, "Sphere", Shp.SPHERE, Obj.BOYCAR, Obj.BOYISD));
		add(getShapeButton(floatButton, 90, 0, 34, 32, "Float", Shp.FLOAT, Obj.LITFLT, Obj.LITFLT));
		add(getShapeButton(beaconButton, 90, 32, 34, 32, "Beacon", Shp.BEACON, Obj.BCNCAR, Obj.BCNISD));
		add(getShapeButton(towerButton, 90, 64, 34, 32, "TowerB", Shp.TOWER, Obj.BCNCAR, Obj.BCNISD));

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
		northButton.setBorderPainted(dlg.panelMain.mark.getCategory() == Cat.CAM_NORTH);
		southButton.setBorderPainted(dlg.panelMain.mark.getCategory() == Cat.CAM_SOUTH);
		eastButton.setBorderPainted(dlg.panelMain.mark.getCategory() == Cat.CAM_EAST);
		westButton.setBorderPainted(dlg.panelMain.mark.getCategory() == Cat.CAM_WEST);
		isolButton.setBorderPainted(SeaMark.GrpMAP.get(dlg.panelMain.mark.getObject()) == Grp.ISD);
		for (Shp shp : shapes.keySet()) {
			JRadioButton button = shapes.get(shp);
			button.setBorderPainted(dlg.panelMain.mark.getShape() == shp);
		}
		topmarkButton.setBorderPainted(dlg.panelMain.mark.getTopmark() != Top.NONE);
		topmarkButton.setSelected(dlg.panelMain.mark.getTopmark() != Top.NONE);
		topmarkButton.setVisible(dlg.panelMain.mark.testValid());
		Boolean lit = (dlg.panelMain.mark.getLightAtt(Att.COL, 0) != Col.UNKNOWN) && !((String)dlg.panelMain.mark.getLightAtt(Att.CHR, 0)).isEmpty();
		lightButton.setBorderPainted(lit);
		lightButton.setSelected(lit);
		lightButton.setVisible(dlg.panelMain.mark.testValid());
	}

	private JRadioButton getCatButton(JRadioButton button, int x, int y, int w, int h, String tip) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alCat);
		catButtons.add(button);
		return button;
	}

	private JRadioButton getShapeButton(JRadioButton button, int x, int y, int w, int h, String tip, Shp shp, Obj car, Obj isd) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alShape);
		shapeButtons.add(button);
		shapes.put(shp, button);
		carObjects.put(shp, car);
		isdObjects.put(shp, isd);
		return button;
	}

}
