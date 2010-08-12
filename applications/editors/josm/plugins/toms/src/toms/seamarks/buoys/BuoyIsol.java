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
		dlg.cbM01StyleOfMark.addItem("Tower");
		dlg.cbM01StyleOfMark.addItem("Float");

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

		String image = "/images/Cardinal";

		switch (getStyleIndex()) {
		case ISOL_PILLAR:
			image += "_Pillar_Single";
			break;
		case ISOL_SPAR:
			image += "_Spar_Single";
			break;
		case ISOL_BEACON:
			image += "_Beacon_Single";
			break;
		case ISOL_TOWER:
			image += "_Tower_Single";
			break;
		case ISOL_FLOAT:
			image += "_Float_Single";
			break;
		default:
		}

		if (image != "/images/Cardinal") {
			if (isFired()) {
				image += "_Lit";
				if (getLightChar() != "") {
					String c;

					c = getLightChar();

					dlg.cbM01Kennung.setSelectedItem(c);
					if (dlg.cbM01Kennung.getSelectedItem() == "Not set")
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
		case ISOL_PILLAR:
			super.saveSign("buoy_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "pillar"));
			break;
		case ISOL_SPAR:
			super.saveSign("buoy_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "spar"));
			break;
		case ISOL_BEACON:
			super.saveSign("beacon_isolated_danger");
			break;
		case ISOL_TOWER:
			super.saveSign("beacon_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:shape", "tower"));
			break;
		case ISOL_FLOAT:
			super.saveSign("light_float");
			break;
		default:
		}
		
		switch (getStyleIndex()) {
		case ISOL_PILLAR:
		case ISOL_SPAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:colour_pattern", "horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:colour", "black;red;black"));
			break;
		case ISOL_BEACON:
		case ISOL_TOWER:
			Main.main.undoRedo
					.add(new ChangePropertyCommand(node,
							"seamark:beacon_isolated_danger:colour_pattern",
							"horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:colour", "black;red;black"));
			break;
		case ISOL_FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour_pattern", "horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour", "black;red;black"));
			break;
		}

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
				setLightChar(c);
				setLightPeriod(keys);
			}

			if (str.compareTo("white") == 0) {
				setFired(true);
				setLightColour("W");

			} else {
				if (getErrMsg() == null)
					setErrMsg("Parse-Error: Invalid light");
				else
					setErrMsg(getErrMsg() + " / Invalid light");

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
			else
				ret = false;
		}
		if (keys.containsKey("seamark:beacon_isolated_danger:shape")) {
			str = keys.get("seamark:beacon_isolated_danger:shape");

			if (str.compareTo("stake") == 0)
				setStyleIndex(ISOL_BEACON);
			else
				ret = false;
		}

		return ret;
	}

}
