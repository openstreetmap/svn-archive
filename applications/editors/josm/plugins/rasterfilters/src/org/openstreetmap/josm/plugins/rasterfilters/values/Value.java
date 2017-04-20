package org.openstreetmap.josm.plugins.rasterfilters.values;

import org.openstreetmap.josm.plugins.rasterfilters.model.FilterStateModel;

/**
 * Generic values which are used by {@link FilterStateModel}.
 *
 * @param <T> generic class of the value
 * @author Nipel-Crumple
 */
public interface Value<T> {

    T getValue();

    void setValue(T value);

    String getParameterName();

    void setParameterName(String name);
}
