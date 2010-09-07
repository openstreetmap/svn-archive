//License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig & Malcolm Herring

package toms.seamarks.buoys;

import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Node;

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

	private int Ratyp = 0;
	
	public int getRatyp() {
		return Ratyp;
	}
	
	public void setRatyp(int typ) {
		Ratyp = typ;
	}
	
	private boolean Fog = false;

	public boolean hasFog() {
		return Fog;
	}

	public void setFog(boolean fog) {
		Fog = fog;
	}

	private boolean Fired = false;

	public boolean isFired() {
		return Fired;
	}

	public void setFired(boolean fired) {
		Fired = fired;
	}

	private String LightChar = "";

	public String getLightChar() {
		return LightChar;
	}

	public void setLightChar(String lightChar) {
		LightChar = lightChar;
	}

	private String LightColour = "";

	public String getLightColour() {
		return LightColour;
	}

	public void setLightColour(String lightColour) {
		LightColour = lightColour;
	}

	private String LightGroup = "";

	public String getLightGroup() {
		return LightGroup;
	}

	public void setLightGroup(String lightGroup) {
		LightGroup = lightGroup;
	}

	protected void setLightGroup(Map<String, String> k) {
		String s = "";

		if (k.containsKey("seamark:light:group")) {
			s = k.get("seamark:light:group");

			LightGroup = s;
		}

	}

	private String Height = "";

	public String getHeight() {
		return Height;
	}

	public void setHeight(String height) {
		Height = height;
	}

	private String Range = "";

	public String getRange() {
		return Range;
	}

	public void setRange(String range) {
		Range = range;
	}

	private String LightPeriod = "";

	public String getLightPeriod() {
		return LightPeriod;
	}

	public void setLightPeriod(String lightPeriod) {
		String regex = "^[\\d\\s.]+$";

		if (lightPeriod.length() == 0)
			lightPeriod = " ";

		Pattern pat = Pattern.compile(regex);
		Matcher matcher = pat.matcher(lightPeriod);

		if (matcher.find()) {
			LightPeriod = lightPeriod;

			setErrMsg(null);
		} else {
			setErrMsg("Must be a number");
			dlg.tfM01RepeatTime.requestFocus();
		}

	}

	protected void setLightPeriod(Map<String, String> k) {
		String s;

		s = "";

		if (k.containsKey("seamark:light:signal:period")) {
			s = k.get("seamark:light:signal:period");
			LightPeriod = s;

			return;
		}

		if (k.containsKey("seamark:light:period")) {
			s = k.get("seamark:light:period");
			LightPeriod = s;

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

	public void paintSign() {

		if (dlg.paintlock) return;
		dlg.lM01Icon.setIcon(null);
		dlg.lM02Icon.setIcon(null);
		dlg.lM03Icon.setIcon(null);
		dlg.lM04Icon.setIcon(null);
		
		dlg.rbM01RegionA.setSelected(!getRegion());
		dlg.rbM01RegionB.setSelected(getRegion());

		dlg.cM01TopMark.setSelected(hasTopMark());
		dlg.cM01Fired.setSelected(isFired());

		dlg.tfM01RepeatTime.setText(LightPeriod);

		dlg.tfM01Name.setText(getName());
		
		if (hasRadar()) {
			dlg.lM03Icon.setIcon(new ImageIcon(getClass().getResource("/images/Radar_Reflector.png")));
		}
		
		if (hasRacon()) {
			dlg.lM04Icon.setIcon(new ImageIcon(getClass().getResource("/images/Radar_Station.png")));
			dlg.cbM01Racon.setVisible(true);
			if (getRatyp() == RATYP_RACON) {
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
		
		if (isFired()) {
			String lp, c;
			String tmp = null;
			int i1;

			dlg.cbM01Kennung.setEnabled(true);

			c = getLightChar();
			if (dlg.cbM01Kennung.getSelectedIndex() == 0)
				dlg.tfM01RepeatTime.setEnabled(false);
			else
				dlg.tfM01RepeatTime.setEnabled(true);

			if (c.contains("+")) {
				i1 = c.indexOf("+");
				tmp = c.substring(i1, c.length());
				c = c.substring(0, i1);
			}

			if (getLightGroup() != "")
				c = c + "(" + getLightGroup() + ")";
			if (tmp != null)
				c = c + tmp;

			c = c + " " + getLightColour();
			lp = getLightPeriod();
			if (lp != "" && lp != " ")
				c = c + " " + lp + "s";
			dlg.lM01FireMark.setText(c);
		} else {
			dlg.tfM01RepeatTime.setEnabled(false);
			dlg.cbM01Kennung.setEnabled(false);
			dlg.lM01FireMark.setText("");
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
			if (LightPeriod != "" && LightPeriod != " " && LightChar != "Q")
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:period", LightPeriod));

			if (LightChar != "")
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:character", LightChar));

			if (LightGroup != "")
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:group", LightGroup));
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

	public void refreshStyles() {
	}

	public void refreshLights() {
	}

	public void resetMask() {
		setRegion(Main.pref.get("tomsplugin.IALA").equals("B"));

		dlg.lM01Icon.setIcon(null);
		dlg.lM02Icon.setIcon(null);
		dlg.lM03Icon.setIcon(null);
		dlg.lM04Icon.setIcon(null);

		dlg.rbM01RegionA.setEnabled(false);
		dlg.rbM01RegionB.setEnabled(false);
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
		setRatyp(0);
		dlg.tfM01Racon.setText("");
		dlg.tfM01Racon.setVisible(false);
		dlg.lM01Racon.setVisible(false);
		setRacon(false);
		dlg.cM01Fog.setSelected(false);
		dlg.cM01Fog.setVisible(false);
		dlg.cbM01Fog.removeAllItems();
		dlg.cbM01Fog.setVisible(false);
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
		dlg.cbM01Kennung.removeAllItems();
		dlg.cbM01Kennung.setVisible(false);
		dlg.lM01Kennung.setVisible(false);
		dlg.tfM01Height.setText("");
		dlg.tfM01Height.setVisible(false);
		dlg.lM01Height.setVisible(false);
		dlg.tfM01Range.setText("");
		dlg.tfM01Range.setVisible(false);
		dlg.lM01Range.setVisible(false);
		dlg.cbM01Colour.setVisible(false);
		dlg.lM01Colour.setVisible(false);
		dlg.cbM01Sector.setVisible(false);
		dlg.lM01Sector.setVisible(false);
		dlg.tfM01Group.setText("");
		dlg.tfM01Group.setVisible(false);
		dlg.lM01Group.setVisible(false);
		dlg.tfM01RepeatTime.setText("");
		dlg.tfM01RepeatTime.setVisible(false);
		dlg.lM01RepeatTime.setVisible(false);
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
