// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.openstreetmap.josm.Main;


/**
 * This class holds all preferences for JOSM.
 * 
 * Other classes can register their beloved properties here. All properties will be
 * saved upon set-access.
 * 
 * @author imi
 */
public class Preferences {

	public static interface PreferenceChangedListener {
		void preferenceChanged(String key, String newValue);
	}

	/**
	 * Class holding one bookmarkentry.
	 * @author imi
	 */
	public static class Bookmark {
		public String name;
		public double[] latlon = new double[4]; // minlat, minlon, maxlat, maxlon
		@Override public String toString() {
			return name;
		}
	}

	public final ArrayList<PreferenceChangedListener> listener = new ArrayList<PreferenceChangedListener>();

	/**
	 * Map the property name to the property object.
	 */
	protected final SortedMap<String, String> properties = new TreeMap<String, String>();

	/**
	 * Override some values on read. This is intended to be used for technology previews
	 * where we want to temporarily modify things without changing the user's preferences
	 * file.
	 */
	protected static final SortedMap<String, String> override = new TreeMap<String, String>();
	static {
		override.put("osm-server.version", "0.5");
		override.put("osm-server.additional-versions", "");
		override.put("osm-server.url", "http://openstreetmap.gryph.de/api");
		override.put("osm-server.username", "fred@remote.org");
		override.put("osm-server.password", "fredfred");
		override.put("plugins", null);
	}

	/**
	 * Return the location of the user defined preferences file
	 */
	public String getPreferencesDir() {
		if (System.getenv("APPDATA") != null)
			return System.getenv("APPDATA")+"/JOSM/";
		return System.getProperty("user.home")+"/.josm/";
	}

	/**
	 * @return A list of all existing directories, where resources could be stored.
	 */
	public Collection<String> getAllPossiblePreferenceDirs() {
	    LinkedList<String> locations = new LinkedList<String>();
        locations.add(Main.pref.getPreferencesDir());
        String s;
        if ((s = System.getenv("JOSM_RESOURCES")) != null) {
	    	if (!s.endsWith("/") && !s.endsWith("\\"))
	    		s = s + "/";
        	locations.add(s);
        }
        if ((s = System.getProperty("josm.resources")) != null) {
	    	if (!s.endsWith("/") && !s.endsWith("\\"))
	    		s = s + "/";
        	locations.add(s);
        }
       	String appdata = System.getenv("APPDATA");
       	if (System.getenv("ALLUSERSPROFILE") != null && appdata != null && appdata.lastIndexOf("\\") != -1) {
       		appdata = appdata.substring(appdata.lastIndexOf("\\"));
       		locations.add(System.getenv("ALLUSERSPROFILE")+appdata+"/JOSM/");
       	}
       	locations.add("/usr/local/share/josm/");
       	locations.add("/usr/local/lib/josm/");
       	locations.add("/usr/share/josm/");
       	locations.add("/usr/lib/josm/");
	    return locations;
	}


	synchronized public boolean hasKey(final String key) {
		return override.containsKey(key) ? override.get(key) != null : properties.containsKey(key);
	}
	synchronized public String get(final String key) {
		if (override.containsKey(key))
			return override.get(key);
		if (!properties.containsKey(key))
			return "";
		return properties.get(key);
	}
	synchronized public String get(final String key, final String def) {
		if (override.containsKey(key)) 
			return override.get(key);
		final String prop = properties.get(key);
		if (prop == null || prop.equals(""))
			return def;
		return prop;
	}
	synchronized public Map<String, String> getAllPrefix(final String prefix) {
		final Map<String,String> all = new TreeMap<String,String>();
		for (final Entry<String,String> e : properties.entrySet())
			if (e.getKey().startsWith(prefix))
				all.put(e.getKey(), e.getValue());
		for (final Entry<String,String> e : override.entrySet())
			if (e.getKey().startsWith(prefix))
				if (e.getValue() == null)
					all.remove(e.getKey());
				else
					all.put(e.getKey(), e.getValue());
		return all;
	}
	synchronized public boolean getBoolean(final String key) {
		return getBoolean(key, false);
	}
	synchronized public boolean getBoolean(final String key, final boolean def) {
		if (override.containsKey(key))
			return override.get(key) == null ? def : Boolean.parseBoolean(override.get(key));
		return properties.containsKey(key) ? Boolean.parseBoolean(properties.get(key)) : def;
	}


	synchronized public void put(final String key, final String value) {
		if (value == null)
			properties.remove(key);
		else
			properties.put(key, value);
		save();
		firePreferenceChanged(key, value);
	}
	synchronized public void put(final String key, final boolean value) {
		properties.put(key, Boolean.toString(value));
		save();
		firePreferenceChanged(key, Boolean.toString(value));
	}


	private final void firePreferenceChanged(final String key, final String value) {
		for (final PreferenceChangedListener l : listener)
			l.preferenceChanged(key, value);
	}

	/**
	 * Called after every put. In case of a problem, do nothing but output the error
	 * in log.
	 */
	protected void save() {
		try {
			final PrintWriter out = new PrintWriter(new FileWriter(getPreferencesDir() + "preferences"), false);
			for (final Entry<String, String> e : properties.entrySet()) {
				if (!e.getValue().equals(""))
					out.println(e.getKey() + "=" + e.getValue());
			}
			out.close();
		} catch (final IOException e) {
			e.printStackTrace();
			// do not message anything, since this can be called from strange
			// places.
		}		
	}

	public void load() throws IOException {
		properties.clear();
		final BufferedReader in = new BufferedReader(new FileReader(getPreferencesDir()+"preferences"));
		int lineNumber = 0;
		for (String line = in.readLine(); line != null; line = in.readLine(), lineNumber++) {
			final int i = line.indexOf('=');
			if (i == -1 || i == 0)
				throw new IOException("Malformed config file at line "+lineNumber);
			properties.put(line.substring(0,i), line.substring(i+1));
		}
	}

	public final void resetToDefault() {
		properties.clear();
		properties.put("laf", "javax.swing.plaf.metal.MetalLookAndFeel");
		properties.put("projection", "org.openstreetmap.josm.data.projection.Epsg4326");
		properties.put("propertiesdialog.visible", "true");
		properties.put("osm-server.url", "http://www.openstreetmap.org/api");
		save();
	}

	public Collection<Bookmark> loadBookmarks() throws IOException {
		File bookmarkFile = new File(getPreferencesDir()+"bookmarks");
		if (!bookmarkFile.exists())
			bookmarkFile.createNewFile();
		BufferedReader in = new BufferedReader(new FileReader(bookmarkFile));

		Collection<Bookmark> bookmarks = new LinkedList<Bookmark>();
		for (String line = in.readLine(); line != null; line = in.readLine()) {
			StringTokenizer st = new StringTokenizer(line, ",");
			if (st.countTokens() < 5)
				continue;
			Bookmark b = new Bookmark();
			b.name = st.nextToken();
			try {
				for (int i = 0; i < b.latlon.length; ++i)
					b.latlon[i] = Double.parseDouble(st.nextToken());
				bookmarks.add(b);
			} catch (NumberFormatException x) {
				// line not parsed
			}
		}
		in.close();
		return bookmarks;
	}

	public void saveBookmarks(Collection<Bookmark> bookmarks) throws IOException {
		File bookmarkFile = new File(Main.pref.getPreferencesDir()+"bookmarks");
		if (!bookmarkFile.exists())
			bookmarkFile.createNewFile();
		PrintWriter out = new PrintWriter(new FileWriter(bookmarkFile));
		for (Bookmark b : bookmarks) {
			b.name.replace(',', '_');
			out.print(b.name+",");
			for (int i = 0; i < b.latlon.length; ++i)
				out.print(b.latlon[i]+(i<b.latlon.length-1?",":""));
			out.println();
		}
		out.close();
	}
}
