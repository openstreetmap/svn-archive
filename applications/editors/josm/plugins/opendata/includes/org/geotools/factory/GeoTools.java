/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.factory;

import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.geotools.resources.Classes;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.Utilities;
import org.geotools.util.logging.Logging;


/**
 * Static methods relative to the global GeoTools configuration. GeoTools can be configured
 * in a system-wide basis through {@linkplain System#getProperties system properties}, some
 * of them are declared as {@link String} constants in this class.
 * <p>
 * There are many aspects to the configuration of GeoTools:
 * <ul>
 *   <li>Default Settings: Are handled as the Hints returned by {@link #getDefaultHints()}, the
 *       default values can be provided in application code, or specified using system properties.</li>
 *   <li>Integration JNDI: Telling the GeoTools library about the facilities of an application,
 *       or application container takes several forms. This class provides the
 *       {@link #init(InitialContext)} method allowing to tell GeoTools about the JNDI
 *       context to use.</li>
 *   <li>Integration Plugins: If hosting GeoTools in a alternate plugin system such as Spring
 *       or OSGi, application may needs to hunt down the {@code FactoryFinder}s and register
 *       additional "Factory Iterators" for GeoTools to search using the
 *       {@link #addFactoryIteratorProvider} method.</li>
 * </ul>
 *
 * @since 2.4
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.7.4/modules/library/metadata/src/main/java/org/geotools/factory/GeoTools.java $
 * @version $Id: GeoTools.java 38443 2011-12-21 15:17:43Z jdeolive $
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public final class GeoTools {
        
    /**
     * Object to inform about system-wide configuration changes.
     * We use the Swing utility listener list since it is lightweight and thread-safe.
     * Note that it doesn't involve any dependency to the remaining of Swing library.
     */
    private static final EventListenerList LISTENERS = new EventListenerList();

    /**
     * The bindings between {@linkplain System#getProperties system properties} and
     * a hint key. This field must be declared before any call to the {@link #bind}
     * method.
     */
    private static final Map<String, RenderingHints.Key> BINDINGS =
            new HashMap<String, RenderingHints.Key>();

    /**
     * The {@linkplain System#getProperty(String) system property} key for the default value to be
     * assigned to the {@link Hints#CRS_AUTHORITY_EXTRA_DIRECTORY CRS_AUTHORITY_EXTRA_DIRECTORY}
     * hint.
     *
     * @see Hints#CRS_AUTHORITY_EXTRA_DIRECTORY
     * @see #getDefaultHints
     */
    public static final String CRS_AUTHORITY_EXTRA_DIRECTORY =
            "org.geotools.referencing.crs-directory";
    static {
        bind(CRS_AUTHORITY_EXTRA_DIRECTORY, Hints.CRS_AUTHORITY_EXTRA_DIRECTORY);
    }

    /**
     * The {@linkplain System#getProperty(String) system property} key for the default
     * value to be assigned to the {@link Hints#EPSG_DATA_SOURCE EPSG_DATA_SOURCE} hint.
     *
     * @see Hints#EPSG_DATA_SOURCE
     * @see #getDefaultHints
     */
    public static final String EPSG_DATA_SOURCE =
            "org.geotools.referencing.epsg-datasource";
    static {
        bind(EPSG_DATA_SOURCE, Hints.EPSG_DATA_SOURCE);
    }

    /**
     * The {@linkplain System#getProperty(String) system property} key for the default
     * value to be assigned to the {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER
     * FORCE_LONGITUDE_FIRST_AXIS_ORDER} hint.
     *
     * This setting can provide a transition path for projects expecting a (<var>longitude</var>,
     * <var>latitude</var>) axis order on a system-wide level. Application developpers can set the
     * default value as below:
     *
     * <blockquote><pre>
     * System.setProperty(FORCE_LONGITUDE_FIRST_AXIS_ORDER, "true");
     * </pre></blockquote>
     *
     * Note that this system property applies mostly to the default EPSG factory. Most other
     * factories ({@code "CRS"}, {@code "AUTO"}, <cite>etc.</cite>) don't need this property
     * since they use (<var>longitude</var>, <var>latitude</var>) axis order by design.
     *
     * @see Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER
     * @see #getDefaultHints
     */
    public static final String FORCE_LONGITUDE_FIRST_AXIS_ORDER =
            "org.geotools.referencing.forceXY";
    static {
        bind(FORCE_LONGITUDE_FIRST_AXIS_ORDER, Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER);
    }
    
    /**
     * The {@linkplain System#getProperty(String) system property} key for the default
     * value to be assigned to the {@link Hints#
     * RESAMPLE_TOLERANCE} hint.
     *
     * This setting specifies the tolerance used when linearizing warp transformation into
     * piecewise linear ones, by default it is 0.333 pixels
     *
     * @see Hints#RESAMPLE_TOLERANCE
     * @see #getDefaultHints
     */
    public static final String RESAMPLE_TOLERANCE =
            "org.geotools.referencing.resampleTolerance";
    static {
        bind(RESAMPLE_TOLERANCE, Hints.RESAMPLE_TOLERANCE);
    }

    /**
     * Do not allow instantiation of this class.
     */
    private GeoTools() {
    }

    /**
     * Binds the specified {@linkplain System#getProperty(String) system property}
     * to the specified key. Only one key can be binded to a given system property.
     * However the same key can be binded to more than one system property names,
     * in which case the extra system property names are aliases.
     *
     * @param  property The system property.
     * @param  key The key to bind to the system property.
     * @throws IllegalArgumentException if an other key is already bounds
     *         to the given system property.
     */
    private static void bind(final String property, final RenderingHints.Key key) {
        synchronized (BINDINGS) {
            final RenderingHints.Key old = BINDINGS.put(property, key);
            if (old == null) {
                return;
            }
            // Roll back
            BINDINGS.put(property, old);
        }
        throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                "property", property));
    }

    /**
     * Scans {@linkplain System#getProperties system properties} for any property keys
     * defined in this class, and add their values to the specified map of hints. For
     * example if the {@value #FORCE_LONGITUDE_FIRST_AXIS_ORDER} system property is
     * defined, then the {@link Hints#FORCE_LONGITUDE_FIRST_AXIS_ORDER
     * FORCE_LONGITUDE_FIRST_AXIS_ORDER} hint will be added to the set of hints.
     *
     * @return {@code true} if at least one hint changed as a result of this scan,
     *         or {@code false} otherwise.
     */
    static boolean scanForSystemHints(final Hints hints) {
        assert Thread.holdsLock(hints);
        boolean changed = false;
        synchronized (BINDINGS) {
            for (final Map.Entry<String, RenderingHints.Key> entry : BINDINGS.entrySet()) {
                final String propertyKey = entry.getKey();
                final String property;
                try {
                    property = System.getProperty(propertyKey);
                } catch (SecurityException e) {
                    unexpectedException(e);
                    continue;
                }
                if (property != null) {
                    /*
                     * Converts the system property value from String to Object (java.lang.Boolean
                     * or java.lang.Number). We perform this conversion only if the key is exactly
                     * of kind Hints.Key,  not a subclass like ClassKey, in order to avoid useless
                     * class loading on  'getValueClass()'  method invocation (ClassKey don't make
                     * sense for Boolean and Number, which are the only types that we convert here).
                     */
                    Object value = property;
                    final RenderingHints.Key hintKey = entry.getValue();
                    if (hintKey.getClass().equals(Hints.Key.class)) {
                        final Class<?> type = ((Hints.Key) hintKey).getValueClass();
                        if (type.equals(Boolean.class)) {
                            value = Boolean.valueOf(property);
                        } else if (Number.class.isAssignableFrom(type)) try {
                            value = Classes.valueOf(type, property);
                        } catch (NumberFormatException e) {
                            unexpectedException(e);
                            continue;
                        }
                    }
                    final Object old;
                    try {
                        old = hints.put(hintKey, value);
                    } catch (IllegalArgumentException e) {
                        // The property value is illegal for this hint.
                        unexpectedException(e);
                        continue;
                    }
                    if (!changed && !Utilities.equals(old, value)) {
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    /**
     * Logs an exception as if it originated from {@link Hints#scanSystemProperties},
     * since it is the public API that may invokes this method.
     */
    private static void unexpectedException(final Exception exception) {
        Logging.unexpectedException(Hints.class, "scanSystemProperties", exception);
    }

    /**
     * Returns the default set of hints used for the various utility classes.
     * This default set is determined by:
     * <p>
     * <ul>
     *   <li>The {@linplain System#getProperties system properties} available. Some property
     *       keys are enumerated in the {@link GeoTools} class.</li>
     *   <li>Any hints added by call to the {@link Hints#putSystemDefault}
     *       or {@link #init} method.</li>
     * </ul>
     * <p>
     * <b>Long term plan:</b>
     * We would like to transition the utility classes to being injected with their
     * required factories, either by taking Hints as part of their constructor, or
     * otherwise. Making this change would be a three step process 1) create instance
     * methods for each static final class method 2) create an singleton instance of the
     * class 3) change each static final class method into a call to the singleton. With
     * this in place we could then encourage client code to make use of utility class
     * instances before eventually retiring the static final methods.
     *
     * @return A copy of the default hints. It is safe to add to it.
     */
    public static Hints getDefaultHints() {
        return Hints.getDefaults(false);
    }

    /**
     * Adds the specified listener to the list of objects to inform when system-wide
     * configuration changed.
     *
     * @param listener The listener to add.
     */
    public static void addChangeListener(final ChangeListener listener) {
        removeChangeListener(listener); // Ensure singleton.
        LISTENERS.add(ChangeListener.class, listener);
    }

    /**
     * Removes the specified listener from the list of objects to inform when system-wide
     * configuration changed.
     *
     * @param listener The listener to remove.
     */
    public static void removeChangeListener(final ChangeListener listener) {
        LISTENERS.remove(ChangeListener.class, listener);
    }

    /**
     * Informs every listeners that system-wide configuration changed.
     */
    public static void fireConfigurationChanged() {
        final ChangeEvent event = new ChangeEvent(GeoTools.class);
        final Object[] listeners = LISTENERS.getListenerList();
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i+1]).stateChanged(event);
            }
        }
    }
}
