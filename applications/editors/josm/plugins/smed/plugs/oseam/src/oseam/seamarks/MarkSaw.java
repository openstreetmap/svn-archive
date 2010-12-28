package oseam.seamarks;

import java.util.Map;

import javax.swing.ImageIcon;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class MarkSaw extends SeaMark {
	public MarkSaw(OSeaMAction dia) {
		super(dia);
	}

	public void parseMark() {

		String str;
		Map<String, String> keys;
		keys = dlg.node.getKeys();

		if (!dlg.panelMain.chanButton.isSelected())
			dlg.panelMain.chanButton.doClick();
		if (!dlg.panelMain.panelChan.safeWaterButton.isSelected())
			dlg.panelMain.panelChan.safeWaterButton.doClick();

		if (keys.containsKey("name"))
			setName(keys.get("name"));

		if (keys.containsKey("seamark:name"))
			setName(keys.get("seamark:name"));

		if (keys.containsKey("seamark:buoy_safe_water:name"))
			setName(keys.get("seamark:buoy_safe_water:name"));
		else if (keys.containsKey("seamark:beacon_safe_water:name"))
			setName(keys.get("seamark:beacon_safe_water:name"));
		else if (keys.containsKey("seamark:light_float:name"))
			setName(keys.get("seamark:light_float:name"));

		if (keys.containsKey("seamark:buoy_safe_water:shape")) {
			str = keys.get("seamark:buoy_safe_water:shape");

			if (str.equals("pillar"))
				dlg.panelMain.panelChan.panelSaw.pillarButton.doClick();
			else if (str.equals("spar"))
				dlg.panelMain.panelChan.panelSaw.sparButton.doClick();
			else if (str.equals("sphere"))
				dlg.panelMain.panelChan.panelSaw.sphereButton.doClick();
			else if (str.equals("barrel"))
				dlg.panelMain.panelChan.panelSaw.barrelButton.doClick();
		} else if ((keys.containsKey("seamark:type")) && (keys.get("seamark:type").equals("light_float"))) {
			dlg.panelMain.panelChan.panelSaw.floatButton.doClick();
		}

		if (keys.containsKey("seamark:topmark:shape") || keys.containsKey("seamark:topmark:colour")) {
//			setTopMark(true);
		}

		parseLights(keys);
		parseFogRadar(keys);
	}

	public void paintSign() {

		String image = "/images/Safe_Water";

		switch (getShape()) {
		case PILLAR:
			image += "_Pillar";
			break;
		case SPAR:
			image += "_Spar";
			break;
		case SPHERE:
			image += "_Sphere";
			break;
		case BEACON:
			image += "_Beacon";
			break;
		case FLOAT:
			image += "_Float";
			break;
		default:
		}

		if (!image.equals("/images/Safe_Water")) {
			image += ".png";
			dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource(image)));
			if (hasTopmark()) {
				image = "";
				switch (getShape()) {
				case PILLAR:
				case SPAR:
					image = "/images/Top_Sphere_Red_Buoy.png";
					break;
				case SPHERE:
					image = "/images/Top_Sphere_Red_Buoy_Small.png";
					break;
				case BEACON:
					image = "/images/Top_Sphere_Red_Beacon.png";
					break;
				case FLOAT:
					image = "/images/Top_Sphere_Red_Float.png";
					break;
				}
				if (!image.isEmpty())
					dlg.panelMain.topIcon.setIcon(new ImageIcon(getClass().getResource(image)));
			} else
				dlg.panelMain.topIcon.setIcon(null);
		} else {
			dlg.panelMain.shapeIcon.setIcon(null);
			dlg.panelMain.topIcon.setIcon(null);
		}
		super.paintSign();
	}
}
