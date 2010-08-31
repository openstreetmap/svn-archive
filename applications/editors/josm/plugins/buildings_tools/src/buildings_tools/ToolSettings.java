package buildings_tools;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.Main;

public class ToolSettings {
	private static double width = 0;
	private static double lenstep = 0;
	private static boolean useAddr;
	private static final Map<String, String> tags = new HashMap<String, String>();
	private static boolean autoSelect;

	static {
		tags.put("building", "yes");
	}

	public static void setAddrDialog(boolean _useAddr) {
		useAddr = _useAddr;
	}

	public static void setSizes(double newwidth, double newlenstep) {
		width = newwidth;
		lenstep = newlenstep;
	}

	public static double getWidth() {
		return width;
	}

	public static double getLenStep() {
		return lenstep;
	}

	public static boolean isUsingAddr() {
		return useAddr;
	}

	public static Map<String, String> getTags() {
		return tags;
	}

	public static void setBBMode(boolean bbmode) {
		Main.pref.put("buildings_tools.bbmode", bbmode);
	}

	public static boolean isBBMode() {
		return Main.pref.getBoolean("buildings_tools.bbmode", false);
	}

	public static void setSoftCursor(boolean softCursor) {
		Main.pref.put("buildings_tools.softcursor", softCursor);
	}

	public static boolean isSoftCursor() {
		return Main.pref.getBoolean("buildings_tools.softcursor", false);
	}

	public static boolean isAutoSelect() {
		return autoSelect;
	}

	public static void setAutoSelect(boolean _autoSelect) {
		autoSelect = _autoSelect;
	}
}
