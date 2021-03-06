// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.extract.apidb.v0_6;

import java.io.File;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.osmosis.extract.apidb.common.Configuration;

import data.util.DataFileUtilities;

/**
 * Tests the operation of the database system time loader.
 * 
 * @author Brett Henderson
 */
public class DatabaseTimeLoaderTest {
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	
	/**
	 * Tests getting the current time from the database.
	 */
	@Test
	public void testGetTime() {
		File authFile;
		Configuration config;
		DatabaseTimeLoader timeLoader;
		Date systemTime;
		Date databaseTime;
		long difference;
		
		authFile = fileUtils.getDataFile("v0_6/apidb-authfile.txt");
		config = new Configuration(authFile);
		timeLoader = new DatabaseTimeLoader(config.getDatabaseLoginCredentials());
		
		databaseTime = timeLoader.getDatabaseTime();
		systemTime = new Date();
		difference = databaseTime.getTime() - systemTime.getTime();
		
		Assert.assertTrue("Database time is different to system time, databaseTime=" + databaseTime + ", systemTime="
				+ systemTime + ".",
				difference > -1000 && difference < 1000);
	}
}
