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
		dlg.tbM01Region.setSelected(region);
		dlg.tbM01Region.setText(region ? "IALA-B" : "IALA-A");
	}

	private boolean Fired = false;

	public boolean isFired() {
		return Fired;
	}

	public void setFired(boolean fired) {
		Fired = fired;
		/*
		 * if (dlg.cM01Fired == null) { return; }
		 */
		dlg.cM01Fired.setSelected(fired);

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
		String regex = "^[\\d\\s]+$";

		if (lightPeriod.length() == 0)
			lightPeriod = " ";

		Pattern pat = Pattern.compile(regex);
		Matcher matcher = pat.matcher(lightPeriod);

		if (matcher.find()) {
			LightPeriod = lightPeriod;

			setErrMsg(null);
		} else {
			setErrMsg("Als Periodendauer sind nur Zahlen erlaubt");
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

	public boolean parseShape(Node node, String str) {
		boolean ret = true;
		Map<String, String> keys;

		keys = node.getKeys();
		if (keys.containsKey(str) == false) {
			setErrMsg("Parse-Error: Seezeichen ohne Form");
			return false;
		}
		return ret;
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
		dlg.cbM01TypeOfMark.setEnabled(true);
		dlg.cbM01StyleOfMark.setEnabled(true);

		dlg.cbM01TypeOfMark.setSelectedIndex(getBuoyIndex());

		dlg.cM01TopMark.setSelected(hasTopMark());
		dlg.cM01Fired.setSelected(isFired());

		dlg.tfM01RepeatTime.setText(LightPeriod);

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
			dlg.tfM01FireMark.setText(c);
		} else {
			dlg.tfM01RepeatTime.setEnabled(false);
			dlg.cbM01Kennung.setEnabled(false);
			dlg.tfM01FireMark.setText("");
		}

		dlg.bM01Save.setEnabled(true);

		dlg.lM01Icon01.setIcon(null);
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
		if (colour.compareTo("") == 0) {
			return;
		}

		if (dlg.cM01Fired.isSelected()) {
			if (colour == "red") {
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:colour", "red"));
				setLightColour("R");
			} else if (colour == "green") {
				Main.main.undoRedo.add(new ChangePropertyCommand(Node,
						"seamark:light:colour", "green"));
				setLightColour("G");
			} else if (colour == "white") {
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

	public boolean parseShape(Node node) {
		return false;
	}

	public void refreshStyles() {
	}

	public void refreshLights() {
	}

}
