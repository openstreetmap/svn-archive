package oseam.seamarks;

import java.util.Map;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.Col;

public class MarkLat extends SeaMark {
	public MarkLat(OSeaMAction dia) {
		super(dia);
	}

	public void parseMark() {

		String str;
		Map<String, String> keys;
		keys = dlg.node.getKeys();

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

		if (getShape() != Shp.PERCH) {
			if (keys.containsKey("seamark:topmark:shape")) {
				top = keys.get("seamark:topmark:shape");
				// setTopMark(true);
			}
			if (keys.containsKey("seamark:topmark:colour")) {
				if (col.isEmpty())
					col = keys.get("seamark:topmark:colour");
				// setTopMark(true);
			}
		}

		if (col.isEmpty()) {
			if (keys.containsKey("seamark:light:colour"))
				col = keys.get("seamark:light:colour");
		}

		/*
		 * if (cat.isEmpty()) { if (col.equals("red")) { setColour(RED); if
		 * (top.equals("cylinder")) { setBuoyIndex(PORT_HAND); setRegion(IALA_A); }
		 * else if (top.equals("cone, point up")) { setBuoyIndex(STARBOARD_HAND);
		 * setRegion(IALA_B); } else { if (getRegion() == IALA_A)
		 * setBuoyIndex(PORT_HAND); else setBuoyIndex(STARBOARD_HAND); } } else if
		 * (col.equals("green")) { setColour(GREEN); if
		 * (top.equals("cone, point up")) { setBuoyIndex(STARBOARD_HAND);
		 * setRegion(IALA_A); } else if (top.equals("cylinder")) {
		 * setBuoyIndex(PORT_HAND); setRegion(IALA_B); } else { if (getRegion() ==
		 * IALA_A) setBuoyIndex(STARBOARD_HAND); else setBuoyIndex(PORT_HAND); } }
		 * else if (col.equals("red;green;red")) { setColour(RED_GREEN_RED); if
		 * (top.equals("cylinder")) { setBuoyIndex(PREF_PORT_HAND);
		 * setRegion(IALA_A); } else if (top.equals("cone, point up")) {
		 * setBuoyIndex(PREF_STARBOARD_HAND); setRegion(IALA_B); } else { if
		 * (getRegion() == IALA_A) setBuoyIndex(PREF_PORT_HAND); else
		 * setBuoyIndex(PREF_STARBOARD_HAND); } } else if
		 * (col.equals("green;red;green")) { setColour(GREEN_RED_GREEN); if
		 * (top.equals("cone, point up")) { setBuoyIndex(PREF_STARBOARD_HAND);
		 * setRegion(IALA_A); } else if (top.equals("cylinder")) {
		 * setBuoyIndex(PREF_PORT_HAND); setRegion(IALA_B); } else { if (getRegion()
		 * == IALA_A) setBuoyIndex(PREF_STARBOARD_HAND); else
		 * setBuoyIndex(PREF_PORT_HAND); } } } else if (cat.equals("port")) {
		 * 
		 * setBuoyIndex(PORT_HAND);
		 * 
		 * if (col.equals("red")) { setRegion(IALA_A); setColour(RED); } else if
		 * (col.equals("green")) { setRegion(IALA_B); setColour(GREEN); } else { if
		 * (getRegion() == IALA_A) setColour(RED); else setColour(GREEN); } } else
		 * if (cat.equals("starboard")) {
		 * 
		 * setBuoyIndex(STARBOARD_HAND);
		 * 
		 * if (col.equals("green")) { setRegion(IALA_A); setColour(GREEN); } else if
		 * (col.equals("red")) { setRegion(IALA_B); setColour(RED); } else { if
		 * (getRegion() == IALA_A) setColour(GREEN); else setColour(RED); } } else
		 * if (cat.equals("preferred_channel_port")) {
		 * 
		 * setBuoyIndex(PREF_PORT_HAND);
		 * 
		 * if (col.equals("red;green;red")) { setRegion(IALA_A);
		 * setColour(RED_GREEN_RED); } else if (col.equals("green;red;green")) {
		 * setRegion(IALA_B); setColour(GREEN_RED_GREEN); } else { if (getRegion()
		 * == IALA_A) setColour(RED_GREEN_RED); else setColour(GREEN_RED_GREEN); }
		 * 
		 * } else if (cat.equals("preferred_channel_starboard")) {
		 * 
		 * setBuoyIndex(PREF_STARBOARD_HAND);
		 * 
		 * if (col.equals("green;red;green")) { setRegion(IALA_A);
		 * setColour(GREEN_RED_GREEN); } else if (col.equals("red;green;red")) {
		 * setRegion(IALA_B); setColour(RED_GREEN_RED); } else { if (getRegion() ==
		 * IALA_A) setColour(GREEN_RED_GREEN); else setColour(RED_GREEN_RED); } }
		 * 
		 * if (keys.containsKey("seamark:buoy_lateral:shape")) { str =
		 * keys.get("seamark:buoy_lateral:shape");
		 * 
		 * switch (getBuoyIndex()) { case PORT_HAND: if (str.equals("can"))
		 * setStyleIndex(CAN); else if (str.equals("pillar")) setStyleIndex(PILLAR);
		 * else if (str.equals("spar")) setStyleIndex(SPAR); break;
		 * 
		 * case PREF_PORT_HAND: if (str.equals("can")) setStyleIndex(CAN); else if
		 * (str.equals("pillar")) setStyleIndex(PILLAR); else if
		 * (str.equals("spar")) setStyleIndex(SPAR); break;
		 * 
		 * case STARBOARD_HAND: if (str.equals("conical")) setStyleIndex(CONE); else
		 * if (str.equals("pillar")) setStyleIndex(PILLAR); else if
		 * (str.equals("spar")) setStyleIndex(SPAR); break;
		 * 
		 * case PREF_STARBOARD_HAND: if (str.equals("conical")) setStyleIndex(CONE);
		 * else if (str.equals("pillar")) setStyleIndex(PILLAR); else if
		 * (str.equals("spar")) setStyleIndex(SPAR); break; } } else if
		 * (keys.containsKey("seamark:beacon_lateral:shape")) { str =
		 * keys.get("seamark:beacon_lateral:shape"); if (str.equals("tower"))
		 * setStyleIndex(TOWER); else if (str.equals("perch")) setStyleIndex(PERCH);
		 * else setStyleIndex(BEACON); } else if (keys.containsKey("seamark:type")
		 * && (keys.get("seamark:type").equals("beacon_lateral"))) {
		 * setStyleIndex(BEACON); } else if (keys.containsKey("seamark:type") &&
		 * (keys.get("seamark:type").equals("light_float"))) { setStyleIndex(FLOAT);
		 * }
		 * 
		 * parseLights(keys); parseFogRadar(keys); setLightColour();
		 */}

	public void setLightColour() {
		if (getRegion() == IALA_A) {
			if (getCategory() == Cat.LAT_PORT || getCategory() == Cat.LAT_PREF_PORT)
				super.setLightColour(Col.RED);
			else
				super.setLightColour(Col.GREEN);
		} else {
			if (getCategory() == Cat.LAT_PORT || getCategory() == Cat.LAT_PREF_PORT)
				super.setLightColour(Col.GREEN);
			else
				super.setLightColour(Col.RED);
		}
	}

	public void paintSign() {
		boolean region = getRegion();
		Shp style = getShape();

		String image = "/images/Lateral";

		switch (getCategory()) {
		case LAT_PORT:
			if (region == IALA_A)
				switch (style) {
				case CAN:
					image += "_Can_Red";
					break;
				case PILLAR:
					image += "_Pillar_Red";
					break;
				case SPAR:
					image += "_Spar_Red";
					break;
				case BEACON:
					image += "_Beacon_Red";
					break;
				case TOWER:
					image += "_Tower_Red";
					break;
				case FLOAT:
					image += "_Float_Red";
					break;
				case PERCH:
					image += "_Perch_Port";
					break;
				default:
				}
			else
				switch (style) {
				case CAN:
					image += "_Can_Green";
					break;
				case PILLAR:
					image += "_Pillar_Green";
					break;
				case SPAR:
					image += "_Spar_Green";
					break;
				case BEACON:
					image += "_Beacon_Green";
					break;
				case TOWER:
					image += "_Tower_Green";
					break;
				case FLOAT:
					image += "_Float_Green";
					break;
				case PERCH:
					image += "_Perch_Port";
					break;
				default:
				}
			break;

		case LAT_STBD:
			if (region == IALA_A)
				switch (style) {
				case CONE:
					image += "_Cone_Green";
					break;
				case PILLAR:
					image += "_Pillar_Green";
					break;
				case SPAR:
					image += "_Spar_Green";
					break;
				case BEACON:
					image += "_Beacon_Green";
					break;
				case TOWER:
					image += "_Tower_Green";
					break;
				case FLOAT:
					image += "_Float_Green";
					break;
				case PERCH:
					image += "_Perch_Starboard";
					break;
				default:
				}
			else
				switch (style) {
				case CONE:
					image += "_Cone_Red";
					break;
				case PILLAR:
					image += "_Pillar_Red";
					break;
				case SPAR:
					image += "_Spar_Red";
					break;
				case BEACON:
					image += "_Beacon_Red";
					break;
				case TOWER:
					image += "_Tower_Red";
					break;
				case FLOAT:
					image += "_Float_Red";
					break;
				case PERCH:
					image += "_Perch_Starboard";
					break;
				default:
				}
			break;

		case LAT_PREF_PORT:
			if (region == IALA_A)
				switch (style) {
				case CAN:
					image += "_Can_Red_Green_Red";
					break;
				case PILLAR:
					image += "_Pillar_Red_Green_Red";
					break;
				case SPAR:
					image += "_Spar_Red_Green_Red";
					break;
				case BEACON:
					image += "_Beacon_Red_Green_Red";
					break;
				case TOWER:
					image += "_Tower_Red_Green_Red";
					break;
				case FLOAT:
					image += "_Float_Red_Green_Red";
					break;
				default:
				}
			else
				switch (style) {
				case CAN:
					image += "_Can_Green_Red_Green";
					break;
				case PILLAR:
					image += "_Pillar_Green_Red_Green";
					break;
				case SPAR:
					image += "_Spar_Green_Red_Green";
					break;
				case BEACON:
					image += "_Beacon_Green_Red_Green";
					break;
				case TOWER:
					image += "_Tower_Green_Red_Green";
					break;
				case FLOAT:
					image += "_Float_Green_Red_Green";
					break;
				default:
				}
			break;

		case LAT_PREF_STBD:
			if (region == IALA_A)
				switch (style) {
				case CONE:
					image += "_Cone_Green_Red_Green";
					break;
				case PILLAR:
					image += "_Pillar_Green_Red_Green";
					break;
				case SPAR:
					image += "_Spar_Green_Red_Green";
					break;
				case BEACON:
					image += "_Beacon_Green_Red_Green";
					break;
				case TOWER:
					image += "_Tower_Green_Red_Green";
					break;
				case FLOAT:
					image += "_Float_Green_Red_Green";
					break;
				default:
				}
			else
				switch (style) {
				case CONE:
					image += "_Cone_Red_Green_Red";
					break;
				case PILLAR:
					image += "_Pillar_Red_Green_Red";
					break;
				case SPAR:
					image += "_Spar_Red_Green_Red";
					break;
				case BEACON:
					image += "_Beacon_Red_Green_Red";
					break;
				case TOWER:
					image += "_Tower_Red_Green_Red";
					break;
				case FLOAT:
					image += "_Float_Red_Green_Red";
					break;
				default:
				}
			break;

		default:
		}

		if (!image.equals("/images/Lateral")) {

			image += ".png";
			dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource(image)));

			if (hasTopmark()) {
				image = "";
				switch (getCategory()) {
				case LAT_PORT:
				case LAT_PREF_PORT:
					if (region == IALA_A)
						switch (style) {
						case CAN:
							image = "/images/Top_Can_Red_Buoy_Small.png";
							break;
						case PILLAR:
						case SPAR:
							image = "/images/Top_Can_Red_Buoy.png";
							break;
						case BEACON:
						case TOWER:
							image = "/images/Top_Can_Red_Beacon.png";
							break;
						case FLOAT:
							image = "/images/Top_Can_Red_Float.png";
							break;
						}
					else
						switch (style) {
						case CAN:
							image = "/images/Top_Can_Green_Buoy_Small.png";
							break;
						case PILLAR:
						case SPAR:
							image = "/images/Top_Can_Green_Buoy.png";
							break;
						case BEACON:
						case TOWER:
							image = "/images/Top_Can_Green_Beacon.png";
							break;
						case FLOAT:
							image = "/images/Top_Can_Green_Float.png";
							break;
						}
					break;

				case LAT_STBD:
				case LAT_PREF_STBD:
					if (region == IALA_A)
						switch (style) {
						case CONE:
							image = "/images/Top_Cone_Green_Buoy_Small.png";
							break;
						case PILLAR:
						case SPAR:
							image = "/images/Top_Cone_Green_Buoy.png";
							break;
						case BEACON:
						case TOWER:
							image = "/images/Top_Cone_Green_Beacon.png";
							break;
						case FLOAT:
							image = "/images/Top_Cone_Green_Float.png";
							break;
						}
					else
						switch (style) {
						case CONE:
							image = "/images/Top_Cone_Red_Buoy_Small.png";
							break;
						case PILLAR:
						case SPAR:
							image = "/images/Top_Cone_Red_Buoy.png";
							break;
						case BEACON:
						case TOWER:
							image = "/images/Top_Cone_Red_Beacon.png";
							break;
						case FLOAT:
							image = "/images/Top_Cone_Red_Float.png";
							break;
						}
					break;
				}
				if (!image.isEmpty())
					dlg.panelMain.topIcon.setIcon(new ImageIcon(getClass().getResource(image)));
			}
		} else
			dlg.panelMain.shapeIcon.setIcon(null);
		super.paintSign();
	}

	public void saveSign() {

		if (dlg.node == null)
			return;
		else
			super.saveSign();

		Cat cat = getCategory();
		String shape = "";
		String colour = "";

		switch (cat) {

		case LAT_PORT:
			switch (getShape()) {
			case CAN:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "can"));
				break;
			case PILLAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "pillar"));
				break;
			case SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "spar"));
				break;
			case BEACON:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_lateral"));
				break;
			case TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:shape", "tower"));
				break;
			case FLOAT:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "light_float"));
				break;
			case PERCH:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:shape", "perch"));
				break;
			default:
			}
			switch (getShape()) {
			case CAN:
			case PILLAR:
			case SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:category", "port"));
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:colour", "red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:colour", "green"));
					colour = "green";
				}
				break;
			case PERCH:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:category", "port"));
				break;
			case BEACON:
			case TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:category", "port"));
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:colour", "red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:colour", "green"));
					colour = "green";
				}
				break;
			case FLOAT:
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour", "red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour", "green"));
					colour = "green";
				}
				break;
			}
			shape = "cylinder";
			break;

		case LAT_PREF_PORT:
			switch (getShape()) {
			case CAN:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "can"));
				break;
			case PILLAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "pillar"));
				break;
			case SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "spar"));
				break;
			case BEACON:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_lateral"));
				break;
			case TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:shape", "tower"));
				break;
			case FLOAT:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "light_float"));
				break;
			default:
			}
			switch (getShape()) {
			case CAN:
			case PILLAR:
			case SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:category", "preferred_channel_port"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:colour_pattern", "horizontal stripes"));
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:colour", "red;green;red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:colour", "green;red;green"));
					colour = "green";
				}
				break;
			case BEACON:
			case TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:category", "preferred_channel_port"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:colour_pattern", "horizontal stripes"));
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:colour", "red;green;red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:colour", "green;red;green"));
					colour = "green";
				}
				break;
			case FLOAT:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour_pattern", "horizontal stripes"));
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour", "red;green;red"));
					colour = "red";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour", "green;red;green"));
					colour = "green";
				}
				break;
			}
			shape = "cylinder";
			break;

		case LAT_STBD:
			switch (getShape()) {
			case CONE:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "conical"));
				break;
			case PILLAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "pillar"));
				break;
			case SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "spar"));
				break;
			case BEACON:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:shape", "stake"));
				break;
			case TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:shape", "tower"));
				break;
			case FLOAT:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "light_float"));
				break;
			case PERCH:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:shape", "perch"));
				break;
			default:
			}
			switch (getShape()) {
			case CAN:
			case PILLAR:
			case SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:category", "starboard"));
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:colour", "green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:colour", "red"));
					colour = "red";
				}
				break;
			case BEACON:
			case TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:category", "starboard"));
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:colour", "green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:colour", "red"));
					colour = "red";
				}
				break;
			case FLOAT:
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour", "green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour", "red"));
					colour = "red";
				}
				break;
			case PERCH:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:category", "starboard"));
				break;
			}
			shape = "cone, point up";
			break;

		case LAT_PREF_STBD:
			switch (getShape()) {
			case CONE:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "conical"));
				break;
			case PILLAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "pillar"));
				break;
			case SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:shape", "spar"));
				break;
			case BEACON:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:shape", "stake"));
				break;
			case TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_lateral"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:shape", "tower"));
				break;
			case FLOAT:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "light_float"));
				break;
			default:
			}
			switch (getShape()) {
			case CAN:
			case PILLAR:
			case SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:category", "preferred_channel_starboard"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:colour_pattern", "horizontal stripes"));
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:colour", "green;red;green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_lateral:colour", "red;green;red"));
					colour = "red";
				}
				break;
			case BEACON:
			case TOWER:
				Main.main.undoRedo
						.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:category", "preferred_channel_starboard"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:colour_pattern", "horizontal stripes"));
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:colour", "green;red;green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_lateral:colour", "red;green;red"));
					colour = "red";
				}
				break;
			case FLOAT:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour_pattern", "horizontal stripes"));
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour", "green;red;green"));
					colour = "green";
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour", "red;green;red"));
					colour = "red";
				}
				break;
			}
			shape = "cone, point up";
			break;

		default:
		}
		Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:topmark:shape", shape));
		Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:topmark:colour", colour));

		Main.pref.put("tomsplugin.IALA", getRegion() ? "B" : "A");
	}
}
