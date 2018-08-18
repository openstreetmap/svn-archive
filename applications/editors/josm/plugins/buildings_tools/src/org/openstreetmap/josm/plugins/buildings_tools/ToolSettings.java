// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.buildings_tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

public final class ToolSettings {

    private ToolSettings() {
        // Hide default constructor for utils classes
    }

    public static final BooleanProperty PROP_USE_ADDR_NODE = new BooleanProperty("buildings_tools.addrNode", false);

    public enum Shape {
            CIRCLE, RECTANGLE
    }

    private static Shape shape = loadShape();
    private static double width = 0;
    private static double lenstep = 0;
    private static boolean useAddr;
    private static final Map<String, String> TAGS = new HashMap<>();
    private static boolean autoSelect;

    public static Shape getShape() {
        loadShape();
        return shape;
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
        loadTags();
        return TAGS;
    }

    public static void saveTags(Map<String, String> tags) {
        TAGS.clear();
        TAGS.putAll(tags);
        ArrayList<String> values = new ArrayList<>(TAGS.size() * 2);
        for (Entry<String, String> entry : TAGS.entrySet()) {
            values.add(entry.getKey());
            values.add(entry.getValue());
        }
        Config.getPref().putList("buildings_tools.tags", values);
    }

    private static void loadTags() {
        TAGS.clear();
        Collection<String> values = Config.getPref().getList("buildings_tools.tags",
                Arrays.asList("building", "yes"));
        try {
            for (Iterator<String> iterator = values.iterator(); iterator.hasNext();) {
                TAGS.put(iterator.next(), iterator.next());
            }
        } catch (NoSuchElementException e) {
            Logging.warn(e);
        }
    }

    public static void saveShape(Shape shape) {
        Config.getPref().put("buildings_tool.shape", shape.name());
    }

    private static Shape loadShape() {
        String shape = Config.getPref().get("buildings_tool.shape");
        if (ToolSettings.Shape.CIRCLE.name().equals(shape)) {
            ToolSettings.shape = Shape.CIRCLE;
            return Shape.CIRCLE;
        } else {
            ToolSettings.shape = Shape.RECTANGLE;
            return (Shape.RECTANGLE);
        }
    }

    public static void setBBMode(boolean bbmode) {
        Config.getPref().putBoolean("buildings_tools.bbmode", bbmode);
    }

    public static boolean isBBMode() {
        return Config.getPref().getBoolean("buildings_tools.bbmode", false);
    }

    public static void setSoftCursor(boolean softCursor) {
        Config.getPref().putBoolean("buildings_tools.softcursor", softCursor);
    }

    public static boolean isSoftCursor() {
        return Config.getPref().getBoolean("buildings_tools.softcursor", false);
    }

    public static boolean isAutoSelect() {
        return autoSelect;
    }

    public static void setAutoSelect(boolean _autoSelect) {
        autoSelect = _autoSelect;
    }
}
