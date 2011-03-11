package org.openstreetmap.osmosis.history.v0_6.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.history.store.HistoryNodeStore;


/**
 * A test validating an Instance of HistoryNodeStore
 * 
 * @author Peter Koerner
 */
public abstract class HistoryNodeStoreTest {
	/**
	 * the store to test.
	 */
	protected HistoryNodeStore store;
	
	/**
	 * the nodes' default id
	 */
	private final long nodeId = 624657635;
	
	/**
	 * the nodes' default version
	 */
	private final int nodeVersion = 12;
	
	/**
	 * the nodes' default creator.
	 */
	private static OsmUser nodeCreator = null;
	
	/**
	 * the nodes' default create date
	 */
	private static Date nodeDate = null;
	
	/**
	 * the nodes' default longitude
	 */
	private final int nodeLon = 37;
	
	/**
	 * the nodes' default latitude
	 */
	private final int nodeLat = 58;
	
	/**
	 * the nodes' default changeset
	 */
	private final int nodeChangeset = 5502564;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		// create a date
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		cal.set(2010, 8, 22, 22, 30, 0);
		nodeDate = cal.getTime();
		
		// create a user
		nodeCreator = new OsmUser(111159, "MaZderMind");
	}
	
	public abstract HistoryNodeStore getStore();
	
	@Before
	public void setUp() throws Exception
	{
		// create the store
		store = getStore();
	}

	@After
	public void tearDown() throws Exception {
		// release the store
		store.release();
		
		// destroy the store
		store = null;
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		// destroy the date
		nodeDate = null;
		
		// destroy the user
		nodeCreator = null;
	}
	
	/**
	 * Add a Node to the store and retrieve it back.
	 */
	@Test
	public void testSingleIdSingleVersion() {
		// create a node
		Node writeNode = new Node(nodeId, nodeVersion, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		
		// add the node to the store
		store.addNode(writeNode);
		
		// complete the write operation
		store.complete();
		
		// read the node from the store
		Node readNode = store.getNode(nodeId, nodeVersion);
		
		// assert that the two nodes are equal
		Assert.assertEquals(writeNode, readNode);
	}
	
	/**
	 * Add two versions of a Node to the store and retrieve them back.
	 */
	@Test
	public void testSingleIdMultipleVersions() {
		// supplementary data
		final int nodeVersionA = 12;
		final int nodeVersionB = 25;
		
		// create a node
		Node writeNodeA = new Node(nodeId, nodeVersionA, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		Node writeNodeB = new Node(nodeId, nodeVersionB, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		
		// add the nodes to the store
		store.addNode(writeNodeA);
		store.addNode(writeNodeB);
		
		// complete the write operation
		store.complete();
		
		// read the nodes from the store
		Node readNodeA = store.getNode(nodeId, nodeVersionA);
		Node readNodeB = store.getNode(nodeId, nodeVersionB);
		
		// assert that the two nodes are equal
		Assert.assertEquals(writeNodeA, readNodeA);
		Assert.assertEquals(writeNodeB, readNodeB);
	}
	
	/**
	 * Add two versions of a Node to the store and retrieve a list of the versions back.
	 */
	@Test
	public void testSingleIdVersionsList() {
		// supplementary data
		final int nodeVersionA = 12;
		final int nodeVersionB = 25;
		
		// create a node
		Node writeNodeA = new Node(nodeId, nodeVersionA, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		Node writeNodeB = new Node(nodeId, nodeVersionB, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		
		// add the nodes to the store
		store.addNode(writeNodeA);
		store.addNode(writeNodeB);
		
		// complete the write operation
		store.complete();
		
		// get the Collection of nodes 
		Collection<Node> nodes = store.getNodeVersions(nodeId);
		
		Assert.assertTrue("collection is missing version A", nodes.contains(writeNodeA));
		Assert.assertTrue("collection is missing version B", nodes.contains(writeNodeB));
	}
	
	/**
	 * Add two Nodes to the store and retrieve them back.
	 */
	@Test
	public void testMultipleIdsSingleVersion() {
		// supplementary data
		final int nodeIdA = 624657635;
		final int nodeIdB = 798690549;
		
		// create two nodes
		Node writeNodeA = new Node(nodeIdA, nodeVersion, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		Node writeNodeB = new Node(nodeIdB, nodeVersion, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		
		// add the nodes to the store
		store.addNode(writeNodeA);
		store.addNode(writeNodeB);
		
		// complete the write operation
		store.complete();
		
		// read the nodes from the store
		Node readNodeA = store.getNode(nodeIdA, nodeVersion);
		Node readNodeB = store.getNode(nodeIdB, nodeVersion);
		
		// assert that the two nodes are equal
		Assert.assertEquals(writeNodeA, readNodeA);
		Assert.assertEquals(writeNodeB, readNodeB);
	}
	
	/**
	 * Add two Versions of two Nodes to the store and retrieve them back.
	 */
	@Test
	public void testMultipleIdsMultipleVersions() {
		// supplementary data
		final int nodeIdA = 624657635;
		final int nodeVersionA = 12;
		final int nodeIdB = 798690549;
		final int nodeVersionB = 25;
		
		// create four nodes
		Node writeNodeAA = new Node(nodeIdA, nodeVersionA, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		Node writeNodeAB = new Node(nodeIdA, nodeVersionB, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		Node writeNodeBA = new Node(nodeIdB, nodeVersionA, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		Node writeNodeBB = new Node(nodeIdB, nodeVersionB, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		
		// add the nodes to the store
		store.addNode(writeNodeAA);
		store.addNode(writeNodeAB);
		store.addNode(writeNodeBA);
		store.addNode(writeNodeBB);
		
		// complete the write operation
		store.complete();
		
		// read the nodes from the store
		Node readNodeAA = store.getNode(nodeIdA, nodeVersionA);
		Node readNodeAB = store.getNode(nodeIdA, nodeVersionB);
		Node readNodeBA = store.getNode(nodeIdB, nodeVersionA);
		Node readNodeBB = store.getNode(nodeIdB, nodeVersionB);
		
		// assert that the two nodes are equal
		Assert.assertEquals(writeNodeAA, readNodeAA);
		Assert.assertEquals(writeNodeAB, readNodeAB);
		Assert.assertEquals(writeNodeBA, readNodeBA);
		Assert.assertEquals(writeNodeBB, readNodeBB);
	}
	
	/**
	 * Add two versions of two Nodes to the store and retrieve a list of the versions back.
	 */
	@Test
	public void testMultipleIdsVersionsList() {
		// supplementary data
		final int nodeIdA = 624657635;
		final int nodeVersionA = 12;
		final int nodeIdB = 798690549;
		final int nodeVersionB = 25;
		
		// create four nodes
		Node writeNodeAA = new Node(nodeIdA, nodeVersionA, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		Node writeNodeAB = new Node(nodeIdA, nodeVersionB, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		Node writeNodeBA = new Node(nodeIdB, nodeVersionA, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		Node writeNodeBB = new Node(nodeIdB, nodeVersionB, nodeDate, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		
		// add the nodes to the store
		store.addNode(writeNodeAA);
		store.addNode(writeNodeAB);
		store.addNode(writeNodeBA);
		store.addNode(writeNodeBB);
		
		// complete the write operation
		store.complete();
		
		// get the Collections of nodes 
		Collection<Node> nodesA = store.getNodeVersions(nodeIdA);
		Collection<Node> nodesB = store.getNodeVersions(nodeIdB);
		
		Assert.assertTrue("collection A is missing version A", nodesA.contains(writeNodeAA));
		Assert.assertTrue("collection A is missing version B", nodesA.contains(writeNodeAB));
		
		Assert.assertTrue("collection B is missing version A", nodesB.contains(writeNodeBA));
		Assert.assertTrue("collection B is missing version B", nodesB.contains(writeNodeBB));
	}
	
	/**
	 * Add two versions of a Node to the store and retrieve them back by date
	 */
	@Test
	public void testFindByTimestamp() {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		
		// supplementary data
		final int nodeVersionA = 12;
		final int nodeVersionB = 25;

		cal.set(2010, 8, 20, 0, 0, 0);
		final Date nodeDateA = cal.getTime();
		
		cal.set(2010, 8, 23, 0, 0, 0);
		final Date lookupDateA = cal.getTime();
		
		cal.set(2010, 8, 27, 0, 0, 0);
		final Date nodeDateB = cal.getTime();

		cal.set(2010, 8, 28, 0, 0, 0);
		final Date lookupDateB = cal.getTime();
		
		
		// create a node
		Node writeNodeA = new Node(nodeId, nodeVersionA, nodeDateA, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		Node writeNodeB = new Node(nodeId, nodeVersionB, nodeDateB, nodeCreator, nodeChangeset, nodeLon, nodeLat);
		
		// add the nodes to the store
		store.addNode(writeNodeA);
		store.addNode(writeNodeB);
		
		// complete the write operation
		store.complete();
		
		// read the nodes from the store
		Node lookupNodeA = store.findNode(nodeId, lookupDateA);
		Node readNodeA = store.findNode(nodeId, nodeDateA);
		Node lookupNodeB = store.findNode(nodeId, lookupDateB);
		Node readNodeB = store.findNode(nodeId, nodeDateB);
		
		// assert that the nodes are equal
		Assert.assertEquals(writeNodeA, lookupNodeA);
		Assert.assertEquals(writeNodeA, readNodeA);
		Assert.assertEquals(writeNodeB, lookupNodeB);
		Assert.assertEquals(writeNodeB, readNodeB);
	}
}
