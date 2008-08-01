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
	 * Returns the plugin's directory of the plugin
	 * 
	 * @return The directory of the plugin
	 */
	public static String getPluginDir()
	{
		return Main.pref.getPreferencesDir() + "plugins/ywms/";
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
        PluginInformation info = PluginInformation.getLoaded("ywms");
        if( info == null )
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
    
    /**
     * Mirrors a file to a local file.
     * <p>
     * The file mirrored is only downloaded if it has been more than one day since last download
     * 
     * @param url The URL of the remote file
     * @param destDir The destionation dir of the mirrored file
     * @param maxTime The time interval, in seconds, to check if the file changed. If less than 0, it defaults to 1 week 
     * @return The local file
     */
    public static File mirror(URL url, String destDir, long maxTime)
    {
        if( url.getProtocol().equals("file") )
            return new File(url.toString() ) ;
        
        String localPath = Main.pref.get("tests.mirror." + url);
        File oldFile = null;
        if( localPath != null && localPath.length() > 0)
        {
            StringTokenizer st = new StringTokenizer(localPath, ";");
            long checkDate = Long.parseLong(st.nextToken());
            localPath = st.nextToken();
            oldFile = new File(localPath);
            maxTime = (maxTime <= 0) ? 7 * 24 * 60 * 60 * 1000 : maxTime * 1000;
            if( System.currentTimeMillis() - checkDate < maxTime )
            {
                if( oldFile.exists() )
                    return oldFile;
            }
        }

        File destDirFile = new File(destDir);
        if( !destDirFile.exists() )
            destDirFile.mkdirs();

        localPath = destDir + System.currentTimeMillis() + "-" + new File(url.getPath()).getName();
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try 
        {
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(5000);
            bis = new BufferedInputStream(conn.getInputStream());
            bos = new BufferedOutputStream( new FileOutputStream(localPath) );
            byte[] buffer = new byte[4096];
            int length;
            while( (length = bis.read( buffer )) > -1 )
            {
                bos.write( buffer, 0, length );
            }
        }
        catch(IOException ioe)
        {
            if( oldFile != null )
                return oldFile;
            else
                return null;
        }
        finally
        {
            if( bis != null )
            {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if( bos != null )
            {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        Main.pref.put("tests.mirror." + url, System.currentTimeMillis() + ";" + localPath);
        
        if( oldFile != null )
            oldFile.delete();

        return new File(localPath);
    }
    
	/**
	 * Copies the ressource 'from' to the file in the plugin directory named 'to'.
	 * @param from The source directory
	 * @param to The destination directory
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public static void copy(String from, String to) throws FileNotFoundException, IOException {
		File pluginDir = new File(getPluginDir());
		if (!pluginDir.exists())
			pluginDir.mkdirs();
    	FileOutputStream out = new FileOutputStream(getPluginDir()+to);
    	InputStream in = Util.class.getResourceAsStream(from);
    	byte[] buffer = new byte[8192];
    	for(int len = in.read(buffer); len > 0; len = in.read(buffer))
    		out.write(buffer, 0, len);
    	in.close();
    	out.close();
    }
}
