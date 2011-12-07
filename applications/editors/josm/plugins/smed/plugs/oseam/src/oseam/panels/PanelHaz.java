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
			dlg.mark.setObjPattern(Pat.HORIZ);
			if (northButton.isSelected()) {
				dlg.mark.setCategory(Cat.CAM_NORTH);
				dlg.mark.setObjColour(Col.BLACK);
				dlg.mark.addObjColour(Col.YELLOW);
				northButton.setBorderPainted(true);
			} else {
				northButton.setBorderPainted(false);
			}
			if (southButton.isSelected()) {
				dlg.mark.setCategory(Cat.CAM_SOUTH);
				dlg.mark.setObjColour(Col.YELLOW);
				dlg.mark.addObjColour(Col.BLACK);
				southButton.setBorderPainted(true);
			} else {
				southButton.setBorderPainted(false);
			}
			if (eastButton.isSelected()) {
				dlg.mark.setCategory(Cat.CAM_EAST);
				dlg.mark.setObjColour(Col.BLACK);
				dlg.mark.addObjColour(Col.YELLOW);
				dlg.mark.addObjColour(Col.BLACK);
				eastButton.setBorderPainted(true);
			} else {
				eastButton.setBorderPainted(false);
			}
			if (westButton.isSelected()) {
				dlg.mark.setCategory(Cat.CAM_WEST);
				dlg.mark.setObjColour(Col.YELLOW);
				dlg.mark.addObjColour(Col.BLACK);
				dlg.mark.addObjColour(Col.YELLOW);
				westButton.setBorderPainted(true);
			} else {
				westButton.setBorderPainted(false);
			}
			if (isolButton.isSelected()) {
				dlg.mark.setCategory(Cat.NONE);
				dlg.mark.setObjColour(Col.BLACK);
				dlg.mark.addObjColour(Col.RED);
				dlg.mark.addObjColour(Col.BLACK);
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
					dlg.mark.setShape(shp);
					if (isolButton.isSelected())
						dlg.mark.setObject(isdObjects.get(shp));
					else
						dlg.mark.setObject(carObjects.get(shp));
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			topmarkButton.setVisible(dlg.mark.testValid());
			lightButton.setVisible(dlg.mark.testValid());
		}
	};
	public JToggleButton topmarkButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/HazTopButton.png")));
	private ActionListener alTop = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (topmarkButton.isSelected()) {
				dlg.mark.setTopPattern(Pat.NONE);
				dlg.mark.setTopColour(Col.BLACK);
				switch (dlg.mark.getCategory()) {
				case CAM_NORTH:
					dlg.mark.setTopmark(Top.NORTH);
					break;
				case CAM_SOUTH:
					dlg.mark.setTopmark(Top.SOUTH);
					break;
				case CAM_EAST:
					dlg.mark.setTopmark(Top.EAST);
					break;
				case CAM_WEST:
					dlg.mark.setTopmark(Top.WEST);
					break;
				default:
					dlg.mark.setTopmark(Top.SPHERES2);
					break;
				}
				topmarkButton.setBorderPainted(true);
			} else {
				dlg.mark.setTopmark(Top.NONE);
				dlg.mark.setTopPattern(Pat.NONE);
				dlg.mark.setTopColour(Col.UNKNOWN);
				topmarkButton.setBorderPainted(false);
			}
			dlg.panelMain.panelTop.syncPanel();
		}
	};
	public JToggleButton lightButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/DefLitButton.png")));
	private ActionListener alLit = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (lightButton.isSelected()) {
				dlg.mark.setLightAtt(Att.COL, 0, Col.WHITE);
				switch (dlg.mark.getCategory()) {
				case CAM_NORTH:
					dlg.mark.setLightAtt(Att.CHR, 0, "Q");
					dlg.mark.setLightAtt(Att.GRP, 0, "");
					break;
				case CAM_SOUTH:
					dlg.mark.setLightAtt(Att.CHR, 0, "Q+LFl");
					dlg.mark.setLightAtt(Att.GRP, 0, "6");
					break;
				case CAM_EAST:
					dlg.mark.setLightAtt(Att.CHR, 0, "Q");
					dlg.mark.setLightAtt(Att.GRP, 0, "3");
					break;
				case CAM_WEST:
					dlg.mark.setLightAtt(Att.CHR, 0, "Q");
					dlg.mark.setLightAtt(Att.GRP, 0, "9");
					break;
				default:
					dlg.mark.setLightAtt(Att.CHR, 0, "Fl");
					dlg.mark.setLightAtt(Att.GRP, 0, "2");
					break;
				}
				lightButton.setBorderPainted(true);
			} else {
				dlg.mark.clrLight();
				lightButton.setBorderPainted(false);
			}
			dlg.panelMain.panelLit.syncPanel();
		}
	};

	public PanelHaz(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getCatButton(northButton, 0, 0, 52, 32, "North"), null);
		this.add(getCatButton(southButton, 0, 32, 52, 32, "South"), null);
		this.add(getCatButton(eastButton, 0, 64, 52, 32, "East"), null);
		this.add(getCatButton(westButton, 0, 96, 52, 32, "West"), null);
		this.add(getCatButton(isolButton, 0, 128, 52, 32, "Isol"), null);

		this.add(getShapeButton(pillarButton, 55, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYCAR, Obj.BOYISD), null);
		this.add(getShapeButton(sparButton, 55, 32, 34, 32, "Spar", Shp.SPAR, Obj.BOYCAR, Obj.BOYISD), null);
		this.add(getShapeButton(canButton, 55, 64, 34, 32, "Can", Shp.CAN, Obj.BOYCAR, Obj.BOYISD), null);
		this.add(getShapeButton(coneButton, 55, 96, 34, 32, "Cone", Shp.CONE, Obj.BOYCAR, Obj.BOYISD), null);
		this.add(getShapeButton(sphereButton, 55, 128, 34, 32, "Sphere", Shp.SPHERE, Obj.BOYCAR, Obj.BOYISD), null);
		this.add(getShapeButton(floatButton, 90, 0, 34, 32, "Float", Shp.FLOAT, Obj.LITFLT, Obj.LITFLT), null);
		this.add(getShapeButton(beaconButton, 90, 32, 34, 32, "Beacon", Shp.BEACON, Obj.BCNCAR, Obj.BCNISD), null);
		this.add(getShapeButton(towerButton, 90, 64, 34, 32, "TowerB", Shp.TOWER, Obj.BCNCAR, Obj.BCNISD), null);

		topmarkButton.setBounds(new Rectangle(130, 0, 34, 32));
		topmarkButton.setToolTipText(Messages.getString("Topmark"));
		topmarkButton.setBorder(BorderFactory.createLoweredBevelBorder());
		topmarkButton.addActionListener(alTop);
		topmarkButton.setVisible(false);
		this.add(topmarkButton);
		lightButton.setBounds(new Rectangle(130, 32, 34, 32));
		lightButton.setToolTipText(Messages.getString("Light"));
		lightButton.setBorder(BorderFactory.createLoweredBevelBorder());
		lightButton.addActionListener(alLit);
		lightButton.setVisible(false);
		this.add(lightButton);
	}

	public void syncPanel() {
		northButton.setBorderPainted(dlg.mark.getCategory() == Cat.CAM_NORTH);
		southButton.setBorderPainted(dlg.mark.getCategory() == Cat.CAM_SOUTH);
		eastButton.setBorderPainted(dlg.mark.getCategory() == Cat.CAM_EAST);
		westButton.setBorderPainted(dlg.mark.getCategory() == Cat.CAM_WEST);
		isolButton.setBorderPainted(SeaMark.GrpMAP.get(dlg.mark.getObject()) == Grp.ISD);
		for (Shp shp : shapes.keySet()) {
			JRadioButton button = shapes.get(shp);
			button.setBorderPainted(dlg.mark.getShape() == shp);
		}
		topmarkButton.setBorderPainted(dlg.mark.getTopmark() != Top.NONE);
		topmarkButton.setSelected(dlg.mark.getTopmark() != Top.NONE);
		topmarkButton.setVisible(dlg.mark.testValid());
		Boolean lit = (dlg.mark.getLightAtt(Att.COL, 0) != Col.UNKNOWN) && !((String)dlg.mark.getLightAtt(Att.CHR, 0)).isEmpty();
		lightButton.setBorderPainted(lit);
		lightButton.setSelected(lit);
		lightButton.setVisible(dlg.mark.testValid());
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
