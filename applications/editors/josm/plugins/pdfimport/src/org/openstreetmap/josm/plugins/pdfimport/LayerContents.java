// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pdfimport;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class LayerContents {

    List<Point2D> points = new ArrayList<>();
    List<PdfPath> paths = new ArrayList<>();
    List<PdfMultiPath> multiPaths = new ArrayList<>();
    LayerInfo info;
}
