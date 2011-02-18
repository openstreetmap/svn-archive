package pdfimport;

import java.awt.geom.Point2D;

public class OrthogonalShapesFilter {
	private double tolerance;

	public OrthogonalShapesFilter(double toleranceDegrees) {
		tolerance = Math.toRadians(toleranceDegrees);
	}

	public boolean isOrthogonal(PdfPath path) {

		if (path.points.size() < 3)
			return false;

		int targetPos = path.isClosed() ? path.points.size(): path.points.size() - 1;

		for(int pos = 1; pos < targetPos; pos++) {
			Point2D p1 = path.points.get(pos -1);
			Point2D p2 = path.points.get(pos);
			Point2D p3 = pos+1 == path.points.size() ? path.points.get(1) : path.points.get(pos+1);

			double angle1 = Math.atan2(p2.getY() - p1.getY(),p2.getX() - p1.getX());
			double angle2 = Math.atan2(p3.getY() - p2.getY(),p3.getX() - p2.getX());

			double angleDifference = angle1 - angle2;
			while (angleDifference < 0) angleDifference += Math.PI;

			//test straight angles
			boolean hasGoodVariant = false;

			for (int quadrant = 0; quadrant <= 4; quadrant ++) {
				double difference = angleDifference - Math.PI / 2 * quadrant;
				if (Math.abs(difference) <= tolerance)
					hasGoodVariant = true;
			}

			if (!hasGoodVariant)
				return false;
		}

		return true;
	}
}
