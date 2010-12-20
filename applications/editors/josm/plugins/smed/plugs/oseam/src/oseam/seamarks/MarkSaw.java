package oseam.seamarks;

import java.util.Map;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class MarkSaw extends SeaMark {
	public MarkSaw(OSeaMAction dia, Node node) {
		super(dia, node);
	}
	
	public void parseMark() {

		String str;
		Map<String, String> keys;
		keys = getNode().getKeys();

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

/*

		setBuoyIndex(SAFE_WATER);
		setColour(SeaMark.RED_WHITE);
		setLightColour("W");
		setRegion(Main.pref.get("tomsplugin.IALA").equals("B"));

		if (keys.containsKey("seamark:buoy_safe_water:shape")) {
			str = keys.get("seamark:buoy_safe_water:shape");

			if (str.equals("pillar"))
				setStyleIndex(SAFE_PILLAR);
			else if (str.equals("spar"))
				setStyleIndex(SAFE_SPAR);
			else if (str.equals("sphere"))
				setStyleIndex(SAFE_SPHERE);
		} else if ((keys.containsKey("seamark:type"))
				&& (keys.get("seamark:type").equals("light_float"))) {
			setStyleIndex(SAFE_FLOAT);
		} else if ((keys.containsKey("seamark:type"))
				&& (keys.get("seamark:type").equals("beacon_safe_water"))) {
			setStyleIndex(SAFE_BEACON);
		}

		if (getStyleIndex() >= dlg.cbM01StyleOfMark.getItemCount())
			setStyleIndex(0);

		if (keys.containsKey("seamark:topmark:shape")
				|| keys.containsKey("seamark:topmark:colour")) {
			setTopMark(true);
		}

		refreshLights();
		parseLights(keys);
		parseFogRadar(keys);

		dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
		dlg.tfM01Name.setText(getName());
		dlg.cM01TopMark.setSelected(hasTopMark());
*/	}
/*
	public void refreshLights() {
		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem(Messages.getString("SmpDialogAction.212"));
		dlg.cbM01Kennung.addItem("Iso");
		dlg.cbM01Kennung.addItem("Oc");
		dlg.cbM01Kennung.addItem("LFl");
		dlg.cbM01Kennung.addItem("Mo");
		dlg.cbM01Kennung.setSelectedIndex(0);
	}

	public boolean isValid() {
		return (getBuoyIndex() > 0) && (getStyleIndex() > 0);
	}

	public void setLightColour() {
		super.setLightColour("W");
	}
*/
	public void paintSign() {
/*		if (dlg.paintlock)
			return;
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		if (isValid()) {
			dlg.tfM01Name.setEnabled(true);
			dlg.tfM01Name.setText(getName());
			dlg.cM01TopMark.setEnabled(true);
			dlg.cM01TopMark.setVisible(true);
			dlg.cM01Radar.setVisible(true);
			dlg.cM01Racon.setVisible(true);
			dlg.cM01Fog.setVisible(true);
			dlg.cM01Fired.setVisible(true);
			dlg.cM01Fired.setEnabled(true);
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
				case SAFE_FLOAT:
				case SAFE_BEACON:
					dlg.lM01Height.setVisible(true);
					dlg.tfM01Height.setVisible(true);
					dlg.lM01Range.setVisible(true);
					dlg.tfM01Range.setVisible(true);
					break;
				default:
				}
			}

			String image = "/images/Safe_Water";

			switch (getStyleIndex()) {
			case SAFE_PILLAR:
				image += "_Pillar";
				break;
			case SAFE_SPAR:
				image += "_Spar";
				break;
			case SAFE_SPHERE:
				image += "_Sphere";
				break;
			case SAFE_BEACON:
				image += "_Beacon";
				break;
			case SAFE_FLOAT:
				image += "_Float";
				break;
			default:
			}

			if (!image.equals("/images/Safe_Water")) {
				image += ".png";
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));
				if (hasTopMark()) {
					image = "";
					switch (getStyleIndex()) {
					case SAFE_PILLAR:
					case SAFE_SPAR:
						image = "/images/Top_Sphere_Red_Buoy.png";
						break;
					case SAFE_SPHERE:
						image = "/images/Top_Sphere_Red_Buoy_Small.png";
						break;
					case SAFE_BEACON:
						image = "/images/Top_Sphere_Red_Beacon.png";
						break;
					case SAFE_FLOAT:
						image = "/images/Top_Sphere_Red_Float.png";
						break;
					}
					if (!image.isEmpty())
						dlg.lM06Icon.setIcon(new ImageIcon(getClass().getResource(image)));
				}
			} else
				dlg.lM01Icon.setIcon(null);
		}
*/	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		switch (getShape()) {
		case PILLAR:
			super.saveSign("buoy_safe_water");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:shape", "pillar"));
			break;
		case SPAR:
			super.saveSign("buoy_safe_water");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:shape", "spar"));
			break;
		case SPHERE:
			super.saveSign("buoy_safe_water");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:shape", "sphere"));
			break;
		case BEACON:
			super.saveSign("beacon_safe_water");
			break;
		case FLOAT:
			super.saveSign("light_float");
			break;
		default:
		}

		switch (getShape()) {
		case PILLAR:
		case SPAR:
		case SPHERE:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:colour_pattern", "vertical stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:colour", "red;white"));
			break;
		case BEACON:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_safe_water:colour_pattern", "vertical stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_safe_water:colour", "red;white"));
			break;
		case FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour_pattern", "vertical stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour", "red;white"));
			break;
		default:
		}
		saveTopMarkData("sphere", "red");
		saveLightData();
		saveRadarFogData();
	}
}
