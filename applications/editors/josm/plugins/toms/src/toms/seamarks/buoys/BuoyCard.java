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

	public BuoyCard(SmpDialogAction dia, int type) {
		super(dia);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("Not set");
		dlg.cbM01StyleOfMark.addItem("Pillar Buoy");
		dlg.cbM01StyleOfMark.addItem("Spar Buoy");
		dlg.cbM01StyleOfMark.addItem("Beacon");
		dlg.cbM01StyleOfMark.addItem("Tower");
		dlg.cbM01StyleOfMark.addItem("Float");

		dlg.cM01Fired.setSelected(false);
		dlg.cM01TopMark.setSelected(false);
		dlg.tbM01Region.setEnabled(false);

		setBuoyIndex(type);
		setStyleIndex(0);
		setLightColour("W");

		switch (type) {
		case CARD_NORTH:
			setColour(SeaMark.BLACK_YELLOW);
			break;
		case CARD_EAST:
			setColour(SeaMark.BLACK_YELLOW_BLACK);
			break;
		case CARD_SOUTH:
			setColour(SeaMark.YELLOW_BLACK);
			break;
		case CARD_WEST:
			setColour(SeaMark.YELLOW_BLACK_YELLOW);
			break;
		default:
		}

		refreshLights();
		paintSign();
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

	public void paintSign() {
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		dlg.tfM01Name.setEnabled(true);
		dlg.tfM01Name.setText(getName());

		dlg.cM01TopMark.setSelected(true);
		dlg.cM01TopMark.setEnabled(false);
		dlg.cM01Fired.setEnabled(true);

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

		if (image != "") {
			if (isFired()) {
				image += "_Lit";

				if (getLightChar() != "") {
					String tmp = null;
					String c;
					int i1;

					c = getLightChar();
					if (c.contains("+")) {
						i1 = c.indexOf("+");
						tmp = c.substring(i1, c.length());
						c = c.substring(0, i1);
					}

					if (getLightGroup() != "")
						c = c + "(" + getLightGroup() + ")";
					if (tmp != null)
						c = c + tmp;

					dlg.cbM01Kennung.setSelectedItem(c);
					if (dlg.cbM01Kennung.getSelectedItem().equals("Not set"))
						c = "";
				}
			}

			image += ".png";

			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(image)));
		} else
			dlg.lM01Icon01.setIcon(null);
	}

	public boolean parseLight(Node node) {
		String str;
		boolean ret = true;
		Map<String, String> keys;

		setFired(false);

		keys = node.getKeys();
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

			if (str.compareTo("white") == 0) {
				setFired(true);
				setLightColour("W");

			} else {
				if (getErrMsg() == null)
					setErrMsg("Parse-Error: Licht falsch");
				else
					setErrMsg(getErrMsg() + " / Licht falsch");

				ret = false;
			}
		}

		return ret;
	}

	public void setLightColour() {
		super.setLightColour("W");
	}

	public boolean parseShape(Node node) {
		String str;
		boolean ret = true;
		Map<String, String> keys;

		keys = node.getKeys();

		if (keys.containsKey("seamark:buoy_cardinal:shape")) {
			str = keys.get("seamark:buoy_cardinal:shape");

			if (str.compareTo("pillar") == 0)
				setStyleIndex(CARD_PILLAR);
			else if (str.compareTo("spar") == 0)
				setStyleIndex(CARD_SPAR);
			else
				ret = false;
		} else if (keys.containsKey("seamark:beacon_cardinal:colour")) {
			if (keys.containsKey("seamark:beacon_cardinal:shape")) {
				str = keys.get("seamark:beacon_cardinal:shape");

				if (str.compareTo("tower") == 0)
					setStyleIndex(CARD_TOWER);
				else
					setStyleIndex(CARD_BEACON);
			} else
				setStyleIndex(CARD_BEACON);
		} else if ((keys.containsKey("seamark:type") == true)
				&& (keys.get("seamark:type").equals("light_float"))) {
			setStyleIndex(CARD_FLOAT);
		}
		return ret;
	}

	public boolean parseTopMark(Node node) {
		return false;
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

	}
}
