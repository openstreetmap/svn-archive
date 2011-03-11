package org.openstreetmap.osmosis.history.v0_6;

import static org.easymock.EasyMock.*;

import org.junit.Test;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.history.v0_6.impl.CopyFilesetBuilderTest;
import org.openstreetmap.osmosis.pgsnapshot.common.CopyFileWriter;

public class WayNodeVersionBuilderTest extends CopyFilesetBuilderTest {
	@Override
	protected boolean isWayNodeVersionBuilderEnabled() {
		return true;
	}
	
	@Test
	public void testNormalOperationContinues() {
		// create a mockup object for the way-node writer
		CopyFileWriter wayNodeWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way-node writer with the mockup object
		writerSet.setWayNodeWriter(wayNodeWriterMock);
		
		// create the test nodes
		Node n1v1 = new Node(1, 1, entityDate, entityCreator, 1, emptyTags, 10, 10);
		Node n2v1 = new Node(2, 1, entityDate, entityCreator, 1, emptyTags, 15, 10);
		
		long[] nodeIds = {n1v1.getId(), n2v1.getId()};
		Way w1v1 = new Way(100, 1, entityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		Way w1v2 = new Way(100, 2, laterEntityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write way w1v1
		expectWayNode(wayNodeWriterMock, n1v1, w1v1);
		expectWayNode(wayNodeWriterMock, n2v1, w1v1);
		
		// express expected behavior - write way w1v2
		expectWayNode(wayNodeWriterMock, n1v1, w1v2);
		expectWayNode(wayNodeWriterMock, n2v1, w1v2);
		
		// express expected behavior - finish write operation
		wayNodeWriterMock.complete();
		
		// start replay operation
		replay(wayNodeWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1v1));
		builder.process(new NodeContainer(n2v1));
		builder.process(new WayContainer(w1v1));
		builder.process(new WayContainer(w1v2));
		builder.complete();
		
		// verify correct replay
		verify(wayNodeWriterMock);
		reset(wayNodeWriterMock);
	}
	
	@Test
	public void testWayNodeChoosesCorrectNodeVersion() {
		// create a mockup object for the way-node writer
		CopyFileWriter wayNodeWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way-node writer with the mockup object
		writerSet.setWayNodeWriter(wayNodeWriterMock);
		
		// create the test nodes
		Node n1v1 = new Node(1, 1, entityDate, entityCreator, 1, emptyTags, 10, 10);
		Node n1v2 = new Node(1, 2, laterEntityDate, entityCreator, 1, emptyTags, 15, 10);
		Node n2v1 = new Node(2, 1, entityDate, entityCreator, 1, emptyTags, 15, 10);
		
		long[] nodeIds = {n1v1.getId(), n2v1.getId()};
		Way w1v1 = new Way(100, 1, entityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		Way w1v2 = new Way(100, 2, laterEntityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write way w1v1
		expectWayNode(wayNodeWriterMock, n1v1, w1v1);
		expectWayNode(wayNodeWriterMock, n2v1, w1v1);
		
		// express expected behavior - write way w1v2, this time with n1v2
		expectWayNode(wayNodeWriterMock, n1v2, w1v2);
		expectWayNode(wayNodeWriterMock, n2v1, w1v2);
		
		// express expected behavior - finish write operation
		wayNodeWriterMock.complete();
		
		// start replay operation
		replay(wayNodeWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1v1));
		builder.process(new NodeContainer(n1v2));
		builder.process(new NodeContainer(n2v1));
		builder.process(new WayContainer(w1v1));
		builder.process(new WayContainer(w1v2));
		builder.complete();
		
		// verify correct replay
		verify(wayNodeWriterMock);
		reset(wayNodeWriterMock);
	}
}
