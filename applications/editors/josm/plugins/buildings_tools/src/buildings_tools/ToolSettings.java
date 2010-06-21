package buildings_tools;

import org.openstreetmap.josm.Main;

public class ToolSettings {
	private static double width = 0;
	private static double lenstep = 0;
	private static boolean useAddr;
	private static String tag = "yes";

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

	public static void setTag(String newtag) {
		tag = newtag;
	}

	public static String getTag() {
		return tag;
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
}
