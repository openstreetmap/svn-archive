// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.domain.v0_6;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.Test;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.store.DataInputStoreReader;
import com.bretth.osmosis.core.store.DataOutputStoreWriter;
import com.bretth.osmosis.core.store.StoreClassRegister;
import com.bretth.osmosis.core.store.StoreReader;
import com.bretth.osmosis.core.store.StoreWriter;


/**
 * Tests the OsmUser class.
 * 
 * @author Karl Newman
 * @author Brett Henderson
 */
public class OsmUserTest {
	
	
	/**
	 * Verify the details of the NONE user.
	 */
	@Test
	public final void testGetInstanceNoUser() {
		assertEquals("None user id is incorrect.", 0, OsmUser.NONE.getUserId());
		assertEquals("None user name is incorrect.", "", OsmUser.NONE.getUserName());
	}
	
	
	/**
	 * Ensure that the class doesn't allow a null user name.
	 */
	@Test(expected=NullPointerException.class)
	public final void testGetInstancePreventsNullUser() {
		new OsmUser(null, 1);
	}
	
	
	/**
	 * Ensure that the class doesn't allow the reserved "NONE" user id to be specified.
	 */
	@Test(expected=OsmosisRuntimeException.class)
	public final void testGetInstancePreventsNoneUser() {
		new OsmUser("MyNoneUser", 0);
	}
	
	
	/**
	 * Ensure the instance is correctly written to and read from the store.
	 */
	@Test
	public final void testGetInstanceFromStore() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StoreWriter sw = new DataOutputStoreWriter(new DataOutputStream(out));
		StoreClassRegister scr = new StoreClassRegister();
		OsmUser user1 = new OsmUser("aUser", 12);
		OsmUser user3 = new OsmUser("aUser2", 13);
		OsmUser user5 = new OsmUser("", 14);
		user1.store(sw, scr);
		user3.store(sw, scr);
		user5.store(sw, scr);
		StoreReader sr = new DataInputStoreReader(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
		OsmUser user2 = new OsmUser(sr, scr);
		OsmUser user4 = new OsmUser(sr, scr);
		OsmUser user6 = new OsmUser(sr, scr);
		assertEquals("Object not equal after retrieval from store", user1, user2);
		assertEquals("Object not equal after retrieval from store", user3, user4);
		assertEquals("Object not equal after retrieval from store", user5, user6);
	}
}
