package oseam.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelSpec extends JPanel {

	private OSeaMAction dlg;
	public ButtonGroup shapeButtons = new ButtonGroup();
	public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
	public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
	public JRadioButton canButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CanButton.png")));
	public JRadioButton coneButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeButton.png")));
	public JRadioButton sphereButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereButton.png")));
	public JRadioButton barrelButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BarrelButton.png")));
	public JRadioButton superButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SuperButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
	public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
	public JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
	public JRadioButton stakeButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/StakeButton.png")));
	public JRadioButton cairnButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CairnButton.png")));
	public EnumMap<Shp, JRadioButton> shapes = new EnumMap<Shp, JRadioButton>(Shp.class);
	public EnumMap<Shp, Obj> objects = new EnumMap<Shp, Obj>(Shp.class);
	public PanelCol panelCol = null;
	public ActionListener alShape = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Shp shp : shapes.keySet()) {
				JRadioButton button = shapes.get(shp);
				if (button.isSelected()) {
					dlg.mark.setShape(shp);
					dlg.mark.setObject(objects.get(shp));
					if ((button == cairnButton) && !(panelCol.offButton.isSelected()))
						panelCol.offButton.doClick();
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			if (dlg.mark != null)
				dlg.mark.paintSign();
		}
	};

	public PanelSpec(OSeaMAction dia) {
		dlg = dia;
		panelCol = new PanelCol(dlg, alShape, Ent.BODY);
		panelCol.setBounds(new Rectangle(0, 0, 34, 160));

		this.setLayout(null);
		this.add(panelCol, null);
		this.add(getShapeButton(pillarButton, 35, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYSPP), null);
		this.add(getShapeButton(sparButton, 35, 32, 34, 32, "Spar", Shp.SPAR, Obj.BOYSPP), null);
		this.add(getShapeButton(canButton, 35, 64, 34, 32, "Can", Shp.CAN, Obj.BOYSPP), null);
		this.add(getShapeButton(coneButton, 35, 96, 34, 32, "Cone", Shp.CONE, Obj.BOYSPP), null);
		this.add(getShapeButton(sphereButton, 70, 0, 34, 32, "Sphere", Shp.SPHERE, Obj.BOYSPP), null);
		this.add(getShapeButton(barrelButton, 70, 32, 34, 32, "Barrel", Shp.BARREL, Obj.BOYSPP), null);
		this.add(getShapeButton(superButton, 70, 64, 34, 32, "Super", Shp.SUPER, Obj.BOYSPP), null);
		this.add(getShapeButton(floatButton, 70, 96, 34, 32, "Float", Shp.FLOAT, Obj.LITFLT), null);
		this.add(getShapeButton(beaconButton, 105, 0, 34, 32, "Beacon", Shp.BEACON, Obj.BCNSPP), null);
		this.add(getShapeButton(towerButton, 105, 32, 34, 32, "Tower", Shp.TOWER, Obj.BCNSPP), null);
		this.add(getShapeButton(stakeButton, 105, 64, 34, 32, "Stake", Shp.STAKE, Obj.BCNSPP), null);
		this.add(getShapeButton(cairnButton, 105, 96, 34, 32, "Cairn", Shp.CAIRN, Obj.BCNSPP), null);
	}

	public void clearSelections() {
		shapeButtons.clearSelection();
		alShape.actionPerformed(null);
	}

	private JRadioButton getShapeButton(JRadioButton button, int x, int y, int w, int h, String tip, Shp shp, Obj obj) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLineBorder(Color.magenta, 2));
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alShape);
		shapeButtons.add(button);
		shapes.put(shp, button);
		objects.put(shp, obj);
		return button;
	}

}
