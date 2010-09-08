//License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig & Malcolm Herring

package toms.seamarks.buoys;

import java.util.Map;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Node;

import toms.dialogs.SmpDialogAction;
import toms.seamarks.SeaMark;

public class BuoyIsol extends Buoy {
	public BuoyIsol(SmpDialogAction dia, Node node) {
		super(dia);

		String str;
		Map<String, String> keys;
		keys = node.getKeys();
		setNode(node);

		resetMask();

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("Not set");
		dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
		dlg.cbM01StyleOfMark.addItem("Spar Buoy");
		dlg.cbM01StyleOfMark.addItem("Beacon");
		dlg.cbM01StyleOfMark.addItem("Tower");
		dlg.cbM01StyleOfMark.addItem("Float");
		dlg.cbM01StyleOfMark.setVisible(true);
		dlg.lM01StyleOfMark.setVisible(true);

		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem("Not set");
		dlg.cbM01Kennung.addItem("Fl(2)");

		setBuoyIndex(ISOLATED_DANGER);
		setColour(SeaMark.BLACK_RED_BLACK);
		setLightColour("W");
		setTopMark(true);
		setRegion(Main.pref.get("tomsplugin.IALA").equals("B"));

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

			if (str.equals("pillar"))
				setStyleIndex(ISOL_PILLAR);
			else if (str.equals("spar"))
				setStyleIndex(ISOL_SPAR);
		} else if (keys.containsKey("seamark:beacon_isolated_danger:colour")) {
			if (keys.containsKey("seamark:beacon_isolated_danger:shape")) {
				str = keys.get("seamark:beacon_isolated_danger:shape");

				if (str.equals("tower"))
					setStyleIndex(ISOL_TOWER);
				else
					setStyleIndex(ISOL_BEACON);
			} else
				setStyleIndex(ISOL_BEACON);
		} else if (keys.containsKey("seamark:type")
				&& (keys.get("seamark:type").equals("light_float"))) {
			setStyleIndex(CARD_FLOAT);
		}

		if (keys.containsKey("seamark:topmark:shape")
				|| keys.containsKey("seamark:topmark:colour")) {
			setTopMark(true);
		}

		if (keys.containsKey("seamark:light:colour")) {
			str = keys.get("seamark:light:colour");

			if (keys.containsKey("seamark:light:character")) {
				setLightGroup(keys);
				String c = keys.get("seamark:light:character");
				setLightChar(c);
				setLightPeriod(keys);
			}

			if (str.equals("white")) {
				setFired(true);
				setLightColour("W");
			}
		}
	}
	
	public boolean isValid() {
		return (getBuoyIndex() > 0) && (getStyleIndex() > 0);
	}

	public void paintSign() {
		if (dlg.paintlock)
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
	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		switch (getStyleIndex()) {
		case ISOL_PILLAR:
			super.saveSign("buoy_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "pillar"));
			break;
		case ISOL_SPAR:
			super.saveSign("buoy_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "spar"));
			break;
		case ISOL_BEACON:
			super.saveSign("beacon_isolated_danger");
			break;
		case ISOL_TOWER:
			super.saveSign("beacon_isolated_danger");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:shape", "tower"));
			break;
		case ISOL_FLOAT:
			super.saveSign("light_float");
			break;
		default:
		}

		switch (getStyleIndex()) {
		case ISOL_PILLAR:
		case ISOL_SPAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:colour_pattern", "horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:colour", "black;red;black"));
			break;
		case ISOL_BEACON:
		case ISOL_TOWER:
			Main.main.undoRedo
					.add(new ChangePropertyCommand(node,
							"seamark:beacon_isolated_danger:colour_pattern",
							"horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:colour", "black;red;black"));
			break;
		case ISOL_FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour_pattern", "horizontal stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour", "black;red;black"));
			break;
		}

		saveTopMarkData("2 spheres", "black");
		saveLightData("white");
		saveRadarFogData();

	}

	public void setLightColour() {
		super.setLightColour("W");
	}

}
