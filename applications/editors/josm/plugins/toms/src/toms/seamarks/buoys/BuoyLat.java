//License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig & Malcolm Herring

package toms.seamarks.buoys;

import java.util.Map;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Node;

import toms.dialogs.SmpDialogAction;

public class BuoyLat extends Buoy {
	public BuoyLat(SmpDialogAction dia, Node node) {
		super(dia);

		String str;
		Map<String, String> keys;
		keys = node.getKeys();
		setNode(node);

		resetMask();

		dlg.rbM01RegionA.setEnabled(true);
		dlg.rbM01RegionB.setEnabled(true);
		dlg.cbM01CatOfMark.setEnabled(true);
		dlg.cbM01CatOfMark.setVisible(true);
		dlg.lM01CatOfMark.setVisible(true);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("Not set");
		dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
		dlg.cbM01StyleOfMark.addItem("Spar Buoy");
		dlg.cbM01StyleOfMark.addItem("Beacon");
		dlg.cbM01StyleOfMark.addItem("Tower");
		dlg.cbM01StyleOfMark.addItem("Float");
		dlg.cbM01StyleOfMark.setEnabled(true);

		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem("Not set");
		dlg.cbM01Kennung.addItem("Fl");
		dlg.cbM01Kennung.addItem("Fl()");
		dlg.cbM01Kennung.addItem("Oc");
		dlg.cbM01Kennung.addItem("Oc()");
		dlg.cbM01Kennung.addItem("Q");
		dlg.cbM01Kennung.addItem("IQ");
		dlg.cbM01Kennung.addItem("Mo()");
		dlg.cbM01Kennung.setSelectedIndex(0);

		if (keys.containsKey("name"))
			setName(keys.get("name"));

		if (keys.containsKey("seamark:name"))
			setName(keys.get("seamark:name"));

		if (keys.containsKey("seamark:buoy_lateral:name"))
			setName(keys.get("seamark:buoy_lateral:name"));
		else if (keys.containsKey("seamark:beacon_lateral:name"))
			setName(keys.get("seamark:beacon_lateral:name"));
		else if (keys.containsKey("seamark:light_float:name"))
			setName(keys.get("seamark:light_float:name"));

		String cat = "";
		String col = "";
		String top = "";

		if (getStyleIndex() != LAT_PERCH) {
			if (keys.containsKey("seamark:topmark:shape")) {
				top = keys.get("seamark:topmark:shape");
				setTopMark(true);
			}
			if (keys.containsKey("seamark:topmark:colour")) {
				setTopMark(true);
			}
		}

		if (keys.containsKey("seamark:buoy_lateral:colour"))
			col = keys.get("seamark:buoy_lateral:colour");
		else if (keys.containsKey("seamark:beacon_lateral:colour"))
			col = keys.get("seamark:beacon_lateral:colour");
		else if (keys.containsKey("seamark:light_float:colour"))
			col = keys.get("seamark:light_float:colour");

		if (keys.containsKey("seamark:buoy_lateral:category"))
			cat = keys.get("seamark:buoy_lateral:category");
		else if (keys.containsKey("seamark:beacon_lateral:category"))
			cat = keys.get("seamark:beacon_lateral:category");

		if (cat.equals("")) {
			if (col.equals("red")) {
				setColour(RED);
				if (top.equals("cylinder")) {
					setBuoyIndex(PORT_HAND);
					setRegion(IALA_A);
				} else if (top.equals("cone, point up")) {
					setBuoyIndex(STARBOARD_HAND);
					setRegion(IALA_B);
				} else {
					if (getRegion() == IALA_A)
						setBuoyIndex(PORT_HAND);
					else
						setBuoyIndex(STARBOARD_HAND);
				}
			} else if (col.equals("green")) {
				setColour(GREEN);
				if (top.equals("cone, point up")) {
					setBuoyIndex(STARBOARD_HAND);
					setRegion(IALA_A);
				} else if (top.equals("cylinder")) {
					setBuoyIndex(PORT_HAND);
					setRegion(IALA_B);
				} else {
					if (getRegion() == IALA_A)
						setBuoyIndex(STARBOARD_HAND);
					else
						setBuoyIndex(PORT_HAND);
				}
			} else if (col.equals("red;green;red")) {
				setColour(RED_GREEN_RED);
				if (top.equals("cylinder")) {
					setBuoyIndex(PREF_PORT_HAND);
					setRegion(IALA_A);
				} else if (top.equals("cone, point up")) {
					setBuoyIndex(PREF_STARBOARD_HAND);
					setRegion(IALA_B);
				} else {
					if (getRegion() == IALA_A)
						setBuoyIndex(PREF_PORT_HAND);
					else
						setBuoyIndex(PREF_STARBOARD_HAND);
				}
			} else if (col.equals("green;red;green")) {
				setColour(GREEN_RED_GREEN);
				if (top.equals("cone, point up")) {
					setBuoyIndex(PREF_STARBOARD_HAND);
					setRegion(IALA_A);
				} else if (top.equals("cylinder")) {
					setBuoyIndex(PREF_PORT_HAND);
					setRegion(IALA_B);
				} else {
					if (getRegion() == IALA_A)
						setBuoyIndex(PREF_STARBOARD_HAND);
					else
						setBuoyIndex(PREF_PORT_HAND);
				}
			}
		} else if (cat.equals("port")) {

			setBuoyIndex(PORT_HAND);

			if (col.equals("red")) {
				setRegion(IALA_A);
				setColour(RED);
			} else if (col.equals("green")) {
				setRegion(IALA_B);
				setColour(GREEN);
			} else {
				if (getRegion() == IALA_A)
					setColour(RED);
				else
					setColour(GREEN);
			}
		} else if (cat.equals("starboard")) {

			setBuoyIndex(STARBOARD_HAND);

			if (col.equals("green")) {
				setRegion(IALA_A);
				setColour(GREEN);
			} else if (col.equals("red")) {
				setRegion(IALA_B);
				setColour(RED);
			} else {
				if (getRegion() == IALA_A)
					setColour(GREEN);
				else
					setColour(RED);
			}
		} else if (cat.equals("preferred_channel_port")) {

			setBuoyIndex(PREF_PORT_HAND);

			if (col.equals("red;green;red")) {
				setRegion(IALA_A);
				setColour(RED_GREEN_RED);
			} else if (col.equals("green;red;green")) {
				setRegion(IALA_B);
				setColour(GREEN_RED_GREEN);
			} else {
				if (getRegion() == IALA_A)
					setColour(RED_GREEN_RED);
				else
					setColour(GREEN_RED_GREEN);
			}

		} else if (cat.equals("preferred_channel_starboard")) {

			setBuoyIndex(PREF_STARBOARD_HAND);

			if (col.equals("green;red;green")) {
				setRegion(IALA_A);
				setColour(GREEN_RED_GREEN);
			} else if (col.equals("red;green;red")) {
				setRegion(IALA_B);
				setColour(RED_GREEN_RED);
			} else {
				if (getRegion() == IALA_A)
					setColour(GREEN_RED_GREEN);
				else
					setColour(RED_GREEN_RED);
			}
		}

		if (keys.containsKey("seamark:buoy_lateral:shape")) {
			str = keys.get("seamark:buoy_lateral:shape");

			switch (getBuoyIndex()) {
			case PORT_HAND:
				if (str.equals("can"))
					setStyleIndex(LAT_CAN);
				else if (str.equals("pillar"))
					setStyleIndex(LAT_PILLAR);
				else if (str.equals("spar"))
					setStyleIndex(LAT_SPAR);
				break;

			case PREF_PORT_HAND:
				if (str.equals("can"))
					setStyleIndex(LAT_CAN);
				else if (str.equals("pillar"))
					setStyleIndex(LAT_PILLAR);
				else if (str.equals("spar"))
					setStyleIndex(LAT_SPAR);
				break;

			case STARBOARD_HAND:
				if (str.equals("conical"))
					setStyleIndex(LAT_CONE);
				else if (str.equals("pillar"))
					setStyleIndex(LAT_PILLAR);
				else if (str.equals("spar"))
					setStyleIndex(LAT_SPAR);
				break;

			case PREF_STARBOARD_HAND:
				if (str.equals("conical"))
					setStyleIndex(LAT_CONE);
				else if (str.equals("pillar"))
					setStyleIndex(LAT_PILLAR);
				else if (str.equals("spar"))
					setStyleIndex(LAT_SPAR);
				break;
			}
		} else if (keys.containsKey("seamark:beacon_lateral:shape")) {
			str = keys.get("seamark:beacon_lateral:shape");
			if (str.equals("tower"))
				setStyleIndex(LAT_TOWER);
			else if (str.equals("perch"))
				setStyleIndex(LAT_PERCH);
			else
				setStyleIndex(LAT_BEACON);
		} else if (keys.containsKey("seamark:type")
				&& (keys.get("seamark:type").equals("beacon_lateral"))) {
			setStyleIndex(LAT_BEACON);
		} else if (keys.containsKey("seamark:type")
				&& (keys.get("seamark:type").equals("light_float"))) {
			setStyleIndex(LAT_FLOAT);
		}

		refreshStyles();

		if (keys.containsKey("seamark:light:colour")) {
			setLightColour(keys.get("seamark:light:colour"));
			setFired(true);
		}

		if (keys.containsKey("seamark:light:character")) {
			setLightGroup(keys);
			setLightChar(keys.get("seamark:light:character"));
			setLightPeriod(keys);
			setFired(true);
		}
		setLightColour();
	}

	public void refreshStyles() {
		int type = getBuoyIndex();
		int style = getStyleIndex();

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("*Select Shape*");

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
		dlg.cbM01StyleOfMark.setVisible(true);
		dlg.lM01StyleOfMark.setVisible(true);
	}

	public void paintSign() {
		if (dlg.paintlock)
			return;
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		if ((getBuoyIndex() > 0) && (getStyleIndex() > 0)) {
			dlg.tfM01Name.setEnabled(true);
			dlg.tfM01Name.setText(getName());
			dlg.cM01TopMark.setEnabled(true);
			dlg.cM01TopMark.setVisible(true);
			dlg.cM01Radar.setEnabled(true);
			dlg.cM01Radar.setVisible(true);
			dlg.cM01Racon.setEnabled(true);
			dlg.cM01Racon.setVisible(true);
			dlg.cM01Fog.setEnabled(true);
			dlg.cM01Fog.setVisible(true);

			dlg.cM01Fired.setVisible(true);
			dlg.cM01Fired.setEnabled(true);

			String image = "/images/Lateral";

			int cat = getBuoyIndex();
			boolean region = getRegion();
			int style = getStyleIndex();

			if (style == LAT_PERCH) {
				dlg.cM01Fired.setSelected(false);
				dlg.cM01TopMark.setSelected(false);
				dlg.cM01Fired.setEnabled(false);
				dlg.cM01TopMark.setEnabled(false);
			} else {
				dlg.cM01Fired.setEnabled(true);
				dlg.cM01TopMark.setEnabled(true);
			}

			switch (getBuoyIndex()) {
			case PORT_HAND:
				if (region == IALA_A)
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

			case STARBOARD_HAND:
				if (region == IALA_A)
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

			case PREF_PORT_HAND:
				if (region == IALA_A)
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

			case PREF_STARBOARD_HAND:
				if (region == IALA_A)
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

			if (!image.equals("/images/Lateral")) {

				if (hasTopMark()) {
					if (cat == PORT_HAND || cat == PREF_PORT_HAND)
						image += "_Can";
					else
						image += "_Cone";
				}
				image += ".png";
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));

				if (hasRadar()) {
					dlg.lM03Icon.setIcon(new ImageIcon(getClass().getResource(
							"/images/Radar_Reflector.png")));
				}

			} else
				dlg.lM01Icon.setIcon(null);
		}
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
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "perch"));
				break;
			default:
			}
			switch (getStyleIndex()) {
			case LAT_CAN:
			case LAT_PILLAR:
			case LAT_SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:category", "port"));
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "green"));
					colour = "green";
				}
				break;
			case LAT_PERCH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "port"));
				break;
			case LAT_BEACON:
			case LAT_TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "port"));
				if (getRegion() == IALA_A) {
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
				if (getRegion() == IALA_A) {
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
				if (getRegion() == IALA_A) {
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
				if (getRegion() == IALA_A) {
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
				if (getRegion() == IALA_A) {
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
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "perch"));
				break;
			default:
			}
			switch (getStyleIndex()) {
			case LAT_CAN:
			case LAT_PILLAR:
			case LAT_SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:category", "starboard"));
				if (getRegion() == IALA_A) {
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
				if (getRegion() == IALA_A) {
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
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "red"));
					colour = "red";
				}
				break;
			case LAT_PERCH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "starboard"));
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
				if (getRegion() == IALA_A) {
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
				if (getRegion() == IALA_A) {
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
				if (getRegion() == IALA_A) {
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

	public void setLightColour() {
		if (getRegion() == IALA_A) {
			if (getBuoyIndex() == PORT_HAND || getBuoyIndex() == PREF_PORT_HAND)
				super.setLightColour("R");
			else
				super.setLightColour("G");
		} else {
			if (getBuoyIndex() == PORT_HAND || getBuoyIndex() == PREF_PORT_HAND)
				super.setLightColour("G");
			else
				super.setLightColour("R");
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
				if (str.equals("red")) {
					setFired(true);
					super.setLightColour("R");
				} else {
					super.setLightColour("");
				}
			} else {
				if (str.equals("green")) {
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
				if (str.equals("green")) {
					setFired(true);
					super.setLightColour("G");
				} else {
					super.setLightColour("");
				}
			} else {
				if (str.equals("red")) {
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

}
