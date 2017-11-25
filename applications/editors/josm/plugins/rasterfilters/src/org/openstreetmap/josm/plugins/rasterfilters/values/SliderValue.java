package org.openstreetmap.josm.plugins.rasterfilters.values;

/**
 * @author Nipel-Crumple
 */
public class SliderValue<T> implements Value<T> {

    private String parameterName;
    private T value;

    public SliderValue(String parameterName, T value) {
        this.value = value;
        this.parameterName = parameterName;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public void setParameterName(String name) {
        this.parameterName = name;
    }

    public boolean isDouble() {
        return value instanceof Double;
    }
}
