package oseam.seamarks;

import java.util.Map;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;
import oseam.seamarks.SeaMark.Col;

public class MarkSpec extends SeaMark {
	public MarkSpec(OSeaMAction dia) {
		super(dia);
	}

	public void parseMark() {

		String str;
		Map<String, String> keys;
		keys = dlg.node.getKeys();

		if (!dlg.panelMain.specButton.isSelected())
			dlg.panelMain.specButton.doClick();

		if (keys.containsKey("name"))
			setName(keys.get("name"));

		if (keys.containsKey("seamark:name"))
			setName(keys.get("seamark:name"));

		if (keys.containsKey("seamark:buoy_special_purpose:name"))
			setName(keys.get("seamark:buoy_special_purpose:name"));
		else if (keys.containsKey("seamark:beacon_special_purpose:name"))
			setName(keys.get("seamark:beacon_special_purpose:name"));
		else if (keys.containsKey("seamark:light_float:name"))
			setName(keys.get("seamark:light_float:name"));

		/*
		 * dlg.cM01TopMark.setEnabled(true);
		 * 
		 * setBuoyIndex(SPECIAL_PURPOSE); setColour(SeaMark.YELLOW);
		 * setLightColour("W");
		 * setRegion(Main.pref.get("tomsplugin.IALA").equals("B"));
		 * 
		 * if (keys.containsKey("seamark:buoy_special_purpose:shape")) { str =
		 * keys.get("seamark:buoy_special_purpose:shape");
		 * 
		 * if (str.equals("pillar")) setStyleIndex(PILLAR); else if
		 * (str.equals("can")) setStyleIndex(CAN); else if (str.equals("conical"))
		 * setStyleIndex(CONE); else if (str.equals("spar")) setStyleIndex(SPAR);
		 * else if (str.equals("sphere")) setStyleIndex(SPHERE); else if
		 * (str.equals("barrel")) setStyleIndex(BARREL); }
		 * 
		 * if (keys.containsKey("seamark:beacon_special_purpose:shape")) { str =
		 * keys.get("seamark:beacon_special_purpose:shape"); if
		 * (str.equals("tower")) setStyleIndex(TOWER); else setStyleIndex(BEACON); }
		 * 
		 * if (keys.containsKey("seamark:light_float:colour")) {
		 * setStyleIndex(FLOAT); }
		 * 
		 * if ((keys.containsKey("seamark:type") && keys.get("seamark:type").equals(
		 * "beacon_special_purpose")) ||
		 * keys.containsKey("seamark:beacon_special_purpose:colour") ||
		 * keys.containsKey("seamark:beacon_special_purpose:shape")) { if
		 * (keys.containsKey("seamark:beacon_special_purpose:shape") &&
		 * keys.get("seamark:beacon_special_purpose:shape").equals("tower"))
		 * setStyleIndex(TOWER); else setStyleIndex(BEACON); } else if
		 * (keys.containsKey("seamark:light_float:colour") &&
		 * keys.get("seamark:light_float:colour").equals("yellow"))
		 * setStyleIndex(FLOAT);
		 * 
		 * if (getStyleIndex() >= dlg.cbM01StyleOfMark.getItemCount())
		 * setStyleIndex(0);
		 * 
		 * keys = node.getKeys(); if (keys.containsKey("seamark:topmark:shape")) {
		 * str = keys.get("seamark:topmark:shape"); setTopMark(true); if
		 * (str.equals("x-shape")) { if (keys.containsKey("seamark:topmark:colour"))
		 * { if (keys.get("seamark:topmark:colour").equals("red"))
		 * setTopMarkIndex(TOP_RED_X); else setTopMarkIndex(TOP_YELLOW_X); } } else
		 * if (str.equals("cone, point up")) { setTopMarkIndex(TOP_YELLOW_CONE); }
		 * else if (str.equals("cylinder")) { setTopMarkIndex(TOP_YELLOW_CAN); } }
		 * 
		 * parseLights(keys); parseFogRadar(keys);
		 * 
		 * dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
		 * dlg.tfM01Name.setText(getName());
		 * dlg.cM01TopMark.setSelected(hasTopMark());
		 */}

	public void setLightColour() {
		super.setLightColour(Col.WHITE);
	}

	public void paintSign() {

		String image = "/images/Special_Purpose";

		switch (getShape()) {
		case PILLAR:
			image += "_Pillar";
			break;
		case CAN:
			image += "_Can";
			break;
		case CONE:
			image += "_Cone";
			break;
		case SPAR:
			image += "_Spar";
			break;
		case SPHERE:
			image += "_Sphere";
			break;
		case BARREL:
			image += "_Barrel";
			break;
		case FLOAT:
			image += "_Float";
			break;
		case BEACON:
			image += "_Beacon";
			break;
		case TOWER:
			image += "_Tower";
			break;
		}

		if (!image.equals("/images/Special_Purpose")) {
			image += ".png";
			dlg.panelMain.topIcon.setIcon(new ImageIcon(getClass().getResource(image)));
			/*
			 * if (hasTopMark()) { image = ""; switch (getShape()) { case PILLAR: case
			 * SPAR: switch (getTopMarkIndex()) { case TOP_YELLOW_X: image =
			 * "/images/Top_X_Yellow_Buoy.png"; break; case TOP_RED_X: image =
			 * "/images/Top_X_Red_Buoy.png"; break; case TOP_YELLOW_CAN: image =
			 * "/images/Top_Can_Yellow_Buoy.png"; break; case TOP_YELLOW_CONE: image =
			 * "/images/Top_Cone_Yellow_Buoy.png"; break; } break; case CAN: case
			 * CONE: case SPHERE: case BARREL: switch (getTopMarkIndex()) { case
			 * TOP_YELLOW_X: image = "/images/Top_X_Yellow_Buoy_Small.png"; break;
			 * case TOP_RED_X: image = "/images/Top_X_Red_Buoy_Small.png"; break; case
			 * TOP_YELLOW_CAN: image = "/images/Top_Can_Yellow_Buoy_Small.png"; break;
			 * case TOP_YELLOW_CONE: image = "/images/Top_Cone_Yellow_Buoy_Small.png";
			 * break; } break; case BEACON: case TOWER: switch (getTopMarkIndex()) {
			 * case TOP_YELLOW_X: image = "/images/Top_X_Yellow_Beacon.png"; break;
			 * case TOP_RED_X: image = "/images/Top_X_Red_Beacon.png"; break; case
			 * TOP_YELLOW_CAN: image = "/images/Top_Can_Yellow_Beacon.png"; break;
			 * case TOP_YELLOW_CONE: image = "/images/Top_Cone_Yellow_Beacon.png";
			 * break; } break; case FLOAT: switch (getTopMarkIndex()) { case
			 * TOP_YELLOW_X: image = "/images/Top_X_Yellow_Float.png"; break; case
			 * TOP_RED_X: image = "/images/Top_X_Red_Float.png"; break; case
			 * TOP_YELLOW_CAN: image = "/images/Top_Can_Yellow_Float.png"; break; case
			 * TOP_YELLOW_CONE: image = "/images/Top_Cone_Yellow_Float.png"; break; }
			 * break; } if (!image.isEmpty()) dlg.lM06Icon.setIcon(new
			 * ImageIcon(getClass().getResource(image))); }
			 */
		} else
			dlg.panelMain.shapeIcon.setIcon(null);
		super.paintSign();
	}

	public void saveSign() {
		if (dlg.node == null)
			return;
		else
			super.saveSign();

		switch (getShape()) {
		case PILLAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_special_purpose"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_special_purpose:shape", "pillar"));
			saveColour(Obj.BUOY);
			break;
		case SPAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_special_purpose"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_special_purpose:shape", "spar"));
			saveColour(Obj.BUOY);
			break;
		case CAN:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_special_purpose"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_special_purpose:shape", "can"));
			saveColour(Obj.BUOY);
			break;
		case CONE:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_special_purpose"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_special_purpose:shape", "conical"));
			saveColour(Obj.BUOY);
			break;
		case SPHERE:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_special_purpose"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_special_purpose:shape", "sphere"));
			saveColour(Obj.BUOY);
			break;
		case BARREL:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_special_purpose"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_special_purpose:shape", "barrel"));
			saveColour(Obj.BUOY);
			break;
		case SUPER:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_special_purpose"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_special_purpose:shape", "super-buoy"));
			saveColour(Obj.BUOY);
			break;
		case FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "light_float"));
			saveColour(Obj.FLOAT);
			break;
		case BEACON:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_special_purpose"));
			saveColour(Obj.BEACON);
			break;
		case TOWER:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_special_purpose"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_special_purpose:shape", "tower"));
			saveColour(Obj.BEACON);
			break;
		default:
		}
		if (hasTopmark()) {
			switch (getTopmark()) {
			case X_SHAPE:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:topmark:shape", "x-shape"));
				break;
			}
			saveColour(Obj.TOPMARK);
		}
	}
	
	private void saveColour(Obj obj) {
		String str = "";
		switch (obj) {
		case BUOY:
			str = "seamark:buoy_special_purpose:colour";
			break;
		case BEACON:
			str = "seamark:beacon_special_purpose:colour";
			break;
		case FLOAT:
			str = "seamark:light_float:colour";
			break;
		case TOPMARK:
			str = "seamark:topmark:colour";
			break;
		}
		switch (getColour(obj)) {
		case WHITE:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, str, "white"));
			break;
		case RED:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, str, "red"));
			break;
		case ORANGE:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, str, "orange"));
			break;
		case AMBER:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, str, "amber"));
			break;
		case YELLOW:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, str, "yellow"));
			break;
		case GREEN:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, str, "green"));
			break;
		case BLUE:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, str, "blue"));
			break;
		case VIOLET:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, str, "violet"));
			break;
		case BLACK:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, str, "black"));
			break;
		}
	}
}
