package oseam.seamarks;

import java.util.Map;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;

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

		/*
		 * if (keys.containsKey("seamark:type")) { String type =
		 * keys.get("seamark:type"); if (type.equals("landmark"))
		 * setBuoyIndex(LIGHT_HOUSE); else if (type.equals("light_major"))
		 * setBuoyIndex(LIGHT_MAJOR); else if (type.equals("light_minor"))
		 * setBuoyIndex(LIGHT_MINOR); else if (type.equals("light_vessel"))
		 * setBuoyIndex(LIGHT_VESSEL); }
		 * 
		 * parseLights(keys); parseFogRadar(keys); setTopMark(false);
		 * setFired(true);
		 * 
		 * dlg.cbM01CatOfMark.setSelectedIndex(getBuoyIndex());
		 * dlg.tfM01Name.setText(getName()); dlg.cM01Fired.setEnabled(false);
		 * dlg.cM01Fired.setSelected(true);
		 */}

	public void paintSign() {
		switch (getCategory()) {
		case LIGHT_HOUSE:
			dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource("/images/Light_House.png")));
			break;

		case LIGHT_MAJOR:
			dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource("/images/Light_Major.png")));
			break;

		case LIGHT_MINOR:
			dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource("/images/Light_Minor.png")));
			break;

		case LIGHT_VESSEL:
			dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource("/images/Float_Major.png")));
			break;

		case LIGHT_FLOAT:
			dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource("/images/Float_Minor.png")));
			break;
		}
		super.paintSign();
	}

}
