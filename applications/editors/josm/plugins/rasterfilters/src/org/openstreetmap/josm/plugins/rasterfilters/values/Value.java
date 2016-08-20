package org.openstreetmap.josm.plugins.rasterfilters.values;

import org.openstreetmap.josm.plugins.rasterfilters.model.FilterStateModel;

/**
 * Generic values which are used by {@link FilterStateModel}.
 *
 * @author Nipel-Crumple
 *
 * @param <T> generic class of the value
 */
public interface Value<T extends Object> {

	public T getValue();

	public void setValue(T value);

	public String getParameterName();

	public void setParameterName(String name);
}
