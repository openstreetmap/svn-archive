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
				if (dlg.node != null && (idx == categoryBox.getSelectedIndex()))
					dlg.panelMain.mark.setCategory(cat);
			}
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
					dlg.panelMain.mark.setObject(objects.get(shp));
					if (button == cairnButton) {
						dlg.panelMain.mark.setObjPattern(Pat.NONE);
						dlg.panelMain.mark.setObjColour(Col.UNKNOWN);
					}
					if (dlg.panelMain.mark.getObjColour(0) == Col.UNKNOWN) {
						dlg.panelMain.mark.setObjPattern(Pat.NONE);
						dlg.panelMain.mark.setObjColour(Col.YELLOW);
					}
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			topmarkButton.setVisible(dlg.panelMain.mark.testValid());
			dlg.panelMain.panelMore.syncPanel();
		}
	};
	public JToggleButton topmarkButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/SpecTopButton.png")));
	private ActionListener alTop = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (topmarkButton.isSelected()) {
				dlg.panelMain.mark.setTopmark(Top.X_SHAPE);
				dlg.panelMain.mark.setTopPattern(Pat.NONE);
				dlg.panelMain.mark.setTopColour(Col.YELLOW);
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

	public PanelSpec(OSeaMAction dia) {
		dlg = dia;
		setLayout(null);
		add(getShapeButton(pillarButton, 0, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYSPP));
		add(getShapeButton(sparButton, 34, 0, 34, 32, "Spar", Shp.SPAR, Obj.BOYSPP));
		add(getShapeButton(canButton, 68, 0, 34, 32, "Can", Shp.CAN, Obj.BOYSPP));
		add(getShapeButton(coneButton, 102, 0, 34, 32, "Cone", Shp.CONE, Obj.BOYSPP));
		add(getShapeButton(sphereButton, 0, 32, 34, 32, "Sphere", Shp.SPHERE, Obj.BOYSPP));
		add(getShapeButton(barrelButton, 34, 32, 34, 32, "Barrel", Shp.BARREL, Obj.BOYSPP));
		add(getShapeButton(superButton, 68, 32, 34, 32, "Super", Shp.SUPER, Obj.BOYSPP));
		add(getShapeButton(floatButton, 102, 32, 34, 32, "Float", Shp.FLOAT, Obj.LITFLT));
		add(getShapeButton(beaconButton, 0, 64, 34, 32, "Beacon", Shp.BEACON, Obj.BCNSPP));
		add(getShapeButton(towerButton, 34, 64, 34, 32, "TowerB", Shp.TOWER, Obj.BCNSPP));
		add(getShapeButton(stakeButton, 68, 64, 34, 32, "Stake", Shp.STAKE, Obj.BCNSPP));
		add(getShapeButton(cairnButton, 102, 64, 34, 32, "CairnB", Shp.CAIRN, Obj.BCNSPP));

		categoryLabel = new JLabel(Messages.getString("Category"), SwingConstants.CENTER);
		categoryLabel.setBounds(new Rectangle(5, 100, 160, 20));
		add(categoryLabel);
		categoryBox = new JComboBox();
		categoryBox.setBounds(new Rectangle(5, 120, 160, 20));
		add(categoryBox);
		categoryBox.addActionListener(alCategoryBox);
		addCatItem("", Cat.NONE);
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

		topmarkButton.setBounds(new Rectangle(136, 0, 34, 32));
		topmarkButton.setBorder(BorderFactory.createLoweredBevelBorder());
		topmarkButton.addActionListener(alTop);
		add(topmarkButton);
	}

	public void syncPanel() {
		for (Shp shp : shapes.keySet()) {
			JRadioButton button = shapes.get(shp);
			if (dlg.panelMain.mark.getShape() == shp) {
				button.setBorderPainted(true);
			} else
				button.setBorderPainted(false);
		}
		for (Cat cat : categories.keySet()) {
			int item = categories.get(cat);
			if (dlg.panelMain.mark.getCategory() == cat)
				categoryBox.setSelectedIndex(item);
		}
		topmarkButton.setBorderPainted(dlg.panelMain.mark.getTopmark() != Top.NONE);
		topmarkButton.setSelected(dlg.panelMain.mark.getTopmark() != Top.NONE);
		topmarkButton.setVisible(dlg.panelMain.mark.testValid());
	}

	private void addCatItem(String str, Cat cat) {
		categories.put(cat, categoryBox.getItemCount());
		categoryBox.addItem(str);
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
