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
		return LightChar[getSectorIndex()];
	}

	public void setLightChar(String lightChar) {
		LightChar[getSectorIndex()] = lightChar;
	}

	private String[] LightColour = new String[10];

	public String getLightColour() {
		return LightColour[getSectorIndex()];
	}

	public void setLightColour(String lightColour) {
		LightColour[getSectorIndex()] = lightColour;
	}

	private String[] LightGroup = new String[10];

	public String getLightGroup() {
		return LightGroup[getSectorIndex()];
	}

	public void setLightGroup(String lightGroup) {
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
		return Height[getSectorIndex()];
	}

	public void setHeight(String height) {
		Height[getSectorIndex()] = height;
	}

	private String[] Range = new String[10];

	public String getRange() {
		return Range[getSectorIndex()];
	}

	public void setRange(String range) {
		Range[getSectorIndex()] = range;
	}

	private String[] LightPeriod = new String[10];

	public String getLightPeriod() {
		return LightPeriod[getSectorIndex()];
	}

	public void setLightPeriod(String lightPeriod) {
		String regex = "^[\\d\\s.]+$";

		if (lightPeriod.length() == 0)
			lightPeriod = " ";

		Pattern pat = Pattern.compile(regex);
		Matcher matcher = pat.matcher(lightPeriod);

		if (matcher.find()) {
			LightPeriod[getSectorIndex()] = lightPeriod;
			setErrMsg(null);
		} else {
			setErrMsg("Must be a number");
			dlg.tfM01RepeatTime.requestFocus();
		}
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

	public void parseLights(Map<String, String> k) {
    Iterator it = k.entrySet().iterator();
    while (it.hasNext()) {
        String key = (String)((Map.Entry)it.next()).getKey();
        if (key.contains("seamark:light:")) {
          String value = ((String)((Map.Entry)it.next()).getValue()).trim();
        	
        }
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

	public void paintSign() {

		if (dlg.paintlock)
			return;
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
				if (col.equals("W"))
					dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource(
							"/images/Light_White_120.png")));
				else if (col.equals("R"))
					dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource(
							"/images/Light_Red_120.png")));
				else if (col.equals("G"))
					dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource(
							"/images/Light_Green_120.png")));
				else
					dlg.lM02Icon.setIcon(new ImageIcon(getClass().getResource(
							"/images/Light_Magenta_120.png")));

				dlg.cbM01Kennung.setEnabled(true);

				c = getLightChar();
				if (dlg.cbM01Kennung.getSelectedIndex() == 0) {
					dlg.tfM01Group.setEnabled(false);
					dlg.tfM01RepeatTime.setEnabled(false);
				} else {
					dlg.tfM01Group.setEnabled(true);
					dlg.tfM01RepeatTime.setEnabled(true);
				}

				if (c.contains("+")) {
					i1 = c.indexOf("+");
					tmp = c.substring(i1, c.length());
					c = c.substring(0, i1);
				}

				if (!getLightGroup().equals(""))
					c = c + "(" + getLightGroup() + ")";
				if (tmp != null)
					c = c + tmp;

				c = c + " " + getLightColour();
				lp = getLightPeriod();
				if (!lp.equals(""))
					c = c + " " + lp + "s";
				dlg.lM01FireMark.setText(c);
			} else {
				dlg.tfM01RepeatTime.setEnabled(false);
				dlg.cbM01Kennung.setEnabled(false);
				dlg.lM01FireMark.setText("");
			}
		} else {
			dlg.bM01Save.setEnabled(false);
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
	}

	public void saveSign(String type) {
		delSeaMarkKeys(Node);

		String str = dlg.tfM01Name.getText();
		if (str.compareTo("") != 0)
			Main.main.undoRedo.add(new ChangePropertyCommand(Node, "seamark:name",
					str));
		Main.main.undoRedo
				.add(new ChangePropertyCommand(Node, "seamark:type", type));
	}

	protected void saveLightData(String colour) {
		if (colour.equals("")) {
			return;
		}

		if (dlg.cM01Fired.isSelected()) {
			if (colour.equals("red")) {
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:colour", "red"));
				setLightColour("R");
			} else if (colour.equals("green")) {
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:colour", "green"));
				setLightColour("G");
			} else if (colour.equals("white")) {
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:colour", "white"));
				setLightColour("W");
			}
			if (getLightPeriod() != "" && getLightPeriod() != " "
					&& getLightChar() != "Q")
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:period", getLightPeriod()));

			if (getLightChar() != "")
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:character", getLightChar()));

			if (getLightGroup() != "")
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:group", getLightGroup()));
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
				if (!getRaconGroup().equals(""))
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
				if (!getFogGroup().equals(""))
					Main.main.undoRedo.add(new ChangePropertyCommand(Node,
							"seamark:fog_group:group", getFogGroup()));
				if (!getFogPeriod().equals(""))
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
		dlg.cbM01StyleOfMark.removeAllItems();
		dlg.cbM01StyleOfMark.setVisible(false);
		dlg.lM01StyleOfMark.setVisible(false);
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
		dlg.rbM01FiredN.setVisible(false);
		setSectored(false);
		setSectorIndex(0);
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
		dlg.tfM02Bearing.setText("");
		dlg.tfM02Bearing.setVisible(false);
		dlg.tfM01Radius.setText("");
		dlg.tfM01Radius.setVisible(false);

		dlg.bM01Save.setEnabled(false);
	}

}
