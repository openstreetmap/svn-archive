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
	public BuoySaw(SmpDialogAction dia, int type) {
		super(dia);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("Not set");
		dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
		dlg.cbM01StyleOfMark.addItem("Spar Buoy");
		dlg.cbM01StyleOfMark.addItem("Sphere Buoy");
		dlg.cbM01StyleOfMark.addItem("Beacon");
		dlg.cbM01StyleOfMark.addItem("Float");

		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem("Not set");
		dlg.cbM01Kennung.addItem("Iso");
		dlg.cbM01Kennung.addItem("Oc");
		dlg.cbM01Kennung.addItem("LFl");
		dlg.cbM01Kennung.addItem("Mo()");

		dlg.cM01Fired.setSelected(false);
		dlg.cM01TopMark.setSelected(false);
		dlg.tbM01Region.setEnabled(false);

		setColour(SeaMark.RED_WHITE);
		setLightColour("W");
		setBuoyIndex(type);

		paintSign();
	}

	public void paintSign() {
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		dlg.tfM01Name.setEnabled(true);
		dlg.tfM01Name.setText(getName());
		dlg.cM01Fired.setEnabled(true);
		dlg.cM01TopMark.setEnabled(true);

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

		if (image != "/images/Safe_Water") {

			if (hasTopMark())
				image += "_Sphere";

			if (isFired()) {
				image += "_Lit";
				if (getLightChar() != "") {
					String c;

					c = getLightChar();

					dlg.cbM01Kennung.setSelectedItem(c);
					if (dlg.cbM01Kennung.getSelectedItem().equals("Not set"))
						c = "";
				}
			}
			image += ".png";

			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(image)));
		} else
			dlg.lM01Icon01.setIcon(null);
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

	public boolean parseTopMark(Node node) {
		if (node == null) {
			return false;
		}

		String str;
		boolean ret = true;
		Map<String, String> keys;

		setTopMark(false);

		keys = node.getKeys();
		if (keys.containsKey("seamark:topmark:shape")) {
			str = keys.get("seamark:topmark:shape");

			if (str.compareTo("spherical") == 0 || str.compareTo("sphere") == 0) {
				setTopMark(true);

			} else {
				setErrMsg("Parse-Error: Unknown topmark");
				ret = false;
			}
		}

		return ret;
	}

	public boolean parseLight(Node node) {
		String str;
		boolean ret = true;
		Map<String, String> keys;

		setFired(false);

		keys = node.getKeys();
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

			if (str.compareTo("white") == 0) {
				setFired(true);
				setLightColour("W");

			} else {
				if (getErrMsg() == null)
					setErrMsg("Parse-Error: Licht falsch");
				else
					setErrMsg(getErrMsg() + " / Licht falsch");

				ret = false;
			}

		}

		return ret;
	}

	public void setLightColour() {
		super.setLightColour("W");
	}

	public boolean parseShape(Node node) {
		String str;
		boolean ret = true;
		Map<String, String> keys;

		keys = node.getKeys();

		if (keys.containsKey("seamark:buoy_safe_water:shape")) {
			str = keys.get("seamark:buoy_safe_water:shape");

			if (str.compareTo("pillar") == 0)
				setStyleIndex(SAFE_PILLAR);
			else if (str.compareTo("spar") == 0)
				setStyleIndex(SAFE_SPAR);
			else if (str.compareTo("sphere") == 0)
				setStyleIndex(SAFE_SPHERE);
			else
				ret = false;
		} else if ((keys.containsKey("seamark:type") == true)
				&& (keys.get("seamark:type").equals("light_float"))) {
			setStyleIndex(SAFE_FLOAT);
		} else if ((keys.containsKey("seamark:type") == true)
				&& (keys.get("seamark:type").equals("beacon_safe_water"))) {
			setStyleIndex(SAFE_BEACON);
		}

		return ret;
	}

}
