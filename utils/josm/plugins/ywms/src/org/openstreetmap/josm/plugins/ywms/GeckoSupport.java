package org.openstreetmap.josm.plugins.ywms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.openstreetmap.josm.Main;

/**
 * Utility class for Gecko browsers operations
 * 
 * @author frsantos
 */
public class GeckoSupport 
{

	/**
	 * Creates a profile in a gecko browser
	 * @param browserPath The path to the executable 
	 * @param profile The profile to create
	 * @return The browser process that is creating the profile
	 * @throws IOException 
	 */
	public static Process createProfile(String browserPath, String profile) throws IOException 
	{
		ArrayList<String> cmdParams = new ArrayList<String>();
		cmdParams.add( browserPath );
		cmdParams.add("-CreateProfile");
		cmdParams.add( profile );
	
		System.out.println("YWMS::Create Firefox profile CMD:" + cmdParams);
		ProcessBuilder builder = new ProcessBuilder( cmdParams );
	
		// Set Mozilla environment variable to avoid reusing same window if already open
		builder.environment().put("MOZ_NO_REMOTE", "1");
		
		return builder.start();
	}
	
	/**
	 * Browses a URL using a profile.
	 * 
	 * @param browserPath The path to the browser executable
	 * @param profile If not empty, the profile to browse with
	 * @param url The URL to browse
	 * @param dump If true, every page loaded will be dumped to a PPM image
	 * @return The browser process
	 * @throws IOException If error
	 */
	public static Process browse(String browserPath, String profile, String url, boolean dump) throws IOException 
	{
		ArrayList<String> cmdParams = new ArrayList<String>();
		cmdParams.add( browserPath );
		if( profile != null && profile.length() != 0 )
		{
			cmdParams.add("-P");
			cmdParams.add( profile );
		}
		cmdParams.add( url );

		System.out.println("YWMS::Browsing URL CMD:" + cmdParams);
		ProcessBuilder builder = new ProcessBuilder( cmdParams );

		Map<String, String> environment = builder.environment();
		// Set Mozilla environment variables
		// This one avoids reusing same window if already open
		environment.put("MOZ_NO_REMOTE", "1");
		// This one causes Firefox to dump a screenshot of the viewport once the page has been loaded
		if( dump )
			environment.put("MOZ_FORCE_PAINT_AFTER_ONLOAD", System.getProperty("java.io.tmpdir") + "/ywms");
		
		return builder.start();
	}
	
	/**
	 * Browses a URL using the configured profile.
	 * 
	 * @param url The URL to browse
	 * @param dump If true, every page loaded will be dumped to a PPM image
	 * @return The browser process
	 * @throws IOException If error
	 */
	public static Process browse(String url, boolean dump) throws IOException
	{
		return browse(Main.pref.get("ywms.firefox", "firefox"), Main.pref.get("ywms.profile"), url, dump);
	}
}
