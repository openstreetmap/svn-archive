package buildings_tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.preferences.BooleanProperty;

public class ToolSettings {
    public static BooleanProperty PROP_USE_ADDR_NODE = new BooleanProperty("buildings_tools.addrNode", false);
    private static double width = 0;
    private static double lenstep = 0;
    private static boolean useAddr;
    private static final Map<String, String> tags = new HashMap<String, String>();
    private static boolean autoSelect;

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
        return tags;
    }

    public static void saveTags() {
        ArrayList<String> values = new ArrayList<String>(tags.size() * 2);
        for (Entry<String, String> entry : tags.entrySet()) {
            values.add(entry.getKey());
            values.add(entry.getValue());
        }
        Main.pref.putCollection("buildings_tools.tags", values);
    }

    private static void loadTags() {
        tags.clear();
        Collection<String> values = Main.pref.getCollection("buildings_tools.tags",
                Arrays.asList(new String[] { "building", "yes" }));
        try {
            for (Iterator<String> iterator = values.iterator(); iterator.hasNext();) {
                tags.put(iterator.next(), iterator.next());
            }
        } catch (NoSuchElementException e) {
        }

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
