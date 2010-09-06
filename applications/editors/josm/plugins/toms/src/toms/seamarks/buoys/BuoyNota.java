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
	public BuoyNota(SmpDialogAction dia, Node node) {
		super(dia);
		
		String str;
		Map<String, String> keys;
		keys = node.getKeys();
		setNode(node);
		
		resetMask();

		dlg.cbM01CatOfMark.setEnabled(true);
		dlg.cbM01CatOfMark.setVisible(true);
		dlg.lM01CatOfMark.setVisible(true);

		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem("Not set");
		dlg.cbM01Kennung.addItem("Fl");
		dlg.cbM01Kennung.addItem("Fl()");
		dlg.cbM01Kennung.addItem("Oc()");
		dlg.cbM01Kennung.addItem("Q");
		dlg.cbM01Kennung.addItem("IQ");
		dlg.cbM01Kennung.setSelectedIndex(0);

		setLightColour();

		setFired(true);
		setTopMark(false);
		paintSign();
	}

	public void paintSign() {
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		dlg.tfM01Name.setEnabled(true);
		dlg.tfM01Name.setText(getName());
		dlg.cM01Fired.setSelected(true);
		dlg.cM01Fired.setEnabled(false);
		dlg.cM01TopMark.setVisible(false);
		dlg.cM01TopMark.setSelected(false);

		switch (getStyleIndex()) {
		case SeaMark.LIGHT_HOUSE:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Light_House.png")));
			break;

		case SeaMark.LIGHT_MAJOR:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Light_Major.png")));
			break;

		case SeaMark.LIGHT_MINOR:
			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
					"/images/Light_Minor.png")));
			break;

		case SeaMark.LIGHT_FLOAT:
//			dlg.lM01Icon01.setIcon(new ImageIcon(getClass().getResource(
//					"/images/Light_Float.png")));
			break;

		default:
		}

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
				if (getLightGroup() != "")
					c = c + "(" + getLightGroup() + ")";
				setLightChar(c);
				setLightPeriod(keys);
			}

			setLightColour(str);

		}

		return ret;
	}

}
