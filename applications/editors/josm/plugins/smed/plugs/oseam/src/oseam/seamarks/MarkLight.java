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

		if (keys.containsKey("name"))
			setName(keys.get("name"));

		if (keys.containsKey("seamark:name"))
			setName(keys.get("seamark:name"));

		if (keys.containsKey("seamark:landmark:name"))
			setName(keys.get("seamark:landmark:name"));
		else if (keys.containsKey("seamark:light_major:name"))
			setName(keys.get("seamark:light_major:name"));
		else if (keys.containsKey("seamark:light_minor:name"))
			setName(keys.get("seamark:light_minor:name"));
		else if (keys.containsKey("seamark:light_vessel:name"))
			setName(keys.get("seamark:light_vessel:name"));
		else if (keys.containsKey("seamark:light_float:name"))
			setName(keys.get("seamark:light_float:name"));

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
