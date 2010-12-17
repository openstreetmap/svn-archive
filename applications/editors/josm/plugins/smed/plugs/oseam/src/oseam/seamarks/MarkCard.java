package oseam.seamarks;

import java.util.Map;

import org.openstreetmap.josm.data.osm.Node;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class MarkCard extends SeaMark {

	public MarkCard(OSeaMAction dia, Node node) {
		super(dia);

		String str;
		Map<String, String> keys;
		keys = node.getKeys();
		setNode(node);
		
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
				setCategory(Cat.CARD_NORTH);
				setColour(Col.BLACK_YELLOW);
			} else if (col.equals("black;yellow;black")) {
				dlg.panelMain.panelHaz.eastButton.doClick();
				setCategory(Cat.CARD_EAST);
				setColour(Col.BLACK_YELLOW_BLACK);
			} else if (col.equals("yellow;black")) {
				dlg.panelMain.panelHaz.southButton.doClick();
				setCategory(Cat.CARD_SOUTH);
				setColour(Col.YELLOW_BLACK);
			} else if (col.equals("yellow;black;yellow")) {
				dlg.panelMain.panelHaz.westButton.doClick();
				setCategory(Cat.CARD_WEST);
				setColour(Col.YELLOW_BLACK_YELLOW);
			}
		} else if (cat.equals("north")) {
			dlg.panelMain.panelHaz.northButton.doClick();
			setCategory(Cat.CARD_NORTH);
			setColour(Col.BLACK_YELLOW);
		} else if (cat.equals("east")) {
			dlg.panelMain.panelHaz.eastButton.doClick();
			setCategory(Cat.CARD_EAST);
			setColour(Col.BLACK_YELLOW_BLACK);
		} else if (cat.equals("south")) {
			dlg.panelMain.panelHaz.southButton.doClick();
			setCategory(Cat.CARD_SOUTH);
			setColour(Col.YELLOW_BLACK);
		} else if (cat.equals("west")) {
			dlg.panelMain.panelHaz.westButton.doClick();
			setCategory(Cat.CARD_WEST);
			setColour(Col.YELLOW_BLACK_YELLOW);
		}

		if (keys.containsKey("seamark:buoy_cardinal:shape")) {
			str = keys.get("seamark:buoy_cardinal:shape");

			if (str.equals("pillar")) {
				dlg.panelMain.panelHaz.pillarButton.doClick();
				setShape(Styl.PILLAR);
			} else if (str.equals("spar")) {
				dlg.panelMain.panelHaz.sparButton.doClick();
				setShape(Styl.SPAR);
			}
		} else if (keys.containsKey("seamark:beacon_cardinal:colour")) {
			if (keys.containsKey("seamark:beacon_cardinal:shape")) {
				str = keys.get("seamark:beacon_cardinal:shape");

				if (str.equals("tower")) {
					setShape(Styl.TOWER);
					dlg.panelMain.panelHaz.towerButton.doClick();
				} else {
					dlg.panelMain.panelHaz.beaconButton.doClick();
					setShape(Styl.BEACON);
				}
			} else {
				dlg.panelMain.panelHaz.beaconButton.doClick();
				setShape(Styl.BEACON);
			}
		} else if (keys.containsKey("seamark:type")
				&& (keys.get("seamark:type").equals("light_float"))) {
			dlg.panelMain.panelHaz.floatButton.doClick();
			setShape(Styl.FLOAT);
		}

//		refreshLights();
//		parseLights(keys);
//		parseFogRadar(keys);

//		dlg.cbM01CatOfMark.setSelectedIndex(getMarkIndex());
//		dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
//		dlg.tfM01Name.setText(getName());
//		dlg.cM01TopMark.setSelected(hasTopMark());
	}
/*
		public void refreshLights() {
		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem(Messages.getString("SmpDialogAction.212")); //$NON-NLS-1$
		dlg.cbM01Kennung.setSelectedIndex(0);

		switch (getMarkIndex()) {
		case CARD_NORTH:
			dlg.cbM01Kennung.addItem("Q"); //$NON-NLS-1$
			dlg.cbM01Kennung.addItem("VQ"); //$NON-NLS-1$
			break;
		case CARD_EAST:
			dlg.cbM01Kennung.addItem("Q(3)"); //$NON-NLS-1$
			dlg.cbM01Kennung.addItem("VQ(3)"); //$NON-NLS-1$
			break;
		case CARD_SOUTH:
			dlg.cbM01Kennung.addItem("Q(6)+LFl"); //$NON-NLS-1$
			dlg.cbM01Kennung.addItem("VQ(6)+LFl"); //$NON-NLS-1$
			break;
		case CARD_WEST:
			dlg.cbM01Kennung.addItem("Q(9)"); //$NON-NLS-1$
			dlg.cbM01Kennung.addItem("VQ(9)"); //$NON-NLS-1$
			break;
		}
	}

	public boolean isValid() {
		return (getMarkIndex() > 0) && (getStyleIndex() > 0);
	}
*/
	public void setLightColour() {
		super.setLightColour("W"); //$NON-NLS-1$
	}

	public void paintSign() {
/*		if (dlg.paintlock)
			return;
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		if (isValid()) {
			dlg.tfM01Name.setEnabled(true);
			dlg.tfM01Name.setText(getName());
			dlg.cM01TopMark.setSelected(true);
			dlg.cM01TopMark.setVisible(true);
			dlg.cM01TopMark.setEnabled(false);
			dlg.cM01Radar.setVisible(true);
			dlg.cM01Racon.setVisible(true);
			dlg.cM01Fog.setVisible(true);
			dlg.cM01Fired.setEnabled(true);
			dlg.cM01Fired.setVisible(true);
			dlg.tfM01Group.setVisible(false);
			dlg.lM01Group.setVisible(false);
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
				case CARD_BEACON:
				case CARD_TOWER:
				case CARD_FLOAT:
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
			case SeaMark.CARD_PILLAR:
				image += "_Pillar"; //$NON-NLS-1$
				break;
			case SeaMark.CARD_SPAR:
				image += "_Spar"; //$NON-NLS-1$
				break;
			case SeaMark.CARD_BEACON:
				image += "_Beacon"; //$NON-NLS-1$
				break;
			case SeaMark.CARD_TOWER:
				image += "_Tower"; //$NON-NLS-1$
				break;
			case SeaMark.CARD_FLOAT:
				image += "_Float"; //$NON-NLS-1$
				break;
			default:
				return;
			}

			switch (getMarkIndex()) {
			case CARD_NORTH:
				image += "_North"; //$NON-NLS-1$
				break;
			case CARD_EAST:
				image += "_East"; //$NON-NLS-1$
				break;
			case CARD_SOUTH:
				image += "_South"; //$NON-NLS-1$
				break;
			case CARD_WEST:
				image += "_West"; //$NON-NLS-1$
				break;
			default:
				return;
			}

			if (!image.equals("/images/Cardinal")) { //$NON-NLS-1$
				image += ".png"; //$NON-NLS-1$
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));

			} else
				dlg.lM01Icon.setIcon(null);
		}
*/	}

	public void saveSign() {
/*		Node node = getNode();
		if (node == null) {
			return;
		}

		String shape = ""; //$NON-NLS-1$

		switch (getStyleIndex()) {
		case CARD_PILLAR:
			super.saveSign("buoy_cardinal"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_cardinal:shape", "pillar")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case CARD_SPAR:
			super.saveSign("buoy_cardinal"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_cardinal:shape", "spar")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case CARD_BEACON:
			super.saveSign("beacon_cardinal"); //$NON-NLS-1$
			break;
		case CARD_TOWER:
			super.saveSign("beacon_cardinal"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_cardinal:shape", "tower")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case CARD_FLOAT:
			super.saveSign("light_float"); //$NON-NLS-1$
			break;
		default:
		}

		switch (getStyleIndex()) {
		case CARD_PILLAR:
		case CARD_SPAR:
			switch (getMarkIndex()) {
			case SeaMark.CARD_NORTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "north")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "black;yellow")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones up"; //$NON-NLS-1$
				break;

			case SeaMark.CARD_EAST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "east")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "black;yellow;black")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones base together"; //$NON-NLS-1$
				break;

			case SeaMark.CARD_SOUTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "south")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "yellow;black")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones down"; //$NON-NLS-1$
				break;

			case SeaMark.CARD_WEST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "west")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "yellow;black;yellow")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones point together"; //$NON-NLS-1$
				break;
			}
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_cardinal:colour_pattern", "horizontal stripes")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case CARD_BEACON:
		case CARD_TOWER:
			switch (getMarkIndex()) {
			case SeaMark.CARD_NORTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "north")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "black;yellow")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones up"; //$NON-NLS-1$
				break;

			case SeaMark.CARD_EAST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "east")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "black;yellow;black")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones base together"; //$NON-NLS-1$
				break;

			case SeaMark.CARD_SOUTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "south")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "yellow;black")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones down"; //$NON-NLS-1$
				break;

			case SeaMark.CARD_WEST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "west")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "yellow;black;yellow")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones point together"; //$NON-NLS-1$
				break;
			}
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_cardinal:colour_pattern", "horizontal stripes")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case CARD_FLOAT:
			switch (getMarkIndex()) {
			case SeaMark.CARD_NORTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour", "black;yellow")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones up"; //$NON-NLS-1$
				break;

			case SeaMark.CARD_EAST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour", "black;yellow;black")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones base together"; //$NON-NLS-1$
				break;

			case SeaMark.CARD_SOUTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour", "yellow;black")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones down"; //$NON-NLS-1$
				break;

			case SeaMark.CARD_WEST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour", "yellow;black;yellow")); //$NON-NLS-1$ //$NON-NLS-2$
				shape = "2 cones point together"; //$NON-NLS-1$
				break;
			}
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour_pattern", "horizontal stripes")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		}
		saveTopMarkData(shape, "black"); //$NON-NLS-1$
		saveLightData(); //$NON-NLS-1$
		saveRadarFogData();
*/	}
	
}
