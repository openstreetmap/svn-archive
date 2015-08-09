package org.openstreetmap.josm.plugins.rasterfilters.values;

public interface Value<T extends Object> {
	
	public T getValue();
	
	public void setValue(T value);
	
	public String getParameterName();
	
	public void setParameterName(String name);
}
