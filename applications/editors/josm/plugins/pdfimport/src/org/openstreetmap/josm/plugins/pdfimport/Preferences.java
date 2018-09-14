/**
 * License: GPL. For details, see LICENSE file.
 */
package org.openstreetmap.josm.plugins.pdfimport;

import org.openstreetmap.josm.spi.preferences.Config;

public class Preferences {

    public enum GuiMode {
        Auto, Expert, Simple
    }

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

    public static boolean isLegacyActions() {
        return (Config.getPref().getInt(Preferences.prefix + "guiCode", 0) == -1);
    }

    public static boolean isMergeNodes() {
        return Config.getPref().getBoolean(Preferences.prefix + "mergeNodes");
    }

    public static double getMergeNodesValue() {
        return Config.getPref().getDouble(Preferences.prefix + "mergeNodes.value", 1e-3);
    }

    public static boolean isRemoveSmall() {
        return Config.getPref().getBoolean(Preferences.prefix + "removeSmall");
    }

    public static double getRemoveSmallValue() {
        return Config.getPref().getDouble(Preferences.prefix + "removeSmall.value", 1);
    }

    public static boolean isRemoveLarge() {
        return Config.getPref().getBoolean(Preferences.prefix + "removeLarge");
    }

    public static double getRemoveLargeValue() {
        return Config.getPref().getDouble(Preferences.prefix + "removeLarge.value", 10);
    }

    public static boolean isRemoveParallel() {
        return Config.getPref().getBoolean(Preferences.prefix + "removeParallel");
    }

    public static double getRemoveParallelValue() {
        return Config.getPref().getDouble(Preferences.prefix + "removeParallel.value", 3);
    }

    public static boolean isLimitPath() {
        return Config.getPref().getBoolean(Preferences.prefix + "limitPath");
    }

    public static int getLimitPathValue() {
        return Config.getPref().getInt(Preferences.prefix + "limitPath.value", Integer.MAX_VALUE);
    }

    public static boolean isLimitColor() {
        return Config.getPref().getBoolean(Preferences.prefix + "limitColor");
    }

    public static String getLimitColorValue() {
        return Config.getPref().get(Preferences.prefix + "limitColor.value","#000000");
    }

    public static boolean isDebugTags() {
        return Config.getPref().getBoolean(Preferences.prefix + "debugTags");
    }

    public static boolean isLayerClosed() {
        return Config.getPref().getBoolean(Preferences.prefix + "layerClosed");
    }

    public static boolean isLayerSegment() {
        return Config.getPref().getBoolean(Preferences.prefix + "layerSegment");
    }

    public static boolean isLayerAttribChange() {
        return Config.getPref().getBoolean(Preferences.prefix + "layerAttribChanges");
    }

    public static boolean isLayerOrtho() {
        return Config.getPref().getBoolean(Preferences.prefix + "layerOrtho");
    }

    protected static int GuiCode;

    private static String prefix;

    private Preferences() {
        return;
    }

    public Preferences (String p) {
        prefix = p + "." ;
    }

}
