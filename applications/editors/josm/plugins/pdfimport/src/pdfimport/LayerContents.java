/**
 *
 */
package pdfimport;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class LayerContents{

	List<Point2D> points = new ArrayList<Point2D>();
	List<PdfPath> paths = new ArrayList<PdfPath>();
	List<PdfMultiPath> multiPaths = new ArrayList<PdfMultiPath>();
	LayerInfo info;
}
