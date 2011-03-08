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

		Map<String, String> keys;
		keys = node.getKeys();
		setNode(node);

		resetMask();

		dlg.cbM01TypeOfMark.setSelectedIndex(LIGHT);

		dlg.cbM01CatOfMark.setEnabled(true);
		dlg.cbM01CatOfMark.setVisible(true);
		dlg.lM01CatOfMark.setVisible(true);

		dlg.cbM01CatOfMark.removeAllItems();
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.157")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.206")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.207")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.208")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.209")); //$NON-NLS-1$

		setRegion(Main.pref.get("tomsplugin.IALA").equals("B")); //$NON-NLS-1$ //$NON-NLS-2$

		if (keys.containsKey("name")) //$NON-NLS-1$
			setName(keys.get("name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:name")) //$NON-NLS-1$
			setName(keys.get("seamark:name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:longname")) //$NON-NLS-1$
			setLongname(keys.get("seamark:longname")); //$NON-NLS-1$

		if (keys.containsKey("seamark:fixme")) //$NON-NLS-1$
			setFixme(keys.get("seamark:fixme")); //$NON-NLS-1$

		if (keys.containsKey("seamark:landmark:height")) //$NON-NLS-1$
			setLMheight(keys.get("seamark:landmark:height")); //$NON-NLS-1$

		if (keys.containsKey("seamark:landmark:name")) //$NON-NLS-1$
			setName(keys.get("seamark:landmark:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:light_major:name")) //$NON-NLS-1$
			setName(keys.get("seamark:light_major:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:light_minor:name")) //$NON-NLS-1$
			setName(keys.get("seamark:light_minor:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:light_vessel:name")) //$NON-NLS-1$
			setName(keys.get("seamark:light_vessel:name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:type")) { //$NON-NLS-1$
			String type = keys.get("seamark:type"); //$NON-NLS-1$
			if (type.equals("landmark"))
				setBuoyIndex(LIGHT_HOUSE);
			else if (type.equals("light_major"))
				setBuoyIndex(LIGHT_MAJOR);
			else if (type.equals("light_minor"))
				setBuoyIndex(LIGHT_MINOR);
			else if (type.equals("light_vessel"))
				setBuoyIndex(LIGHT_VESSEL);
		}

		refreshLights();
		parseLights(keys);
		parseFogRadar(keys);
		setTopMark(false);
		setFired(true);

		dlg.cbM01CatOfMark.setSelectedIndex(getBuoyIndex());
		dlg.tfM01Name.setText(getName());
		dlg.cM01Fired.setEnabled(false);
		dlg.cM01Fired.setSelected(true);
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

			dlg.rbM01Fired1.setVisible(true);
			dlg.rbM01FiredN.setVisible(true);
			dlg.lM01Height.setVisible(true);
			dlg.tfM01Height.setVisible(true);
			dlg.lM01Range.setVisible(true);
			dlg.tfM01Range.setVisible(true);

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

		switch (getBuoyIndex()) {
		case LIGHT_HOUSE:
			super.saveSign("landmark"); //$NON-NLS-1$
			break;
		case LIGHT_MAJOR:
			super.saveSign("light_major"); //$NON-NLS-1$
			break;
		case LIGHT_MINOR:
			super.saveSign("light_minor"); //$NON-NLS-1$
			break;
		case LIGHT_VESSEL:
			super.saveSign("light_vessel"); //$NON-NLS-1$
			break;
		default:
		}
		if (!getLMheight().isEmpty())
			Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:landmark:height", getLMheight()));
		saveLightData(); //$NON-NLS-1$
		saveRadarFogData();
	}

	public void setLightColour() {
	}

}
