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
		dlg.cbM01Kennung.addItem(Messages.getString("SmpDialogAction.212")); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Fl"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Fl()"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Oc()"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Q"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("IQ"); //$NON-NLS-1$
		dlg.cbM01Kennung.setSelectedIndex(0);

		setLightColour();

		setTopMark(false);
		paintSign();
	}

	public boolean isValid() {
		return (getBuoyIndex() > 0);
	}

	public void paintSign() {
		if (dlg.paintlock)
			return;
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		if (getBuoyIndex() > 0) {
			dlg.cM01Radar.setEnabled(true);
			dlg.cM01Radar.setVisible(true);
			dlg.cM01Racon.setEnabled(true);
			dlg.cM01Racon.setVisible(true);

			dlg.cM01Fog.setEnabled(true);
			dlg.cM01Fog.setVisible(true);

			setFired(true);
			dlg.cM01Fired.setVisible(true);
			dlg.cM01Fired.setEnabled(false);
			dlg.cM01Fired.setSelected(true);

			switch (getBuoyIndex()) {
			case SeaMark.LIGHT_HOUSE:
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(
						"/images/Light_House.png"))); //$NON-NLS-1$
				break;

			case SeaMark.LIGHT_MAJOR:
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(
						"/images/Light_Major.png"))); //$NON-NLS-1$
				break;

			case SeaMark.LIGHT_MINOR:
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(
						"/images/Light_Minor.png"))); //$NON-NLS-1$
				break;

			case SeaMark.LIGHT_VESSEL:
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(
						"/images/Major_Float.png"))); //$NON-NLS-1$
				break;

			default:
			}
		}
	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		switch (getStyleIndex()) {
		case LIGHT_HOUSE:
			super.saveSign("lighthouse"); //$NON-NLS-1$
			break;
		case LIGHT_MAJOR:
			super.saveSign("major_light"); //$NON-NLS-1$
			break;
		case LIGHT_MINOR:
			super.saveSign("minor_light"); //$NON-NLS-1$
			break;
		default:
		}
		saveLightData("white"); //$NON-NLS-1$
		saveRadarFogData();
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

		if (keys.containsKey("seamark:light:colour")) { //$NON-NLS-1$
			str = keys.get("seamark:light:colour"); //$NON-NLS-1$

			if (keys.containsKey("seamark:light:character")) { //$NON-NLS-1$
				setLightGroup(keys);

				String c = keys.get("seamark:light:character"); //$NON-NLS-1$
				if (getLightGroup() != "") //$NON-NLS-1$
					c = c + "(" + getLightGroup() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				setLightChar(c);
				setLightPeriod(keys);
			}

			setLightColour(str);

		}

		return ret;
	}

}
