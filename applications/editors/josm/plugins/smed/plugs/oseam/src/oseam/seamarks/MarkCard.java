package oseam.seamarks;

import java.util.Map;
import javax.swing.ImageIcon;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class MarkCard extends SeaMark {

	public MarkCard(OSeaMAction dia) {
		super(dia);
	}

	public void parseMark() {
		String str;
		Map<String, String> keys;
		keys = dlg.node.getKeys();

		if (!dlg.panelMain.hazButton.isSelected())
			dlg.panelMain.hazButton.doClick();

		String cat = "";
		String col = "";

		if (keys.containsKey("seamark:buoy_cardinal:category"))
			cat = keys.get("seamark:buoy_cardinal:category");
		else if (keys.containsKey("seamark:beacon_cardinal:category"))
			cat = keys.get("seamark:beacon_cardinal:category");

		if (keys.containsKey("seamark:buoy_cardinal:colour"))
			col = keys.get("seamark:buoy_cardinal:colour");
		else if (keys.containsKey("seamark:beacon_cardinal:colour"))
			col = keys.get("seamark:beacon_cardinal:colour");
		else if (keys.containsKey("seamark:light_float:colour"))
			col = keys.get("seamark:light_float:colour");

		if (cat.isEmpty()) {
			if (col.equals("black;yellow")) {
				dlg.panelMain.panelHaz.northButton.doClick();
			} else if (col.equals("black;yellow;black")) {
				dlg.panelMain.panelHaz.eastButton.doClick();
			} else if (col.equals("yellow;black")) {
				dlg.panelMain.panelHaz.southButton.doClick();
			} else if (col.equals("yellow;black;yellow")) {
				dlg.panelMain.panelHaz.westButton.doClick();
			}
		} else if (cat.equals("north")) {
			dlg.panelMain.panelHaz.northButton.doClick();
		} else if (cat.equals("east")) {
			dlg.panelMain.panelHaz.eastButton.doClick();
		} else if (cat.equals("south")) {
			dlg.panelMain.panelHaz.southButton.doClick();
		} else if (cat.equals("west")) {
			dlg.panelMain.panelHaz.westButton.doClick();
		}

		if (keys.containsKey("seamark:buoy_cardinal:shape")) {
			str = keys.get("seamark:buoy_cardinal:shape");

			if (str.equals("pillar")) {
				dlg.panelMain.panelHaz.pillarButton.doClick();
			} else if (str.equals("spar")) {
				dlg.panelMain.panelHaz.sparButton.doClick();
			}
		} else if (keys.containsKey("seamark:beacon_cardinal:shape")) {
			str = keys.get("seamark:beacon_cardinal:shape");
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

}
