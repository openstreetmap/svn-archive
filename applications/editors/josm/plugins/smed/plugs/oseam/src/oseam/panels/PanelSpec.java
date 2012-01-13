package oseam.panels;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;
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
				if (dlg.node != null && (idx == categoryBox.getSelectedIndex()))
					dlg.panelMain.mark.setCategory(cat);
			}
		}
	};
	public JComboBox mooringBox;
	public EnumMap<Cat, Integer> moorings = new EnumMap<Cat, Integer>(Cat.class);
	private ActionListener alMooringBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Cat cat : moorings.keySet()) {
				int idx = moorings.get(cat);
				if (dlg.node != null && (idx == mooringBox.getSelectedIndex())) {
					dlg.panelMain.mark.setCategory(cat);
					if ((cat == Cat.INB_CALM) || (cat == Cat.INB_SBM)) {
						dlg.panelMain.mark.setObject(Obj.BOYINB);
					} else {
						dlg.panelMain.mark.setObject(Obj.MORFAC);
					}
				}
			}
			if (dlg.node != null) syncPanel();
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
					dlg.panelMain.mark.setShape(shp);
					if (SeaMark.EntMAP.get(dlg.panelMain.mark.getObject()) != Ent.MOORING) {
						dlg.panelMain.mark.setObject(objects.get(shp));
						if (dlg.panelMain.mark.getObjColour(0) == Col.UNKCOL) {
							dlg.panelMain.mark.setObjPattern(Pat.NOPAT);
							dlg.panelMain.mark.setObjColour(Col.YELLOW);
						}
						if (button == cairnButton) {
							dlg.panelMain.mark.setObjPattern(Pat.NOPAT);
							dlg.panelMain.mark.setObjColour(Col.UNKCOL);
						}
						topmarkButton.setVisible(dlg.panelMain.mark.testValid());
					}
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			dlg.panelMain.panelMore.syncPanel();
		}
	};
	public JToggleButton topmarkButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/SpecTopButton.png")));
	private ActionListener alTop = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (topmarkButton.isSelected()) {
				dlg.panelMain.mark.setTopmark(Top.X_SHAPE);
				dlg.panelMain.mark.setTopPattern(Pat.NOPAT);
				dlg.panelMain.mark.setTopColour(Col.YELLOW);
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
	public JToggleButton mooringButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/MooringButton.png")));
	private ActionListener alMooring = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			dlg.panelMain.mark.setObject(Obj.UNKOBJ);
			dlg.panelMain.mark.setCategory(Cat.NOCAT);
			dlg.panelMain.mark.setTopmark(Top.NOTOP);
			if (mooringButton.isSelected()) {
				categoryBox.setVisible(false);
				mooringBox.setVisible(true);
				mooringButton.setBorderPainted(true);
			} else {
				mooringBox.setVisible(false);
				categoryBox.setVisible(true);
				mooringButton.setBorderPainted(false);
			}
		}
	};

	public PanelSpec(OSeaMAction dia) {
		dlg = dia;
		setLayout(null);
		add(getShapeButton(pillarButton, 0, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYSPP));
		add(getShapeButton(sparButton, 34, 0, 34, 32, "Spar", Shp.SPAR, Obj.BOYSPP));
		add(getShapeButton(canButton, 68, 0, 34, 32, "Can", Shp.CAN, Obj.BOYSPP));
		add(getShapeButton(coneButton, 102, 0, 34, 32, "Cone", Shp.CONI, Obj.BOYSPP));
		add(getShapeButton(sphereButton, 0, 32, 34, 32, "Sphere", Shp.SPHERI, Obj.BOYSPP));
		add(getShapeButton(barrelButton, 34, 32, 34, 32, "Barrel", Shp.BARREL, Obj.BOYSPP));
		add(getShapeButton(superButton, 68, 32, 34, 32, "Super", Shp.SUPER, Obj.BOYSPP));
		add(getShapeButton(floatButton, 102, 32, 34, 32, "Float", Shp.FLOAT, Obj.LITFLT));
		add(getShapeButton(beaconButton, 0, 64, 34, 32, "Beacon", Shp.BEACON, Obj.BCNSPP));
		add(getShapeButton(towerButton, 34, 64, 34, 32, "TowerB", Shp.TOWER, Obj.BCNSPP));
		add(getShapeButton(stakeButton, 68, 64, 34, 32, "Stake", Shp.STAKE, Obj.BCNSPP));
		add(getShapeButton(cairnButton, 102, 64, 34, 32, "CairnB", Shp.CAIRN, Obj.BCNSPP));

		categoryLabel = new JLabel(Messages.getString("Category"), SwingConstants.CENTER);
		categoryLabel.setBounds(new Rectangle(5, 110, 160, 20));
		add(categoryLabel);
		categoryBox = new JComboBox();
		categoryBox.setBounds(new Rectangle(5, 130, 160, 20));
		add(categoryBox);
		categoryBox.setVisible(true);
		categoryBox.addActionListener(alCategoryBox);
		addCatItem("", Cat.NOCAT);
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
		mooringBox = new JComboBox();
		mooringBox.setBounds(new Rectangle(5, 130, 160, 20));
		add(mooringBox);
		mooringBox.setVisible(false);
		mooringBox.addActionListener(alMooringBox);
		addMorItem("", Cat.NOCAT);
		addMorItem(Messages.getString("Dolphin"), Cat.MOR_DLPN);
		addMorItem(Messages.getString("DevDolphin"), Cat.MOR_DDPN);
		addMorItem(Messages.getString("Bollard"), Cat.MOR_BLRD);
		addMorItem(Messages.getString("Wall"), Cat.MOR_WALL);
		addMorItem(Messages.getString("Post"), Cat.MOR_POST);
		addMorItem(Messages.getString("Chain"), Cat.MOR_CHWR);
		addMorItem(Messages.getString("Rope"), Cat.MOR_ROPE);
		addMorItem(Messages.getString("Automatic"), Cat.MOR_AUTO);
		addMorItem(Messages.getString("MooringBuoy"), Cat.MOR_BUOY);
		addMorItem(Messages.getString("CALM"), Cat.INB_CALM);
		addMorItem(Messages.getString("SBM"), Cat.INB_SBM);

		topmarkButton.setBounds(new Rectangle(136, 0, 34, 32));
		topmarkButton.setToolTipText(Messages.getString("Topmark"));
		topmarkButton.setBorder(BorderFactory.createLoweredBevelBorder());
		topmarkButton.addActionListener(alTop);
		add(topmarkButton);

		mooringButton.setBounds(new Rectangle(136, 64, 34, 32));
		mooringButton.setToolTipText(Messages.getString("Mooring"));
		mooringButton.setBorder(BorderFactory.createLoweredBevelBorder());
		mooringButton.addActionListener(alMooring);
		add(mooringButton);
	}

	public void syncPanel() {
		if (SeaMark.EntMAP.get(dlg.panelMain.mark.getObject()) == Ent.MOORING) {
			mooringButton.setBorderPainted(true);
			categoryBox.setVisible(false);
			mooringBox.setVisible(true);
			topmarkButton.setVisible(false);
			for (Cat cat : moorings.keySet()) {
				int item = moorings.get(cat);
				if (dlg.panelMain.mark.getCategory() == cat)
					mooringBox.setSelectedIndex(item);
			}
		} else {
			mooringButton.setBorderPainted(false);
			mooringBox.setVisible(false);
			categoryBox.setVisible(true);
			topmarkButton.setBorderPainted(dlg.panelMain.mark.getTopmark() != Top.NOTOP);
			topmarkButton.setSelected(dlg.panelMain.mark.getTopmark() != Top.NOTOP);
			topmarkButton.setVisible(dlg.panelMain.mark.testValid());
			for (Cat cat : categories.keySet()) {
				int item = categories.get(cat);
				if (dlg.panelMain.mark.getCategory() == cat)
					categoryBox.setSelectedIndex(item);
			}
		}
		for (Shp shp : shapes.keySet()) {
			JRadioButton button = shapes.get(shp);
			if (dlg.panelMain.mark.getShape() == shp) {
				button.setBorderPainted(true);
			} else
				button.setBorderPainted(false);
		}
		dlg.panelMain.mark.testValid();
	}

	private void addCatItem(String str, Cat cat) {
		categories.put(cat, categoryBox.getItemCount());
		categoryBox.addItem(str);
	}

	private void addMorItem(String str, Cat cat) {
		moorings.put(cat, mooringBox.getItemCount());
		mooringBox.addItem(str);
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
