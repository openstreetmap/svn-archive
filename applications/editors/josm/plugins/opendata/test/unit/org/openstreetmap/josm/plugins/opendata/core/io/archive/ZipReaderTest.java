// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.core.io.NonRegFunctionalTests;

/**
 * Unit tests of {@link ZipReader} class.
 */
public class ZipReaderTest {

    /**
     * Setup test.
     */
    @BeforeClass
    public static void setUp() {
        JOSMFixture.createUnitTestFixture().init();
    }

    /**
     * Test for various zip files reading
     * @throws IOException if an error occurs during reading
     */
    @Test
    public void testReadZipFiles() throws IOException, XMLStreamException, FactoryConfigurationError, JAXBException {
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
