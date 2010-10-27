package pdfimport.pdfbox;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pdfimport.LayerInfo;
import pdfimport.PathOptimizer;
import pdfimport.PdfPath;

public class GraphicsProcessor extends Graphics2D {

	public PathOptimizer target;
	private Shape clipShape;
	private List<PdfPath> clipPath;
	private final LayerInfo info = new LayerInfo();
	int pathNo = 0;
	private boolean complexClipShape;
	private boolean clipAreaDrawn;
	private final double height;

	private final AffineTransform transform;

	public GraphicsProcessor(PathOptimizer target, int rotation, double height)
	{
		this.height = height;
		this.target = target;
		this.transform = new AffineTransform();
		this.transform.rotate(Math.toRadians(rotation));
	}

	private void addPath(Shape s, boolean fill) {
		List<PdfPath> paths = this.parsePath(s);
		if (fill) {
			this.info.fill = true;
			this.info.stroke = false;
		}
		else {
			this.info.fill = false;
			this.info.stroke = true;
		}

		for (PdfPath p: paths){
			p.nr = pathNo;
		}

		pathNo ++;

		if (paths.size() > 1) {
			this.target.addMultiPath(this.info, paths);
		}
		else if (paths.size() == 1) {
			this.target.addPath(this.info, paths.get(0));
		}
	}


	private List<PdfPath> parsePath(Shape s) {
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
		Point2D point = new Point2D.Double(buffer[offset], this.height - buffer[offset + 1]);
		Point2D dest = new Point2D.Double();
		this.transform.transform(point, dest);
		return this.target.getUniquePoint(dest);
	}


	@Override
	public void draw(Shape s) {

		if (complexClipShape)
		{
			if (!this.clipAreaDrawn)
			{
				this.addPath(this.clipShape, true);
				this.clipAreaDrawn = true;
			}
		}
		else
		{
			this.addPath(s, false);
		}
	}


	@Override
	public void fill(Shape s) {
		this.addPath(s, true);
	}

	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {

		if (!this.clipAreaDrawn)
		{
			this.addPath(this.clipShape, true);
			this.clipAreaDrawn = true;
		}
		return true;
	}


	@Override
	public void setClip(Shape clip) {
		if (this.clipShape == clip)
			return;

		this.clipPath = this.parsePath(clip);

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

	@Override
	public void setColor(Color c) {
		this.info.color = c;
		this.info.fillColor = c;
	}


	@Override
	public void setStroke(Stroke s) {
		BasicStroke stroke = (BasicStroke) s;
		this.info.width = stroke.getLineWidth();
		this.info.dash = 0;

		if (stroke.getDashArray() != null) {
			this.info.dash = stroke.getDashArray().hashCode();
		}
	}


	@Override
	public void clip(Shape s) {
		//TODO:
	}


	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		// TODO Auto-generated method stub
	}


	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawString(String str, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawString(String str, float x, float y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		// TODO Auto-generated method stub

	}


	@Override
	public Color getBackground() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Composite getComposite() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Paint getPaint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RenderingHints getRenderingHints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stroke getStroke() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AffineTransform getTransform() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void rotate(double theta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rotate(double theta, double x, double y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scale(double sx, double sy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBackground(Color color) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setComposite(Composite comp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPaint(Paint paint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTransform(AffineTransform Tx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void shear(double shx, double shy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transform(AffineTransform Tx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void translate(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void translate(double tx, double ty) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// TODO Auto-generated method stub

	}

	@Override
	public Graphics create() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, Color bgcolor,
			ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		// TODO Auto-generated method stub

	}

	@Override
	public Shape getClip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle getClipBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Font getFont() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FontMetrics getFontMetrics(Font f) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setClip(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}


	@Override
	public void setFont(Font font) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPaintMode() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setXORMode(Color c1) {
		// TODO Auto-generated method stub

	}

}
