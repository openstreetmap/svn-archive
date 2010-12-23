package oseam.seamarks;

import java.util.Map;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;

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
		}

		parseLights(keys);
		parseFogRadar(keys);

		// dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
		// dlg.tfM01Name.setText(getName());
		// dlg.cM01TopMark.setSelected(hasTopMark());
	}

	public void setLightColour() {
		super.setLightColour("W");
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
		}

		if (!image.equals("/images/Cardinal")) {
			image += ".png";
			dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource(image)));
		} else
			dlg.panelMain.shapeIcon.setIcon(null);
		super.paintSign();
	}

	public void saveSign() {

		if (dlg.node == null) {
			return;
		}

		switch (getShape()) {
		case PILLAR:
			super.saveSign("buoy_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_isolated_danger:shape", "pillar"));
			break;
		case SPAR:
			super.saveSign("buoy_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_isolated_danger:shape", "spar"));
			break;
		case BEACON:
			super.saveSign("beacon_isolated_danger");
			break;
		case TOWER:
			super.saveSign("beacon_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_isolated_danger:shape", "tower"));
			break;
		case FLOAT:
			super.saveSign("light_float");
			break;
		default:
		}

		switch (getShape()) {
		case PILLAR:
		case SPAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_isolated_danger:colour_pattern",
					"horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_isolated_danger:colour", "black;red;black"));
			break;
		case BEACON:
		case TOWER:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_isolated_danger:colour_pattern",
					"horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_isolated_danger:colour", "black;red;black"));
			break;
		case FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour_pattern", "horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour", "black;red;black"));
			break;
		}

		saveTopMarkData("2 spheres", "black");
		saveLightData();
		saveRadarFogData();
	}
}
