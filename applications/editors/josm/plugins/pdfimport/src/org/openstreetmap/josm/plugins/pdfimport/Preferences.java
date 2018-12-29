/**
 * License: GPL. For details, see LICENSE file.
 */
package org.openstreetmap.josm.plugins.pdfimport;

import org.openstreetmap.josm.spi.preferences.Config;

public class Preferences {

	public enum GuiMode {
		Auto, Expert, Simple
	};

	public static String getLoadDir() {
		return Config.getPref().get(Preferences.prefix + "loadDir");
	}

	public static void setLoadDir(String loadDir) {
		Config.getPref().put(Preferences.prefix + "loadDir", loadDir);
	}

	public static GuiMode getGuiMode() {
		int GuiCode = Config.getPref().getInt(Preferences.prefix + "guiCode", 0);
		switch (GuiCode) {
		case -1:
		case 1:
			return GuiMode.Expert;
		case 2:
			return GuiMode.Simple;
		default:
			if (Config.getPref().getBoolean("expert"))
				return GuiMode.Expert;
			else
				return GuiMode.Simple;
		}
	}

	public static void setGuiCode(int guiCode) {
		Config.getPref().putInt(Preferences.prefix + "guiCode", guiCode);
	}

	public static int getGuiCode() {
		int GuiCode = Config.getPref().getInt(Preferences.prefix + "guiCode", 0);
		return GuiCode;
	}

	public static boolean isLegacyActions() {
		return (Config.getPref().getInt(Preferences.prefix + "guiCode", 0) == -1);
	}

	public static void setMergeNodes(boolean v) {
		Config.getPref().putBoolean(Preferences.prefix + "mergeNodes",v);
	}

	public static boolean isMergeNodes() {
		return Config.getPref().getBoolean(Preferences.prefix + "mergeNodes");
	}

	public static double getMergeNodesValue() {
		return Config.getPref().getDouble(Preferences.prefix + "mergeNodes.value", 1e-3);
	}

	public static void setMergeNodesValue(double v) {
		Config.getPref().putDouble(Preferences.prefix + "mergeNodes.value", v);
	}

	public static boolean isRemoveSmall() {
		return Config.getPref().getBoolean(Preferences.prefix + "removeSmall");
	}

	public static void setRemoveSmall(boolean v) {
		Config.getPref().putBoolean(Preferences.prefix + "removeSmall",v);
	}

	public static double getRemoveSmallValue() {
		return Config.getPref().getDouble(Preferences.prefix + "removeSmall.value", 1);
	}

	public static void setRemoveSmallValue(double v) {
		Config.getPref().putDouble(Preferences.prefix + "removeSmall.value", v);
	}

	public static boolean isRemoveLarge() {
		return Config.getPref().getBoolean(Preferences.prefix + "removeLarge");
	}

	public static void setRemoveLarge(boolean v) {
		Config.getPref().putBoolean(Preferences.prefix + "removeLarge",v);
	}

	public static double getRemoveLargeValue() {
		return Config.getPref().getDouble(Preferences.prefix + "removeLarge.value", 10);
	}

	public static void setRemoveLargeValue(double v) {
		Config.getPref().putDouble(Preferences.prefix + "removeLarge.value", v);
	}

	public static boolean isRemoveParallel() {
		return Config.getPref().getBoolean(Preferences.prefix + "removeParallel");
	}

	public static void setRemoveParallel(boolean v) {
		Config.getPref().putBoolean(Preferences.prefix + "removeParallel",v);
	}

	public static double getRemoveParallelValue() {
		return Config.getPref().getDouble(Preferences.prefix + "removeParallel.value", 3);
	}

	public static void setRemoveParallelValue(double v) {
		Config.getPref().putDouble(Preferences.prefix + "removeParallel.value", v);
	}

	public static boolean isLimitPath() {
		return Config.getPref().getBoolean(Preferences.prefix + "limitPath");
	}

	public static void setLimitPath(boolean v) {
		Config.getPref().putBoolean(Preferences.prefix + "limitPath",v);
	}

	public static int getLimitPathValue() {
		return Config.getPref().getInt(Preferences.prefix + "limitPath.value", Integer.MAX_VALUE);
	}

	public static void setLimitPathValue(int v) {
		Config.getPref().putInt(Preferences.prefix + "limitPath.value", v);
	}

	public static boolean isLimitColor() {
		return Config.getPref().getBoolean(Preferences.prefix + "limitColor");
	}

	public static void setLimitColor(boolean v) {
		Config.getPref().putBoolean(Preferences.prefix + "limitColor",v);
	}

	public static String getLimitColorValue() {
		return Config.getPref().get(Preferences.prefix + "limitColor.value", "#000000");
	}

	public static void setLimitColorValue(String v) {
		Config.getPref().put(Preferences.prefix + "limitColor.value", v);
	}

	public static boolean isDebugTags() {
		return Config.getPref().getBoolean(Preferences.prefix + "debugTags");
	}

	public static void setDebugTags(boolean v) {
		Config.getPref().putBoolean(Preferences.prefix + "debugTags",v);
	}

	public static boolean isLayerClosed() {
		return Config.getPref().getBoolean(Preferences.prefix + "layerClosed");
	}

	public static void setLayerClosed(boolean v) {
		Config.getPref().putBoolean(Preferences.prefix + "layerClosed",v);
	}

	public static boolean isLayerSegment() {
		boolean v =Config.getPref().getBoolean(Preferences.prefix + "layerSegment");
		return Config.getPref().getBoolean(Preferences.prefix + "layerSegment");
	}

	public static void setLayerSegment(boolean v) {
		Config.getPref().putBoolean(Preferences.prefix + "layerSegment",v);
	}

	public static boolean isLayerAttribChange() {
		return Config.getPref().getBoolean(Preferences.prefix + "layerAttribChanges");
	}

	public static void setLayerAttribChange(boolean v) {
		Config.getPref().putBoolean(Preferences.prefix + "layerAttribChanges",v);
	}

	public static boolean isLayerOrtho() {
		return Config.getPref().getBoolean(Preferences.prefix + "layerOrtho");
	}

	public static void setLayerOrtho(boolean v) {
		Config.getPref().putBoolean(Preferences.prefix + "layerOrtho",v);
	}

	protected static int GuiCode;

	private static String prefix;

	private Preferences() {
		return;
	}

	public Preferences(String p) {
		prefix = p + ".";
	}

}
