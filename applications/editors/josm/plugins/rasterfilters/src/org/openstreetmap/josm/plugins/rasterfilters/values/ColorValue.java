package org.openstreetmap.josm.plugins.rasterfilters.values;

public class ColorValue<Color> implements Value<Color>{

	private Color value;
	private String parameterName;

	public ColorValue(String parameterName, Color color) {
		this.parameterName = parameterName;
		this.value = color;
	}

	@Override
	public Color getValue() {
		return value;
	}

	@Override
	public void setValue(Color value) {
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

}
