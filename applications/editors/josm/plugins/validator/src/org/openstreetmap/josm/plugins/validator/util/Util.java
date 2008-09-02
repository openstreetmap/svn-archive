package org.openstreetmap.josm.plugins.validator.util;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.*;

import javax.swing.JButton;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.*;
import org.openstreetmap.josm.plugins.validator.PreferenceEditor;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Utility class
 *
 * @author frsantos
 */
public class Util
{
	/**
	 * Returns the plugin's directory of the plugin
	 *
	 * @return The directory of the plugin
	 */
	public static String getPluginDir()
	{
		return Main.pref.getPreferencesDir() + "plugins/validator/";
	}

	/**
	 * Returns the version
	 * @return The version of the application
	 */
	public static Version getVersion()
	{
		PluginInformation info = PluginInformation.getLoaded("validator");
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
	 * Returns the start and end cells of a way.
	 * @param w The way
	 * @param cellWays The map with all cells
	 * @return A list with all the cells the way starts or ends
	 */
	public static List<List<Way>> getWaysInCell(Way w, Map<Point2D,List<Way>> cellWays)
	{
		if (w.nodes.size() == 0)
			return Collections.emptyList();

		Node n1 = w.nodes.get(0);
		Node n2 = w.nodes.get(w.nodes.size() - 1);

		List<List<Way>> cells = new ArrayList<List<Way>>(2);
		Set<Point2D> cellNodes = new HashSet<Point2D>();
		Point2D cell;

		// First, round coordinates
		long x0 = Math.round(n1.eastNorth.east()  * 10000);
		long y0 = Math.round(n1.eastNorth.north() * 10000);
		long x1 = Math.round(n2.eastNorth.east()  * 10000);
		long y1 = Math.round(n2.eastNorth.north() * 10000);

		// Start of the way
		cell = new Point2D.Double(x0, y0);
		cellNodes.add(cell);
		List<Way> ways = cellWays.get( cell );
		if( ways == null )
		{
			ways = new ArrayList<Way>();
			cellWays.put(cell, ways);
		}
		cells.add(ways);

		// End of the way
		cell = new Point2D.Double(x1, y1);
		if( !cellNodes.contains(cell) )
		{
			cellNodes.add(cell);
			ways = cellWays.get( cell );
			if( ways == null )
			{
				ways = new ArrayList<Way>();
				cellWays.put(cell, ways);
			}
			cells.add(ways);
		}

		// Then floor coordinates, in case the way is in the border of the cell.
		x0 = (long)Math.floor(n1.eastNorth.east()  * 10000);
		y0 = (long)Math.floor(n1.eastNorth.north() * 10000);
		x1 = (long)Math.floor(n2.eastNorth.east()  * 10000);
		y1 = (long)Math.floor(n2.eastNorth.north() * 10000);

		// Start of the way
		cell = new Point2D.Double(x0, y0);
		if( !cellNodes.contains(cell) )
		{
			cellNodes.add(cell);
			ways = cellWays.get( cell );
			if( ways == null )
			{
				ways = new ArrayList<Way>();
				cellWays.put(cell, ways);
			}
			cells.add(ways);
		}

		// End of the way
		cell = new Point2D.Double(x1, y1);
		if( !cellNodes.contains(cell) )
		{
			cellNodes.add(cell);
			ways = cellWays.get( cell );
			if( ways == null )
			{
				ways = new ArrayList<Way>();
				cellWays.put(cell, ways);
			}
			cells.add(ways);
		}

		return cells;
	}

	/**
	 * Returns the coordinates of all cells in a grid that a line between 2
	 * nodes intersects with.
	 *
	 * @param n1 The first node.
	 * @param n2 The second node.
	 * @param gridDetail The detail of the grid. Bigger values give smaller
	 * cells, but a bigger number of them.
	 * @return A list with the coordinates of all cells
	 */
	public static List<Point2D> getSegmentCells(Node n1, Node n2, int gridDetail)
	{
		List<Point2D> cells = new ArrayList<Point2D>();
		double x0 = n1.eastNorth.east() * gridDetail;
		double x1 = n2.eastNorth.east() * gridDetail;
		double y0 = n1.eastNorth.north() * gridDetail + 1;
		double y1 = n2.eastNorth.north() * gridDetail + 1;

		if( x0 > x1 )
		{
			// Move to 1st-4th cuadrants
			double aux;
			aux = x0; x0 = x1; x1 = aux;
			aux = y0; y0 = y1; y1 = aux;
		}

		double dx  = x1 - x0;
		double dy  = y1 - y0;
		long stepY = y0 <= y1 ? 1 : -1;
		long gridX0 = (long)Math.floor(x0);
		long gridX1 = (long)Math.floor(x1);
		long gridY0 = (long)Math.floor(y0);
		long gridY1 = (long)Math.floor(y1);

		long maxSteps = (gridX1 - gridX0) + Math.abs(gridY1 - gridY0) + 1;
		while( (gridX0 <= gridX1 && (gridY0 - gridY1)*stepY <= 0) && maxSteps-- > 0)
		{
			cells.add( new Point2D.Double(gridX0, gridY0) );

			// Is the cross between the segment and next vertical line nearer than the cross with next horizontal line?
			// Note: segment line formula: y=dy/dx(x-x1)+y1
			// Note: if dy < 0, must use *bottom* line. If dy > 0, must use upper line
			double scanY = dy/dx * (gridX0 + 1 - x1) + y1 + (dy < 0 ? -1 : 0);
			double scanX = dx/dy * (gridY0 + (dy < 0 ? 0 : 1)*stepY - y1) + x1;

			double distX = Math.pow(gridX0 + 1 - x0, 2) + Math.pow(scanY - y0, 2);
			double distY = Math.pow(scanX - x0, 2) + Math.pow(gridY0 + stepY - y0, 2);

			if( distX < distY)
				gridX0 += 1;
			else
				gridY0 += stepY;
		}

		return cells;
	}
}
