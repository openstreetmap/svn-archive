// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.tests;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Rule;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.elevation.HgtReader;
import org.openstreetmap.josm.testutils.JOSMTestRules;

import junit.framework.TestCase;

public class HgtReaderTest extends TestCase {

    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

    /**
     * Setup test.
     * @throws IOException if SRTM files cannot be installed
     */
    @Override
    public void setUp() throws IOException {
        // Install SRTM files to plugin directory
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(TestUtils.getTestDataRoot()), "*.hgt")) {
            Path dir = Paths.get(System.getProperty("josm.home")).resolve("elevation");
            if (!Files.exists(dir)) {
                Files.createDirectory(dir);
            }
            for (Path src: stream) {
                Path dst = dir.resolve(src.getFileName());
                if (!Files.exists(dst)) {
                    Files.copy(src, dst);
                }
            }
        } catch (DirectoryIteratorException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw ex.getCause();
        }
    }

    public void testGetElevationFromHgt() {
        // Staufenberg, Hessen
        testHgtData(50.6607106, 8.7337029, "N50E008.hgt", 199);
        // Ulrichstein, Hessen
        testHgtData(50.5767627, 9.1938483, "N50E009.hgt", 560);
        // Fujijama
        //testHgtData(35.360555, 138.727777, "N35E138.hgt", 3741);
    }

    private void testHgtData(final double lat, final double lon,
            final String expTag, final int expHeight) {
        LatLon l = new LatLon(lat, lon);
        HgtReader hr = new HgtReader();
        String text = hr.getHgtFileName(l);

        assertEquals(expTag, text);

        double d = hr.getElevationFromHgt(l);
        System.out.println(d);
        assertFalse("Data missing or void for coor " + l, Double.isNaN(d));

        assertEquals((int)d, expHeight);
    }
}
