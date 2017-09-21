// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo.utils;

import java.util.HashMap;
import java.util.List;

/**
 * A mutable class-to-instances map.
 *
 * @param <B> the common supertype that all entries must share
 */
public class MutableClassToInstancesMap<B> extends HashMap<Class<? extends B>, List<B>> implements ClassToInstancesMap<B> {

    @Override
    @SuppressWarnings("unchecked")
    public <T extends B> List<T> getInstances(Class<T> type) {
        return (List<T>) get(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends B> List<T> putInstances(Class<T> type, List<T> values) {
        return (List<T>) put(type, (List<B>) values);
    }

    @Override
    public <T extends B> boolean addInstance(Class<T> type, T value) {
        return get(type).add(value);
    }
}
