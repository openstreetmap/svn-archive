// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pdfimport.pdfbox;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.pdfimport.PathOptimizer;

public class PdfBoxParser extends PDFStreamEngine {
    private final PathOptimizer target;

    public PdfBoxParser(PathOptimizer target) {
        this.target = target;
    }

    public void parse(File file, int maxPaths, ProgressMonitor monitor) throws IOException {
        monitor.beginTask(tr("Parsing PDF", 1));

        try (PDDocument document = PDDocument.load(file)) {

            if (document.isEncrypted()) {
                throw new IllegalArgumentException(tr("Encrypted documents not supported."));
            }

            List<?> allPages = document.getDocumentCatalog().getAllPages();

            if (allPages.size() != 1) {
                throw new IllegalArgumentException(tr("The PDF file must have exactly one page."));
            }

            PDPage page = (PDPage) allPages.get(0);
            PDRectangle pageSize = page.findMediaBox();
            Integer rotationVal = page.getRotation();
            int rotation = 0;
            if (rotationVal != null) {
                rotation = rotationVal.intValue();
            }

            new PageDrawer().drawPage(new GraphicsProcessor(target, rotation, maxPaths, monitor), page);
            this.target.bounds = new Rectangle2D.Double(
                    pageSize.getLowerLeftX(),
                    pageSize.getLowerLeftY(),
                    pageSize.getWidth(),
                    pageSize.getHeight());
        }

        monitor.finishTask();
    }
}
