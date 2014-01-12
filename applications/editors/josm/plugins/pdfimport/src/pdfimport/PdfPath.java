package pdfimport;

import java.awt.geom.Point2D;
import java.util.List;

public class PdfPath {
	public List<Point2D> points;
	public double length;

	LayerContents layer;
	public int nr;


	public PdfPath(List<Point2D> nodes) {
		points = nodes;
	}

	public boolean isClosed() {
		return points.size() > 1 && points.get(0) == points.get(points.size() - 1);
	}

	public Point2D firstPoint() {
		return points.get(0);
	}

	public Point2D lastPoint() {
		return points.get(points.size() - 1);
	}

	public void calculateLength() {
		double len = 0;

		for(int pos =1; pos < points.size(); pos ++) {
			len += points.get(pos).distance(points.get(pos -1));
		}

		this.length = len;
	}

	public Point2D getOtherEnd(Point2D endPoint) {
		if (this.firstPoint() == endPoint) {
			return this.lastPoint();
		}

		if (this.lastPoint() == endPoint) {
			return this.firstPoint();
		}

		throw new RuntimeException("Unexpected point");

	}
}
