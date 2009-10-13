package org.openstreetmap.josm.plugins.graphview.core.property;

import java.util.List;

import org.openstreetmap.josm.plugins.graphview.core.graph.ConnectorEvaluationGroup;
import org.openstreetmap.josm.plugins.graphview.core.graph.JunctionEvaluationGroup;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;
import org.openstreetmap.josm.plugins.graphview.core.transition.TransitionStructure;

/**
 * the series of segments that are represented by a GraphEdge. Requesting this
 * property for the graph that is being constructed will preserve information
 * from the {@link TransitionStructure}.
 * 
 * TODO: for some purposes, segments are not needed (only coordinate lists;
 * without properties etc.)
 */
public final class GraphEdgeSegments implements GraphEdgePropertyType<List<Segment>> {
	
	public static final GraphEdgeSegments PROPERTY = new GraphEdgeSegments();
	
	/**
	 * private constructor to make sure that {@link #INSTANCE} is the only instance
	 */
	private GraphEdgeSegments() { }
	
	public List<Segment> evaluate(JunctionEvaluationGroup junctionGroup,
			List<Segment> segmentSequence, TransitionStructure transitionStructure) {
		return segmentSequence;
	}
	
	public List<Segment> evaluate(ConnectorEvaluationGroup connectorGroup,
			List<Segment> segmentSequence, TransitionStructure transitionStructure) {
		return segmentSequence;
	}
	
}