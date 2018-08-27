// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pdfimport;

import java.util.List;

public class PdfMultiPath {
    public List<PdfPath> paths;
    public LayerContents layer;

    public PdfMultiPath(List<PdfPath> paths2) {
        paths = paths2;
    }
}
