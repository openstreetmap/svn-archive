package org.openstreetmap.osmosis.plugins.areapoints;

import java.util.Date;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.IndexedObjectStore;
import org.openstreetmap.osmosis.core.store.IndexedObjectStoreReader;
import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

// This software is released into the Public Domain.


/**
 * A simple class to create Points out of closed ways
 * 
 * @author Christoph Wagner
 */
public class AreaPointsTask implements SinkSource, EntityProcessor {

	private Sink sink;

	private final IndexedObjectStore<Node> allNodes;
	private final SimpleObjectStore<Way> closedWays;


	public AreaPointsTask() {
		allNodes = new IndexedObjectStore<Node>(
				new SingleClassObjectSerializationFactory(Node.class), "nodes");
		closedWays = new SimpleObjectStore<Way>(
				new SingleClassObjectSerializationFactory(Way.class), "ways", true);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// Ask the entity container to invoke the appropriate processing method
		// for the entity type.
		entityContainer.process(this);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(BoundContainer boundContainer) {
		// By default, pass it on unchanged
		sink.process(boundContainer);
	}


	/**
	 * collect all nodes
	 */
	public void process(NodeContainer container) {
		//stuff all nodes into a file
		Node nd = container.getEntity();
		allNodes.add(nd.getId(),nd);

		sink.process(container);
	}


	/**
	 * collect all closed ways
	 */
	public void process(WayContainer container) {
		Way way = container.getEntity();
		if (way.isClosed()) {
			closedWays.add(way);
		}
		sink.process(container);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		sink.process(container);
	}


	/**
	 * complete the task
	 */
	public void complete() {
		// all nodes and ways are collected now
		allNodes.complete();
		closedWays.complete();

		IndexedObjectStoreReader<Node> iosr = allNodes.createReader();
		ReleasableIterator<Way> iter = closedWays.iterate();
		// just compute boundingbox
		double minLat,minLon,maxLat,maxLon,lat,lon;
		Way way;
		Node n;

		// iterate through all collected ways
		while (iter.hasNext()) {

			way = iter.next();

			// just compute boundingbox
			minLat = 90;
			minLon = 180;
			maxLat = -90;
			maxLon = -180;
			lat = 0;
			lon = 0;

			for (WayNode wn : way.getWayNodes()) {
				n = iosr.get(wn.getNodeId());
				lat = n.getLatitude();
				lon = n.getLongitude();
				if (maxLat < lat) maxLat = lat;
				else if (minLat > lat) minLat = lat;

				if (maxLon < lon) maxLon = lon;
				else if (minLon > lon) minLon = lon;
			}

			// compute centerpoint of bounding box
			lat = (maxLat+minLat)/2;
			lon = (maxLon+minLon)/2;

			// meta info for the new node
			CommonEntityData ced = new CommonEntityData(
					-way.getId(), // use the negative way-id for the new point
					0, //version
					new Date(),
					OsmUser.NONE,
					0, // changeset id
					way.getTags() // copy all tags from the way to the node
			);

			// insert node
			sink.process(new NodeContainer(new Node(ced,lat,lon)));
		}

		sink.complete();
	}


	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (allNodes != null) {
			allNodes.release();
		}
		if (closedWays != null) {
			closedWays.release();
		}
		sink.release();
	}


	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
