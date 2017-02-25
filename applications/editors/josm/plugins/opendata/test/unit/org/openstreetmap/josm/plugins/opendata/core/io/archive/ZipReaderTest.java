// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map.Entry;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.core.io.NonRegFunctionalTests;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests of {@link ZipReader} class.
 */
public class ZipReaderTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences().projection().noTimeout();

    /**
     * Test for various zip files reading
     * @throws Exception if an error occurs during reading
     */
    @Test
    public void testReadZipFiles() throws Exception {
        for (Path p : NonRegFunctionalTests.listDataFiles("zip")) {
            File zipfile = p.toFile();
            Main.info("Testing reading file "+zipfile.getPath());
            try (InputStream is = new FileInputStream(zipfile)) {
                for (Entry<File, DataSet> entry : ZipReader.parseDataSets(is, null, null, false).entrySet()) {
                    String name = entry.getKey().getName();
                    Main.info("Checking dataset for entry "+name);
                    NonRegFunctionalTests.testGeneric(zipfile.getName()+"/"+name, entry.getValue());
                }
            }
        }
    }
}
