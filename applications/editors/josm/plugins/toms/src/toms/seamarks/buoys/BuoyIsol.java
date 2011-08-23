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

public class BuoyIsol extends Buoy {
	public BuoyIsol(SmpDialogAction dia, Node node) {
		super(dia);

		String str;
		Map<String, String> keys;
		keys = node.getKeys();
		setNode(node);

		resetMask();
		dlg.cbM01TypeOfMark.setSelectedIndex(ISOLATED_DANGER);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem(Messages.getString("SmpDialogAction.212")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.setVisible(true);
		dlg.lM01StyleOfMark.setVisible(true);

		setBuoyIndex(ISOLATED_DANGER);
		setColour(SeaMark.BLACK_RED_BLACK);
		setLightColour("W"); //$NON-NLS-1$
		setTopMark(true);
		setRegion(Main.pref.get("tomsplugin.IALA").equals("B")); //$NON-NLS-1$ //$NON-NLS-2$

		if (keys.containsKey("name")) //$NON-NLS-1$
			setName(keys.get("name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:name")) //$NON-NLS-1$
			setName(keys.get("seamark:name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:longname")) //$NON-NLS-1$
			setLongname(keys.get("seamark:longname")); //$NON-NLS-1$

		if (keys.containsKey("seamark:fixme")) //$NON-NLS-1$
			setFixme(keys.get("seamark:fixme")); //$NON-NLS-1$

		if (keys.containsKey("seamark:buoy_isolated_danger:name")) //$NON-NLS-1$
			setName(keys.get("seamark:buoy_isolated_danger:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:beacon_isolated_danger:name")) //$NON-NLS-1$
			setName(keys.get("seamark:beacon_isolated_danger:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:light_float:name")) //$NON-NLS-1$
			setName(keys.get("seamark:light_float:name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:buoy_isolated_danger:shape")) { //$NON-NLS-1$
			str = keys.get("seamark:buoy_isolated_danger:shape"); //$NON-NLS-1$

			if (str.equals("pillar")) //$NON-NLS-1$
				setStyleIndex(ISOL_PILLAR);
			else if (str.equals("spar")) //$NON-NLS-1$
				setStyleIndex(ISOL_SPAR);
		} else if (keys.containsKey("seamark:beacon_isolated_danger:shape")) { //$NON-NLS-1$
				str = keys.get("seamark:beacon_isolated_danger:shape"); //$NON-NLS-1$

				if (str.equals("tower")) //$NON-NLS-1$
					setStyleIndex(ISOL_TOWER);
				else 
					setStyleIndex(ISOL_BEACON);
		} else if (keys.containsKey("seamark:type") //$NON-NLS-1$
				&& (keys.get("seamark:type").equals("light_float"))) { //$NON-NLS-1$ //$NON-NLS-2$
			setStyleIndex(ISOL_FLOAT);
		}

		if (getStyleIndex() >= dlg.cbM01StyleOfMark.getItemCount())
			setStyleIndex(0);
		dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());

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
		dlg.cbM01Kennung.addItem("Fl(2)"); //$NON-NLS-1$
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
			dlg.cM01TopMark.setVisible(true);
			dlg.cM01Radar.setVisible(true);
			dlg.cM01Racon.setVisible(true);
			dlg.cM01Fog.setVisible(true);
			dlg.cM01Fired.setVisible(true);
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
				case ISOL_BEACON:
				case ISOL_TOWER:
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
			case ISOL_PILLAR:
				image += "_Pillar_Single"; //$NON-NLS-1$
				break;
			case ISOL_SPAR:
				image += "_Spar_Single"; //$NON-NLS-1$
				break;
			case ISOL_BEACON:
				image += "_Beacon_Single"; //$NON-NLS-1$
				break;
			case ISOL_TOWER:
				image += "_Tower_Single"; //$NON-NLS-1$
				break;
			case ISOL_FLOAT:
				image += "_Float_Single"; //$NON-NLS-1$
				break;
			default:
			}

			if (!image.equals("/images/Cardinal")) { //$NON-NLS-1$
				image += ".png"; //$NON-NLS-1$
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));
			} else
				dlg.lM01Icon.setIcon(null);
		} else {
			dlg.tfM01Name.setEnabled(false);
			dlg.tfM01Name.setText(""); //$NON-NLS-1$
			dlg.cM01TopMark.setVisible(false);
			dlg.cM01Radar.setVisible(false);
			dlg.cM01Racon.setVisible(false);
			dlg.cM01Fog.setVisible(false);
			dlg.cM01Fired.setVisible(false);
		}
	}

	public void saveSign() {
		Node node = getNode();

		if (node == null) {
			return;
		}

		switch (getStyleIndex()) {
		case ISOL_PILLAR:
			super.saveSign("buoy_isolated_danger"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "pillar")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case ISOL_SPAR:
			super.saveSign("buoy_isolated_danger"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:shape", "spar")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case ISOL_BEACON:
			super.saveSign("beacon_isolated_danger"); //$NON-NLS-1$
			break;
		case ISOL_TOWER:
			super.saveSign("beacon_isolated_danger"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:shape", "tower")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case ISOL_FLOAT:
			super.saveSign("light_float"); //$NON-NLS-1$
			break;
		default:
		}

		switch (getStyleIndex()) {
		case ISOL_PILLAR:
		case ISOL_SPAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:colour_pattern", "horizontal")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_isolated_danger:colour", "black;red;black")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case ISOL_BEACON:
		case ISOL_TOWER:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:colour_pattern", //$NON-NLS-1$
					"horizontal")); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_isolated_danger:colour", "black;red;black")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case ISOL_FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour_pattern", "horizontal")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour", "black;red;black")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		}

		saveTopMarkData("2 spheres", "black"); //$NON-NLS-1$ //$NON-NLS-2$
		saveLightData(); //$NON-NLS-1$
		saveRadarFogData();
	}
}
