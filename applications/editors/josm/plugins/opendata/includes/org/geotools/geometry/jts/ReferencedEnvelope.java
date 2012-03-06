/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.geometry.jts;

import org.geotools.referencing.CRS;
import org.geotools.resources.Classes;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * A JTS envelope associated with a
 * {@linkplain CoordinateReferenceSystem coordinate reference system}. In
 * addition, this JTS envelope also implements the GeoAPI
 * {@linkplain org.opengis.geometry.coordinate.Envelope envelope} interface
 * for interoperability with GeoAPI.
 *
 * @since 2.2
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/geometry/jts/ReferencedEnvelope.java $
 * @version $Id: ReferencedEnvelope.java 38435 2011-12-20 08:11:41Z aaime $
 * @author Jody Garnett
 * @author Martin Desruisseaux
 * @author Simone Giannecchini
 *
 * @see org.geotools.geometry.Envelope2D
 * @see org.geotools.geometry.GeneralEnvelope
 * @see org.opengis.metadata.extent.GeographicBoundingBox
 */
public class ReferencedEnvelope extends Envelope implements BoundingBox {
    
    /** A ReferencedEnvelope containing "everything" */
    public static ReferencedEnvelope EVERYTHING = new ReferencedEnvelope(Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, null) {
        private static final long serialVersionUID = -3188702602373537164L;

        public boolean contains(BoundingBox bbox) {
            return true;
        }

        public boolean contains(Coordinate p) {
            return true;
        }

        public boolean contains(DirectPosition pos) {
            return true;
        }

        public boolean contains(double x, double y) {
            return true;
        }

        public boolean contains(Envelope other) {
            return true;
        }

        public boolean isEmpty() {
            return false;
        }

        public boolean isNull() {
            return true;
        }
        
        public double getArea() {
            //return super.getArea();
            return Double.POSITIVE_INFINITY;
        }
        
        public void setBounds(BoundingBox arg0) {
            throw new IllegalStateException("Cannot modify ReferencedEnvelope.EVERYTHING");
        }
        public Coordinate centre() {
            return new Coordinate();
        }
        public void setToNull() {
            // um ignore this as we are already "null"
        }
        public boolean equals(Object obj) {
            if( obj == EVERYTHING ){
                return true;
            }
            if( obj instanceof ReferencedEnvelope ){
                ReferencedEnvelope other = (ReferencedEnvelope) obj;
                if( other.crs != EVERYTHING.crs ) return false;
                if( other.getMinX() != EVERYTHING.getMinX() ) return false;
                if( other.getMinY() != EVERYTHING.getMinY() ) return false;
                if( other.getMaxX() != EVERYTHING.getMaxX() ) return false;
                if( other.getMaxY() != EVERYTHING.getMaxY() ) return false;
                
                return true;
            }
            return super.equals(obj);
        }
        
        public String toString() {
            return "ReferencedEnvelope.EVERYTHING";
        }
    };
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -3188702602373537163L;

    /**
     * The coordinate reference system, or {@code null}.
     */
    private CoordinateReferenceSystem crs;

    /**
     * Creates a null envelope with a null coordinate reference system.
     */
    public ReferencedEnvelope() {
        this((CoordinateReferenceSystem) null);
    }

    /**
     * Creates a null envelope with the specified coordinate reference system.
     *
     * @param crs The coordinate reference system.
     * @throws MismatchedDimensionException if the CRS dimension is not valid.
     */
    public ReferencedEnvelope(CoordinateReferenceSystem crs)
        throws MismatchedDimensionException {
        this.crs = crs;
        checkCoordinateReferenceSystemDimension();
    }

    /**
     * Creates an envelope for a region defined by maximum and minimum values.
     *
     * @param x1  The first x-value.
     * @param x2  The second x-value.
     * @param y1  The first y-value.
     * @param y2  The second y-value.
     * @param crs The coordinate reference system.
     *
     * @throws MismatchedDimensionException if the CRS dimension is not valid.
     */
    public ReferencedEnvelope(final double x1, final double x2, final double y1, final double y2,
        final CoordinateReferenceSystem crs) throws MismatchedDimensionException {
        super(x1, x2, y1, y2);
        this.crs = crs;
        checkCoordinateReferenceSystemDimension();
    }

    /**
     * Creates a new envelope from an existing bounding box.
     *
     * @param bbox The bounding box to initialize from.
     * @throws MismatchedDimensionException if the CRS dimension is not valid.
     *
     * @since 2.4
     */
    public ReferencedEnvelope(final BoundingBox bbox) throws MismatchedDimensionException {
        this(bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY(),
            bbox.getCoordinateReferenceSystem());
    }

    /**
     * Creates a new envelope from an existing JTS envelope.
     *
     * @param envelope The envelope to initialize from.
     * @param crs The coordinate reference system.
     * @throws MismatchedDimensionExceptionif the CRS dimension is not valid.
     */
    public ReferencedEnvelope(final Envelope envelope, final CoordinateReferenceSystem crs)
        throws MismatchedDimensionException {
        super(envelope);
        this.crs = crs;
        checkCoordinateReferenceSystemDimension();
    }

    /**
     * Sets this envelope to the specified bounding box.
     */
    public void init(BoundingBox bounds) {
        super.init(bounds.getMinimum(0), bounds.getMaximum(0), bounds.getMinimum(1),
            bounds.getMaximum(1));
        this.crs = bounds.getCoordinateReferenceSystem();
    }

    /**
     * Returns the specified bounding box as a JTS envelope.
     */
    private static Envelope getJTSEnvelope(final BoundingBox bbox) {
        if( bbox == null ){
            throw new NullPointerException("Provided bbox envelope was null");
        }
        if (bbox instanceof Envelope) {
            return (Envelope) bbox;
        }        
        return new ReferencedEnvelope(bbox);
    }

    /**
     * Convenience method for checking coordinate reference system validity.
     *
     * @throws IllegalArgumentException if the CRS dimension is not valid.
     */
    private void checkCoordinateReferenceSystemDimension()
        throws MismatchedDimensionException {
        if (crs != null) {
            final int expected = getDimension();
            final int dimension = crs.getCoordinateSystem().getDimension();
            if (dimension != expected) {
                throw new MismatchedDimensionException(Errors.format(
                        ErrorKeys.MISMATCHED_DIMENSION_$3, crs.getName().getCode(),
                        new Integer(dimension), new Integer(expected)));
            }
        }
    }

    /**
     * Make sure that the specified bounding box uses the same CRS than this one.
     * 
     * @param  bbox The other bounding box to test for compatibility.
     * @throws MismatchedReferenceSystemException if the CRS are incompatibles.
     */
    private void ensureCompatibleReferenceSystem(final BoundingBox bbox)
        throws MismatchedReferenceSystemException {
        if (crs != null) {
            final CoordinateReferenceSystem other = bbox.getCoordinateReferenceSystem();
            if (other != null) {
                if (!CRS.equalsIgnoreMetadata(crs, other)) {
                    throw new MismatchedReferenceSystemException(Errors.format(
                            ErrorKeys.MISMATCHED_COORDINATE_REFERENCE_SYSTEM));
                }
            }
        }
    }
    
    /**
     * Returns the coordinate reference system associated with this envelope.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Returns the number of dimensions.
     */
    public int getDimension() {
        return 2;
    }

    /**
     * Returns the minimal ordinate along the specified dimension.
     */
    public double getMinimum(final int dimension) {
        switch (dimension) {
        case 0:
            return getMinX();

        case 1:
            return getMinY();

        default:
            throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * Returns the maximal ordinate along the specified dimension.
     */
    public double getMaximum(final int dimension) {
        switch (dimension) {
        case 0:
            return getMaxX();

        case 1:
            return getMaxY();

        default:
            throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * @deprecated Use {@link #getMedian}.
     */
    public double getCenter(final int dimension) {
        return getMedian(dimension);
    }

    /**
     * Returns the center ordinate along the specified dimension.
     */
    public double getMedian(final int dimension) {
        switch (dimension) {
        case 0:
            return 0.5 * (getMinX() + getMaxX());

        case 1:
            return 0.5 * (getMinY() + getMaxY());

        default:
            throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * @deprecated Use {@link #getSpan}.
     */
    public double getLength(final int dimension) {
        return getSpan(dimension);
    }

    /**
     * Returns the envelope length along the specified dimension. This length is
     * equals to the maximum ordinate minus the minimal ordinate.
     */
    public double getSpan(final int dimension) {
        switch (dimension) {
        case 0:
            return getWidth();

        case 1:
            return getHeight();

        default:
            throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * Returns {@code true} if lengths along all dimension are zero.
     *
     * @since 2.4
     */
    public boolean isEmpty() {
        return super.isNull();
    }

    /**
     * Returns {@code true} if the provided location is contained by this bounding box.
     *
     * @since 2.4
     */
    public boolean contains(DirectPosition pos) {
        return super.contains(pos.getOrdinate(0), pos.getOrdinate(1));
    }

    /**
     * Returns {@code true} if the provided bounds are contained by this bounding box.
     *
     * @since 2.4
     */
    public boolean contains(final BoundingBox bbox) {
        ensureCompatibleReferenceSystem(bbox);

        return super.contains(getJTSEnvelope(bbox));
    }

    /**
     * Check if this bounding box intersects the provided bounds.
     */    
    @Override
    public Envelope intersection(Envelope env) {
        if( env instanceof BoundingBox ){
            BoundingBox bbox = (BoundingBox) env;
            ensureCompatibleReferenceSystem( bbox );
        }
        return super.intersection(env);
    }    
    /**
     * Include the provided bounding box, expanding as necessary.
     *
     * @since 2.4
     */
    public void include(final BoundingBox bbox) {
        if( crs == null ){
            this.crs = bbox.getCoordinateReferenceSystem();
        }
        else {
            ensureCompatibleReferenceSystem(bbox);
        }
        super.expandToInclude(getJTSEnvelope(bbox));        
    }

    /**
     * Include the provided envelope, expanding as necessary.
     */
    @Override
    public void expandToInclude(Envelope other) {
        if( other instanceof BoundingBox ){
            BoundingBox bbox = (BoundingBox) other;
            ensureCompatibleReferenceSystem( bbox );
        }
        super.expandToInclude(other);
    }
    
    /**
     * Include the provided coordinates, expanding as necessary.
     *
     * @since 2.4
     */
    public void include(double x, double y) {
        super.expandToInclude(x, y);
    }

    /**
     * Initialize the bounding box with another bounding box.
     *
     * @since 2.4
     */
    public void setBounds(final BoundingBox bbox) {
        ensureCompatibleReferenceSystem(bbox);
        super.init(getJTSEnvelope(bbox));
    }

    /**
     * Returns a hash value for this envelope. This value need not remain
     * consistent between different implementations of the same class.
     */
    @Override
    public int hashCode() {
        int code = super.hashCode() ^ (int) serialVersionUID;
        if (crs != null) {
            code ^= crs.hashCode();
        }
        return code;
    }

    /**
     * Compares the specified object with this envelope for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final CoordinateReferenceSystem otherCRS = (object instanceof ReferencedEnvelope)
                ? ((ReferencedEnvelope) object).crs : null;

            if(otherCRS == null) {
                return crs == null;
            } else {
                return CRS.equalsIgnoreMetadata(crs, otherCRS);
            }
        }
        return false;
    }

    /**
     * Returns a string representation of this envelope. The default implementation
     * is okay for occasional formatting (for example for debugging purpose).
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(Classes.getShortClassName(this)).append('[');
        final int dimension = getDimension();

        for (int i = 0; i < dimension; i++) {
            if (i != 0) {
                buffer.append(", ");
            }

            buffer.append(getMinimum(i)).append(" : ").append(getMaximum(i));
        }

        return buffer.append(']').toString();
    }
    
    /**
     * Utility method to ensure that an Envelope if a ReferencedEnvelope.
     * <p>
     * This method first checks if <tt>e</tt> is an instanceof {@link ReferencedEnvelope},
     * if it is, itself is returned. If not <code>new ReferencedEnvelpe(e,null)</code>
     * is returned.
     * </p>
     * <p>
     * If e is null, null is returned.
     * </p>
     * @param e The envelope.  Can be null.
     * @return A ReferencedEnvelope using the specified envelope, or null if the envelope was null.
     */
    public static ReferencedEnvelope reference(Envelope e) {
        if (e == null) {
            return null;
        } else {
            if (e instanceof ReferencedEnvelope) {
                return (ReferencedEnvelope) e;
            }

            return new ReferencedEnvelope(e, null);
        }
    }

    /**
     * Utility method to ensure that an BoundingBox in a ReferencedEnvelope.
     * <p>
     * This method first checks if <tt>e</tt> is an instanceof {@link ReferencedEnvelope},
     * if it is, itself is returned. If not <code>new ReferencedEnvelpe(e)</code>
     * is returned.
     * </p>
     * @param e The envelope.
     * @return
     */
    public static ReferencedEnvelope reference(BoundingBox e) {
        if (e == null) {
            return null;
        }

        if (e instanceof ReferencedEnvelope) {
            return (ReferencedEnvelope) e;
        }

        return new ReferencedEnvelope(e);
    }
}
