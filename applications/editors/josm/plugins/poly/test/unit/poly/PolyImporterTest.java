// License: GPL. For details, see LICENSE file.
package poly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.testutils.JOSMTestRules;

/**
 * Unit tests for {@link PolyImporter}.
 * @author Gerd Petermann
 */
public class PolyImporterTest {

    /**
     * Setup test.
     */
    @Rule
    public JOSMTestRules rules = new JOSMTestRules().preferences();

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void testSimple() throws Exception {
        DataSet ds = new PolyImporter().parseDataSet(TestUtils.getTestDataRoot() + "/simple.poly");
        assertNotNull(ds);
        assertEquals(4, ds.getNodes().size());
        assertEquals(1, ds.getWays().size());
        assertEquals(0, ds.getRelations().size());
    }

    /**
     * File with human friendly coordinate format
     * @throws Exception if an error occurs
     */
    @Test
    public void testSimple2() throws Exception {
        DataSet ds = new PolyImporter().parseDataSet(TestUtils.getTestDataRoot() + "/splitter.poly");
        assertNotNull(ds);
        assertEquals(14, ds.getNodes().size());
        assertEquals(1, ds.getWays().size());
        assertEquals(0, ds.getRelations().size());
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void testHoles() throws Exception {
        DataSet ds = new PolyImporter().parseDataSet(TestUtils.getTestDataRoot() + "/holes.poly");
        assertNotNull(ds);
        assertEquals(76, ds.getNodes().size());
        assertEquals(4, ds.getWays().size());
        assertEquals(1, ds.getRelations().size());
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void testTwoOuter() throws Exception {
        DataSet ds = new PolyImporter().parseDataSet(TestUtils.getTestDataRoot() + "/australia_v.poly");
        assertNotNull(ds);
        assertEquals(18, ds.getNodes().size());
        assertEquals(2, ds.getWays().size());
        assertEquals(1, ds.getRelations().size());
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void testDoubleEnd() throws Exception {
        DataSet ds = new PolyImporter().parseDataSet(TestUtils.getTestDataRoot() + "/bremen-double-end.poly");
        assertNotNull(ds);
        assertEquals(337, ds.getNodes().size());
        assertEquals(2, ds.getWays().size());
        assertEquals(1, ds.getRelations().size());
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void testMultipleFile() throws Exception {
        DataSet ds = new PolyImporter().parseDataSet(TestUtils.getTestDataRoot() + "/multi-concat.poly");
        assertNotNull(ds);
        assertEquals(40, ds.getNodes().size());
        assertEquals(4, ds.getWays().size());
        assertEquals(1, ds.getRelations().size());
    }

    /**
     * Should throw an IllegalDataException
     * @throws Exception if an error occurs
     */
    @Test (expected = IllegalDataException.class)
    public void testNameMissing() throws Exception {
        DataSet ds = new PolyImporter().parseDataSet(TestUtils.getTestDataRoot() + "/name-missing.poly");
        assertNull(ds);
    }

}
