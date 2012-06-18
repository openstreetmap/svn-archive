//License: GPL. For details, see README file.

package org.openstreetmap.josm.plugins.epsg31287;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Point2D;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;

public class ProjectionEPSG31287 implements org.openstreetmap.josm.data.projection.Projection {

	private final double dx;
	private final double dy;
	private final static String projCode = "EPSG:31287";

	private final com.jhlabs.map.proj.Projection projection;

	public ProjectionEPSG31287() {
		this(0,0);
	}

	public ProjectionEPSG31287(double dx, double dy) {
		this.dx = dx;
		this.dy = dy;
		// use use com.jhlabs.map.proj.ProjectionFactory for doing all the math
		projection = com.jhlabs.map.proj.ProjectionFactory.fromPROJ4Specification(
				new String[] {
						"+datum=WGS84"
						,"+proj=lcc"
						,"+lat_1=46.0103424"
						,"+lat_2=48.988621"
						,"+lat_0=47.5"
						,"+lon_0=13.33616275"
						,"+x_0=400268.785"
						,"+y_0=400057.553"
						,"+units=m"
						,"+no_defs"
				}
		);
	}

	@Override
	public double getDefaultZoomInPPD() {
		return 1.01;
	}

	/**
	 * @param LatLon WGS84 (in degree)
	 * @return xy epsg31287 east/north (in meters)
	 */
	@Override
	public EastNorth latlon2eastNorth(LatLon p) {
		Point2D.Double c = new Point2D.Double();
		c.x = p.lon();
		c.y = p.lat();
		//System.out.println("From " + c.x + " " + c.y);
		projection.transform( c, c );
		//System.out.println("To " + c.x + " " + c.y);
		return new EastNorth(c.x+dx, c.y+dy);
	}

	/**
	 * @param xy epsg31287 east/north (in meters)
	 * @return LatLon WGS84 (in degree)
	 */
	@Override
	public LatLon eastNorth2latlon(EastNorth p) {
		Point2D.Double c = new Point2D.Double();
		c.x = p.east()-dx;
		c.y = p.north()-dy;
		//System.out.println("InvFrom " + c.x + " " + c.y);
		projection.inverseTransform( c, c );
		//System.out.println("InvTo " + c.x + " " + c.y);
		return new LatLon(c.y, c.x);
	}

	@Override
	public String toString() {
		return tr(projCode + " - Bessel 1841 in Lambert Conformal Conic");
	}

	@Override
	public String toCode() {
		return projCode;
	}

	@Override
	public String getCacheDirectoryName() {
		return "EPSG_31287";
	}

	@Override
	public Bounds getWorldBoundsLatLon() {
		return new Bounds(new LatLon(45.4, 8.7), new LatLon(49.4, 17.5));
	}

	public static String getProjCode() {
		return projCode;
	}


}
