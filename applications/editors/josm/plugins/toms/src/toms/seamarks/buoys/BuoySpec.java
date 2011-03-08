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

public class BuoySpec extends Buoy {
	public BuoySpec(SmpDialogAction dia, Node node) {
		super(dia);

		String str;
		Map<String, String> keys;
		keys = node.getKeys();
		setNode(node);

		resetMask();
		dlg.cbM01TypeOfMark.setSelectedIndex(SPECIAL_PURPOSE);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem(Messages.getString("SmpDialogAction.212")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.02")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.03")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.08")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.09")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.setVisible(true);
		dlg.lM01StyleOfMark.setVisible(true);

		dlg.cbM01TopMark.removeAllItems();
		dlg.cbM01TopMark.addItem(Messages.getString("SmpDialogAction.212"));
		dlg.cbM01TopMark.addItem(Messages.getString("SmpDialogAction.210")); //$NON-NLS-1$
		dlg.cbM01TopMark.addItem(Messages.getString("SmpDialogAction.211")); //$NON-NLS-1$
		dlg.cbM01TopMark.addItem(Messages.getString("SmpDialogAction.214")); //$NON-NLS-1$
		dlg.cbM01TopMark.addItem(Messages.getString("SmpDialogAction.215")); //$NON-NLS-1$

		dlg.cM01TopMark.setEnabled(true);

		setBuoyIndex(SPECIAL_PURPOSE);
		setColour(SeaMark.YELLOW);
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

		if (keys.containsKey("seamark:buoy_special_purpose:name")) //$NON-NLS-1$
			setName(keys.get("seamark:buoy_special_purpose:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:beacon_special_purpose:name")) //$NON-NLS-1$
			setName(keys.get("seamark:beacon_special_purpose:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:light_float:name")) //$NON-NLS-1$
			setName(keys.get("seamark:light_float:name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:buoy_special_purpose:shape")) { //$NON-NLS-1$
			str = keys.get("seamark:buoy_special_purpose:shape"); //$NON-NLS-1$

			if (str.equals("pillar")) //$NON-NLS-1$
				setStyleIndex(SPEC_PILLAR);
			else if (str.equals("can")) //$NON-NLS-1$
				setStyleIndex(SPEC_CAN);
			else if (str.equals("conical")) //$NON-NLS-1$
				setStyleIndex(SPEC_CONE);
			else if (str.equals("spar")) //$NON-NLS-1$
				setStyleIndex(SPEC_SPAR);
			else if (str.equals("sphere")) //$NON-NLS-1$
				setStyleIndex(SPEC_SPHERE);
			else if (str.equals("barrel")) //$NON-NLS-1$
				setStyleIndex(SPEC_BARREL);
		}

		if (keys.containsKey("seamark:beacon_special_purpose:shape")) { //$NON-NLS-1$
			str = keys.get("seamark:beacon_special_purpose:shape"); //$NON-NLS-1$
			if (str.equals("tower")) //$NON-NLS-1$
				setStyleIndex(SPEC_TOWER);
			else
				setStyleIndex(SPEC_BEACON);
		}

		if (keys.containsKey("seamark:light_float:colour")) {
			setStyleIndex(SPEC_FLOAT);
		}

		if ((keys.containsKey("seamark:type") && keys.get("seamark:type").equals( //$NON-NLS-1$ //$NON-NLS-2$
				"beacon_special_purpose")) //$NON-NLS-1$
				|| keys.containsKey("seamark:beacon_special_purpose:colour") //$NON-NLS-1$
				|| keys.containsKey("seamark:beacon_special_purpose:shape")) { //$NON-NLS-1$
			if (keys.containsKey("seamark:beacon_special_purpose:shape") //$NON-NLS-1$
					&& keys.get("seamark:beacon_special_purpose:shape").equals("tower")) //$NON-NLS-1$ //$NON-NLS-2$
				setStyleIndex(SPEC_TOWER);
			else
				setStyleIndex(SPEC_BEACON);
		} else if (keys.containsKey("seamark:light_float:colour") //$NON-NLS-1$
				&& keys.get("seamark:light_float:colour").equals("yellow")) //$NON-NLS-1$ //$NON-NLS-2$
			setStyleIndex(SPEC_FLOAT);

		if (getStyleIndex() >= dlg.cbM01StyleOfMark.getItemCount())
			setStyleIndex(0);

		keys = node.getKeys();
		if (keys.containsKey("seamark:topmark:shape")) { //$NON-NLS-1$
			str = keys.get("seamark:topmark:shape"); //$NON-NLS-1$
			setTopMark(true);
			if (str.equals("x-shape")) { //$NON-NLS-1$
				if (keys.containsKey("seamark:topmark:colour")) { //$NON-NLS-1$
					if (keys.get("seamark:topmark:colour").equals("red"))
						setTopMarkIndex(TOP_RED_X);
					else
						setTopMarkIndex(TOP_YELLOW_X);
				}
			} else if (str.equals("cone, point up")) { //$NON-NLS-1$
					setTopMarkIndex(TOP_YELLOW_CONE);
			} else if (str.equals("cylinder")) { //$NON-NLS-1$
				setTopMarkIndex(TOP_YELLOW_CAN);
			}
		}

		refreshLights();
		parseLights(keys);
		parseFogRadar(keys);

		dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
		dlg.tfM01Name.setText(getName());
		dlg.cM01TopMark.setSelected(hasTopMark());
	}

	public void refreshLights() {
		super.refreshLights();

		switch (getStyleIndex()) {
		case SPEC_BARREL:
			dlg.cM01Fired.setSelected(false);
			dlg.cM01Fired.setEnabled(false);
			dlg.cM01TopMark.setEnabled(true);
			break;
		default:
			dlg.cM01Fired.setEnabled(true);
			dlg.cM01TopMark.setEnabled(true);
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
			dlg.cM01Radar.setVisible(true);
			dlg.cM01Racon.setVisible(true);
			dlg.cM01TopMark.setEnabled(true);
			dlg.cM01TopMark.setVisible(true);
			if (hasTopMark()) {
				dlg.cbM01TopMark.setEnabled(true);
				dlg.cbM01TopMark.setVisible(true);
			} else {
				dlg.cbM01TopMark.setVisible(false);
			}
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
				case SPEC_FLOAT:
				case SPEC_BEACON:
				case SPEC_TOWER:
					dlg.lM01Height.setVisible(true);
					dlg.tfM01Height.setVisible(true);
					dlg.lM01Range.setVisible(true);
					dlg.tfM01Range.setVisible(true);
					break;
				default:
				}
			}

			String image = "/images/Special_Purpose"; //$NON-NLS-1$

			switch (getStyleIndex()) {
			case SPEC_PILLAR:
				image += "_Pillar"; //$NON-NLS-1$
				break;
			case SPEC_CAN:
				image += "_Can"; //$NON-NLS-1$
				break;
			case SPEC_CONE:
				image += "_Cone"; //$NON-NLS-1$
				break;
			case SPEC_SPAR:
				image += "_Spar"; //$NON-NLS-1$
				break;
			case SPEC_SPHERE:
				image += "_Sphere"; //$NON-NLS-1$
				break;
			case SPEC_BARREL:
				image += "_Barrel"; //$NON-NLS-1$
				break;
			case SPEC_FLOAT:
				image += "_Float"; //$NON-NLS-1$
				break;
			case SPEC_BEACON:
				image += "_Beacon"; //$NON-NLS-1$
				break;
			case SPEC_TOWER:
				image += "_Tower"; //$NON-NLS-1$
				break;
			default:
			}

			if (!image.equals("/images/Special_Purpose")) { //$NON-NLS-1$
				image += ".png"; //$NON-NLS-1$
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));
				if (hasTopMark()) {
					image = "";
					switch (getStyleIndex()) {
					case SPEC_PILLAR:
					case SPEC_SPAR:
						switch (getTopMarkIndex()) {
						case TOP_YELLOW_X:
							image = "/images/Top_X_Yellow_Buoy.png"; //$NON-NLS-1$
							break;
						case TOP_RED_X:
							image = "/images/Top_X_Red_Buoy.png"; //$NON-NLS-1$
							break;
						case TOP_YELLOW_CAN:
							image = "/images/Top_Can_Yellow_Buoy.png"; //$NON-NLS-1$
							break;
						case TOP_YELLOW_CONE:
							image = "/images/Top_Cone_Yellow_Buoy.png"; //$NON-NLS-1$
							break;
						}
						break;
					case SPEC_CAN:
					case SPEC_CONE:
					case SPEC_SPHERE:
					case SPEC_BARREL:
						switch (getTopMarkIndex()) {
						case TOP_YELLOW_X:
							image = "/images/Top_X_Yellow_Buoy_Small.png"; //$NON-NLS-1$
							break;
						case TOP_RED_X:
							image = "/images/Top_X_Red_Buoy_Small.png"; //$NON-NLS-1$
							break;
						case TOP_YELLOW_CAN:
							image = "/images/Top_Can_Yellow_Buoy_Small.png"; //$NON-NLS-1$
							break;
						case TOP_YELLOW_CONE:
							image = "/images/Top_Cone_Yellow_Buoy_Small.png"; //$NON-NLS-1$
							break;
						}
						break;
					case SPEC_BEACON:
					case SPEC_TOWER:
						switch (getTopMarkIndex()) {
						case TOP_YELLOW_X:
							image = "/images/Top_X_Yellow_Beacon.png"; //$NON-NLS-1$
							break;
						case TOP_RED_X:
							image = "/images/Top_X_Red_Beacon.png"; //$NON-NLS-1$
							break;
						case TOP_YELLOW_CAN:
							image = "/images/Top_Can_Yellow_Beacon.png"; //$NON-NLS-1$
							break;
						case TOP_YELLOW_CONE:
							image = "/images/Top_Cone_Yellow_Beacon.png"; //$NON-NLS-1$
							break;
						}
						break;
					case SPEC_FLOAT:
						switch (getTopMarkIndex()) {
						case TOP_YELLOW_X:
							image = "/images/Top_X_Yellow_Float.png"; //$NON-NLS-1$
							break;
						case TOP_RED_X:
							image = "/images/Top_X_Red_Float.png"; //$NON-NLS-1$
							break;
						case TOP_YELLOW_CAN:
							image = "/images/Top_Can_Yellow_Float.png"; //$NON-NLS-1$
							break;
						case TOP_YELLOW_CONE:
							image = "/images/Top_Cone_Yellow_Float.png"; //$NON-NLS-1$
							break;
						}
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
		case SPEC_PILLAR:
			super.saveSign("buoy_special_purpose"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "pillar")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SPEC_SPAR:
			super.saveSign("buoy_special_purpose"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "spar")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SPEC_CAN:
			super.saveSign("buoy_special_purpose"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "can")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SPEC_CONE:
			super.saveSign("buoy_special_purpose"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "conical")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SPEC_SPHERE:
			super.saveSign("buoy_special_purpose"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "sphere")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SPEC_BARREL:
			super.saveSign("buoy_special_purpose"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:shape", "barrel")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_special_purpose:colour", "yellow")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SPEC_FLOAT:
			super.saveSign("light_float"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:colour", "yellow")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SPEC_BEACON:
			super.saveSign("beacon_special_purpose"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_special_purpose:colour", "yellow")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case SPEC_TOWER:
			super.saveSign("beacon_special_purpose"); //$NON-NLS-1$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_special_purpose:shape", "tower")); //$NON-NLS-1$ //$NON-NLS-2$
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_special_purpose:colour", "yellow")); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		default:
		}
		switch (getTopMarkIndex()) {
		case TOP_YELLOW_X:
			saveTopMarkData("x-shape", "yellow"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case TOP_RED_X:
			saveTopMarkData("x-shape", "red"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case TOP_YELLOW_CAN:
			saveTopMarkData("cylinder", "yellow"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case TOP_YELLOW_CONE:
			saveTopMarkData("cone, point up", "yellow"); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		}
		saveLightData(); //$NON-NLS-1$
		saveRadarFogData();
	}
}
