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

public class BuoyLat extends Buoy {
	public BuoyLat(SmpDialogAction dia, Node node) {
		super(dia);

		String str;
		Map<String, String> keys;
		keys = node.getKeys();
		setNode(node);

		resetMask();
		dlg.cbM01TypeOfMark.setSelectedIndex(LATERAL);

		dlg.cbM01CatOfMark.removeAllItems();
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.152")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.153")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.154")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.155")); //$NON-NLS-1$
		dlg.cbM01CatOfMark.addItem(Messages.getString("SmpDialogAction.156")); //$NON-NLS-1$

		dlg.rbM01RegionA.setEnabled(true);
		dlg.rbM01RegionB.setEnabled(true);
		dlg.cbM01CatOfMark.setEnabled(true);
		dlg.cbM01CatOfMark.setVisible(true);
		dlg.lM01CatOfMark.setVisible(true);

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem(Messages.getString("SmpDialogAction.212")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07")); //$NON-NLS-1$
		dlg.cbM01StyleOfMark.setEnabled(true);

		if (keys.containsKey("name")) //$NON-NLS-1$
			setName(keys.get("name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:name")) //$NON-NLS-1$
			setName(keys.get("seamark:name")); //$NON-NLS-1$

		if (keys.containsKey("seamark:longname")) //$NON-NLS-1$
			setLongname(keys.get("seamark:longname")); //$NON-NLS-1$

		if (keys.containsKey("seamark:fixme")) //$NON-NLS-1$
			setFixme(keys.get("seamark:fixme")); //$NON-NLS-1$

		if (keys.containsKey("seamark:buoy_lateral:name")) //$NON-NLS-1$
			setName(keys.get("seamark:buoy_lateral:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:beacon_lateral:name")) //$NON-NLS-1$
			setName(keys.get("seamark:beacon_lateral:name")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:light_float:name")) //$NON-NLS-1$
			setName(keys.get("seamark:light_float:name")); //$NON-NLS-1$

		String cat = ""; //$NON-NLS-1$
		String col = ""; //$NON-NLS-1$
		String top = ""; //$NON-NLS-1$

		if (keys.containsKey("seamark:buoy_lateral:category")) //$NON-NLS-1$
			cat = keys.get("seamark:buoy_lateral:category"); //$NON-NLS-1$
		else if (keys.containsKey("seamark:beacon_lateral:category")) //$NON-NLS-1$
			cat = keys.get("seamark:beacon_lateral:category"); //$NON-NLS-1$

		if (keys.containsKey("seamark:buoy_lateral:colour")) //$NON-NLS-1$
			col = keys.get("seamark:buoy_lateral:colour"); //$NON-NLS-1$
		else if (keys.containsKey("seamark:beacon_lateral:colour")) //$NON-NLS-1$
			col = keys.get("seamark:beacon_lateral:colour"); //$NON-NLS-1$
		else if (keys.containsKey("seamark:light_float:colour")) //$NON-NLS-1$
			col = keys.get("seamark:light_float:colour"); //$NON-NLS-1$

		if (getStyleIndex() != LAT_PERCH) {
			if (keys.containsKey("seamark:topmark:shape")) { //$NON-NLS-1$
				top = keys.get("seamark:topmark:shape"); //$NON-NLS-1$
				setTopMark(true);
			}
			if (keys.containsKey("seamark:topmark:colour")) { //$NON-NLS-1$
				if (col.isEmpty()) col = keys.get("seamark:topmark:colour");
				setTopMark(true);
			}
		}
		
		if (col.isEmpty()) {
			if (keys.containsKey("seamark:light:colour")) //$NON-NLS-1$
				col = keys.get("seamark:light:colour"); //$NON-NLS-1$
		}

		if (cat.isEmpty()) { //$NON-NLS-1$
			if (col.equals("red")) { //$NON-NLS-1$
				setColour(RED);
				if (top.equals("cylinder")) { //$NON-NLS-1$
					setBuoyIndex(PORT_HAND);
					setRegion(IALA_A);
				} else if (top.equals("cone, point up")) { //$NON-NLS-1$
					setBuoyIndex(STARBOARD_HAND);
					setRegion(IALA_B);
				} else {
					if (getRegion() == IALA_A)
						setBuoyIndex(PORT_HAND);
					else
						setBuoyIndex(STARBOARD_HAND);
				}
			} else if (col.equals("green")) { //$NON-NLS-1$
				setColour(GREEN);
				if (top.equals("cone, point up")) { //$NON-NLS-1$
					setBuoyIndex(STARBOARD_HAND);
					setRegion(IALA_A);
				} else if (top.equals("cylinder")) { //$NON-NLS-1$
					setBuoyIndex(PORT_HAND);
					setRegion(IALA_B);
				} else {
					if (getRegion() == IALA_A)
						setBuoyIndex(STARBOARD_HAND);
					else
						setBuoyIndex(PORT_HAND);
				}
			} else if (col.equals("red;green;red")) { //$NON-NLS-1$
				setColour(RED_GREEN_RED);
				if (top.equals("cylinder")) { //$NON-NLS-1$
					setBuoyIndex(PREF_PORT_HAND);
					setRegion(IALA_A);
				} else if (top.equals("cone, point up")) { //$NON-NLS-1$
					setBuoyIndex(PREF_STARBOARD_HAND);
					setRegion(IALA_B);
				} else {
					if (getRegion() == IALA_A)
						setBuoyIndex(PREF_PORT_HAND);
					else
						setBuoyIndex(PREF_STARBOARD_HAND);
				}
			} else if (col.equals("green;red;green")) { //$NON-NLS-1$
				setColour(GREEN_RED_GREEN);
				if (top.equals("cone, point up")) { //$NON-NLS-1$
					setBuoyIndex(PREF_STARBOARD_HAND);
					setRegion(IALA_A);
				} else if (top.equals("cylinder")) { //$NON-NLS-1$
					setBuoyIndex(PREF_PORT_HAND);
					setRegion(IALA_B);
				} else {
					if (getRegion() == IALA_A)
						setBuoyIndex(PREF_STARBOARD_HAND);
					else
						setBuoyIndex(PREF_PORT_HAND);
				}
			}
		} else if (cat.equals("port")) { //$NON-NLS-1$

			setBuoyIndex(PORT_HAND);

			if (col.equals("red")) { //$NON-NLS-1$
				setRegion(IALA_A);
				setColour(RED);
			} else if (col.equals("green")) { //$NON-NLS-1$
				setRegion(IALA_B);
				setColour(GREEN);
			} else {
				if (getRegion() == IALA_A)
					setColour(RED);
				else
					setColour(GREEN);
			}
		} else if (cat.equals("starboard")) { //$NON-NLS-1$

			setBuoyIndex(STARBOARD_HAND);

			if (col.equals("green")) { //$NON-NLS-1$
				setRegion(IALA_A);
				setColour(GREEN);
			} else if (col.equals("red")) { //$NON-NLS-1$
				setRegion(IALA_B);
				setColour(RED);
			} else {
				if (getRegion() == IALA_A)
					setColour(GREEN);
				else
					setColour(RED);
			}
		} else if (cat.equals("preferred_channel_port")) { //$NON-NLS-1$

			setBuoyIndex(PREF_PORT_HAND);

			if (col.equals("red;green;red")) { //$NON-NLS-1$
				setRegion(IALA_A);
				setColour(RED_GREEN_RED);
			} else if (col.equals("green;red;green")) { //$NON-NLS-1$
				setRegion(IALA_B);
				setColour(GREEN_RED_GREEN);
			} else {
				if (getRegion() == IALA_A)
					setColour(RED_GREEN_RED);
				else
					setColour(GREEN_RED_GREEN);
			}

		} else if (cat.equals("preferred_channel_starboard")) { //$NON-NLS-1$

			setBuoyIndex(PREF_STARBOARD_HAND);

			if (col.equals("green;red;green")) { //$NON-NLS-1$
				setRegion(IALA_A);
				setColour(GREEN_RED_GREEN);
			} else if (col.equals("red;green;red")) { //$NON-NLS-1$
				setRegion(IALA_B);
				setColour(RED_GREEN_RED);
			} else {
				if (getRegion() == IALA_A)
					setColour(GREEN_RED_GREEN);
				else
					setColour(RED_GREEN_RED);
			}
		}

		if (keys.containsKey("seamark:buoy_lateral:shape")) { //$NON-NLS-1$
			str = keys.get("seamark:buoy_lateral:shape"); //$NON-NLS-1$

			switch (getBuoyIndex()) {
			case PORT_HAND:
				if (str.equals("can")) //$NON-NLS-1$
					setStyleIndex(LAT_CAN);
				else if (str.equals("pillar")) //$NON-NLS-1$
					setStyleIndex(LAT_PILLAR);
				else if (str.equals("spar")) //$NON-NLS-1$
					setStyleIndex(LAT_SPAR);
				break;

			case PREF_PORT_HAND:
				if (str.equals("can")) //$NON-NLS-1$
					setStyleIndex(LAT_CAN);
				else if (str.equals("pillar")) //$NON-NLS-1$
					setStyleIndex(LAT_PILLAR);
				else if (str.equals("spar")) //$NON-NLS-1$
					setStyleIndex(LAT_SPAR);
				break;

			case STARBOARD_HAND:
				if (str.equals("conical")) //$NON-NLS-1$
					setStyleIndex(LAT_CONE);
				else if (str.equals("pillar")) //$NON-NLS-1$
					setStyleIndex(LAT_PILLAR);
				else if (str.equals("spar")) //$NON-NLS-1$
					setStyleIndex(LAT_SPAR);
				break;

			case PREF_STARBOARD_HAND:
				if (str.equals("conical")) //$NON-NLS-1$
					setStyleIndex(LAT_CONE);
				else if (str.equals("pillar")) //$NON-NLS-1$
					setStyleIndex(LAT_PILLAR);
				else if (str.equals("spar")) //$NON-NLS-1$
					setStyleIndex(LAT_SPAR);
				break;
			}
		} else if (keys.containsKey("seamark:beacon_lateral:shape")) { //$NON-NLS-1$
			str = keys.get("seamark:beacon_lateral:shape"); //$NON-NLS-1$
			if (str.equals("tower")) //$NON-NLS-1$
				setStyleIndex(LAT_TOWER);
			else if (str.equals("perch")) //$NON-NLS-1$
				setStyleIndex(LAT_PERCH);
			else
				setStyleIndex(LAT_BEACON);
		} else if (keys.containsKey("seamark:type") //$NON-NLS-1$
				&& (keys.get("seamark:type").equals("beacon_lateral"))) { //$NON-NLS-1$ //$NON-NLS-2$
			setStyleIndex(LAT_BEACON);
		} else if (keys.containsKey("seamark:type") //$NON-NLS-1$
				&& (keys.get("seamark:type").equals("light_float"))) { //$NON-NLS-1$ //$NON-NLS-2$
			setStyleIndex(LAT_FLOAT);
		}

		if (keys.containsKey("seamark:buoy_lateral:system")) //$NON-NLS-1$
			setRegion(keys.get("seamark:buoy_lateral:system").equals("iala-b")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:beacon_lateral:system")) //$NON-NLS-1$
			setRegion(keys.get("seamark:beacon_lateral:system").equals("iala-b")); //$NON-NLS-1$
		else if (keys.containsKey("seamark:light_float:system")) //$NON-NLS-1$
			setRegion(keys.get("seamark:light_float:system").equals("iala-b")); //$NON-NLS-1$

		refreshStyles();
		refreshLights();
		parseLights(keys);
		parseFogRadar(keys);
		setLightColour();

		dlg.cbM01CatOfMark.setSelectedIndex(getBuoyIndex());
		dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
		dlg.tfM01Name.setText(getName());
		dlg.cM01TopMark.setSelected(hasTopMark());
	}

	public void refreshStyles() {
		int type = getBuoyIndex();
		int style = getStyleIndex();

		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.addItem(Messages.getString("SmpDialogAction.213")); //$NON-NLS-1$

		switch (type) {
		case PORT_HAND:
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.02")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.10")); //$NON-NLS-1$
			break;
		case STARBOARD_HAND:
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.03")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.10")); //$NON-NLS-1$
			break;
		case PREF_PORT_HAND:
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.02")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07")); //$NON-NLS-1$
			break;
		case PREF_STARBOARD_HAND:
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.03")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.01")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.04")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.05")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.06")); //$NON-NLS-1$
			dlg.cbM01StyleOfMark.addItem(Messages.getString("Buoy.07")); //$NON-NLS-1$
			break;
		}

		if (style >= dlg.cbM01StyleOfMark.getItemCount())
			style = 0;
		setStyleIndex(style);
		dlg.cbM01StyleOfMark.setSelectedIndex(style);
		dlg.cbM01StyleOfMark.setVisible(true);
		dlg.lM01StyleOfMark.setVisible(true);
	}

	public void refreshLights() {
		super.refreshLights();
	}

	public boolean isValid() {
		return (getBuoyIndex() > 0) && (getStyleIndex() > 0);
	}

	public void setLightColour() {
		if (getRegion() == IALA_A) {
			if (getBuoyIndex() == PORT_HAND || getBuoyIndex() == PREF_PORT_HAND)
				super.setLightColour("R"); //$NON-NLS-1$
			else
				super.setLightColour("G"); //$NON-NLS-1$
		} else {
			if (getBuoyIndex() == PORT_HAND || getBuoyIndex() == PREF_PORT_HAND)
				super.setLightColour("G"); //$NON-NLS-1$
			else
				super.setLightColour("R"); //$NON-NLS-1$
		}
	}

	public void paintSign() {
		if (dlg.paintlock)
			return;
		super.paintSign();

		dlg.sM01StatusBar.setText(getErrMsg());

		if (isValid()) {
			dlg.tfM01Name.setEnabled(true);
			dlg.tfM01Name.setText(getName());

			boolean region = getRegion();
			int style = getStyleIndex();

			if (style == LAT_PERCH) {
				dlg.cM01TopMark.setVisible(false);
				dlg.cM01TopMark.setSelected(false);
				dlg.cM01Radar.setVisible(false);
				dlg.cM01Racon.setVisible(false);
				dlg.cM01Fog.setVisible(false);
				dlg.cM01Fired.setVisible(false);
				dlg.cM01Fired.setSelected(false);
			} else {
				dlg.cM01TopMark.setEnabled(true);
				dlg.cM01TopMark.setVisible(true);
				dlg.cM01Radar.setVisible(true);
				dlg.cM01Racon.setVisible(true);
				dlg.cM01Fog.setVisible(true);
				dlg.cM01Fired.setVisible(true);
				dlg.cM01Fired.setEnabled(true);
				dlg.cM01TopMark.setEnabled(true);
			}
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
				switch (style) {
				case LAT_BEACON:
				case LAT_TOWER:
				case LAT_FLOAT:
					dlg.lM01Height.setVisible(true);
					dlg.tfM01Height.setVisible(true);
					dlg.lM01Range.setVisible(true);
					dlg.tfM01Range.setVisible(true);
					break;
				default:
				}
			}

			String image = "/images/Lateral"; //$NON-NLS-1$

			switch (getBuoyIndex()) {
			case PORT_HAND:
				if (region == IALA_A)
					switch (style) {
					case LAT_CAN:
						image += "_Can_Red"; //$NON-NLS-1$
						break;
					case LAT_PILLAR:
						image += "_Pillar_Red"; //$NON-NLS-1$
						break;
					case LAT_SPAR:
						image += "_Spar_Red"; //$NON-NLS-1$
						break;
					case LAT_BEACON:
						image += "_Beacon_Red"; //$NON-NLS-1$
						break;
					case LAT_TOWER:
						image += "_Tower_Red"; //$NON-NLS-1$
						break;
					case LAT_FLOAT:
						image += "_Float_Red"; //$NON-NLS-1$
						break;
					case LAT_PERCH:
						image += "_Perch_Port"; //$NON-NLS-1$
						break;
					default:
					}
				else
					switch (style) {
					case LAT_CAN:
						image += "_Can_Green"; //$NON-NLS-1$
						break;
					case LAT_PILLAR:
						image += "_Pillar_Green"; //$NON-NLS-1$
						break;
					case LAT_SPAR:
						image += "_Spar_Green"; //$NON-NLS-1$
						break;
					case LAT_BEACON:
						image += "_Beacon_Green"; //$NON-NLS-1$
						break;
					case LAT_TOWER:
						image += "_Tower_Green"; //$NON-NLS-1$
						break;
					case LAT_FLOAT:
						image += "_Float_Green"; //$NON-NLS-1$
						break;
					case LAT_PERCH:
						image += "_Perch_Port"; //$NON-NLS-1$
						break;
					default:
					}
				break;

			case STARBOARD_HAND:
				if (region == IALA_A)
					switch (style) {
					case LAT_CONE:
						image += "_Cone_Green"; //$NON-NLS-1$
						break;
					case LAT_PILLAR:
						image += "_Pillar_Green"; //$NON-NLS-1$
						break;
					case LAT_SPAR:
						image += "_Spar_Green"; //$NON-NLS-1$
						break;
					case LAT_BEACON:
						image += "_Beacon_Green"; //$NON-NLS-1$
						break;
					case LAT_TOWER:
						image += "_Tower_Green"; //$NON-NLS-1$
						break;
					case LAT_FLOAT:
						image += "_Float_Green"; //$NON-NLS-1$
						break;
					case LAT_PERCH:
						image += "_Perch_Starboard"; //$NON-NLS-1$
						break;
					default:
					}
				else
					switch (style) {
					case LAT_CONE:
						image += "_Cone_Red"; //$NON-NLS-1$
						break;
					case LAT_PILLAR:
						image += "_Pillar_Red"; //$NON-NLS-1$
						break;
					case LAT_SPAR:
						image += "_Spar_Red"; //$NON-NLS-1$
						break;
					case LAT_BEACON:
						image += "_Beacon_Red"; //$NON-NLS-1$
						break;
					case LAT_TOWER:
						image += "_Tower_Red"; //$NON-NLS-1$
						break;
					case LAT_FLOAT:
						image += "_Float_Red"; //$NON-NLS-1$
						break;
					case LAT_PERCH:
						image += "_Perch_Starboard"; //$NON-NLS-1$
						break;
					default:
					}
				break;

			case PREF_PORT_HAND:
				if (region == IALA_A)
					switch (style) {
					case LAT_CAN:
						image += "_Can_Red_Green_Red"; //$NON-NLS-1$
						break;
					case LAT_PILLAR:
						image += "_Pillar_Red_Green_Red"; //$NON-NLS-1$
						break;
					case LAT_SPAR:
						image += "_Spar_Red_Green_Red"; //$NON-NLS-1$
						break;
					case LAT_BEACON:
						image += "_Beacon_Red_Green_Red"; //$NON-NLS-1$
						break;
					case LAT_TOWER:
						image += "_Tower_Red_Green_Red"; //$NON-NLS-1$
						break;
					case LAT_FLOAT:
						image += "_Float_Red_Green_Red"; //$NON-NLS-1$
						break;
					default:
					}
				else
					switch (style) {
					case LAT_CAN:
						image += "_Can_Green_Red_Green"; //$NON-NLS-1$
						break;
					case LAT_PILLAR:
						image += "_Pillar_Green_Red_Green"; //$NON-NLS-1$
						break;
					case LAT_SPAR:
						image += "_Spar_Green_Red_Green"; //$NON-NLS-1$
						break;
					case LAT_BEACON:
						image += "_Beacon_Green_Red_Green"; //$NON-NLS-1$
						break;
					case LAT_TOWER:
						image += "_Tower_Green_Red_Green"; //$NON-NLS-1$
						break;
					case LAT_FLOAT:
						image += "_Float_Green_Red_Green"; //$NON-NLS-1$
						break;
					default:
					}
				break;

			case PREF_STARBOARD_HAND:
				if (region == IALA_A)
					switch (style) {
					case LAT_CONE:
						image += "_Cone_Green_Red_Green"; //$NON-NLS-1$
						break;
					case LAT_PILLAR:
						image += "_Pillar_Green_Red_Green"; //$NON-NLS-1$
						break;
					case LAT_SPAR:
						image += "_Spar_Green_Red_Green"; //$NON-NLS-1$
						break;
					case LAT_BEACON:
						image += "_Beacon_Green_Red_Green"; //$NON-NLS-1$
						break;
					case LAT_TOWER:
						image += "_Tower_Green_Red_Green"; //$NON-NLS-1$
						break;
					case LAT_FLOAT:
						image += "_Float_Green_Red_Green"; //$NON-NLS-1$
						break;
					default:
					}
				else
					switch (style) {
					case LAT_CONE:
						image += "_Cone_Red_Green_Red"; //$NON-NLS-1$
						break;
					case LAT_PILLAR:
						image += "_Pillar_Red_Green_Red"; //$NON-NLS-1$
						break;
					case LAT_SPAR:
						image += "_Spar_Red_Green_Red"; //$NON-NLS-1$
						break;
					case LAT_BEACON:
						image += "_Beacon_Red_Green_Red"; //$NON-NLS-1$
						break;
					case LAT_TOWER:
						image += "_Tower_Red_Green_Red"; //$NON-NLS-1$
						break;
					case LAT_FLOAT:
						image += "_Float_Red_Green_Red"; //$NON-NLS-1$
						break;
					default:
					}
				break;

			default:
			}

			if (!image.equals("/images/Lateral")) { //$NON-NLS-1$

				image += ".png"; //$NON-NLS-1$
				dlg.lM01Icon.setIcon(new ImageIcon(getClass().getResource(image)));

				if (hasTopMark()) {
					image = "";
					switch (getBuoyIndex()) {
					case PORT_HAND:
					case PREF_PORT_HAND:
						if (region == IALA_A)
							switch (style) {
							case LAT_CAN:
								image = "/images/Top_Can_Red_Buoy_Small.png"; //$NON-NLS-1$
								break;
							case LAT_PILLAR:
							case LAT_SPAR:
								image = "/images/Top_Can_Red_Buoy.png"; //$NON-NLS-1$
								break;
							case LAT_BEACON:
							case LAT_TOWER:
								image = "/images/Top_Can_Red_Beacon.png"; //$NON-NLS-1$
								break;
							case LAT_FLOAT:
								image = "/images/Top_Can_Red_Float.png"; //$NON-NLS-1$
								break;
							}
						else
							switch (style) {
							case LAT_CAN:
								image = "/images/Top_Can_Green_Buoy_Small.png"; //$NON-NLS-1$
								break;
							case LAT_PILLAR:
							case LAT_SPAR:
								image = "/images/Top_Can_Green_Buoy.png"; //$NON-NLS-1$
								break;
							case LAT_BEACON:
							case LAT_TOWER:
								image = "/images/Top_Can_Green_Beacon.png"; //$NON-NLS-1$
								break;
							case LAT_FLOAT:
								image = "/images/Top_Can_Green_Float.png"; //$NON-NLS-1$
								break;
							}
						break;

					case STARBOARD_HAND:
					case PREF_STARBOARD_HAND:
						if (region == IALA_A)
							switch (style) {
							case LAT_CONE:
								image = "/images/Top_Cone_Green_Buoy_Small.png"; //$NON-NLS-1$
								break;
							case LAT_PILLAR:
							case LAT_SPAR:
								image = "/images/Top_Cone_Green_Buoy.png"; //$NON-NLS-1$
								break;
							case LAT_BEACON:
							case LAT_TOWER:
								image = "/images/Top_Cone_Green_Beacon.png"; //$NON-NLS-1$
								break;
							case LAT_FLOAT:
								image = "/images/Top_Cone_Green_Float.png"; //$NON-NLS-1$
								break;
							}
						else
							switch (style) {
							case LAT_CONE:
								image = "/images/Top_Cone_Red_Buoy_Small.png"; //$NON-NLS-1$
								break;
							case LAT_PILLAR:
							case LAT_SPAR:
								image = "/images/Top_Cone_Red_Buoy.png"; //$NON-NLS-1$
								break;
							case LAT_BEACON:
							case LAT_TOWER:
								image = "/images/Top_Cone_Red_Beacon.png"; //$NON-NLS-1$
								break;
							case LAT_FLOAT:
								image = "/images/Top_Cone_Red_Float.png"; //$NON-NLS-1$
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

		int cat = getBuoyIndex();
		String shape = ""; //$NON-NLS-1$
		String colour = ""; //$NON-NLS-1$

		switch (cat) {

		case PORT_HAND:
			switch (getStyleIndex()) {
			case LAT_CAN:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "can")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_PILLAR:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_SPAR:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_BEACON:
				super.saveSign("beacon_lateral"); //$NON-NLS-1$
				break;
			case LAT_TOWER:
				super.saveSign("beacon_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_FLOAT:
				super.saveSign("light_float"); //$NON-NLS-1$
				break;
			case LAT_PERCH:
				super.saveSign("beacon_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "perch")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			default:
			}
			switch (getStyleIndex()) {
			case LAT_CAN:
			case LAT_PILLAR:
			case LAT_SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:category", "port")); //$NON-NLS-1$ //$NON-NLS-2$
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				}
				break;
			case LAT_PERCH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "port")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_BEACON:
			case LAT_TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "port")); //$NON-NLS-1$ //$NON-NLS-2$
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				}
				break;
			case LAT_FLOAT:
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				}
				break;
			}
			shape = "cylinder"; //$NON-NLS-1$
			break;

		case PREF_PORT_HAND:
			switch (getStyleIndex()) {
			case LAT_CAN:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "can")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_PILLAR:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_SPAR:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_BEACON:
				super.saveSign("beacon_lateral"); //$NON-NLS-1$
				break;
			case LAT_TOWER:
				super.saveSign("beacon_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_FLOAT:
				super.saveSign("light_float"); //$NON-NLS-1$
				break;
			default:
			}
			switch (getStyleIndex()) {
			case LAT_CAN:
			case LAT_PILLAR:
			case LAT_SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:category", "preferred_channel_port")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:colour_pattern", "horizontal")); //$NON-NLS-1$ //$NON-NLS-2$
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "red;green;red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "green;red;green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				}
				break;
			case LAT_BEACON:
			case LAT_TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "preferred_channel_port")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:colour_pattern", "horizontal")); //$NON-NLS-1$ //$NON-NLS-2$
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "red;green;red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "green;red;green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				}
				break;
			case LAT_FLOAT:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour_pattern", "horizontal")); //$NON-NLS-1$ //$NON-NLS-2$
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "red;green;red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "green;red;green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				}
				break;
			}
			shape = "cylinder"; //$NON-NLS-1$
			break;

		case STARBOARD_HAND:
			switch (getStyleIndex()) {
			case LAT_CONE:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "conical")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_PILLAR:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_SPAR:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_BEACON:
				super.saveSign("beacon_lateral"); //$NON-NLS-1$
				break;
			case LAT_TOWER:
				super.saveSign("beacon_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_FLOAT:
				super.saveSign("light_float"); //$NON-NLS-1$
				break;
			case LAT_PERCH:
				super.saveSign("beacon_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "perch")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			default:
			}
			switch (getStyleIndex()) {
			case LAT_CAN:
			case LAT_PILLAR:
			case LAT_SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:category", "starboard")); //$NON-NLS-1$ //$NON-NLS-2$
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				}
				break;
			case LAT_BEACON:
			case LAT_TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "starboard")); //$NON-NLS-1$ //$NON-NLS-2$
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				}
				break;
			case LAT_FLOAT:
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				}
				break;
			case LAT_PERCH:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "starboard")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			}
			shape = "cone, point up"; //$NON-NLS-1$
			break;

		case PREF_STARBOARD_HAND:
			switch (getStyleIndex()) {
			case LAT_CONE:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "conical")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_PILLAR:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "pillar")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_SPAR:
				super.saveSign("buoy_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:shape", "spar")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_BEACON:
				super.saveSign("beacon_lateral"); //$NON-NLS-1$
				break;
			case LAT_TOWER:
				super.saveSign("beacon_lateral"); //$NON-NLS-1$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:shape", "tower")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case LAT_FLOAT:
				super.saveSign("light_float"); //$NON-NLS-1$
				break;
			default:
			}
			switch (getStyleIndex()) {
			case LAT_CAN:
			case LAT_PILLAR:
			case LAT_SPAR:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:category", "preferred_channel_starboard")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:buoy_lateral:colour_pattern", "horizontal")); //$NON-NLS-1$ //$NON-NLS-2$
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "green;red;green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:buoy_lateral:colour", "red;green;red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				}
				break;
			case LAT_BEACON:
			case LAT_TOWER:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:category", "preferred_channel_starboard")); //$NON-NLS-1$ //$NON-NLS-2$
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:beacon_lateral:colour_pattern", "horizontal")); //$NON-NLS-1$ //$NON-NLS-2$
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "green;red;green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:beacon_lateral:colour", "red;green;red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				}
				break;
			case LAT_FLOAT:
				Main.main.undoRedo.add(new ChangePropertyCommand(node,
						"seamark:light_float:colour_pattern", "horizontal")); //$NON-NLS-1$ //$NON-NLS-2$
				if (getRegion() == IALA_A) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "green;red;green")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "green"; //$NON-NLS-1$
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node,
							"seamark:light_float:colour", "red;green;red")); //$NON-NLS-1$ //$NON-NLS-2$
					colour = "red"; //$NON-NLS-1$
				}
				break;
			}
			shape = "cone, point up"; //$NON-NLS-1$
			break;

		default:
		}

		switch (getStyleIndex()) {
		case LAT_CAN:
//		case LAT_CONE:
		case LAT_PILLAR:
		case LAT_SPAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:buoy_lateral:system", (getRegion() ? "iala-b" : "iala-a"))); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case LAT_BEACON:
		case LAT_TOWER:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:beacon_lateral:system", (getRegion() ? "iala-b" : "iala-a"))); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		case LAT_FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:light_float:system", (getRegion() ? "iala-b" : "iala-a"))); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		}
		
		saveTopMarkData(shape, colour);
		saveLightData();
		saveRadarFogData();

		Main.pref.put("tomsplugin.IALA", getRegion() ? "iala-b" : "iala-a"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
