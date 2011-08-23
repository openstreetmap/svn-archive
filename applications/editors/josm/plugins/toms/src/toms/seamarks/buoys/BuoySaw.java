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

public class BuoySaw extends Buoy {
	public BuoySaw(SmpDialogAction dia, Node node) {
		super(dia);

		String str;
		Map<String, String> keys;
		keys = node.getKeys();
		setNode(node);

		resetMask();
		dlg.cbM01TypeOfMark.setSelectedIndex(SAFE_WATER);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem(Messages.getString("SmpDialogAction.212")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.08")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.setVisible(true);
		dlg.lM01StyleOfMark.setVisible(true);

		setBuoyIndex(SAFE_WATER);
		setColour(SeaMark.RED_WHITE);
		setLightColour("W"); //$NON-NLS-1$
		setRegion(Main.pref.get("tomsplugin.IALA").equals("B")); //$NON-NLS-1$ //$NON-NLS-2$

		if (keys.containsKey("name")) //$NON-NLS-1$
			setName(keys.get("name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:name")) //$NON-NLS-1$
			setName(keys.get("seamark:name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:longname")) //$NON-NLS-1$
			setLongname(keys.get("seamark:longname")); //$NON-NLS-1$

		if (keys.containsKey("seamark:fixme")) //$NON-NLS-1$
			setFixme(keys.get("seamark:fixme")); //$NON-NLS-1$

		if (keys.containsKey("seamark:buoy_safe_water:name")) //$NON-NLS-1$
			setName(keys.get("seamark:buoy_safe_water:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:beacon_safe_water:name")) //$NON-NLS-1$
			setName(keys.get("seamark:beacon_safe_water:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:light_float:name")) //$NON-NLS-1$
			setName(keys.get("seamark:light_float:name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:buoy_safe_water:shape")) { //$NON-NLS-1$
			str = keys.get("seamark:buoy_safe_water:shape"); //$NON-NLS-1$

			if (str.equals("pillar")) //$NON-NLS-1$
				setStyleIndex(SAFE_PILLAR);
			else if (str.equals("spar")) //$NON-NLS-1$
				setStyleIndex(SAFE_SPAR);
			else if (str.equals("spherical")) //$NON-NLS-1$
				setStyleIndex(SAFE_SPHERE);
		} else if ((keys.containsKey("seamark:type")) //$NON-NLS-1$
				&& (keys.get("seamark:type").equals("light_float"))) { //$NON-NLS-1$ //$NON-NLS-2$
			setStyleIndex(SAFE_FLOAT);
		} else if ((keys.containsKey("seamark:type")) //$NON-NLS-1$
				&& (keys.get("seamark:type").equals("beacon_safe_water"))) { //$NON-NLS-1$ //$NON-NLS-2$
			setStyleIndex(SAFE_BEACON);
		}

		if (getStyleIndex() >= dlg.cbM01StyleOfMark.getItemCount())
			setStyleIndex(0);

		if (keys.containsKey("seamark:topmark:shape") //$NON-NLS-1$
				|| keys.containsKey("seamark:topmark:colour")) { //$NON-NLS-1$
			setTopMark(true);
		}

		refreshLights();
		parseLights(keys);
		parseFogRadar(keys);

		dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
		dlg.tfM01Name.setText(getName());
		dlg.cM01TopMark.setSelected(hasTopMark());
	}

	public void refreshLights() {
		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem(Messages.getString("SmpDialogAction.212")); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Iso"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Oc"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("LFl"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Mo"); //$NON-NLS-1$
		dlg.cbM01Kennung.setSelectedIndex(0);
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
			dlg.cM01TopMark.setEnabled(true);
			dlg.cM01TopMark.setVisible(true);
			dlg.cM01Radar.setVisible(true);
			dlg.cM01Racon.setVisible(true);
			dlg.cM01Fog.setVisible(true);
			dlg.cM01Fired.setVisible(true);
			dlg.cM01Fired.setEnabled(true);
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
				case SAFE_FLOAT:
				case SAFE_BEACON:
					dlg.lM01Height.setVisible(true);
					dlg.tfM01Height.setVisible(true);
					dlg.lM01Range.setVisible(true);
					dlg.tfM01Range.setVisible(true);
					break;
				default:
				}
			}

			String image = "/images/Safe_Water"; //$NON-NLS-1$

			switch (getStyleIndex()) {
			case SAFE_PILLAR:
				image += "_Pillar"; //$NON-NLS-1$
				break;
			case SAFE_SPAR:
				image += "_Spar"; //$NON-NLS-1$
				break;
			case SAFE_SPHERE:
				image += "_Sphere"; //$NON-NLS-1$
				break;
			case SAFE_BEACON:
				image += "_Beacon"; //$NON-NLS-1$
				break;
			case SAFE_FLOAT:
				image += "_Float"; //$NON-NLS-1$
				break;
			default:
			}

			if (!image.equals("/images/Safe_Water")) { //$NON-NLS-1$
				image += ".png"; //$NON-NLS-1$
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));
				if (hasTopMark()) {
					image = "";
					switch (getStyleIndex()) {
					case SAFE_PILLAR:
					case SAFE_SPAR:
						image = "/images/Top_Sphere_Red_Buoy.png"; //$NON-NLS-1$
						break;
					case SAFE_SPHERE:
						image = "/images/Top_Sphere_Red_Buoy_Small.png"; //$NON-NLS-1$
						break;
					case SAFE_BEACON:
						image = "/images/Top_Sphere_Red_Beacon.png"; //$NON-NLS-1$
						break;
					case SAFE_FLOAT:
						image = "/images/Top_Sphere_Red_Float.png"; //$NON-NLS-1$
						break;
					}
					if (!image.isEmpty())
						dlg.lM06Icon.setIcon(new ImageIcon(getClass().getResource(image)));
				}
			} else
				dlg.lM01Icon.setIcon(null);
		}
	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		switch (getStyleIndex()) {
		case SAFE_PILLAR:
			super.saveSign("buoy_safe_water"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:shape", "pillar")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SAFE_SPAR:
			super.saveSign("buoy_safe_water"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:shape", "spar")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SAFE_SPHERE:
			super.saveSign("buoy_safe_water"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:shape", "spherical")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SAFE_BEACON:
			super.saveSign("beacon_safe_water"); //$NON-NLS-1$
			break;
		case SAFE_FLOAT:
			super.saveSign("light_float"); //$NON-NLS-1$
			break;
		default:
		}

		switch (getStyleIndex()) {
		case SAFE_PILLAR:
		case SAFE_SPAR:
		case SAFE_SPHERE:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:colour_pattern", "vertical")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_safe_water:colour", "red;white")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SAFE_BEACON:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_safe_water:colour_pattern", "vertical")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_safe_water:colour", "red;white")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SAFE_FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour_pattern", "vertical")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour", "red;white")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		default:
		}
		saveTopMarkData("sphere", "red"); //$NON-NLS-1$ //$NON-NLS-2$
		saveLightData(); //$NON-NLS-1$
		saveRadarFogData();
	}
}
