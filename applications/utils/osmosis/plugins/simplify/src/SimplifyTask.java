import java.util.List;

import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.filter.common.IdTracker;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerFactory;
import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.SimpleObjectStore;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * SimplifyTask defines what happens as we encounter nodes and ways, and how we
 * filter then before sending them to the sink
 * It filters them (dropping a load of elements) similar to AreaFilterTask
 */
public class SimplifyTask implements SinkSource, EntityProcessor {
	
	private Sink sink;
	
	private IdTracker requiredNodes; //Nodes we want to feed through
	
	private SimpleObjectStore<NodeContainer> allNodes;
	
	
	private int count=0; //just for debug stats
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param idTrackerType
	 *            Defines the id tracker implementation to use.
	 */
	public SimplifyTask(IdTrackerType idTrackerType) {
		
		requiredNodes = IdTrackerFactory.createInstance(idTrackerType);
		
		allNodes = new SimpleObjectStore<NodeContainer>(
					new SingleClassObjectSerializationFactory(NodeContainer.class), "afnd", true);
		
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
		//pass on the bounds information unchanged
		sink.process(boundContainer);
	}

	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer container) {
		
		//stuff all nodes into a file
		allNodes.add(container);
		
		//debug
		count++;
		if (count % 50000 == 0) System.out.println(count + " nodes processed so far");
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer container) {
		Way way = container.getEntity();

		WayNode startNode, endNode;

		Way filteredWay = way.getWriteableInstance();

		List<WayNode> wayNodes = filteredWay.getWayNodes();
				
		if (wayNodes.size() <2 ) {
			//why does this sometimes happen?
		} else {
			// Remove node references for nodes that are unavailable.
			while (wayNodes.size()>2) {
				wayNodes.remove(1);
			}
			
			startNode = wayNodes.get(0);
			requiredNodes.set(startNode.getNodeId());
	
			endNode = wayNodes.get(1);
			requiredNodes.set(endNode.getNodeId());
	
			sink.process(new WayContainer(filteredWay));
		}
	}
	
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		//Do nothing (Drop all relations)
	}


	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		ReleasableIterator<NodeContainer> nodeIterator;
		NodeContainer nodeContainer;
		long nodeId;

		// Send on only the required nodes
		nodeIterator = allNodes.iterate();
		while (nodeIterator.hasNext()) {
			nodeContainer = nodeIterator.next();
			nodeId = nodeContainer.getEntity().getId();

			if (requiredNodes.get(nodeId)) {
				sink.process(nodeContainer);
			}
		}

		nodeIterator.release();
		nodeIterator = null;

		sink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (allNodes != null) {
			allNodes.release();
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
