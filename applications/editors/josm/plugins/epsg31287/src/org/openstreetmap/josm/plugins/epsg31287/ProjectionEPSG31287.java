//License: GPL. For details, see README file.

package org.openstreetmap.josm.plugins.epsg31287;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.GBC;

public class ProjectionEPSG31287 implements org.openstreetmap.josm.data.projection.Projection, org.openstreetmap.josm.data.projection.ProjectionSubPrefs {

	private double dx = 0.0;
	private double dy = 0.0;
	private final static String projCode = "EPSG:31287";

	private final com.jhlabs.map.proj.Projection projection;

	public ProjectionEPSG31287() {
		super();
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

	// PreferencePanel, not used in plugin mode, useful for JOSM custom-build
	@Override
	public void setupPreferencePanel(JPanel p, ActionListener actionListener) {
		//p.add(new HtmlPanel("<i>EPSG:31287 - Bessel 1841 in Lambert_Conformal_Conic_2SP</i>"), GBC.eol().fill(GBC.HORIZONTAL));
		//p.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.BOTH));
		p.setLayout(new GridBagLayout());

		JTextField gdx = new JTextField(""+dx);
		JTextField gdy = new JTextField(""+dy);

		p.add(new JLabel(tr("dx")), GBC.std().insets(5,5,0,5));
		p.add(GBC.glue(1, 0), GBC.std().fill(GBC.HORIZONTAL));
		p.add(gdx, GBC.eop().fill(GBC.HORIZONTAL));

		p.add(new JLabel(tr("dy")), GBC.std().insets(5,5,0,5));
		p.add(GBC.glue(1, 0), GBC.std().fill(GBC.HORIZONTAL));
		p.add(gdy, GBC.eop().fill(GBC.HORIZONTAL));
		p.add(GBC.glue(1, 1), GBC.eol().fill(GBC.BOTH));

	}

	// for PreferencePanel, not used in plugin mode, useful for JOSM custom-build
	@Override
	public Collection<String> getPreferences(JPanel p) {
		dx = new Double(((JTextField)p.getComponent(2)).getText());
		dy = new Double(((JTextField)p.getComponent(5)).getText());
		return Arrays.asList(""+dx,""+dy);
	}

	// for PreferencePanel, not used in plugin mode, useful for JOSM custom-build
	@Override
	public Collection<String> getPreferencesFromCode(String code) {
		dx = 85.0;
		dy = 45.0;
		return null;
	}

	// for PreferencePanel, not used in plugin mode, useful for JOSM custom-build
	@Override
	public void setPreferences(Collection<String> args) {
		if(args != null)
		{
			String[] array = args.toArray(new String[0]);
			if (array.length > 0) {
				try {
					dx = Double.parseDouble(array[0]);
				} catch(NumberFormatException e) {}
			}
			if (array.length > 1) {
				try {
					dy = Double.parseDouble(array[1]);
				} catch(NumberFormatException e) {}
			}
		}
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

	@Override
	public String[] allCodes() {
		return new String[] {projCode};
	}

}
