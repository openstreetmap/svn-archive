// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.v0_6;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openstreetmap.osmosis.core.Osmosis;

import data.util.DataFileUtilities;


/**
 * Tests the change appender task.
 * 
 * @author Brett Henderson
 */
public class ChangeAppenderTest {
	private DataFileUtilities fileUtils = new DataFileUtilities();
	
	
	/**
	 * Tests appending two change files into a single file.
	 * 
	 * @throws IOException
	 *             if any file operations fail.
	 */
	@Test
	public void testAppend() throws IOException {
		File sourceFile1;
		File sourceFile2;
		File expectedOutputFile;
		File actualOutputFile;
		
		// Generate files.
		sourceFile1 = fileUtils.getDataFile("v0_6/append-change-in1.osc");
		sourceFile2 = fileUtils.getDataFile("v0_6/append-change-in2.osc");
		expectedOutputFile = fileUtils.getDataFile("v0_6/append-change-out.osc");
		actualOutputFile = File.createTempFile("test", ".osm");
		
		// Append the two source files into the destination file.
		Osmosis.run(
			new String [] {
				"-q",
				"--read-xml-change-0.6",
				sourceFile2.getPath(),
				"--read-xml-change-0.6",
				sourceFile1.getPath(),
				"--append-change-0.6",
				"--write-xml-change-0.6",
				actualOutputFile.getPath()
			}
		);
		
		// Validate that the output file matches the expected result.
		fileUtils.compareFiles(expectedOutputFile, actualOutputFile);
		
		// Success so delete the output file.
		actualOutputFile.delete();
	}
}
