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

public class BuoyCard extends Buoy {

	public BuoyCard(SmpDialogAction dia, Node node) {
		super(dia);

		String str;
		Map<String, String> keys;
		keys = node.getKeys();
		setNode(node);

		resetMask();
		dlg.cbM01CatOfMark.setEnabled(true);
		dlg.cbM01CatOfMark.setVisible(true);
		dlg.lM01CatOfMark.setVisible(true);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("Not set");
		dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
		dlg.cbM01StyleOfMark.addItem("Spar Buoy");
		dlg.cbM01StyleOfMark.addItem("Beacon");
		dlg.cbM01StyleOfMark.addItem("Tower");
		dlg.cbM01StyleOfMark.addItem("Float");
		dlg.cbM01StyleOfMark.setVisible(true);
		dlg.lM01StyleOfMark.setVisible(true);

		setRegion(Main.pref.get("tomsplugin.IALA").equals("B"));
		if (keys.containsKey("name"))
			setName(keys.get("name"));

		if (keys.containsKey("seamark:name"))
			setName(keys.get("seamark:name"));

		if (keys.containsKey("seamark:buoy_cardinal:name"))
			setName(keys.get("seamark:buoy_cardinal:name"));
		else if (keys.containsKey("seamark:beacon_cardinal:name"))
			setName(keys.get("seamark:beacon_cardinal:name"));
		else if (keys.containsKey("seamark:light_float:name"))
			setName(keys.get("seamark:light_float:name"));

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

		if (cat.equals("")) {
			if (col.equals("black;yellow")) {
				setBuoyIndex(CARD_NORTH);
				setColour(BLACK_YELLOW);
			} else if (col.equals("black;yellow;black")) {
				setBuoyIndex(CARD_EAST);
				setColour(BLACK_YELLOW_BLACK);
			} else if (col.equals("yellow;black")) {
				setBuoyIndex(CARD_SOUTH);
				setColour(YELLOW_BLACK);
			} else if (col.equals("yellow;black;yellow")) {
				setBuoyIndex(CARD_WEST);
				setColour(YELLOW_BLACK_YELLOW);
			}
		} else if (cat.equals("north")) {
			setBuoyIndex(CARD_NORTH);
			setColour(BLACK_YELLOW);
		} else if (cat.equals("east")) {
			setBuoyIndex(CARD_EAST);
			setColour(BLACK_YELLOW_BLACK);
		} else if (cat.equals("south")) {
			setBuoyIndex(CARD_SOUTH);
			setColour(YELLOW_BLACK);
		} else if (cat.equals("west")) {
			setBuoyIndex(CARD_WEST);
			setColour(YELLOW_BLACK_YELLOW);
		}

		if (keys.containsKey("seamark:buoy_cardinal:shape")) {
			str = keys.get("seamark:buoy_cardinal:shape");

			if (str.equals("pillar"))
				setStyleIndex(CARD_PILLAR);
			else if (str.equals("spar"))
				setStyleIndex(CARD_SPAR);
		} else if (keys.containsKey("seamark:beacon_cardinal:colour")) {
			if (keys.containsKey("seamark:beacon_cardinal:shape")) {
				str = keys.get("seamark:beacon_cardinal:shape");

				if (str.equals("tower"))
					setStyleIndex(CARD_TOWER);
				else
					setStyleIndex(CARD_BEACON);
			} else
				setStyleIndex(CARD_BEACON);
		} else if (keys.containsKey("seamark:type")
				&& (keys.get("seamark:type").equals("light_float"))) {
			setStyleIndex(CARD_FLOAT);
		}

		refreshLights();

		if (keys.containsKey("seamark:light:colour")) {
			str = keys.get("seamark:light:colour");

			if (keys.containsKey("seamark:light:character")) {
				int i1;
				String tmp = null;

				setLightGroup(keys);

				String c = keys.get("seamark:light:character");

				if (c.contains("+")) {
					i1 = c.indexOf("+");
					tmp = c.substring(i1, c.length());
					c = c.substring(0, i1);
				}

				if (getLightGroup() != "")
					if (tmp != null) {
						c = c + tmp;
					}

				setLightChar(c);
				setLightPeriod(keys);
			}

			if (str.equals("white")) {
				setFired(true);
				setLightColour("W");
			}
		}
	}

	public void refreshLights() {
		int type = getBuoyIndex();

		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem("Not set");
		dlg.cbM01Kennung.setSelectedIndex(0);

		switch (type) {
		case SeaMark.CARD_NORTH:
			dlg.cbM01Kennung.addItem("Q");
			dlg.cbM01Kennung.addItem("VQ");
			break;

		case SeaMark.CARD_EAST:
			dlg.cbM01Kennung.addItem("Q(3)");
			dlg.cbM01Kennung.addItem("VQ(3)");
			break;

		case SeaMark.CARD_SOUTH:
			dlg.cbM01Kennung.addItem("Q(6)+LFl");
			dlg.cbM01Kennung.addItem("VQ(6)+LFl");
			break;

		case SeaMark.CARD_WEST:
			dlg.cbM01Kennung.addItem("Q(9)");
			dlg.cbM01Kennung.addItem("VQ(9)");
			break;

		default:
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

		if ((getBuoyIndex() > 0) && (getStyleIndex() > 0)) {
			dlg.tfM01Name.setEnabled(true);
			dlg.tfM01Name.setText(getName());
			dlg.cM01TopMark.setSelected(true);
			dlg.cM01TopMark.setVisible(true);
			dlg.cM01TopMark.setEnabled(false);
			dlg.cM01Radar.setEnabled(true);
			dlg.cM01Radar.setVisible(true);
			dlg.cM01Racon.setEnabled(true);
			dlg.cM01Racon.setVisible(true);
			dlg.cM01Fog.setEnabled(true);
			dlg.cM01Fog.setVisible(true);
			dlg.cM01Fired.setEnabled(true);
			dlg.cM01Fired.setVisible(true);

			String image = "/images/Cardinal";

			switch (getStyleIndex()) {
			case SeaMark.CARD_PILLAR:
				image += "_Pillar";
				break;

			case SeaMark.CARD_SPAR:
				image += "_Spar";
				break;

			case SeaMark.CARD_BEACON:
				image += "_Beacon";
				break;

			case SeaMark.CARD_TOWER:
				image += "_Tower";
				break;

			case SeaMark.CARD_FLOAT:
				image += "_Float";
				break;

			default:
				return;
			}

			switch (getBuoyIndex()) {
			case CARD_NORTH:
				image += "_North";
				break;
			case CARD_EAST:
				image += "_East";
				break;
			case CARD_SOUTH:
				image += "_South";
				break;
			case CARD_WEST:
				image += "_West";
				break;
			default:
				return;
			}

			if (!image.equals("/images/Cardinal")) {
				image += ".png";
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));

			} else
				dlg.lM01Icon.setIcon(null);
		}
	}

	public void setLightColour() {
		super.setLightColour("W");
	}

	public void saveSign() {
		Node node = getNode();
		if (node == null) {
			return;
		}

		String shape = "";

		switch (getStyleIndex()) {
		case CARD_PILLAR:
			super.saveSign("buoy_cardinal");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_cardinal:shape", "pillar"));
			break;
		case CARD_SPAR:
			super.saveSign("buoy_cardinal");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_cardinal:shape", "spar"));
			break;
		case CARD_BEACON:
			super.saveSign("beacon_cardinal");
			break;
		case CARD_TOWER:
			super.saveSign("beacon_cardinal");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_cardinal:shape", "tower"));
			break;
		case CARD_FLOAT:
			super.saveSign("light_float");
			break;
		default:
		}

		switch (getStyleIndex()) {
		case CARD_PILLAR:
		case CARD_SPAR:
			switch (getBuoyIndex()) {
			case SeaMark.CARD_NORTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "north"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "black;yellow"));
				shape = "2 cones up";
				break;

			case SeaMark.CARD_EAST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "east"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "black;yellow;black"));
				shape = "2 cones base together";
				break;

			case SeaMark.CARD_SOUTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "south"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "yellow;black"));
				shape = "2 cones down";
				break;

			case SeaMark.CARD_WEST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "west"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "yellow;black;yellow"));
				shape = "2 cones point together";
				break;
			}
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_cardinal:colour_pattern", "horizontal stripes"));
			break;
		case CARD_BEACON:
		case CARD_TOWER:
			switch (getBuoyIndex()) {
			case SeaMark.CARD_NORTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "north"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "black;yellow"));
				shape = "2 cones up";
				break;

			case SeaMark.CARD_EAST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "east"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "black;yellow;black"));
				shape = "2 cones base together";
				break;

			case SeaMark.CARD_SOUTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "south"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "yellow;black"));
				shape = "2 cones down";
				break;

			case SeaMark.CARD_WEST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "west"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "yellow;black;yellow"));
				shape = "2 cones point together";
				break;
			}
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_cardinal:colour_pattern", "horizontal stripes"));
			break;
		case CARD_FLOAT:
			switch (getBuoyIndex()) {
			case SeaMark.CARD_NORTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour", "black;yellow"));
				shape = "2 cones up";
				break;

			case SeaMark.CARD_EAST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour", "black;yellow;black"));
				shape = "2 cones base together";
				break;

			case SeaMark.CARD_SOUTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour", "yellow;black"));
				shape = "2 cones down";
				break;

			case SeaMark.CARD_WEST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour", "yellow;black;yellow"));
				shape = "2 cones point together";
				break;
			}
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour_pattern", "horizontal stripes"));
			break;
		}
		saveTopMarkData(shape, "black");
		saveLightData("white");
		saveRadarFogData();
	}
}
