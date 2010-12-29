package oseam.seamarks;

import java.util.Map;

import javax.swing.ImageIcon;

import oseam.dialogs.OSeaMAction;

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

		if (cat.isEmpty()) {
			if (col.equals("red")) {
				setColour(Ent.BODY, Col.RED);
				if (top.equals("cylinder")) {
					dlg.panelMain.panelChan.portButton.doClick();
					dlg.panelMain.panelChan.panelPort.regionAButton.doClick();
				} else if (top.equals("cone, point up")) {
					dlg.panelMain.panelChan.stbdButton.doClick();
					dlg.panelMain.panelChan.panelPort.regionBButton.doClick();
				} else {
					if (getRegion() == IALA_A)
						dlg.panelMain.panelChan.portButton.doClick();
					else
						dlg.panelMain.panelChan.stbdButton.doClick();
				}
			} else if (col.equals("green")) {
				setColour(Ent.BODY, Col.GREEN);
				if (top.equals("cone, point up")) {
					dlg.panelMain.panelChan.stbdButton.doClick();
					dlg.panelMain.panelChan.panelPort.regionAButton.doClick();
				} else if (top.equals("cylinder")) {
					dlg.panelMain.panelChan.portButton.doClick();
					dlg.panelMain.panelChan.panelPort.regionBButton.doClick();
				} else {
					if (getRegion() == IALA_A)
						dlg.panelMain.panelChan.stbdButton.doClick();
					else
						dlg.panelMain.panelChan.portButton.doClick();
				}
			} else if (col.equals("red;green;red")) {
				setColour(Ent.BODY, Col.RED_GREEN_RED);
				if (top.equals("cylinder")) {
					dlg.panelMain.panelChan.prefPortButton.doClick();
					dlg.panelMain.panelChan.panelPort.regionAButton.doClick();
				} else if (top.equals("cone, point up")) {
					dlg.panelMain.panelChan.prefStbdButton.doClick();
					dlg.panelMain.panelChan.panelPort.regionBButton.doClick();
				} else {
					if (getRegion() == IALA_A)
						dlg.panelMain.panelChan.prefPortButton.doClick();
					else
						dlg.panelMain.panelChan.prefStbdButton.doClick();
				}
			} else if (col.equals("green;red;green")) {
				setColour(Ent.BODY, Col.GREEN_RED_GREEN);
				if (top.equals("cone, point up")) {
					dlg.panelMain.panelChan.prefStbdButton.doClick();
					dlg.panelMain.panelChan.panelPort.regionAButton.doClick();
				} else if (top.equals("cylinder")) {
					dlg.panelMain.panelChan.prefPortButton.doClick();
					dlg.panelMain.panelChan.panelPort.regionBButton.doClick();
				} else {
					if (getRegion() == IALA_A)
						dlg.panelMain.panelChan.prefStbdButton.doClick();
					else
						dlg.panelMain.panelChan.prefPortButton.doClick();
				}
			}
		} else if (cat.equals("port")) {

			dlg.panelMain.panelChan.portButton.doClick();

			if (col.equals("red")) {
				dlg.panelMain.panelChan.panelPort.regionAButton.doClick();
			} else if (col.equals("green")) {
				dlg.panelMain.panelChan.panelPort.regionBButton.doClick();
			}
		} else if (cat.equals("starboard")) {

			dlg.panelMain.panelChan.stbdButton.doClick();

			if (col.equals("green")) {
				dlg.panelMain.panelChan.panelPort.regionAButton.doClick();
			} else if (col.equals("red")) {
				dlg.panelMain.panelChan.panelPort.regionBButton.doClick();
			}
		} else if (cat.equals("preferred_channel_port")) {

			dlg.panelMain.panelChan.prefPortButton.doClick();

			if (col.equals("red;green;red")) {
				dlg.panelMain.panelChan.panelPort.regionAButton.doClick();
			} else if (col.equals("green;red;green")) {
				dlg.panelMain.panelChan.panelPort.regionBButton.doClick();
			}

		} else if (cat.equals("preferred_channel_starboard")) {

			dlg.panelMain.panelChan.prefStbdButton.doClick();

			if (col.equals("green;red;green")) {
				dlg.panelMain.panelChan.panelPort.regionAButton.doClick();
			} else if (col.equals("red;green;red")) {
				dlg.panelMain.panelChan.panelPort.regionBButton.doClick();
			}
		}

		if (keys.containsKey("seamark:buoy_lateral:shape"))
			str = keys.get("seamark:buoy_lateral:shape");
		else if (keys.containsKey("seamark:beacon_lateral:shape"))
			str = keys.get("seamark:beacon_lateral:shape");
		else str = "";

		switch (getCategory()) {
		case LAT_PORT:
		case LAT_PREF_PORT:
			if (str.equals("can"))
				dlg.panelMain.panelChan.panelPort.canButton.doClick();
			else if (str.equals("pillar"))
				dlg.panelMain.panelChan.panelPort.pillarButton.doClick();
			else if (str.equals("spar"))
				dlg.panelMain.panelChan.panelPort.sparButton.doClick();
			else if (str.equals("tower"))
				setShape(Shp.TOWER);
			else if (str.equals("perch"))
				setShape(Shp.PERCH);
			else if (keys.containsKey("seamark:type") && (keys.get("seamark:type").equals("light_float")))
				dlg.panelMain.panelChan.panelPort.floatButton.doClick();
			else
				dlg.panelMain.panelChan.panelPort.beaconButton.doClick();
			break;

		case LAT_STBD:
		case LAT_PREF_STBD:
			if (str.equals("conical"))
				dlg.panelMain.panelChan.panelStbd.coneButton.doClick();
			else if (str.equals("pillar"))
				dlg.panelMain.panelChan.panelStbd.pillarButton.doClick();
			else if (str.equals("spar"))
				dlg.panelMain.panelChan.panelStbd.sparButton.doClick();
			else if (str.equals("tower"))
				setShape(Shp.TOWER);
			else if (str.equals("perch"))
				setShape(Shp.PERCH);
			else if (keys.containsKey("seamark:type") && (keys.get("seamark:type").equals("light_float")))
				dlg.panelMain.panelChan.panelStbd.floatButton.doClick();
			else
				dlg.panelMain.panelChan.panelStbd.beaconButton.doClick();
			break;
		}

		super.parseMark();
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
					dlg.panelMain.shapeIcon.setIcon(null);
					return;
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
					dlg.panelMain.shapeIcon.setIcon(null);
					return;
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
					dlg.panelMain.shapeIcon.setIcon(null);
					return;
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
					dlg.panelMain.shapeIcon.setIcon(null);
					return;
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
					dlg.panelMain.shapeIcon.setIcon(null);
					return;
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
					dlg.panelMain.shapeIcon.setIcon(null);
					return;
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
					dlg.panelMain.shapeIcon.setIcon(null);
					return;
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
					dlg.panelMain.shapeIcon.setIcon(null);
					return;
				}
			break;
		default:
			dlg.panelMain.shapeIcon.setIcon(null);
			return;
		}
			image += ".png";
			dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource(image)));

		super.paintSign();
	}
}
