package oseam.seamarks;

import java.util.Map;

import javax.swing.ImageIcon;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class MarkIsol extends SeaMark {
	public MarkIsol(OSeaMAction dia) {
		super(dia);
	}

	public void parseMark() {

		String str;
		Map<String, String> keys;
		keys = dlg.node.getKeys();

		if (!dlg.panelMain.hazButton.isSelected())
			dlg.panelMain.hazButton.doClick();

		if (!dlg.panelMain.panelHaz.isolButton.isSelected())
			dlg.panelMain.panelHaz.isolButton.doClick();

		if (keys.containsKey("name"))
			setName(keys.get("name"));

		if (keys.containsKey("seamark:name"))
			setName(keys.get("seamark:name"));

		if (keys.containsKey("seamark:buoy_isolated_danger:name"))
			setName(keys.get("seamark:buoy_isolated_danger:name"));
		else if (keys.containsKey("seamark:beacon_isolated_danger:name"))
			setName(keys.get("seamark:beacon_isolated_danger:name"));
		else if (keys.containsKey("seamark:light_float:name"))
			setName(keys.get("seamark:light_float:name"));

		if (keys.containsKey("seamark:buoy_isolated_danger:shape")) {
			str = keys.get("seamark:buoy_isolated_danger:shape");

			if (str.equals("pillar")) {
				dlg.panelMain.panelHaz.pillarButton.doClick();
			} else if (str.equals("spar")) {
				dlg.panelMain.panelHaz.sparButton.doClick();
			}
		} else if (keys.containsKey("seamark:beacon_isolated_danger:shape")) {
			str = keys.get("seamark:beacon_isolated_danger:shape");
			if (str.equals("tower")) {
				dlg.panelMain.panelHaz.towerButton.doClick();
			} else {
				dlg.panelMain.panelHaz.beaconButton.doClick();
			}
		} else if (keys.containsKey("seamark:type") && (keys.get("seamark:type").equals("light_float"))) {
			dlg.panelMain.panelHaz.floatButton.doClick();
		} else {
			dlg.panelMain.panelHaz.beaconButton.doClick();
		}

		super.parseMark();
	}

	public void paintSign() {
		String image = "/images/Cardinal";

		switch (getShape()) {
		case PILLAR:
			image += "_Pillar_Single";
			break;
		case SPAR:
			image += "_Spar_Single";
			break;
		case BEACON:
			image += "_Beacon_Single";
			break;
		case TOWER:
			image += "_Tower_Single";
			break;
		case FLOAT:
			image += "_Float_Single";
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
