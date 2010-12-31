package oseam.seamarks;

import java.util.Map;

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

		if (keys.containsKey("seamark:buoy_special_purpose:name"))
			dlg.panelMain.nameBox.setText(keys.get("seamark:buoy_special_purpose:name"));
		else if (keys.containsKey("seamark:beacon_special_purpose:name"))
			dlg.panelMain.nameBox.setText(keys.get("seamark:beacon_special_purpose:name"));
		else if (keys.containsKey("seamark:light_float:name"))
			dlg.panelMain.nameBox.setText(keys.get("seamark:light_float:name"));

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
		}

		super.parseMark();
	}

}
