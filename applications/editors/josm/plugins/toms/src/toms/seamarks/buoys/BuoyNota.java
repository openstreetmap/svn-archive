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

		refreshLights();
		setLightColour();

		setTopMark(false);
		setFired(true);
	}

	public boolean isValid() {
		return (getBuoyIndex() > 0);
	}

	public void paintSign() {
		if (dlg.paintlock)
			return;
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		if (isValid()) {
			dlg.cM01Radar.setVisible(true);
			dlg.cM01Racon.setVisible(true);
			dlg.cM01Fog.setVisible(true);

			dlg.cM01Fired.setVisible(true);
			dlg.cM01Fired.setEnabled(false);
			dlg.cM01Fired.setSelected(true);
			dlg.rbM01Fired1.setVisible(true);
			dlg.rbM01FiredN.setVisible(true);
			dlg.lM01Kennung.setVisible(true);
			dlg.cbM01Kennung.setVisible(true);
			dlg.lM01Height.setVisible(true);
			dlg.tfM01Height.setVisible(true);
			dlg.lM01Range.setVisible(true);
			dlg.tfM01Range.setVisible(true);
			dlg.lM01Group.setVisible(true);
			dlg.tfM01Group.setVisible(true);
			dlg.lM01RepeatTime.setVisible(true);
			dlg.tfM01RepeatTime.setVisible(true);
			dlg.lM01Colour.setVisible(true);
			dlg.cbM01Colour.setVisible(true);
			if (isSectored()) {
				dlg.lM01Sector.setVisible(true);
				dlg.cbM01Sector.setVisible(true);
				dlg.lM01Bearing.setVisible(true);
				dlg.tfM01Bearing.setVisible(true);
				dlg.tfM02Bearing.setVisible(true);
				dlg.tfM01Radius.setVisible(true);
			} else {
				dlg.lM01Sector.setVisible(false);
				dlg.cbM01Sector.setVisible(false);
				dlg.lM01Bearing.setVisible(false);
				dlg.tfM01Bearing.setVisible(false);
				dlg.tfM02Bearing.setVisible(false);
				dlg.tfM01Radius.setVisible(false);
			}

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
