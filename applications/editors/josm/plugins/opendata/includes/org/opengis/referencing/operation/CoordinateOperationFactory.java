/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.referencing.operation;

import static org.opengis.annotation.Specification.OGC_01009;

import java.util.Map;

import org.opengis.annotation.Extension;
import org.opengis.annotation.UML;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ObjectFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Creates {@linkplain CoordinateOperation coordinate operations}.
 * This factory is capable to find coordinate {@linkplain Transformation transformations}
 * or {@linkplain Conversion conversions} between two
 * {@linkplain CoordinateReferenceSystem coordinate reference systems}.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/referencing/operation/CoordinateOperationFactory.java $
 * @version <A HREF="http://www.opengis.org/docs/01-009.pdf">Implementation specification 1.0</A>
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 1.0
 */
@UML(identifier="CT_CoordinateTransformationFactory", specification=OGC_01009)
public interface CoordinateOperationFactory extends ObjectFactory {
    /**
     * Returns an operation for conversion or transformation between two coordinate reference systems.
     * <ul>
     *   <li>If an operation exists, it is returned.</li>
     *   <li>If more than one operation exists, the default is returned.</li>
     *   <li>If no operation exists, then the exception is thrown.</li>
     * </ul>
     * <p>
     * Implementations may try to
     * {@linkplain CoordinateOperationAuthorityFactory#createFromCoordinateReferenceSystemCodes
     * query an authority factory} first, and compute the operation next if no operation from
     * {@code source} to {@code target} code was explicitly defined by the authority.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @return A coordinate operation from {@code sourceCRS} to {@code targetCRS}.
     * @throws OperationNotFoundException if no operation path was found from {@code sourceCRS}
     *         to {@code targetCRS}.
     * @throws FactoryException if the operation creation failed for some other reason.
     */
    @UML(identifier="createFromCoordinateSystems", specification=OGC_01009)
    CoordinateOperation createOperation(CoordinateReferenceSystem sourceCRS,
                                        CoordinateReferenceSystem targetCRS)
            throws OperationNotFoundException, FactoryException;

    /**
     * Returns an operation using a particular method for conversion or transformation
     * between two coordinate reference systems.
     * <ul>
     *   <li>If the operation exists on the implementation, then it is returned.</li>
     *   <li>If the operation does not exist on the implementation, then the implementation
     *       has the option of inferring the operation from the argument objects.</li>
     *   <li>If for whatever reason the specified operation will not be returned, then
     *       the exception is thrown.</li>
     * </ul>
     * <p>
     * <b>Example:</b> A transformation between two {@linkplain org.opengis.referencing.crs.GeographicCRS
     * geographic CRS} using different {@linkplain org.opengis.datum.GeodeticDatum datum} requires a
     * <cite>datum shift</cite>. Many methods exist for this purpose, including interpolations in a
     * grid, a scale/rotation/translation in geocentric coordinates or the Molodenski approximation.
     * When invoking {@code createOperation} without operation method, this factory may select by
     * default the most accurate transformation (typically interpolation in a grid). When invoking
     * {@code createOperation} with an operation method, user can force usage of Molodenski
     * approximation for instance.
     *
     * @param  sourceCRS Input coordinate reference system.
     * @param  targetCRS Output coordinate reference system.
     * @param  method The algorithmic method for conversion or transformation.
     * @return A coordinate operation from {@code sourceCRS} to {@code targetCRS}.
     * @throws OperationNotFoundException if no operation path was found from {@code sourceCRS}
     *         to {@code targetCRS}.
     * @throws FactoryException if the operation creation failed for some other reason.
     */
    @Extension
    CoordinateOperation createOperation(CoordinateReferenceSystem sourceCRS,
                                        CoordinateReferenceSystem targetCRS,
                                        OperationMethod           method)
            throws OperationNotFoundException, FactoryException;

    /**
     * Creates a concatenated operation from a sequence of operations.
     *
     * @param  properties Name and other properties to give to the new object.
     *         Available properties are {@linkplain ObjectFactory listed there}.
     * @param  operations The sequence of operations.
     * @return The concatenated operation.
     * @throws FactoryException if the object creation failed.
     */
    @Extension
    CoordinateOperation createConcatenatedOperation(Map<String, ?> properties,
                                                    CoordinateOperation[] operations)
            throws FactoryException;
}
