package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.plugins.graphview.core.graph.GraphEdge;
import org.openstreetmap.josm.plugins.graphview.core.graph.GraphNode;
import org.openstreetmap.josm.plugins.graphview.core.property.GraphEdgeSegments;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadPropertyType;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;

/**
 * scheme using values of a property to determine a color.
 * Nodes are given an average color of all incoming and outgoing segments.
 */
public class FloatPropertyColorScheme implements ColorScheme {

	private final Class<? extends RoadPropertyType<Float>> propertyClass;
	private final Map<Float, Color> colorMap;
	private final Color defaultColor;

	/**
	 * @param propertyClass  property type to get values for; != null
	 * @param colorMap       map from some values to colors.
	 *                       Colors for all other values are interpolated.
	 *                       This map will be copied and not used directly later. != null
	 * @param defaultColor   color that is used when the property is not available; != null
	 */
	public FloatPropertyColorScheme(Class<? extends RoadPropertyType<Float>> propertyClass,
			Map<Float, Color> colorMap, Color defaultColor) {
		assert propertyClass != null && colorMap != null && defaultColor != null;

		this.propertyClass = propertyClass;
		this.colorMap = new HashMap<Float, Color>(colorMap);
		this.defaultColor = defaultColor;
	}

	public Color getSegmentColor(Segment segment) {
		assert segment != null;

		Float propertyValue = null;
		Collection<RoadPropertyType<?>> availableProperties = segment.getAvailableProperties();
		for (RoadPropertyType<?> property : availableProperties) {
			if (propertyClass.isInstance(property)) {
				@SuppressWarnings("unchecked") //has been checked using isInstance
				RoadPropertyType<Float> floatProperty = (RoadPropertyType<Float>)property;
				propertyValue = segment.getPropertyValue(floatProperty);
				break;
			}
		}

		if (propertyValue != null) {
			return getColorForValue(propertyValue);
		} else {
			return defaultColor;
		}
	}

	public Color getNodeColor(GraphNode node) {

		List<Color> segmentColors = new ArrayList<Color>();

		
		
		for (GraphEdge edge : node.getInboundEdges()) {
			List<Segment> edgeSegments = edge.getPropertyValue(GraphEdgeSegments.PROPERTY);
			if (edgeSegments.size() > 0) {
				Segment firstSegment = edgeSegments.get(0);
				segmentColors.add(getSegmentColor(firstSegment));
			}
		}
		for (GraphEdge edge : node.getOutboundEdges()) {
			List<Segment> edgeSegments = edge.getPropertyValue(GraphEdgeSegments.PROPERTY);
			if (edgeSegments.size() > 0) {
				Segment lastSegment = edgeSegments.get(edgeSegments.size()-1);
				segmentColors.add(getSegmentColor(lastSegment));
			}
		}

		if (segmentColors.size() > 0) {
			return averageColor(segmentColors);
		} else {
			return Color.WHITE;
		}

	}

	/**
	 * returns the color for a value
	 * @param value  value to get color for; != null
	 * @return       color; != null
	 */
	protected Color getColorForValue(Float value) {
		assert value != null;

		if (colorMap.containsKey(value)) {

			return colorMap.get(value);

		} else {

			LinkedList<Float> valuesWithDefinedColor = new LinkedList<Float>(colorMap.keySet());
			Collections.sort(valuesWithDefinedColor);

			if (value <= valuesWithDefinedColor.getFirst()) {

				return colorMap.get(valuesWithDefinedColor.getFirst());

			} else if (value >= valuesWithDefinedColor.getLast()) {

				return colorMap.get(valuesWithDefinedColor.getLast());

			} else {

				/* interpolate */

				Float lowerValue = valuesWithDefinedColor.getFirst();
				Float higherValue = null;

				for (Float v : valuesWithDefinedColor) {
					if (v >= value) {
						higherValue = v;
						break;
					}
					lowerValue = v;
				}

				assert lowerValue != null && higherValue != null;

				Color lowerColor = colorMap.get(lowerValue);
				Color higherColor = colorMap.get(higherValue);

				float weightHigherColor = (value - lowerValue) / (higherValue - lowerValue);

				return weightedAverageColor(lowerColor, higherColor, weightHigherColor);

			}

		}

	}

	/**
	 * returns an average of all colors that have been passed as parameter
	 *
	 * @param colors  colors to calculate average from; not empty or null
	 * @return        average color; != null
	 */
	private static Color averageColor(List<Color> colors) {
		assert colors != null && colors.size() > 0;

		float weightPerColor = 1.0f / colors.size();

		Color average = new Color(0,0,0);

		for (Color color : colors) {
			average = new Color(
					Math.min(Math.round(average.getRed() + weightPerColor*color.getRed()), 255),
					Math.min(Math.round(average.getGreen() + weightPerColor*color.getGreen()), 255),
					Math.min(Math.round(average.getBlue() + weightPerColor*color.getBlue()), 255));
		}

		return average;
	}

	/**
	 * returns a weighted average of two colors
	 *
	 * @param color1        first color for the average; != null
	 * @param color2        second color for the average; != null
	 * @param weightColor2  weight of color2; must be in [0..1]
	 * @return              average color; != null
	 */
	private static Color weightedAverageColor(Color color1, Color color2, float weightColor2) {
		assert color1 != null && color2 != null;
		assert 0 <= weightColor2 && weightColor2 <= 1;

		return new Color(
				Math.round((1 - weightColor2) * color1.getRed() + weightColor2 * color2.getRed()),
				Math.round((1 - weightColor2) * color1.getGreen() + weightColor2 * color2.getGreen()),
				Math.round((1 - weightColor2) * color1.getBlue() + weightColor2 * color2.getBlue()));
	}

}
