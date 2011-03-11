package org.openstreetmap.osmosis.history.v0_6.impl;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.history.domain.MinorWay;
import org.openstreetmap.osmosis.history.store.HistoryNodeStoreType;
import org.openstreetmap.osmosis.history.v0_6.impl.CopyFileWriterSet;
import org.openstreetmap.osmosis.history.v0_6.impl.HistoryCopyFilesetBuilder;
import org.openstreetmap.osmosis.hstore.PGHStore;
import org.openstreetmap.osmosis.pgsnapshot.common.CopyFileWriter;
import org.postgis.Point;

public abstract class CopyFilesetBuilderTest {
	/**
	 * the builder to test
	 */
	protected HistoryCopyFilesetBuilder builder;
		
	/**
	 * the entities' default creator.
	 */
	protected OsmUser entityCreator;

	/**
	 * the entities' default create date
	 */
	protected Date entityDate;
	
	/**
	 * the entities' default create date
	 */
	protected Date laterEntityDate;
	
	/**
	 * the entities' default tags
	 */
	protected Collection<Tag> emptyTags;
	
	/**
	 * the entities' default tags
	 */
	protected Collection<Tag> entityTags;

	/**
	 * the default writer set
	 */
	protected CopyFileWriterSet writerSet;

	private Calendar cal;
	
	@After
	public void tearDown() throws Exception {
		if(builder != null)
			builder.release();
	}
	
	@Before
	public void setUp() throws Exception {
		// create calendar
		cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		
		// create a date
		entityDate = buildDate(2010, 8, 21);
		
		// create a date that comes after the entityDate
		laterEntityDate = buildDate(2010, 8, 22);
		
		// create a user
		entityCreator = new OsmUser(111159, "MaZderMind");
		
		// create an empty tag-set
		emptyTags = new ArrayList<Tag>();
		
		// create a filled demo tag-set
		entityTags = new ArrayList<Tag>();
		entityTags.add(new Tag("foo", "bar"));
		entityTags.add(new Tag("moo", "mar"));
		
		// create an empty writer-set
		writerSet = CopyFileWriterSet.createEmpty();
		
		// create the file-set builder to test
		builder = new HistoryCopyFilesetBuilder(
				writerSet, 
				isBboxBuilderEnabled(), 
				isLinestringBuilderEnabled(),
				isWayNodeVersionBuilderEnabled(), 
				isMinorVersionBuilderEnabled(), 
				getNodeStoreType()
		);
	}
	
	protected List<Long> buildWayIdsList(long[] nodeIds) {
		List<Long> nodeIdsList = new ArrayList<Long>();
		
		for (long nodeId : nodeIds) {
			nodeIdsList.add(nodeId);
		}
		
		return nodeIdsList;
	}
	
	private List<Long>  buildWayIdsList(List<WayNode> wayNodes) {
		List<Long> nodeIdsList = new ArrayList<Long>();
		
		for (WayNode wayNode : wayNodes) {
			nodeIdsList.add(wayNode.getNodeId());
		}
		
		return nodeIdsList;
	}

	protected List<WayNode> buildWayNodesList(long[] nodeIds) {
		List<WayNode> nodeIdsList = new ArrayList<WayNode>();
		for (long nodeId : nodeIds) {
			nodeIdsList.add(new WayNode(nodeId));
		}
		return nodeIdsList;
	}
	
	protected PGHStore buildPGHstore(Collection<Tag> tags) {
		PGHStore hstore = new PGHStore();
		for (Tag tag : tags) {
			hstore.put(tag.getKey(), tag.getValue());
		}
		
		return hstore;
	}

	protected Point buildPoint(double longitude, double latitude) {
		Point result = new Point(longitude, latitude);
		result.srid = 4326;
		
		return result;
	}

	protected Date buildDate(int year, int month, int day) {
		cal.set(year, month, day, 10, 0, 0);
		return cal.getTime();
	}
	
	protected boolean isBboxBuilderEnabled()
	{
		return false;
	}
	
	protected boolean isLinestringBuilderEnabled()
	{
		return false;
	}
	
	protected boolean isWayNodeVersionBuilderEnabled()
	{
		return false;
	}
	
	protected boolean isMinorVersionBuilderEnabled()
	{
		return false;
	}
	
	protected HistoryNodeStoreType getNodeStoreType()
	{
		return HistoryNodeStoreType.Example;
	}
	
	protected void expectNode(CopyFileWriter nodeWriterMock, Node node) {
		nodeWriterMock.writeField(node.getId());
		nodeWriterMock.writeField(node.getVersion());
		nodeWriterMock.writeField(node.getUser().getId());
		nodeWriterMock.writeField(node.getTimestamp());
		nodeWriterMock.writeField(node.getChangesetId());
		nodeWriterMock.writeField(buildPGHstore(node.getTags()));
		nodeWriterMock.writeField(buildPoint(node.getLongitude(), node.getLatitude()));
		nodeWriterMock.endRecord();
	}
	
	protected void expectWay(CopyFileWriter wayWriterMock, Way way) {
		wayWriterMock.writeField(way.getId());
		wayWriterMock.writeField(way.getVersion());
		wayWriterMock.writeField(way.getUser().getId());
		wayWriterMock.writeField(way.getTimestamp());
		wayWriterMock.writeField(way.getChangesetId());
		wayWriterMock.writeField(buildPGHstore(way.getTags()));
		wayWriterMock.writeField(buildWayIdsList(way.getWayNodes()));
		if(isMinorVersionBuilderEnabled())
			wayWriterMock.writeField(0);
		
		wayWriterMock.endRecord();
	}
	
	protected void expectWay(CopyFileWriter wayWriterMock, MinorWay way) {
		if(!isMinorVersionBuilderEnabled())
			return;
		
		wayWriterMock.writeField(way.getId());
		wayWriterMock.writeField(way.getVersion());
		wayWriterMock.writeField(way.getUser().getId());
		wayWriterMock.writeField(way.getTimestamp());
		wayWriterMock.writeField(way.getChangesetId());
		wayWriterMock.writeField(buildPGHstore(way.getTags()));
		wayWriterMock.writeField(buildWayIdsList(way.getWayNodes()));
		wayWriterMock.writeField(way.getMinorVersion());
		
		wayWriterMock.endRecord();
	}
	
	protected void expectWayNode(CopyFileWriter wayNodeWriterMock, Node node, Way way) {
		wayNodeWriterMock.writeField(way.getId());
		wayNodeWriterMock.writeField(node.getId());
		wayNodeWriterMock.writeField(anyInt());
		wayNodeWriterMock.writeField(way.getVersion());
		if(isWayNodeVersionBuilderEnabled())
			wayNodeWriterMock.writeField(node.getVersion());
		
		wayNodeWriterMock.endRecord();
	}
}
