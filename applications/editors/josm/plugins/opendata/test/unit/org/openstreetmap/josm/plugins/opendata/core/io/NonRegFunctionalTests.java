// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.CheckParameterUtil;

public abstract class NonRegFunctionalTests {
    
    /**
     * Non-regression generic test.
     */
    public static void testGeneric(DataSet ds) {
        CheckParameterUtil.ensureParameterNotNull(ds, "ds");
        Collection<OsmPrimitive> prims = ds.allPrimitives();
        assertFalse(prims.isEmpty());
    }
    
    /**
     * Non-regression test for ticket <a href="https://josm.openstreetmap.de/ticket/10214">#10214</a>
     */
    public static void testTicket10214(DataSet ds) {
        testGeneric(ds);
        boolean found = false;
        for (Node n : ds.getNodes()) {
            if (n.hasTag("id", "1")) {
                found = true;
                String expected = "à as italian località";
                String actual = n.get("some_text");
                assertEquals(expected, actual);
            }
        }
        assertTrue(found);
    }
    
    /**
     * Lists all datasets files matching given extension.
     * @param ext file extension to search for
     * @returns List of all datasets files matching given extension
     * @throws IOException in case of I/O error
     */
    public static Collection<Path> listDataFiles(String ext) throws IOException {
        Collection<Path> result = new ArrayList<>();
        addTree(Paths.get(TestUtils.getTestDataRoot()+"datasets"), result, ext.toLowerCase());
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
