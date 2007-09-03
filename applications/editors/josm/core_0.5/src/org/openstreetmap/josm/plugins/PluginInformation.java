// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.plugins;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.openstreetmap.josm.Main;

/**
 * Encapsulate general information about a plugin. This information is available
 * without the need of loading any class from the plugin jar file.
 *
 * @author imi
 */
public class PluginInformation {
	
	public final File file;
	public final String name;
	public final String className;
	public final String description;
	public final boolean early;
	public final String author;
	public final int stage;
	public final String version;
	public final List<URL> libraries = new LinkedList<URL>();

	public final Map<String, String> attr = new TreeMap<String, String>();

	/**
	 * Used in the Plugin constructor to make the information of the plugin
	 * that is currently initializing available.
	 * 
	 * If you think this is hacky, you are probably right. But it is 
	 * convinient anyway ;-) 
	 */
	static PluginInformation currentPluginInitialization = null;

	/**
	 * @param file the plugin jar file.
	 */
	public PluginInformation(File file) {
		this(file, file.getName().substring(0, file.getName().length()-4), null);
	}

	public PluginInformation(File file, String name, InputStream manifestStream) {
		this.name = name;
		this.file = file;
		try {
			Manifest manifest;
			JarInputStream jar = null;
			if (file != null) {
				jar = new JarInputStream(new FileInputStream(file));
				manifest = jar.getManifest();
				if (manifest == null)
					throw new IOException(file+" contains no manifest.");
			} else {
				manifest = new Manifest();
		        manifest.read(manifestStream);
			}
			if (manifest != null) {
				Attributes attr = manifest.getMainAttributes();
				className = attr.getValue("Plugin-Class");
				description = attr.getValue("Plugin-Description");
				early = Boolean.parseBoolean(attr.getValue("Plugin-Early"));
				String stageStr = attr.getValue("Plugin-Stage");
				stage = stageStr == null ? 50 : Integer.parseInt(stageStr);
				version = attr.getValue("Plugin-Version");
				author = attr.getValue("Author");

				String classPath = attr.getValue(Attributes.Name.CLASS_PATH);
				if (classPath != null) {
					String[] cp = classPath.split(" ");
					StringBuilder entry = new StringBuilder();
					for (String s : cp) {
						entry.append(s);
						if (s.endsWith("\\")) {
							entry.setLength(entry.length()-1);
							entry.append("%20"); // append the split character " " as html-encode
							continue;
						}
						s = entry.toString();
						entry = new StringBuilder();
						if (!s.startsWith("/") && !s.startsWith("\\") && !s.matches("^.\\:") && file != null)
							s = file.getParent() + File.separator + s;
						libraries.add(new URL(getURLString(s)));
					}
				}
				for (Object o : attr.keySet())
					this.attr.put(o.toString(), attr.getValue(o.toString()));
			} else {
				// resource-only plugin
				className = null;
				description = tr("unknown");
				early = false;
				stage = 50;
				version = null;
				author = null;
			}
			if (file != null)
				libraries.add(0, new URL(getURLString(file.getAbsolutePath())));

			if (jar != null)
				jar.close();
		} catch (IOException e) {
			throw new PluginException(null, name, e);
		}
	}

	/**
	 * Load and instantiate the plugin
	 */
	public PluginProxy load(Class<?> klass) {
		try {
			currentPluginInitialization = this;
			return new PluginProxy(klass.newInstance(), this);
		} catch (Exception e) {
			throw new PluginException(null, name, e);
		}
	}

	/**
	 * Load the class of the plugin
	 */
	public Class<?> loadClass(ClassLoader classLoader) {
		if (className == null)
			return null;
		try {
			Class<?> realClass = Class.forName(className, true, classLoader);
			return realClass;
		} catch (Exception e) {
			throw new PluginException(null, name, e);
		}
	}

	public static String getURLString(String fileName) {
		if (System.getProperty("os.name").startsWith("Windows"))
			return "file:/"+fileName;
		return "file://"+fileName;
	}

	/**
	 * Try to find a plugin after some criterias. Extract the plugin-information
	 * from the plugin and return it. The plugin is searched in the following way:
	 * 
	 *<li>first look after an MANIFEST.MF in the package org.openstreetmap.josm.plugins.<plugin name>
	 *    (After removing all fancy characters from the plugin name).
	 *    If found, the plugin is loaded using the bootstrap classloader.
	 *<li>If not found, look for a jar file in the user specific plugin directory
	 *    (~/.josm/plugins/<plugin name>.jar)
	 *<li>If not found and the environment variable JOSM_RESSOURCES + "/plugins/" exist, look there.
	 *<li>Try for the java property josm.ressources + "/plugins/" (set via java -Djosm.plugins.path=...)
	 *<li>If the environment variable ALLUSERSPROFILE and APPDATA exist, look in 
	 *    ALLUSERSPROFILE/<the last stuff from APPDATA>/JOSM/plugins. 
	 *    (*sic* There is no easy way under Windows to get the All User's application 
	 *    directory)
	 *<li>Finally, look in some typical unix paths:<ul> 
	 *    <li>/usr/local/share/josm/plugins/
	 *    <li>/usr/local/lib/josm/plugins/
	 *    <li>/usr/share/josm/plugins/
	 *    <li>/usr/lib/josm/plugins/
	 * 
	 * If a plugin class or jar file is found earlier in the list but seem not to
	 * be working, an PluginException is thrown rather than continuing the search.
	 * This is so JOSM can detect broken user-provided plugins and do not go silently
	 * ignore them. 
	 * 
	 * The plugin is not initialized. If the plugin is a .jar file, it is not loaded
	 * (only the manifest is extracted). In the classloader-case, the class is 
	 * bootstraped (e.g. static {} - declarations will run. However, nothing else is done.
	 *
	 * @param pluginName The name of the plugin (in all lowercase). E.g. "lang-de"
	 * @return Information about the plugin or <code>null</code>, if the plugin
	 * 	       was nowhere to be found.
	 * @throws PluginException In case of broken plugins.
	 */
	public static PluginInformation findPlugin(String pluginName) throws PluginException {
    	String name = pluginName;
    	name = name.replaceAll("[-. ]", "");
    	InputStream manifestStream = PluginInformation.class.getResourceAsStream("/org/openstreetmap/josm/plugins/"+name+"/MANIFEST.MF");
    	if (manifestStream != null)
	        return new PluginInformation(null, pluginName, manifestStream);

        Collection<String> locations = getPluginLocations();

       	for (String s : locations) {
       		File pluginFile = new File(s+"/"+pluginName+".jar");
       		if (pluginFile.exists()) {
				PluginInformation info = new PluginInformation(pluginFile);
				return info;
       		}
       	}
       	return null;
	}

	public static Collection<String> getPluginLocations() {
	    Collection<String> locations = Main.pref.getAllPossiblePreferenceDirs();
	    Collection<String> all = new ArrayList<String>(locations.size());
	    for (String s : locations)
	    	all.add(s+"plugins");
	    return all;
    }
	
	
	/**
	 * Return information about a loaded plugin.
	 * 
	 * Note that if you call this in your plugins bootstrap, you may get <code>null</code> if
	 * the plugin requested is not loaded yet.
	 * 
	 * @return The PluginInformation to a specific plugin, but only if the plugin is loaded.
	 * If it is not loaded, <code>null</code> is returned.
	 */
	public static PluginInformation getLoaded(String pluginName) {
		for (PluginProxy p : Main.plugins)
			if (p.info.name.equals(pluginName))
				return p.info;
		return null;
	}
}

