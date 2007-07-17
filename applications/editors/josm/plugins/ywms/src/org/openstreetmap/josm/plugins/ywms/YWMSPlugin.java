package org.openstreetmap.josm.plugins.ywms;

import java.io.IOException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * 
 * YWMS server
 * <p>
 * Emulates a primitive WMS server (only GetMap requests) that serves Yahoo!
 * satellite images.
 * <p>
 * This plugin is heavily based on Frederik Ramm <frederik@remote.org> YWMS
 * server in perl (see
 * http://lists.openstreetmap.org/pipermail/dev/2007-January/002814.html), so
 * most of the calculations and documentation is taken from his code. However,
 * this plugin does not need any X server to run, because of a Firefox feature
 * that dumps all loaded pages to PPM files, activated through the environment
 * variable MOZ_FORCE_PAINT_AFTER_ONLOAD, pointing to the directory and prefix
 * of the generated image files.
 * <p>
 * For each incoming request:
 * <ul>
 * <li>starts a firefox instance to render the web page
 * <li>converts the resulting image to jpeg and returns it
 * </ul>
 * <p>
 * This method can theoretically be used to display maps from any web site where
 * the API supports selecting a geographic region AND includes a call to find
 * out the region actually displayed.
 * <p>
 * This is required because most such services have discrete zoom levels, while
 * the WMS request issued by josm expects to receive EXACTLY the coordinates
 * requested in an image of the requested pixel size.
 * <p>
 * The HTML/Javscript used by this server uses the Yahoo! API to display an
 * image depicting the selected area in the best available zoom level and then
 * inquires about the area actually displayed, which will always be larger than
 * what was requested. This information is then written to stdout using the
 * "dump" Javascript command. From there it is read by this server, and used to
 * stretch and cut the resulting browser image to the size requested in the WMS
 * request.
 * <p>
 * To illustrate: <lu>
 * <li>josm says "I want the area (48.5,7.0)-(48.8,7.1) as a 1000x800 pixel
 * image"
 * <li>we ask Yahoo about the best zoom level to display this area on 1000x800
 * and request the image
 * <li>we ask Yahoo about the extents actually displayed and receive the
 * answer: (48.45,6.95)-(48.85,7.15)
 * <li>This is .4 degrees high (we requested .3 degrees high) and .2 degrees
 * wide (we requested .1 wide).
 * <li>We scale the image to 1333x1600 and cut a 1000x800 section from the
 * middle, knowing that this will now be exactly .3 degrees by .1 degrees (minus
 * projection errors of course!) <lu>
 * <p>
 * <br>
 * <b>Implementation note:</b> <lu>
 * <li>Some information is passed from Javascript to Java, so Firefox must be
 * configured with the method "dump" to work. To allow this method in firefox,
 * create or modify the option "browser.dom.window.dump.enabled=true" in
 * "about:config"
 * <p>
 * <li>Also, as firefox must be started and killed once and again, it is
 * recommended to create a profile with the option
 * "browser.sessionstore.resume_from_crash" to false and set other profile to
 * default, so no nag screens are shown. </lu>
 * 
 * @author Francisco R. Santos <frsantos@gmail.com>
 * @author Frederik Ramm <frederik@remote.org>
 * @version 0.2 03/10/2007
 */
public class YWMSPlugin extends Plugin 
{
	/** The HTTP server this plugin launches */
	static HTTPServer server;
    
	/**
	 * Creates the plugin, and starts the HTTP server
	 */
	public YWMSPlugin()
	{
		try
		{
			Util.copy("/resources/ymap.html", "ymap.html");
			Util.copy("/resources/config.html", "config.html");
			restartServer();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
    @Override
	public PreferenceSetting getPreferenceSetting() 
	{
		return new YWMSPreferenceSetting();
	}
	
	/**
	 * Starts or restarts the HTTP server
	 *
	 */
	public void restartServer()
	{
		try
		{
			if( server != null )
				server.stopServer();
			
			int port;
			String strPort = Main.pref.get("ywms.port");
			try
			{
				port = Integer.parseInt( strPort );
			}
			catch(Exception e)
			{
				System.out.println("YWMS::Invalid port '" + strPort + "'. Using default " + HTTPServer.DEFAULT_PORT);
				port = HTTPServer.DEFAULT_PORT;
			}
			server = new HTTPServer(port);
			server.start();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}
