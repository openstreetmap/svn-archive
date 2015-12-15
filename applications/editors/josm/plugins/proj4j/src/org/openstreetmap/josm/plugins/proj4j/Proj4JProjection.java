// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.proj4j;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;

public class Proj4JProjection implements Projection {

    private String crsCode;

    private org.osgeo.proj4j.CoordinateTransform transformToWGS84;
    private org.osgeo.proj4j.CoordinateTransform transformFromWGS84;
    private org.osgeo.proj4j.CoordinateReferenceSystem proj4jCRS;
    private org.osgeo.proj4j.CoordinateReferenceSystem wgs84CRS;

    public Proj4JProjection(String crsCode) {
        this.crsCode = crsCode;

        org.osgeo.proj4j.CRSFactory crsFactory =
                new org.osgeo.proj4j.CRSFactory();
        org.osgeo.proj4j.CoordinateTransformFactory transFactory =
                new org.osgeo.proj4j.CoordinateTransformFactory();

        // Create coordinate reference systems for source and target
        proj4jCRS = crsFactory.createFromName(crsCode);
        wgs84CRS = crsFactory.createFromName("EPSG:4326");

        // Create transformations between CRS
        transformToWGS84 = transFactory.createTransform(proj4jCRS, wgs84CRS);
        transformFromWGS84 = transFactory.createTransform(wgs84CRS, proj4jCRS);
    }

    @Override
    public double getDefaultZoomInPPD() {
        //TODO: this needs to be changed per projection
        return 1.01;
    }

    /**
        * @param LatLon WGS84 (in degree)
        * @return xy east/north (in whatever unit the projection uses, m/ft/deg/etc)
        */
    @Override
    public EastNorth latlon2eastNorth(LatLon p) {
        org.osgeo.proj4j.ProjCoordinate pc1 = new org.osgeo.proj4j.ProjCoordinate(p.lon(), p.lat());
        org.osgeo.proj4j.ProjCoordinate pc2 = new org.osgeo.proj4j.ProjCoordinate();
        //System.out.println("From " + pc1.x + " " + pc1.y);
        transformFromWGS84.transform(pc1, pc2);
        //System.out.println("To " + pc2.x + " " + pc2.y);
        return new EastNorth(pc2.x, pc2.y);
    }

    /**
        * @param xy east/north (in whatever unit the projection uses, m/ft/deg/etc)
        * @return LatLon WGS84 (in degree)
        */
    @Override
    public LatLon eastNorth2latlon(EastNorth p) {
        org.osgeo.proj4j.ProjCoordinate pc1 = new org.osgeo.proj4j.ProjCoordinate(p.east(), p.north());
        org.osgeo.proj4j.ProjCoordinate pc2 = new org.osgeo.proj4j.ProjCoordinate();
        //System.out.println("InvFrom " + pc1.x + " " + pc1.y);
        transformToWGS84.transform(pc1, pc2);
        //System.out.println("InvTo " + pc2.x + " " + pc2.y);
        return new LatLon(pc2.y, pc2.x);
    }

    @Override
    public String toString() {
        // TODO: include description in string
        return tr("Proj4J: {0} selected", crsCode);
    }

    @Override
    public String toCode() {
        return crsCode;
    }

    @Override
    public String getCacheDirectoryName() {
        return "Proj4J";
    }

    @Override
    public ProjectionBounds getWorldBoundsBoxEastNorth() {
      // TODO: Check if this method does, what it should do. For now it's just a workaround to fix the failing build.
      // This code is a simplified version of org.openstreetmap.josm.data.projection.AbstractProjection.getWorldBoundsBoxEastNorth()
      Bounds b = getWorldBoundsLatLon();
      // add 4 corners
      ProjectionBounds result = new ProjectionBounds(latlon2eastNorth(b.getMin()));
      result.extend(latlon2eastNorth(b.getMax()));
      result.extend(latlon2eastNorth(new LatLon(b.getMinLat(), b.getMaxLon())));
      result.extend(latlon2eastNorth(new LatLon(b.getMaxLat(), b.getMinLon())));
      // and trace along the outline
      double dLon = (b.getMaxLon() - b.getMinLon()) / 1000;
      double dLat = (b.getMaxLat() - b.getMinLat()) / 1000;
      for (double lon=b.getMinLon(); lon<b.getMaxLon(); lon += dLon) {
          result.extend(latlon2eastNorth(new LatLon(b.getMinLat(), lon)));
          result.extend(latlon2eastNorth(new LatLon(b.getMaxLat(), lon)));
      }
      for (double lat=b.getMinLat(); lat<b.getMaxLat(); lat += dLat) {
          result.extend(latlon2eastNorth(new LatLon(lat, b.getMinLon())));
          result.extend(latlon2eastNorth(new LatLon(lat, b.getMaxLon())));
      }
      return result;
    }

    @Override
    public Bounds getWorldBoundsLatLon() {
        org.osgeo.proj4j.proj.Projection proj = proj4jCRS.getProjection();

        // FIXME: Do we need to convert these coords because of possibly differing datums?
        LatLon min = new LatLon(proj.getMinLatitudeDegrees(), proj.getMinLongitudeDegrees());
        LatLon max = new LatLon(proj.getMaxLatitudeDegrees(), proj.getMaxLongitudeDegrees());
        return new Bounds(min, max, true);
    }

	@Override
	public double getMetersPerUnit() {
		return 1.0 / proj4jCRS.getProjection().getFromMetres();
	}

	@Override
	public boolean switchXY() {
		// FIXME: once PROJ4J exposes interface to this information - expose it
		return false;
	}

}
