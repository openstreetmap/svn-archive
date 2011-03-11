package org.openstreetmap.osmosis.history.v0_6;

import static org.easymock.EasyMock.*;

import org.junit.Test;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.history.v0_6.impl.CopyFilesetBuilderTest;
import org.openstreetmap.osmosis.pgsnapshot.common.CopyFileWriter;

public class NodeBuilderTest extends CopyFilesetBuilderTest {
	@Test
	public void testWriteNodes() {
		// create a mockup object for the node writer
		CopyFileWriter nodeWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty node writer with the mockup object
		writerSet.setNodeWriter(nodeWriterMock);
		
		// create the test nodes
		Node n1 = new Node(1, 1, entityDate, entityCreator, 1, emptyTags, 10, 10);
		Node n2 = new Node(2, 1, entityDate, entityCreator, 1, entityTags, 15, 10);
		
		// express expected behavior - write nodes
		expectNode(nodeWriterMock, n1);
		expectNode(nodeWriterMock, n2);
		
		// express expected behavior - finish write operation
		nodeWriterMock.complete();
		
		// start replay operation
		replay(nodeWriterMock);
		
		// process the nodes
		builder.process(new NodeContainer(n1));
		builder.process(new NodeContainer(n2));
		builder.complete();
		
		// verify correct replay
		verify(nodeWriterMock);
		reset(nodeWriterMock);
	}
	
	@Test
	public void testWriteNodeHistory() {
		// create a mockup object for the node writer
		CopyFileWriter nodeWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty node writer with the mockup object
		writerSet.setNodeWriter(nodeWriterMock);
		
		// create the test nodes
		Node n1v1 = new Node(1, 1, entityDate, entityCreator, 1, emptyTags, 10, 10);
		Node n1v2 = new Node(1, 2, entityDate, entityCreator, 1, emptyTags, 10, 10);
		
		// express expected behavior - write nodes
		expectNode(nodeWriterMock, n1v1);
		expectNode(nodeWriterMock, n1v2);
		
		// express expected behavior - finish write operation
		nodeWriterMock.complete();
		
		// start replay operation
		replay(nodeWriterMock);
		
		// process the nodes
		builder.process(new NodeContainer(n1v1));
		builder.process(new NodeContainer(n1v2));
		builder.complete();
		
		// verify correct replay
		verify(nodeWriterMock);
		reset(nodeWriterMock);
	}
}
