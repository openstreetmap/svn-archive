package oseam.seamarks;

import java.util.Map;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;

import oseam.dialogs.OSeaMAction;

public class MarkLat extends SeaMark {
	public MarkLat(OSeaMAction dia, Node node) {
		super(dia, node);
	}
	
	public void parseMark() {

		String str;
		Map<String, String> keys;
		keys = getNode().getKeys();

		if (!dlg.panelMain.chanButton.isSelected())
			dlg.panelMain.chanButton.doClick();

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

		if (keys.containsKey("seamark:buoy_lateral:category"))
			cat = keys.get("seamark:buoy_lateral:category");
		else if (keys.containsKey("seamark:beacon_lateral:category"))
			cat = keys.get("seamark:beacon_lateral:category");

		if (keys.containsKey("seamark:buoy_lateral:colour"))
			col = keys.get("seamark:buoy_lateral:colour");
		else if (keys.containsKey("seamark:beacon_lateral:colour"))
			col = keys.get("seamark:beacon_lateral:colour");
		else if (keys.containsKey("seamark:light_float:colour"))
			col = keys.get("seamark:light_float:colour");

		if (getShape() != Styl.PERCH) {
			if (keys.containsKey("seamark:topmark:shape")) {
				top = keys.get("seamark:topmark:shape");
//				setTopMark(true);
			}
			if (keys.containsKey("seamark:topmark:colour")) {
				if (col.isEmpty()) col = keys.get("seamark:topmark:colour");
//				setTopMark(true);
			}
		}
		
		if (col.isEmpty()) {
			if (keys.containsKey("seamark:light:colour"))
				col = keys.get("seamark:light:colour");
		}

		/*
		if (cat.isEmpty()) {
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
		refreshLights();
		parseLights(keys);
		parseFogRadar(keys);
		setLightColour();

		dlg.cbM01CatOfMark.setSelectedIndex(getBuoyIndex());
		dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
		dlg.tfM01Name.setText(getName());
		dlg.cM01TopMark.setSelected(hasTopMark());
*/	}
/*
	public void refreshStyles() {
		int type = getBuoyIndex();
		int style = getStyleIndex();

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem(Messages.getString("SmpDialogAction.213"));

		switch (type) {
		case PORT_HAND:
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.02"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.10"));
			break;
		case STARBOARD_HAND:
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.03"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.10"));
			break;
		case PREF_PORT_HAND:
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.02"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07"));
			break;
		case PREF_STARBOARD_HAND:
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.03"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06"));
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07"));
			break;
		}

		if (style >= dlg.cbM01StyleOfMark.getItemCount())
			style = 0;
		setStyleIndex(style);
		dlg.cbM01StyleOfMark.setSelectedIndex(style);
		dlg.cbM01StyleOfMark.setVisible(true);
		dlg.lM01StyleOfMark.setVisible(true);
	}

	public void refreshLights() {
		super.refreshLights();
	}

	public boolean isValid() {
		return (getBuoyIndex() > 0) && (getStyleIndex() > 0);
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
*/
	public void paintSign() {
/*		if (dlg.paintlock)
			return;
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		if (isValid()) {
			dlg.tfM01Name.setEnabled(true);
			dlg.tfM01Name.setText(getName());

			boolean region = getRegion();
			int style = getStyleIndex();

			if (style == LAT_PERCH) {
				dlg.cM01TopMark.setVisible(false);
				dlg.cM01TopMark.setSelected(false);
				dlg.cM01Radar.setVisible(false);
				dlg.cM01Racon.setVisible(false);
				dlg.cM01Fog.setVisible(false);
				dlg.cM01Fired.setVisible(false);
				dlg.cM01Fired.setSelected(false);
			} else {
				dlg.cM01TopMark.setEnabled(true);
				dlg.cM01TopMark.setVisible(true);
				dlg.cM01Radar.setVisible(true);
				dlg.cM01Racon.setVisible(true);
				dlg.cM01Fog.setVisible(true);
				dlg.cM01Fired.setVisible(true);
				dlg.cM01Fired.setEnabled(true);
				dlg.cM01TopMark.setEnabled(true);
			}
			if (!isSectored()) {
				dlg.cbM01Colour.setVisible(false);
				dlg.lM01Colour.setVisible(false);
			}
			dlg.rbM01Fired1.setVisible(false);
			dlg.rbM01FiredN.setVisible(false);
			dlg.lM01Height.setVisible(false);
			dlg.tfM01Height.setVisible(false);
			dlg.lM01Range.setVisible(false);
			dlg.tfM01Range.setVisible(false);

			if (isFired()) {
				switch (style) {
				case LAT_BEACON:
				case LAT_TOWER:
				case LAT_FLOAT:
					dlg.lM01Height.setVisible(true);
					dlg.tfM01Height.setVisible(true);
					dlg.lM01Range.setVisible(true);
					dlg.tfM01Range.setVisible(true);
					break;
				default:
				}
			}

			String image = "/images/Lateral";

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

				image += ".png";
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));

				if (hasTopMark()) {
					image = "";
					switch (getBuoyIndex()) {
					case PORT_HAND:
					case PREF_PORT_HAND:
						if (region == IALA_A)
							switch (style) {
							case LAT_CAN:
								image = "/images/Top_Can_Red_Buoy_Small.png";
								break;
							case LAT_PILLAR:
							case LAT_SPAR:
								image = "/images/Top_Can_Red_Buoy.png";
								break;
							case LAT_BEACON:
							case LAT_TOWER:
								image = "/images/Top_Can_Red_Beacon.png";
								break;
							case LAT_FLOAT:
								image = "/images/Top_Can_Red_Float.png";
								break;
							}
						else
							switch (style) {
							case LAT_CAN:
								image = "/images/Top_Can_Green_Buoy_Small.png";
								break;
							case LAT_PILLAR:
							case LAT_SPAR:
								image = "/images/Top_Can_Green_Buoy.png";
								break;
							case LAT_BEACON:
							case LAT_TOWER:
								image = "/images/Top_Can_Green_Beacon.png";
								break;
							case LAT_FLOAT:
								image = "/images/Top_Can_Green_Float.png";
								break;
							}
						break;

					case STARBOARD_HAND:
					case PREF_STARBOARD_HAND:
						if (region == IALA_A)
							switch (style) {
							case LAT_CONE:
								image = "/images/Top_Cone_Green_Buoy_Small.png";
								break;
							case LAT_PILLAR:
							case LAT_SPAR:
								image = "/images/Top_Cone_Green_Buoy.png";
								break;
							case LAT_BEACON:
							case LAT_TOWER:
								image = "/images/Top_Cone_Green_Beacon.png";
								break;
							case LAT_FLOAT:
								image = "/images/Top_Cone_Green_Float.png";
								break;
							}
						else
							switch (style) {
							case LAT_CONE:
								image = "/images/Top_Cone_Red_Buoy_Small.png";
								break;
							case LAT_PILLAR:
							case LAT_SPAR:
								image = "/images/Top_Cone_Red_Buoy.png";
								break;
							case LAT_BEACON:
							case LAT_TOWER:
								image = "/images/Top_Cone_Red_Beacon.png";
								break;
							case LAT_FLOAT:
								image = "/images/Top_Cone_Red_Float.png";
								break;
							}
						break;
					}
					if (!image.isEmpty())
						dlg.lM06Icon.setIcon(new ImageIcon(getClass().getResource(image)));
				}
			} else
				dlg.lM01Icon.setIcon(null);
		}
*/	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		Cat cat = getCategory();
		String shape = "";
		String colour = "";

		switch (cat) {

		case PORT_HAND:
			switch (getShape()) {
			case CAN:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "can"));
				break;
			case PILLAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar"));
				break;
			case SPAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar"));
				break;
			case BEACON:
				super.saveSign("beacon_lateral");
				break;
			case TOWER:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower"));
				break;
			case FLOAT:
				super.saveSign("light_float");
				break;
			case PERCH:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "perch"));
				break;
			default:
			}
			switch (getShape()) {
			case CAN:
			case PILLAR:
			case SPAR:
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
			case PERCH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "port"));
				break;
			case BEACON:
			case TOWER:
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
			case FLOAT:
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
			switch (getShape()) {
			case CAN:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "can"));
				break;
			case PILLAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar"));
				break;
			case SPAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar"));
				break;
			case BEACON:
				super.saveSign("beacon_lateral");
				break;
			case TOWER:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower"));
				break;
			case FLOAT:
				super.saveSign("light_float");
				break;
			default:
			}
			switch (getShape()) {
			case CAN:
			case PILLAR:
			case SPAR:
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
			case BEACON:
			case TOWER:
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
			case FLOAT:
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
			switch (getShape()) {
			case CONE:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "conical"));
				break;
			case PILLAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar"));
				break;
			case SPAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar"));
				break;
			case BEACON:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "stake"));
				break;
			case TOWER:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower"));
				break;
			case FLOAT:
				super.saveSign("light_float");
				break;
			case PERCH:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "perch"));
				break;
			default:
			}
			switch (getShape()) {
			case CAN:
			case PILLAR:
			case SPAR:
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
			case BEACON:
			case TOWER:
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
			case FLOAT:
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
			case PERCH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "starboard"));
				break;
			}
			shape = "cone, point up";
			break;

		case PREF_STARBOARD_HAND:
			switch (getShape()) {
			case CONE:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "conical"));
				break;
			case PILLAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar"));
				break;
			case SPAR:
				super.saveSign("buoy_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar"));
				break;
			case BEACON:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "stake"));
				break;
			case TOWER:
				super.saveSign("beacon_lateral");
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower"));
				break;
			case FLOAT:
				super.saveSign("light_float");
				break;
			default:
			}
			switch (getShape()) {
			case CAN:
			case PILLAR:
			case SPAR:
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
			case BEACON:
			case TOWER:
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
			case FLOAT:
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
		saveLightData();
		saveRadarFogData();

		Main.pref.put("tomsplugin.IALA", getRegion() ? "B" : "A");
	}
}
