package org.openstreetmap.josm.plugins.graphview.core.transition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.plugins.graphview.core.access.AccessEvaluator;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessParameters;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessRuleset;
import org.openstreetmap.josm.plugins.graphview.core.access.RulesetAccessEvaluator;
import org.openstreetmap.josm.plugins.graphview.core.data.DataSource;
import org.openstreetmap.josm.plugins.graphview.core.data.DataSourceObserver;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadPropertyType;

/**
 * generic TransitionStructure implementation using a {@link DataSource} to access OSM data
 *
 * @param <N>  node type
 * @param <W>  way type
 * @param <R>  relation type
 */
public class GenericTransitionStructure<N, W, R> implements TransitionStructure, DataSourceObserver {

	private static final Collection<Segment> EMPTY_SEGMENT_LIST =
		Collections.unmodifiableList(new ArrayList<Segment>(0));
	private static final Collection<Restriction> EMPTY_RESTRICTION_COLLECTION =
		new ArrayList<Restriction>(0);

	private static class SegmentNodeImpl implements SegmentNode {
		private final double lat;
		private final double lon;
		private final List<Segment> inboundSegments = new LinkedList<Segment>();
		private final List<Segment> outboundSegments = new LinkedList<Segment>();
		private final Map<RoadPropertyType<?>, Object> properties;
		public SegmentNodeImpl(double lat, double lon, Map<RoadPropertyType<?>, Object> properties) {
			assert properties != null;
			this.lat = lat;
			this.lon = lon;
			this.properties = properties;
		}
		public double getLat() {
			return lat;
		}
		public double getLon() {
			return lon;
		}
		public void addInboundSegment(Segment segment) {
			inboundSegments.add(segment);
		}
		public void addOutboundSegment(Segment segment) {
			outboundSegments.add(segment);
		}
		public Collection<Segment> getOutboundSegments() {
			return outboundSegments;
		}
		public Collection<Segment> getInboundSegments() {
			return inboundSegments;
		}

		public <P> void setProperty(RoadPropertyType<P> property, P value) {
			properties.put(property, value);
		}
		public Collection<RoadPropertyType<?>> getAvailableProperties() {
			return properties.keySet();
		}
		public <P> P getPropertyValue(RoadPropertyType<P> property) {
			@SuppressWarnings("unchecked") //cast is safe due to type parameter of setProperty
			P result = (P) properties.get(property);
			return result;
		}
		public Map<RoadPropertyType<?>, Object> getProperties() {
			return properties;
		}

		@Override
		public String toString() {
			return "(" + lat + ", " + lon + ")";
		}
	}

	private static class SegmentImpl implements Segment {
		private final SegmentNode node1;
		private final SegmentNode node2;
		private final Map<RoadPropertyType<?>, Object> properties;
		public SegmentImpl(SegmentNode node1, SegmentNode node2, Map<RoadPropertyType<?>, Object> properties) {
			this.node1 = node1;
			this.node2 = node2;
			this.properties = properties;
		}
		public SegmentNode getNode1() {
			return node1;
		}
		public SegmentNode getNode2() {
			return node2;
		}
		public <P> void setProperty(RoadPropertyType<P> property, P value) {
			properties.put(property, value);
		}
		public Collection<RoadPropertyType<?>> getAvailableProperties() {
			return properties.keySet();
		}
		public <P> P getPropertyValue(RoadPropertyType<P> property) {
			@SuppressWarnings("unchecked") //cast is safe due to type parameter of setProperty
			P result = (P) properties.get(property);
			return result;
		}

		@Override
		public String toString() {
			return "(" + node1 + "->" + node2 + ")";
		}
	}

	private static class RestrictionImpl implements Restriction {
		private final Segment from;
		private final Collection<Segment> vias;
		private final Collection<Segment> tos;

		/** constructor, will directly use collection references, collections must not be changed after usage as constructor param */
		public RestrictionImpl(Segment from, Collection<Segment> vias, Collection<Segment> tos) {
			this.from = from;
			this.vias = Collections.unmodifiableCollection(vias);
			this.tos = Collections.unmodifiableCollection(tos);
		}

		public Segment getFrom() {
			return from;
		}
		public Collection<Segment> getVias() {
			return vias;
		}
		public Collection<Segment> getTos() {
			return tos;
		}

		@Override
		public String toString() {
			return from + " -> " + vias + " -> " + tos;
		}
	}

	private final Class<N> nodeClass;
	private final Class<W> wayClass;
	private final Class<R> relationClass;

	private final Set<TransitionStructureObserver> observers = new HashSet<TransitionStructureObserver>();

	private final Collection<RoadPropertyType<?>> properties;

	private final DataSource<N, W, R> dataSource;

	private AccessParameters accessParameters;
	private AccessRuleset ruleset;

	private AccessEvaluator<N, W> accessEvaluator;

	private Collection<SegmentNode> nodes = null;
	private Collection<Segment> segments = new LinkedList<Segment>();
	private Collection<Restriction> restrictions = new LinkedList<Restriction>();

	public GenericTransitionStructure(
			Class<N> nodeClass, Class<W> wayClass, Class<R> relationClass,
			AccessParameters accessParameters, AccessRuleset ruleset,
			DataSource<N, W, R> dataSource,
			Collection<RoadPropertyType<?>> properties) {

		assert nodeClass != null && wayClass != null && relationClass != null;
		assert accessParameters != null && ruleset != null;
		assert dataSource != null;
		assert properties != null;

		this.nodeClass = nodeClass;
		this.wayClass = wayClass;
		this.relationClass = relationClass;

		this.dataSource = dataSource;

		this.properties = properties;

		setAccessParametersAndRuleset(accessParameters, ruleset);

		dataSource.addObserver(this);
	}

	/**
	 * sets new access parameters and/or a new ruleset.
	 * Causes a data update if at least one is actually changed.
	 *
	 * @param accessParameters  new access parameters, null indicates no change
	 * @param ruleset           new ruleset, null indicates no change
	 */
	public void setAccessParametersAndRuleset(AccessParameters accessParameters, AccessRuleset ruleset) {

		if (accessParameters != null) {
			this.accessParameters = accessParameters;
		}
		if (ruleset != null) {
			this.ruleset = ruleset;
		}

		if (accessParameters != null || ruleset != null) {

			assert dataSource != null;

			accessEvaluator = new RulesetAccessEvaluator<N, W, R>(
					dataSource,
					this.ruleset,
					this.accessParameters);

			updateData();
			notifyObservers();

		}

	}

	public Collection<SegmentNode> getNodes() {
		return nodes;
	}

	public Collection<Segment> getSegments() {
		return segments;
	}

	public Collection<Restriction> getRestrictions() {
		return restrictions;
	}

	/**
	 * creates nodes, segments and restrictions based on the data source
	 */
	protected void updateData() {

		ArrayList<SegmentNode> nodes = new ArrayList<SegmentNode>();
		ArrayList<Segment> segments = new ArrayList<Segment>();

		Map<N, SegmentNodeImpl> nodeCreationMap = new HashMap<N, SegmentNodeImpl>();
		Map<W, List<Segment>> waySegmentMap = new HashMap<W, List<Segment>>();

		/* create segments (nodes are created only when included in a segment) */

		for (W way : dataSource.getWays()) {
			createSegmentsAndSegmentNodes(way, accessEvaluator, nodes, segments, nodeCreationMap, waySegmentMap);
		}

		nodes.trimToSize();
		segments.trimToSize();

		/* create restrictions */

		Collection<Restriction> restrictions =
			createRestrictionsFromTurnRestrictions(dataSource.getRelations(), nodeCreationMap, waySegmentMap);

		restrictions.addAll(createRestrictionsFromBarrierNodes(nodeCreationMap, waySegmentMap));

		/* keep data and inform observers */

		this.nodes = nodes;
		this.segments = segments;
		this.restrictions = restrictions;

		notifyObservers();

	}

	/**
	 * creates all Segments and SegmentNodes for a way
	 *
	 * @param way                 way to create Segments and SegmentNodes from; != null
	 * @param wayAccessEvaluator  evaluator object that decides whether way is usable; != null
	 * @param nodes               collection of SegmentNodes, new SegmentNodes will be added here; != null
	 * @param segments            collection of Segments, new Segments will be added here; != null
	 * @param nodeCreationMap     map providing the SegmentNode that has been created from a Node,
	 *                            if new SegmentNodes are created, they will be added appropriately; != null
	 * @param waySegmentMap       map providing the Segments that have been created from a Way,
	 *                            if new Segments are created, they will be added appropriately; != null
	 */
	private void createSegmentsAndSegmentNodes(W way, AccessEvaluator<N, W> wayAccessEvaluator,
			Collection<SegmentNode> nodes, Collection<Segment> segments,
			Map<N, SegmentNodeImpl> nodeCreationMap, Map<W, List<Segment>> waySegmentMap) {

		assert way != null && wayAccessEvaluator != null && nodes != null && segments != null && nodeCreationMap != null && waySegmentMap != null;

		/* calculate property values */

		Map<RoadPropertyType<?>, Object> forwardPropertyValues = getWayPropertyMap(way, true);
		Map<RoadPropertyType<?>, Object> backwardPropertyValues = getWayPropertyMap(way, false);

		/* create segments from the way if it can be accessed and isn't incomplete or deleted */

		boolean forwardAccess = wayAccessEvaluator.wayUsable(way, true, forwardPropertyValues);
		boolean backwardAccess = wayAccessEvaluator.wayUsable(way, false, backwardPropertyValues);

		if (forwardAccess || backwardAccess) {

			if (!waySegmentMap.containsKey(way)) {
				waySegmentMap.put(way, new LinkedList<Segment>());
			}

			/* create segments from all pairs of subsequent nodes */

			N previousNode = null;
			for (N node : dataSource.getNodes(way)) {
				if (previousNode != null) {

					SegmentNodeImpl node1 =
						getOrCreateSegmentNodeForNode(previousNode, nodes, nodeCreationMap);
					SegmentNodeImpl node2 =
						getOrCreateSegmentNodeForNode(node, nodes, nodeCreationMap);

					if (forwardAccess) {
						SegmentImpl segment = new SegmentImpl(node1, node2, forwardPropertyValues);
						segments.add(segment);
						waySegmentMap.get(way).add(segment);
						node1.addOutboundSegment(segment);
						node2.addInboundSegment(segment);
					}
					if (backwardAccess) { //no "else if" because both can be valid
						SegmentImpl segment = new SegmentImpl(node2, node1, backwardPropertyValues);
						segments.add(segment);
						waySegmentMap.get(way).add(segment);
						node1.addInboundSegment(segment);
						node2.addOutboundSegment(segment);
					}

				}
				previousNode = node;
			}

		}
	}

	/**
	 * if no segment node for a node exists in the nodeCreationMap,
	 * creates a segment node for it and adds it to the nodeCreationMap and the nodes collection
	 * and returns it; otherwise returns the existing segment node.
	 */
	private SegmentNodeImpl getOrCreateSegmentNodeForNode(N node,
			Collection<SegmentNode> nodes, Map<N, SegmentNodeImpl> nodeCreationMap) {

		SegmentNodeImpl segmentNode = nodeCreationMap.get(node);

		if (segmentNode == null) {

			Map<RoadPropertyType<?>, Object> nodePropertyValues = getNodePropertyMap(node);
			segmentNode = new SegmentNodeImpl(dataSource.getLat(node), dataSource.getLon(node),
					nodePropertyValues);

			nodeCreationMap.put(node, segmentNode);
			nodes.add(segmentNode);

		}

		return segmentNode;
	}

	/**
	 * creates all Restrictions from a collection of Relations.
	 * Only "type=restriction" relations are relevant for restrictions.
	 *
	 * @param relations        Relations to create Restrictions from.
	 *                         They can have any type key, as filtering is done inside this method.
	 * @param nodeCreationMap  map providing the SegmentNode that has been created from a Node,
	 *                         will not be modified; != null
	 * @param waySegmentMap    map providing the Segments that have been created from a Way,
	 *                         will not be modified; != null
	 * @return                 Restrictions created from the Relations; != null, but may be empty
	 */
	private Collection<Restriction> createRestrictionsFromTurnRestrictions(
			Iterable<R> relations,
			Map<N, SegmentNodeImpl> nodeCreationMap,
			Map<W, List<Segment>> waySegmentMap) {

		assert relations != null && nodeCreationMap != null && waySegmentMap != null;

		Collection<Restriction> results = new LinkedList<Restriction>();

		for (R relation : relations) {

			TagGroup tags = dataSource.getTagsR(relation);

			if ("restriction".equals(tags.getValue("type"))
					&& tags.getValue("restriction") != null ) {

				//evaluate relation
				if (tags.getValue("restriction").startsWith("no_")) {
					results.addAll(createRestrictionsFromRestrictionRelation(relation, true, nodeCreationMap, waySegmentMap));
				} else if (tags.getValue("restriction").startsWith("only_")) {
					results.addAll(createRestrictionsFromRestrictionRelation(relation, false, nodeCreationMap, waySegmentMap));
				}

			}
		}

		return results;
	}

	@SuppressWarnings("unchecked") //several generic casts that are checked with isInstance
	private Collection<Restriction> createRestrictionsFromRestrictionRelation(
			R relation,
			boolean restrictive,
			Map<N, SegmentNodeImpl> nodeCreationMap,
			Map<W, List<Segment>> waySegmentMap) {

		assert relationClass.isInstance(relation);

		/* collect information about the relation */

		W fromWay = null;
		Collection<N> viaNodes = new LinkedList<N>();
		Collection<W> viaWays = new LinkedList<W>();
		Collection<W> toWays = new LinkedList<W>();

		for (DataSource.RelationMember member : dataSource.getMembers(relation)) {

			if ("from".equals(member.getRole())) {
				if (fromWay != null || !wayClass.isInstance(member.getMember())) {
					//broken restriction
					return EMPTY_RESTRICTION_COLLECTION;
				} else {
					fromWay = (W)member.getMember();
				}
			} else if ("to".equals(member.getRole())) {
				if (!wayClass.isInstance(member.getMember())) {
					//broken restriction
					return EMPTY_RESTRICTION_COLLECTION;
				} else {
					toWays.add((W)member.getMember());
				}
			} else if ("via".equals(member.getRole())) {
				if (wayClass.isInstance(member.getMember())) {
					viaWays.add((W)member.getMember());
				} else if (nodeClass.isInstance(member.getMember())) {
					viaNodes.add((N)member.getMember());
				}
			}

		}

		if (fromWay != null && toWays.size() > 0 &&
				(viaNodes.size() > 0 || viaWays.size() > 0)) {

			return createRestrictionsFromRestrictionRelationMembers(
					restrictive, nodeCreationMap, waySegmentMap,
					fromWay, viaNodes, viaWays, toWays);

		} else {
			return new ArrayList<Restriction>(0);
		}
	}

	private Collection<Restriction> createRestrictionsFromRestrictionRelationMembers(
			boolean restrictive,
			Map<N, SegmentNodeImpl> nodeCreationMap, Map<W, List<Segment>> waySegmentMap,
			W fromWay, Collection<N> viaNodes, Collection<W> viaWays, Collection<W> toWays) {

		Collection<SegmentNode> nodesCreatedFromViaNodes = new ArrayList<SegmentNode>(viaNodes.size());
		for (N viaNode : viaNodes) {
			if (nodeCreationMap.containsKey(viaNode)) {
				nodesCreatedFromViaNodes.add(nodeCreationMap.get(viaNode));
			}
		}

		/* check completeness of restriction to avoid dealing with incomplete restriction info */

		if (!waySegmentMap.containsKey(fromWay)) {
			//broken restriction
			return EMPTY_RESTRICTION_COLLECTION;
		}

		for (W viaWay : viaWays) {
			if (!waySegmentMap.containsKey(viaWay)) {
				//broken restriction
				return EMPTY_RESTRICTION_COLLECTION;
			}
		}

		for (W toWay : toWays) {
			if (!waySegmentMap.containsKey(toWay)) {
				//broken restriction
				return EMPTY_RESTRICTION_COLLECTION;
			}
		}

		/* find all via segments:
		 * via segments are segments created from via ways
		 * or segments starting and ending with nodes created from via nodes */

		ArrayList<Segment> viaSegments = new ArrayList<Segment>();

		for (W viaWay : viaWays) {
			viaSegments.addAll(waySegmentMap.get(viaWay));
		}

		for (SegmentNode nodeCreatedFromViaNode : nodesCreatedFromViaNodes) {
			for (Segment segment : nodeCreatedFromViaNode.getOutboundSegments()) {
				if (nodesCreatedFromViaNodes.contains(segment.getNode2())) {
					viaSegments.add(segment);
				}
			}
		}

		viaSegments.trimToSize();

		/* create a set with all nodes that are based on via members */

		Set<SegmentNode> nodesCreatedFromViaMembers
		= new HashSet<SegmentNode>(nodesCreatedFromViaNodes);

		for (W viaWay : viaWays) {
			for (N viaWayNode : dataSource.getNodes(viaWay)) {
				nodesCreatedFromViaMembers.add(nodeCreationMap.get(viaWayNode));
			}
		}

		/*
		 * find from segment and to segments:
		 * Such a segment contains a node based on a via member.
		 * Each way should contain only one possible segment
		 * connecting to via members (due to splitting).
		 */

		Segment fromSegment = null;
		Collection<Segment> toSegments = new ArrayList<Segment>();

		for (Segment possibleFromSegment : waySegmentMap.get(fromWay)) {
			if (nodesCreatedFromViaMembers.contains(possibleFromSegment.getNode2())) {

				if (fromSegment == null) {
					fromSegment = possibleFromSegment;
				} else {
					//broken restriction
					return EMPTY_RESTRICTION_COLLECTION;
				}

			}
		}
		if (fromSegment == null) {
			//broken restriction
			return EMPTY_RESTRICTION_COLLECTION;
		}

		if (restrictive) {

			for (W toWay : toWays) {
				if (waySegmentMap.containsKey(toWay)) {
					Segment toSegment = null;
					for (Segment possibleToSegment : waySegmentMap.get(toWay)) {
						if (nodesCreatedFromViaMembers.contains(possibleToSegment.getNode1())) {

							if (toSegment == null) {
								toSegment = possibleToSegment;
							} else {
								//broken restriction
								return EMPTY_RESTRICTION_COLLECTION;
							}

						}
					}
					if (toSegment == null) {
						//broken restriction
						return EMPTY_RESTRICTION_COLLECTION;
					} else {
						toSegments.add(toSegment);
					}
				}
			}

		} else { //!restrictive

			/* forbidden "to" segments are all segments that start at a "via" node
			 * and are neither a via segment nor created from an allowed "to" way */

			for (SegmentNode toStartingNode : nodesCreatedFromViaMembers) {
				for (Segment outboundSegment : toStartingNode.getOutboundSegments()) {

					if (!viaSegments.contains(outboundSegment)) {

						boolean isAllowed = false;

						for (W toWay : toWays) {
							if (waySegmentMap.get(toWay).contains(outboundSegment)) {
								isAllowed = true;
								break;
							}
						}

						if (!isAllowed) {
							toSegments.add(outboundSegment);
						}

					}

				}
			}

		}

		/* create restriction */

		Collection<Restriction> results = new ArrayList<Restriction>(1);
		results.add(new RestrictionImpl(fromSegment, viaSegments, toSegments));
		return results;
	}

	/**
	 * creates Restrictions from barrier nodes (nodes that are considered impassable by the
	 * {@link #accessEvaluator}). These restrictions prevent moving from a segment before the
	 * barrier node to a segment after the barrier node.
	 *
	 * @param nodeCreationMap  map providing the SegmentNode that has been created from a node,
	 *                         will not be modified; != null
	 * @param waySegmentMap    map providing the Segments that have been created from a way,
	 *                         will not be modified; != null
	 * @return                 Restrictions created from barrier nodes; != null, but may be empty
	 */
	private Collection<Restriction> createRestrictionsFromBarrierNodes(
			Map<N, SegmentNodeImpl> nodeCreationMap,
			Map<W, List<Segment>> waySegmentMap) {

		assert nodeCreationMap != null;
		assert waySegmentMap != null;

		Collection<Restriction> results = new LinkedList<Restriction>();

		for (N node : nodeCreationMap.keySet()) {

			if (!accessEvaluator.nodeUsable(node, nodeCreationMap.get(node).getProperties())) {

				SegmentNode barrierNode = nodeCreationMap.get(node);

				for (Segment inboundSegment : barrierNode.getInboundSegments()) {
					for (Segment outboundSegment : barrierNode.getOutboundSegments()) {
						results.add(new RestrictionImpl(inboundSegment, EMPTY_SEGMENT_LIST, Arrays.asList(outboundSegment)));
					}
				}

			}

		}

		return results;
	}

	/**
	 * determines the values of all RoadPropertyTypes from {@link #properties} for a way and
	 * creates a map with the types that have non-null values as keys, property values as content
	 */
	private Map<RoadPropertyType<?>, Object> getWayPropertyMap(W way, boolean forward) {
		Map<RoadPropertyType<?>, Object> propertyValues;
		propertyValues = new HashMap<RoadPropertyType<?>, Object>();
		for (RoadPropertyType<?> property : properties) {
			Object value = property.evaluateW(way, forward, accessParameters, dataSource);
			if (value != null) {
				propertyValues.put(property, value);
			}
		}
		return propertyValues;
	}

	/**
	 * determines the values of all RoadPropertyTypes from {@link #properties} for a node and
	 * creates a map with the types that have non-null values as keys, property values as content
	 */
	private Map<RoadPropertyType<?>, Object> getNodePropertyMap(N node) {
		Map<RoadPropertyType<?>, Object> propertyValues;
		propertyValues = new HashMap<RoadPropertyType<?>, Object>();
		for (RoadPropertyType<?> property : properties) {
			Object value = property.evaluateN(node, accessParameters, dataSource);
			if (value != null) {
				propertyValues.put(property, value);
			}
		}
		return propertyValues;
	}

	public void update(DataSource<?, ?, ?> dataSource) {
		assert this.dataSource == dataSource;
		updateData();
	}

	public void addObserver(TransitionStructureObserver observer) {
		observers.add(observer);
	}

	public void deleteObserver(TransitionStructureObserver observer) {
		observers.remove(observer);
	}

	protected void notifyObservers() {
		for (TransitionStructureObserver observer : observers) {
			observer.update(this);
		}
	}

}