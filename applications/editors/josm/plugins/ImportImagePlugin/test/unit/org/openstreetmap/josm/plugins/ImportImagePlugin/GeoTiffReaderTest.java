package org.openstreetmap.josm.plugins.ImportImagePlugin;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.geotools.coverage.grid.GridCoverage2D;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Test that some geotiff sample files can be read.
 * Data downloaded from <a href="ftp://ftp.remotesensing.org/pub/geotiff/samples">remotesensing.org</a>.
 */
public class GeoTiffReaderTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

    @Test
    public void testReadGeoTiffFiles() throws IOException {
        for (Path p : listDataFiles("tif")) {
            File file = p.toFile();
            Main.info("Testing reading file "+file.getPath());
            GridCoverage2D grid = PluginOperations.createGridFromFile(file, null, false);
            assertNotNull(grid);
        }
    }
    
    /**
     * Lists all datasets files matching given extension.
     * @param ext file extension to search for
     * @returns List of all datasets files matching given extension
     * @throws IOException in case of I/O error
     */
    public static Collection<Path> listDataFiles(String ext) throws IOException {
        Collection<Path> result = new ArrayList<>();
        addTree(Paths.get(TestUtils.getTestDataRoot()+"geotiff"), result, ext.toLowerCase());
        return result;
    }
    
    static void addTree(Path directory, Collection<Path> all, String ext) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory)) {
            for (Path child : ds) {
                if (Files.isDirectory(child)) {
                    addTree(child, all, ext);
                } else if (child.toString().toLowerCase().endsWith(ext)) {
                    all.add(child);
                }
            }
        } catch (DirectoryIteratorException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw ex.getCause();
        }
    }
}
