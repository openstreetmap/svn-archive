package oseam.seamarks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.table.*;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark.*;

public class Light extends AbstractTableModel {
	
	OSeaMAction dlg;

	private String[] columns = { "Sector", "Start", "End", "Colour",
			"Character", "Group", "Period", "Height", "Range", "Visibility" };
	private ArrayList<Object[]> lights;
	
	public Light(OSeaMAction dia) {
		dlg = dia;
		lights = new ArrayList<Object[]>();
		lights.add(new Object[]{null, null, null, null, null, null, null, null, null, null, null, null});
	}

	public String getColumnName(int col) {
		return columns[col].toString();
	}

	public int getColumnCount() {
		return columns.length;
	}

	public int getRowCount() {
		return lights.size()-1;
	}

	public Object getValueAt(int row, int col) {
		if (col == 0) {
			return row+1;
		} else {
			return ((Object[])lights.get(row+1))[col+1];
		}
	}

	public boolean isCellEditable(int row, int col) {
		return (col > 0);
	}

	public void setValueAt(Object value, int row, int col) {
		((Object[])lights.get(row+1))[col+1] = value;
	}
	
	public void addSector(int idx) {
		lights.add(idx, new Object[]{null, null, null, null, null, null, null, null, null, null, null, null});
	}
	
	public void deleteSector(int idx) {
		lights.remove(idx);
	}
	
	public String getBearing1(int idx) {
		return (String)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 1);
	}

	public void setBearing1(int idx, String bearing) {
		dlg.panelMain.panelLit.panelSector.table.setValueAt(bearing, idx, 1);
	}

	public String getBearing2(int idx) {
		return (String)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 2);
	}

	public void setBearing2(int idx, String bearing) {
		dlg.panelMain.panelLit.panelSector.table.setValueAt(bearing, idx, 2);
	}

	public Col getLightColour(int idx) {
		return (Col)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 3);
	}

	public void setLightColour(int idx, Col col) {
		dlg.panelMain.panelLit.panelSector.table.setValueAt(col, idx, 3);
	}

	public Chr getLightChar(int idx) {
		return (Chr)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 4);
	}

	public void setLightChar(int idx, Chr chr) {
		dlg.panelMain.panelLit.panelSector.table.setValueAt(chr, idx, 4);
	}

	public String getLightGroup(int idx) {
		return (String)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 5);
	}

	public void setLightGroup(int idx, String grp) {
		dlg.panelMain.panelLit.panelSector.table.setValueAt(grp, idx, 5);
	}

	protected void setLightGroup(int idx, Map<String, String> keys) {
		String s = "";
		if (keys.containsKey("seamark:light:group")) {
			s = keys.get("seamark:light:group");
			setLightGroup(0, s);
		}
	}

	public String getLightPeriod(int idx) {
		return (String)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 6);
	}

	public void setLightPeriod(int idx, String prd) {
		String regex = "^[\\d\\s.]+$";

		if (!prd.isEmpty()) {

			Pattern pat = Pattern.compile(regex);
			Matcher matcher = pat.matcher(prd);

			if (matcher.find()) {
				// setErrMsg(null);
			} else {
				// setErrMsg("Must be a number");
				prd = "";
				// dlg.tfM01RepeatTime.requestFocus();
			}
		}
		dlg.panelMain.panelLit.panelSector.table.setValueAt(prd, idx, 6);
	}

	public String getHeight(int idx) {
		return (String)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 7);
	}

	public void setHeight(int idx, String hgt) {
		dlg.panelMain.panelLit.panelSector.table.setValueAt(hgt, idx, 7);
	}

	public String getRange(int idx) {
		return (String)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 8);
	}

	public void setRange(int idx, String rng) {
		dlg.panelMain.panelLit.panelSector.table.setValueAt(rng, idx, 8);
	}

	public enum Vis { UNKNOWN, HIGH, LOW, FAINT, INTEN, UNINTEN, REST, OBS, PARTOBS }
	public static final Map<EnumSet<Vis>, String> VisMAP = new HashMap<EnumSet<Vis>, String>();
	static {
		VisMAP.put(EnumSet.of(Vis.UNKNOWN), "");
		VisMAP.put(EnumSet.of(Vis.HIGH), "high");
		VisMAP.put(EnumSet.of(Vis.LOW), "low");
		VisMAP.put(EnumSet.of(Vis.FAINT), "faint");
		VisMAP.put(EnumSet.of(Vis.INTEN), "intensified");
		VisMAP.put(EnumSet.of(Vis.UNINTEN), "unintensified");
		VisMAP.put(EnumSet.of(Vis.REST), "restricted");
		VisMAP.put(EnumSet.of(Vis.OBS), "obscured");
		VisMAP.put(EnumSet.of(Vis.PARTOBS), "part_obscured");
	}
	
	public Vis getVisibility(int idx) {
		return (Vis)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 9);
	}

	public void setVisibility(int idx, Vis vis) {
		dlg.panelMain.panelLit.panelSector.table.setValueAt(vis, idx, 9);
	}

	public enum Lit { UNKNOWN, VERT, VERT2, VERT3, VERT4, HORIZ, HORIZ2, HORIZ3, HORIZ4,
		UPPER, LOWER, REAR, FRONT, AERO, AIROBS, FOGDET, FLOOD, STRIP, SUBS, SPOT, MOIRE, EMERG, BEAR }
	public static final Map<EnumSet<Lit>, String> LitMAP = new HashMap<EnumSet<Lit>, String>();
	static {
		LitMAP.put(EnumSet.of(Lit.UNKNOWN), "");
	}
		
		public Lit getLightCategory(int idx) {
		return (Lit)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 10);
	}

	public void setLightCategory(int idx, Lit cat) {
		dlg.panelMain.panelLit.panelSector.table.setValueAt(cat, idx, 10);
	}

	public enum Exh { UNKNOWN, H24, DAY, NIGHT, FOG }
	public static final Map<EnumSet<Exh>, String> ExhMAP = new HashMap<EnumSet<Exh>, String>();
	static {
		ExhMAP.put(EnumSet.of(Exh.UNKNOWN), "");
		ExhMAP.put(EnumSet.of(Exh.H24), "24h");
		ExhMAP.put(EnumSet.of(Exh.DAY), "day");
		ExhMAP.put(EnumSet.of(Exh.NIGHT), "night");
		ExhMAP.put(EnumSet.of(Exh.FOG), "fog");
	}
	
	public Exh getExhibition(int idx) {
		return (Exh)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 11);
	}

	public void setExhibition(int idx, Exh exh) {
		dlg.panelMain.panelLit.panelSector.table.setValueAt(exh, idx, 11);
	}

	public String getOrientation(int idx) {
		return (String)dlg.panelMain.panelLit.panelSector.table.getValueAt(idx, 12);
	}

	public void setOrientation(int idx, String ori) {
		dlg.panelMain.panelLit.panelSector.table.setValueAt(ori, idx, 12);
	}

}

