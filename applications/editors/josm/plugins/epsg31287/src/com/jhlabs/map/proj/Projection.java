/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.jhlabs.map.proj;

import java.awt.*;
import java.awt.geom.*;
import com.jhlabs.map.*;

/**
 * The superclass for all map projections
 */
public class Projection implements Cloneable {

	/**
	 * The minimum latitude of the bounds of this projection
	 */
	protected double minLatitude = -Math.PI/2;

	/**
	 * The minimum longitude of the bounds of this projection. This is relative to the projection centre.
	 */
	protected double minLongitude = -Math.PI;

	/**
	 * The maximum latitude of the bounds of this projection
	 */
	protected double maxLatitude = Math.PI/2;

	/**
	 * The maximum longitude of the bounds of this projection. This is relative to the projection centre.
	 */
	protected double maxLongitude = Math.PI;

	/**
	 * The latitude of the centre of projection
	 */
	protected double projectionLatitude = 0.0;

	/**
	 * The longitude of the centre of projection
	 */
	protected double projectionLongitude = 0.0;

	/**
	 * Standard parallel 1 (for projections which use it)
	 */
	protected double projectionLatitude1 = 0.0;

	/**
	 * Standard parallel 2 (for projections which use it)
	 */
	protected double projectionLatitude2 = 0.0;

	/**
	 * The projection scale factor
	 */
	protected double scaleFactor = 1.0;

	/**
	 * The false Easting of this projection
	 */
	protected double falseEasting = 0;

	/**
	 * The false Northing of this projection
	 */
	protected double falseNorthing = 0;

	/**
	 * The latitude of true scale. Only used by specific projections.
	 */
	protected double trueScaleLatitude = 0.0;

	/**
	 * The equator radius
	 */
	protected double a = 0;

	/**
	 * The eccentricity
	 */
	protected double e = 0;

	/**
	 * The eccentricity squared
	 */
	protected double es = 0;

	/**
	 * 1-(eccentricity squared)
	 */
	protected double one_es = 0;

	/**
	 * 1/(1-(eccentricity squared))
	 */
	protected double rone_es = 0;

	/**
	 * The ellipsoid used by this projection
	 */
	protected Ellipsoid ellipsoid;

	/**
	 * True if this projection is using a sphere (es == 0)
	 */
	protected boolean spherical;

	/**
	 * True if this projection is geocentric
	 */
	protected boolean geocentric;

	/**
	 * The name of this projection
	 */
	protected String name = null;

	/**
	 * Conversion factor from metres to whatever units the projection uses.
	 */
	protected double fromMetres = 1;

	/**
	 * The total scale factor = Earth radius * units
	 */
	private double totalScale = 0;

	/**
	 * falseEasting, adjusted to the appropriate units using fromMetres
	 */
	private double totalFalseEasting = 0;

	/**
	 * falseNorthing, adjusted to the appropriate units using fromMetres
	 */
  private double totalFalseNorthing = 0;

	// Some useful constants
	protected final static double EPS10 = 1e-10;
	protected final static double RTD = 180.0/Math.PI;
	protected final static double DTR = Math.PI/180.0;
	
	protected Projection() {
		setEllipsoid( Ellipsoid.SPHERE );
	}
	
	public Object clone() {
		try {
			Projection e = (Projection)super.clone();
			return e;
		}
		catch ( CloneNotSupportedException e ) {
			throw new InternalError();
		}
	}
	
	/**
	 * Project a lat/long point (in degrees), producing a result in metres
	 */
	public Point2D.Double transform( Point2D.Double src, Point2D.Double dst ) {
		double x = src.x*DTR;
		if ( projectionLongitude != 0 )
			x = MapMath.normalizeLongitude( x-projectionLongitude );
		project(x, src.y*DTR, dst);
		dst.x = totalScale * dst.x + totalFalseEasting;
		dst.y = totalScale * dst.y + totalFalseNorthing;
		return dst;
	}

	/**
	 * Project a lat/long point, producing a result in metres
	 */
	public Point2D.Double transformRadians( Point2D.Double src, Point2D.Double dst ) {
		double x = src.x;
		if ( projectionLongitude != 0 )
			x = MapMath.normalizeLongitude( x-projectionLongitude );
		project(x, src.y, dst);
		dst.x = totalScale * dst.x + totalFalseEasting;
		dst.y = totalScale * dst.y + totalFalseNorthing;
		return dst;
	}

	/**
	 * The method which actually does the projection. This should be overridden for all projections.
	 */
	public Point2D.Double project(double x, double y, Point2D.Double dst) {
		dst.x = x;
		dst.y = y;
		return dst;
	}

	/**
	 * Project a number of lat/long points (in degrees), producing a result in metres
	 */
	public void transform(double[] srcPoints, int srcOffset, double[] dstPoints, int dstOffset, int numPoints) {
		Point2D.Double in = new Point2D.Double();
		Point2D.Double out = new Point2D.Double();
		for (int i = 0; i < numPoints; i++) {
			in.x = srcPoints[srcOffset++];
			in.y = srcPoints[srcOffset++];
			transform(in, out);
			dstPoints[dstOffset++] = out.x;
			dstPoints[dstOffset++] = out.y;
		}
	}

	/**
	 * Project a number of lat/long points (in radians), producing a result in metres
	 */
	public void transformRadians(double[] srcPoints, int srcOffset, double[] dstPoints, int dstOffset, int numPoints) {
		Point2D.Double in = new Point2D.Double();
		Point2D.Double out = new Point2D.Double();
		for (int i = 0; i < numPoints; i++) {
			in.x = srcPoints[srcOffset++];
			in.y = srcPoints[srcOffset++];
			transform(in, out);
			dstPoints[dstOffset++] = out.x;
			dstPoints[dstOffset++] = out.y;
		}
	}

	/**
	 * Inverse-project a point (in metres), producing a lat/long result in degrees
	 */
	public Point2D.Double inverseTransform(Point2D.Double src, Point2D.Double dst) {
		double x = (src.x - totalFalseEasting) / totalScale;
		double y = (src.y - totalFalseNorthing) / totalScale;
		projectInverse(x, y, dst);
		if (dst.x < -Math.PI)
			dst.x = -Math.PI;
		else if (dst.x > Math.PI)
			dst.x = Math.PI;
		if (projectionLongitude != 0)
			dst.x = MapMath.normalizeLongitude(dst.x+projectionLongitude);
		dst.x *= RTD;
		dst.y *= RTD;
		return dst;
	}

	/**
	 * Inverse-project a point (in metres), producing a lat/long result in radians
	 */
	public Point2D.Double inverseTransformRadians(Point2D.Double src, Point2D.Double dst) {
		double x = (src.x - totalFalseEasting) / totalScale;
		double y = (src.y - totalFalseNorthing) / totalScale;
		projectInverse(x, y, dst);
		if (dst.x < -Math.PI)
			dst.x = -Math.PI;
		else if (dst.x > Math.PI)
			dst.x = Math.PI;
		if (projectionLongitude != 0)
			dst.x = MapMath.normalizeLongitude(dst.x+projectionLongitude);
		return dst;
	}

	/**
	 * The method which actually does the inverse projection. This should be overridden for all projections.
	 */
	public Point2D.Double projectInverse(double x, double y, Point2D.Double dst) {
		dst.x = x;
		dst.y = y;
		return dst;
	}

	/**
	 * Inverse-project a number of points (in metres), producing a lat/long result in degrees
	 */
	public void inverseTransform(double[] srcPoints, int srcOffset, double[] dstPoints, int dstOffset, int numPoints) {
		Point2D.Double in = new Point2D.Double();
		Point2D.Double out = new Point2D.Double();
		for (int i = 0; i < numPoints; i++) {
			in.x = srcPoints[srcOffset++];
			in.y = srcPoints[srcOffset++];
			inverseTransform(in, out);
			dstPoints[dstOffset++] = out.x;
			dstPoints[dstOffset++] = out.y;
		}
	}

	/**
	 * Inverse-project a number of points (in metres), producing a lat/long result in radians
	 */
	public void inverseTransformRadians(double[] srcPoints, int srcOffset, double[] dstPoints, int dstOffset, int numPoints) {
		Point2D.Double in = new Point2D.Double();
		Point2D.Double out = new Point2D.Double();
		for (int i = 0; i < numPoints; i++) {
			in.x = srcPoints[srcOffset++];
			in.y = srcPoints[srcOffset++];
			inverseTransformRadians(in, out);
			dstPoints[dstOffset++] = out.x;
			dstPoints[dstOffset++] = out.y;
		}
	}

	/**
	 * Finds the smallest lat/long rectangle wholly inside the given view rectangle.
	 * This is only a rough estimate.
	 */
	public Rectangle2D inverseTransform(Rectangle2D r) {
		Point2D.Double in = new Point2D.Double();
		Point2D.Double out = new Point2D.Double();
		Rectangle2D bounds = null;
		if (isRectilinear()) {
			for (int ix = 0; ix < 2; ix++) {
				double x = r.getX()+r.getWidth()*ix;
				for (int iy = 0; iy < 2; iy++) {
					double y = r.getY()+r.getHeight()*iy;
					in.x = x;
					in.y = y;
					inverseTransform(in, out);
					if (ix == 0 && iy == 0)
						bounds = new Rectangle2D.Double(out.x, out.y, 0, 0);
					else
						bounds.add(out.x, out.y);
				}
			}
		} else {
			for (int ix = 0; ix < 7; ix++) {
				double x = r.getX()+r.getWidth()*ix/6;
				for (int iy = 0; iy < 7; iy++) {
					double y = r.getY()+r.getHeight()*iy/6;
					in.x = x;
					in.y = y;
					inverseTransform(in, out);
					if (ix == 0 && iy == 0)
						bounds = new Rectangle2D.Double(out.x, out.y, 0, 0);
					else
						bounds.add(out.x, out.y);
				}
			}
		}
		return bounds;
	}
	
	/**
	 * Transform a bounding box. This is only a rough estimate.
	 */
	public Rectangle2D transform( Rectangle2D r ) {
		Point2D.Double in = new Point2D.Double();
		Point2D.Double out = new Point2D.Double();
		Rectangle2D bounds = null;
		if ( isRectilinear() ) {
			for (int ix = 0; ix < 2; ix++) {
				double x = r.getX()+r.getWidth()*ix;
				for (int iy = 0; iy < 2; iy++) {
					double y = r.getY()+r.getHeight()*iy;
					in.x = x;
					in.y = y;
					transform(in, out);
					if (ix == 0 && iy == 0)
						bounds = new Rectangle2D.Double(out.x, out.y, 0, 0);
					else
						bounds.add(out.x, out.y);
				}
			}
		} else {
			for (int ix = 0; ix < 7; ix++) {
				double x = r.getX()+r.getWidth()*ix/6;
				for (int iy = 0; iy < 7; iy++) {
					double y = r.getY()+r.getHeight()*iy/6;
					in.x = x;
					in.y = y;
					transform(in, out);
					if (ix == 0 && iy == 0)
						bounds = new Rectangle2D.Double(out.x, out.y, 0, 0);
					else
						bounds.add(out.x, out.y);
				}
			}
		}
		return bounds;
	}
	
	/**
	 * Returns true if this projection is conformal
	 */
	public boolean isConformal() {
		return false;
	}
	
	/**
	 * Returns true if this projection is equal area
	 */
	public boolean isEqualArea() {
		return false;
	}
	
	/**
	 * Returns true if this projection has an inverse
	 */
	public boolean hasInverse() {
		return false;
	}

	/**
	 * Returns true if lat/long lines form a rectangular grid for this projection
	 */
	public boolean isRectilinear() {
		return false;
	}

	/**
	 * Returns true if latitude lines are parallel for this projection
	 */
	public boolean parallelsAreParallel() {
		return isRectilinear();
	}

	/**
	 * Returns true if the given lat/long point is visible in this projection
	 */
	public boolean inside(double x, double y) {
		x = normalizeLongitude( (float)(x*DTR-projectionLongitude) );
		return minLongitude <= x && x <= maxLongitude && minLatitude <= y && y <= maxLatitude;
	}

	/**
	 * Set the name of this projection.
	 */
	public void setName( String name ) {
		this.name = name;
	}
	
	public String getName() {
		if ( name != null )
			return name;
		return toString();
	}

	/**
	 * Get a string which describes this projection in PROJ.4 format.
	 */
	public String getPROJ4Description() {
		AngleFormat format = new AngleFormat( AngleFormat.ddmmssPattern, false );
		StringBuffer sb = new StringBuffer();
		sb.append(
			"+proj="+getName()+
			" +a="+a
		);
		if ( es != 0 )
			sb.append( " +es="+es );
		sb.append( " +lon_0=" );
		format.format( projectionLongitude, sb, null );
		sb.append( " +lat_0=" );
		format.format( projectionLatitude, sb, null );
		if ( falseEasting != 1 )
			sb.append( " +x_0="+falseEasting );
		if ( falseNorthing != 1 )
			sb.append( " +y_0="+falseNorthing );
		if ( scaleFactor != 1 )
			sb.append( " +k="+scaleFactor );
		if ( fromMetres != 1 )
			sb.append( " +fr_meters="+fromMetres );
		return sb.toString();
	}

	public String toString() {
		return "None";
	}

	/**
	 * Set the minimum latitude. This is only used for Shape clipping and doesn't affect projection.
	 */
	public void setMinLatitude( double minLatitude ) {
		this.minLatitude = minLatitude;
	}
	
	public double getMinLatitude() {
		return minLatitude;
	}

	/**
	 * Set the maximum latitude. This is only used for Shape clipping and doesn't affect projection.
	 */
	public void setMaxLatitude( double maxLatitude ) {
		this.maxLatitude = maxLatitude;
	}
	
	public double getMaxLatitude() {
		return maxLatitude;
	}

	public double getMaxLatitudeDegrees() {
		return maxLatitude*RTD;
	}

	public double getMinLatitudeDegrees() {
		return minLatitude*RTD;
	}

	public void setMinLongitude( double minLongitude ) {
		this.minLongitude = minLongitude;
	}
	
	public double getMinLongitude() {
		return minLongitude;
	}

	public void setMinLongitudeDegrees( double minLongitude ) {
		this.minLongitude = DTR*minLongitude;
	}
	
	public double getMinLongitudeDegrees() {
		return minLongitude*RTD;
	}

	public void setMaxLongitude( double maxLongitude ) {
		this.maxLongitude = maxLongitude;
	}
	
	public double getMaxLongitude() {
		return maxLongitude;
	}

	public void setMaxLongitudeDegrees( double maxLongitude ) {
		this.maxLongitude = DTR*maxLongitude;
	}
	
	public double getMaxLongitudeDegrees() {
		return maxLongitude*RTD;
	}

	/**
	 * Set the projection latitude in radians.
	 */
	public void setProjectionLatitude( double projectionLatitude ) {
		this.projectionLatitude = projectionLatitude;
	}
	
	public double getProjectionLatitude() {
		return projectionLatitude;
	}
	
	/**
	 * Set the projection latitude in degrees.
	 */
	public void setProjectionLatitudeDegrees( double projectionLatitude ) {
		this.projectionLatitude = DTR*projectionLatitude;
	}
	
	public double getProjectionLatitudeDegrees() {
		return projectionLatitude*RTD;
	}
	
	/**
	 * Set the projection longitude in radians.
	 */
	public void setProjectionLongitude( double projectionLongitude ) {
		this.projectionLongitude = normalizeLongitudeRadians( projectionLongitude );
	}
	
	public double getProjectionLongitude() {
		return projectionLongitude;
	}
	
	/**
	 * Set the projection longitude in degrees.
	 */
	public void setProjectionLongitudeDegrees( double projectionLongitude ) {
		this.projectionLongitude = DTR*projectionLongitude;
	}
	
	public double getProjectionLongitudeDegrees() {
		return projectionLongitude*RTD;
	}
	
	/**
	 * Set the latitude of true scale in radians. This is only used by certain projections.
	 */
	public void setTrueScaleLatitude( double trueScaleLatitude ) {
		this.trueScaleLatitude = trueScaleLatitude;
	}
	
	public double getTrueScaleLatitude() {
		return trueScaleLatitude;
	}
	
	/**
	 * Set the latitude of true scale in degrees. This is only used by certain projections.
	 */
	public void setTrueScaleLatitudeDegrees( double trueScaleLatitude ) {
		this.trueScaleLatitude = DTR*trueScaleLatitude;
	}
	
	public double getTrueScaleLatitudeDegrees() {
		return trueScaleLatitude*RTD;
	}
	
	/**
	 * Set the projection latitude in radians.
	 */
	public void setProjectionLatitude1( double projectionLatitude1 ) {
		this.projectionLatitude1 = projectionLatitude1;
	}
	
	public double getProjectionLatitude1() {
		return projectionLatitude1;
	}
	
	/**
	 * Set the projection latitude in degrees.
	 */
	public void setProjectionLatitude1Degrees( double projectionLatitude1 ) {
		this.projectionLatitude1 = DTR*projectionLatitude1;
	}
	
	public double getProjectionLatitude1Degrees() {
		return projectionLatitude1*RTD;
	}
	
	/**
	 * Set the projection latitude in radians.
	 */
	public void setProjectionLatitude2( double projectionLatitude2 ) {
		this.projectionLatitude2 = projectionLatitude2;
	}
	
	public double getProjectionLatitude2() {
		return projectionLatitude2;
	}
	
	/**
	 * Set the projection latitude in degrees.
	 */
	public void setProjectionLatitude2Degrees( double projectionLatitude2 ) {
		this.projectionLatitude2 = DTR*projectionLatitude2;
	}
	
	public double getProjectionLatitude2Degrees() {
		return projectionLatitude2*RTD;
	}
	
	/**
	 * Set the false Northing in projected units.
	 */
	public void setFalseNorthing( double falseNorthing ) {
		this.falseNorthing = falseNorthing;
	}
	
	public double getFalseNorthing() {
		return falseNorthing;
	}
	
	/**
	 * Set the false Easting in projected units.
	 */
	public void setFalseEasting( double falseEasting ) {
		this.falseEasting = falseEasting;
	}
	
	public double getFalseEasting() {
		return falseEasting;
	}
	
	/**
	 * Set the projection scale factor. This is set to 1 by default.
	 */
	public void setScaleFactor( double scaleFactor ) {
		this.scaleFactor = scaleFactor;
	}

	public double getScaleFactor() {
		return scaleFactor;
	}

	public double getEquatorRadius() {
		return a;
	}

	/**
	 * Set the conversion factor from metres to projected units. This is set to 1 by default.
	 */
	public void setFromMetres( double fromMetres ) {
		this.fromMetres = fromMetres;
	}
	
	public double getFromMetres() {
		return fromMetres;
	}
	
	public void setEllipsoid( Ellipsoid ellipsoid ) {
		this.ellipsoid = ellipsoid;
		a = ellipsoid.equatorRadius;
		e = ellipsoid.eccentricity;
		es = ellipsoid.eccentricity2;
	}
	
	public Ellipsoid getEllipsoid() {
		return ellipsoid;
	}

	/**
	 * Returns the ESPG code for this projection, or 0 if unknown.
	 */
	public int getEPSGCode() {
		return 0;
	}
	
	/**
	 * Initialize the projection. This should be called after setting parameters and before using the projection.
	 * This is for performance reasons as initialization may be expensive.
	 */
	public void initialize() {
		spherical = e == 0.0;
		one_es = 1-es;
		rone_es = 1.0/one_es;
		totalScale = a * fromMetres;
		totalFalseEasting = falseEasting * fromMetres;
		totalFalseNorthing = falseNorthing * fromMetres;		
	}

	public static float normalizeLongitude(float angle) {
		if ( Double.isInfinite(angle) || Double.isNaN(angle) )
			throw new IllegalArgumentException("Infinite longitude");
		while (angle > 180)
			angle -= 360;
		while (angle < -180)
			angle += 360;
		return angle;
	}

	public static double normalizeLongitudeRadians( double angle ) {
		if ( Double.isInfinite(angle) || Double.isNaN(angle) )
			throw new IllegalArgumentException("Infinite longitude");
		while (angle > Math.PI)
			angle -= MapMath.TWOPI;
		while (angle < -Math.PI)
			angle += MapMath.TWOPI;
		return angle;
	}

}

