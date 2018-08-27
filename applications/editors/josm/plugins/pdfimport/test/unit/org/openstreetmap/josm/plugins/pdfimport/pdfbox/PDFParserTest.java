// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pdfimport.pdfbox;

import static org.junit.Assert.assertEquals;

import java.awt.Rectangle;
import java.io.File;

import org.junit.Test;
import org.openstreetmap.josm.TestUtils;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.plugins.pdfimport.PathOptimizer;

public class PDFParserTest {

    private PathOptimizer parse(String fileName) throws Exception {
        PathOptimizer data = new PathOptimizer(0.0, null, false);
        PdfBoxParser parser = new PdfBoxParser(data);
        parser.parse(new File(fileName), Integer.MAX_VALUE, NullProgressMonitor.INSTANCE);
        return data;
    }

    @Test
    public void testParse9053() throws Exception {
        PathOptimizer data = parse(TestUtils.getRegressionDataFile(9053, "testpdf.pdf"));
        assertEquals(0, data.bounds.getMinX(), 0);
        assertEquals(0, data.bounds.getMinY(), 0);
        assertEquals(595.27557, data.bounds.getMaxX(), 0.00001);
        assertEquals(841.88977, data.bounds.getMaxY(), 0.00001);
        assertEquals(4, data.uniquePoints.size());
        assertEquals(1, data.getLayers().size());
    }

    @Test
    public void testParse12176() throws Exception {
        PathOptimizer data = parse(TestUtils.getRegressionDataFile(12176, "LYD_Etage_0.pdf"));
        assertEquals(new Rectangle(595, 842), data.bounds);
        assertEquals(127300, data.uniquePoints.size());
        assertEquals(34, data.getLayers().size());
    }
}
