package org.openstreetmap.josm.plugins.remotecontrol;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

import javax.swing.JButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.*;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Utility class
 * 
 */
public class Util 
{
    /**
     * Utility method to retrieve the plugin for classes that can't access to the plugin object directly.
     * 
     * @param clazz The plugin class
     * @return The YWMS plugin
     */
    public static Plugin getPlugin(Class<? extends Plugin> clazz)
    {
    	String classname = clazz.getName();
        for (PluginProxy plugin : Main.plugins)
        {
            if(plugin.info.className.equals(classname))
            {
                return (Plugin)plugin.plugin;
            }
        }
        return null;
    }
    
	/** 
	 * Returns the plugin's directory of the plugin
	 * 
	 * @return The directory of the plugin
	 */
	public static String getPluginDir()
	{
		return Main.pref.getPreferencesDir() + "plugins/ywms/";
	}

	/**
	 * Returns the version
	 * @return The version of the application
	 */
	public static Version getVersion()
    {
        PluginInformation info = PluginInformation.getLoaded("ywms");
        if (info == null) 
            return null;

        return new Version(info.version, info.attr.get("Plugin-Date"));
    }

    /**
     * Utility class for displaying versions
     * 
     * @author frsantos
     */
    public static class Version
    {
    	/** The revision */
    	public String revision;
    	/** The build time */
    	public String time;
    	
        /**
         * Constructor
         * @param revision
         * @param time
         */
        public Version(String revision, String time) 
        {
			this.revision = revision;
			this.time = time;
		}
    }
}
