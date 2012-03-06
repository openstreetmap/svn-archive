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
package org.geotools.referencing;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geotools.factory.FactoryNotFoundException;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.referencing.factory.IdentifiedObjectFinder;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.GenericName;
import org.geotools.util.logging.Logging;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;


/**
 * Simple utility class for making use of the {@linkplain CoordinateReferenceSystem
 * coordinate reference system} and associated {@linkplain org.opengis.referencing.Factory}
 * implementations. This utility class is made up of static final functions. This class is
 * not a factory or a builder. It makes use of the GeoAPI factory interfaces provided by
 * {@link ReferencingFactoryFinder}.
 * <p>
 * The following methods may be added in a future version:
 * <ul>
 *   <li>{@code CoordinateReferenceSystem parseXML(String)}</li>
 * </ul>
 * <p>
 * When using {@link CoordinateReferenceSystem} matching methods of this class 
 * ({@link #equalsIgnoreMetadata(Object, Object)},{@link #lookupIdentifier(IdentifiedObject, boolean)}, 
 * {@link #lookupEpsgCode(CoordinateReferenceSystem, boolean)}, 
 * {@link #lookupIdentifier(IdentifiedObject, boolean)}, 
 * {@link #lookupIdentifier(Citation, CoordinateReferenceSystem, boolean)})
 * against objects derived from a database other than the 
 * official EPSG one it may be advisable to set a non zero comparison tolerance with 
 * {@link Hints#putSystemDefault(java.awt.RenderingHints.Key, Object)} using
 * the {@link Hints#COMPARISON_TOLERANCE} key. A value of 10e-9 has proved to give satisfactory 
 * results with definitions commonly found in .prj files accompaining shapefiles and georeferenced images.<br>
 * <b>Warning</b>: the tolerance value is used in all internal comparison, this will also change 
 * the way math transforms are setup. Use with care.
 * 
 *
 * @since 2.1
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/CRS.java $
 * @version $Id: CRS.java 37602 2011-07-10 19:20:25Z aaime $
 * @author Jody Garnett (Refractions Research)
 * @author Martin Desruisseaux
 * @author Andrea Aime
 *
 * @tutorial http://docs.codehaus.org/display/GEOTOOLS/Coordinate+Transformation+Services+for+Geotools+2.1
 */
public final class CRS {
    
    static final Logger LOGGER = Logging.getLogger(CRS.class);
    
    /**
     * Enumeration describing axis order for geographic coordinate reference systems.
     */
    public static enum AxisOrder {
        /** 
         * Ordering in which longitude comes before latitude, commonly referred to as x/y ordering. 
         */
        EAST_NORTH,
        
        NORTH_EAST,
        
        /**
         * Indicates axis ordering is not applicable to the coordinate reference system.
         */
        INAPPLICABLE;
    }
    
    /**
     * A map with {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER} set to {@link Boolean#TRUE}.
     */
    private static final Hints FORCE_LONGITUDE_FIRST_AXIS_ORDER = new Hints(
            Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);

    /**
     * A factory for CRS creation with (<var>latitude</var>, <var>longitude</var>) axis order
     * (unless otherwise specified in system property). Will be created only when first needed.
     */
    private static CRSAuthorityFactory defaultFactory;

    /**
     * A factory for CRS creation with (<var>longitude</var>, <var>latitude</var>) axis order.
     * Will be created only when first needed.
     */
    private static CRSAuthorityFactory xyFactory;

    /**
     * A factory for default (non-lenient) operations.
     */
    private static CoordinateOperationFactory strictFactory;

    /**
     * A factory for default lenient operations.
     */
    private static CoordinateOperationFactory lenientFactory;

    /**
     * Registers a listener automatically invoked when the system-wide configuration changed.
     */
    static {
        GeoTools.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                synchronized (CRS.class) {
                    defaultFactory = null;
                    xyFactory      = null;
                    strictFactory  = null;
                    lenientFactory = null;
                }
            }
        });
    }

    /**
     * Do not allow instantiation of this class.
     */
    private CRS() {
    }


    //////////////////////////////////////////////////////////////
    ////                                                      ////
    ////        FACTORIES, CRS CREATION AND INSPECTION        ////
    ////                                                      ////
    //////////////////////////////////////////////////////////////

    /**
     * Returns the CRS authority factory used by the {@link #decode(String,boolean) decode} methods.
     * This factory is {@linkplain org.geotools.referencing.factory.BufferedAuthorityFactory buffered},
     * scans over {@linkplain org.geotools.referencing.factory.AllAuthoritiesFactory all factories} and
     * uses additional factories as {@linkplain org.geotools.referencing.factory.FallbackAuthorityFactory
     * fallbacks} if there is more than one {@linkplain ReferencingFactoryFinder#getCRSAuthorityFactories
     * registered factory} for the same authority.
     * <p>
     * This factory can be used as a kind of <cite>system-wide</cite> factory for all authorities.
     * However for more determinist behavior, consider using a more specific factory (as returned
     * by {@link ReferencingFactoryFinder#getCRSAuthorityFactory} when the authority in known.
     *
     * @param  longitudeFirst {@code true} if axis order should be forced to
     *         (<var>longitude</var>,<var>latitude</var>). Note that {@code false} means
     *         "<cite>use default</cite>", <strong>not</strong> "<cite>latitude first</cite>".
     * @return The CRS authority factory.
     * @throws FactoryRegistryException if the factory can't be created.
     *
     * @since 2.3
     */
    public static synchronized CRSAuthorityFactory getAuthorityFactory(final boolean longitudeFirst)
            throws FactoryRegistryException
    {
        CRSAuthorityFactory factory = (longitudeFirst) ? xyFactory : defaultFactory;
        if (factory == null) try {
            factory = new DefaultAuthorityFactory(longitudeFirst);
            if (longitudeFirst) {
                xyFactory = factory;
            } else {
                defaultFactory = factory;
            }
        } catch (NoSuchElementException exception) {
            // No factory registered in FactoryFinder.
            throw new FactoryNotFoundException(null, exception);
        }
        return factory;
    }

    /**
     * Returns the coordinate operation factory used by
     * {@link #findMathTransform(CoordinateReferenceSystem, CoordinateReferenceSystem)
     * findMathTransform} convenience methods.
     *
     * @param lenient {@code true} if the coordinate operations should be created
     *        even when there is no information available for a datum shift.
     *
     * @since 2.4
     */
    public static synchronized CoordinateOperationFactory getCoordinateOperationFactory(final boolean lenient) {
        CoordinateOperationFactory factory = (lenient) ? lenientFactory : strictFactory;
        if (factory == null) {
            final Hints hints = GeoTools.getDefaultHints();
            if (lenient) {
                hints.put(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
            }
            factory = ReferencingFactoryFinder.getCoordinateOperationFactory(hints);
            if (lenient) {
                lenientFactory = factory;
            } else {
                strictFactory = factory;
            }
        }
        return factory;
    }

    /**
     * Return a Coordinate Reference System for the specified code.
     * Note that the code needs to mention the authority. Examples:
     *
     * <blockquote><pre>
     * EPSG:1234
     * AUTO:42001, ..., ..., ...
     * </pre></blockquote>
     *
     * If there is more than one factory implementation for the same authority, then all additional
     * factories are {@linkplain org.geotools.referencing.factory.FallbackAuthorityFactory fallbacks}
     * to be used only when the first acceptable factory failed to create the requested CRS object.
     * <p>
     * CRS objects created by previous calls to this method are
     * {@linkplain org.geotools.referencing.factory.BufferedAuthorityFactory cached in a buffer}
     * using {@linkplain java.lang.ref.WeakReference weak references}. Subsequent calls to this
     * method with the same authority code should be fast, unless the CRS object has been garbage
     * collected.
     *
     * @param  code The Coordinate Reference System authority code.
     * @return The Coordinate Reference System for the provided code.
     * @throws NoSuchAuthorityCodeException If the code could not be understood.
     * @throws FactoryException if the CRS creation failed for an other reason.
     *
     * @see #getSupportedCodes
     * @see org.geotools.referencing.factory.AllAuthoritiesFactory#createCoordinateReferenceSystem
     */
    public static CoordinateReferenceSystem decode(final String code)
            throws NoSuchAuthorityCodeException, FactoryException
    {
        /*
         * Do not use Boolean.getBoolean(GeoTools.FORCE_LONGITUDE_FIRST_AXIS_ORDER).
         * The boolean argument should be 'false', which means "use system default"
         * (not "latitude first").
         */
        return decode(code, false);
    }

    /**
     * Return a Coordinate Reference System for the specified code, maybe forcing the axis order
     * to (<var>longitude</var>, <var>latitude</var>). The {@code code} argument value is parsed
     * as in "<code>{@linkplain #decode(String) decode}(code)</code>". The {@code longitudeFirst}
     * argument value controls the hints to be given to the {@linkplain ReferencingFactoryFinder
     * factory finder} as in the following pseudo-code:
     * <p>
     * <blockquote><pre>
     * if (longitudeFirst) {
     *     hints.put({@linkplain Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER}, {@linkplain Boolean#TRUE});
     * } else {
     *     // Do not set the FORCE_LONGITUDE_FIRST_AXIS_ORDER hint to FALSE.
     *     // Left it unset, which means "use system default".
     * }
     * </pre></blockquote>
     *
     * The following table compare this method {@code longitudeFirst} argument with the
     * hint meaning:
     * <p>
     * <table border='1'>
     * <tr>
     *   <th>This method argument</th>
     *   <th>{@linkplain Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER Hint} value</th>
     *   <th>Meaning</th>
     * </tr>
     * <tr>
     *   <td>{@code true}</td>
     *   <td>{@link Boolean#TRUE TRUE}</td>
     *   <td>All coordinate reference systems are forced to
     *       (<var>longitude</var>, <var>latitude</var>) axis order.</td>
     * </tr>
     * <tr>
     *   <td>{@code false}</td>
     *   <td>{@code null}</td>
     *   <td>Coordinate reference systems may or may not be forced to
     *       (<var>longitude</var>, <var>latitude</var>) axis order. The behavior depends on user
     *       setting, for example the value of the <code>{@value
     *       org.geotools.referencing.factory.epsg.LongitudeFirstFactory#SYSTEM_DEFAULT_KEY}</code>
     *       system property.</td>
     * </tr>
     * <tr>
     *   <td></td>
     *   <td>{@link Boolean#FALSE FALSE}</td>
     *   <td>Forcing (<var>longitude</var>, <var>latitude</var>) axis order is not allowed,
     *       no matter the value of the <code>{@value
     *       org.geotools.referencing.factory.epsg.LongitudeFirstFactory#SYSTEM_DEFAULT_KEY}</code>
     *       system property.</td>
     * </tr>
     * </table>
     *
     * @param  code The Coordinate Reference System authority code.
     * @param  longitudeFirst {@code true} if axis order should be forced to
     *         (<var>longitude</var>, <var>latitude</var>). Note that {@code false} means
     *         "<cite>use default</cite>", <strong>not</strong> "<cite>latitude first</cite>".
     * @return The Coordinate Reference System for the provided code.
     * @throws NoSuchAuthorityCodeException If the code could not be understood.
     * @throws FactoryException if the CRS creation failed for an other reason.
     *
     * @see #getSupportedCodes
     * @see Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER
     *
     * @since 2.3
     */
    public static CoordinateReferenceSystem decode(String code, final boolean longitudeFirst)
            throws NoSuchAuthorityCodeException, FactoryException
    {
        // @deprecated: 'toUpperCase()' is required only for epsg-wkt.
        // Remove after we deleted the epsg-wkt module.
        code = code.trim().toUpperCase();
        return getAuthorityFactory(longitudeFirst).createCoordinateReferenceSystem(code);
    }

    /**
     * Parses a
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite></A> (WKT) into a CRS object. This convenience method is a
     * shorthand for the following:
     *
     * <blockquote><code>
     * FactoryFinder.{@linkplain ReferencingFactoryFinder#getCRSFactory getCRSFactory}(null).{@linkplain
     * org.opengis.referencing.crs.CRSFactory#createFromWKT createFromWKT}(wkt);
     * </code></blockquote>
     */
    public static CoordinateReferenceSystem parseWKT(final String wkt) throws FactoryException { // NO_UCD
    	return ReferencingFactoryFinder.getCRSFactory(null).createFromWKT(wkt);
    }

    /**
     * Returns the valid geographic area for the specified coordinate reference system,
     * or {@code null} if unknown.
     *
     * This method fetchs the {@linkplain CoordinateReferenceSystem#getDomainOfValidity domain
     * of validity} associated with the given CRS. Only {@linkplain GeographicExtent geographic
     * extents} of kind {@linkplain GeographicBoundingBox geographic bounding box} are taken in
     * account.
     *
     * @param  crs The coordinate reference system, or {@code null}.
     * @return The geographic area, or {@code null} if none.
     *
     * @see #getEnvelope
     *
     * @since 2.3
     */
    public static GeographicBoundingBox getGeographicBoundingBox(final CoordinateReferenceSystem crs) { // NO_UCD
        GeographicBoundingBox     bounds = null;
        GeographicBoundingBoxImpl merged = null;
        if (crs != null) {
            final Extent domainOfValidity = crs.getDomainOfValidity();
            if (domainOfValidity != null) {
                for (final GeographicExtent extent : domainOfValidity.getGeographicElements()) {
                    if (extent instanceof GeographicBoundingBox) {
                        final GeographicBoundingBox candidate = (GeographicBoundingBox) extent;
                        if (bounds == null) {
                            bounds = candidate;
                        } else {
                            if (merged == null) {
                                bounds = merged = new GeographicBoundingBoxImpl(bounds);
                            }
                            merged.add(candidate);
                        }
                    }
                }
            }
        }
        return bounds;
    }

    /**
     * Returns the first ellipsoid found in a coordinate reference system,
     * or {@code null} if there is none.
     *
     * @param  crs The coordinate reference system, or {@code null}.
     * @return The ellipsoid, or {@code null} if none.
     *
     * @since 2.4
     */
    public static Ellipsoid getEllipsoid(final CoordinateReferenceSystem crs) { // NO_UCD
        final Datum datum = CRSUtilities.getDatum(crs);
        if (datum instanceof GeodeticDatum) {
            return ((GeodeticDatum) datum).getEllipsoid();
        }
        if (crs instanceof CompoundCRS) {
            final CompoundCRS cp = (CompoundCRS) crs;
            for (final CoordinateReferenceSystem c : cp.getCoordinateReferenceSystems()) {
                final Ellipsoid candidate = getEllipsoid(c);
                if (candidate != null) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Compares the specified objects for equality. If both objects are Geotools
     * implementations of class {@link AbstractIdentifiedObject}, then this method
     * will ignore the metadata during the comparaison.
     *
     * @param  object1 The first object to compare (may be null).
     * @param  object2 The second object to compare (may be null).
     * @return {@code true} if both objects are equals.
     *
     * @since 2.2
     */
    public static boolean equalsIgnoreMetadata(final Object object1, final Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 instanceof AbstractIdentifiedObject &&
            object2 instanceof AbstractIdentifiedObject)
        {
            return ((AbstractIdentifiedObject) object1).equals(
                   ((AbstractIdentifiedObject) object2), false);
        }
        return object1!=null && object1.equals(object2);
    }

    /**
     * Returns the <cite>Spatial Reference System</cite> identifier, or {@code null} if none. OGC
     * Web Services have the concept of a Spatial Reference System identifier used to communicate
     * CRS information between systems.
     * <p>
     * Spatial Reference System (ie SRS) values:
     * <ul>
     *   <li>{@code EPSG:4326} - this is the usual format understood to mean <cite>forceXY</cite>
     *       order. Note that the axis order is <em>not necessarly</em> (<var>longitude</var>,
     *       <var>latitude</var>), but this is the common behavior we observe in practice.</li>
     *   <li>{@code AUTO:43200} - </li>
     *   <li>{@code ogc:uri:.....} - understood to match the EPSG database axis order.</li>
     *   <li>Well Known Text (WKT)</li>
     * </ul>
     *
     * @param  crs The coordinate reference system, or {@code null}.
     * @return SRS represented as a string for communication between systems, or {@code null}.
     *
     * @since 2.5
     */
    public static String toSRS(final CoordinateReferenceSystem crs) {
        boolean forcedLonLat = false;
        try {
            forcedLonLat = Boolean.getBoolean("org.geotools.referencing.forceXY") || 
                Boolean.TRUE.equals(Hints.getSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER));
        } catch(Exception e) {
            // all right it was a best effort attempt
            LOGGER.log(Level.FINE, "Failed to determine if we are in forced lon/lat mode", e);
        }
        if (forcedLonLat && CRS.getAxisOrder(crs, false) == AxisOrder.NORTH_EAST) {
            try {
                // not usual axis order, check if we can have a EPSG code
                Integer code = CRS.lookupEpsgCode(crs, false);
                if (code != null) {
                    return "urn:ogc:def:crs:EPSG::" + code;
                }
            } catch (Exception e) {
                // all right it was a best effort attempt
                LOGGER.log(Level.FINE, "Failed to determine EPSG code", e);
            }
        }
        
        // fall back on simple lookups
        if( crs == null ){
            return null;
        }
        final Set<ReferenceIdentifier> identifiers = crs.getIdentifiers();
        if (identifiers.isEmpty()) {
            // fallback unfortunately this often does not work
            final ReferenceIdentifier name = crs.getName();
            if (name != null) {
                return name.toString();
            }
        } else {
            return identifiers.iterator().next().toString();
        }
        return null;
    }

    /**
     * Looks up an identifier of the specified authority for the given
     * {@linkplain CoordinateReferenceSystem coordinate reference system}). This method is similar
     * to <code>{@linkplain #lookupIdentifier(IdentifiedObject, boolean) lookupIdentifier}(object,
     * fullScan)</code> except that the search is performed only among the factories of the given
     * authority.
     * <p>
     * If the CRS does not have an {@linkplain ReferenceIdentifier identifier} which corresponds
     * to the {@linkplain Citations#EPSG EPSG} authority, then:
     * <ul>
     *   <li>if {@code fullScan} is {@code true}, then this method scans the factories in search
     *       for an object {@linkplain #equalsIgnoreMetadata equals, ignoring metadata}, to the
     *       given object. If one is found, its identifier is returned.</li>
     *   <li>Otherwise (if {@code fullScan} is {@code false} or if no identifier was found in the
     *       previous step), this method returns {@code null}.</li>
     * </ul>
     *
     * @param  authority The authority for the code to search.
     * @param  crs The coordinate reference system instance, or {@code null}.
     * @return The CRS identifier, or {@code null} if none was found.
     * @throws FactoryException if an error occured while searching for the identifier.
     *
     * @since 2.5
     */
    public static String lookupIdentifier(final Citation authority,
            final CoordinateReferenceSystem crs, final boolean fullScan)
            throws FactoryException
    {
        ReferenceIdentifier id = AbstractIdentifiedObject.getIdentifier(crs, authority);
        if (id != null) {
            return id.getCode();
        }
        for (final CRSAuthorityFactory factory : ReferencingFactoryFinder
                    .getCRSAuthorityFactories(FORCE_LONGITUDE_FIRST_AXIS_ORDER))
        {
            if (!Citations.identifierMatches(factory.getAuthority(), authority)) {
                continue;
            }
            if (!(factory instanceof AbstractAuthorityFactory)) {
                continue;
            }
            final AbstractAuthorityFactory f = (AbstractAuthorityFactory) factory;
            final IdentifiedObjectFinder finder = f.getIdentifiedObjectFinder(crs.getClass());
            finder.setFullScanAllowed(fullScan);
            final String code = finder.findIdentifier(crs);
            if (code != null) {
                return code;
            }
        }
        return null;
    }

    /**
     * Looks up an EPSG code for the given {@linkplain CoordinateReferenceSystem
     * coordinate reference system}). This is a convenience method for <code>{@linkplain
     * #lookupIdentifier(Citations, IdentifiedObject, boolean) lookupIdentifier}({@linkplain
     * Citations#EPSG}, crs, fullScan)</code> except that code is parsed as an integer.
     *
     * @param  crs The coordinate reference system instance, or {@code null}.
     * @return The CRS identifier, or {@code null} if none was found.
     * @throws FactoryException if an error occured while searching for the identifier.
     *
     * @since 2.5
     */
    public static Integer lookupEpsgCode(final CoordinateReferenceSystem crs, final boolean fullScan)
            throws FactoryException
    {
        final String identifier = lookupIdentifier(Citations.EPSG, crs, fullScan);
        if (identifier != null) {
            final int split = identifier.lastIndexOf(GenericName.DEFAULT_SEPARATOR);
            final String code = identifier.substring(split + 1);
            // The above code works even if the separator was not found, since in such case
            // split == -1, which implies a call to substring(0) which returns 'identifier'.
            try {
                return Integer.parseInt(code);
            } catch (NumberFormatException e) {
                throw new FactoryException(Errors.format(ErrorKeys.ILLEGAL_IDENTIFIER_$1, identifier), e);
            }
        }
        return null;
    }


    /////////////////////////////////////////////////
    ////                                         ////
    ////          COORDINATE OPERATIONS          ////
    ////                                         ////
    /////////////////////////////////////////////////

    /**
     * Grab a transform between two Coordinate Reference Systems. This convenience method is a
     * shorthand for the following:
     *
     * <blockquote><code>FactoryFinder.{@linkplain ReferencingFactoryFinder#getCoordinateOperationFactory
     * getCoordinateOperationFactory}(null).{@linkplain CoordinateOperationFactory#createOperation
     * createOperation}(sourceCRS, targetCRS).{@linkplain CoordinateOperation#getMathTransform
     * getMathTransform}();</code></blockquote>
     *
     * Note that some metadata like {@linkplain CoordinateOperation#getPositionalAccuracy
     * positional accuracy} are lost by this method. If those metadata are wanted, use the
     * {@linkplain CoordinateOperationFactory coordinate operation factory} directly.
     * <p>
     * Sample use:
     * <blockquote><code>
     * {@linkplain MathTransform} transform = CRS.findMathTransform(
     * CRS.{@linkplain #decode decode}("EPSG:42102"),
     * CRS.{@linkplain #decode decode}("EPSG:4326") );
     * </blockquote></code>
     *
     * @param  sourceCRS The source CRS.
     * @param  targetCRS The target CRS.
     * @return The math transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws FactoryException If no math transform can be created for the specified source and
     *         target CRS.
     */
    public static MathTransform findMathTransform(final CoordinateReferenceSystem sourceCRS,
                                                  final CoordinateReferenceSystem targetCRS)
            throws FactoryException
    {
    	return findMathTransform(sourceCRS, targetCRS, false);
    }

    /**
     * Grab a transform between two Coordinate Reference Systems. This method is similar to
     * <code>{@linkplain #findMathTransform(CoordinateReferenceSystem, CoordinateReferenceSystem)
     * findMathTransform}(sourceCRS, targetCRS)</code>, except that it can optionally tolerate
     * <cite>lenient datum shift</cite>. If the {@code lenient} argument is {@code true},
     * then this method will not throw a "<cite>Bursa-Wolf parameters required</cite>"
     * exception during datum shifts if the Bursa-Wolf paramaters are not specified.
     * Instead it will assume a no datum shift.
     *
     * @param  sourceCRS The source CRS.
     * @param  targetCRS The target CRS.
     * @param  lenient {@code true} if the math transform should be created even when there is
     *         no information available for a datum shift. The default value is {@code false}.
     * @return The math transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws FactoryException If no math transform can be created for the specified source and
     *         target CRS.
     *
     * @see Hints#LENIENT_DATUM_SHIFT
     */
    public static MathTransform findMathTransform(final CoordinateReferenceSystem sourceCRS,
                                                  final CoordinateReferenceSystem targetCRS,
                                                  boolean lenient)
            throws FactoryException
    {
        if (equalsIgnoreMetadata(sourceCRS, targetCRS)) {
            // Slight optimization in order to avoid the overhead of loading the full referencing engine.
            return IdentityTransform.create(sourceCRS.getCoordinateSystem().getDimension());
        }
        CoordinateOperationFactory operationFactory = getCoordinateOperationFactory(lenient);
        return operationFactory.createOperation(sourceCRS, targetCRS).getMathTransform();
    }

    /**
     * Determines the axis ordering of a specified crs object.
     * <p>
     * The <tt>useBaseGeoCRS</tt> parameter is used to control behaviour for 
     * projected crs. When set to <tt>true</tt> the comparison will use the 
     * coordinate system for the underlying geographic crs object for the 
     * comparison. When set to false the comparison will use the coordinate 
     * system from projected crs itself.
     * </p>
     * @param crs The coordinate reference system.
     * @param useBaseGeoCRS Controls whether the base geo crs is used for 
     *   projected crs.
     * 
     * @return One of {@link AxisOrder#EAST_NORTH}, {@link AxisOrder@NORTH_EAST}, 
     * or {@link AxisOrder#INAPPLICABLE}
     */
    public static AxisOrder getAxisOrder(CoordinateReferenceSystem crs, boolean useBaseGeoCRS) { 
        CoordinateSystem cs = null;

        if (crs instanceof ProjectedCRS) {
            cs = !useBaseGeoCRS ? crs.getCoordinateSystem() :  
                ((ProjectedCRS)crs).getBaseCRS().getCoordinateSystem();
        }
        else if (crs instanceof GeographicCRS) {
            cs = crs.getCoordinateSystem();
        }
        else {
            return AxisOrder.INAPPLICABLE;
        }

        int dimension = cs.getDimension();
        int longitudeDim = -1;
        int latitudeDim = -1;

        for (int i = 0; i < dimension; i++) {
            AxisDirection dir = cs.getAxis(i).getDirection().absolute();

            if (dir.equals(AxisDirection.EAST)) {
                longitudeDim = i;
            }

            if (dir.equals(AxisDirection.NORTH)) {
                latitudeDim = i;
            }
        }

        if ((longitudeDim >= 0) && (latitudeDim >= 0)) {
            if (longitudeDim < latitudeDim) {
                return AxisOrder.EAST_NORTH;
            } else {
                return AxisOrder.NORTH_EAST;
            }
        }

        return AxisOrder.INAPPLICABLE;
    }
}
