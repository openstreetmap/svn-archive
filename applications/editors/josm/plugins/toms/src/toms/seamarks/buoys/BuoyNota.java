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

public class BuoyNota extends Buoy {
	public BuoyNota(SmpDialogAction dia, int type) {
		super(dia);

		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem("Not set");
		dlg.cbM01Kennung.addItem("Fl");
		dlg.cbM01Kennung.addItem("Fl(2)");
		dlg.cbM01Kennung.addItem("Fl(3)");
		dlg.cbM01Kennung.addItem("Fl(4)");
		dlg.cbM01Kennung.addItem("Fl(5)");
		dlg.cbM01Kennung.addItem("Oc(2)");
		dlg.cbM01Kennung.addItem("Oc(3)");
		dlg.cbM01Kennung.addItem("Q");
		dlg.cbM01Kennung.addItem("IQ");
		dlg.cbM01Kennung.setSelectedIndex(0);

		dlg.cbM01Colour.removeAllItems();
		dlg.cbM01Colour.addItem("Not set");
		dlg.cbM01Colour.addItem("R");
		dlg.cbM01Colour.addItem("G");
		dlg.cbM01Colour.addItem("W");
		dlg.cbM01Colour.setEnabled(true);

		setBuoyIndex(type);
		setStyleIndex(0);
		setLightColour();

		setFired(true);
		setTopMark(false);
		refreshStyles();
		paintSign();
	}

	public void refreshStyles() {
		int type = getBuoyIndex();
		int style = getStyleIndex();

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem("Not set");
		dlg.cbM01StyleOfMark.addItem("Lighthouse");
		dlg.cbM01StyleOfMark.addItem("Major Light");
		dlg.cbM01StyleOfMark.addItem("Minor Light");
		dlg.cbM01StyleOfMark.addItem("Light Float");

		if (style >= dlg.cbM01StyleOfMark.getItemCount())
			style = 0;
		setStyleIndex(style);
		dlg.cbM01StyleOfMark.setSelectedIndex(style);
	}

	public void paintSign() {
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		dlg.tfM01Name.setEnabled(true);
		dlg.tfM01Name.setText(getName());
		dlg.cM01Fired.setSelected(true);
		dlg.cM01Fired.setEnabled(false);
		dlg.cM01TopMark.setEnabled(false);
		dlg.cM01TopMark.setSelected(false);


		switch (getStyleIndex()) {
		case SeaMark.LIGHT_HOUSE:
		case SeaMark.LIGHT_MAJOR:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Light_Major.png")));
			break;

		case SeaMark.LIGHT_MINOR:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Light_Minor.png")));
			break;

		case SeaMark.LIGHT_FLOAT:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Major_Float.png")));
			break;

		default:
		}

		if (getLightColour() == "W")
			dlg.lM01Icon03.setIcon(new ImageIcon(getClass().getResource(
					"/images/Light_White.png")));
		else if (getLightColour() == "R")
			dlg.lM01Icon03.setIcon(new ImageIcon(getClass().getResource(
					"/images/Light_Red.png")));
		else if (getLightColour() == "G")
			dlg.lM01Icon03.setIcon(new ImageIcon(getClass().getResource(
					"/images/Light_Green.png")));

	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		switch (getStyleIndex()) {
		case LIGHT_HOUSE:
			super.saveSign("lighthouse");
			break;
		case LIGHT_MAJOR:
			super.saveSign("major_light");
			break;
		case LIGHT_MINOR:
			super.saveSign("minor_light");
			break;
		case LIGHT_FLOAT:
			super.saveSign("major_floating_light");
			break;
		default:
		}

		saveLightData("white");
	}

	public void setLightColour() {
	}

	public boolean parseTopMark(Node node) {
		return false;
	}

	public boolean parseLight(Node node) {
		String str;
		boolean ret = true;
		Map<String, String> keys;

		setFired(true);

		keys = node.getKeys();

		if (keys.containsKey("seamark:light:colour")) {
			str = keys.get("seamark:light:colour");

			if (keys.containsKey("seamark:light:character")) {
				setLightGroup(keys);

				String c = keys.get("seamark:light:character");
				String ce = c;
				setLightChar(c);
				setLightPeriod(keys);
			}

			setLightColour(str);

		}

		return ret;
	}

}
