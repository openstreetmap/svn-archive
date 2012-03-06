/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.geometry;

import java.io.Serializable;
import java.util.Arrays;

import org.geotools.referencing.CRS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.Utilities;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.Cloneable;

/**
 * A minimum bounding box or rectangle. Regardless of dimension, an {@code Envelope} can be
 * represented without ambiguity as two {@linkplain DirectPosition direct positions} (coordinate
 * points). To encode an {@code Envelope}, it is sufficient to encode these two points.
 * <p>
 * This particular implementation of {@code Envelope} is said "General" because it uses coordinates
 * of an arbitrary dimension.
 * <p>
 * <strong>Tip:</strong> The metadata package provides a
 * {@link org.opengis.metadata.extent.GeographicBoundingBox}, which can be used as a kind of
 * envelope with a coordinate reference system fixed to WGS 84 (EPSG:4326).
 * 
 * @since 2.0
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/geometry/GeneralEnvelope.java $
 *         http://svn.osgeo.org/geotools/branches/2.6.x/modules/library/referencing/src/main/java
 *         /org/geotools/geometry/GeneralEnvelope.java $
 * @version $Id: GeneralEnvelope.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 * @author Simone Giannecchini
 * 
 * @see Envelope2D
 * @see org.geotools.geometry.jts.ReferencedEnvelope
 * @see org.opengis.metadata.extent.GeographicBoundingBox
 */
public class GeneralEnvelope extends AbstractEnvelope implements Cloneable, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1752330560227688940L;

    /**
     * Minimum and maximum ordinate values. The first half contains minimum ordinates, while the
     * last half contains maximum ordinates. This layout is convenient for the creation of lower and
     * upper corner direct positions.
     * <p>
     * Consider this reference as final; it is modified by {@link #clone} only.
     */
    private double[] ordinates;

    /**
     * The coordinate reference system, or {@code null}.
     */
    private CoordinateReferenceSystem crs;

    /**
     * Constructs a envelope defined by two positions.
     * 
     * @param minDP
     *            Minimum ordinate values.
     * @param maxDP
     *            Maximum ordinate values.
     * @throws MismatchedDimensionException
     *             if the two positions don't have the same dimension.
     * @throws IllegalArgumentException
     *             if an ordinate value in the minimum point is not less than or equal to the
     *             corresponding ordinate value in the maximum point.
     */
    public GeneralEnvelope(final double[] minDP, final double[] maxDP)
            throws IllegalArgumentException {
        ensureNonNull("minDP", minDP);
        ensureNonNull("maxDP", maxDP);
        ensureSameDimension(minDP.length, maxDP.length);
        ordinates = new double[minDP.length + maxDP.length];
        System.arraycopy(minDP, 0, ordinates, 0, minDP.length);
        System.arraycopy(maxDP, 0, ordinates, minDP.length, maxDP.length);
        checkCoordinates(ordinates);
    }

    /**
     * Constructs a envelope defined by two positions. The coordinate reference system is inferred
     * from the supplied direct position.
     * 
     * @param minDP
     *            Point containing minimum ordinate values.
     * @param maxDP
     *            Point containing maximum ordinate values.
     * @throws MismatchedDimensionException
     *             if the two positions don't have the same dimension.
     * @throws MismatchedReferenceSystemException
     *             if the two positions don't use the same CRS.
     * @throws IllegalArgumentException
     *             if an ordinate value in the minimum point is not less than or equal to the
     *             corresponding ordinate value in the maximum point.
     */
    public GeneralEnvelope(final GeneralDirectPosition minDP, final GeneralDirectPosition maxDP)
            throws MismatchedReferenceSystemException, IllegalArgumentException {
        // Uncomment next lines if Sun fixes RFE #4093999
        // ensureNonNull("minDP", minDP);
        // ensureNonNull("maxDP", maxDP);
        this(minDP.ordinates, maxDP.ordinates);
        crs = getCoordinateReferenceSystem(minDP, maxDP);
        AbstractDirectPosition.checkCoordinateReferenceSystemDimension(crs, ordinates.length / 2);
    }

    /**
     * Constructs a new envelope with the same data than the specified envelope.
     * 
     * @param envelope
     *            The envelope to copy.
     */
    public GeneralEnvelope(final Envelope envelope) {
        ensureNonNull("envelope", envelope);
        if (envelope instanceof GeneralEnvelope) {
            final GeneralEnvelope e = (GeneralEnvelope) envelope;
            ordinates = e.ordinates.clone();
            crs = e.crs;
        } else {
            crs = envelope.getCoordinateReferenceSystem();
            final int dimension = envelope.getDimension();
            ordinates = new double[2 * dimension];
            for (int i = 0; i < dimension; i++) {
                ordinates[i] = envelope.getMinimum(i);
                ordinates[i + dimension] = envelope.getMaximum(i);
            }
            checkCoordinates(ordinates);
        }
    }

    /**
     * Makes sure an argument is non-null.
     * 
     * @param name
     *            Argument name.
     * @param object
     *            User argument.
     * @throws InvalidParameterValueException
     *             if {@code object} is null.
     */
    private static void ensureNonNull(final String name, final Object object)
            throws IllegalArgumentException {
        if (object == null) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.NULL_ARGUMENT_$1, name));
        }
    }

    /**
     * Makes sure the specified dimensions are identical.
     */
    private static void ensureSameDimension(final int dim1, final int dim2)
            throws MismatchedDimensionException {
        if (dim1 != dim2) {
            throw new MismatchedDimensionException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$2,
                    dim1, dim2));
        }
    }

    /**
     * Checks if ordinate values in the minimum point are less than or equal to the corresponding
     * ordinate value in the maximum point.
     * <p>
     * This code will recognize the following exceptions:
     * <ul>
     * <li>ordinates encoding isNil</li>
     * <li>ordinates encoding isEmpty</li>
     * </ul>
     * @throws IllegalArgumentException
     *             if an ordinate value in the minimum point is not less than or equal to the
     *             corresponding ordinate value in the maximum point.
     */
    private static void checkCoordinates(final double[] ordinates) throws IllegalArgumentException {
        if( isNilCoordinates( ordinates )){
            return; // null ordinates are okay            
        }
        if( isEmptyOrdinates(ordinates)){
            return; // empty ordinates are also a valid encoding....
        }
        final int dimension = ordinates.length / 2;
        for (int i = 0; i < dimension; i++) {
            if (!(ordinates[i] <= ordinates[dimension + i])) { // Use '!' in order to catch 'NaN'.
                throw new IllegalArgumentException(Errors.format(
                        ErrorKeys.ILLEGAL_ENVELOPE_ORDINATE_$1, i));
            }
        }
    }

    /**
     * Returns the coordinate reference system in which the coordinates are given.
     * 
     * @return The coordinate reference system, or {@code null}.
     */
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        assert crs == null || crs.getCoordinateSystem().getDimension() == getDimension();
        return crs;
    }

    /**
     * Sets the coordinate reference system in which the coordinate are given. This method
     * <strong>do not</strong> reproject the envelope, and do not check if the envelope is contained
     * in the new domain of validity. The later can be enforced by a call to {@link #normalize}.
     * 
     * @param crs
     *            The new coordinate reference system, or {@code null}.
     * @throws MismatchedDimensionException
     *             if the specified CRS doesn't have the expected number of dimensions.
     */
    public void setCoordinateReferenceSystem(final CoordinateReferenceSystem crs)
            throws MismatchedDimensionException {
        AbstractDirectPosition.checkCoordinateReferenceSystemDimension(crs, getDimension());
        this.crs = crs;
    }

    /**
     * Returns the number of dimensions.
     */
    public final int getDimension() {
        return ordinates.length / 2;
    }

    /**
     * A coordinate position consisting of all the {@linkplain #getMinimum minimal ordinates} for
     * each dimension for all points within the {@code Envelope}.
     * 
     * @return The lower corner.
     */
    @Override
    public DirectPosition getLowerCorner() {
        final int dim = ordinates.length / 2;
        final GeneralDirectPosition position = new GeneralDirectPosition(dim);
        System.arraycopy(ordinates, 0, position.ordinates, 0, dim);
        position.setCoordinateReferenceSystem(crs);
        return position;
    }

    /**
     * A coordinate position consisting of all the {@linkplain #getMaximum maximal ordinates} for
     * each dimension for all points within the {@code Envelope}.
     * 
     * @return The upper corner.
     */
    @Override
    public DirectPosition getUpperCorner() {
        final int dim = ordinates.length / 2;
        final GeneralDirectPosition position = new GeneralDirectPosition(dim);
        System.arraycopy(ordinates, dim, position.ordinates, 0, dim);
        position.setCoordinateReferenceSystem(crs);
        return position;
    }

    /**
     * Creates an exception for an index out of bounds.
     */
    private static IndexOutOfBoundsException indexOutOfBounds(final int dimension) {
        return new IndexOutOfBoundsException(Errors.format(ErrorKeys.INDEX_OUT_OF_BOUNDS_$1,
                dimension));
    }

    /**
     * Returns the minimal ordinate along the specified dimension.
     * 
     * @param dimension
     *            The dimension to query.
     * @return The minimal ordinate value along the given dimension.
     * @throws IndexOutOfBoundsException
     *             If the given index is out of bounds.
     */
    public final double getMinimum(final int dimension) throws IndexOutOfBoundsException {
        if (dimension < ordinates.length / 2) {
            return ordinates[dimension];
        } else {
            throw indexOutOfBounds(dimension);
        }
    }

    /**
     * Returns the maximal ordinate along the specified dimension.
     * 
     * @param dimension
     *            The dimension to query.
     * @return The maximal ordinate value along the given dimension.
     * @throws IndexOutOfBoundsException
     *             If the given index is out of bounds.
     */
    public final double getMaximum(final int dimension) throws IndexOutOfBoundsException {
        if (dimension >= 0) {
            return ordinates[dimension + ordinates.length / 2];
        } else {
            throw indexOutOfBounds(dimension);
        }
    }

    /**
     * Returns the center ordinate along the specified dimension.
     * 
     * @param dimension
     *            The dimension to query.
     * @return The mid ordinate value along the given dimension.
     * 
     * @deprecated Renamed as {@link #getMedian(int)}.
     */
    @Deprecated
    public final double getCenter(final int dimension) {
        return getMedian(dimension);
    }

    /**
     * Returns the median ordinate along the specified dimension. The result should be equals (minus
     * rounding error) to <code>({@linkplain #getMaximum getMaximum}(dimension) -
     * {@linkplain #getMinimum getMinimum}(dimension)) / 2</code>.
     * 
     * @param dimension
     *            The dimension to query.
     * @return The mid ordinate value along the given dimension.
     * @throws IndexOutOfBoundsException
     *             If the given index is out of bounds.
     */
    public final double getMedian(final int dimension) throws IndexOutOfBoundsException {
        return 0.5 * (ordinates[dimension] + ordinates[dimension + ordinates.length / 2]);
    }

    /**
     * Returns the envelope length along the specified dimension. This length is equals to the
     * maximum ordinate minus the minimal ordinate.
     * 
     * @param dimension
     *            The dimension to query.
     * @return The difference along maximal and minimal ordinates in the given dimension.
     * 
     * @deprecated Renamed as {@link #getSpan(int)}.
     */
    @Deprecated
    public final double getLength(final int dimension) {
        return getSpan(dimension);
    }

    /**
     * Returns the envelope span (typically width or height) along the specified dimension. The
     * result should be equals (minus rounding error) to <code>{@linkplain #getMaximum
     * getMaximum}(dimension) - {@linkplain #getMinimum getMinimum}(dimension)</code>.
     * 
     * @param dimension
     *            The dimension to query.
     * @return The difference along maximal and minimal ordinates in the given dimension.
     * @throws IndexOutOfBoundsException
     *             If the given index is out of bounds.
     */
    public final double getSpan(final int dimension) throws IndexOutOfBoundsException {
        return ordinates[dimension + ordinates.length / 2] - ordinates[dimension];
    }

    /**
     * Returns {@code false} if at least one ordinate value is not {@linkplain Double#NaN NaN}. The
     * {@code isNull()} check is a little bit different than {@link #isEmpty()} since it returns
     * {@code false} for a partially initialized envelope, while {@code isEmpty()} returns {@code
     * false} only after all dimensions have been initialized. More specifically, the following
     * rules apply:
     * <p>
     * <ul>
     * <li>If <code>isNull() == true</code>, then <code>{@linkplain #isEmpty()} == true</code></li>
     * <li>If <code>{@linkplain #isEmpty()} == false</code>, then <code>isNull() == false</code></li>
     * <li>The converse of the above-cited rules are not always true.</li>
     * </ul>
     * 
     * @return {@code true} if this envelope has NaN values.
     * 
     * @since 2.2
     */
    public boolean isNull() {
        if (!isNilCoordinates(ordinates)) {
            return false;
        }
        assert isEmpty() : this;
        return true;
    }

    /**
     * Check if the ordinates indicate a "nil" envelope.
     * @param ordinates
     * @return
     * @throws IllegalArgumentException
     */
    private static boolean isNilCoordinates(final double[] ordinates)
            throws IllegalArgumentException {
        for (int i = 0; i < ordinates.length; i++) {
            if (!Double.isNaN(ordinates[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines whether or not this envelope is empty. An envelope is non-empty only if it has at
     * least one {@linkplain #getDimension dimension}, and the {@linkplain #getLength length} is
     * greater than 0 along all dimensions. Note that a non-empty envelope is always non-
     * {@linkplain #isNull null}, but the converse is not always true.
     * 
     * @return {@code true} if this envelope is empty.
     */
    public boolean isEmpty() {
        if( isEmptyOrdinates(ordinates)){
            return true;
        }
        assert !isNull() : this; // JG I worry that this is circular
        return false;
    }
    /**
     * Static method used to recognize an empty encoding of ordindates
     * @param ordinates
     * @return true of the ordinates indicate an empty envelope
     * @see #isEmpty()
     */
    private static boolean isEmptyOrdinates( double ordinates[] ){
        final int dimension = ordinates.length / 2;
        if (dimension == 0) {
            return true;
        }
        for (int i = 0; i < dimension; i++) {
            if (!(ordinates[i] < ordinates[i + dimension])) { // Use '!' in order to catch NaN
                return true;
            }
        }
        return false;
    }
    /**
     * Returns {@code true} if at least one of the specified CRS is null, or both CRS are equals.
     * This special processing for {@code null} values is different from the usual contract of an
     * {@code equals} method, but allow to handle the case where the CRS is unknown.
     */
    private static boolean equalsIgnoreMetadata(final CoordinateReferenceSystem crs1,
            final CoordinateReferenceSystem crs2) {
        return crs1 == null || crs2 == null || CRS.equalsIgnoreMetadata(crs1, crs2);
    }

    /**
     * Adds a point to this envelope. The resulting envelope is the smallest envelope that contains
     * both the original envelope and the specified point. After adding a point, a call to
     * {@link #contains} with the added point as an argument will return {@code true}, except if one
     * of the point's ordinates was {@link Double#NaN} (in which case the corresponding ordinate
     * have been ignored).
     * <p>
     * This method assumes that the specified point uses the same CRS than this envelope. For
     * performance reason, it will no be verified unless J2SE assertions are enabled.
     * 
     * @param position
     *            The point to add.
     * @throws MismatchedDimensionException
     *             if the specified point doesn't have the expected dimension.
     */
    public void add(final DirectPosition position) throws MismatchedDimensionException {
        ensureNonNull("position", position);
        final int dim = ordinates.length / 2;
        AbstractDirectPosition.ensureDimensionMatch("position", position.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, position.getCoordinateReferenceSystem()) : position;
        for (int i = 0; i < dim; i++) {
            final double value = position.getOrdinate(i);
            if (value < ordinates[i])
                ordinates[i] = value;
            if (value > ordinates[i + dim])
                ordinates[i + dim] = value;
        }
        assert isEmpty() || contains(position);
    }

    /**
     * Tests if a specified coordinate is inside the boundary of this envelope.
     * <p>
     * This method assumes that the specified point uses the same CRS than this envelope. For
     * performance reason, it will no be verified unless J2SE assertions are enabled.
     * 
     * @param position
     *            The point to text.
     * @return {@code true} if the specified coordinates are inside the boundary of this envelope;
     *         {@code false} otherwise.
     * @throws MismatchedDimensionException
     *             if the specified point doesn't have the expected dimension.
     */
    public boolean contains(final DirectPosition position) throws MismatchedDimensionException {
        ensureNonNull("position", position);
        final int dim = ordinates.length / 2;
        AbstractDirectPosition.ensureDimensionMatch("point", position.getDimension(), dim);
        assert equalsIgnoreMetadata(crs, position.getCoordinateReferenceSystem()) : position;
        for (int i = 0; i < dim; i++) {
            final double value = position.getOrdinate(i);
            if (!(value >= ordinates[i]))
                return false;
            if (!(value <= ordinates[i + dim]))
                return false;
            // Use '!' in order to take 'NaN' in account.
        }
        return true;
    }

    /**
     * Returns a hash value for this envelope.
     */
    @Override
    public int hashCode() {
        int code = Arrays.hashCode(ordinates);
        if (crs != null) {
            code += crs.hashCode();
        }
        assert code == super.hashCode();
        return code;
    }

    /**
     * Compares the specified object with this envelope for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object != null && object.getClass().equals(getClass())) {
            final GeneralEnvelope that = (GeneralEnvelope) object;
            return Arrays.equals(this.ordinates, that.ordinates)
                    && Utilities.equals(this.crs, that.crs);
        }
        return false;
    }

    /**
     * Returns a deep copy of this envelope.
     * 
     * @return A clone of this envelope.
     */
    @Override
    public GeneralEnvelope clone() {
        try {
            GeneralEnvelope e = (GeneralEnvelope) super.clone();
            e.ordinates = e.ordinates.clone();
            return e;
        } catch (CloneNotSupportedException exception) {
            // Should not happen, since we are cloneable.
            throw new AssertionError(exception);
        }
    }
}
