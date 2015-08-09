package org.openstreetmap.josm.plugins.rasterfilters.values;

public class SliderValue<Number> implements Value<Number> {

	private String parameterName;
	private Number value;
	
	public SliderValue(String parameterName, Number value) {
		this.value = value;
		this.parameterName = parameterName;
	}
	
	@Override
	public Number getValue() {
		return value;
	}

	@Override
	public void setValue(Number value) {
		this.value = value;
	}

	@Override
	public String getParameterName() {
		return parameterName;
	}
	
	@Override
	public void setParameterName(String name) {
		this.parameterName = name;;
	}
	
	public boolean isDouble() {
		if (value instanceof Double) 
			return true;
		return false;
	}
}
