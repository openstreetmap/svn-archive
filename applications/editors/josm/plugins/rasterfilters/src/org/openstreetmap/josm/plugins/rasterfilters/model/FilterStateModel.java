package org.openstreetmap.josm.plugins.rasterfilters.model;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.openstreetmap.josm.plugins.rasterfilters.values.BooleanValue;
import org.openstreetmap.josm.plugins.rasterfilters.values.ColorValue;
import org.openstreetmap.josm.plugins.rasterfilters.values.SelectValue;
import org.openstreetmap.josm.plugins.rasterfilters.values.SliderValue;
import org.openstreetmap.josm.plugins.rasterfilters.values.Value;
/**
 * Filter state's model which stores all parameters of
 * the filter according to its meta-information.
 * The usual values from meta-information are converted
 * into subtypes of the generic interface {@link Value}
 *
 * @author Nipel-Crumple
 *
 */
public class FilterStateModel {

	private Map<String, Value<?>> params = new HashMap<>();
	private String filterClassName;

	public FilterStateModel() {

	}

	public Map<String, Value<?>> getParams() {
		return params;
	}

	public String getFilterClassName() {
		return filterClassName;
	}

	public void setFilterClassName(String filterClassName) {
		this.filterClassName = filterClassName;
	}

	public void addParams(JsonObject json) {

		String parameterName = json.getString("name");
		String valueType = json.getString("value_type");

		// setting up the beginning state of filter
		// according to its metainfo
		if (json.getString("type").equals("linear_slider")) {

			if (valueType.equals("float") || valueType.equals("double")) {

				double defaultValue = json.getJsonNumber("default")
						.doubleValue();

				SliderValue<Double> value = new SliderValue<>(parameterName,
						defaultValue);
				params.put(parameterName, value);

			} else if (valueType.equals("integer")) {

				int defaultValue = json.getJsonNumber("default").intValue();

				SliderValue<Integer> value = new SliderValue<>(parameterName,
						defaultValue);
				params.put(parameterName, value);

			}

		} else if (json.getString("type").equals("checkbox")) {

			boolean defaultValue = json.getBoolean("default");

			BooleanValue value = new BooleanValue(parameterName, defaultValue);
			params.put(parameterName, value);

		} else if (json.getString("type").equals("select")) {

			String defaultValue = json.getString("default");

			SelectValue<String> value = new SelectValue<>(parameterName,
					defaultValue);
			params.put(parameterName, value);

		} else if (json.getString("type").equals("colorpicker")) {

			JsonObject defaultColorJson = json.getJsonObject("default");
			int r = defaultColorJson.getInt("red");
			int g = defaultColorJson.getInt("green");
			int b = defaultColorJson.getInt("blue");

			Color defaultColor = new Color(r, g, b);

			ColorValue<Color> value = new ColorValue<>(parameterName,
					defaultColor);
			params.put(parameterName, value);

		}
	}

	/**
	 * Method generates json from the current filter's model state.
	 *
	 * @return encoded json which describes current filter's state
	 */
	public JsonObject encodeJson() {

		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

		for (Entry<String, Value<?>> entry : params.entrySet()) {

			Object value = entry.getValue().getValue();

			if (value instanceof String) {

				jsonBuilder.add(entry.getKey(),
						Json.createObjectBuilder().add("value", (String) value)
								.build());

			}

			if (value instanceof Boolean) {
				jsonBuilder.add(entry.getKey(),
						Json.createObjectBuilder()
								.add("value", (Boolean) value).build());
			}

			if (value instanceof Number) {

				if (value instanceof Double) {

					jsonBuilder.add(entry.getKey(), Json.createObjectBuilder()
							.add("value", (Double) value).build());

				} else if (value instanceof Integer) {

					jsonBuilder.add(entry.getKey(), Json.createObjectBuilder()
							.add("value", (Integer) value).build());

				}
			}

			if (value instanceof Color) {

				jsonBuilder.add(entry.getKey(),
							Json.createObjectBuilder()
								.add("value", Json.createObjectBuilder()
												  .add("red", ((Color) value).getRed())
												  .add("green", ((Color) value).getGreen())
												  .add("blue", ((Color) value).getBlue())
												  .build())
								.build());

			}
		}

		return jsonBuilder.build();
	}
}
