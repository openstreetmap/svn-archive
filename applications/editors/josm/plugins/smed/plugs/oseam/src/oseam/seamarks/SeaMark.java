package oseam.seamarks;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Node;

import oseam.dialogs.OSeaMAction;

abstract public class SeaMark {

	/**
	 * Variables
	 */

	protected OSeaMAction dlg = null;

	public OSeaMAction getDlg() {
		return dlg;
	}

	protected SeaMark(OSeaMAction dia) {
		dlg = dia;
		region = Main.pref.get("tomsplugin.IALA").equals("B");
	}

	public final static boolean IALA_A = false;
	public final static boolean IALA_B = true;

	private boolean region = false;

	public boolean getRegion() {
		return region;
	}

	public void setRegion(boolean reg) {
		region = reg;
	}

	public enum Col {
		UNKNOWN, WHITE, RED, ORANGE, AMBER, YELLOW, GREEN, BLUE, VIOLET,
		BLACK, RED_GREEN_RED, GREEN_RED_GREEN, RED_WHITE,
		BLACK_YELLOW, BLACK_YELLOW_BLACK, YELLOW_BLACK, YELLOW_BLACK_YELLOW, BLACK_RED_BLACK
	}

	private Col colour = Col.UNKNOWN;

	public Col getColour() {
		return colour;
	}

	public void setColour(Col col) {
		colour = col;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String nam) {
		name = nam;
	}

	public enum Cat {
		UNKNOWN, LAT_PORT, LAT_STBD, LAT_PREF_PORT, LAT_PREF_STBD,
		CARD_NORTH, CARD_EAST, CARD_SOUTH, CARD_WEST,
		LIGHT_HOUSE, LIGHT_MAJOR, LIGHT_MINOR, LIGHT_VESSEL, LIGHT_FLOAT
	}

	private Cat category = Cat.UNKNOWN;

	public Cat getCategory() {
		return category;
	}

	public void setCategory(Cat cat) {
		category = cat;
	}

	public enum Shp {
		UNKNOWN, PILLAR, SPAR, CAN, CONE, SPHERE, BARREL, FLOAT, SUPER, BEACON, TOWER, STAKE, PERCH
	}

	private Shp shape = Shp.UNKNOWN;

	public Shp getShape() {
		return shape;
	}

	public void setShape(Shp styl) {
		shape = styl;
	}

	public enum Top {
		UNKNOWN, X_SHAPE, CAN, CONE
	}

	private boolean TopMark = false;

	public boolean hasTopMark() {
		return TopMark;
	}

	public void setTopMark(boolean topMark) {
		TopMark = topMark;
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

	public enum Rtb {
		UNKNOWN, RACON, RAMARK, LEADING
	}

	private Rtb RaType = Rtb.UNKNOWN;

	public Rtb getRaType() {
		return RaType;
	}

	public void setRaType(Rtb type) {
		RaType = type;
	}

	private String RaconGroup = "";

	public String getRaconGroup() {
		return RaconGroup;
	}

	public void setRaconGroup(String raconGroup) {
		RaconGroup = raconGroup;
	}

	private boolean FogSignal = false;

	public boolean hasFog() {
		return FogSignal;
	}

	public void setFog(boolean fog) {
		FogSignal = fog;
	}

	public enum Fog {
		UNKNOWN, HORN, SIREN, DIA, BELL, WHIS, GONG, EXPLOS
	}

	private Fog FogSound = Fog.UNKNOWN;

	public Fog getFogSound() {
		return FogSound;
	}

	public void setFogSound(Fog sound) {
		FogSound = sound;
	}

	private String FogGroup = "";

	public String getFogGroup() {
		return FogGroup;
	}

	public void setFogGroup(String group) {
		FogGroup = group;
	}

	private String FogPeriod = "";

	public String getFogPeriod() {
		return FogPeriod;
	}

	public void setFogPeriod(String period) {
		FogPeriod = period;
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
		if (sectored) {
			LightColour[0] = "";
		} else {
			setSectorIndex(0);
			setLightChar("");
			setLightColour("");
			setLightGroup("");
			setHeight("");
			setRange("");
			setBearing1("");
			setBearing2("");
			setRadius("");
		}
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
		if (LightChar[SectorIndex] == null)
			return (LightChar[0]);
		return LightChar[SectorIndex];
	}

	public void setLightChar(String lightChar) {
		if (SectorIndex == 0) {
			LightChar = new String[10];
			LightChar[0] = lightChar;
		} else if (LightChar[0].isEmpty())
			LightChar[SectorIndex] = lightChar;
	}

	private String[] LightColour = new String[10];

	public String getLightColour() {
		if (LightColour[SectorIndex] == null)
			return (LightColour[0]);
		return LightColour[SectorIndex];
	}

	public void setLightColour(String lightColour) {
		LightColour[SectorIndex] = lightColour;
	}

	private String[] LightGroup = new String[10];

	public String getLightGroup() {
		if (LightGroup[SectorIndex] == null)
			return (LightGroup[0]);
		return LightGroup[SectorIndex];
	}

	public void setLightGroup(String lightGroup) {
		if (SectorIndex == 0)
			LightGroup = new String[10];
		LightGroup[SectorIndex] = lightGroup;
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
		if (Height[SectorIndex] == null)
			return (Height[0]);
		return Height[SectorIndex];
	}

	public void setHeight(String height) {
		if (SectorIndex == 0)
			Height = new String[10];
		Height[SectorIndex] = height;
	}

	private String[] Range = new String[10];

	public String getRange() {
		if (Range[SectorIndex] == null)
			return (Range[0]);
		return Range[SectorIndex];
	}

	public void setRange(String range) {
		if (SectorIndex == 0)
			Range = new String[10];
		Range[SectorIndex] = range;
	}

	private String[] Bearing1 = new String[10];

	public String getBearing1() {
		if (Bearing1[SectorIndex] == null)
			return (Bearing1[0]);
		return Bearing1[SectorIndex];
	}

	public void setBearing1(String bearing) {
		if (SectorIndex == 0)
			Bearing1 = new String[10];
		Bearing1[SectorIndex] = bearing;
	}

	private String[] Bearing2 = new String[10];

	public String getBearing2() {
		if (Bearing2[SectorIndex] == null)
			return (Bearing2[0]);
		return Bearing2[SectorIndex];
	}

	public void setBearing2(String bearing) {
		if (SectorIndex == 0)
			Bearing2 = new String[10];
		Bearing2[SectorIndex] = bearing;
	}

	private String[] Radius = new String[10];

	public String getRadius() {
		if (Radius[SectorIndex] == null)
			return (Radius[0]);
		return Radius[SectorIndex];
	}

	public void setRadius(String radius) {
		if (SectorIndex == 0)
			Radius = new String[10];
		Radius[SectorIndex] = radius;
	}

	private String[] LightPeriod = new String[10];

	public String getLightPeriod() {
		if (LightPeriod[SectorIndex] == null)
			return (LightPeriod[0]);
		return LightPeriod[SectorIndex];
	}

	public void setLightPeriod(String lightPeriod) {
		String regex = "^[\\d\\s.]+$";

		if (!lightPeriod.isEmpty()) {

			Pattern pat = Pattern.compile(regex);
			Matcher matcher = pat.matcher(lightPeriod);

			if (matcher.find()) {
				// setErrMsg(null);
			} else {
				// setErrMsg("Must be a number");
				lightPeriod = "";
				// dlg.tfM01RepeatTime.requestFocus();
			}
		}
		if (SectorIndex == 0)
			LightPeriod = new String[10];
		LightPeriod[SectorIndex] = lightPeriod;
	}

	public abstract void parseMark();

	public void parseLights(Map<String, String> k) {
		setFired(false);
		setSectored(false);
		Iterator it = k.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
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
					if (values.length > 1)
						Bearing1[index] = values[1];
					if (values.length > 2)
						Bearing2[index] = values[2];
					if (values.length > 3)
						Radius[index] = values[3];
				} else {
					index = 0;
				}
				if (index != 0)
					setSectored(true);
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

	public void parseFogRadar(Map<String, String> k) {
		String str;
		setFog(false);
		setRadar(false);
		setRacon(false);
		if (k.containsKey("seamark:fog_signal") || k.containsKey("seamark:fog_signal:category")
				|| k.containsKey("seamark:fog_signal:group") || k.containsKey("seamark:fog_signal:period")) {
			setFog(true);
			if (k.containsKey("seamark:fog_signal:category")) {
				str = k.get("seamark:fog_signal:category");
				if (str.equals("horn"))
					setFogSound(Fog.HORN);
				else if (str.equals("siren"))
					setFogSound(Fog.SIREN);
				else if (str.equals("diaphone"))
					setFogSound(Fog.DIA);
				else if (str.equals("bell"))
					setFogSound(Fog.BELL);
				else if (str.equals("whis"))
					setFogSound(Fog.WHIS);
				else if (str.equals("gong"))
					setFogSound(Fog.GONG);
				else if (str.equals("explosive"))
					setFogSound(Fog.EXPLOS);
				else
					setFogSound(Fog.UNKNOWN);
			}
			if (k.containsKey("seamark:fog_signal:group"))
				setFogGroup(k.get("seamark:fog_signal:group"));
			if (k.containsKey("seamark:fog_signal:period"))
				setFogPeriod(k.get("seamark:fog_signal:period"));
		}

		if (k.containsKey("seamark:radar_transponder") || k.containsKey("seamark:radar_transponder:category")
				|| k.containsKey("seamark:radar_transponder:group")) {
			setRacon(true);
			if (k.containsKey("seamark:radar_transponder:category")) {
				str = k.get("seamark:radar_transponder:category");
				if (str.equals("racon"))
					setRaType(Rtb.RACON);
				else if (str.equals("ramark"))
					setRaType(Rtb.RAMARK);
				else if (str.equals("leading"))
					setRaType(Rtb.LEADING);
				else
					setRaType(Rtb.UNKNOWN);
			}
			if (k.containsKey("seamark:radar_transponder:group"))
				setRaconGroup(k.get("seamark:radar_transponder:group"));
		} else if (k.containsKey("seamark:radar_reflector"))
			setRadar(true);
	}

	public void paintSign() {
		/*
		 * dlg.lM01NameMark.setText(getName());
		 * 
		 * dlg.bM01Save.setEnabled(true);
		 * 
		 * dlg.cM01TopMark.setSelected(hasTopMark());
		 * dlg.cM01Fired.setSelected(isFired());
		 * 
		 * dlg.tfM01RepeatTime.setText(getLightPeriod());
		 * 
		 * dlg.tfM01Name.setText(getName()); dlg.tfM01Name.setEnabled(true);
		 * 
		 * if (hasRadar()) { dlg.lM03Icon.setIcon(new
		 * ImageIcon(getClass().getResource( "/images/Radar_Reflector_355.png"))); }
		 * 
		 * else if (hasRacon()) { dlg.lM04Icon.setIcon(new
		 * ImageIcon(getClass().getResource( "/images/Radar_Station.png"))); if
		 * (getRaType() != 0) { String c = (String)
		 * dlg.cbM01Racon.getSelectedItem(); if ((getRaType() == RATYPE_RACON) &&
		 * !getRaconGroup().isEmpty()) c += ("(" + getRaconGroup() + ")");
		 * dlg.lM01RadarMark.setText(c); } dlg.cbM01Racon.setVisible(true); if
		 * (getRaType() == RATYPE_RACON) { dlg.lM01Racon.setVisible(true);
		 * dlg.tfM01Racon.setVisible(true); dlg.tfM01Racon.setEnabled(true); } else
		 * { dlg.lM01Racon.setVisible(false); dlg.tfM01Racon.setVisible(false); } }
		 * else { dlg.cbM01Racon.setVisible(false); dlg.lM01Racon.setVisible(false);
		 * dlg.tfM01Racon.setVisible(false); }
		 * 
		 * if (hasFog()) { dlg.lM05Icon.setIcon(new
		 * ImageIcon(getClass().getResource( "/images/Fog_Signal.png"))); if
		 * (getFogSound() != 0) { String c = (String)
		 * dlg.cbM01Fog.getSelectedItem(); if (!getFogGroup().isEmpty()) c += ("(" +
		 * getFogGroup() + ")"); if (!getFogPeriod().isEmpty()) c += (" " +
		 * getFogPeriod() + "s"); dlg.lM01FogMark.setText(c); }
		 * dlg.cbM01Fog.setVisible(true); if (getFogSound() == 0) {
		 * dlg.lM01FogGroup.setVisible(false); dlg.tfM01FogGroup.setVisible(false);
		 * dlg.lM01FogPeriod.setVisible(false);
		 * dlg.tfM01FogPeriod.setVisible(false); } else {
		 * dlg.lM01FogGroup.setVisible(true); dlg.tfM01FogGroup.setVisible(true);
		 * dlg.lM01FogPeriod.setVisible(true); dlg.tfM01FogPeriod.setVisible(true);
		 * } } else { dlg.cbM01Fog.setVisible(false);
		 * dlg.lM01FogGroup.setVisible(false); dlg.tfM01FogGroup.setVisible(false);
		 * dlg.lM01FogPeriod.setVisible(false);
		 * dlg.tfM01FogPeriod.setVisible(false); }
		 * 
		 * if (isFired()) { String lp, c; String tmp = null; int i1;
		 * 
		 * String col = getLightColour(); if (col.equals("W")) {
		 * dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource(
		 * "/images/Light_White_120.png")));
		 * dlg.cbM01Colour.setSelectedIndex(WHITE_LIGHT); } else if
		 * (col.equals("R")) { dlg.lM02Icon.setIcon(new
		 * ImageIcon(getClass().getResource( "/images/Light_Red_120.png")));
		 * dlg.cbM01Colour.setSelectedIndex(RED_LIGHT); } else if (col.equals("G"))
		 * { dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource(
		 * "/images/Light_Green_120.png")));
		 * dlg.cbM01Colour.setSelectedIndex(GREEN_LIGHT); } else {
		 * dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource(
		 * "/images/Light_Magenta_120.png")));
		 * dlg.cbM01Colour.setSelectedIndex(UNKNOWN_COLOUR); }
		 * 
		 * c = getLightChar(); dlg.cbM01Kennung.setSelectedItem(c); if
		 * (c.contains("+")) { i1 = c.indexOf("+"); tmp = c.substring(i1,
		 * c.length()); c = c.substring(0, i1); if (!getLightGroup().isEmpty()) { c
		 * = c + "(" + getLightGroup() + ")"; } if (tmp != null) c = c + tmp;
		 * dlg.cbM01Kennung.setSelectedItem(c); } else if
		 * (!getLightGroup().isEmpty()) c = c + "(" + getLightGroup() + ")"; if
		 * (dlg.cbM01Kennung.getSelectedIndex() == 0)
		 * dlg.cbM01Kennung.setSelectedItem(c); c = c + " " + getLightColour(); lp =
		 * getLightPeriod(); if (!lp.isEmpty()) c = c + " " + lp + "s";
		 * dlg.lM01FireMark.setText(c); dlg.cM01Fired.setVisible(true);
		 * dlg.lM01Kennung.setVisible(true); dlg.cbM01Kennung.setVisible(true); if
		 * (((String) dlg.cbM01Kennung.getSelectedItem()).contains("(")) {
		 * dlg.tfM01Group.setVisible(false); dlg.lM01Group.setVisible(false); } else
		 * { dlg.lM01Group.setVisible(true); dlg.tfM01Group.setVisible(true); }
		 * dlg.tfM01Group.setText(getLightGroup());
		 * dlg.lM01RepeatTime.setVisible(true);
		 * dlg.tfM01RepeatTime.setVisible(true); if (isSectored()) {
		 * dlg.rbM01Fired1.setSelected(false); dlg.rbM01FiredN.setSelected(true); if
		 * ((getSectorIndex() != 0) && (!LightChar[0].isEmpty()))
		 * dlg.cbM01Kennung.setEnabled(false); else
		 * dlg.cbM01Kennung.setEnabled(true);
		 * dlg.cbM01Kennung.setSelectedItem(getLightChar()); if ((getSectorIndex()
		 * != 0) && (!LightGroup[0].isEmpty())) dlg.tfM01Group.setEnabled(false);
		 * else dlg.tfM01Group.setEnabled(true);
		 * dlg.tfM01Group.setText(getLightGroup()); if ((getSectorIndex() != 0) &&
		 * (!LightPeriod[0].isEmpty())) dlg.tfM01RepeatTime.setEnabled(false); else
		 * dlg.tfM01RepeatTime.setEnabled(true);
		 * dlg.tfM01RepeatTime.setText(getLightPeriod()); if ((getSectorIndex() !=
		 * 0) && (!Height[0].isEmpty())) dlg.tfM01Height.setEnabled(false); else
		 * dlg.tfM01Height.setEnabled(true); dlg.tfM01Height.setText(getHeight());
		 * if ((getSectorIndex() != 0) && (!Range[0].isEmpty()))
		 * dlg.tfM01Range.setEnabled(false); else dlg.tfM01Range.setEnabled(true);
		 * dlg.tfM01Range.setText(getRange()); dlg.lM01Sector.setVisible(true);
		 * dlg.cbM01Sector.setVisible(true); } else { } } else { } } else {
		 */}

	public void saveSign() {
		Iterator<String> it = dlg.node.getKeys().keySet().iterator();
		String str;

		while (it.hasNext()) {
			str = it.next();
			if (str.contains("seamark"))
				if (!str.equals("seamark")) {
					Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, str, null));
				}
		}
		str = dlg.panelMain.nameBox.getText();
		if (!str.isEmpty())
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:name", str));
		/*
		 * String colour; if (dlg.cM01Fired.isSelected()) { if (!(colour =
		 * LightColour[0]).isEmpty()) if (colour.equals("R")) {
		 * Main.main.undoRedo.add(new ChangePropertyCommand(Node,
		 * "seamark:light:colour", "red")); } else if (colour.equals("G")) {
		 * Main.main.undoRedo.add(new ChangePropertyCommand(Node,
		 * "seamark:light:colour", "green")); } else if (colour.equals("W")) {
		 * Main.main.undoRedo.add(new ChangePropertyCommand(Node,
		 * "seamark:light:colour", "white")); }
		 * 
		 * if (!LightPeriod[0].isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:period", LightPeriod[0]));
		 * 
		 * if (!LightChar[0].isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:character", LightChar[0]));
		 * 
		 * if (!LightGroup[0].isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:group", LightGroup[0]));
		 * 
		 * if (!Height[0].isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:height", Height[0]));
		 * 
		 * if (!Range[0].isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:range", Range[0]));
		 * 
		 * for (int i = 1; i < 10; i++) { if ((colour = LightColour[i]) != null) if
		 * (colour.equals("R")) { Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:" + i + ":colour", "red")); if
		 * ((Bearing1[i] != null) && (Bearing2[i] != null) && (Radius[i] != null))
		 * Main.main.undoRedo.add(new ChangePropertyCommand(Node, "seamark:light:" +
		 * i, "red:" + Bearing1[i] + ":" + Bearing2[i] + ":" + Radius[i])); } else
		 * if (colour.equals("G")) { Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:" + i + ":colour", "green"));
		 * if ((Bearing1[i] != null) && (Bearing2[i] != null) && (Radius[i] !=
		 * null)) Main.main.undoRedo.add(new ChangePropertyCommand(Node,
		 * "seamark:light:" + i, "green:" + Bearing1[i] + ":" + Bearing2[i] + ":" +
		 * Radius[i])); } else if (colour.equals("W")) { Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:" + i + ":colour", "white"));
		 * if ((Bearing1[i] != null) && (Bearing2[i] != null) && (Radius[i] !=
		 * null)) Main.main.undoRedo.add(new ChangePropertyCommand(Node,
		 * "seamark:light:" + i, "white:" + Bearing1[i] + ":" + Bearing2[i] + ":" +
		 * Radius[i])); }
		 * 
		 * if (LightPeriod[i] != null) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:" + i + ":period",
		 * LightPeriod[i]));
		 * 
		 * if (LightChar[i] != null) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:" + i + ":character",
		 * LightChar[i]));
		 * 
		 * if (LightGroup[i] != null) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:" + i + ":group",
		 * LightGroup[i]));
		 * 
		 * if (Height[i] != null) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:" + i + ":height",
		 * Height[i]));
		 * 
		 * if (Range[i] != null) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:" + i + ":range", Range[i]));
		 * 
		 * if (Bearing1[i] != null) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:" + i + ":sector_start",
		 * Bearing1[i]));
		 * 
		 * if (Bearing2[i] != null) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:light:" + i + ":sector_end",
		 * Bearing2[i])); } }
		 * if (hasTopMark()) { Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:topmark:shape", shape));
		 * Main.main.undoRedo.add(new ChangePropertyCommand(Node,
		 * "seamark:topmark:colour", colour)); }
		 * if (hasRadar()) { Main.main.undoRedo.add(new ChangePropertyCommand(Node,
		 * "seamark:radar_reflector", "yes")); } if (hasRacon()) { switch (RaType) {
		 * case RATYPE_RACON: Main.main.undoRedo.add(new ChangePropertyCommand(Node,
		 * "seamark:radar_transponder:category", "racon")); if
		 * (!getRaconGroup().isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:radar_transponder:group",
		 * getRaconGroup())); break; case RATYPE_RAMARK: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:radar_transponder:category",
		 * "ramark")); break; case RATYPE_LEADING: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:radar_transponder:category",
		 * "leading")); break; default: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:radar_transponder", "yes")); } } if
		 * (hasFog()) { if (getFogSound() == 0) { Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:fog_signal", "yes")); } else {
		 * switch (getFogSound()) { case FOG_HORN: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:fog_signal:category", "horn"));
		 * break; case FOG_SIREN: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:fog_signal:category", "siren"));
		 * break; case FOG_DIA: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:fog_signal:category", "diaphone"));
		 * break; case FOG_BELL: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:fog_signal:category", "bell"));
		 * break; case FOG_WHIS: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:fog_signal:category", "whistle"));
		 * break; case FOG_GONG: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:fog_signal:category", "gong"));
		 * break; case FOG_EXPLOS: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:fog_signal:category", "explosive"));
		 * break; } if (!getFogGroup().isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:fog_signal:group", getFogGroup()));
		 * if (!getFogPeriod().isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(Node, "seamark:fog_signal:period",
		 * getFogPeriod())); } }
		 */}

}
