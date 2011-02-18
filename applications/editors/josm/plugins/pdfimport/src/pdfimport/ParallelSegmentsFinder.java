package pdfimport;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParallelSegmentsFinder {
	public double angle;
	public double angleSum;
	public int refCount;
	public List<PdfPath> paths = new ArrayList<PdfPath>();

	public void addPath(PdfPath path, double angle2) {
		angleSum += angle2;
		paths.add(path);
		angle = angleSum /paths.size();
	}

	public List<ParallelSegmentsFinder> splitByDistance(double maxDistance)
	{
		//sort perpendicular to angle
		AffineTransform tr = new AffineTransform();
		tr.rotate(-angle);

		final Map<PdfPath, Point2D> positions = new HashMap<PdfPath, Point2D>();
		Point2D src = new Point2D.Double();

		for(PdfPath path: paths)
		{
			src.setLocation((path.firstPoint().getX() + path.lastPoint().getX()) / 2, (path.firstPoint().getY() + path.lastPoint().getY()) / 2);
			Point2D dest = new Point2D.Double();
			Point2D destA = new Point2D.Double();
			Point2D destB = new Point2D.Double();
			tr.transform(src, dest);
			tr.transform(path.firstPoint(), destA);
			tr.transform(path.lastPoint(), destB);
			positions.put(path, dest);
		}
		//point.y = Perpendicular lines, point.x = parallel lines

		Collections.sort(paths, new Comparator<PdfPath>() {
			public int compare(PdfPath o1, PdfPath o2) {
				double y1 = positions.get(o1).getY();
				double y2 = positions.get(o2).getY();
				return y1 > y2 ? 1: y1 < y2 ? -1 : 0;
			}
		});

		//process sweep
		List<ParallelSegmentsFinder> result = new ArrayList<ParallelSegmentsFinder>();

		Map<PdfPath, ParallelSegmentsFinder> sweepLine = new HashMap<PdfPath, ParallelSegmentsFinder>();

		Set<ParallelSegmentsFinder> adjacentClustersSet = new HashSet<ParallelSegmentsFinder>();
		List<ParallelSegmentsFinder> adjacentClusters = new ArrayList<ParallelSegmentsFinder>();
		List<PdfPath> pathsToRemove = new ArrayList<PdfPath>();

		for (PdfPath path: paths){
			adjacentClusters.clear();
			adjacentClustersSet.clear();
			pathsToRemove.clear();

			for (PdfPath p: sweepLine.keySet()) {
				Point2D pathPos = positions.get(path);
				Point2D pPos = positions.get(p);

				if (pathPos.getY() - pPos.getY() > maxDistance) {
					//path too far from sweep line
					pathsToRemove.add(p);
				} else {

					double distance = distanceLineLine(path, p);

					if (distance <= maxDistance) {
						if (adjacentClustersSet.add(sweepLine.get(p)))
						{
							adjacentClusters.add(sweepLine.get(p));
						}
					}
				}
			}

			//remove segments too far apart
			for(PdfPath p: pathsToRemove){
				ParallelSegmentsFinder finder = sweepLine.remove(p);
				finder.refCount --;
				if (finder.refCount == 0){
					result.add(finder);
				}
			}

			//join together joinable parts
			if (adjacentClusters.size() > 0){
				ParallelSegmentsFinder finder = adjacentClusters.remove(0);
				finder.paths.add(path);
				sweepLine.put(path, finder);
				finder.refCount ++;

				for(ParallelSegmentsFinder finder1: adjacentClusters){
					for(PdfPath path1: finder1.paths){
						finder.paths.add(path1);
						sweepLine.put(path1, finder);
						finder.refCount ++;
					}
				}
			}
			else
			{
				ParallelSegmentsFinder finder = new ParallelSegmentsFinder();
				finder.addPath(path, angle);
				sweepLine.put(path, finder);
				finder.refCount = 1;
			}
		}

		//process remaining paths in sweep line
		for (PdfPath p: sweepLine.keySet()) {
			ParallelSegmentsFinder finder = sweepLine.get(p);
			finder.refCount --;
			if (finder.refCount == 0){
				result.add(finder);
			}
		}

		return result;
	}

	private double distanceLineLine(PdfPath p1, PdfPath p2) {
		return distanceLineLine(p1.firstPoint(), p1.lastPoint(), p2.firstPoint(), p2.lastPoint());
	}

	private double distanceLineLine(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
		double dist1 = closestPointToSegment(p1, p2, p3).distance(p3);
		double dist2 = closestPointToSegment(p1, p2, p4).distance(p4);
		double dist3 = closestPointToSegment(p3, p4, p1).distance(p1);
		double dist4 = closestPointToSegment(p3, p4, p2).distance(p2);
		return Math.min(Math.min(dist1, dist2),Math.min(dist3, dist4));
	}

	/**
	 * Calculates closest point to a line segment.
	 * @param segmentP1
	 * @param segmentP2
	 * @param point
	 * @return segmentP1 if it is the closest point, segmentP2 if it is the closest point,
	 * a new point if closest point is between segmentP1 and segmentP2.
	 */
	public static Point2D closestPointToSegment(Point2D segmentP1, Point2D segmentP2, Point2D point) {

		double ldx = segmentP2.getX() - segmentP1.getX();
		double ldy = segmentP2.getY() - segmentP1.getY();

		if (ldx == 0 && ldy == 0) //segment zero length
			return segmentP1;

		double pdx = point.getX() - segmentP1.getX();
		double pdy = point.getY() - segmentP1.getY();

		double offset = (pdx * ldx + pdy * ldy) / (ldx * ldx + ldy * ldy);

		if (offset <= 0)
			return segmentP1;
		else if (offset >= 1)
			return segmentP2;
		else
			return new Point2D.Double(segmentP1.getX() + ldx * offset, segmentP1.getY() + ldy * offset);

	}
}
