package oseam.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelSaw extends JPanel {

	private OSeaMAction dlg;
	public ButtonGroup shapeButtons = new ButtonGroup();
	public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
	public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
	public JRadioButton sphereButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
	public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
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
				dlg.panelMain.mark.setObjPattern(Pat.VSTRP);
				dlg.panelMain.mark.setObjColour(Col.RED);
				dlg.panelMain.mark.addObjColour(Col.WHITE);
			} else {
				dlg.panelMain.panelChan.topmarkButton.setVisible(false);
			}
			dlg.panelMain.panelMore.syncPanel();
		}
	};

	public PanelSaw(OSeaMAction dia) {
		dlg = dia;
		setLayout(null);
		add(getShapeButton(pillarButton, 0, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYSAW));
		add(getShapeButton(sparButton, 0, 32, 34, 32, "Spar", Shp.SPAR, Obj.BOYSAW));
		add(getShapeButton(sphereButton, 0, 64, 34, 32, "Sphere", Shp.SPHERI, Obj.BOYSAW));
		add(getShapeButton(floatButton, 0, 96, 34, 32, "Float", Shp.FLOAT, Obj.FLTSAW));
		add(getShapeButton(beaconButton, 0, 128, 34, 32, "Beacon", Shp.BEACON, Obj.BCNSAW));
	}

	public void syncPanel() {
		for (Shp shp : shapes.keySet()) {
			JRadioButton button = shapes.get(shp);
			if (dlg.panelMain.mark.getShape() == shp) {
				button.setBorderPainted(true);
			} else
				button.setBorderPainted(false);
		}
		dlg.panelMain.mark.testValid();
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
