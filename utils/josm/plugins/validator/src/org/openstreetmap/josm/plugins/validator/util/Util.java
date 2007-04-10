package org.openstreetmap.josm.plugins.validator.util;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginProxy;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Utility class
 * 
 * @author frsantos
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
            if( plugin.info.className.equals(classname) )
            {
                return (Plugin)plugin.plugin;
            }
        }
        
        return null;
    }
    
	/** 
	 * Returns the plugin's directory of a plugin
	 * <p>
	 * Utility method for classes that can't acces the plugin object
	 * 
	 * @param clazz The plugin class to look for
	 * @return The directory of the plugin
	 */
	public static String getStaticPluginDir(Class<? extends Plugin> clazz)
	{
	    Plugin plugin = getPlugin(clazz);
	    return ( plugin != null ) ? plugin.getPluginDir() : null;
	}

	/**
	 * Utility method for creating buttons
	 * @param name The name of the button
	 * @param icon Icon of the button
	 * @param tooltip Tooltip
	 * @param action The action performed when clicking the button
	 * @return The created button
	 */
    public static JButton createButton(String name, String icon, String tooltip, ActionListener action) 
    {
		JButton button = new JButton(tr(name), ImageProvider.get(icon));
		button.setActionCommand(name);
		button.addActionListener(action);
		button.setToolTipText(tr(tooltip));
		button.putClientProperty("help", "Dialog/SelectionList/" + name);
		return button;
	}
    
    
	/**
	 * Returns the version
	 * @return The version of the application
	 */
	public static Version getVersion()
    {
    	String revision;
    	try 
    	{
			revision = loadFile(Util.class.getResource("/resources/REVISION"));
		} 
    	catch (Exception e) 
    	{
			return null;
		}

		Pattern versionPattern = Pattern.compile(".*?Revision: ([0-9]*).*", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
		Matcher match = versionPattern.matcher(revision);
		String version = match.matches() ? match.group(1) : "UNKNOWN";

		Pattern timePattern = Pattern.compile(".*?Last Changed Date: ([^\n]*).*", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
		match = timePattern.matcher(revision);
		String time = match.matches() ? match.group(1) : "UNKNOWN";
		
		return new Version(version, time);
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
    
    
    /**
     * Loads a text file in a String
     * 
     * @param resource The URL of the file
     * @return A String with the file contents
     * @throws IOException when error reading the file
     */
    public static String loadFile(URL resource) throws IOException
    {
    	BufferedReader in = null;
		try 
		{
			in = new BufferedReader(new InputStreamReader(resource.openStream()));
			StringBuilder sb = new StringBuilder();
			for (String line = in.readLine(); line != null; line = in.readLine()) 
			{
				sb.append(line);
				sb.append('\n');
			}
			return sb.toString();
		}
		finally
		{
			if( in != null )
			{
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
}
