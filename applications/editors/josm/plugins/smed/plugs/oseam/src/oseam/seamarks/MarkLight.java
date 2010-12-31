package oseam.seamarks;

import java.util.Map;

import javax.swing.ImageIcon;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class MarkLight extends SeaMark {
	public MarkLight(OSeaMAction dia) {
		super(dia);
	}

	public void parseMark() {

		Map<String, String> keys;
		keys = dlg.node.getKeys();

		if (!dlg.panelMain.lightsButton.isSelected())
			dlg.panelMain.lightsButton.doClick();

		if (keys.containsKey("seamark:type")) {
			String type = keys.get("seamark:type");
			if (type.equals("landmark"))
				setCategory(Cat.LIGHT_HOUSE);
			else if (type.equals("light_major"))
				setCategory(Cat.LIGHT_MAJOR);
			else if (type.equals("light_minor"))
				setCategory(Cat.LIGHT_MINOR);
			else if (type.equals("light_vessel"))
				setCategory(Cat.LIGHT_VESSEL);
			else if (type.equals("light_float"))
				setCategory(Cat.LIGHT_FLOAT);
		}

		super.parseMark();
	}

}
