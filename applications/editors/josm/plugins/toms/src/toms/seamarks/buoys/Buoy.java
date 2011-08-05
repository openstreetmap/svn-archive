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

	private String Longname = "";

	public String getLongname() {
		return Longname;
	}

	public void setLongname(String name) {
		Longname = name;
	}

	private String Fixme = "";

	public String getFixme() {
		return Fixme;
	}

	public void setFixme(String name) {
		Fixme = name;
	}

	private String LMheight = "";

	public String getLMheight() {
		return LMheight;
	}

	public void setLMheight(String height) {
		LMheight = height;
	}

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

	public void setRaconGroup(String group) {
		RaconGroup = group;
	}

	private String RaconPeriod = "";

	public String getRaconPeriod() {
		return RaconPeriod;
	}

	public void setRaconPeriod(String period) {
		RaconPeriod = period;
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

	private String LitRef = "";

	public String getLitRef() {
		return LitRef;
	}

	public void setLitRef(String ref) {
		LitRef = ref;
	}

	private String LitInf = "";

	public String getLitInf() {
		return LitInf;
	}

	public void setLitInf(String inf) {
		LitInf = inf;
	}

	private String LitCat = "";

	public String getLitCat() {
		return LitCat;
	}

	public void setLitCat(String cat) {
		LitCat = cat;
	}

	private String LitMul = "";

	public String getLitMul() {
		return LitMul;
	}

	public void setLitMul(String mul) {
		LitMul = mul;
	}

	private boolean Sectored = false;

	public boolean isSectored() {
		return Sectored;
	}

	public void setSectored(boolean sectored) {
		Sectored = sectored;
		if (!sectored) {
			setSectorIndex(0);
			setLightChar("");
			setLightColour("");
			setLightGroup("");
			setHeight("");
			setRange("");
			setBearing1("");
			setBearing2("");
			setRadius("");
			setSeq("");
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
		Height[SectorIndex] = height;
	}

	private String[] Range = new String[10];

	public String getRange() {
		if (Range[SectorIndex] == null)
			return (Range[0]);
		return Range[SectorIndex];
	}

	public void setRange(String range) {
		Range[SectorIndex] = range;
	}

	private String[] Sequence = new String[10];

	public String getSeq() {
		if (Sequence[SectorIndex] == null)
			return (Sequence[0]);
		return Sequence[SectorIndex];
	}

	public void setSeq(String seq) {
		Sequence[SectorIndex] = seq;
	}

	private String[] Bearing1 = new String[10];

	public String getBearing1() {
		if (Bearing1[SectorIndex] == null)
			return (Bearing1[0]);
		return Bearing1[SectorIndex];
	}

	public void setBearing1(String bearing) {
		Bearing1[SectorIndex] = bearing;
	}

	private String[] Bearing2 = new String[10];

	public String getBearing2() {
		if (Bearing2[SectorIndex] == null)
			return (Bearing2[0]);
		return Bearing2[SectorIndex];
	}

	public void setBearing2(String bearing) {
		Bearing2[SectorIndex] = bearing;
	}

	private String[] Radius = new String[10];

	public String getRadius() {
		if (Radius[SectorIndex] == null)
			return (Radius[0]);
		return Radius[SectorIndex];
	}

	public void setRadius(String radius) {
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
				dlg.tfM01RepeatTime.requestFocus();
			}
		}
		LightPeriod[SectorIndex] = lightPeriod;
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

	private int TopMarkIndex = 0;

	public int getTopMarkIndex() {
		return TopMarkIndex;
	}

	public void setTopMarkIndex(int index) {
		TopMarkIndex = index;
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
					else if (values[0].equals("yellow"))
						LightColour[index] = "Y";
					if (values.length > 1)
						Bearing1[index] = values[1];
					if (values.length > 2)
						Bearing2[index] = values[2];
					if (values.length > 3)
						Radius[index] = String.valueOf((((Integer.parseInt(values[3])*100)+50)/278)/100.0);
				} else {
					index = 0;
				}
				if (index != 0)
					setSectored(true);
				if (key.equals("ref"))
					setLitRef(value);
				if (key.equals("information"))
					setLitInf(value);
				if (key.equals("category"))
					setLitCat(value);
				if (key.equals("multiple"))
					setLitMul(value);
				if (key.equals("colour")) {
					if (value.equals("red"))
						LightColour[index] = "R";
					else if (value.equals("green"))
						LightColour[index] = "G";
					else if (value.equals("white"))
						LightColour[index] = "W";
					else if (value.equals("yellow"))
						LightColour[index] = "Y";
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
				} else if (key.equals("sequence")) {
					Sequence[index] = value;
				} else if (key.equals("sector_start")) {
					Bearing1[index] = value;
				} else if (key.equals("sector_end")) {
					Bearing2[index] = value;
				} else if (key.equals("radius")) {
					Radius[index] = value;
				}
			}
		}
		setSectorIndex(0);
		dlg.cbM01Sector.setSelectedIndex(0);
		dlg.cM01Fired.setSelected(isFired());
		dlg.rbM01Fired1.setSelected(!isSectored());
		dlg.rbM01FiredN.setSelected(isSectored());
		dlg.cbM01Kennung.setSelectedItem(getLightChar());
		dlg.tfM01Height.setText(getHeight());
		dlg.tfM01Range.setText(getRange());
		dlg.tfM01Group.setText(getLightGroup());
		dlg.tfM01RepeatTime.setText(getLightPeriod());
		dlg.cbM01Colour.setSelectedItem(getLightColour());
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
		dlg.cM01Fog.setSelected(hasFog());
		dlg.cbM01Fog.setSelectedIndex(getFogSound());
		dlg.tfM01FogGroup.setText(getFogGroup());
		dlg.tfM01FogPeriod.setText(getFogPeriod());

		if (k.containsKey("seamark:radar_transponder")
				|| k.containsKey("seamark:radar_transponder:category")
				|| k.containsKey("seamark:radar_transponder:period")
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
			if (k.containsKey("seamark:radar_transponder:period"))
				setRaconPeriod(k.get("seamark:radar_transponder:period"));
		} else if (k.containsKey("seamark:radar_reflector"))
			setRadar(true);
		dlg.cM01Radar.setSelected(hasRadar());
		dlg.cM01Racon.setSelected(hasRacon());
		dlg.cbM01Racon.setSelectedIndex(getRaType());
		dlg.tfM01Racon.setText(getRaconGroup());
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
		dlg.lM06Icon.setIcon(null);
		dlg.lM01NameMark.setText("");
		dlg.lM01FireMark.setText("");
		dlg.lM01FogMark.setText("");
		dlg.lM01RadarMark.setText("");

		dlg.rbM01RegionA.setSelected(!getRegion());
		dlg.rbM01RegionB.setSelected(getRegion());

		if (isValid()) {
			dlg.lM01NameMark.setText(getName());

			dlg.bM01Save.setEnabled(true);

			dlg.cM01TopMark.setSelected(hasTopMark());
			dlg.cM01Fired.setSelected(isFired());

			dlg.tfM01RepeatTime.setText(getLightPeriod());

			dlg.tfM01Name.setText(getName());
			dlg.tfM01Name.setEnabled(true);

			if (hasRadar()) {
				dlg.lM03Icon.setIcon(new ImageIcon(getClass().getResource(
						"/images/Radar_Reflector_355.png")));
			}

			else if (hasRacon()) {
				dlg.lM04Icon.setIcon(new ImageIcon(getClass().getResource(
						"/images/Radar_Station.png")));
				if (getRaType() != 0) {
					String c = (String) dlg.cbM01Racon.getSelectedItem();
					if ((getRaType() == RATYPE_RACON) && !getRaconGroup().isEmpty())
						c += ("(" + getRaconGroup() + ")");
					dlg.lM01RadarMark.setText(c);
				}
				dlg.cbM01Racon.setVisible(true);
				if (getRaType() == RATYPE_RACON) {
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
				if (getFogSound() != 0) {
					String c = (String) dlg.cbM01Fog.getSelectedItem();
					if (!getFogGroup().isEmpty())
						c += ("(" + getFogGroup() + ")");
					if (!getFogPeriod().isEmpty())
						c += (" " + getFogPeriod() + "s");
					dlg.lM01FogMark.setText(c);
				}
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

				c = getLightChar();
				dlg.cbM01Kennung.setSelectedItem(c);
				if (c.contains("+")) {
					i1 = c.indexOf("+");
					tmp = c.substring(i1, c.length());
					c = c.substring(0, i1);
					if (!getLightGroup().isEmpty()) {
						c = c + "(" + getLightGroup() + ")";
					}
					if (tmp != null)
						c = c + tmp;
					dlg.cbM01Kennung.setSelectedItem(c);
				} else if (!getLightGroup().isEmpty())
					c = c + "(" + getLightGroup() + ")";
				if (dlg.cbM01Kennung.getSelectedIndex() == 0)
					dlg.cbM01Kennung.setSelectedItem(c);
				c = c + " " + getLightColour();
				lp = getLightPeriod();
				if (!lp.isEmpty())
					c = c + " " + lp + "s";
				dlg.lM01FireMark.setText(c);
				dlg.cM01Fired.setVisible(true);
				dlg.lM01Kennung.setVisible(true);
				dlg.cbM01Kennung.setVisible(true);
				if (((String) dlg.cbM01Kennung.getSelectedItem()).contains("(")) {
					dlg.tfM01Group.setVisible(false);
					dlg.lM01Group.setVisible(false);
				} else {
					dlg.lM01Group.setVisible(true);
					dlg.tfM01Group.setVisible(true);
				}
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
			Main.main.undoRedo.add(new ChangePropertyCommand(Node, "seamark:name", str));
		if (!Longname.isEmpty())
			Main.main.undoRedo.add(new ChangePropertyCommand(Node, "seamark:longname", Longname));
		if (!Fixme.isEmpty())
			Main.main.undoRedo.add(new ChangePropertyCommand(Node, "seamark:fixme", Fixme));
		Main.main.undoRedo
				.add(new ChangePropertyCommand(Node, "seamark:type", type));
	}

	protected void saveLightData() {
		String colour;
		if (dlg.cM01Fired.isSelected()) {
			if (!(colour = LightColour[0]).isEmpty()) {
				if (colour.equals("R")) {
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:colour", "red"));
				} else if (colour.equals("G")) {
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:colour", "green"));
				} else if (colour.equals("W")) {
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:colour", "white"));
				} else if (colour.equals("Y")) {
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:colour", "yellow"));
				}
			}

			if (!LitRef.isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:ref", LitRef));

			if (!LitInf.isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:information", LitInf));

			if (!LitCat.isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:category", LitCat));

			if (!LitMul.isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:multiple", LitMul));

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

			if (!Sequence[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:sequence", Sequence[0]));

			if (!Radius[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:radius", Radius[0]));

			for (int i = 1; i < 10; i++) {

				if (LightColour[i] != null) {
					if (LightColour[i].equals("R")) {
						Main.main.undoRedo.add(new ChangePropertyCommand(Node,
								"seamark:light:" + i + ":colour", "red"));
						}
					else if (LightColour[i].equals("G")) {
						Main.main.undoRedo.add(new ChangePropertyCommand(Node,
								"seamark:light:" + i + ":colour", "green"));
						}
					else if (LightColour[i].equals("W")) {
						Main.main.undoRedo.add(new ChangePropertyCommand(Node,
								"seamark:light:" + i + ":colour", "white"));
						}
					else if (LightColour[i].equals("Y")) {
						Main.main.undoRedo.add(new ChangePropertyCommand(Node,
								"seamark:light:" + i + ":colour", "yellow"));
						}
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

				if (Sequence[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":sequence", Sequence[i]));

				if (Range[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":range", Range[i]));

				if (Bearing1[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":sector_start", Bearing1[i]));

				if (Bearing2[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":sector_end", Bearing2[i]));
				
				if (Radius[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:light:" + i + ":radius", Radius[i]));
			}
		}
	}

	protected void saveTopMarkData(String shape, String colour) {
		if (hasTopMark()) {
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
			case RATYPE_RACON:
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:radar_transponder:category", "racon"));
				if (!getRaconGroup().isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:radar_transponder:group", getRaconGroup()));
				if (!getRaconPeriod().isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:radar_transponder:period", getRaconPeriod()));
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
		dlg.lM05Icon.setIcon(null);
		dlg.lM06Icon.setIcon(null);

		dlg.rbM01RegionA.setEnabled(false);
		dlg.rbM01RegionB.setEnabled(false);
		dlg.lM01FireMark.setText("");
		dlg.cbM01CatOfMark.setVisible(false);
		dlg.lM01CatOfMark.setVisible(false);
		setBuoyIndex(0);
		dlg.cbM01StyleOfMark.setVisible(false);
		dlg.lM01StyleOfMark.setVisible(false);
		setStyleIndex(0);
		dlg.tfM01Name.setText("");
		dlg.tfM01Name.setEnabled(false);
		setName("");
		setLongname("");
		setFixme("");
		setLitRef("");
		setLitInf("");
		setLitCat("");
		setLitMul("");
		dlg.cM01TopMark.setSelected(false);
		dlg.cM01TopMark.setVisible(false);
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
