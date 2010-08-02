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

		dlg.cbM01Colour.removeAllItems();
		dlg.cbM01Colour.addItem("W");
		dlg.cbM01Colour.setSelectedIndex(0);
		dlg.cbM01Colour.setEnabled(false);
		dlg.cM01Fired.setSelected(false);
		dlg.cM01TopMark.setSelected(false);

		setStyleIndex(0);
		setBuoyIndex(type);
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

		switch (getBuoyIndex()) {
		case SeaMark.CARD_NORTH:
			switch (getStyleIndex()) {
			case CARD_PILLAR:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Pillar_North.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/North_Top_Buoy.png")));
				break;
			case CARD_SPAR:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Spar_North.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/North_Top_Buoy.png")));
				break;
			case CARD_BEACON:
				if (isFired())
					dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
							"/images/Cardinal_Beacon_Lit_North.png")));
				else
					dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
							"/images/Cardinal_Beacon_North.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/North_Top_Post.png")));
				break;
			case CARD_TOWER:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Tower_North.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/North_Top_Post.png")));
				break;
			case CARD_FLOAT:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Float_North.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/North_Top_Post.png")));
				break;
			default:
			}
			break;

		case SeaMark.CARD_EAST:
			switch (getStyleIndex()) {
			case CARD_PILLAR:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Pillar_East.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/East_Top_Buoy.png")));
				break;
			case CARD_SPAR:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Spar_East.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/East_Top_Buoy.png")));
				break;
			case CARD_BEACON:
				if (isFired())
					dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
							"/images/Cardinal_Beacon_Lit_East.png")));
				else
					dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
							"/images/Cardinal_Beacon_East.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/East_Top_Post.png")));
				break;
			case CARD_TOWER:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Tower_East.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/East_Top_Post.png")));
				break;
			case CARD_FLOAT:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Float_East.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/East_Top_Post.png")));
				break;
			default:
			}
			break;

		case SeaMark.CARD_SOUTH:
			switch (getStyleIndex()) {
			case CARD_PILLAR:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Pillar_South.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/South_Top_Buoy.png")));
				break;
			case CARD_SPAR:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Spar_South.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/South_Top_Buoy.png")));
				break;
			case CARD_BEACON:
				if (isFired())
					dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
							"/images/Cardinal_Beacon_Lit_South.png")));
				else
					dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
							"/images/Cardinal_Beacon_South.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/South_Top_Post.png")));
				break;
			case CARD_TOWER:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Tower_South.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/South_Top_Post.png")));
				break;
			case CARD_FLOAT:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Float_South.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/South_Top_Post.png")));
				break;
			default:
			}
			break;

		case SeaMark.CARD_WEST:
			switch (getStyleIndex()) {
			case CARD_PILLAR:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Pillar_West.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/West_Top_Buoy.png")));
				break;
			case CARD_SPAR:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Spar_West.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/West_Top_Buoy.png")));
				break;
			case CARD_BEACON:
				if (isFired())
					dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
							"/images/Cardinal_Beacon_Lit_West.png")));
				else
					dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
							"/images/Cardinal_Beacon_Lit_West.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/West_Top_Post.png")));
				break;
			case CARD_TOWER:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Tower_West.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/West_Top_Post.png")));
				break;
			case CARD_FLOAT:
				dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
						"/images/Cardinal_Float_West.png")));
				dlg.lM01Icon02.setIcon(new ImageIcon(getClass().getResource(
						"/images/West_Top_Post.png")));
				break;
			default:
			}
			break;

		default:
		}

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
			if (dlg.cbM01Kennung.getSelectedItem() == "Not set")
				c = "";
		}

		Checker(null, "/images/Light_White.png");
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
				String ce = c;

				if (c.contains("+")) {
					i1 = c.indexOf("+");
					tmp = c.substring(i1, c.length());
					c = c.substring(0, i1);
				}

				if (getLightGroup() != "")
					ce = c + "(" + getLightGroup() + ")";
				if (tmp != null) {
					c = c + tmp;
					ce = ce + tmp;
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
			else if (str.compareTo("float") == 0)
				setStyleIndex(CARD_FLOAT);
			else
				ret = false;
		}
		else if (keys.containsKey("seamark:beacon_cardinal:shape")) {
			str = keys.get("seamark:beacon_cardinal:shape");

			if (str.compareTo("stake") == 0)
				setStyleIndex(CARD_BEACON);
			else if (str.compareTo("tower") == 0)
				setStyleIndex(CARD_TOWER);
			else
				ret = false;
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
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_cardinal:shape", "stake"));
			break;
		case CARD_TOWER:
			super.saveSign("beacon_cardinal");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_cardinal:shape", "tower"));
			break;
		case CARD_FLOAT:
			super.saveSign("buoy_cardinal");
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_cardinal:shape", "float"));
			break;
		default:
		}
		Main.main.undoRedo.add(new ChangePropertyCommand(node,
				"seamark:buoy_cardinal:colour_pattern", "horizontal stripes"));

		switch (getStyleIndex()) {
		case CARD_PILLAR:
		case CARD_SPAR:
		case CARD_FLOAT:
			switch (getBuoyIndex()) {
			case SeaMark.CARD_NORTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "north"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "black;yellow"));
				break;

			case SeaMark.CARD_EAST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "east"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "black;yellow;black"));
				break;

			case SeaMark.CARD_SOUTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "south"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "yellow;black"));
				break;

			case SeaMark.CARD_WEST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:category", "west"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_cardinal:colour", "yellow;black;yellow"));
				break;
			}
		case CARD_BEACON:
		case CARD_TOWER:
			switch (getBuoyIndex()) {
			case SeaMark.CARD_NORTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "north"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "black;yellow"));
				break;

			case SeaMark.CARD_EAST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "east"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "black;yellow;black"));
				break;

			case SeaMark.CARD_SOUTH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "south"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "yellow;black"));
				break;

			case SeaMark.CARD_WEST:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:category", "west"));
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_cardinal:colour", "yellow;black;yellow"));
				break;
			}
		}

		saveLightData("white");

	}
}
