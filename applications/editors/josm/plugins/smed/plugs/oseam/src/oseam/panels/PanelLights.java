package oseam.panels;

import java.awt.event.*;
import java.awt.*;

import javax.swing.*;

import java.util.*;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class PanelLights extends JPanel {

	private OSeaMAction dlg;

	public JLabel categoryLabel;

	public JComboBox landCatBox;
	public EnumMap<Cat, Integer> landCats = new EnumMap<Cat, Integer>(Cat.class);
	private ActionListener alLandCatBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Cat cat : landCats.keySet()) {
				int idx = landCats.get(cat);
				if (dlg.node != null && (idx == landCatBox.getSelectedIndex())) {
					dlg.panelMain.mark.setCategory(cat);
					dlg.panelMain.mark.testValid();
				}
			}
		}
	};
	public JComboBox trafficCatBox;
	public EnumMap<Cat, Integer> trafficCats = new EnumMap<Cat, Integer>(Cat.class);
	private ActionListener alTrafficCatBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Cat cat : trafficCats.keySet()) {
				int idx = trafficCats.get(cat);
				if (dlg.node != null && (idx == trafficCatBox.getSelectedIndex())) {
					dlg.panelMain.mark.setCategory(cat);
					dlg.panelMain.mark.testValid();
				}
			}
		}
	};
	public JComboBox warningCatBox;
	public EnumMap<Cat, Integer> warningCats = new EnumMap<Cat, Integer>(Cat.class);
	private ActionListener alWarningCatBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Cat cat : warningCats.keySet()) {
				int idx = warningCats.get(cat);
				if (dlg.node != null && (idx == warningCatBox.getSelectedIndex())) {
					dlg.panelMain.mark.setCategory(cat);
					dlg.panelMain.mark.testValid();
				}
			}
		}
	};
	public JComboBox platformCatBox;
	public EnumMap<Cat, Integer> platformCats = new EnumMap<Cat, Integer>(Cat.class);
	private ActionListener alPlatformCatBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Cat cat : platformCats.keySet()) {
				int idx = platformCats.get(cat);
				if (dlg.node != null && (idx == platformCatBox.getSelectedIndex())) {
					dlg.panelMain.mark.setCategory(cat);
					dlg.panelMain.mark.testValid();
				}
			}
		}
	};
	public JComboBox pilotCatBox;
	public EnumMap<Cat, Integer> pilotCats = new EnumMap<Cat, Integer>(Cat.class);
	private ActionListener alPilotCatBox = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Cat cat : pilotCats.keySet()) {
				int idx = pilotCats.get(cat);
				if (dlg.node != null && (idx == pilotCatBox.getSelectedIndex())) {
					dlg.panelMain.mark.setCategory(cat);
					dlg.panelMain.mark.testValid();
				}
			}
		}
	};
	private ButtonGroup objButtons = new ButtonGroup();
	public JRadioButton houseButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LighthouseButton.png")));
	public JRadioButton majorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMajorButton.png")));
	public JRadioButton minorButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightMinorButton.png")));
	public JRadioButton vesselButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightVesselButton.png")));
	public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LightFloatButton.png")));
	public JRadioButton landButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/LandmarkButton.png")));
	public JRadioButton trafficButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TrafficButton.png")));
	public JRadioButton warningButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/WarningButton.png")));
	public JRadioButton platformButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PlatformButton.png")));
	public JRadioButton coastguardButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CoastguardButton.png")));
	public JRadioButton pilotButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PilotButton.png")));
	public JRadioButton rescueButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RescueButton.png")));
	public JRadioButton radioButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RadioStationButton.png")));
	public JRadioButton radarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/RadarStationButton.png")));
	public EnumMap<Obj, JRadioButton> objects = new EnumMap<Obj, JRadioButton>(Obj.class);
	private ActionListener alObj = new ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			for (Obj obj : objects.keySet()) {
				JRadioButton button = objects.get(obj);
				if (button.isSelected()) {
					dlg.panelMain.mark.setObject(obj);
					button.setBorderPainted(true);
				} else
					button.setBorderPainted(false);
			}
			if (dlg.panelMain.mark.getObject() == Obj.LITVES)
				dlg.panelMain.mark.setShape(Shp.SUPER);
			else if (dlg.panelMain.mark.getObject() == Obj.LITFLT)
				dlg.panelMain.mark.setShape(Shp.FLOAT);
			else dlg.panelMain.mark.setShape(Shp.UNKSHP);
			categoryLabel.setVisible(false);
			landCatBox.setVisible(false);
			trafficCatBox.setVisible(false);
			warningCatBox.setVisible(false);
			platformCatBox.setVisible(false);
			pilotCatBox.setVisible(false);
			dlg.panelMain.mark.setCategory(Cat.NOCAT);
			if (landButton.isSelected()) {
				categoryLabel.setVisible(true);
				landCatBox.setVisible(true);
				alLandCatBox.actionPerformed(null);
			} else if (trafficButton.isSelected()) {
				categoryLabel.setVisible(true);
				trafficCatBox.setVisible(true);
				alTrafficCatBox.actionPerformed(null);
			} else if (warningButton.isSelected()) {
				categoryLabel.setVisible(true);
				warningCatBox.setVisible(true);
				alWarningCatBox.actionPerformed(null);
			} else if (platformButton.isSelected()) {
				categoryLabel.setVisible(true);
				platformCatBox.setVisible(true);
				alPlatformCatBox.actionPerformed(null);
			} else if (pilotButton.isSelected()) {
				categoryLabel.setVisible(true);
				pilotCatBox.setVisible(true);
				alPilotCatBox.actionPerformed(null);
			}
			dlg.panelMain.mark.testValid();
		}
	};

	public PanelLights(OSeaMAction dia) {
		dlg = dia;
		setLayout(null);
		add(getObjButton(houseButton, 0, 0, 34, 32, "Lighthouse", Obj.LITHSE));
		add(getObjButton(majorButton, 34, 0, 34, 32, "MajorLight", Obj.LITMAJ));
		add(getObjButton(minorButton, 68, 0, 34, 32, "MinorLight", Obj.LITMIN));
		add(getObjButton(landButton, 102, 0, 34, 32, "Landmark", Obj.LNDMRK));
		add(getObjButton(platformButton, 136, 0, 34, 32, "Platform", Obj.OFSPLF));
		add(getObjButton(vesselButton, 0, 35, 34, 32, "LightVessel", Obj.LITVES));
		add(getObjButton(floatButton, 34, 35, 34, 32, "LightFloat", Obj.LITFLT));
		add(getObjButton(trafficButton, 68, 35, 34, 32, "SSTraffic", Obj.SISTAT));
		add(getObjButton(warningButton, 102, 35, 34, 32, "SSWarning", Obj.SISTAW));
		add(getObjButton(coastguardButton, 0, 70, 34, 32, "Coastguard", Obj.CGUSTA));
		add(getObjButton(pilotButton, 34, 70, 34, 32, "PilotBoarding", Obj.PILBOP));
		add(getObjButton(rescueButton, 68, 70, 34, 32, "RescueStation", Obj.RSCSTA));
		add(getObjButton(radioButton, 102, 70, 34, 32, "RadioStation", Obj.RDOSTA));
		add(getObjButton(radarButton, 136, 70, 34, 32, "RadarStation", Obj.RADSTA));

		categoryLabel = new JLabel(Messages.getString("Category"), SwingConstants.CENTER);
		categoryLabel.setBounds(new Rectangle(5, 110, 160, 20));
		add(categoryLabel);
		categoryLabel.setVisible(false);

		landCatBox = new JComboBox();
		landCatBox.setBounds(new Rectangle(5, 130, 160, 20));
		add(landCatBox);
		landCatBox.addActionListener(alLandCatBox);
		addLCItem("", Cat.NOCAT);
		addLCItem(Messages.getString("Tower"), Cat.LMK_TOWR);
		addLCItem(Messages.getString("WaterTower"), Cat.LMK_WTRT);
		addLCItem(Messages.getString("Chimney"), Cat.LMK_CHMY);
		addLCItem(Messages.getString("Mast"), Cat.LMK_MAST);
		addLCItem(Messages.getString("Column"), Cat.LMK_CLMN);
		addLCItem(Messages.getString("DishAerial"), Cat.LMK_DSHA);
		addLCItem(Messages.getString("Flagstaff"), Cat.LMK_FLGS);
		addLCItem(Messages.getString("FlareStack"), Cat.LMK_FLRS);
		addLCItem(Messages.getString("Monument"), Cat.LMK_MNMT);
		addLCItem(Messages.getString("WindMotor"), Cat.LMK_WNDM);
		addLCItem(Messages.getString("WindSock"), Cat.LMK_WNDS);
		addLCItem(Messages.getString("Obelisk"), Cat.LMK_OBLK);
		addLCItem(Messages.getString("Statue"), Cat.LMK_STAT);
		addLCItem(Messages.getString("Cross"), Cat.LMK_CROS);
		addLCItem(Messages.getString("Dome"), Cat.LMK_DOME);
		addLCItem(Messages.getString("RadarScanner"), Cat.LMK_SCNR);
		addLCItem(Messages.getString("Windmill"), Cat.LMK_WNDL);
		addLCItem(Messages.getString("SpireMinaret"), Cat.LMK_SPIR);
		addLCItem(Messages.getString("Cairn"), Cat.LMK_CARN);
		landCatBox.setVisible(false);

		trafficCatBox = new JComboBox();
		trafficCatBox.setBounds(new Rectangle(5, 130, 160, 20));
		add(trafficCatBox);
		trafficCatBox.addActionListener(alTrafficCatBox);
		addTCItem("", Cat.NOCAT);
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
		warningCatBox.setBounds(new Rectangle(5, 130, 160, 20));
		add(warningCatBox);
		warningCatBox.addActionListener(alWarningCatBox);
		addWCItem("", Cat.NOCAT);
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

		platformCatBox = new JComboBox();
		platformCatBox.setBounds(new Rectangle(5, 130, 160, 20));
		add(platformCatBox);
		platformCatBox.addActionListener(alPlatformCatBox);
		addPLItem("", Cat.NOCAT);
		addPLItem(Messages.getString("Oil"), Cat.OFP_OIL);
		addPLItem(Messages.getString("Production"), Cat.OFP_PRD);
		addPLItem(Messages.getString("Observation"), Cat.OFP_OBS);
		addPLItem(Messages.getString("ALP"), Cat.OFP_ALP);
		addPLItem(Messages.getString("SALM"), Cat.OFP_SALM);
		addPLItem(Messages.getString("MooringTower"), Cat.OFP_MOR);
		addPLItem(Messages.getString("ArtificialIsland"), Cat.OFP_ISL);
		addPLItem(Messages.getString("FPSO"), Cat.OFP_FPSO);
		addPLItem(Messages.getString("Accommodation"), Cat.OFP_ACC);
		addPLItem(Messages.getString("NCCB"), Cat.OFP_NCCB);
		platformCatBox.setVisible(false);

		pilotCatBox = new JComboBox();
		pilotCatBox.setBounds(new Rectangle(5, 130, 160, 20));
		add(pilotCatBox);
		pilotCatBox.addActionListener(alPilotCatBox);
		addPTItem("", Cat.NOCAT);
		addPTItem(Messages.getString("CruisingVessel"), Cat.PIL_VESS);
		addPTItem(Messages.getString("Helicopter"), Cat.PIL_HELI);
		addPTItem(Messages.getString("FromShore"), Cat.PIL_SHORE);
		pilotCatBox.setVisible(false);
	}

	public void syncPanel() {
		categoryLabel.setVisible(false);
		landCatBox.setVisible(false);
		trafficCatBox.setVisible(false);
		warningCatBox.setVisible(false);
		platformCatBox.setVisible(false);
		pilotCatBox.setVisible(false);
		if ((dlg.panelMain.mark.getObject() == Obj.LNDMRK) && (dlg.panelMain.mark.getCategory() != Cat.NOCAT)) {
			categoryLabel.setVisible(true);
			landCatBox.setVisible(true);
			for (Cat cat : landCats.keySet()) {
				int item = landCats.get(cat);
				if (dlg.panelMain.mark.getCategory() == cat)
					landCatBox.setSelectedIndex(item);
			}
		} else if (dlg.panelMain.mark.getObject() == Obj.SISTAT) {
				categoryLabel.setVisible(true);
				trafficCatBox.setVisible(true);
				for (Cat cat : trafficCats.keySet()) {
					int item = trafficCats.get(cat);
					if (dlg.panelMain.mark.getCategory() == cat)
						trafficCatBox.setSelectedIndex(item);
				}
		} else if (dlg.panelMain.mark.getObject() == Obj.SISTAW) {
			categoryLabel.setVisible(true);
			warningCatBox.setVisible(true);
			for (Cat cat : warningCats.keySet()) {
				int item = warningCats.get(cat);
				if (dlg.panelMain.mark.getCategory() == cat)
					warningCatBox.setSelectedIndex(item);
			}
		} else if (dlg.panelMain.mark.getObject() == Obj.OFSPLF) {
			categoryLabel.setVisible(true);
			platformCatBox.setVisible(true);
			for (Cat cat : platformCats.keySet()) {
				int item = platformCats.get(cat);
				if (dlg.panelMain.mark.getCategory() == cat)
					platformCatBox.setSelectedIndex(item);
			}
		} else if (dlg.panelMain.mark.getObject() == Obj.PILBOP) {
			categoryLabel.setVisible(true);
			pilotCatBox.setVisible(true);
			for (Cat cat : pilotCats.keySet()) {
				int item = pilotCats.get(cat);
				if (dlg.panelMain.mark.getCategory() == cat)
					pilotCatBox.setSelectedIndex(item);
			}
		} else {
		}
		for (Obj obj : objects.keySet()) {
			JRadioButton button = objects.get(obj);
			button.setBorderPainted(dlg.panelMain.mark.getObject() == obj);
		}
		dlg.panelMain.mark.testValid();
	}
	
	private void addLCItem(String str, Cat cat) {
		landCats.put(cat, landCatBox.getItemCount());
		landCatBox.addItem(str);
	}

	private void addTCItem(String str, Cat cat) {
		trafficCats.put(cat, trafficCatBox.getItemCount());
		trafficCatBox.addItem(str);
	}

	private void addWCItem(String str, Cat cat) {
		warningCats.put(cat, warningCatBox.getItemCount());
		warningCatBox.addItem(str);
	}

	private void addPLItem(String str, Cat cat) {
		platformCats.put(cat, platformCatBox.getItemCount());
		platformCatBox.addItem(str);
	}

	private void addPTItem(String str, Cat cat) {
		pilotCats.put(cat, pilotCatBox.getItemCount());
		pilotCatBox.addItem(str);
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
