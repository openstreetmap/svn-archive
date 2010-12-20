package oseam.seamarks;

import java.util.Map;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class MarkSpec extends SeaMark {
	public MarkSpec(OSeaMAction dia, Node node) {
		super(dia, node);
	}
	
	public void parseMark() {

		String str;
		Map<String, String> keys;
		keys = getNode().getKeys();

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
		dlg.cM01TopMark.setEnabled(true);

		setBuoyIndex(SPECIAL_PURPOSE);
		setColour(SeaMark.YELLOW);
		setLightColour("W");
		setRegion(Main.pref.get("tomsplugin.IALA").equals("B"));

		if (keys.containsKey("seamark:buoy_special_purpose:shape")) {
			str = keys.get("seamark:buoy_special_purpose:shape");

			if (str.equals("pillar"))
				setStyleIndex(SPEC_PILLAR);
			else if (str.equals("can"))
				setStyleIndex(SPEC_CAN);
			else if (str.equals("conical"))
				setStyleIndex(SPEC_CONE);
			else if (str.equals("spar"))
				setStyleIndex(SPEC_SPAR);
			else if (str.equals("sphere"))
				setStyleIndex(SPEC_SPHERE);
			else if (str.equals("barrel"))
				setStyleIndex(SPEC_BARREL);
		}

		if (keys.containsKey("seamark:beacon_special_purpose:shape")) {
			str = keys.get("seamark:beacon_special_purpose:shape");
			if (str.equals("tower"))
				setStyleIndex(SPEC_TOWER);
			else
				setStyleIndex(SPEC_BEACON);
		}

		if (keys.containsKey("seamark:light_float:colour")) {
			setStyleIndex(SPEC_FLOAT);
		}

		if ((keys.containsKey("seamark:type") && keys.get("seamark:type").equals(
				"beacon_special_purpose"))
				|| keys.containsKey("seamark:beacon_special_purpose:colour")
				|| keys.containsKey("seamark:beacon_special_purpose:shape")) {
			if (keys.containsKey("seamark:beacon_special_purpose:shape")
					&& keys.get("seamark:beacon_special_purpose:shape").equals("tower"))
				setStyleIndex(SPEC_TOWER);
			else
				setStyleIndex(SPEC_BEACON);
		} else if (keys.containsKey("seamark:light_float:colour")
				&& keys.get("seamark:light_float:colour").equals("yellow"))
			setStyleIndex(SPEC_FLOAT);

		if (getStyleIndex() >= dlg.cbM01StyleOfMark.getItemCount())
			setStyleIndex(0);

		keys = node.getKeys();
		if (keys.containsKey("seamark:topmark:shape")) {
			str = keys.get("seamark:topmark:shape");
			setTopMark(true);
			if (str.equals("x-shape")) {
				if (keys.containsKey("seamark:topmark:colour")) {
					if (keys.get("seamark:topmark:colour").equals("red"))
						setTopMarkIndex(TOP_RED_X);
					else
						setTopMarkIndex(TOP_YELLOW_X);
				}
			} else if (str.equals("cone, point up")) {
					setTopMarkIndex(TOP_YELLOW_CONE);
			} else if (str.equals("cylinder")) {
				setTopMarkIndex(TOP_YELLOW_CAN);
			}
		}

		refreshLights();
		parseLights(keys);
		parseFogRadar(keys);

		dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
		dlg.tfM01Name.setText(getName());
		dlg.cM01TopMark.setSelected(hasTopMark());
*/	}
/*
	public void refreshLights() {
		super.refreshLights();

		switch (getStyleIndex()) {
		case SPEC_BARREL:
			dlg.cM01Fired.setSelected(false);
			dlg.cM01Fired.setEnabled(false);
			dlg.cM01TopMark.setEnabled(true);
			break;
		default:
			dlg.cM01Fired.setEnabled(true);
			dlg.cM01TopMark.setEnabled(true);
		}
	}

	public boolean isValid() {
		return (getBuoyIndex() > 0) && (getStyleIndex() > 0);
	}

	public void setLightColour() {
		super.setLightColour("W");
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
			dlg.cM01Radar.setVisible(true);
			dlg.cM01Racon.setVisible(true);
			dlg.cM01TopMark.setEnabled(true);
			dlg.cM01TopMark.setVisible(true);
			if (hasTopMark()) {
				dlg.cbM01TopMark.setEnabled(true);
				dlg.cbM01TopMark.setVisible(true);
			} else {
				dlg.cbM01TopMark.setVisible(false);
			}
			dlg.cM01Fog.setVisible(true);
			dlg.cM01Fired.setVisible(true);
			dlg.cM01Fired.setEnabled(true);
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
				switch (getStyleIndex()) {
				case SPEC_FLOAT:
				case SPEC_BEACON:
				case SPEC_TOWER:
					dlg.lM01Height.setVisible(true);
					dlg.tfM01Height.setVisible(true);
					dlg.lM01Range.setVisible(true);
					dlg.tfM01Range.setVisible(true);
					break;
				default:
				}
			}

			String image = "/images/Special_Purpose";

			switch (getStyleIndex()) {
			case SPEC_PILLAR:
				image += "_Pillar";
				break;
			case SPEC_CAN:
				image += "_Can";
				break;
			case SPEC_CONE:
				image += "_Cone";
				break;
			case SPEC_SPAR:
				image += "_Spar";
				break;
			case SPEC_SPHERE:
				image += "_Sphere";
				break;
			case SPEC_BARREL:
				image += "_Barrel";
				break;
			case SPEC_FLOAT:
				image += "_Float";
				break;
			case SPEC_BEACON:
				image += "_Beacon";
				break;
			case SPEC_TOWER:
				image += "_Tower";
				break;
			default:
			}

			if (!image.equals("/images/Special_Purpose")) {
				image += ".png";
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));
				if (hasTopMark()) {
					image = "";
					switch (getStyleIndex()) {
					case SPEC_PILLAR:
					case SPEC_SPAR:
						switch (getTopMarkIndex()) {
						case TOP_YELLOW_X:
							image = "/images/Top_X_Yellow_Buoy.png";
							break;
						case TOP_RED_X:
							image = "/images/Top_X_Red_Buoy.png";
							break;
						case TOP_YELLOW_CAN:
							image = "/images/Top_Can_Yellow_Buoy.png";
							break;
						case TOP_YELLOW_CONE:
							image = "/images/Top_Cone_Yellow_Buoy.png";
							break;
						}
						break;
					case SPEC_CAN:
					case SPEC_CONE:
					case SPEC_SPHERE:
					case SPEC_BARREL:
						switch (getTopMarkIndex()) {
						case TOP_YELLOW_X:
							image = "/images/Top_X_Yellow_Buoy_Small.png";
							break;
						case TOP_RED_X:
							image = "/images/Top_X_Red_Buoy_Small.png";
							break;
						case TOP_YELLOW_CAN:
							image = "/images/Top_Can_Yellow_Buoy_Small.png";
							break;
						case TOP_YELLOW_CONE:
							image = "/images/Top_Cone_Yellow_Buoy_Small.png";
							break;
						}
						break;
					case SPEC_BEACON:
					case SPEC_TOWER:
						switch (getTopMarkIndex()) {
						case TOP_YELLOW_X:
							image = "/images/Top_X_Yellow_Beacon.png";
							break;
						case TOP_RED_X:
							image = "/images/Top_X_Red_Beacon.png";
							break;
						case TOP_YELLOW_CAN:
							image = "/images/Top_Can_Yellow_Beacon.png";
							break;
						case TOP_YELLOW_CONE:
							image = "/images/Top_Cone_Yellow_Beacon.png";
							break;
						}
						break;
					case SPEC_FLOAT:
						switch (getTopMarkIndex()) {
						case TOP_YELLOW_X:
							image = "/images/Top_X_Yellow_Float.png";
							break;
						case TOP_RED_X:
							image = "/images/Top_X_Red_Float.png";
							break;
						case TOP_YELLOW_CAN:
							image = "/images/Top_Can_Yellow_Float.png";
							break;
						case TOP_YELLOW_CONE:
							image = "/images/Top_Cone_Yellow_Float.png";
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

		switch (getShape()) {
		case PILLAR:
			super.saveSign("buoy_special_purpose");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "pillar"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow"));
			break;
		case SPAR:
			super.saveSign("buoy_special_purpose");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "spar"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow"));
			break;
		case CAN:
			super.saveSign("buoy_special_purpose");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "can"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow"));
			break;
		case CONE:
			super.saveSign("buoy_special_purpose");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "conical"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow"));
			break;
		case SPHERE:
			super.saveSign("buoy_special_purpose");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "sphere"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow"));
			break;
		case BARREL:
			super.saveSign("buoy_special_purpose");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "barrel"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow"));
			break;
		case FLOAT:
			super.saveSign("light_float");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour", "yellow"));
			break;
		case BEACON:
			super.saveSign("beacon_special_purpose");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_special_purpose:colour", "yellow"));
			break;
		case TOWER:
			super.saveSign("beacon_special_purpose");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_special_purpose:shape", "tower"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_special_purpose:colour", "yellow"));
			break;
		default:
		}
/*		switch (getTopMarkIndex()) {
		case TOP_YELLOW_X:
			saveTopMarkData("x-shape", "yellow");
			break;
		case TOP_RED_X:
			saveTopMarkData("x-shape", "red");
			break;
		case TOP_YELLOW_CAN:
			saveTopMarkData("cylinder", "yellow");
			break;
		case TOP_YELLOW_CONE:
			saveTopMarkData("cone, point up", "yellow");
			break;
		}
*/		saveLightData();
		saveRadarFogData();
	}
}
