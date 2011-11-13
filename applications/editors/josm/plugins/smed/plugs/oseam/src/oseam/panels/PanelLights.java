package oseam.panels;

import java.awt.event.*;
import java.awt.*;

import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;
import oseam.seamarks.SeaMark.*;

public class PanelLights extends JPanel {

	private OSeaMAction dlg;

	public JLabel categoryLabel;

	public JComboBox trafficCatBox;
	public EnumMap<Cat, Integer> trafficCats = new EnumMap<Cat, Integer>(Cat.class);
	private ActionListener alTrafficCatBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Cat cat : trafficCats.keySet()) {
				int idx = trafficCats.get(cat);
				if (dlg.mark != null && (idx == trafficCatBox.getSelectedIndex()))
					dlg.mark.setCategory(cat);
			}
		}
	};

	public JComboBox warningCatBox;
	public EnumMap<Cat, Integer> warningCats = new EnumMap<Cat, Integer>(Cat.class);
	private ActionListener alWarningCatBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Cat cat : warningCats.keySet()) {
				int idx = warningCats.get(cat);
				if (dlg.mark != null && (idx == warningCatBox.getSelectedIndex()))
					dlg.mark.setCategory(cat);
			}
		}
	};

	private ButtonGroup objButtons = new ButtonGroup();
	public JRadioButton houseButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LighthouseButton.png")));
	public JRadioButton majorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMajorButton.png")));
	public JRadioButton minorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMinorButton.png")));
	public JRadioButton vesselButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightVesselButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightFloatButton.png")));
	public JRadioButton trafficButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TrafficButton.png")));
	public JRadioButton warningButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/WarningButton.png")));
	public EnumMap<Obj, JRadioButton> objects = new EnumMap<Obj, JRadioButton>(Obj.class);
	private ActionListener alObj = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Obj obj : objects.keySet()) {
				JRadioButton button = objects.get(obj);
				if (button.isSelected()) {
					dlg.mark.setObject(obj);
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			if (trafficButton.isSelected()) {
				categoryLabel.setVisible(true);
				trafficCatBox.setVisible(true);
				warningCatBox.setVisible(false);
			} else if (warningButton.isSelected()) {
				categoryLabel.setVisible(true);
				warningCatBox.setVisible(true);
				trafficCatBox.setVisible(false);
			} else {
				categoryLabel.setVisible(false);
				trafficCatBox.setVisible(false);
				warningCatBox.setVisible(false);
			}
			dlg.mark.testValid();
			dlg.mark.paintSign();
		}
	};

	public PanelLights(OSeaMAction dia) {
		dlg = dia;
		this.setLayout(null);
		this.add(getObjButton(houseButton, 0, 0, 34, 32, "Lighthouse", Obj.LNDMRK), null);
		this.add(getObjButton(majorButton, 35, 0, 34, 32, "MajorLight", Obj.LITMAJ), null);
		this.add(getObjButton(minorButton, 70, 0, 34, 32, "MinorLight", Obj.LITMIN), null);
		this.add(getObjButton(vesselButton, 105, 0, 34, 32, "LightVessel", Obj.LITVES), null);
		this.add(getObjButton(floatButton, 140, 0, 34, 32, "LightFloat", Obj.LITFLT), null);
		this.add(getObjButton(trafficButton, 50, 35, 34, 32, "SSTraffic", Obj.SISTAT), null);
		this.add(getObjButton(warningButton, 90, 35, 34, 32, "SSWarning", Obj.SISTAW), null);

		categoryLabel = new JLabel(Messages.getString("SSCategory"), SwingConstants.CENTER);
		categoryLabel.setBounds(new Rectangle(10, 80, 160, 20));
		this.add(categoryLabel, null);
		categoryLabel.setVisible(false);

		trafficCatBox = new JComboBox();
		trafficCatBox.setBounds(new Rectangle(10, 100, 160, 20));
		this.add(trafficCatBox, null);
		trafficCatBox.addActionListener(alTrafficCatBox);
		addTCItem("", Cat.NONE);
		addTCItem(Messages.getString("Traffic"), Cat.SIS_TRFC);
		addTCItem(Messages.getString("PortControl"), Cat.SIS_PTCL);
		addTCItem(Messages.getString("PortEntry"), Cat.SIS_PTED);
		addTCItem(Messages.getString("IPT"), Cat.SIS_IPT);
		addTCItem(Messages.getString("Berthing"), Cat.SIS_BRTH);
		addTCItem(Messages.getString("Dock"), Cat.SIS_DOCK);
		addTCItem(Messages.getString("Lock"), Cat.SIS_LOCK);
		addTCItem(Messages.getString("Barrage"), Cat.SIS_FBAR);
		addTCItem(Messages.getString("Bridge"), Cat.SIS_BRDG);
		addTCItem(Messages.getString("Dredging"), Cat.SIS_DRDG);
		trafficCatBox.setVisible(false);

		warningCatBox = new JComboBox();
		warningCatBox.setBounds(new Rectangle(10, 100, 160, 20));
		this.add(warningCatBox, null);
		warningCatBox.addActionListener(alWarningCatBox);
		addWCItem("", Cat.NONE);
		addWCItem(Messages.getString("Danger"), Cat.SIS_DNGR);
		addWCItem(Messages.getString("Storm"), Cat.SIS_STRM);
		addWCItem(Messages.getString("Weather"), Cat.SIS_WTHR);
		addWCItem(Messages.getString("Obstruction"), Cat.SIS_OBST);
		addWCItem(Messages.getString("Cable"), Cat.SIS_CABL);
		addWCItem(Messages.getString("Distress"), Cat.SIS_DSTR);
		addWCItem(Messages.getString("Time"), Cat.SIS_TIME);
		addWCItem(Messages.getString("Tide"), Cat.SIS_TIDE);
		addWCItem(Messages.getString("TidalStream"), Cat.SIS_TSTM);
		addWCItem(Messages.getString("TideGauge"), Cat.SIS_TGAG);
		addWCItem(Messages.getString("TideScale"), Cat.SIS_TSCL);
		addWCItem(Messages.getString("Diving"), Cat.SIS_DIVE);
		addWCItem(Messages.getString("Ice"), Cat.SIS_ICE);
		addWCItem(Messages.getString("LevelGauge"), Cat.SIS_LGAG);
		addWCItem(Messages.getString("Military"), Cat.SIS_MILY);
		warningCatBox.setVisible(false);

	}

	public void syncPanel() {
		trafficCatBox.setSelectedIndex(0);
		for (Cat cat : trafficCats.keySet()) {
			int item = trafficCats.get(cat);
			if (dlg.mark.getCategory() == cat)
				trafficCatBox.setSelectedIndex(item);
		}
		warningCatBox.setSelectedIndex(0);
		for (Cat cat : warningCats.keySet()) {
			int item = warningCats.get(cat);
			if (dlg.mark.getCategory() == cat)
				warningCatBox.setSelectedIndex(item);
		}
		for (Obj obj : objects.keySet()) {
			JRadioButton button = objects.get(obj);
			button.setBorderPainted(dlg.mark.getObject() == obj);
		}
		dlg.mark.testValid();
	}
	
	private void addTCItem(String str, Cat cat) {
		trafficCats.put(cat, trafficCatBox.getItemCount());
		trafficCatBox.addItem(str);
	}

	private void addWCItem(String str, Cat cat) {
		warningCats.put(cat, warningCatBox.getItemCount());
		warningCatBox.addItem(str);
	}

	private JRadioButton getObjButton(JRadioButton button, int x, int y, int w, int h, String tip, Obj obj) {
		button.setBounds(new Rectangle(x, y, w, h));
		button.setBorder(BorderFactory.createLoweredBevelBorder());
		button.setToolTipText(Messages.getString(tip));
		button.addActionListener(alObj);
		objButtons.add(button);
		objects.put(obj, button);
		return button;
	}

}
