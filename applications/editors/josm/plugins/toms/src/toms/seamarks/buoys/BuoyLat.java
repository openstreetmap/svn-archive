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

public class BuoyLat extends Buoy {
	public BuoyLat(SmpDialogAction dia, int type) {
		super(dia);

		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem("Not set");
		dlg.cbM01Kennung.addItem("Fl");
		dlg.cbM01Kennung.addItem("Fl(2)");
		dlg.cbM01Kennung.addItem("Fl(3)");
		dlg.cbM01Kennung.addItem("Fl(4)");
		dlg.cbM01Kennung.addItem("Fl(5)");
		dlg.cbM01Kennung.addItem("Oc(2)");
		dlg.cbM01Kennung.addItem("Oc(3)");
		dlg.cbM01Kennung.addItem("Q");
		dlg.cbM01Kennung.addItem("IQ");
		dlg.cbM01Kennung.setSelectedIndex(0);

		setBuoyIndex(type);
		setStyleIndex(0);
		setLightColour();
		setFired(false);
		setTopMark(false);
		setRegion(Main.pref.get("tomsplugin.IALA").equals("B"));

		refreshStyles();

		paintSign();
	}

	public void refreshStyles() {
		int type = getBuoyIndex();
		int style = getStyleIndex();

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("Not set");

		switch (type) {
		case PORT_HAND:
			dlg.cbM01StyleOfMark.addItem("Can Buoy");
			dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
			dlg.cbM01StyleOfMark.addItem("Spar Buoy");
			dlg.cbM01StyleOfMark.addItem("Beacon");
			dlg.cbM01StyleOfMark.addItem("Tower");
			dlg.cbM01StyleOfMark.addItem("Float");
			dlg.cbM01StyleOfMark.addItem("Perch");
			break;

		case STARBOARD_HAND:
			dlg.cbM01StyleOfMark.addItem("Cone Buoy");
			dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
			dlg.cbM01StyleOfMark.addItem("Spar Buoy");
			dlg.cbM01StyleOfMark.addItem("Beacon");
			dlg.cbM01StyleOfMark.addItem("Tower");
			dlg.cbM01StyleOfMark.addItem("Float");
			dlg.cbM01StyleOfMark.addItem("Perch");
			break;

		case PREF_PORT_HAND:
			dlg.cbM01StyleOfMark.addItem("Can Buoy");
			dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
			dlg.cbM01StyleOfMark.addItem("Spar Buoy");
			dlg.cbM01StyleOfMark.addItem("Beacon");
			dlg.cbM01StyleOfMark.addItem("Tower");
			dlg.cbM01StyleOfMark.addItem("Float");
			break;

		case PREF_STARBOARD_HAND:
			dlg.cbM01StyleOfMark.addItem("Cone Buoy");
			dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
			dlg.cbM01StyleOfMark.addItem("Spar Buoy");
			dlg.cbM01StyleOfMark.addItem("Beacon");
			dlg.cbM01StyleOfMark.addItem("Tower");
			dlg.cbM01StyleOfMark.addItem("Float");
			break;

		default:
		}

		if (style >= dlg.cbM01StyleOfMark.getItemCount())
			style = 0;
		setStyleIndex(style);
		dlg.cbM01StyleOfMark.setSelectedIndex(style);

	}

	public void paintSign() {
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		dlg.tfM01Name.setEnabled(true);
		dlg.tfM01Name.setText(getName());
		dlg.cM01Fired.setEnabled(true);
		dlg.cM01TopMark.setEnabled(true);

		String image = "/images/Lateral";

		int cat = getBuoyIndex();
		boolean region = getRegion();
		int style = getStyleIndex();

		switch (getBuoyIndex()) {
		case SeaMark.PORT_HAND:
			if (region != SeaMark.IALA_B)
				switch (style) {
				case LAT_CAN:
					image += "_Can_Red";
					break;
				case LAT_PILLAR:
					image += "_Pillar_Red";
					break;
				case LAT_SPAR:
					image += "_Spar_Red";
					break;
				case LAT_BEACON:
					image += "_Beacon_Red";
					break;
				case LAT_TOWER:
					image += "_Tower_Red";
					break;
				case LAT_FLOAT:
					image += "_Float_Red";
					break;
				case LAT_PERCH:
					image += "_Perch_Port";
					break;
				default:
				}
			else
				switch (style) {
				case LAT_CAN:
					image += "_Can_Green";
					break;
				case LAT_PILLAR:
					image += "_Pillar_Green";
					break;
				case LAT_SPAR:
					image += "_Spar_Green";
					break;
				case LAT_BEACON:
					image += "_Beacon_Green";
					break;
				case LAT_TOWER:
					image += "_Tower_Green";
					break;
				case LAT_FLOAT:
					image += "_Float_Green";
					break;
				case LAT_PERCH:
					image += "_Perch_Port";
					break;
				default:
				}
			break;

		case SeaMark.STARBOARD_HAND:
			if (region != SeaMark.IALA_B)
				switch (style) {
				case LAT_CONE:
					image += "_Cone_Green";
					break;
				case LAT_PILLAR:
					image += "_Pillar_Green";
					break;
				case LAT_SPAR:
					image += "_Spar_Green";
					break;
				case LAT_BEACON:
					image += "_Beacon_Green";
					break;
				case LAT_TOWER:
					image += "_Tower_Green";
					break;
				case LAT_FLOAT:
					image += "_Float_Green";
					break;
				case LAT_PERCH:
					image += "_Perch_Starboard";
					break;
				default:
				}
			else
				switch (style) {
				case LAT_CONE:
					image += "_Cone_Red";
					break;
				case LAT_PILLAR:
					image += "_Pillar_Red";
					break;
				case LAT_SPAR:
					image += "_Spar_Red";
					break;
				case LAT_BEACON:
					image += "_Beacon_Red";
					break;
				case LAT_TOWER:
					image += "_Tower_Red";
					break;
				case LAT_FLOAT:
					image += "_Float_Red";
					break;
				case LAT_PERCH:
					image += "_Perch_Starboard";
					break;
				default:
				}
			break;

		case SeaMark.PREF_PORT_HAND:
			if (region != SeaMark.IALA_B)
				switch (style) {
				case LAT_CAN:
					image += "_Can_Red_Green_Red";
					break;
				case LAT_PILLAR:
					image += "_Pillar_Red_Green_Red";
					break;
				case LAT_SPAR:
					image += "_Spar_Red_Green_Red";
					break;
				case LAT_BEACON:
					image += "_Beacon_Red_Green_Red";
					break;
				case LAT_TOWER:
					image += "_Tower_Red_Green_Red";
					break;
				case LAT_FLOAT:
					image += "_Float_Red_Green_Red";
					break;
				default:
				}
			else
				switch (style) {
				case LAT_CAN:
					image += "_Can_Green_Red_Green";
					break;
				case LAT_PILLAR:
					image += "_Pillar_Green_Red_Green";
					break;
				case LAT_SPAR:
					image += "_Spar_Green_Red_Green";
					break;
				case LAT_BEACON:
					image += "_Beacon_Green_Red_Green";
					break;
				case LAT_TOWER:
					image += "_Tower_Green_Red_Green";
					break;
				case LAT_FLOAT:
					image += "_Float_Green_Red_Green";
					break;
				default:
				}
			break;

		case SeaMark.PREF_STARBOARD_HAND:
			if (region != SeaMark.IALA_B)
				switch (style) {
				case LAT_CONE:
					image += "_Cone_Green_Red_Green";
					break;
				case LAT_PILLAR:
					image += "_Pillar_Green_Red_Green";
					break;
				case LAT_SPAR:
					image += "_Spar_Green_Red_Green";
					break;
				case LAT_BEACON:
					image += "_Beacon_Green_Red_Green";
					break;
				case LAT_TOWER:
					image += "_Tower_Green_Red_Green";
					break;
				case LAT_FLOAT:
					image += "_Float_Green_Red_Green";
					break;
				default:
				}
			else
				switch (style) {
				case LAT_CONE:
					image += "_Cone_Red_Green_Red";
					break;
				case LAT_PILLAR:
					image += "_Pillar_Red_Green_Red";
					break;
				case LAT_SPAR:
					image += "_Spar_Red_Green_Red";
					break;
				case LAT_BEACON:
					image += "_Beacon_Red_Green_Red";
					break;
				case LAT_TOWER:
					image += "_Tower_Red_Green_Red";
					break;
				case LAT_FLOAT:
					image += "_Float_Red_Green_Red";
					break;
				default:
				}
			break;

		default:
		}

		if (image != "/images/Lateral") {

			if (hasTopMark()) {
				if (cat == PORT_HAND || cat == PREF_PORT_HAND)
					image += "_Can";
				else
					image += "_Cone";
			}

			if (isFired())
				image += "_Lit";
			if (getLightChar() != "") {
				String c;

				c = getLightChar();
				if (getLightGroup() != "")
					c = c + "(" + getLightGroup() + ")";

				dlg.cbM01Kennung.setSelectedItem(c);
				if (dlg.cbM01Kennung.getSelectedItem() == "Not set")
					c = "";
			}

			image += ".png";

			// System.out.println("finename: " + image);
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(image)));
		} else
			dlg.lM01Icon01.setIcon(null);
	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		int cat = getBuoyIndex();
		String shape = "";
		String colour = "";

		switch (cat) {

		case PORT_HAND:
			switch (getStyleIndex()) {
			case LAT_CAN:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "can"));
				break;
			case LAT_PILLAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar"));
				break;
			case LAT_SPAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar"));
				break;
			case LAT_BEACON:
				super.saveSign("beacon_lateral");
				break;
			case LAT_TOWER:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower"));
				break;
			case LAT_FLOAT:
				super.saveSign("light_float");
				break;
			case LAT_PERCH:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "perch"));
				break;
			default:
			}
			switch (getStyleIndex()) {
			case LAT_CAN:
			case LAT_PILLAR:
			case LAT_SPAR:
			case LAT_PERCH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:category", "port"));
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "green"));
					colour = "green";
				}
				break;
			case LAT_BEACON:
			case LAT_TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "port"));
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "green"));
					colour = "green";
				}
				break;
			case LAT_FLOAT:
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "green"));
					colour = "green";
				}
				break;
			}
			shape = "cylinder";
			break;

		case PREF_PORT_HAND:
			switch (getStyleIndex()) {
			case LAT_CAN:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "can"));
				break;
			case LAT_PILLAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar"));
				break;
			case LAT_SPAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar"));
				break;
			case LAT_BEACON:
				super.saveSign("beacon_lateral");
				break;
			case LAT_TOWER:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower"));
				break;
			case LAT_FLOAT:
				super.saveSign("light_float");
				break;
			default:
			}
			switch (getStyleIndex()) {
			case LAT_CAN:
			case LAT_PILLAR:
			case LAT_SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:category", "preferred_channel_port"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:colour_pattern", "horizontal stripes"));
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "red;green;red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "green;red;green"));
					colour = "green";
				}
				break;
			case LAT_BEACON:
			case LAT_TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "preferred_channel_port"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:colour_pattern", "horizontal stripes"));
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "red;green;red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "green;red;green"));
					colour = "green";
				}
				break;
			case LAT_FLOAT:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour_pattern", "horizontal stripes"));
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "red;green;red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "green;red;green"));
					colour = "green";
				}
				break;
			}
			shape = "cylinder";
			break;

		case STARBOARD_HAND:
			switch (getStyleIndex()) {
			case LAT_CONE:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "conical"));
				break;
			case LAT_PILLAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar"));
				break;
			case LAT_SPAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar"));
				break;
			case LAT_BEACON:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "stake"));
				break;
			case LAT_TOWER:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower"));
				break;
			case LAT_FLOAT:
				super.saveSign("light_float");
				break;
			case LAT_PERCH:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "perch"));
				break;
			default:
			}
			switch (getStyleIndex()) {
			case LAT_CAN:
			case LAT_PILLAR:
			case LAT_SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:category", "starboard"));
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "red"));
					colour = "red";
				}
				break;
			case LAT_BEACON:
			case LAT_TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "starboard"));
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "red"));
					colour = "red";
				}
				break;
			case LAT_FLOAT:
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "red"));
					colour = "red";
				}
				break;
			}
			shape = "cone, point up";
			break;

		case PREF_STARBOARD_HAND:
			switch (getStyleIndex()) {
			case LAT_CONE:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "conical"));
				break;
			case LAT_PILLAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar"));
				break;
			case LAT_SPAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar"));
				break;
			case LAT_BEACON:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "stake"));
				break;
			case LAT_TOWER:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower"));
				break;
			case LAT_FLOAT:
				super.saveSign("light_float");
				break;
			default:
			}
			switch (getStyleIndex()) {
			case LAT_CAN:
			case LAT_PILLAR:
			case LAT_SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:category", "preferred_channel_starboard"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:colour_pattern", "horizontal stripes"));
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "green;red;green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "red;green;red"));
					colour = "red";
				}
				break;
			case LAT_BEACON:
			case LAT_TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "preferred_channel_starboard"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:colour_pattern", "horizontal stripes"));
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "green;red;green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "red;green;red"));
					colour = "red";
				}
				break;
			case LAT_FLOAT:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour_pattern", "horizontal stripes"));
				if (getRegion() != SeaMark.IALA_B) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "green;red;green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "red;green;red"));
					colour = "red";
				}
				break;
			}
			shape = "cone, point up";
			break;

		default:
		}

		saveTopMarkData(shape, colour);
		saveLightData(colour);

		Main.pref.put("tomsplugin.IALA", getRegion() ? "B" : "A");
	}

	public boolean parseTopMark(Node node) {
		if (node == null) {
			return false;
		}

		String str;
		boolean ret = true;
		Map<String, String> keys;

		keys = node.getKeys();

		if (keys.containsKey("seamark:topmark:shape")) {
			str = keys.get("seamark:topmark:shape");

			int cat = getBuoyIndex();
			switch (cat) {
			case PORT_HAND:
			case PREF_PORT_HAND:
				if (str.equals("cylinder")) {
					setTopMark(true);
					setRegion(IALA_A);
				} else if (str.equals("cone, point up")) {
					setTopMark(true);
					setRegion(IALA_B);
				} else {
					setTopMark(false);
					ret = false;
				}
				break;

			case STARBOARD_HAND:
			case PREF_STARBOARD_HAND:
				if (str.equals("cone, point up")) {
					setTopMark(true);
					setRegion(IALA_A);
				} else if (str.equals("cylinder")) {
					setTopMark(true);
					setRegion(IALA_B);
				} else {
					setTopMark(false);
					ret = false;
				}
				break;

			default:
				ret = false;
			}
			if (!hasTopMark()) {
				setErrMsg("Parse-Error: Topmark falsch");
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

			setLightColour(str);

			if (isFired()) {

			} else {
				if (getErrMsg() == null)
					setErrMsg("Parse-Error: Befeuerung falsch");
				else
					setErrMsg(getErrMsg() + " / Befeuerung falsch");
			}

		}

		return ret;
	}

	public void setLightColour() {
		if (getRegion() == IALA_A
				&& (getBuoyIndex() == PORT_HAND || getBuoyIndex() == PREF_PORT_HAND)) {
			super.setLightColour("R");
		} else {
			super.setLightColour("G");
		}
	}

	public void setLightColour(String str) {
		int cat = getBuoyIndex();

		if (str == null) {
			return;
		}

		switch (cat) {
		case PORT_HAND:
		case PREF_PORT_HAND:
			if (getRegion() == IALA_A) {
				if (str.compareTo("red") == 0) {
					setFired(true);
					super.setLightColour("R");
				} else {
					super.setLightColour("");
				}
			} else {
				if (str.compareTo("green") == 0) {
					setFired(true);
					super.setLightColour("G");
				} else {
					super.setLightColour("");
				}
			}
			break;

		case STARBOARD_HAND:
		case PREF_STARBOARD_HAND:
			if (getRegion() == IALA_A) {
				if (str.compareTo("green") == 0) {
					setFired(true);
					super.setLightColour("G");
				} else {
					super.setLightColour("");
				}
			} else {
				if (str.compareTo("red") == 0) {
					setFired(true);
					super.setLightColour("R");
				} else {
					super.setLightColour("");
				}
			}
			break;

		default:
		}

	}

	public boolean parseShape(Node node) {
		String str;
		boolean ret = true;
		Map<String, String> keys;

		keys = node.getKeys();

		if (keys.containsKey("seamark:buoy_lateral:shape")) {
			str = keys.get("seamark:buoy_lateral:shape");

			int cat = getBuoyIndex();
			switch (cat) {
			case PORT_HAND:
				if (str.compareTo("can") == 0)
					setStyleIndex(LAT_CAN);
				else if (str.compareTo("pillar") == 0)
					setStyleIndex(LAT_PILLAR);
				else if (str.compareTo("spar") == 0)
					setStyleIndex(LAT_SPAR);
				else
					ret = false;
				break;

			case PREF_PORT_HAND:
				if (str.compareTo("can") == 0)
					setStyleIndex(LAT_CAN);
				else if (str.compareTo("pillar") == 0)
					setStyleIndex(LAT_PILLAR);
				else if (str.compareTo("spar") == 0)
					setStyleIndex(LAT_SPAR);
				else
					ret = false;
				break;

			case STARBOARD_HAND:
				if (str.compareTo("conical") == 0)
					setStyleIndex(LAT_CONE);
				else if (str.compareTo("pillar") == 0)
					setStyleIndex(LAT_PILLAR);
				else if (str.compareTo("spar") == 0)
					setStyleIndex(LAT_SPAR);
				else
					ret = false;
				break;

			case PREF_STARBOARD_HAND:
				if (str.compareTo("conical") == 0)
					setStyleIndex(LAT_CONE);
				else if (str.compareTo("pillar") == 0)
					setStyleIndex(LAT_PILLAR);
				else if (str.compareTo("spar") == 0)
					setStyleIndex(LAT_SPAR);
				else
					ret = false;
				break;

			default:
				ret = false;
			}
		} else if (keys.containsKey("seamark:beacon_lateral:colour")) {
			if (keys.containsKey("seamark:beacon_lateral:shape")) {
				str = keys.get("seamark:beacon_lateral:shape");
				if (str.compareTo("tower") == 0)
					setStyleIndex(LAT_TOWER);
				else
					setStyleIndex(LAT_BEACON);
			} else
				setStyleIndex(LAT_BEACON);
		} else if ((keys.containsKey("seamark:type") == true)
				&& (keys.get("seamark:type").equals("light_float"))) {
			setStyleIndex(LAT_FLOAT);
		}
		return ret;
	}
}
