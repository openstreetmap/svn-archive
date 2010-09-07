//License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig & Malcolm Herring

package toms.seamarks.buoys;

import java.util.Map;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Node;

import toms.dialogs.SmpDialogAction;
import toms.seamarks.SeaMark;

public class BuoySaw extends Buoy {
	public BuoySaw(SmpDialogAction dia,  Node node) {
		super(dia);

		String str;
		Map<String, String> keys;
		keys = node.getKeys();
		setNode(node);
		
		resetMask();

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("Not set");
		dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
		dlg.cbM01StyleOfMark.addItem("Spar Buoy");
		dlg.cbM01StyleOfMark.addItem("Sphere Buoy");
		dlg.cbM01StyleOfMark.addItem("Beacon");
		dlg.cbM01StyleOfMark.addItem("Float");
		dlg.cbM01StyleOfMark.setVisible(true);
		dlg.lM01StyleOfMark.setVisible(true);

		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem("Not set");
		dlg.cbM01Kennung.addItem("Iso");
		dlg.cbM01Kennung.addItem("Oc");
		dlg.cbM01Kennung.addItem("LFl");
		dlg.cbM01Kennung.addItem("Mo()");

		setBuoyIndex(SAFE_WATER);
		setColour(SeaMark.RED_WHITE);
		setLightColour("W");
		setRegion(Main.pref.get("tomsplugin.IALA").equals("B"));

		if (keys.containsKey("name"))
			setName(keys.get("name"));

		if (keys.containsKey("seamark:name"))
			setName(keys.get("seamark:name"));

		if (keys.containsKey("seamark:buoy_safe_water:name"))
			setName(keys.get("seamark:buoy_safe_water:name"));
		else if (keys.containsKey("seamark:beacon_safe_water:name"))
			setName(keys.get("seamark:beacon_safe_water:name"));
		else if (keys.containsKey("seamark:light_float:name"))
			setName(keys.get("seamark:light_float:name"));

		if (keys.containsKey("seamark:buoy_safe_water:shape")) {
			str = keys.get("seamark:buoy_safe_water:shape");

			if (str.equals("pillar"))
				setStyleIndex(SAFE_PILLAR);
			else if (str.equals("spar"))
				setStyleIndex(SAFE_SPAR);
			else if (str.equals("sphere"))
				setStyleIndex(SAFE_SPHERE);
		} else if ((keys.containsKey("seamark:type"))
				&& (keys.get("seamark:type").equals("light_float"))) {
			setStyleIndex(SAFE_FLOAT);
		} else if ((keys.containsKey("seamark:type"))
				&& (keys.get("seamark:type").equals("beacon_safe_water"))) {
			setStyleIndex(SAFE_BEACON);
		}

		if (keys.containsKey("seamark:light:colour")) {
			str = keys.get("seamark:light:colour");

			if (keys.containsKey("seamark:light:character")) {
				setLightGroup(keys);
				String c = keys.get("seamark:light:character");
				if (getLightGroup() != "")
					c = c + "(" + getLightGroup() + ")";
				setLightChar(c);
				setLightPeriod(keys);
			}

			if (str.equals("white")) {
				setFired(true);
				setLightColour("W");
			}
		}

		if (keys.containsKey("seamark:topmark:shape")
				|| keys.containsKey("seamark:topmark:colour")) {
				setTopMark(true);
		}
	}

	public void paintSign() {
		if (dlg.paintlock) return;
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		dlg.tfM01Name.setEnabled(true);
		dlg.tfM01Name.setText(getName());
		dlg.cM01TopMark.setEnabled(true);
		dlg.cM01TopMark.setVisible(true);
		dlg.cM01Radar.setEnabled(true);
		dlg.cM01Radar.setVisible(true);
		dlg.cM01Racon.setEnabled(true);
		dlg.cM01Racon.setVisible(true);
		dlg.cM01Fired.setEnabled(true);

		String image = "/images/Safe_Water";

		switch (getStyleIndex()) {
		case SAFE_PILLAR:
			image += "_Pillar";
			break;
		case SAFE_SPAR:
			image += "_Spar";
			break;
		case SAFE_SPHERE:
			image += "_Sphere";
			break;
		case SAFE_BEACON:
			image += "_Beacon";
			break;
		case SAFE_FLOAT:
			image += "_Float";
			break;
		default:
		}

		if (!image.equals("/images/Safe_Water")) {
			image += ".png";
			dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));

			if (hasTopMark())
				image += "_Sphere";

			if (isFired()) {
				dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource("/images/Light_White_120.png")));
				if (getLightChar() != "") {
					String c;

					c = getLightChar();

					dlg.cbM01Kennung.setSelectedItem(c);
					if (dlg.cbM01Kennung.getSelectedItem().equals("Not set"))
						c = "";
				}
			}
		} else
			dlg.lM01Icon.setIcon(null);
	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		switch (getStyleIndex()) {
		case SAFE_PILLAR:
			super.saveSign("buoy_safe_water");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:shape", "pillar"));
			break;
		case SAFE_SPAR:
			super.saveSign("buoy_safe_water");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:shape", "spar"));
			break;
		case SAFE_SPHERE:
			super.saveSign("buoy_safe_water");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:shape", "sphere"));
			break;
		case SAFE_BEACON:
			super.saveSign("beacon_safe_water");
			break;
		case SAFE_FLOAT:
			super.saveSign("light_float");
			break;
		default:
		}

		switch (getStyleIndex()) {
		case SAFE_PILLAR:
		case SAFE_SPAR:
		case SAFE_SPHERE:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:colour_pattern", "vertical stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:colour", "red;white"));
			break;
		case SAFE_BEACON:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_safe_water:colour_pattern", "vertical stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_safe_water:colour", "red;white"));
			break;
		case SAFE_FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour_pattern", "vertical stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour", "red;white"));
			break;
		default:
		}

		saveTopMarkData("spherical", "red");

		saveLightData("white");

	}

	public void setLightColour() {
		super.setLightColour("W");
	}

}
