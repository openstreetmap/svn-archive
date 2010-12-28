package oseam.seamarks;

import java.util.Map;

import javax.swing.ImageIcon;

import oseam.dialogs.OSeaMAction;

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

		dlg.panelMain.panelSpec.panelCol.yellowButton.doClick();
		dlg.panelMain.panelLit.panelCol.whiteButton.doClick();

		if (keys.containsKey("seamark:buoy_special_purpose:shape")) {
			str = keys.get("seamark:buoy_special_purpose:shape");

			if (str.equals("pillar"))
				dlg.panelMain.panelSpec.pillarButton.doClick();
			else if (str.equals("can"))
				dlg.panelMain.panelSpec.canButton.doClick();
			else if (str.equals("conical"))
				dlg.panelMain.panelSpec.coneButton.doClick();
			else if (str.equals("spar"))
				dlg.panelMain.panelSpec.sparButton.doClick();
			else if (str.equals("sphere"))
				dlg.panelMain.panelSpec.sphereButton.doClick();
			else if (str.equals("barrel"))
				dlg.panelMain.panelSpec.barrelButton.doClick();
		}

		if (keys.containsKey("seamark:beacon_special_purpose:shape")) {
			str = keys.get("seamark:beacon_special_purpose:shape");
			if (str.equals("tower"))
				dlg.panelMain.panelSpec.towerButton.doClick();
			else
				dlg.panelMain.panelSpec.beaconButton.doClick();
		}

		if (keys.containsKey("seamark:light_float:colour")) {
			dlg.panelMain.panelSpec.floatButton.doClick();
		}

		if ((keys.containsKey("seamark:type") && keys.get("seamark:type").equals("beacon_special_purpose"))
				|| keys.containsKey("seamark:beacon_special_purpose:colour") || keys.containsKey("seamark:beacon_special_purpose:shape")) {
			if (keys.containsKey("seamark:beacon_special_purpose:shape")
					&& keys.get("seamark:beacon_special_purpose:shape").equals("tower"))
				dlg.panelMain.panelSpec.towerButton.doClick();
			else
				dlg.panelMain.panelSpec.beaconButton.doClick();
		} else if (keys.containsKey("seamark:light_float:colour") && keys.get("seamark:light_float:colour").equals("yellow"))
			dlg.panelMain.panelSpec.floatButton.doClick();

		if (keys.containsKey("seamark:topmark:shape")) {
			str = keys.get("seamark:topmark:shape");
/*			setTopMark(true);
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
*/		}

		parseLights(keys);
		parseFogRadar(keys);
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

			if (hasTopmark()) {
				image = "";
				switch (getShape()) {
				case PILLAR:
				case SPAR:
					switch (getTopmark()) {
					case X_SHAPE:
						if (getColour(Ent.TOPMARK) == Col.YELLOW)
							image = "/images/Top_X_Yellow_Buoy.png";
						else
							image = "/images/Top_X_Red_Buoy.png";
						break;
					case CAN:
						image = "/images/Top_Can_Yellow_Buoy.png";
						break;
					case CONE:
						image = "/images/Top_Cone_Yellow_Buoy.png";
						break;
					}
					break;
				case CAN:
				case CONE:
				case SPHERE:
				case BARREL:
					switch (getTopmark()) {
					case X_SHAPE:
						if (getColour(Ent.TOPMARK) == Col.YELLOW)
							image = "/images/Top_X_Yellow_Buoy_Small.png";
						else
							image = "/images/Top_X_Red_Buoy_Small.png";
						break;
					case CAN:
						image = "/images/Top_Can_Yellow_Buoy_Small.png";
						break;
					case CONE:
						image = "/images/Top_Cone_Yellow_Buoy_Small.png";
						break;
					}
					break;
				case BEACON:
				case TOWER:
					switch (getTopmark()) {
					case X_SHAPE:
						if (getColour(Ent.TOPMARK) == Col.YELLOW)
							image = "/images/Top_X_Yellow_Beacon.png";
						else
							image = "/images/Top_X_Red_Beacon.png";
						break;
					case CAN:
						image = "/images/Top_Can_Yellow_Beacon.png";
						break;
					case CONE:
						image = "/images/Top_Cone_Yellow_Beacon.png";
						break;
					}
					break;
				case FLOAT:
				case SUPER:
					switch (getTopmark()) {
					case X_SHAPE:
						if (getColour(Ent.TOPMARK) == Col.YELLOW)
							image = "/images/Top_X_Yellow_Float.png";
						else
							image = "/images/Top_X_Red_Float.png";
						break;
					case CAN:
						image = "/images/Top_Can_Yellow_Float.png";
						break;
					case CONE:
						image = "/images/Top_Cone_Yellow_Float.png";
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
}
