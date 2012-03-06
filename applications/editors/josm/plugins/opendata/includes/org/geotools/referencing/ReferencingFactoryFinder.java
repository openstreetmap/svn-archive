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
package org.geotools.referencing;

import java.util.Set;

import javax.imageio.spi.ServiceRegistry;

import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryFinder;
import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.resources.LazySet;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.Factory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransformFactory;


/**
 * Defines static methods used to access the application's default {@linkplain Factory
 * factory} implementation.
 *
 * <P>To declare a factory implementation, a services subdirectory is placed within the
 * {@code META-INF} directory that is present in every JAR file. This directory
 * contains a file for each factory interface that has one or more implementation classes
 * present in the JAR file. For example, if the JAR file contained a class named
 * {@code com.mycompany.DatumFactoryImpl} which implements the {@link DatumFactory}
 * interface, the JAR file would contain a file named:</P>
 *
 * <blockquote><pre>META-INF/services/org.opengis.referencing.datum.DatumFactory</pre></blockquote>
 *
 * <P>containing the line:</P>
 *
 * <blockquote><pre>com.mycompany.DatumFactoryImpl</pre></blockquote>
 *
 * <P>If the factory classes implements {@link javax.imageio.spi.RegisterableService}, it will
 * be notified upon registration and deregistration. Note that the factory classes should be
 * lightweight and quick to load. Implementations of these interfaces should avoid complex
 * dependencies on other classes and on native code. The usual pattern for more complex services
 * is to register a lightweight proxy for the heavyweight service.</P>
 *
 * <H2>Note on factory ordering</H2>
 * <P>This class is thread-safe. However, calls to any {@link #setAuthorityOrdering} or
 * {@link #setVendorOrdering} methods have a system-wide effect. If two threads or two
 * applications need a different ordering, they shall manage their own instance of
 * {@link FactoryRegistry}. This {@code FactoryFinder} class is simply a convenience
 * wrapper around a {@code FactoryRegistry} instance.</P>
 *
 * @since 2.4
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/referencing/src/main/java/org/geotools/referencing/ReferencingFactoryFinder.java $
 * @version $Id: ReferencingFactoryFinder.java 37299 2011-05-25 05:21:24Z mbedward $
 * @author Martin Desruisseaux (IRD)
 */
public final class ReferencingFactoryFinder extends FactoryFinder {
    /**
     * The service registry for this manager.
     * Will be initialized only when first needed.
     */
    private static FactoryRegistry registry;

    /**
     * Do not allows any instantiation of this class.
     */
    private ReferencingFactoryFinder() {
    }

    /**
     * Returns the service registry. The registry will be created the first
     * time this method is invoked.
     */
    private static FactoryRegistry getServiceRegistry() {
        assert Thread.holdsLock(ReferencingFactoryFinder.class);
        if (registry == null) {
            registry = new FactoryCreator(new Class<?>[] {
                    DatumFactory.class,
                    CSFactory.class,
                    CRSFactory.class,
                    DatumAuthorityFactory.class,
                    CSAuthorityFactory.class,
                    CRSAuthorityFactory.class,
                    MathTransformFactory.class,
                    CoordinateOperationFactory.class,
                    CoordinateOperationAuthorityFactory.class});
        }
        return registry;
    }

    /**
     * Returns all providers of the specified category.
     *
     * @param  category The factory category.
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available factory implementations.
     */
    private static synchronized <T extends Factory>
            Set<T> getFactories(final Class<T> category, Hints hints)
    {
        hints = mergeSystemHints(hints);
        return new LazySet<T>(getServiceRegistry().getServiceProviders(category, null, hints));
    }

    /**
     * Returns a provider of the specified category.
     *
     * @param  category The factory category.
     * @param  hints An optional map of hints, or {@code null} if none.
     * @param  key The hint key to use for searching an implementation.
     * @return The first factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         specified interface.
     */
    private static synchronized <T extends Factory> T getFactory(final Class<T> category,
            Hints hints, final Hints.Key key) throws FactoryRegistryException
    {
        hints = mergeSystemHints(hints);
        return getServiceRegistry().getServiceProvider(category, null, hints, key);
    }

    /**
     * Returns the first implementation of a factory matching the specified hints. If no
     * implementation matches, a new one is created if possible or an exception is thrown
     * otherwise. If more than one implementation is registered and an
     * {@linkplain #setVendorOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @param  category  The authority factory type.
     * @param  authority The desired authority (e.g. "EPSG").
     * @param  hints     An optional map of hints, or {@code null} if none.
     * @param  key       The hint key to use for searching an implementation.
     * @return The first authority factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         specfied interface.
     */
    private static synchronized <T extends AuthorityFactory> T getAuthorityFactory(
            final Class<T> category, final String authority, Hints hints, final Hints.Key key)
            throws FactoryRegistryException
    {
        hints = mergeSystemHints(hints);
        return getServiceRegistry().getServiceProvider(category, new AuthorityFilter(authority), hints, key);
    }

    /**
     * Returns the first implementation of {@link DatumFactory} matching the specified hints.
     * If no implementation matches, a new one is created if possible or an exception is thrown
     * otherwise. If more than one implementation is registered and an
     * {@linkplain #setVendorOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first datum factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link DatumFactory} interface.
     */
    public static DatumFactory getDatumFactory(final Hints hints) throws FactoryRegistryException {
        return getFactory(DatumFactory.class, hints, Hints.DATUM_FACTORY);
    }

    /**
     * Returns a set of all available implementations for the {@link DatumFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available datum factory implementations.
     */
    public static Set<DatumFactory> getDatumFactories(final Hints hints) {
        return getFactories(DatumFactory.class, hints);
    }

    /**
     * Returns the first implementation of {@link CSFactory} matching the specified hints.
     * If no implementation matches, a new one is created if possible or an exception is thrown
     * otherwise. If more than one implementation is registered and an
     * {@linkplain #setVendorOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first coordinate system factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link CSFactory} interface.
     */
    public static CSFactory getCSFactory(final Hints hints) throws FactoryRegistryException {
        return getFactory(CSFactory.class, hints, Hints.CS_FACTORY);
    }

    /**
     * Returns the first implementation of {@link CRSFactory} matching the specified hints.
     * If no implementation matches, a new one is created if possible or an exception is thrown
     * otherwise. If more than one implementation is registered and an
     * {@linkplain #setVendorOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first coordinate reference system factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link CRSFactory} interface.
     */
    public static CRSFactory getCRSFactory(final Hints hints) throws FactoryRegistryException {
        return getFactory(CRSFactory.class, hints, Hints.CRS_FACTORY);
    }

    /**
     * Returns the first implementation of {@link CoordinateOperationFactory} matching the specified
     * hints. If no implementation matches, a new one is created if possible or an exception is
     * thrown otherwise. If more than one implementation is registered and an
     * {@linkplain #setVendorOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     * <p>
     * Hints that may be understood includes
     * {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM_FACTORY},
     * {@link Hints#DATUM_SHIFT_METHOD     DATUM_SHIFT_METHOD},
     * {@link Hints#LENIENT_DATUM_SHIFT    LENIENT_DATUM_SHIFT} and
     * {@link Hints#VERSION                VERSION}.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first coordinate operation factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link CoordinateOperationFactory} interface.
     */
    public static CoordinateOperationFactory getCoordinateOperationFactory(final Hints hints)
            throws FactoryRegistryException
    {
        return getFactory(CoordinateOperationFactory.class, hints,
                Hints.COORDINATE_OPERATION_FACTORY);
    }

    /**
     * Returns a set of all available implementations for the
     * {@link CoordinateOperationFactory} interface.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available coordinate operation factory implementations.
     */
    public static Set<CoordinateOperationFactory> getCoordinateOperationFactories(final Hints hints) {
        return getFactories(CoordinateOperationFactory.class, hints);
    }

    /**
     * Returns a set of all available implementations for the {@link CRSAuthorityFactory} interface.
     * This set can be used to list the available codes known to all authorities.
     * In the event that the same code is understood by more then one authority
     * you will need to assume both are close enough, or make use of this set directly
     * rather than use the {@link CRS#decode} convenience method.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return Set of available coordinate reference system authority factory implementations.
     */
    public static Set<CRSAuthorityFactory> getCRSAuthorityFactories(final Hints hints) {
        return getFactories(CRSAuthorityFactory.class, hints);
    }

    /**
     * Returns the first implementation of {@link CoordinateOperationAuthorityFactory} matching
     * the specified hints. If no implementation matches, a new one is created if possible or an
     * exception is thrown otherwise. If more than one implementation is registered and an
     * {@linkplain #setVendorOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @param  authority The desired authority (e.g. "EPSG").
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first coordinate operation authority factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link CoordinateOperationAuthorityFactory} interface.
     */
    public static CoordinateOperationAuthorityFactory getCoordinateOperationAuthorityFactory(
            final String authority, final Hints hints)
            throws FactoryRegistryException
    {
        return getAuthorityFactory(CoordinateOperationAuthorityFactory.class, authority, hints,
                Hints.COORDINATE_OPERATION_AUTHORITY_FACTORY);
    }

    /**
     * Returns the first implementation of {@link MathTransformFactory} matching the specified
     * hints. If no implementation matches, a new one is created if possible or an exception is
     * thrown otherwise. If more than one implementation is registered and an
     * {@linkplain #setVendorOrdering ordering is set}, then the preferred
     * implementation is returned. Otherwise an arbitrary one is selected.
     *
     * @param  hints An optional map of hints, or {@code null} if none.
     * @return The first math transform factory that matches the supplied hints.
     * @throws FactoryRegistryException if no implementation was found or can be created for the
     *         {@link MathTransformFactory} interface.
     */
    public static MathTransformFactory getMathTransformFactory(final Hints hints)
            throws FactoryRegistryException
    {
        return getFactory(MathTransformFactory.class, hints, Hints.MATH_TRANSFORM_FACTORY);
    }

    /**
     * A filter for factories provided for a given authority.
     */
    private static final class AuthorityFilter implements ServiceRegistry.Filter {
        /** The authority to filter. */
        private final String authority;

        /** Constructs a filter for the given authority. */
        public AuthorityFilter(final String authority) {
            this.authority = authority;
        }

        /** Returns {@code true} if the specified provider is for the authority. */
        public boolean filter(final Object provider) {
            if (authority == null) {
                // If the user didn't specified an authority name, then the factory to use must
                // be specified explicitly through a hint (e.g. Hints.CRS_AUTHORITY_FACTORY).
                return false;
            }
            return Citations.identifierMatches(((AuthorityFactory)provider).getAuthority(), authority);
        }
    }
}
