//License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig & Malcolm Herring

package toms.seamarks.buoys;

import java.util.Enumeration;
import java.util.Map;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Node;

import toms.Messages;
import toms.dialogs.SmpDialogAction;
import toms.seamarks.SeaMark;

abstract public class Buoy extends SeaMark {

	public abstract void setLightColour();

	/**
	 * private Variablen
	 */

	private int BuoyIndex = 0;

	public int getBuoyIndex() {
		return BuoyIndex;
	}

	public void setBuoyIndex(int buoyIndex) {
		BuoyIndex = buoyIndex;
	}

	private int StyleIndex = 0;

	public int getStyleIndex() {
		return StyleIndex;
	}

	public void setStyleIndex(int styleIndex) {
		StyleIndex = styleIndex;
	}

	private boolean Region = false;

	public boolean getRegion() {
		return Region;
	}

	public void setRegion(boolean region) {
		Region = region;
	}

	private boolean Radar = false;

	public boolean hasRadar() {
		return Radar;
	}

	public void setRadar(boolean radar) {
		Radar = radar;
	}

	private boolean Racon = false;

	public boolean hasRacon() {
		return Racon;
	}

	public void setRacon(boolean racon) {
		Racon = racon;
	}

	private int RaType = 0;

	public int getRaType() {
		return RaType;
	}

	public void setRaType(int type) {
		RaType = type;
	}

	private String RaconGroup = "";

	public String getRaconGroup() {
		return RaconGroup;
	}

	public void setRaconGroup(String raconGroup) {
		RaconGroup = raconGroup;
	}

	private boolean Fog = false;

	public boolean hasFog() {
		return Fog;
	}

	public void setFog(boolean fog) {
		Fog = fog;
	}

	private int FogSound = 0;

	public int getFogSound() {
		return FogSound;
	}

	public void setFogSound(int fogSound) {
		FogSound = fogSound;
	}

	private String FogGroup = "";

	public String getFogGroup() {
		return FogGroup;
	}

	public void setFogGroup(String fogGroup) {
		FogGroup = fogGroup;
	}

	private String FogPeriod = "";

	public String getFogPeriod() {
		return FogPeriod;
	}

	public void setFogPeriod(String fogPeriod) {
		FogPeriod = fogPeriod;
	}

	private boolean Fired = false;

	public boolean isFired() {
		return Fired;
	}

	public void setFired(boolean fired) {
		Fired = fired;
	}

	private boolean Sectored = false;

	public boolean isSectored() {
		return Sectored;
	}

	public void setSectored(boolean sectored) {
		Sectored = sectored;
	}

	private int SectorIndex = 0;

	public int getSectorIndex() {
		return SectorIndex;
	}

	public void setSectorIndex(int sector) {
		SectorIndex = sector;
	}

	private String[] LightChar = new String[10];

	public String getLightChar() {
		if (LightChar[getSectorIndex()] == null)
			return (LightChar[0]);
		return LightChar[getSectorIndex()];
	}

	public void setLightChar(String lightChar) {
		if (getSectorIndex() == 0)
			LightChar = new String[10];
		LightChar[getSectorIndex()] = lightChar;
	}

	private String[] LightColour = new String[10];

	public String getLightColour() {
		if (LightColour[getSectorIndex()] == null)
			return (LightColour[0]);
		return LightColour[getSectorIndex()];
	}

	public void setLightColour(String lightColour) {
		if (getSectorIndex() == 0)
			LightColour = new String[10];
		LightColour[getSectorIndex()] = lightColour;
	}

	private String[] LightGroup = new String[10];

	public String getLightGroup() {
		if (LightGroup[getSectorIndex()] == null)
			return (LightGroup[0]);
		return LightGroup[getSectorIndex()];
	}

	public void setLightGroup(String lightGroup) {
		if (getSectorIndex() == 0)
			LightGroup = new String[10];
		LightGroup[getSectorIndex()] = lightGroup;
	}

	protected void setLightGroup(Map<String, String> k) {
		String s = "";
		if (k.containsKey("seamark:light:group")) {
			s = k.get("seamark:light:group");
			setLightGroup(s);
		}
	}

	private String[] Height = new String[10];

	public String getHeight() {
		if (Height[getSectorIndex()] == null)
			return (Height[0]);
		return Height[getSectorIndex()];
	}

	public void setHeight(String height) {
		if (getSectorIndex() == 0)
			Height = new String[10];
		Height[getSectorIndex()] = height;
	}

	private String[] Range = new String[10];

	public String getRange() {
		if (Range[getSectorIndex()] == null)
			return (Range[0]);
		return Range[getSectorIndex()];
	}

	public void setRange(String range) {
		if (getSectorIndex() == 0)
			Range = new String[10];
		Range[getSectorIndex()] = range;
	}

	private String[] Bearing1 = new String[10];

	public String getBearing1() {
		if (Bearing1[getSectorIndex()] == null)
			return (Bearing1[0]);
		return Bearing1[getSectorIndex()];
	}

	public void setBearing1(String bearing) {
		if (getSectorIndex() == 0)
			Bearing1 = new String[10];
		Bearing1[getSectorIndex()] = bearing;
	}

	private String[] Bearing2 = new String[10];

	public String getBearing2() {
		if (Bearing2[getSectorIndex()] == null)
			return (Bearing2[0]);
		return Bearing2[getSectorIndex()];
	}

	public void setBearing2(String bearing) {
		if (getSectorIndex() == 0)
			Bearing2 = new String[10];
		Bearing2[getSectorIndex()] = bearing;
	}

	private String[] Radius = new String[10];

	public String getRadius() {
		if (Radius[getSectorIndex()] == null)
			return (Radius[0]);
		return Radius[getSectorIndex()];
	}

	public void setRadius(String radius) {
		if (getSectorIndex() == 0)
			Radius = new String[10];
		Radius[getSectorIndex()] = radius;
	}

	private String[] LightPeriod = new String[10];

	public String getLightPeriod() {
		if (LightPeriod[getSectorIndex()] == null)
			return (LightPeriod[0]);
		return LightPeriod[getSectorIndex()];
	}

	public void setLightPeriod(String lightPeriod) {
		String regex = "^[\\d\\s.]+$";

		if (!lightPeriod.isEmpty()) {

			Pattern pat = Pattern.compile(regex);
			Matcher matcher = pat.matcher(lightPeriod);

			if (matcher.find()) {
				setErrMsg(null);
			} else {
				setErrMsg("Must be a number");
				lightPeriod = "";
				dlg.tfM01RepeatTime.requestFocus();
			}
		}
		if (getSectorIndex() == 0)
			LightPeriod = new String[10];
		LightPeriod[getSectorIndex()] = lightPeriod;
	}

	protected void setLightPeriod(Map<String, String> k) {
		String s = "";
		if (k.containsKey("seamark:light:period")) {
			s = k.get("seamark:light:period");
			setSectorIndex(0);
			setLightPeriod(s);
			return;
		}
	}

	private Node Node = null;

	public Node getNode() {
		return Node;
	}

	public void setNode(Node node) {
		Node = node;
	}

	private boolean TopMark = false;

	public boolean hasTopMark() {
		return TopMark;
	}

	public void setTopMark(boolean topMark) {
		TopMark = topMark;
		/*
		 * if (dlg.cM01TopMark == null) { return; }
		 */
		dlg.cM01TopMark.setSelected(topMark);
	}

	protected SmpDialogAction dlg = null; // hier wird der Dialog referenziert

	public SmpDialogAction getDlg() {
		return dlg;
	}

	public void setDlg(SmpDialogAction dlg) {
		this.dlg = dlg;
	}

	protected Buoy(SmpDialogAction dia) {
		dlg = dia;
	}

	public boolean isValid() {
		return false;
	}

	public void parseLights(Map<String, String> k) {
		setFired(false);
		setSectored(false);
		Iterator it = k.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String key = (String) entry.getKey();
			String value = ((String) entry.getValue()).trim();
			if (key.contains("seamark:light:")) {
				setFired(true);
				int index = 0;
				key = key.substring(14);
				if (key.matches("^\\d:.*")) {
					index = key.charAt(0) - '0';
					key = key.substring(2);
				} else if (key.matches("^\\d$")) {
					index = key.charAt(0) - '0';
					String values[] = value.split(":");
					if (values[0].equals("red"))
						LightColour[index] = "R";
					else if (values[0].equals("green"))
						LightColour[index] = "G";
					else if (values[0].equals("white"))
						LightColour[index] = "W";
					Bearing1[index] = values[1];
					Bearing2[index] = values[2];
					Radius[index] = values[3];
				} else {
					index = 0;
				}
				if (index != 0) setSectored(true);
				if (key.equals("colour")) {
					if (value.equals("red"))
						LightColour[index] = "R";
					else if (value.equals("green"))
						LightColour[index] = "G";
					else if (value.equals("white"))
						LightColour[index] = "W";
				} else if (key.equals("character")) {
					LightChar[index] = value;
				} else if (key.equals("group")) {
					LightGroup[index] = value;
				} else if (key.equals("period")) {
					LightPeriod[index] = value;
				} else if (key.equals("height")) {
					Height[index] = value;
				} else if (key.equals("range")) {
					Range[index] = value;
				}
			}
		}
	}

	public void paintSign() {

		if (dlg.paintlock)
			return;
		else
			dlg.paintlock = true;

		dlg.lM01Icon.setIcon(null);
		dlg.lM02Icon.setIcon(null);
		dlg.lM03Icon.setIcon(null);
		dlg.lM04Icon.setIcon(null);
		dlg.lM05Icon.setIcon(null);

		dlg.rbM01RegionA.setSelected(!getRegion());
		dlg.rbM01RegionB.setSelected(getRegion());

		if (isValid()) {
			dlg.bM01Save.setEnabled(true);

			dlg.cM01TopMark.setSelected(hasTopMark());
			dlg.cM01Fired.setSelected(isFired());

			dlg.tfM01RepeatTime.setText(getLightPeriod());

			dlg.tfM01Name.setText(getName());
			dlg.tfM01Name.setEnabled(true);

			if (hasRadar()) {
				dlg.lM03Icon.setIcon(new ImageIcon(getClass().getResource(
						"/images/Radar_Reflector.png")));
			}

			if (hasRacon()) {
				dlg.lM04Icon.setIcon(new ImageIcon(getClass().getResource(
						"/images/Radar_Station.png")));
				dlg.cbM01Racon.setVisible(true);
				if (getRaType() == RATYP_RACON) {
					dlg.lM01Racon.setVisible(true);
					dlg.tfM01Racon.setVisible(true);
					dlg.tfM01Racon.setEnabled(true);
				} else {
					dlg.lM01Racon.setVisible(false);
					dlg.tfM01Racon.setVisible(false);
				}
			} else {
				dlg.cbM01Racon.setVisible(false);
				dlg.lM01Racon.setVisible(false);
				dlg.tfM01Racon.setVisible(false);
			}

			if (hasFog()) {
				dlg.lM05Icon.setIcon(new ImageIcon(getClass().getResource(
						"/images/Fog_Signal.png")));
				dlg.cbM01Fog.setVisible(true);
				if (getFogSound() == 0) {
					dlg.lM01FogGroup.setVisible(false);
					dlg.tfM01FogGroup.setVisible(false);
					dlg.lM01FogPeriod.setVisible(false);
					dlg.tfM01FogPeriod.setVisible(false);
				} else {
					dlg.lM01FogGroup.setVisible(true);
					dlg.tfM01FogGroup.setVisible(true);
					dlg.lM01FogPeriod.setVisible(true);
					dlg.tfM01FogPeriod.setVisible(true);
				}
			} else {
				dlg.cbM01Fog.setVisible(false);
				dlg.lM01FogGroup.setVisible(false);
				dlg.tfM01FogGroup.setVisible(false);
				dlg.lM01FogPeriod.setVisible(false);
				dlg.tfM01FogPeriod.setVisible(false);
			}

			if (isFired()) {
				String lp, c;
				String tmp = null;
				int i1;

				String col = getLightColour();
				if (col.equals("W")) {
					dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource(
							"/images/Light_White_120.png")));
					dlg.cbM01Colour.setSelectedIndex(WHITE_LIGHT);
				} else if (col.equals("R")) {
					dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource(
							"/images/Light_Red_120.png")));
					dlg.cbM01Colour.setSelectedIndex(RED_LIGHT);
				} else if (col.equals("G")) {
					dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource(
							"/images/Light_Green_120.png")));
					dlg.cbM01Colour.setSelectedIndex(GREEN_LIGHT);
				} else {
					dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource(
							"/images/Light_Magenta_120.png")));
					dlg.cbM01Colour.setSelectedIndex(UNKNOWN_COLOUR);
				}

				c = LightChar[0];

				if (c.contains("+")) {
					i1 = c.indexOf("+");
					tmp = c.substring(i1, c.length());
					c = c.substring(0, i1);
				}

				if (!LightGroup[0].isEmpty())
					c = c + "(" + LightGroup[0] + ")";
				if (tmp != null)
					c = c + tmp;

				c = c + " " + LightColour[0];
				lp = LightPeriod[0];
				if (!lp.isEmpty())
					c = c + " " + lp + "s";
				dlg.lM01FireMark.setText(c);

				dlg.cM01Fired.setVisible(true);
				dlg.lM01Kennung.setVisible(true);
				dlg.cbM01Kennung.setVisible(true);
				dlg.lM01Group.setVisible(true);
				dlg.tfM01Group.setVisible(true);
				dlg.tfM01Group.setText(getLightGroup());
				dlg.lM01RepeatTime.setVisible(true);
				dlg.tfM01RepeatTime.setVisible(true);
				if (isSectored()) {
					dlg.rbM01Fired1.setSelected(false);
					dlg.rbM01FiredN.setSelected(true);
					if ((getSectorIndex() != 0) && (!LightChar[0].isEmpty()))
						dlg.cbM01Kennung.setEnabled(false);
					else
						dlg.cbM01Kennung.setEnabled(true);
					dlg.cbM01Kennung.setSelectedItem(getLightChar());
					if ((getSectorIndex() != 0) && (!LightGroup[0].isEmpty()))
						dlg.tfM01Group.setEnabled(false);
					else
						dlg.tfM01Group.setEnabled(true);
					dlg.tfM01Group.setText(getLightGroup());
					if ((getSectorIndex() != 0) && (!LightPeriod[0].isEmpty()))
						dlg.tfM01RepeatTime.setEnabled(false);
					else
						dlg.tfM01RepeatTime.setEnabled(true);
					dlg.tfM01RepeatTime.setText(getLightPeriod());
					if ((getSectorIndex() != 0) && (!Height[0].isEmpty()))
						dlg.tfM01Height.setEnabled(false);
					else
						dlg.tfM01Height.setEnabled(true);
					dlg.tfM01Height.setText(getHeight());
					if ((getSectorIndex() != 0) && (!Range[0].isEmpty()))
						dlg.tfM01Range.setEnabled(false);
					else
						dlg.tfM01Range.setEnabled(true);
					dlg.tfM01Range.setText(getRange());
					dlg.lM01Sector.setVisible(true);
					dlg.cbM01Sector.setVisible(true);
					if (getSectorIndex() == 0) {
						dlg.lM01Colour.setVisible(false);
						dlg.cbM01Colour.setVisible(false);
						dlg.lM01Bearing.setVisible(false);
						dlg.tfM01Bearing.setVisible(false);
						dlg.tfM02Bearing.setVisible(false);
						dlg.tfM01Radius.setVisible(false);
					} else {
						dlg.lM01Colour.setVisible(true);
						dlg.cbM01Colour.setVisible(true);
						dlg.lM01Bearing.setVisible(true);
						dlg.tfM01Bearing.setVisible(true);
						dlg.tfM01Bearing.setText(getBearing1());
						dlg.tfM02Bearing.setVisible(true);
						dlg.tfM02Bearing.setText(getBearing2());
						dlg.tfM01Radius.setVisible(true);
						dlg.tfM01Radius.setText(getRadius());
					}
				} else {
					dlg.rbM01FiredN.setSelected(false);
					dlg.rbM01Fired1.setSelected(true);
					dlg.cbM01Kennung.setEnabled(true);
					dlg.tfM01Group.setEnabled(true);
					dlg.tfM01RepeatTime.setEnabled(true);
					dlg.tfM01Height.setEnabled(true);
					dlg.tfM01Range.setEnabled(true);
					dlg.lM01Colour.setVisible(true);
					dlg.cbM01Colour.setVisible(true);
					dlg.lM01Sector.setVisible(false);
					dlg.cbM01Sector.setVisible(false);
					dlg.lM01Bearing.setVisible(false);
					dlg.tfM01Bearing.setVisible(false);
					dlg.tfM02Bearing.setVisible(false);
					dlg.tfM01Radius.setVisible(false);
				}
			} else {
				dlg.lM01FireMark.setText("");
			}
		} else {
			dlg.bM01Save.setEnabled(false);
			dlg.tfM01Name.setEnabled(false);
			dlg.cM01TopMark.setVisible(false);
			dlg.cbM01TopMark.setVisible(false);
			dlg.cM01Radar.setVisible(false);
			dlg.cM01Racon.setVisible(false);
			dlg.cbM01Racon.setVisible(false);
			dlg.tfM01Racon.setVisible(false);
			dlg.lM01Racon.setVisible(false);
			dlg.cM01Fog.setVisible(false);
			dlg.cbM01Fog.setVisible(false);
			dlg.tfM01FogGroup.setVisible(false);
			dlg.lM01FogGroup.setVisible(false);
			dlg.tfM01FogPeriod.setVisible(false);
			dlg.lM01FogPeriod.setVisible(false);
			dlg.cM01Fired.setVisible(false);
			dlg.rbM01Fired1.setVisible(false);
			dlg.rbM01FiredN.setVisible(false);
			dlg.cbM01Kennung.setVisible(false);
			dlg.lM01Kennung.setVisible(false);
			dlg.tfM01Height.setVisible(false);
			dlg.lM01Height.setVisible(false);
			dlg.tfM01Range.setVisible(false);
			dlg.lM01Range.setVisible(false);
			dlg.cbM01Colour.setVisible(false);
			dlg.lM01Colour.setVisible(false);
			dlg.cbM01Sector.setVisible(false);
			dlg.lM01Sector.setVisible(false);
			dlg.tfM01Group.setVisible(false);
			dlg.lM01Group.setVisible(false);
			dlg.tfM01RepeatTime.setVisible(false);
			dlg.lM01RepeatTime.setVisible(false);
			dlg.tfM01Bearing.setVisible(false);
			dlg.lM01Bearing.setVisible(false);
			dlg.tfM02Bearing.setVisible(false);
			dlg.tfM01Radius.setVisible(false);
		}
		dlg.paintlock = false;
	}

	public void saveSign(String type) {
		delSeaMarkKeys(Node);

		String str = dlg.tfM01Name.getText();
		if (!str.isEmpty())
			Main.main.undoRedo.add(new ChangePropertyCommand(Node, "seamark:name",
					str));
		Main.main.undoRedo
				.add(new ChangePropertyCommand(Node, "seamark:type", type));
	}

	protected void saveLightData() {
		String colour;
		if (dlg.cM01Fired.isSelected()) {
			if (!(colour = LightColour[0]).isEmpty())
				if (colour.equals("R")) {
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:colour", "red"));
				} else if (colour.equals("G")) {
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:colour", "green"));
				} else if (colour.equals("W")) {
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:colour", "white"));
				}

			if (!LightPeriod[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:period", LightPeriod[0]));

			if (!LightChar[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:character", LightChar[0]));

			if (!LightGroup[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:group", LightGroup[0]));

			if (!Height[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:height", Height[0]));

			if (!Range[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:range", Range[0]));

			for (int i = 1; i < 10; i++) {
				if ((colour = LightColour[i]) != null)
					if (colour.equals("R")) {
						Main.main.undoRedo.add(new ChangePropertyCommand(Node,
								"seamark:light:" + i + ":colour", "red"));
						if ((Bearing1[i] != null) && (Bearing2[i] != null)
								&& (Radius[i] != null))
							Main.main.undoRedo.add(new ChangePropertyCommand(Node,
									"seamark:light:" + i, "red:" + Bearing1[i] + ":"
											+ Bearing2[i] + ":" + Radius[i]));
					} else if (colour.equals("G")) {
						Main.main.undoRedo.add(new ChangePropertyCommand(Node,
								"seamark:light:" + i + ":colour", "green"));
						if ((Bearing1[i] != null) && (Bearing2[i] != null)
								&& (Radius[i] != null))
							Main.main.undoRedo.add(new ChangePropertyCommand(Node,
									"seamark:light:" + i, "green:" + Bearing1[i] + ":"
											+ Bearing2[i] + ":" + Radius[i]));
					} else if (colour.equals("W")) {
						Main.main.undoRedo.add(new ChangePropertyCommand(Node,
								"seamark:light:" + i + ":colour", "white"));
						if ((Bearing1[i] != null) && (Bearing2[i] != null)
								&& (Radius[i] != null))
							Main.main.undoRedo.add(new ChangePropertyCommand(Node,
									"seamark:light:" + i, "white:" + Bearing1[i] + ":"
											+ Bearing2[i] + ":" + Radius[i]));
					}

				if (LightPeriod[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":period", LightPeriod[i]));

				if (LightChar[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":character", LightChar[i]));

				if (LightGroup[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":group", LightGroup[i]));

				if (Height[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":height", Height[i]));

				if (Range[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":range", Range[i]));

				if (Bearing1[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":sector_start", Bearing1[i]));

				if (Bearing2[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":sector_end", Bearing2[i]));
			}
		}
	}

	protected void saveTopMarkData(String shape, String colour) {
		if (dlg.cM01TopMark.isSelected()) {
			Main.main.undoRedo.add(new ChangePropertyCommand(Node,
					"seamark:topmark:shape", shape));
			Main.main.undoRedo.add(new ChangePropertyCommand(Node,
					"seamark:topmark:colour", colour));
		}
	}

	protected void saveRadarFogData() {
		if (hasRadar()) {
			Main.main.undoRedo.add(new ChangePropertyCommand(Node,
					"seamark:radar_reflector", "yes"));
		}
		if (hasRacon()) {
			switch (RaType) {
			case RATYP_RACON:
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:radar_transponder:category", "racon"));
				if (!getRaconGroup().isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:radar_transponder:group", getRaconGroup()));
				break;
			case RATYP_RAMARK:
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:radar_transponder:category", "ramark"));
				break;
			case RATYP_LEADING:
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:radar_transponder:category", "leading"));
				break;
			default:
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:radar_transponder", "yes"));
			}
		}
		if (hasFog()) {
			if (FogSound == 0) {
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:fog_signal", "yes"));
			} else {
				switch (FogSound) {
				case FOG_HORN:
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:fog_signal:category", "horn"));
					break;
				case FOG_SIREN:
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:fog_signal:category", "siren"));
					break;
				case FOG_DIA:
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:fog_signal:category", "diaphone"));
					break;
				case FOG_BELL:
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:fog_signal:category", "bell"));
					break;
				case FOG_WHIS:
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:fog_signal:category", "whistle"));
					break;
				case FOG_GONG:
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:fog_signal:category", "gong"));
					break;
				case FOG_EXPLOS:
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:fog_signal:category", "explosive"));
					break;
				}
				if (!getFogGroup().isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:fog_group:group", getFogGroup()));
				if (!getFogPeriod().isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:fog_period:group", getFogPeriod()));
			}
		}
	}

	public void refreshStyles() {
	}

	public void refreshLights() {
		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.addItem(Messages.getString("SmpDialogAction.212")); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Fl"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("LFl"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Iso"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("F"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("FFl"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Oc"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Q"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("IQ"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("VQ"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("IVQ"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("UQ"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("IUQ"); //$NON-NLS-1$
		dlg.cbM01Kennung.addItem("Mo"); //$NON-NLS-1$
		dlg.cbM01Kennung.setSelectedIndex(0);
	}

	public void resetMask() {
		setRegion(Main.pref.get("tomsplugin.IALA").equals("B"));

		dlg.lM01Icon.setIcon(null);
		dlg.lM02Icon.setIcon(null);
		dlg.lM03Icon.setIcon(null);
		dlg.lM04Icon.setIcon(null);

		dlg.rbM01RegionA.setEnabled(false);
		dlg.rbM01RegionB.setEnabled(false);
		dlg.lM01FireMark.setText("");
		dlg.cbM01CatOfMark.removeAllItems();
		dlg.cbM01CatOfMark.setVisible(false);
		dlg.lM01CatOfMark.setVisible(false);
		setBuoyIndex(0);
		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.setVisible(false);
		dlg.lM01StyleOfMark.setVisible(false);
		setStyleIndex(0);
		dlg.tfM01Name.setText("");
		dlg.tfM01Name.setEnabled(false);
		setName("");
		dlg.cM01TopMark.setSelected(false);
		dlg.cM01TopMark.setVisible(false);
		dlg.cbM01TopMark.removeAllItems();
		dlg.cbM01TopMark.setVisible(false);
		setTopMark(false);
		dlg.cM01Radar.setSelected(false);
		dlg.cM01Radar.setVisible(false);
		setRadar(false);
		dlg.cM01Racon.setSelected(false);
		dlg.cM01Racon.setVisible(false);
		dlg.cbM01Racon.setVisible(false);
		dlg.tfM01Racon.setText("");
		dlg.tfM01Racon.setVisible(false);
		dlg.lM01Racon.setVisible(false);
		setRacon(false);
		setRaType(0);
		dlg.cM01Fog.setSelected(false);
		dlg.cM01Fog.setVisible(false);
		dlg.cbM01Fog.setVisible(false);
		setFogSound(0);
		dlg.tfM01FogGroup.setText("");
		dlg.tfM01FogGroup.setVisible(false);
		dlg.lM01FogGroup.setVisible(false);
		dlg.tfM01FogPeriod.setText("");
		dlg.tfM01FogPeriod.setVisible(false);
		dlg.lM01FogPeriod.setVisible(false);
		setFog(false);
		dlg.cM01Fired.setSelected(false);
		dlg.cM01Fired.setVisible(false);
		setFired(false);
		dlg.rbM01Fired1.setVisible(false);
		dlg.rbM01Fired1.setSelected(true);
		dlg.rbM01FiredN.setVisible(false);
		dlg.rbM01FiredN.setSelected(false);
		setSectored(false);
		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.setVisible(false);
		dlg.lM01Kennung.setVisible(false);
		setLightChar("");
		dlg.tfM01Height.setText("");
		dlg.tfM01Height.setVisible(false);
		dlg.lM01Height.setVisible(false);
		setHeight("");
		dlg.tfM01Range.setText("");
		dlg.tfM01Range.setVisible(false);
		dlg.lM01Range.setVisible(false);
		setRange("");
		dlg.cbM01Colour.setVisible(false);
		dlg.lM01Colour.setVisible(false);
		setLightColour("");
		dlg.cbM01Sector.setVisible(false);
		dlg.lM01Sector.setVisible(false);
		setSectorIndex(0);
		dlg.tfM01Group.setText("");
		dlg.tfM01Group.setVisible(false);
		dlg.lM01Group.setVisible(false);
		setLightGroup("");
		dlg.tfM01RepeatTime.setText("");
		dlg.tfM01RepeatTime.setVisible(false);
		dlg.lM01RepeatTime.setVisible(false);
		setLightPeriod("");
		dlg.tfM01Bearing.setText("");
		dlg.tfM01Bearing.setVisible(false);
		dlg.lM01Bearing.setVisible(false);
		setBearing1("");
		dlg.tfM02Bearing.setText("");
		dlg.tfM02Bearing.setVisible(false);
		setBearing2("");
		dlg.tfM01Radius.setText("");
		dlg.tfM01Radius.setVisible(false);
		setRadius("");

		dlg.bM01Save.setEnabled(false);
	}

}
