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
	public JLabel categoryLabel;
	public JComboBox categoryBox;
	public EnumMap<Cat, Integer> categories = new EnumMap<Cat, Integer>(Cat.class);
	private ActionListener alCategoryBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Cat cat : categories.keySet()) {
				int idx = categories.get(cat);
				if (dlg.mark != null && (idx == categoryBox.getSelectedIndex()))
					dlg.mark.setCategory(cat);
			}
			checkValidity();
		}
	};

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
	public ActionListener alShape = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Shp shp : shapes.keySet()) {
				JRadioButton button = shapes.get(shp);
				if (button.isSelected()) {
					dlg.mark.setShape(shp);
					dlg.mark.setObject(objects.get(shp));
					if ((button == cairnButton) && !(dlg.panelMain.panelMore.panelPat.panelCol.delButton.isSelected()))
						dlg.panelMain.panelMore.panelPat.panelCol.delButton.doClick();
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			checkValidity();
		}
	};

	public PanelSpec(OSeaMAction dia) {
		dlg = dia;

		this.setLayout(null);
		this.add(getShapeButton(pillarButton, 0, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYSPP), null);
		this.add(getShapeButton(sparButton, 35, 0, 34, 32, "Spar", Shp.SPAR, Obj.BOYSPP), null);
		this.add(getShapeButton(canButton, 70, 0, 34, 32, "Can", Shp.CAN, Obj.BOYSPP), null);
		this.add(getShapeButton(coneButton, 105, 0, 34, 32, "Cone", Shp.CONE, Obj.BOYSPP), null);
		this.add(getShapeButton(sphereButton, 140, 0, 34, 32, "Sphere", Shp.SPHERE, Obj.BOYSPP), null);
		this.add(getShapeButton(barrelButton, 35, 32, 34, 32, "Barrel", Shp.BARREL, Obj.BOYSPP), null);
		this.add(getShapeButton(superButton, 70, 32, 34, 32, "Super", Shp.SUPER, Obj.BOYSPP), null);
		this.add(getShapeButton(floatButton, 105, 32, 34, 32, "Float", Shp.FLOAT, Obj.LITFLT), null);
		this.add(getShapeButton(beaconButton, 17, 64, 34, 32, "Beacon", Shp.BEACON, Obj.BCNSPP), null);
		this.add(getShapeButton(towerButton, 52, 64, 34, 32, "Tower", Shp.TOWER, Obj.BCNSPP), null);
		this.add(getShapeButton(stakeButton, 87, 64, 34, 32, "Stake", Shp.STAKE, Obj.BCNSPP), null);
		this.add(getShapeButton(cairnButton, 122, 64, 34, 32, "Cairn", Shp.CAIRN, Obj.BCNSPP), null);

		categoryLabel = new JLabel(Messages.getString("Category"), SwingConstants.CENTER);
		categoryLabel.setBounds(new Rectangle(5, 100, 170, 20));
		this.add(categoryLabel, null);
		categoryBox = new JComboBox();
		categoryBox.setBounds(new Rectangle(5, 120, 170, 20));
		this.add(categoryBox, null);
		categoryBox.addActionListener(alCategoryBox);
		addCatItem(Messages.getString("UKPurpose"), Cat.SPM_UNKN);
		addCatItem(Messages.getString("Warning"), Cat.SPM_WARN);
		addCatItem(Messages.getString("ChanSeparation"), Cat.SPM_CHBF);
		addCatItem(Messages.getString("Yachting"), Cat.SPM_YCHT);
		addCatItem(Messages.getString("Cable"), Cat.SPM_CABL);
		addCatItem(Messages.getString("Outfall"), Cat.SPM_OFAL);
		addCatItem(Messages.getString("ODAS"), Cat.SPM_ODAS);
		addCatItem(Messages.getString("RecreationZone"), Cat.SPM_RECN);
		addCatItem(Messages.getString("Mooring"), Cat.SPM_MOOR);
		addCatItem(Messages.getString("LANBY"), Cat.SPM_LNBY);
		addCatItem(Messages.getString("Leading"), Cat.SPM_LDNG);
		addCatItem(Messages.getString("Notice"), Cat.SPM_NOTC);
		addCatItem(Messages.getString("TSS"), Cat.SPM_TSS);
		addCatItem(Messages.getString("FoulGround"), Cat.SPM_FOUL);
		addCatItem(Messages.getString("Diving"), Cat.SPM_DIVE);
		addCatItem(Messages.getString("FerryCross"), Cat.SPM_FRRY);
		addCatItem(Messages.getString("Anchorage"), Cat.SPM_ANCH);
	}

	private void checkValidity() {
		if (dlg.mark != null) {
			if ((dlg.mark.getObject() != Obj.UNKNOWN) && (dlg.mark.getShape() != Shp.UNKNOWN)) {
				dlg.panelMain.topButton.setEnabled(true);
				dlg.panelMain.fogButton.setEnabled(true);
				dlg.panelMain.radButton.setEnabled(true);
				dlg.panelMain.litButton.setEnabled(true);
				dlg.panelMain.moreButton.setVisible(true);
			} else {
				dlg.panelMain.topButton.setEnabled(false);
				dlg.panelMain.fogButton.setEnabled(false);
				dlg.panelMain.radButton.setEnabled(false);
				dlg.panelMain.litButton.setEnabled(false);
				dlg.panelMain.moreButton.setVisible(false);
			}
			dlg.mark.paintSign();
		}
	}
	
	public void updateSelections() {
		if (dlg.mark != null) {
			if (dlg.mark.getObject() == Obj.UNKNOWN) {
				clearSelections();
				dlg.panelMain.panelMore.panelPat.panelCol.yellowButton.doClick();
			} else {
				dlg.panelMain.panelMore.panelPat.panelCol.colours.get(dlg.mark.getColour(Ent.BODY, 0)).doClick();
			}
			if (shapes.containsKey(dlg.mark.getShape())) {
				shapes.get(dlg.mark.getShape()).doClick();
			} else {
				shapeButtons.clearSelection();
				alShape.actionPerformed(null);
			}
		} else
			clearSelections();
	}

	private void addCatItem(String str, Cat cat) {
		categories.put(cat, categoryBox.getItemCount());
		categoryBox.addItem(str);
	}

	public void clearSelections() {
		shapeButtons.clearSelection();
		alShape.actionPerformed(null);
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
