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

public class BuoyIsol extends Buoy {
	public BuoyIsol(SmpDialogAction dia, int type) {
		super(dia);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("Not set");
		dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
		dlg.cbM01StyleOfMark.addItem("Spar Buoy");
		dlg.cbM01StyleOfMark.addItem("Beacon");

		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem("Not set");
		dlg.cbM01Kennung.addItem("Fl(2)");

		dlg.cbM01Colour.removeAllItems();
		dlg.cbM01Colour.addItem("W");
		dlg.cbM01Colour.setSelectedIndex(0);
		dlg.cbM01Colour.setEnabled(false);
		dlg.cM01Fired.setSelected(false);
		dlg.cM01TopMark.setSelected(true);
		dlg.cM01TopMark.setEnabled(false);

		setColour(SeaMark.BLACK_RED_BLACK);
		setLightColour("W");
		setBuoyIndex(type);
		setTopMark(true);

		paintSign();
	}

	public void paintSign() {
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		dlg.tfM01Name.setEnabled(true);
		dlg.tfM01Name.setText(getName());
		dlg.cM01Fired.setEnabled(true);
		dlg.cM01TopMark.setEnabled(false);

		switch (getStyleIndex()) {
		case ISOL_PILLAR:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Isolated_Danger_Pillar.png")));
			break;
		case ISOL_SPAR:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Isolated_Danger_Spar.png")));
			break;
		case ISOL_BEACON:
			if (isFired())
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Isolated_Danger_Beacon_Lit.png")));
			else
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Isolated_Danger_Beacon.png")));
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
		case ISOL_PILLAR:
		case ISOL_SPAR:
			Checker("/images/Danger_Top_Buoy.png", "/images/Light_White.png");
			break;
		case ISOL_BEACON:
			Checker("/images/Danger_Top_Post.png", "/images/Light_White.png");
			break;
		default:
		}
	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		super.saveSign("buoy_isolated_danger");

		switch (getStyleIndex()) {
		case ISOL_PILLAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "pillar"));
			break;
		case ISOL_SPAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "spar"));
			break;
		case ISOL_BEACON:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "beacon"));
			break;
		default:
		}
		Main.main.undoRedo.add(new ChangePropertyCommand(node,
				"seamark:buoy_isolated_danger:colour_pattern", "horizontal stripes"));
		Main.main.undoRedo.add(new ChangePropertyCommand(node,
				"seamark:buoy_isolated_danger:colour", "black;red;black"));

		saveTopMarkData("2 spheres", "black");

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

			if (str.compareTo("2 spheres") == 0) {
				setTopMark(true);

			} else {
				setErrMsg("Parse-Error: Topmark invalid");
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

		if (keys.containsKey("seamark:buoy_isolated_danger:shape")) {
			str = keys.get("seamark:buoy_isolated_danger:shape");

			if (str.compareTo("pillar") == 0)
				setStyleIndex(ISOL_PILLAR);
			else if (str.compareTo("spar") == 0)
				setStyleIndex(ISOL_SPAR);
			else if (str.compareTo("beacon") == 0)
				setStyleIndex(ISOL_BEACON);
			else
				ret = false;
		}

		return ret;
	}

}
