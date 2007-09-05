// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.data;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.io.OsmConnection;
import org.openstreetmap.josm.io.XmlWriter;
import org.openstreetmap.josm.tools.Base64;
import org.openstreetmap.josm.tools.XmlObjectParser;

/**
 * This class tweak the Preferences class to provide server side preference settings, as example
 * used in the applet version.
 * 
 * @author Imi
 */
public class ServerSidePreferences extends Preferences {

	private final Connection connection;

	private class Connection extends OsmConnection {
		URL serverUrl;
		public Connection(URL serverUrl) {
			this.serverUrl = serverUrl;
		}
		public String download() {
			try {
				System.out.println("reading preferences from "+serverUrl);
				HttpURLConnection con = (HttpURLConnection)serverUrl.openConnection();
				addAuth(con);
				con.connect();
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				StringBuilder b = new StringBuilder();
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					b.append(line);
					b.append("\n");
				}
				con.disconnect();
				return b.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		public void upload(String s) {
			try {
				URL u = new URL(getPreferencesDir());
				System.out.println("uplaoding preferences to "+u);
				HttpURLConnection con = (HttpURLConnection)u.openConnection();
				con.addRequestProperty("Authorization", "Basic "+Base64.encode(get("osm-server.username")+":"+get("osm-server.password")));
				con.setRequestMethod("POST");
				con.setDoOutput(true);
				con.connect();
				PrintWriter out = new PrintWriter(new OutputStreamWriter(con.getOutputStream()));
				out.println(s);
				out.close();
				con.getInputStream().close();
				con.disconnect();
				JOptionPane.showMessageDialog(Main.parent, tr("Preferences stored on {0}", u.getHost()));
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(Main.parent, tr("Could not upload preferences. Reason: {0}", e.getMessage()));
			}
		}
	}

	public ServerSidePreferences(URL serverUrl) {
		Connection connection = null;
		try {
			connection = new Connection(new URL(serverUrl+"user/preferences"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Main.parent, tr("Could not load preferences from server."));
		}
		this.connection = connection;
	}

	@Override public String getPreferencesDir() {
		return connection.serverUrl.toString();
	}

	/**
	 * Do nothing on load. Preferences are loaded with download().
	 */
	@Override public void load() {
	}

	/**
	 * Do nothing on save. Preferences are uploaded using upload().
	 */
	@Override protected void save() {
	}

	public static class Prop {
		public String key;
		public String value;
	}

	public void download(String userName, String password) {
		resetToDefault();
		if (!properties.containsKey("osm-server.username") && userName != null)
			properties.put("osm-server.username", userName);
		if (!properties.containsKey("osm-server.password") && password != null)
			properties.put("osm-server.password", password);
		Reader in = new StringReader(connection.download());
		try {
			XmlObjectParser.Uniform<Prop> parser = new XmlObjectParser.Uniform<Prop>(in, "tag", Prop.class);
			for (Prop p : parser)
				properties.put(p.key, p.value);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Use this instead of save() for the ServerSidePreferences, since uploads
	 * are costly while save is called often.
	 * 
	 * This is triggered by an explicit menu option.
	 */
	public void upload() {
		StringBuilder b = new StringBuilder("<preferences>\n");
		for (Entry<String, String> p : properties.entrySet()) {
			if (p.getKey().equals("osm-server.password"))
				continue; // do not upload password. It would get stored in plain!
			b.append("<tag key='");
			b.append(XmlWriter.encode(p.getKey()));
			b.append("' value='");
			b.append(XmlWriter.encode(p.getValue()));
			b.append("' />\n");
		}
		b.append("</preferences>");
		connection.upload(b.toString());
	}

	@Override public Collection<Bookmark> loadBookmarks() {
		try {
			Collection<Bookmark> bookmarks;
			BufferedReader in = new BufferedReader(new InputStreamReader(new URL("http://"+connection.serverUrl.getHost()+"/josm/bookmarks").openStream()));
			bookmarks = new LinkedList<Bookmark>();
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
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	@Override public void saveBookmarks(Collection<Bookmark> bookmarks) {
	}
}
