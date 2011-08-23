//License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig & Malcolm Herring

package toms.seamarks.buoys;

import java.util.Map;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Node;

import toms.Messages;
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
		dlg.cbM01TypeOfMark.setSelectedIndex(CARDINAL);

		dlg.cbM01CatOfMark.removeAllItems();
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.157")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.158")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.159")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.160")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.161")); //$NON-NLS-1$

		dlg.cbM01CatOfMark.setEnabled(true);
		dlg.cbM01CatOfMark.setVisible(true);
		dlg.lM01CatOfMark.setVisible(true);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem(Messages.getString("SmpDialogAction.212")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.setVisible(true);
		dlg.lM01StyleOfMark.setVisible(true);

		setRegion(Main.pref.get("tomsplugin.IALA").equals("B")); //$NON-NLS-1$ //$NON-NLS-2$
		if (keys.containsKey("name")) //$NON-NLS-1$
			setName(keys.get("name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:name")) //$NON-NLS-1$
			setName(keys.get("seamark:name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:longname")) //$NON-NLS-1$
			setLongname(keys.get("seamark:longname")); //$NON-NLS-1$

		if (keys.containsKey("seamark:fixme")) //$NON-NLS-1$
			setFixme(keys.get("seamark:fixme")); //$NON-NLS-1$

		if (keys.containsKey("seamark:buoy_cardinal:name")) //$NON-NLS-1$
			setName(keys.get("seamark:buoy_cardinal:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:beacon_cardinal:name")) //$NON-NLS-1$
			setName(keys.get("seamark:beacon_cardinal:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:light_float:name")) //$NON-NLS-1$
			setName(keys.get("seamark:light_float:name")); //$NON-NLS-1$

		String cat = ""; //$NON-NLS-1$
		String col = ""; //$NON-NLS-1$

		if (keys.containsKey("seamark:buoy_cardinal:category")) //$NON-NLS-1$
			cat = keys.get("seamark:buoy_cardinal:category"); //$NON-NLS-1$
		else if (keys.containsKey("seamark:beacon_cardinal:category")) //$NON-NLS-1$
			cat = keys.get("seamark:beacon_cardinal:category"); //$NON-NLS-1$

		if (keys.containsKey("seamark:buoy_cardinal:colour")) //$NON-NLS-1$
			col = keys.get("seamark:buoy_cardinal:colour"); //$NON-NLS-1$
		else if (keys.containsKey("seamark:beacon_cardinal:colour")) //$NON-NLS-1$
			col = keys.get("seamark:beacon_cardinal:colour"); //$NON-NLS-1$
		else if (keys.containsKey("seamark:light_float:colour")) //$NON-NLS-1$
			col = keys.get("seamark:light_float:colour"); //$NON-NLS-1$

		if (cat.isEmpty()) { //$NON-NLS-1$
			if (col.equals("black;yellow")) { //$NON-NLS-1$
				setBuoyIndex(CARD_NORTH);
				setColour(BLACK_YELLOW);
			} else if (col.equals("black;yellow;black")) { //$NON-NLS-1$
				setBuoyIndex(CARD_EAST);
				setColour(BLACK_YELLOW_BLACK);
			} else if (col.equals("yellow;black")) { //$NON-NLS-1$
				setBuoyIndex(CARD_SOUTH);
				setColour(YELLOW_BLACK);
			} else if (col.equals("yellow;black;yellow")) { //$NON-NLS-1$
				setBuoyIndex(CARD_WEST);
				setColour(YELLOW_BLACK_YELLOW);
			}
		} else if (cat.equals("north")) { //$NON-NLS-1$
			setBuoyIndex(CARD_NORTH);
			setColour(BLACK_YELLOW);
		} else if (cat.equals("east")) { //$NON-NLS-1$
			setBuoyIndex(CARD_EAST);
			setColour(BLACK_YELLOW_BLACK);
		} else if (cat.equals("south")) { //$NON-NLS-1$
			setBuoyIndex(CARD_SOUTH);
			setColour(YELLOW_BLACK);
		} else if (cat.equals("west")) { //$NON-NLS-1$
			setBuoyIndex(CARD_WEST);
			setColour(YELLOW_BLACK_YELLOW);
		}

		if (keys.containsKey("seamark:buoy_cardinal:shape")) { //$NON-NLS-1$
			str = keys.get("seamark:buoy_cardinal:shape"); //$NON-NLS-1$

			if (str.equals("pillar")) //$NON-NLS-1$
				setStyleIndex(CARD_PILLAR);
			else if (str.equals("spar")) //$NON-NLS-1$
				setStyleIndex(CARD_SPAR);
		} else if (keys.containsKey("seamark:beacon_cardinal:colour")) { //$NON-NLS-1$
			if (keys.containsKey("seamark:beacon_cardinal:shape")) { //$NON-NLS-1$
				str = keys.get("seamark:beacon_cardinal:shape"); //$NON-NLS-1$

				if (str.equals("tower")) //$NON-NLS-1$
					setStyleIndex(CARD_TOWER);
				else
					setStyleIndex(CARD_BEACON);
			} else
				setStyleIndex(CARD_BEACON);
		} else if (keys.containsKey("seamark:type") //$NON-NLS-1$
				&& (keys.get("seamark:type").equals("light_float"))) { //$NON-NLS-1$ //$NON-NLS-2$
			setStyleIndex(CARD_FLOAT);
		}

		if (getStyleIndex() >= dlg.cbM01StyleOfMark.getItemCount())
			setStyleIndex(0);

		refreshLights();
		parseLights(keys);
		parseFogRadar(keys);

		dlg.cbM01CatOfMark.setSelectedIndex(getBuoyIndex());
		dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
		dlg.tfM01Name.setText(getName());
		dlg.cM01TopMark.setSelected(hasTopMark());
	}

		public void refreshLights() {
		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem(Messages.getString("SmpDialogAction.212")); //$NON-NLS-1$
		dlg.cbM01Kennung.setSelectedIndex(0);

		switch (getBuoyIndex()) {
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
		return (getBuoyIndex() > 0) && (getStyleIndex() > 0);
	}

	public void setLightColour() {
		super.setLightColour("W"); //$NON-NLS-1$
	}

	public void paintSign() {
		if (dlg.paintlock)
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

			switch (getBuoyIndex()) {
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
	}

	public void saveSign() {
		Node node = getNode();
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
			switch (getBuoyIndex()) {
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
					"seamark:buoy_cardinal:colour_pattern", "horizontal")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case CARD_BEACON:
		case CARD_TOWER:
			switch (getBuoyIndex()) {
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
					"seamark:beacon_cardinal:colour_pattern", "horizontal")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case CARD_FLOAT:
			switch (getBuoyIndex()) {
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
					"seamark:light_float:colour_pattern", "horizontal")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		}
		saveTopMarkData(shape, "black"); //$NON-NLS-1$
		saveLightData(); //$NON-NLS-1$
		saveRadarFogData();
	}
}
