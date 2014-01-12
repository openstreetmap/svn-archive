package pdfimport;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DuplicateNodesFinder {

	/***
	 * This method finds very close nodes and constructs a mapping from node to suggested representative node.
	 * Works by performing a sweep and noting down similar nodes.
	 * @param nodes the nodes to process
	 * @return map from nodes that need replacement to a representative node.
	 */
	public static Map<Point2D, Point2D> findDuplicateNodes(Collection<Point2D> nodes, final double tolerance){
		List<Point2D> points = new ArrayList<Point2D>(nodes);
		Collections.sort(points, new Comparator<Point2D>(){
			public int compare(Point2D o1, Point2D o2) {
				double diff = o1.getY() - o2.getY();
				return diff > 0 ? 1 : (diff < 0 ? -1 : 0);
			}
		});

		Map<Point2D, Point2D> result = new HashMap<Point2D, Point2D>();
		TreeMap<Point2D, Point2D> sweepLine = new TreeMap<Point2D, Point2D>(new Comparator<Point2D>(){
			public int compare(Point2D o1, Point2D o2) {
				double diff = o1.getX() - o2.getX();

				if (Math.abs(diff) <= tolerance){
					return 0;
				}

				return diff > 0 ? 1 : (diff < 0 ? -1 : 0);
			}
		});

		//sweep from top to bottom.
		double prevY = Double.NEGATIVE_INFINITY;

		for(Point2D point: points) {
			boolean mappedToOtherPoint = false;

			if (point.getY() - prevY > tolerance){
				sweepLine.clear();
				//big offset, clear old points
			} else {
				//small offset, test against existing points (there may be more than one)

				while (!mappedToOtherPoint && sweepLine.containsKey(point)) {
					//a close point found
					Point2D closePoint = sweepLine.get(point);
					double dy = point.getY() - closePoint.getY();
					if (dy <= tolerance) {
						//mark them as close
						result.put(point, closePoint);
						mappedToOtherPoint = true;
					}
					else
					{
						sweepLine.remove(point);

					}
				}
			}

			if (!mappedToOtherPoint) {
				sweepLine.put(point, point);
			}

			prevY = point.getY();
		}

		return result;
	}
}
