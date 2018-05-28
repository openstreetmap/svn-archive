// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo.utils;

import java.util.List;
import java.util.Map;

/**
 * A map, each entry of which maps a Java raw type to a list of instances of that type.
 * In addition to implementing {@code Map}, the additional type-safe operations
 * {@link #putInstances}, {@link #addInstance} and {@link #getInstances} are available.
 *
 * Inspired from Guava's {@code ClassToInstanceMap}, but handling a list of instances.
 *
 * @param <B> the common supertype that all entries must share
 */
public interface ClassToInstancesMap<B> extends Map<Class<? extends B>, List<B>> {

    /**
     * Returns the values the specified class is mapped to, or {@code null} if no
     * entry for this class is present. This will only return values that were
     * bound to this specific class, not values that may have been bound to a subtype.
     * @param <T> block type
     * @param type type
     * @return the values the specified class is mapped to, or {@code null}
     */
    <T extends B> List<T> getInstances(Class<T> type);

    /**
     * Maps the specified class to the specified values. Does <i>not</i> associate
     * these values with any of the class's supertypes.
     * @param <T> block type
     * @param type type
     * @param values new values
     *
     * @return the values previously associated with this class (possibly {@code
     *     null}), or {@code null} if there was no previous entry.
     */
    <T extends B> List<T> putInstances(Class<T> type, List<T> values);

    /**
     * Adds a new value to the list mapped to the specified class.
     * @param <T> block type
     * @param type type
     * @param value value to add
     * @return {@code true}
     */
    <T extends B> boolean addInstance(Class<T> type, T value);
}
