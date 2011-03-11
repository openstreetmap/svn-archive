package org.openstreetmap.osmosis.history.v0_6;

import static org.easymock.EasyMock.*;

import org.junit.Test;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.history.domain.MinorWay;
import org.openstreetmap.osmosis.history.v0_6.impl.CopyFilesetBuilderTest;
import org.openstreetmap.osmosis.pgsnapshot.common.CopyFileWriter;

public class MinorVersionBuilderTest extends CopyFilesetBuilderTest {
	@Override
	protected boolean isMinorVersionBuilderEnabled() {
		return true;
	}
	
	@Test
	public void testMinorVersionBuilderDoesNotChangeNormalOperation() {
		// create a mockup object for the way-node writer
		CopyFileWriter wayWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way-node writer with the mockup object
		writerSet.setWayWriter(wayWriterMock);
		
		// create the test nodes
		Node n1v1 = new Node(1, 1, entityDate, entityCreator, 1, emptyTags, 10, 10);
		Node n2v1 = new Node(2, 1, entityDate, entityCreator, 1, emptyTags, 15, 10);
		
		long[] nodeIds = {n1v1.getId(), n2v1.getId()};
		Way w1 = new Way(100, 1, entityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write ways
		expectWay(wayWriterMock, w1);
		
		// express expected behavior - finish write operation
		wayWriterMock.complete();
		
		// start replay operation
		replay(wayWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1v1));
		builder.process(new NodeContainer(n2v1));
		builder.process(new WayContainer(w1));
		builder.complete();
		
		// verify correct replay
		verify(wayWriterMock);
		reset(wayWriterMock);
	}
	
	@Test
	public void testSingleMinorVersionAtEndOfLifetime() {
		// create a mockup object for the way-node writer
		CopyFileWriter wayWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way-node writer with the mockup object
		writerSet.setWayWriter(wayWriterMock);
		
		// create the test nodes
		Node n1v1 = new Node(1, 1, entityDate, entityCreator, 1, emptyTags, 10, 10);
		Node n1v2 = new Node(1, 2, laterEntityDate, entityCreator, 1, emptyTags, 10, 10);
		Node n2v1 = new Node(2, 1, entityDate, entityCreator, 1, emptyTags, 15, 10);
		
		long[] nodeIds = {n1v1.getId(), n2v1.getId()};
		Way w1 = new Way(100, 1, entityDate, entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write way
		expectWay(wayWriterMock, w1);
		
		// express expected behavior - write minor way
		expectWay(wayWriterMock, new MinorWay(w1.getId(), w1.getVersion(), 1, n1v2.getTimestamp(), n1v2.getUser(), 
				n1v2.getChangesetId(), w1.getTags(), buildWayNodesList(nodeIds)));
		
		// express expected behavior - finish write operation
		wayWriterMock.complete();
		
		// start replay operation
		replay(wayWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1v1));
		builder.process(new NodeContainer(n1v2));
		builder.process(new NodeContainer(n2v1));
		builder.process(new WayContainer(w1));
		builder.complete();
		
		// verify correct replay
		verify(wayWriterMock);
		reset(wayWriterMock);
	}
	
	@Test
	public void testMultipleMinorVersionsAtEndOfLifetime() {
		// create a mockup object for the way-node writer
		CopyFileWriter wayWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way-node writer with the mockup object
		writerSet.setWayWriter(wayWriterMock);
		
		// create the test nodes
		Node n1v1 = new Node(1, 1, buildDate(2010, 9, 1), entityCreator, 1, emptyTags, 10, 10);
		Node n2v1 = new Node(2, 1, buildDate(2010, 9, 1), entityCreator, 1, emptyTags, 15, 10);
		
		Node n1v2 = new Node(1, 2, buildDate(2010, 9, 2), entityCreator, 1, emptyTags, 10, 10);
		
		Node n1v3 = new Node(1, 3, buildDate(2010, 9, 3), entityCreator, 1, emptyTags, 10, 10);
		
		long[] nodeIds = {n1v1.getId(), n2v1.getId()};
		Way w1 = new Way(100, 1, buildDate(2010, 9, 1), entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write way
		expectWay(wayWriterMock, w1);

		// express expected behavior - write minor way
		expectWay(wayWriterMock, new MinorWay(w1.getId(), w1.getVersion(), 1, n1v2.getTimestamp(), n1v2.getUser(), 
				n1v2.getChangesetId(), w1.getTags(), buildWayNodesList(nodeIds)));

		// express expected behavior - write second minor way
		expectWay(wayWriterMock, new MinorWay(w1.getId(), w1.getVersion(), 2, n1v3.getTimestamp(), n1v3.getUser(), 
				n1v3.getChangesetId(), w1.getTags(), buildWayNodesList(nodeIds)));
		
		// express expected behavior - finish write operation
		wayWriterMock.complete();
		
		// start replay operation
		replay(wayWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1v1));
		builder.process(new NodeContainer(n1v2));
		builder.process(new NodeContainer(n1v3));
		builder.process(new NodeContainer(n2v1));
		builder.process(new WayContainer(w1));
		builder.complete();
		
		// verify correct replay
		verify(wayWriterMock);
		reset(wayWriterMock);
	}

	@Test
	public void testSingleMinorVersionBetweenNormalVersions() {
		// create a mockup object for the way-node writer
		CopyFileWriter wayWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way-node writer with the mockup object
		writerSet.setWayWriter(wayWriterMock);
		
		// create the test nodes
		Node n1v1 = new Node(1, 1, buildDate(2010, 9, 1), entityCreator, 1, emptyTags, 10, 10);
		Node n2v1 = new Node(2, 1, buildDate(2010, 9, 1), entityCreator, 1, emptyTags, 15, 10);
		
		Node n1v2 = new Node(1, 2, buildDate(2010, 9, 2), entityCreator, 1, emptyTags, 10, 10);
		
		long[] nodeIds = {n1v1.getId(), n2v1.getId()};
		Way w1v1 = new Way(100, 1, buildDate(2010, 9, 1), entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		Way w1v2 = new Way(100, 2, buildDate(2010, 9, 3), entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write way version 1
		expectWay(wayWriterMock, w1v1);
		
		// express expected behavior - write minor way
		expectWay(wayWriterMock, new MinorWay(w1v1.getId(), w1v1.getVersion(), 1, n1v2.getTimestamp(), n1v2.getUser(), 
				n1v2.getChangesetId(), w1v1.getTags(), buildWayNodesList(nodeIds)));

		// express expected behavior - write way version 2
		expectWay(wayWriterMock, w1v2);
		
		// express expected behavior - finish write operation
		wayWriterMock.complete();
		
		// start replay operation
		replay(wayWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1v1));
		builder.process(new NodeContainer(n1v2));
		builder.process(new NodeContainer(n2v1));
		builder.process(new WayContainer(w1v1));
		builder.process(new WayContainer(w1v2));
		builder.complete();
		
		// verify correct replay
		verify(wayWriterMock);
		reset(wayWriterMock);
	}
	

	@Test
	public void testMultipleMinorVersionsBetweenNormalVersions() {
		// create a mockup object for the way-node writer
		CopyFileWriter wayWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way-node writer with the mockup object
		writerSet.setWayWriter(wayWriterMock);
		
		// create the test nodes
		Node n1v1 = new Node(1, 1, buildDate(2010, 9, 1), entityCreator, 1, emptyTags, 10, 10);
		Node n2v1 = new Node(2, 1, buildDate(2010, 9, 1), entityCreator, 1, emptyTags, 15, 10);
		
		Node n1v2 = new Node(1, 2, buildDate(2010, 9, 2), entityCreator, 1, emptyTags, 10, 10);
		
		Node n1v3 = new Node(1, 3, buildDate(2010, 9, 3), entityCreator, 1, emptyTags, 10, 10);
		
		long[] nodeIds = {n1v1.getId(), n2v1.getId()};
		Way w1v1 = new Way(100, 1, buildDate(2010, 9, 1), entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		Way w1v2 = new Way(100, 2, buildDate(2010, 9, 4), entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write way version 1
		expectWay(wayWriterMock, w1v1);
		
		// express expected behavior - write minor way version 1/1
		expectWay(wayWriterMock, new MinorWay(w1v1.getId(), w1v1.getVersion(), 1, n1v2.getTimestamp(), n1v2.getUser(), 
				n1v2.getChangesetId(), w1v1.getTags(), buildWayNodesList(nodeIds)));

		// express expected behavior - write minor way version 1/2
		expectWay(wayWriterMock, new MinorWay(w1v1.getId(), w1v1.getVersion(), 2, n1v3.getTimestamp(), n1v3.getUser(), 
				n1v3.getChangesetId(), w1v1.getTags(), buildWayNodesList(nodeIds)));

		// express expected behavior - write way version 2
		expectWay(wayWriterMock, w1v2);
		
		// express expected behavior - finish write operation
		wayWriterMock.complete();
		
		// start replay operation
		replay(wayWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1v1));
		builder.process(new NodeContainer(n1v2));
		builder.process(new NodeContainer(n1v3));
		builder.process(new NodeContainer(n2v1));
		builder.process(new WayContainer(w1v1));
		builder.process(new WayContainer(w1v2));
		builder.complete();
		
		// verify correct replay
		verify(wayWriterMock);
		reset(wayWriterMock);
	}
	
	
	@Test
	public void testMultipleMinorVersionsBetweenNormalVersionsAndAtEndOfLifetime() {
		// create a mockup object for the way-node writer
		CopyFileWriter wayWriterMock = createStrictMock(CopyFileWriter.class);

		// replace the empty way-node writer with the mockup object
		writerSet.setWayWriter(wayWriterMock);
		
		// create the test nodes
		Node n1v1 = new Node(1, 1, buildDate(2010, 9, 1), entityCreator, 1, emptyTags, 10, 10);
		Node n2v1 = new Node(2, 1, buildDate(2010, 9, 1), entityCreator, 1, emptyTags, 15, 10);
		
		Node n1v2 = new Node(1, 2, buildDate(2010, 9, 2), entityCreator, 1, emptyTags, 10, 10);
		
		Node n1v3 = new Node(1, 3, buildDate(2010, 9, 3), entityCreator, 1, emptyTags, 10, 10);
		
		Node n1v4 = new Node(1, 4, buildDate(2010, 9, 6), entityCreator, 1, emptyTags, 10, 10);
		
		Node n1v5 = new Node(1, 5, buildDate(2010, 9, 6), entityCreator, 1, emptyTags, 10, 10);
		
		long[] nodeIds = {n1v1.getId(), n2v1.getId()};
		Way w1v1 = new Way(100, 1, buildDate(2010, 9, 1), entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		Way w1v2 = new Way(100, 2, buildDate(2010, 9, 4), entityCreator, 1, entityTags, buildWayNodesList(nodeIds));
		
		// express expected behavior - write way version 1
		expectWay(wayWriterMock, w1v1);
		
		// express expected behavior - write minor way version 1/1
		expectWay(wayWriterMock, new MinorWay(w1v1.getId(), w1v1.getVersion(), 1, n1v2.getTimestamp(), n1v2.getUser(), 
				n1v2.getChangesetId(), w1v1.getTags(), buildWayNodesList(nodeIds)));

		// express expected behavior - write minor way version 1/2
		expectWay(wayWriterMock, new MinorWay(w1v1.getId(), w1v1.getVersion(), 2, n1v3.getTimestamp(), n1v3.getUser(), 
				n1v3.getChangesetId(), w1v1.getTags(), buildWayNodesList(nodeIds)));

		// express expected behavior - write way version 2
		expectWay(wayWriterMock, w1v2);

		// express expected behavior - write minor way version 2/1
		expectWay(wayWriterMock, new MinorWay(w1v2.getId(), w1v2.getVersion(), 1, n1v4.getTimestamp(), n1v4.getUser(), 
				n1v4.getChangesetId(), w1v2.getTags(), buildWayNodesList(nodeIds)));

		// express expected behavior - write minor way version 2/1
		expectWay(wayWriterMock, new MinorWay(w1v2.getId(), w1v2.getVersion(), 2, n1v5.getTimestamp(), n1v5.getUser(), 
				n1v5.getChangesetId(), w1v2.getTags(), buildWayNodesList(nodeIds)));

		// express expected behavior - finish write operation
		wayWriterMock.complete();
		
		// start replay operation
		replay(wayWriterMock);
		
		// process the nodes and the way
		builder.process(new NodeContainer(n1v1));
		builder.process(new NodeContainer(n1v2));
		builder.process(new NodeContainer(n1v3));
		builder.process(new NodeContainer(n1v4));
		builder.process(new NodeContainer(n1v5));
		builder.process(new NodeContainer(n2v1));
		builder.process(new WayContainer(w1v1));
		builder.process(new WayContainer(w1v2));
		builder.complete();
		
		// verify correct replay
		verify(wayWriterMock);
		reset(wayWriterMock);
	}
	
}
