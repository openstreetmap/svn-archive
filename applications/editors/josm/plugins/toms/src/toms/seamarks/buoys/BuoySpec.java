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

public class BuoySpec extends Buoy {
	public BuoySpec(SmpDialogAction dia, int type) {
		super(dia);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("Not set");
		dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
		dlg.cbM01StyleOfMark.addItem("Spar Buoy");
		dlg.cbM01StyleOfMark.addItem("Beacon");
		dlg.cbM01StyleOfMark.addItem("Sphere Buoy");
		dlg.cbM01StyleOfMark.addItem("Barrel");

		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem("Not set");

		dlg.cbM01Colour.removeAllItems();
		dlg.cbM01Colour.addItem("W");
		dlg.cbM01Colour.setSelectedIndex(0);
		dlg.cbM01Colour.setEnabled(false);
		dlg.cM01Fired.setSelected(false);
		dlg.cM01TopMark.setSelected(false);

		setColour(SeaMark.YELLOW);
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

		switch (getStyleIndex()) {
		case SPEC_PILLAR:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Special_Purpose_Pillar.png")));
			break;
		case SPEC_SPAR:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Special_Purpose_Spar.png")));
			break;
		case SPEC_BEACON:
			if (isFired())
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Special_Purpose_Beacon_Lit.png")));
			else
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Special_Purpose_Beacon.png")));
			break;
		case SPEC_SPHERE:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Special_Purpose_Sphere.png")));
			break;
		case SPEC_BARREL:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Special_Purpose_Barrel.png")));
			break;
		default:
		}

		if (getLightChar() != "") {
			String c;

			c = getLightChar();

			dlg.cbM01Kennung.setSelectedItem(c);
			if (dlg.cbM01Kennung.getSelectedItem() == "Not set")
				c = "";
		}

		switch (getStyleIndex()) {
		case SPEC_PILLAR:
		case SPEC_SPAR:
			Checker("/images/Cross_Top_Buoy_Yellow.png", "/images/Light_White.png");
			break;
		case SPEC_BEACON:
			Checker("/images/Cross_Top_Post_Yellow.png", "/images/Light_White.png");
			break;
		case SPEC_SPHERE:
		case SPEC_BARREL:
			Checker("/images/Cross_Top_Sphere_Yellow.png", "/images/Light_White.png");
			break;
		default:
		}
	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		if (getStyleIndex() == SPEC_BEACON)
			super.saveSign("beacon_special_purpose");
		else
			super.saveSign("buoy_special_purpose");

		switch (getStyleIndex()) {
		case SPEC_PILLAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "pillar"));
			break;
		case SPEC_SPAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "spar"));
			break;
		case SPEC_SPHERE:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "sphere"));
			break;
		case SPEC_BEACON:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_special_purpose:shape", "stake"));
			break;
		case SPEC_BARREL:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "barrel"));
			break;
		default:
		}
		Main.main.undoRedo.add(new ChangePropertyCommand(node,
				"seamark:buoy_special_purpose:colour", "yellow"));

		saveTopMarkData("cross", "yellow");

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

			if (str.compareTo("cross") == 0) {
				setTopMark(true);

			} else {
				setErrMsg("Parse-Error: Topmark unbekannt");
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
				String ce = c;

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

		if (keys.containsKey("seamark:buoy_special_purpose:shape")) {
			str = keys.get("seamark:buoy_special_purpose:shape");

			if (str.compareTo("pillar") == 0)
				setStyleIndex(SPEC_PILLAR);
			else if (str.compareTo("spar") == 0)
				setStyleIndex(SPEC_SPAR);
			else if (str.compareTo("sphere") == 0)
				setStyleIndex(SPEC_SPHERE);
			else if (str.compareTo("barrel") == 0)
				setStyleIndex(SPEC_BARREL);
			else
				ret = false;
		}

		return ret;
	}

}
