/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2001-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.operation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.referencing.AbstractIdentifiedObject;
import org.geotools.referencing.crs.AbstractDerivedCRS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.Utilities;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.quality.PositionalAccuracy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.ConicProjection;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CylindricalProjection;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Operation;
import org.opengis.referencing.operation.PlanarProjection;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.Transformation;
import org.opengis.util.InternationalString;


/**
 * Establishes an association between a source and a target coordinate reference system,
 * and provides a {@linkplain MathTransform transform} for transforming coordinates in
 * the source CRS to coordinates in the target CRS. Many but not all coordinate operations (from
 * {@linkplain CoordinateReferenceSystem coordinate reference system} <VAR>A</VAR> to
 * {@linkplain CoordinateReferenceSystem coordinate reference system} <VAR>B</VAR>)
 * also uniquely define the inverse operation (from
 * {@linkplain CoordinateReferenceSystem coordinate reference system} <VAR>B</VAR> to
 * {@linkplain CoordinateReferenceSystem coordinate reference system} <VAR>A</VAR>).
 * In some cases, the operation method algorithm for the inverse operation is the same
 * as for the forward algorithm, but the signs of some operation parameter values must
 * be reversed. In other cases, different algorithms are required for the forward and
 * inverse operations, but the same operation parameter values are used. If (some)
 * entirely different parameter values are needed, a different coordinate operation
 * shall be defined.
 * <p>
 * This class is conceptually <cite>abstract</cite>, even if it is technically possible to
 * instantiate it. Typical applications should create instances of the most specific subclass with
 * {@code Default} prefix instead. An exception to this rule may occurs when it is not possible to
 * identify the exact type.
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/operation/AbstractCoordinateOperation.java $
 * @version $Id: AbstractCoordinateOperation.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public class AbstractCoordinateOperation extends AbstractIdentifiedObject
        implements CoordinateOperation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1237358357729193885L;

    /**
     * An empty array of positional accuracy. This is usefull for fetching accuracies as an array,
     * using the following idiom:
     * <blockquote><pre>
     * {@linkplain #getPositionalAccuracy()}.toArray(EMPTY_ACCURACY_ARRAY);
     * </pre></blockquote>
     */
    public static final PositionalAccuracy[] EMPTY_ACCURACY_ARRAY = new PositionalAccuracy[0];

    /**
     * List of localizable properties. To be given to {@link AbstractIdentifiedObject} constructor.
     */
    private static final String[] LOCALIZABLES = {SCOPE_KEY};

    /**
     * The source CRS, or {@code null} if not available.
     */
    protected final CoordinateReferenceSystem sourceCRS;

    /**
     * The target CRS, or {@code null} if not available.
     */
    protected final CoordinateReferenceSystem targetCRS;

    /**
     * Version of the coordinate transformation
     * (i.e., instantiation due to the stochastic nature of the parameters).
     */
    final String operationVersion;

    /**
     * Estimate(s) of the impact of this operation on point accuracy, or {@code null}
     * if none.
     */
    private final Collection<PositionalAccuracy> coordinateOperationAccuracy;

    /**
     * Area in which this operation is valid, or {@code null} if not available.
     */
    protected final Extent domainOfValidity;

    /**
     * Description of domain of usage, or limitations of usage, for which this operation is valid.
     */
    private final InternationalString scope;

    /**
     * Transform from positions in the {@linkplain #getSourceCRS source coordinate reference system}
     * to positions in the {@linkplain #getTargetCRS target coordinate reference system}.
     */
    protected final MathTransform transform;

    /**
     * Constructs a new coordinate operation with the same values than the specified
     * defining conversion, together with the specified source and target CRS. This
     * constructor is used by {@link DefaultConversion} only.
     */
    AbstractCoordinateOperation(final Conversion               definition,
                                final CoordinateReferenceSystem sourceCRS,
                                final CoordinateReferenceSystem targetCRS,
                                final MathTransform             transform)
    {
        super(definition);
        this.sourceCRS                   = sourceCRS;
        this.targetCRS                   = targetCRS;
        this.operationVersion            = definition.getOperationVersion();
        this.coordinateOperationAccuracy = definition.getCoordinateOperationAccuracy();
        this.domainOfValidity            = definition.getDomainOfValidity();
        this.scope                       = definition.getScope();
        this.transform                   = transform;
    }

    /**
     * Constructs a coordinate operation from a set of properties.
     * The properties given in argument follow the same rules than for the
     * {@linkplain AbstractIdentifiedObject#AbstractIdentifiedObject(Map) super-class constructor}.
     * Additionally, the following properties are understood by this construtor:
     * <p>
     * <table border='1'>
     *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
     *     <th nowrap>Property name</th>
     *     <th nowrap>Value type</th>
     *     <th nowrap>Value given to</th>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@value org.opengis.referencing.operation.CoordinateOperation#OPERATION_VERSION_KEY}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getOperationVersion}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@value org.opengis.referencing.operation.CoordinateOperation#COORDINATE_OPERATION_ACCURACY_KEY}&nbsp;</td>
     *     <td nowrap>&nbsp;<code>{@linkplain PositionalAccuracy}[]</code>&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getCoordinateOperationAccuracy}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@value org.opengis.referencing.operation.CoordinateOperation#DOMAIN_OF_VALIDITY_KEY}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link Extent}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getDomainOfValidity}</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>&nbsp;{@value org.opengis.referencing.operation.CoordinateOperation#SCOPE_KEY}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link String} or {@link InternationalString}&nbsp;</td>
     *     <td nowrap>&nbsp;{@link #getScope}</td>
     *   </tr>
     * </table>
     *
     * @param properties Set of properties. Should contains at least {@code "name"}.
     * @param sourceCRS The source CRS.
     * @param targetCRS The target CRS.
     * @param transform Transform from positions in the {@linkplain #getSourceCRS source CRS}
     *                  to positions in the {@linkplain #getTargetCRS target CRS}.
     */
    public AbstractCoordinateOperation(final Map<String,?>            properties,
                                       final CoordinateReferenceSystem sourceCRS,
                                       final CoordinateReferenceSystem targetCRS,
                                       final MathTransform             transform)
    {
        this(properties, new HashMap<String,Object>(), sourceCRS, targetCRS, transform);
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private AbstractCoordinateOperation(final Map<String,?>            properties,
                                        final Map<String,Object>    subProperties,
                                        final CoordinateReferenceSystem sourceCRS,
                                        final CoordinateReferenceSystem targetCRS,
                                        final MathTransform             transform)
    {
        super(properties, subProperties, LOCALIZABLES);
        PositionalAccuracy[] positionalAccuracy;
        domainOfValidity   = (Extent)               subProperties.get(DOMAIN_OF_VALIDITY_KEY);
        scope              = (InternationalString)  subProperties.get(SCOPE_KEY);
        operationVersion   = (String)               subProperties.get(OPERATION_VERSION_KEY);
        positionalAccuracy = (PositionalAccuracy[]) subProperties.get(COORDINATE_OPERATION_ACCURACY_KEY);
        if (positionalAccuracy==null || positionalAccuracy.length==0) {
            positionalAccuracy = null;
        } else {
            positionalAccuracy = positionalAccuracy.clone();
            for (int i=0; i<positionalAccuracy.length; i++) {
                ensureNonNull(COORDINATE_OPERATION_ACCURACY_KEY, positionalAccuracy, i);
            }
        }
        this.coordinateOperationAccuracy = asSet(positionalAccuracy);
        this.sourceCRS = sourceCRS;
        this.targetCRS = targetCRS;
        this.transform = transform;
        validate();
    }

    /**
     * Checks the validity of this operation. This method is invoked by the constructor after
     * every fields have been assigned. It can be overriden by subclasses if different rules
     * should be applied.
     * <p>
     * {@link DefaultConversion} overrides this method in order to allow null values, providing
     * that all of {@code transform}, {@code sourceCRS} and {@code targetCRS} are null together.
     * Note that null values are not allowed for transformations, so {@link DefaultTransformation}
     * does not override this method.
     *
     * @throws IllegalArgumentException if at least one of {@code transform}, {@code sourceCRS}
     *         or {@code targetCRS} is invalid. We throw this kind of exception rather than
     *         {@link IllegalStateException} because this method is invoked by the constructor
     *         for checking argument validity.
     */
    void validate() throws IllegalArgumentException {
        ensureNonNull ("sourceCRS", transform);
        ensureNonNull ("targetCRS", transform);
        ensureNonNull ("transform", transform);
        checkDimension("sourceCRS", sourceCRS, transform.getSourceDimensions());
        checkDimension("targetCRS", targetCRS, transform.getTargetDimensions());
    }

    /**
     * Checks if a reference coordinate system has the expected number of dimensions.
     *
     * @param name     The argument name.
     * @param crs      The coordinate reference system to check.
     * @param expected The expected number of dimensions.
     */
    private static void checkDimension(final String name,
                                       final CoordinateReferenceSystem crs,
                                       final int expected)
    {
        final int actual = crs.getCoordinateSystem().getDimension();
        if (actual != expected) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.MISMATCHED_DIMENSION_$3, name, actual, expected));
        }
    }

    /**
     * Returns the source CRS.
     */
    public CoordinateReferenceSystem getSourceCRS() {
        return sourceCRS;
    }

    /**
     * Returns the target CRS.
     */
    public CoordinateReferenceSystem getTargetCRS() {
        return targetCRS;
    }

    /**
     * Version of the coordinate transformation (i.e., instantiation due to the stochastic
     * nature of the parameters). Mandatory when describing a transformation, and should not
     * be supplied for a conversion.
     *
     * @return The coordinate operation version, or {@code null} in none.
     */
    public String getOperationVersion() {
        return operationVersion;
    }

    /**
     * Estimate(s) of the impact of this operation on point accuracy. Gives
     * position error estimates for target coordinates of this coordinate
     * operation, assuming no errors in source coordinates.
     *
     * @return The position error estimates, or an empty collection if not available.
     *
     * @see #getAccuracy()
     *
     * @since 2.4
     */
    public Collection<PositionalAccuracy> getCoordinateOperationAccuracy() {
        if (coordinateOperationAccuracy == null) {
            return Collections.emptySet();
        }
        return coordinateOperationAccuracy;
    }

    /**
     * Area or region or timeframe in which this coordinate operation is valid.
     * Returns {@code null} if not available.
     *
     * @since 2.4
     */
    public Extent getDomainOfValidity() {
        return domainOfValidity;
    }

    /**
     * Description of domain of usage, or limitations of usage, for which this operation is valid.
     */
    public InternationalString getScope() {
        return scope;
    }

    /**
     * Gets the math transform. The math transform will transform positions in the
     * {@linkplain #getSourceCRS source coordinate reference system} into positions
     * in the {@linkplain #getTargetCRS target coordinate reference system}.
     */
    public MathTransform getMathTransform() {
        return transform;
    }

    /**
     * Returns the most specific GeoAPI interface implemented by the specified operation.
     *
     * @param  object A coordinate operation.
     * @return The most specific GeoAPI interface
     *         (e.g. <code>{@linkplain Transformation}.class</code>).
     */
    public static Class<? extends CoordinateOperation> getType(final CoordinateOperation object) {
        if (object instanceof        Transformation) return        Transformation.class;
        if (object instanceof       ConicProjection) return       ConicProjection.class;
        if (object instanceof CylindricalProjection) return CylindricalProjection.class;
        if (object instanceof      PlanarProjection) return      PlanarProjection.class;
        if (object instanceof            Projection) return            Projection.class;
        if (object instanceof            Conversion) return            Conversion.class;
        if (object instanceof             Operation) return             Operation.class;
        return CoordinateOperation.class;
    }

    /**
     * Compares this coordinate operation with the specified object for equality.
     * If {@code compareMetadata} is {@code true}, then all available properties are
     * compared including {@linkplain #getDomainOfValidity domain of validity} and
     * {@linkplain #getScope scope}.
     *
     * @param  object The object to compare to {@code this}.
     * @param  compareMetadata {@code true} for performing a strict comparaison, or
     *         {@code false} for comparing only properties relevant to transformations.
     * @return {@code true} if both objects are equal.
     */
    @Override
    public boolean equals(final AbstractIdentifiedObject object, final boolean compareMetadata) {
        if (object == this) {
            return true; // Slight optimization.
        }
        if (super.equals(object, compareMetadata)) {
            final AbstractCoordinateOperation that = (AbstractCoordinateOperation) object;
            if (equals(this.sourceCRS, that.sourceCRS, compareMetadata) &&
                Utilities.equals(this.transform, that.transform))
                // See comment in DefaultOperation.equals(...) about why we compare MathTransform.
            {
                if (compareMetadata) {
                    if (!Utilities.equals(this.domainOfValidity, that.domainOfValidity) ||
                        !Utilities.equals(this.scope, that.scope) ||
                        !Utilities.equals(this.coordinateOperationAccuracy, that.coordinateOperationAccuracy))
                    {
                        return false;
                    }
                }
                /*
                 * Avoid never-ending recursivity: AbstractDerivedCRS has a 'conversionFromBase'
                 * field that is set to this AbstractCoordinateOperation.
                 */
                final Boolean comparing = AbstractDerivedCRS._COMPARING.get();
                if (comparing!=null && comparing.booleanValue()) {
                    return true;
                }
                try {
                    AbstractDerivedCRS._COMPARING.set(Boolean.TRUE);
                    return equals(this.targetCRS, that.targetCRS, compareMetadata);
                } finally {
                    AbstractDerivedCRS._COMPARING.set(Boolean.FALSE);
                }
            }
        }
        return false;
    }

    /**
     * Returns a hash code value for this coordinate operation.
     */
    @Override
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (sourceCRS != null) code ^= sourceCRS.hashCode();
        if (targetCRS != null) code ^= targetCRS.hashCode();
        if (transform != null) code ^= transform.hashCode();
        return code;
    }
}
