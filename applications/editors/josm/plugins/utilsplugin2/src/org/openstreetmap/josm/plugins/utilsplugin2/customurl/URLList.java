// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.customurl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.utilsplugin2.UtilsPlugin2;
import org.openstreetmap.josm.tools.Logging;

public final class URLList {
    public static final String defaultURL = "http://osm.mapki.com/history/{#type}.php?id={#id}";

    private URLList() {
        // Hide default constructor for utilities classes
    }

    public static String getSelectedURL() {
        getURLList();
        return Main.pref.get("utilsplugin2.customurl", defaultURL);
    }

    public static void select(String url) {
        Main.pref.put("utilsplugin2.customurl", url);
    }

    public static List<String> resetURLList() {
        List<String> items = new ArrayList<>();
        items.add("Wikipedia");
        items.add("https://en.wikipedia.org/w/index.php?search={name}&fulltext=Search");
        items.add("Wikipedia RU");
        items.add(defaultURL);
        items.add("Who Dit It?");
        items.add("http://simon04.dev.openstreetmap.org/whodidit/?zoom=12&lat={#lat}&lon={#lon}&layers=BTT");
        items.add("Keep Right validator");
        items.add("http://keepright.ipax.at/report_map.php?zoom=14&lat={#lat}&lon={#lon}&layers=B0T");
        items.add("Element history [demo, =Ctrl-Shift-H]");
        items.add("https://www.openstreetmap.org/{#type}/{#id}/history");
        items.add("Browse element [demo, =Ctrl-Shift-I]");
        items.add("https://www.openstreetmap.org/{#type}/{#id}");
        Main.pref.putCollection("utilsplugin2.urlHistory", items);
        Main.pref.put("utilsplugin2.customurl", items.get(9));
        return items;
    }

    public static List<String> getURLList() {
        List<String> items = (List<String>) Main.pref.getCollection("utilsplugin2.urlHistory");
        if (items == null || items.isEmpty()) {
            resetURLList();
            items = (List<String>) Main.pref.getCollection("utilsplugin2.urlHistory");
        }
        return items;
    }

    public static void updateURLList(List<String> lst) {
        Main.pref.putCollection("utilsplugin2.urlHistory", lst);
        try {
            Main.pref.save();
        } catch (IOException ex) {
            Logger.getLogger(UtilsPluginPreferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<String> loadURLList() {
        ArrayList<String> items = new ArrayList<>();
        File f = new File(UtilsPlugin2.getInstance().getPluginDir(), "customurl.txt");
        try (BufferedReader fr = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
            String s;
            while ((s = fr.readLine()) != null) {
                items.add(s);
            }
        } catch (IOException e) {
            Logging.error(e);
        }
        return items;
    }

    public static void saveURLList(List<String> items) {
        File f = new File(UtilsPlugin2.getInstance().getPluginDir(), "customurl.txt");
        PrintWriter fw = null;
        try {
            f.getParentFile().mkdirs();
            fw = new PrintWriter(f, "UTF-8");
            for (String s : items) {
                fw.println(s);
            }
        } catch (IOException e) {
            Logging.error(e);
        } finally {
            try {
                if (fw != null)
                    fw.close();
            } catch (Exception e) {
                Logging.warn(e);
            }
        }
    }
}
