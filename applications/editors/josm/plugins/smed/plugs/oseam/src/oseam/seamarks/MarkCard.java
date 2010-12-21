package oseam.seamarks;

import java.util.Map;
import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class MarkCard extends SeaMark {

	public MarkCard(OSeaMAction dia) {
		super(dia);
	}
	
	public void parseMark() {
		String str;
		Map<String, String> keys;
		keys = dlg.node.getKeys();

		if (!dlg.panelMain.hazButton.isSelected())
			dlg.panelMain.hazButton.doClick();

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

		if (cat.isEmpty()) {
			if (col.equals("black;yellow")) {
				dlg.panelMain.panelHaz.northButton.doClick();
			} else if (col.equals("black;yellow;black")) {
				dlg.panelMain.panelHaz.eastButton.doClick();
			} else if (col.equals("yellow;black")) {
				dlg.panelMain.panelHaz.southButton.doClick();
			} else if (col.equals("yellow;black;yellow")) {
				dlg.panelMain.panelHaz.westButton.doClick();
			}
		} else if (cat.equals("north")) {
			dlg.panelMain.panelHaz.northButton.doClick();
		} else if (cat.equals("east")) {
			dlg.panelMain.panelHaz.eastButton.doClick();
		} else if (cat.equals("south")) {
			dlg.panelMain.panelHaz.southButton.doClick();
		} else if (cat.equals("west")) {
			dlg.panelMain.panelHaz.westButton.doClick();
		}

		if (keys.containsKey("seamark:buoy_cardinal:shape")) {
			str = keys.get("seamark:buoy_cardinal:shape");

			if (str.equals("pillar")) {
				dlg.panelMain.panelHaz.pillarButton.doClick();
			} else if (str.equals("spar")) {
				dlg.panelMain.panelHaz.sparButton.doClick();
			}
		} else if (keys.containsKey("seamark:beacon_cardinal:shape")) {
				str = keys.get("seamark:beacon_cardinal:shape");
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

		// dlg.cbM01CatOfMark.setSelectedIndex(getMarkIndex());
		// dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
		// dlg.tfM01Name.setText(getName());
		// dlg.cM01TopMark.setSelected(hasTopMark());
	}

	public void setLightColour() {
		super.setLightColour("W");
	}

	public void paintSign() {
		/*
		if (dlg.paintlock) return; super.paintSign();
		 */
		if ((getCategory() != Cat.UNKNOWN) && (getShape() != Shp.UNKNOWN)) {
			String image = "/images/Cardinal";
			switch (getShape()) {
			case PILLAR:
				image += "_Pillar";
				break;
			case SPAR:
				image += "_Spar";
				break;
			case BEACON:
				image += "_Beacon";
				break;
			case TOWER:
				image += "_Tower";
				break;
			case FLOAT:
				image += "_Float";
				break;
			default:
				return;
			}

			switch (getCategory()) {
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
				dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass()
						.getResource(image)));
			} else
				dlg.panelMain.shapeIcon.setIcon(null);
		}
	}

	public void saveSign() {
		if (dlg.node == null)
			return;

		String shape = "";

		switch (getShape()) {
		case PILLAR:
			super.saveSign("buoy_cardinal");
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
					"seamark:buoy_cardinal:shape", "pillar"));
			break;
		case SPAR:
			super.saveSign("buoy_cardinal");
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
					"seamark:buoy_cardinal:shape", "spar"));
			break;
		case BEACON:
			super.saveSign("beacon_cardinal");
			break;
		case TOWER:
			super.saveSign("beacon_cardinal");
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
					"seamark:beacon_cardinal:shape", "tower"));
			break;
		case FLOAT:
			super.saveSign("light_float");
			break;
		default:
		}

		switch (getShape()) {
		case PILLAR:
		case SPAR:
			switch (getCategory()) {
			case CARD_NORTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:buoy_cardinal:category", "north"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:buoy_cardinal:colour", "black;yellow"));
				shape = "2 cones up";
				break;

			case CARD_EAST:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:buoy_cardinal:category", "east"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:buoy_cardinal:colour", "black;yellow;black"));
				shape = "2 cones base together";
				break;

			case CARD_SOUTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:buoy_cardinal:category", "south"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:buoy_cardinal:colour", "yellow;black"));
				shape = "2 cones down";
				break;

			case CARD_WEST:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:buoy_cardinal:category", "west"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:buoy_cardinal:colour", "yellow;black;yellow"));
				shape = "2 cones point together";
				break;
			}
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
					"seamark:buoy_cardinal:colour_pattern",
					"horizontal stripes"));
			break;
			
		case BEACON:
		case TOWER:
			switch (getCategory()) {
			case CARD_NORTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:beacon_cardinal:category", "north"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:beacon_cardinal:colour", "black;yellow"));
				shape = "2 cones up";
				break;

			case CARD_EAST:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:beacon_cardinal:category", "east"));
				Main.main.undoRedo
						.add(new ChangePropertyCommand(dlg.node,
								"seamark:beacon_cardinal:colour",
								"black;yellow;black"));
				shape = "2 cones base together";
				break;

			case CARD_SOUTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:beacon_cardinal:category", "south"));
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:beacon_cardinal:colour", "yellow;black"));
				shape = "2 cones down";
				break;

			case CARD_WEST:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:beacon_cardinal:category", "west"));
				Main.main.undoRedo
						.add(new ChangePropertyCommand(dlg.node,
								"seamark:beacon_cardinal:colour",
								"yellow;black;yellow"));
				shape = "2 cones point together";
				break;
			}
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
					"seamark:beacon_cardinal:colour_pattern",
					"horizontal stripes"));
			break;
			
		case FLOAT:
			switch (getCategory()) {
			case CARD_NORTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:light_float:colour", "black;yellow"));
				shape = "2 cones up";
				break;

			case CARD_EAST:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:light_float:colour", "black;yellow;black"));
				shape = "2 cones base together";
				break;

			case CARD_SOUTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:light_float:colour", "yellow;black"));
				shape = "2 cones down";
				break;

			case CARD_WEST:
				Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node,
						"seamark:light_float:colour", "yellow;black;yellow"));
				shape = "2 cones point together";
				break;
			}
			Main.main.undoRedo
					.add(new ChangePropertyCommand(dlg.node,
							"seamark:light_float:colour_pattern",
							"horizontal stripes"));
			break;
		}
		saveTopMarkData(shape, "black");
		saveLightData();
		saveRadarFogData();
	}

}
