package org.openstreetmap.josm.plugins.piclayer.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class PictureTransform {

	private AffineTransform cachedTransform;
	private boolean modified = false;

	private List<Point2D> originPoints;

	public PictureTransform() {
		cachedTransform = new AffineTransform();
		originPoints = new ArrayList<Point2D>(3);
	}

	public AffineTransform getTransform() {
		return cachedTransform;
	}

	private AffineTransform solveEquation(List<Point2D> desiredPoints) throws NoSolutionException {
		Matrix3D X = new Matrix3D(originPoints);
		Matrix3D Y = new Matrix3D(desiredPoints);
		Matrix3D result = Y.multiply(X.inverse());

		return result.toAffineTransform();
	}

	public void addOriginPoint(Point2D originPoint) {
		if (originPoints.size() < 3)
			originPoints.add(originPoint);
	}

	public void resetCalibration() {
		originPoints.clear();
		modified = false;
		cachedTransform = new AffineTransform();
	}

	/**
	 * updates pair of points (suppose that other pairs are (origin=>origin) points are the same),
	 * solves equation,
	 * applies transform matrix to the existing cachedTransform
	 *
	 * @param originPoint - should be one of origin points, otherwise - no transform applied
	 * @param desiredPoint - new place for the point
	 */
	public void updatePair(Point2D originPoint, Point2D desiredPoint) {
		if (originPoints.size() < 3) // not enough information for creating transform - 3 points needed
			return;

		if (originPoint == null)
			return;

		List<Point2D> desiredPoints = new ArrayList<Point2D>(3);

		for (Point2D origin : originPoints) {
			if (origin.equals(originPoint))
				desiredPoints.add(desiredPoint);
			else
				desiredPoints.add(origin);
		}
		trySolve(desiredPoints);
	}

    private void trySolve(List<Point2D> desiredPoints) {
        if (desiredPoints.size() == 3 && originPoints.size() == 3) {
            try {
                cachedTransform.concatenate(solveEquation(desiredPoints));
                modified = true;
                desiredPoints.clear();
            } catch (NoSolutionException e) {
                System.err.println(e.getMessage());
            }
        }
    }

	public void replaceOriginPoint(Point2D originPoint, Point2D newOriginPoint) {
		if (originPoint == null || newOriginPoint == null)
			return;

		int index = originPoints.indexOf(originPoint);
		if (index < 0)
			return;

		originPoints.set(index, newOriginPoint);
	}

	public void concatenateTransformPoint(AffineTransform transform, Point2D trans) {

        AffineTransform centered = AffineTransform.getTranslateInstance(trans.getX(), trans.getY());
        centered.concatenate(transform);
        centered.translate(-trans.getX(), -trans.getY());
        cachedTransform.concatenate(centered);


		for (int i = 0; i < originPoints.size(); i++) {
			Point2D point = originPoints.get(i);
			transform.transform(point, point);
		}
		modified = true;
	}

	public boolean isModified() {
	    return modified;
	}

    public void setModified() {
        modified = true;

    }

    public void resetModified() {
        modified = false;
    }

    public void setTransform(AffineTransform newTransform) {
        cachedTransform = new AffineTransform(newTransform);
    }

    public List<Point2D> getOriginPoints() {
        return originPoints;
    }

    public void setOriginPoints(List<Point2D> list) {
        this.originPoints = new ArrayList<Point2D>(list);
    }
}
