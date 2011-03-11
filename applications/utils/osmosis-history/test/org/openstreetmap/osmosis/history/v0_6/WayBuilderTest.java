package org.openstreetmap.osmosis.history.v0_6;

import static org.easymock.EasyMock.*;

import org.junit.Test;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.history.v0_6.impl.CopyFilesetBuilderTest;
import org.openstreetmap.osmosis.pgsnapshot.common.CopyFileWriter;

public class WayBuilderTest extends CopyFilesetBuilderTest {
	@Test
	public void testWriteWays() {
		// create a mockup object for the way writer
		CopyFileWriter wayWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way writer with the mockup object
		writerSet.setWayWriter(wayWriterMock);
		
		// create the test nodes
		Node n1 = new Node(1, 1, entityDate, entityCreator, 1, emptyTags, 10, 10);
		Node n2 = new Node(2, 1, entityDate, entityCreator, 1, emptyTags, 15, 10);
		
		long[] nodeIds = {n1.getId(), n2.getId()};
		Way w1 = new Way(100, 1, entityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write way
		expectWay(wayWriterMock, w1);
		
		// express expected behavior - finish write operation
		wayWriterMock.complete();
		
		// start replay operation
		replay(wayWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1));
		builder.process(new NodeContainer(n2));
		builder.process(new WayContainer(w1));
		builder.complete();
		
		// verify correct replay
		verify(wayWriterMock);
		reset(wayWriterMock);
	}
	
	@Test
	public void testWriteWayHistory() {
		// create a mockup object for the way writer
		CopyFileWriter wayWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way writer with the mockup object
		writerSet.setWayWriter(wayWriterMock);
		
		// create the test nodes
		Node n1 = new Node(1, 1, entityDate, entityCreator, 1, emptyTags, 10, 10);
		Node n2 = new Node(2, 1, entityDate, entityCreator, 1, emptyTags, 15, 10);
		
		long[] nodeIds = {n1.getId(), n2.getId()};
		Way w1v1 = new Way(100, 1, entityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		Way w1v2 = new Way(100, 2, entityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write way
		expectWay(wayWriterMock, w1v1);
		expectWay(wayWriterMock, w1v2);
		
		// express expected behavior - finish write operation
		wayWriterMock.complete();
		
		// start replay operation
		replay(wayWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1));
		builder.process(new NodeContainer(n2));
		builder.process(new WayContainer(w1v1));
		builder.process(new WayContainer(w1v2));
		builder.complete();
		
		// verify correct replay
		verify(wayWriterMock);
		reset(wayWriterMock);
	}

	@Test
	public void testWriteWayNodes() {
		// create a mockup object for the way-node writer
		CopyFileWriter wayNodeWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way-node writer with the mockup object
		writerSet.setWayNodeWriter(wayNodeWriterMock);
		
		// create the test nodes
		Node n1 = new Node(1, 1, entityDate, entityCreator, 1, emptyTags, 10, 10);
		Node n2 = new Node(2, 1, entityDate, entityCreator, 1, emptyTags, 15, 10);
		
		long[] nodeIds = {n1.getId(), n2.getId()};
		Way w1 = new Way(100, 1, entityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write ways
		expectWayNode(wayNodeWriterMock, n1, w1);
		expectWayNode(wayNodeWriterMock, n2, w1);
		
		// express expected behavior - finish write operation
		wayNodeWriterMock.complete();
		
		// start replay operation
		replay(wayNodeWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1));
		builder.process(new NodeContainer(n2));
		builder.process(new WayContainer(w1));
		builder.complete();
		
		// verify correct replay
		verify(wayNodeWriterMock);
		reset(wayNodeWriterMock);
	}
	
	@Test
	public void testWriteWayNodeHistory() {
		// create a mockup object for the way-node writer
		CopyFileWriter wayNodeWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way-node writer with the mockup object
		writerSet.setWayNodeWriter(wayNodeWriterMock);
		
		// create the test nodes
		Node n1 = new Node(1, 1, entityDate, entityCreator, 1, emptyTags, 10, 10);
		Node n2 = new Node(2, 1, entityDate, entityCreator, 1, emptyTags, 15, 10);
		
		long[] nodeIds = {n1.getId(), n2.getId()};
		Way w1v1 = new Way(100, 1, entityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		Way w1v2 = new Way(100, 2, entityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write ways
		expectWayNode(wayNodeWriterMock, n1, w1v1);
		expectWayNode(wayNodeWriterMock, n2, w1v1);
		expectWayNode(wayNodeWriterMock, n1, w1v2);
		expectWayNode(wayNodeWriterMock, n2, w1v2);
		
		// express expected behavior - finish write operation
		wayNodeWriterMock.complete();
		
		// start replay operation
		replay(wayNodeWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1));
		builder.process(new NodeContainer(n2));
		builder.process(new WayContainer(w1v1));
		builder.process(new WayContainer(w1v2));
		builder.complete();
		
		// verify correct replay
		verify(wayNodeWriterMock);
		reset(wayNodeWriterMock);
	}
}
