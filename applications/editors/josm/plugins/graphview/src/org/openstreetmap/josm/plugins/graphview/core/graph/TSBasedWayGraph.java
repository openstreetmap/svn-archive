package org.openstreetmap.josm.plugins.graphview.core.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.plugins.graphview.core.property.GraphEdgePropertyType;
import org.openstreetmap.josm.plugins.graphview.core.property.GraphEdgeSegments;
import org.openstreetmap.josm.plugins.graphview.core.transition.Restriction;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;
import org.openstreetmap.josm.plugins.graphview.core.transition.SegmentNode;
import org.openstreetmap.josm.plugins.graphview.core.transition.TransitionStructure;
import org.openstreetmap.josm.plugins.graphview.core.transition.TransitionStructureObserver;

/**
 * WayGraph that is based on a {@link TransitionStructure} and updated when it changes.
 */
public class TSBasedWayGraph implements WayGraph, TransitionStructureObserver {

	private static final GraphEdgePropertyType<?>[] PROPERTY_TYPES =
		{GraphEdgeSegments.PROPERTY};
		//TODO: -> parameter
	
	private static class GraphNodeImpl implements GraphNode {
		private final SegmentNode node;
		private final Segment segment;
		private final List<GraphEdge> incomingEdges = new ArrayList<GraphEdge>();
		private final List<GraphEdge> outgoingEdges = new ArrayList<GraphEdge>();
		public GraphNodeImpl(SegmentNode node, Segment segment) {
			assert node != null && segment != null;
			assert segment.getNode1() == node || segment.getNode2() == node;
			this.node = node;
			this.segment = segment;
		}
		public SegmentNode getSegmentNode() {
			return node;
		}
		public Segment getSegment() {
			return segment;
		}
		public void addIncomingEdge(GraphEdge edge) {
			assert edge != null;
			incomingEdges.add(edge);
		}
		public Collection<GraphEdge> getInboundEdges() {
			return incomingEdges;
		}
		public void addOutgoingEdge(GraphEdge edge) {
			assert edge != null;
			outgoingEdges.add(edge);
		}
		public Collection<GraphEdge> getOutboundEdges() {
			return outgoingEdges;
		}
		@Override
		public String toString() {
			return "(" + node + "; " + segment + ")";
		}
	}

	private static class GraphEdgeImpl implements GraphEdge {
		
		private final GraphNode startNode;
		private final GraphNode targetNode;
		private final Map<GraphEdgePropertyType<?>, Object> properties;
		
		public GraphEdgeImpl(GraphNode startNode, GraphNode targetNode,
				Map<GraphEdgePropertyType<?>, Object> properties) {
			assert startNode != null && targetNode != null && properties != null;
			this.startNode = startNode;
			this.targetNode = targetNode;
			this.properties = properties;
		}
		
		public GraphNode getStartNode() {
			return startNode;
		}
		public GraphNode getTargetNode() {
			return targetNode;
		}
		
		public Collection<GraphEdgePropertyType<?>> getAvailableProperties() {
			return properties.keySet();
		}
		public <V> V getPropertyValue(GraphEdgePropertyType<V> property) {
			V result = (V) properties.get(property);
			return result;
		}
		
		@Override
		public String toString() {
			return "(" + startNode + "-->" + targetNode + ")";
		}
		
	};

	private final Set<WayGraphObserver> observers = new HashSet<WayGraphObserver>();

	private final TransitionStructure transitionStructure;

	private Collection<GraphNode> nodes;
	private List<GraphEdge> edges;

	/**
	 * create a WayGraph based on a {@link TransitionStructure}
	 * @param transitionStructure  transition structure this graph is to be based on; != null
	 */
	public TSBasedWayGraph(TransitionStructure transitionStructure) {
		assert transitionStructure != null;

		this.transitionStructure = transitionStructure;
		transitionStructure.addObserver(this);

		createNodesAndEdges();
	}

	public Collection<GraphEdge> getEdges() {
		return edges;
	}

	public Collection<GraphNode> getNodes() {
		return nodes;
	}

	private void createNodesAndEdges() {

		Collection<EvaluationGroup> evaluationGroups =
			createEvaluationGroups(transitionStructure);

		for (EvaluationGroup evaluationGroup : evaluationGroups) {
			evaluationGroup.evaluate(transitionStructure.getRestrictions());
		}

		createNodesAndEdgesFromEvaluationGroups(evaluationGroups);

		evaluationGroups = null;
	}

	private static Collection<EvaluationGroup> createEvaluationGroups(
			TransitionStructure transitionStructure) {

		Map<SegmentNode, Set<SegmentNode>> nodeSetMap =
			new HashMap<SegmentNode, Set<SegmentNode>>();

		/* first step: everything that is part of the same restriction goes into the same set */

		for (Restriction restriction : transitionStructure.getRestrictions()) {

			/* group every node in via segments (which includes the
			 * last node of from and the first node of to) into a set */

			SegmentNode firstNode = restriction.getFrom().getNode2();
			createSetIfHasNone(firstNode, nodeSetMap);

			for (Segment segment : restriction.getVias()) {
				putInSameSet(segment.getNode1(), firstNode, nodeSetMap);
				putInSameSet(segment.getNode2(), firstNode, nodeSetMap);
			}

			for (Segment segment : restriction.getTos()) {
				putInSameSet(segment.getNode1(), firstNode, nodeSetMap);
			}

		}

		/* second step: create own sets for each junction and end point
		 * (node connected with more than / less than two nodes). */

		for (SegmentNode node : transitionStructure.getNodes()) {

			if (!nodeSetMap.containsKey(node)
					&& !isConnectedWithExactly2Nodes(node)) {

				createSetIfHasNone(node, nodeSetMap);

			}

		}

		/* third step: create segment sets for all segments that are not in one of the node sets
		 * (that is, at least one node is not part of a junction evaluation group
		 *  or the nodes are part of different junction evaluation groups)  */

		Map<Segment, Set<Segment>> segmentSetMap =
			new HashMap<Segment, Set<Segment>>();

		for (Segment segment : transitionStructure.getSegments()) {

			SegmentNode node1 = segment.getNode1();
			SegmentNode node2 = segment.getNode2();

			if (!nodeSetMap.containsKey(node1) || !nodeSetMap.containsKey(node2)
					|| nodeSetMap.get(node1) != nodeSetMap.get(node2)) {

				createSetIfHasNone(segment, segmentSetMap);

				for (Segment subsequentSegment : segment.getNode2().getOutboundSegments()) {
					if (!nodeSetMap.containsKey(node2)
							|| subsequentSegment.getNode2() == node1) {
						putInSameSet(subsequentSegment, segment, segmentSetMap);
					}
				}
				//note that segments leading to this segment will share sets anyway,
				//because this segment is a subsequent segment of them


			}

		}

		/* create EvaluationGroup objects */

		Collection<EvaluationGroup> evaluationGroups =
			new ArrayList<EvaluationGroup>(nodeSetMap.size() + segmentSetMap.size());

		Set<Set<SegmentNode>> nodeSets = new HashSet<Set<SegmentNode>>(nodeSetMap.values());
		for (Set<SegmentNode> nodeSet : nodeSets) {
			evaluationGroups.add(new JunctionEvaluationGroup(nodeSet));
		}

		HashSet<Set<Segment>> hashSets = new HashSet<Set<Segment>>(segmentSetMap.values());
		for (Set<Segment> segmentSet : hashSets) {
			Set<SegmentNode> borderNodes = new HashSet<SegmentNode>();
			for (Segment segment : segmentSet) {
				if (nodeSetMap.containsKey(segment.getNode1())) {
					borderNodes.add(segment.getNode1());
				}
				if (nodeSetMap.containsKey(segment.getNode2())) {
					borderNodes.add(segment.getNode2());
				}
			}
			evaluationGroups.add(new ConnectorEvaluationGroup(segmentSet, borderNodes));
		}

		return evaluationGroups;
	}

	private void createNodesAndEdgesFromEvaluationGroups(
			Collection<EvaluationGroup> evaluationGroups) {

		nodes = new LinkedList<GraphNode>();
		edges = new LinkedList<GraphEdge>();

		//map from Segments to GraphNodes;
		//for those GraphNodes representing an "approaching node on segment" state
		final Map<Segment, GraphNodeImpl> segment2GNMap_approaching =
			new HashMap<Segment, GraphNodeImpl>();

		//map from Segments to GraphNodes;
		//for those GraphNodes representing a "leaving node on segment" state
		final Map<Segment, GraphNodeImpl> segment2GNMap_leaving =
			new HashMap<Segment, GraphNodeImpl>();

		//map from SegmentNodes to GraphNode collections;
		//for those GraphNodes representing an "approaching node on segment" state
		final Map<SegmentNode, Collection<GraphNodeImpl>> segNode2GNMap_approaching =
			new HashMap<SegmentNode, Collection<GraphNodeImpl>>();

		//map from SegmentNodes to GraphNodes collections;
		//for those GraphNodes representing a "leaving node on segment" state
		final Map<SegmentNode, Collection<GraphNodeImpl>> segNode2GNMap_leaving =
			new HashMap<SegmentNode, Collection<GraphNodeImpl>>();



		/* create graph nodes and edges for junction evaluation groups */

		for (EvaluationGroup evaluationGroup : evaluationGroups) {
			if (evaluationGroup instanceof JunctionEvaluationGroup) {

				JunctionEvaluationGroup junctionEG = (JunctionEvaluationGroup) evaluationGroup;

				//create graph nodes
				for (Segment segment : junctionEG.getInboundSegments()) {
					GraphNodeImpl graphNode = new GraphNodeImpl(segment.getNode2(), segment);
					nodes.add(graphNode);
					segment2GNMap_approaching.put(segment, graphNode);
					addToCollectionMap(segNode2GNMap_approaching, segment.getNode2(), graphNode);
				}
				for (Segment segment : junctionEG.getOutboundSegments()) {
					GraphNodeImpl graphNode = new GraphNodeImpl(segment.getNode1(), segment);
					nodes.add(graphNode);
					segment2GNMap_leaving.put(segment, graphNode);
					addToCollectionMap(segNode2GNMap_leaving, segment.getNode1(), graphNode);
				}

				//create graph edges for all segment sequences between in- and outbound edges
				for (Segment inboundSegment : junctionEG.getInboundSegments()) {
					for (Segment outboundSegment : junctionEG.getOutboundSegments()) {

						List<Segment> segmentSequence =
							junctionEG.getSegmentSequence(inboundSegment, outboundSegment);

						if (segmentSequence != null) {

							createGraphEdge(
									segment2GNMap_approaching.get(inboundSegment),
									segment2GNMap_leaving.get(outboundSegment),
									segmentSequence,
									junctionEG);

						}
					}
				}

			}
		}

		/* create graph edges for connector evaluation groups.
		 * Because GraphNodes are created for pairs of SegmentNodes (from connector groups)
		 * and Segments (from junction groups), the GraphNodes already exist.
		 */

		for (EvaluationGroup evaluationGroup : evaluationGroups) {
			if (evaluationGroup instanceof ConnectorEvaluationGroup) {

				ConnectorEvaluationGroup connectorEG = (ConnectorEvaluationGroup) evaluationGroup;

				for (SegmentNode startNode : connectorEG.getBorderNodes()) {
					for (SegmentNode targetNode : connectorEG.getBorderNodes()) {

						if (segNode2GNMap_leaving.containsKey(startNode)
								&& segNode2GNMap_approaching.containsKey(targetNode)) {

							for (GraphNodeImpl startGraphNode : segNode2GNMap_leaving.get(startNode)) {
								for (GraphNodeImpl targetGraphNode : segNode2GNMap_approaching.get(targetNode)) {

									if (connectorEG.getSegments().contains(startGraphNode.getSegment())
											&& connectorEG.getSegments().contains(targetGraphNode.getSegment())) {

										List<Segment> segmentSequence =
											connectorEG.getSegmentSequence(startNode, targetNode);

										if (segmentSequence != null) {
											createGraphEdge(
													startGraphNode,
													targetGraphNode,
													segmentSequence,
													connectorEG);
										}

									}

								}
							}

						}

					}
				}

			}
		}

	}

	private void createGraphEdge(
			GraphNodeImpl startNode, GraphNodeImpl targetNode, 
			List<Segment> segments, ConnectorEvaluationGroup evaluationGroup) {

		Map<GraphEdgePropertyType<?>, Object> properties = 
			new HashMap<GraphEdgePropertyType<?>, Object>(); //TODO: replace HashMap with List-based solution
		
		for (GraphEdgePropertyType<?> propertyType : PROPERTY_TYPES) {
			Object value = propertyType.evaluate(evaluationGroup, segments, transitionStructure);
			properties.put(propertyType, value);
		}
		
		createGraphEdge(startNode, targetNode, properties);

	}

	private void createGraphEdge(
			GraphNodeImpl startNode, GraphNodeImpl targetNode, 
			List<Segment> segments, JunctionEvaluationGroup evaluationGroup) {

		Map<GraphEdgePropertyType<?>, Object> properties = 
			new HashMap<GraphEdgePropertyType<?>, Object>(); //TODO: replace HashMap with List-based solution
		
		for (GraphEdgePropertyType<?> propertyType : PROPERTY_TYPES) {
			Object value = propertyType.evaluate(evaluationGroup, segments, transitionStructure);
			properties.put(propertyType, value);
		}
		
		createGraphEdge(startNode, targetNode, properties);

	}

	/**
	 * creates a GraphEdge;
	 * adds it to its nodes' collections and {@link #edges} collection.
	 */
	private void createGraphEdge(GraphNodeImpl startNode, GraphNodeImpl targetNode, 
			Map<GraphEdgePropertyType<?>, Object> properties) {

		GraphEdge newEdge = new GraphEdgeImpl(startNode, targetNode, properties);

		startNode.addOutgoingEdge(newEdge);
		targetNode.addIncomingEdge(newEdge);

		edges.add(newEdge);

	}
	
	private static boolean isConnectedWithExactly2Nodes(SegmentNode node) {

		Set<SegmentNode> connectedNodes = new HashSet<SegmentNode>(2);

		for (Segment segment : node.getInboundSegments()) {
			connectedNodes.add(segment.getNode1());
		}
		for (Segment segment : node.getOutboundSegments()) {
			connectedNodes.add(segment.getNode2());
		}

		return connectedNodes.size() == 2;
	}

	/**
	 * creates a set for an object if none exists in a map.
	 * The set will contain the object and be added to the map with the object being its key.
	 */
	private static <T> void createSetIfHasNone(T object, Map<T, Set<T>> objectSetMap) {

		if (!objectSetMap.containsKey(object)) {
			@SuppressWarnings("unchecked") //no set with generic parameter can be created directly
			Set<T> set = new HashSet();
			set.add(object);
			objectSetMap.put(object, set);
		}

	}

	/**
	 * puts an object in another object's set.
	 * If both nodes have sets already, these sets are merged.
	 * The objectSetMap is modified accordingly.
	 *
	 * @param object        object that might or might not be in a set; != null
	 * @param objectInSet   object that is guaranteed to be in a set; != null
	 * @param objectSetMap  map from objects to the one set they are part of; != null
	 */
	private static <T> void putInSameSet(T object, T objectInSet, Map<T, Set<T>> objectSetMap) {
		assert object != null && objectInSet != null && objectSetMap != null;
		assert objectSetMap.containsKey(objectInSet);

		Set<T> set = objectSetMap.get(objectInSet);

		if (objectSetMap.containsKey(object)) {

			/* merge the two sets */
			Set<T> oldSet = objectSetMap.get(object);
			for (T objectFromOldSet : oldSet) {
				set.add(objectFromOldSet);
				objectSetMap.put(objectFromOldSet, set);
			}

		} else {

			/* add object to objectInSet's set */
			set.add(object);
			objectSetMap.put(object, set);

		}

	}

	private static <K, E> void addToCollectionMap(final Map<K, Collection<E>> map, K key, E entry) {
		if (!map.containsKey(key)) {
			Collection<E> newCollection = new ArrayList<E>();
			map.put(key, newCollection);
		}
		map.get(key).add(entry);
	}

	public void update(TransitionStructure transitionStructure) {
		createNodesAndEdges();
		notifyObservers();
	}

	public void addObserver(WayGraphObserver observer) {
		observers.add(observer);
	}

	public void deleteObserver(WayGraphObserver observer) {
		observers.remove(observer);
	}

	private void notifyObservers() {
		for (WayGraphObserver observer : observers) {
			observer.update(this);
		}
	}

}