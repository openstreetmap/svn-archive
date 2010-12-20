package oseam.seamarks;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Node;

import oseam.dialogs.OSeaMAction;

abstract public class SeaMark {

	public enum Type {
		UNKNOWN_TYPE, LATERAL, CARDINAL, SAFE_WATER, ISOLATED_DANGER, SPECIAL_PURPOSE, LIGHT
	}

	public enum Cat {
		UNKNOWN_CAT, PORT_HAND, STARBOARD_HAND, PREF_PORT_HAND, PREF_STARBOARD_HAND, CARD_NORTH, CARD_EAST, CARD_SOUTH, CARD_WEST, LIGHT_HOUSE, LIGHT_MAJOR, LIGHT_MINOR, LIGHT_VESSEL
	}

	public final static boolean IALA_A = false;
	public final static boolean IALA_B = true;

	public enum Styl {
		UNKNOWN_SHAPE, PILLAR, SPAR, CAN, CONE, SPHERE, BARREL, FLOAT, SUPER, BEACON, TOWER, STAKE, PERCH
	}

	public enum Col {
		UNKNOWN_COLOUR, RED, GREEN, RED_GREEN_RED, GREEN_RED_GREEN, RED_WHITE, BLACK_YELLOW, BLACK_YELLOW_BLACK, YELLOW_BLACK, YELLOW_BLACK_YELLOW, BLACK_RED_BLACK, YELLOW
	}

	public final static int WHITE_LIGHT = 1;
	public final static int RED_LIGHT = 2;
	public final static int GREEN_LIGHT = 3;

	/**
	 * Topmark Shapes - correspond to TopMarkIndex
	 */

	public final static int UNKNOWN_TOPMARK = 0;
	public final static int TOP_YELLOW_X = 1;
	public final static int TOP_RED_X = 2;
	public final static int TOP_YELLOW_CAN = 3;
	public final static int TOP_YELLOW_CONE = 4;

	/**
	 * Radar Beacons - correspond to Ratyp Index
	 */

	public final static int UNKNOWN_RATYPE = 0;
	public final static int RATYPE_RACON = 1;
	public final static int RATYPE_RAMARK = 2;
	public final static int RATYPE_LEADING = 3;

	/**
	 * Fog Signals - correspond to FogSound Index
	 */

	public final static int UNKNOWN_FOG = 0;
	public final static int FOG_HORN = 1;
	public final static int FOG_SIREN = 2;
	public final static int FOG_DIA = 3;
	public final static int FOG_BELL = 4;
	public final static int FOG_WHIS = 5;
	public final static int FOG_GONG = 6;
	public final static int FOG_EXPLOS = 7;

	/**
	 * Variables
	 */

	protected OSeaMAction dlg = null;

	public OSeaMAction getDlg() {
		return dlg;
	}

	protected SeaMark(OSeaMAction dia, Node node) {
		dlg = dia;
		this.node = node;
	}

	private Node node = null;

	public Node getNode() {
		return node;
	}

	public void setNode(Node nod) {
		node = nod;
	}

	private boolean region = false;

	public boolean getRegion() {
		return region;
	}

	public void setRegion(boolean reg) {
		region = reg;
	}

	private Col colour = Col.UNKNOWN_COLOUR;

	public Col getColour() {
		return colour;
	}

	public void setColour(Col col) {
		colour = col;
	}

	private String errMsg = null;

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String msg) {
		errMsg = msg;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String nam) {
		name = nam;
	}

	private Cat category = Cat.UNKNOWN_CAT;

	public Cat getCategory() {
		return category;
	}

	public void setCategory(Cat cat) {
		category = cat;
	}

	private Styl shape = Styl.UNKNOWN_SHAPE;

	public Styl getShape() {
		return shape;
	}

	public void setShape(Styl styl) {
		shape = styl;
	}

	private boolean valid = true;

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean val) {
		valid = val;
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

	public void setFogSound(int sound) {
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
				setErrMsg(null);
			} else {
				setErrMsg("Must be a number");
				lightPeriod = "";
//				dlg.tfM01RepeatTime.requestFocus();
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
		setSectorIndex(0);
//		dlg.cbM01Sector.setSelectedIndex(0);
//		dlg.cM01Fired.setSelected(isFired());
//		dlg.rbM01Fired1.setSelected(!isSectored());
//		dlg.rbM01FiredN.setSelected(isSectored());
//		dlg.cbM01Kennung.setSelectedItem(getLightChar());
//		dlg.tfM01Height.setText(getHeight());
//		dlg.tfM01Range.setText(getRange());
//		dlg.tfM01Group.setText(getLightGroup());
//		dlg.tfM01RepeatTime.setText(getLightPeriod());
//		dlg.cbM01Colour.setSelectedItem(getLightColour());
	}

	public void parseFogRadar(Map<String, String> k) {
		String str;
		setFog(false);
		setRadar(false);
		setRacon(false);
		if (k.containsKey("seamark:fog_signal")
				|| k.containsKey("seamark:fog_signal:category")
				|| k.containsKey("seamark:fog_signal:group")
				|| k.containsKey("seamark:fog_signal:period")) {
			setFog(true);
			if (k.containsKey("seamark:fog_signal:category")) {
				str = k.get("seamark:fog_signal:category");
				if (str.equals("horn"))
					setFogSound(FOG_HORN);
				else if (str.equals("siren"))
					setFogSound(FOG_SIREN);
				else if (str.equals("diaphone"))
					setFogSound(FOG_DIA);
				else if (str.equals("bell"))
					setFogSound(FOG_BELL);
				else if (str.equals("whis"))
					setFogSound(FOG_WHIS);
				else if (str.equals("gong"))
					setFogSound(FOG_GONG);
				else if (str.equals("explosive"))
					setFogSound(FOG_EXPLOS);
				else
					setFogSound(UNKNOWN_FOG);
			}
			if (k.containsKey("seamark:fog_signal:group"))
				setFogGroup(k.get("seamark:fog_signal:group"));
			if (k.containsKey("seamark:fog_signal:period"))
				setFogPeriod(k.get("seamark:fog_signal:period"));
		}
//		dlg.cM01Fog.setSelected(hasFog());
//		dlg.cbM01Fog.setSelectedIndex(getFogSound());
//		dlg.tfM01FogGroup.setText(getFogGroup());
//		dlg.tfM01FogPeriod.setText(getFogPeriod());

		if (k.containsKey("seamark:radar_transponder")
				|| k.containsKey("seamark:radar_transponder:category")
				|| k.containsKey("seamark:radar_transponder:group")) {
			setRacon(true);
			if (k.containsKey("seamark:radar_transponder:category")) {
				str = k.get("seamark:radar_transponder:category");
				if (str.equals("racon"))
					setRaType(RATYPE_RACON);
				else if (str.equals("ramark"))
					setRaType(RATYPE_RAMARK);
				else if (str.equals("leading"))
					setRaType(RATYPE_LEADING);
				else
					setRaType(UNKNOWN_RATYPE);
			}
			if (k.containsKey("seamark:radar_transponder:group"))
				setRaconGroup(k.get("seamark:radar_transponder:group"));
		} else if (k.containsKey("seamark:radar_reflector"))
			setRadar(true);
//		dlg.cM01Radar.setSelected(hasRadar());
//		dlg.cM01Racon.setSelected(hasRacon());
//		dlg.cbM01Racon.setSelectedIndex(getRaType());
//		dlg.tfM01Racon.setText(getRaconGroup());
	}

	public abstract void paintSign();

	public void saveSign(String type) {
		delSeaMarkKeys(node);

		String str = dlg.panelMain.nameBox.getText();
		if (!str.isEmpty())
			Main.main.undoRedo.add(new ChangePropertyCommand(node,
					"seamark:name", str));
		Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:type",
				type));
	}

	protected void saveLightData() {
/*		String colour;
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
*/	}

	protected void saveTopMarkData(String shape, String colour) {
/*		if (hasTopMark()) {
			Main.main.undoRedo.add(new ChangePropertyCommand(Node,
					"seamark:topmark:shape", shape));
			Main.main.undoRedo.add(new ChangePropertyCommand(Node,
					"seamark:topmark:colour", colour));
		}
*/	}

	protected void saveRadarFogData() {
/*		if (hasRadar()) {
			Main.main.undoRedo.add(new ChangePropertyCommand(Node,
					"seamark:radar_reflector", "yes"));
		}
		if (hasRacon()) {
			switch (RaType) {
			case RATYPE_RACON:
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:radar_transponder:category", "racon"));
				if (!getRaconGroup().isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:radar_transponder:group", getRaconGroup()));
				break;
			case RATYPE_RAMARK:
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:radar_transponder:category", "ramark"));
				break;
			case RATYPE_LEADING:
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:radar_transponder:category", "leading"));
				break;
			default:
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:radar_transponder", "yes"));
			}
		}
		if (hasFog()) {
			if (getFogSound() == 0) {
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:fog_signal", "yes"));
			} else {
				switch (getFogSound()) {
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
							"seamark:fog_signal:group", getFogGroup()));
				if (!getFogPeriod().isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:fog_signal:period", getFogPeriod()));
			}
		}
*/	}

	protected void delSeaMarkKeys(Node node) {
		Iterator<String> it = node.getKeys().keySet().iterator();
		String str;

		while (it.hasNext()) {
			str = it.next();

			if (str.contains("seamark") == true)
				if (str.compareTo("seamark") != 0) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, str,
							null));
				}
		}
	}

}
