// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.gui.layer.markerlayer;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Basic marker class. Requires a position, and supports 
 * a custom icon and a name.
 * 
 * This class is also used to create appropriate Marker-type objects
 * when waypoints are imported.
 * 
 * It hosts a public list object, named makers, containing implementations of
 * the MarkerMaker interface. Whenever a Marker needs to be created, each 
 * object in makers is called with the waypoint parameters (Lat/Lon and tag
 * data), and the first one to return a Marker object wins.
 * 
 * By default, one the list contains one default "Maker" implementation that
 * will create AudioMarkers for .wav files, ImageMarkers for .png/.jpg/.jpeg 
 * files, and WebMarkers for everything else. (The creation of a WebMarker will
 * fail if there's no vaild URL in the <link> tag, so it might still make sense
 * to add Makers for such waypoints at the end of the list.)
 * 
 * The default implementation only looks at the value of the <link> tag inside
 * the <wpt> tag of the GPX file.
 * 
 * <h2>HowTo implement a new Marker</h2>
 * <ul>
 * <li> Subclass Marker or ButtonMarker and override <code>containsPoint</code>
 *      if you like to respond to user clicks</li>
 * <li> Override paint, if you want a custom marker look (not "a label and a symbol")</li>
 * <li> Implement MarkerCreator to return a new instance of your marker class</li>
 * <li> In you plugin constructor, add an instance of your MarkerCreator
 *      implementation either on top or bottom of Marker.markerProducers.
 *      Add at top, if your marker should overwrite an current marker or at bottom
 *      if you only add a new marker style.</li>
 * </ul>
 * 
 * @author Frederik Ramm <frederik@remote.org>
 */
public class Marker implements ActionListener {

	public final EastNorth eastNorth;
	public final String text;
	public final Icon symbol;

	/**
	 * Plugins can add their Marker creation stuff at the bottom or top of this list
	 * (depending on whether they want to override default behaviour or just add new
	 * stuff).
	 */
	public static LinkedList<MarkerProducers> markerProducers = new LinkedList<MarkerProducers>();

	// Add one Maker specifying the default behaviour.
	static {
		Marker.markerProducers.add(new MarkerProducers() {
			public Marker createMarker(LatLon ll, Map<String,String> data, File relativePath) {
				String link = data.get("link");

				// Try a relative file:// url, if the link is not in an URL-compatible form
				if (relativePath != null && link != null && !isWellFormedAddress(link))
					link = new File(relativePath, link).toURI().toString();

				if (link == null)
					return new Marker(ll, data.get("name"), data.get("symbol"));
				if (link.endsWith(".wav"))
					return AudioMarker.create(ll, link);
				else if (link.endsWith(".png") || link.endsWith(".jpg") || link.endsWith(".jpeg") || link.endsWith(".gif"))
					return ImageMarker.create(ll, link);
				else
					return WebMarker.create(ll, link);
			}

			private boolean isWellFormedAddress(String link) {
				try {
					new URL(link);
					return true;
				} catch (MalformedURLException x) {
					return false;
				}
            }
		});
	}

	public Marker(LatLon ll, String text, String iconName) {
		eastNorth = Main.proj.latlon2eastNorth(ll); 
		this.text = text;
		Icon symbol = ImageProvider.getIfAvailable("markers",iconName);
		if (symbol == null)
			symbol = ImageProvider.getIfAvailable("symbols",iconName);
		if (symbol == null)
			symbol = ImageProvider.getIfAvailable("nodes",iconName);
		this.symbol = symbol;
	}

	/**
	 * Checks whether the marker display area contains the given point.
	 * Markers not interested in mouse clicks may always return false.
	 * 
	 * @param p The point to check
	 * @return <code>true</code> if the marker "hotspot" contains the point.
	 */
	public boolean containsPoint(Point p) {
		return false;
	}

	/**
	 * Called when the mouse is clicked in the marker's hotspot. Never
	 * called for markers which always return false from containsPoint.
	 * 
	 * @param ev A dummy ActionEvent
	 */
	public void actionPerformed(ActionEvent ev) {
	}

	/**
	 * Paints the marker.
	 * @param g graphics context
	 * @param mv map view
	 * @param mousePressed true if the left mouse button is pressed
	 */
	public void paint(Graphics g, MapView mv, boolean mousePressed, String show) {
		Point screen = mv.getPoint(eastNorth);
		if (symbol != null) {
			symbol.paintIcon(mv, g, screen.x-symbol.getIconWidth()/2, screen.y-symbol.getIconHeight()/2);
		} else {
			g.drawLine(screen.x-2, screen.y-2, screen.x+2, screen.y+2);
			g.drawLine(screen.x+2, screen.y-2, screen.x-2, screen.y+2);
		}

		if ((text != null) && (show.equalsIgnoreCase("show")))
			g.drawString(text, screen.x+4, screen.y+2);
	}

	/**
	 * Returns an object of class Marker or one of its subclasses
	 * created from the parameters given.
	 *
	 * @param ll lat/lon for marker
	 * @param data hash containing keys and values from the GPX waypoint structure
	 * @param relativePath An path to use for constructing relative URLs or 
	 *        <code>null</code> for no relative URLs
	 * @return a new Marker object
	 */
	public static Marker createMarker(LatLon ll, HashMap<String,String> data, File relativePath) {
		for (MarkerProducers maker : Marker.markerProducers) {
			Marker marker = maker.createMarker(ll, data, relativePath);
			if (marker != null)
				return marker;
		}
		return null;
	}
}
