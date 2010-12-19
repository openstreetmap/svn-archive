package oseam.seamarks;

import java.util.Map;

import org.openstreetmap.josm.data.osm.Node;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;
import oseam.seamarks.SeaMark.Cat;
import oseam.seamarks.SeaMark.Styl;

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

		dlg.panelMain.panelHaz.isolButton.doClick();

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

			String image = "/images/Cardinal"; //$NON-NLS-1$

			switch (getStyleIndex()) {
			case ISOL_PILLAR:
				image += "_Pillar_Single"; //$NON-NLS-1$
				break;
			case ISOL_SPAR:
				image += "_Spar_Single"; //$NON-NLS-1$
				break;
			case ISOL_BEACON:
				image += "_Beacon_Single"; //$NON-NLS-1$
				break;
			case ISOL_TOWER:
				image += "_Tower_Single"; //$NON-NLS-1$
				break;
			case ISOL_FLOAT:
				image += "_Float_Single"; //$NON-NLS-1$
				break;
			default:
			}

			if (!image.equals("/images/Cardinal")) { //$NON-NLS-1$
				image += ".png"; //$NON-NLS-1$
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));
			} else
				dlg.lM01Icon.setIcon(null);
		} else {
			dlg.tfM01Name.setEnabled(false);
			dlg.tfM01Name.setText(""); //$NON-NLS-1$
			dlg.cM01TopMark.setVisible(false);
			dlg.cM01Radar.setVisible(false);
			dlg.cM01Racon.setVisible(false);
			dlg.cM01Fog.setVisible(false);
			dlg.cM01Fired.setVisible(false);
		}
*/	}

	public void saveSign() {
/*		Node node = getNode();

		if (node == null) {
			return;
		}

		switch (getStyleIndex()) {
		case ISOL_PILLAR:
			super.saveSign("buoy_isolated_danger"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "pillar")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case ISOL_SPAR:
			super.saveSign("buoy_isolated_danger"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "spar")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case ISOL_BEACON:
			super.saveSign("beacon_isolated_danger"); //$NON-NLS-1$
			break;
		case ISOL_TOWER:
			super.saveSign("beacon_isolated_danger"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:shape", "tower")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case ISOL_FLOAT:
			super.saveSign("light_float"); //$NON-NLS-1$
			break;
		default:
		}

		switch (getStyleIndex()) {
		case ISOL_PILLAR:
		case ISOL_SPAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:colour_pattern", "horizontal stripes")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:colour", "black;red;black")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case ISOL_BEACON:
		case ISOL_TOWER:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:colour_pattern", //$NON-NLS-1$
					"horizontal stripes")); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:colour", "black;red;black")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case ISOL_FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour_pattern", "horizontal stripes")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour", "black;red;black")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		}

		saveTopMarkData("2 spheres", "black"); //$NON-NLS-1$ //$NON-NLS-2$
		saveLightData(); //$NON-NLS-1$
		saveRadarFogData();
*/	}
}
