package oseam.seamarks;

import java.util.Map;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Node;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class MarkIsol extends SeaMark {
	public MarkIsol(OSeaMAction dia, Node node) {
		super(dia, node);
	}
	
	public void parseMark() {

		String str;
		Map<String, String> keys;
		keys = getNode().getKeys();

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
		} else if (keys.containsKey("seamark:type")
				&& (keys.get("seamark:type").equals("light_float"))) {
			dlg.panelMain.panelHaz.floatButton.doClick();
		}

		parseLights(keys);
		parseFogRadar(keys);

//		dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
//		dlg.tfM01Name.setText(getName());
//		dlg.cM01TopMark.setSelected(hasTopMark());
	}

	public boolean isValid() {
		return (getCategory() != Cat.UNKNOWN_CAT)
				&& (getShape() != Styl.UNKNOWN_SHAPE);
	}

	public void setLightColour() {
		super.setLightColour("W");
	}

	public void paintSign() {
/*		if (dlg.paintlock)
			return;

		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		if (isValid()) {
			dlg.tfM01Name.setEnabled(true);
			dlg.tfM01Name.setText(getName());
			dlg.cM01TopMark.setVisible(true);
			dlg.cM01Radar.setVisible(true);
			dlg.cM01Racon.setVisible(true);
			dlg.cM01Fog.setVisible(true);
			dlg.cM01Fired.setVisible(true);
			if (!isSectored()) {
				dlg.cbM01Colour.setVisible(false);
				dlg.lM01Colour.setVisible(false);
			}
			dlg.rbM01Fired1.setVisible(false);
			dlg.rbM01FiredN.setVisible(false);
			dlg.lM01Height.setVisible(false);
			dlg.tfM01Height.setVisible(false);
			dlg.lM01Range.setVisible(false);
			dlg.tfM01Range.setVisible(false);

			if (isFired()) {
				switch (getStyleIndex()) {
				case ISOL_BEACON:
				case ISOL_TOWER:
					dlg.lM01Height.setVisible(true);
					dlg.tfM01Height.setVisible(true);
					dlg.lM01Range.setVisible(true);
					dlg.tfM01Range.setVisible(true);
					break;
				default:
				}
			}

			String image = "/images/Cardinal";

			switch (getStyleIndex()) {
			case ISOL_PILLAR:
				image += "_Pillar_Single";
				break;
			case ISOL_SPAR:
				image += "_Spar_Single";
				break;
			case ISOL_BEACON:
				image += "_Beacon_Single";
				break;
			case ISOL_TOWER:
				image += "_Tower_Single";
				break;
			case ISOL_FLOAT:
				image += "_Float_Single";
				break;
			default:
			}

			if (!image.equals("/images/Cardinal")) {
				image += ".png";
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));
			} else
				dlg.lM01Icon.setIcon(null);
		} else {
			dlg.tfM01Name.setEnabled(false);
			dlg.tfM01Name.setText("");
			dlg.cM01TopMark.setVisible(false);
			dlg.cM01Radar.setVisible(false);
			dlg.cM01Racon.setVisible(false);
			dlg.cM01Fog.setVisible(false);
			dlg.cM01Fired.setVisible(false);
		}
*/	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		switch (getShape()) {
		case PILLAR:
			super.saveSign("buoy_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "pillar"));
			break;
		case SPAR:
			super.saveSign("buoy_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "spar"));
			break;
		case BEACON:
			super.saveSign("beacon_isolated_danger");
			break;
		case TOWER:
			super.saveSign("beacon_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:shape", "tower"));
			break;
		case FLOAT:
			super.saveSign("light_float");
			break;
		default:
		}

		switch (getShape()) {
		case PILLAR:
		case SPAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:colour_pattern", "horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:colour", "black;red;black"));
			break;
		case BEACON:
		case TOWER:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:colour_pattern",
					"horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:colour", "black;red;black"));
			break;
		case FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour_pattern", "horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour", "black;red;black"));
			break;
		}

		saveTopMarkData("2 spheres", "black");
		saveLightData();
		saveRadarFogData();
	}
}
