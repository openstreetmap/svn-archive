package org.openstreetmap.josm.plugins.graphview.core.property;

import java.util.List;

import org.openstreetmap.josm.plugins.graphview.core.graph.ConnectorEvaluationGroup;
import org.openstreetmap.josm.plugins.graphview.core.graph.GraphEdge;
import org.openstreetmap.josm.plugins.graphview.core.graph.JunctionEvaluationGroup;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;
import org.openstreetmap.josm.plugins.graphview.core.transition.TransitionStructure;

/**
 * type of a {@link GraphEdge} property
 *
 * GraphEdgePropertyType objects should be stateless (except for performance speedups).
 *
 * @param <V>  property value type
 */
public interface GraphEdgePropertyType<V> {
	
	/**
	 * determines the property value for segments created from junction groups
	 */
	public V evaluate(JunctionEvaluationGroup junctionGroup,
			List<Segment> segmentSequence,
			TransitionStructure transitionStructure);

	/**
	 * determines the property value for segments created from connector groups
	 */
	public V evaluate(ConnectorEvaluationGroup connectorGroup,
			List<Segment> segmentSequence,
			TransitionStructure transitionStructure);
	
}
