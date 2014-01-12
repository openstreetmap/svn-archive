package pdfimport.pdfbox;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.gui.progress.ProgressMonitor;

import pdfimport.LayerInfo;
import pdfimport.PathOptimizer;
import pdfimport.PdfPath;

public class GraphicsProcessor{

	public PathOptimizer target;
	private Shape  clipShape;
	private List<PdfPath> clipPath;
	private final LayerInfo info = new LayerInfo();
	int pathNo = 0;
	private boolean complexClipShape;
	private boolean clipAreaDrawn;

	private final AffineTransform transform;
	private final ProgressMonitor monitor;
	private final int maxPaths;


	public GraphicsProcessor(PathOptimizer target, int rotation, int maxPaths, ProgressMonitor monitor)
	{
		this.maxPaths = maxPaths;
		this.target = target;
		this.transform = new AffineTransform();
		this.transform.rotate(-Math.toRadians(rotation));
		this.info.stroke = Color.BLACK;
		this.info.fill = Color.BLACK;
		this.monitor = monitor;
	}


	private void addPath(Shape s, boolean closed) {
		pathNo ++;

		if (pathNo % 100 == 0) {
			this.monitor.setCustomText(tr(" {0} objects so far", pathNo));
		}

		if (pathNo >= maxPaths) {
			return;
		}

		List<PdfPath> paths = this.parsePath(s, closed);

		for (PdfPath p: paths){
			p.nr = pathNo;
		}


		if (paths.size() > 1) {
			this.target.addMultiPath(this.info, paths);
			this.parsePath(s, closed);
		}
		else if (paths.size() == 1) {
			this.target.addPath(this.info, paths.get(0));
		}

	}


	private List<PdfPath> parsePath(Shape s, boolean closed) {
		List<PdfPath> result = new ArrayList<PdfPath>(2);
		List<Point2D> points = new ArrayList<Point2D>(2);

		PathIterator iter = s.getPathIterator(null);
		double[] coords = new double[6];

		while (!iter.isDone()) {
			int type = iter.currentSegment(coords);

			if (type == PathIterator.SEG_CLOSE) {
				//close polygon
				this.addPoint(points, points.get(0));
				if (points.size() > 1) {
					result.add(new PdfPath(points));
				}
				points = new ArrayList<Point2D>(2);
			} else if (type == PathIterator.SEG_CUBICTO) {
				//cubic curve
				this.addPoint(points, this.parsePoint(coords, 4));
			}
			else if (type == PathIterator.SEG_LINETO) {
				this.addPoint(points, this.parsePoint(coords, 0));
			}
			else if (type == PathIterator.SEG_MOVETO) {
				//new path
				if (points.size() > 1){
					result.add(new PdfPath(points));
				}
				points = new ArrayList<Point2D>(2);
				this.addPoint(points, this.parsePoint(coords, 0));
			}
			else if (type == PathIterator.SEG_QUADTO) {
				//quadratic curve
				this.addPoint(points, this.parsePoint(coords, 2));
			}
			else if (type == PathIterator.WIND_EVEN_ODD) {
				//fill even odd
			}
			else if (type == PathIterator.WIND_NON_ZERO) {
				//fill all
			}
			else
			{
				//Unexpected operation
				int a = 10;
				a++;
			}

			iter.next();
		}

		if (points.size() > 1 )
		{
			if (closed) {
				this.addPoint(points, points.get(0));
			}

			result.add(new PdfPath(points));
		}

		return result;
	}

	private void addPoint(List<Point2D> points, Point2D point) {
		if (points.size() > 0) {
			Point2D prevPoint = points.get(points.size() - 1);

			if (prevPoint.getX() == point.getX() && prevPoint.getY() == point.getY()) {
				return;
			}
		}

		points.add(point);
	}

	private Point2D parsePoint(double[] buffer, int offset) {
		//invert Y.
		Point2D point = new Point2D.Double(buffer[offset], buffer[offset + 1]);
		Point2D dest = new Point2D.Double();
		this.transform.transform(point, dest);
		return this.target.getUniquePoint(dest);
	}

	public void drawPath(Shape path, Color stroke, Color fill,
			int windingRule) {

		if (complexClipShape) {
			if (!this.clipAreaDrawn) {
				this.info.stroke = null;
				this.info.fill = Color.CYAN;
				this.addPath(this.clipShape, true);
				this.clipAreaDrawn = true;
			}
		}

		if (!complexClipShape || fill != null) {
			this.info.stroke = stroke;
			this.info.fill = fill;
			this.addPath(path, fill != null);
		}
	}


	public void drawImage() {

		if (!this.clipAreaDrawn) {
			this.info.stroke = null;
			this.info.fill = Color.CYAN;
			this.addPath(this.clipShape, true);
			this.clipAreaDrawn = true;
		}
	}


	public void setClip(Shape  clip) {
		if (this.shapesEqual(this.clipShape,clip))
			return;

		this.clipPath = this.parsePath(clip, true);

		boolean complexClipPath = false;

		if (clipPath.size() > 1)
		{
			complexClipPath = true;
		}
		else if (clipPath.size() == 1 && clipPath.get(0).points.size() > 5)
		{
			//more than 4 points.
			complexClipPath = true;
		}

		this.complexClipShape = complexClipPath;
		this.clipAreaDrawn = false;
		this.clipShape = clip;
	}


	private boolean shapesEqual(Shape shape1, Shape shape2) {

		if (shape1== null || shape2 == null){
			return false;
		}

		return shape1.getBounds2D().equals(shape2.getBounds2D());
	}


	public void setStroke(Stroke s) {
		BasicStroke stroke = (BasicStroke) s;
		this.info.width = stroke.getLineWidth();
		this.info.dash = 0;

		if (stroke.getDashArray() != null) {
			this.info.dash = stroke.getDashArray().hashCode();
		}
	}

	public void drawString(float x, float y, String character, Color color) {
		// TODO Auto-generated method stub
	}
}
