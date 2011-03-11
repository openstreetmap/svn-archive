package org.openstreetmap.osmosis.history.v0_6;

import org.openstreetmap.osmosis.history.store.HistoryNodeStore;
import org.openstreetmap.osmosis.history.store.ExampleHistoryNodeStore;
import org.openstreetmap.osmosis.history.v0_6.impl.HistoryNodeStoreTest;

/**
 * A test validating an Instance of InMemoryHistoryNodeStore
 * 
 * @author Peter Koerner
 */
public class ExampleHistoryNodeStoreTest extends HistoryNodeStoreTest {
	/**
	 * override getStore method to create an InMemoryHistoryNodeStore to test.
	 * @return 
	 */
	@Override
	public HistoryNodeStore getStore() {
		// create a class instance to test
		return new ExampleHistoryNodeStore();
	}
}
