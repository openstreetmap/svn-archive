package pdfimport;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PathOptimizer {

	public List<Point2D> uniquePoints;
	public Map<Point2D, Point2D> uniquePointMap;
	private final Map<LayerInfo, LayerContents> layerMap;
	private List<LayerContents> layers;
	public Rectangle2D bounds;
	private final double pointsTolerance;
	private final Color color;
	boolean splitOnColorChange;

	LayerContents prevLayer = null;

	public PathOptimizer(double _pointsTolerance, Color _color, boolean _splitOnColorChange)
	{
		uniquePointMap = new HashMap<Point2D, Point2D>();
		uniquePoints = new ArrayList<Point2D>();
		layerMap = new HashMap<LayerInfo, LayerContents>();
		layers = new ArrayList<LayerContents>();
		pointsTolerance = _pointsTolerance;
		color = _color;
		splitOnColorChange = _splitOnColorChange;
	}

	public Point2D getUniquePoint(Point2D point) {

		if (uniquePointMap.containsKey(point)){
			return uniquePointMap.get(point);
		}
		else {
			uniquePointMap.put(point, point);
			uniquePoints.add(point);
			return point;
		}
	}

	public void addPath(LayerInfo info, PdfPath path)
	{
		if (!isColorOK(info)){
			return;
		}

		if (path.points.size() > 10){
			int a = 10;
			a++;
		}

		LayerContents layer = this.getLayer(info);
		layer.paths.add(path);
	}

	public void addMultiPath(LayerInfo info, List<PdfPath> paths) {

		if (!isColorOK(info)){
			return;
		}

		LayerContents layer = this.getLayer(info);

		//optimize the paths
		Set<Point2D> points = new HashSet<Point2D>();
		for(PdfPath path: paths) {
			points.addAll(path.points);
		}
		LayerContents multipathLayer = new LayerContents();
		multipathLayer.paths = paths;
		Map<Point2D, Point2D> pointMap = DuplicateNodesFinder.findDuplicateNodes(points, pointsTolerance);
		this.fixPoints(multipathLayer,pointMap);
		this.concatenatePaths(multipathLayer);

		paths = multipathLayer.paths;

		boolean goodMultiPath = true;
		for(PdfPath path: paths) {
			goodMultiPath &= path.isClosed();
		}

		if (goodMultiPath) {
			PdfMultiPath p = new PdfMultiPath(paths);
			layer.multiPaths.add(p);
		} else {
			layer.paths.addAll(paths);
		}
	}

	private boolean isColorOK(LayerInfo info) {

		if (color == null) {
			return true;
		}

		int rgb = color.getRGB() & 0xffffff;
		boolean good = false;

		if (info.fill != null && (info.fill.getRGB() & 0xffffff) == rgb) {
			good = true;
		}

		if (info.stroke != null && (info.stroke.getRGB() & 0xffffff) == rgb) {
			good = true;
		}

		return good;
	}


	public void removeParallelLines(double maxDistance){
		for(LayerContents layer: this.layers) {
			this.removeParallelLines(layer, maxDistance);
		}
	}

	public void mergeNodes() {
		Map<Point2D, Point2D> pointMap = DuplicateNodesFinder.findDuplicateNodes(uniquePoints, pointsTolerance);

		for(LayerContents layer: this.layers) {
			this.fixPoints(layer, pointMap);
		}
	}

	public void mergeSegments() {
		for(LayerContents layer: this.layers) {
			this.concatenatePaths(layer);
		}
	}


	public void removeSmallObjects(double tolerance) {
		for(LayerContents layer: this.layers) {
			this.removeSmallObjects(layer, tolerance, Double.POSITIVE_INFINITY);
		}
	}


	public void removeLargeObjects(double tolerance) {
		for(LayerContents layer: this.layers) {
			this.removeSmallObjects(layer, 0.0, tolerance);
		}
	}

	public void splitLayersBySimilarShapes(double tolerance) {
		List<LayerContents> newLayers = new ArrayList<LayerContents>();
		for(LayerContents l: this.layers) {
			List<LayerContents> splitResult = splitBySimilarGroups(l);

			for(LayerContents ll: splitResult) {
				newLayers.add(ll);
			}
		}
		this.layers = newLayers;
	}

	public void splitLayersByPathKind(boolean closed, boolean single, boolean orthogonal) {
		List<LayerContents> newLayers = new ArrayList<LayerContents>();
		for(LayerContents l: this.layers) {
			List<LayerContents> splitResult = splitBySegmentKind(l, closed, single, orthogonal);

			for(LayerContents ll: splitResult) {
				newLayers.add(ll);
			}
		}

		this.layers = newLayers;
	}


	public void finish() {
		int nr = 0;
		for(LayerContents layer: this.layers) {
			layer.info.nr = nr;
			nr++;
			finalizeLayer(layer);
		}
	}


	private LayerContents getLayer(LayerInfo info) {

		LayerContents layer = null;

		if (this.layerMap.containsKey(info))
		{
			layer = this.layerMap.get(info);

			if (layer != this.prevLayer && this.splitOnColorChange) {
				layer = null;
			}
		}

		if (layer == null)
		{
			layer = new LayerContents();
			layer.info = info.copy();
			layer.info.nr = this.layers.size();
			this.layerMap.put(layer.info, layer);
			this.layers.add(layer);
		}

		this.prevLayer = layer;
		return layer;
	}


	private void finalizeLayer(LayerContents layer){
		Set<Point2D> points = new HashSet<Point2D>();
		layer.points = new ArrayList<Point2D>();

		for(PdfPath pp: layer.paths){
			pp.layer = layer;

			for(Point2D point: pp.points){
				if (!points.contains(point)) {
					layer.points.add(point);
					points.add(point);
				}
			}
		}

		for (PdfMultiPath multipath: layer.multiPaths) {
			multipath.layer = layer;
			for(PdfPath pp: multipath.paths){
				pp.layer = layer;

				for(Point2D point: pp.points){
					if (!points.contains(point)) {
						layer.points.add(point);
						points.add(point);
					}
				}
			}
		}
	}

	private void fixPoints(LayerContents layer, Map<Point2D, Point2D> pointMap) {

		List<PdfPath> newPaths = new ArrayList<PdfPath>(layer.paths.size());

		for(PdfPath path: layer.paths) {
			List<Point2D> points = fixPoints(path.points, pointMap);
			path.points = points;
			if (points.size() > 2 || (!path.isClosed() && points.size() > 1)){

				newPaths.add(path);
			}
		}

		layer.paths = newPaths;

		for (PdfMultiPath mp: layer.multiPaths){
			for(PdfPath path: mp.paths) {
				path.points = fixPoints(path.points, pointMap);
			}
		}
	}


	private List<Point2D> fixPoints(List<Point2D> points, Map<Point2D, Point2D> pointMap) {

		List<Point2D> newPoints = new ArrayList<Point2D>(points.size());
		Point2D prevPoint = null;

		for(Point2D p: points){
			Point2D pp = p;

			if (pointMap.containsKey(p)){
				pp = pointMap.get(p);
			}

			if (prevPoint != pp){
				newPoints.add(pp);
			}

			prevPoint = pp;
		}

		return newPoints;
	}


	private void removeSmallObjects(LayerContents layer, double min, double max) {
		List<PdfPath> newPaths = new ArrayList<PdfPath>(layer.paths.size());

		for(PdfPath path: layer.paths) {
			double size = getShapeSize(path);
			boolean good = size >= min && size <= max;

			if (good) {
				newPaths.add(path);
			}
		}

		layer.paths = newPaths;

		List<PdfMultiPath> newMPaths = new ArrayList<PdfMultiPath>(layer.multiPaths.size());

		for (PdfMultiPath mp: layer.multiPaths){
			boolean good = true;
			for(PdfPath path: mp.paths) {
				double size = getShapeSize(path);
				good &= size >= min && size <= max;
			}

			if (good) {
				newMPaths.add(mp);
			}
		}

		layer.multiPaths = newMPaths;
	}


	private double getShapeSize(PdfPath path) {

		Rectangle2D bounds = new Rectangle2D.Double();
		bounds.setRect(path.points.get(0).getX(), path.points.get(0).getY(), 0,0);

		for(Point2D n: path.points) {
			bounds.add(n);
		}

		return Math.max(bounds.getWidth(), bounds.getHeight());
	}



	/***
	 * This method finds parralel lines with similar distance and removes them.
	 * @param layer
	 */
	private void removeParallelLines(LayerContents layer, double maxDistance) {
		double angleTolerance = 1.0 / 180.0 * Math.PI; // 1 degree
		int minSegments = 10;

		//filter paths by direction
		List<ParallelSegmentsFinder> angles = new ArrayList<ParallelSegmentsFinder>();

		for(PdfPath path: layer.paths) {
			if (path.points.size() != 2){
				continue;
			}

			Point2D p1 = path.firstPoint();
			Point2D p2 = path.lastPoint();
			double angle = Math.atan2(p2.getX() - p1.getX(), p2.getY() - p1.getY());
			//normalize between 0 and 180 degrees
			while (angle < 0) angle += Math.PI;
			while (angle > Math.PI) angle -= Math.PI;
			boolean added = false;

			for(ParallelSegmentsFinder pa: angles) {
				if (Math.abs(pa.angle - angle) < angleTolerance){
					pa.addPath(path, angle);
					added = true;
					break;
				}
			}

			if (!added) {
				ParallelSegmentsFinder pa = new ParallelSegmentsFinder();
				pa.addPath(path, angle);
				angles.add(pa);
			}
		}

		Set<PdfPath> pathsToRemove = new HashSet<PdfPath>();

		//process each direction
		for (ParallelSegmentsFinder pa: angles){
			if (pa.paths.size() < minSegments){
				continue;
			}

			List<ParallelSegmentsFinder> parts = pa.splitByDistance(maxDistance);

			for(ParallelSegmentsFinder part: parts) {
				if (part.paths.size() >= minSegments){
					pathsToRemove.addAll(part.paths);
				}
			}
		}

		//generate new path list
		List<PdfPath> result = new ArrayList<PdfPath>(layer.paths.size() - pathsToRemove.size());

		for(PdfPath path: layer.paths) {
			if (!pathsToRemove.contains(path)) {
				result.add(path);
			}
		}

		layer.paths = result;
	}


	/**
	 * This method merges together paths with common end nodes.
	 * @param layer the layer to process.
	 */
	private void concatenatePaths(LayerContents layer) {
		Map<Point2D, List<PdfPath>> pathEndpoints = new HashMap<Point2D, List<PdfPath>>();
		Set<PdfPath> mergedPaths = new HashSet<PdfPath>();
		List<PdfPath> newPaths = new ArrayList<PdfPath>();

		//fill pathEndpoints map
		for(PdfPath pp: layer.paths){
			if (pp.isClosed()) {
				newPaths.add(pp);
				continue;
			}

			List<PdfPath> paths = pathEndpoints.get(pp.firstPoint());
			if (paths == null){
				paths = new ArrayList<PdfPath>(2);
				pathEndpoints.put(pp.firstPoint(), paths);
			}
			paths.add(pp);

			paths = pathEndpoints.get(pp.lastPoint());
			if (paths == null){
				paths = new ArrayList<PdfPath>(2);
				pathEndpoints.put(pp.lastPoint(), paths);
			}
			paths.add(pp);
		}

		List<PdfPath> pathChain = new ArrayList<PdfPath>(2);
		Set<Point2D> pointsInPath = new HashSet<Point2D>();

		//join the paths
		for(PdfPath pp: layer.paths) {

			if (pp.isClosed() || mergedPaths.contains(pp)) {
				continue;
			}

			boolean changed = true;

			PdfPath firstPath = pp;
			PdfPath lastPath = pp;
			Point2D firstPoint = pp.firstPoint();
			Point2D lastPoint = pp.lastPoint();


			pathChain.clear();
			pathChain.add(pp);
			pointsInPath.clear();
			pointsInPath.add(firstPoint);
			pointsInPath.add(lastPoint);

			//process last point
			while (changed && firstPoint != lastPoint) {
				changed = false;

				List<PdfPath> adjacentPaths = pathEndpoints.get(lastPoint);
				PdfPath nextPath = findNextPath(adjacentPaths, lastPath);

				if (nextPath != null) {
					Point2D nextPoint = nextPath.getOtherEnd(lastPoint);

					lastPoint = nextPoint;
					lastPath = nextPath;
					pathChain.add(lastPath);

					if (!pointsInPath.contains(lastPoint)) {
						pointsInPath.add(lastPoint);
						changed = true;
					}
					else
					{
						//closed path found
						//remove excess segments from start of chain
						while (lastPoint != firstPoint) {
							PdfPath pathToRemove = pathChain.remove(0);
							firstPoint = pathToRemove.getOtherEnd(firstPoint);
						}

						changed = false;
					}
				}
			}


			//process first point
			changed = true;
			while (changed && firstPoint != lastPoint) {
				changed = false;

				List<PdfPath> adjacentPaths = pathEndpoints.get(firstPoint);
				PdfPath nextPath = findNextPath(adjacentPaths, firstPath);

				if (nextPath != null) {
					Point2D nextPoint = nextPath.getOtherEnd(firstPoint);

					firstPoint = nextPoint;
					firstPath = nextPath;
					pathChain.add(0, firstPath);

					if (!pointsInPath.contains(firstPoint)) {
						pointsInPath.add(firstPoint);
						changed = true;
					}
					else
					{
						//closed path found
						//remove excess segments from end of chain
						while (lastPoint != firstPoint) {
							PdfPath pathToRemove = pathChain.remove(pathChain.size() - 1);
							lastPoint = pathToRemove.getOtherEnd(lastPoint);
						}

						changed = false;
					}
				}
			}

			//remove from map
			for (PdfPath path: pathChain) {
				pathEndpoints.get(path.firstPoint()).remove(path);
				pathEndpoints.get(path.lastPoint()).remove(path);
				mergedPaths.add(path);
			}


			//construct path
			PdfPath path = pathChain.get(0);

			for (int pos = 1; pos < pathChain.size(); pos ++) {
				path.points = tryMergeNodeLists(path.points, pathChain.get(pos).points);

				if (path.points == null) {
					throw new RuntimeException();
				}
			}

			newPaths.add(path);
		}

		layer.paths = newPaths;
	}

	private PdfPath findNextPath(List<PdfPath> adjacentPaths, PdfPath firstPath) {
		for (int pos = 0; pos < adjacentPaths.size(); pos ++) {
			PdfPath p = adjacentPaths.get(pos);
			if (p != firstPath && !isSubpathOf(firstPath, p)){
				return p;
			}
		}

		return null;
	}


	/**
	 * Tests if sub is subpath of main.
	 * @param main
	 * @param sub
	 * @return
	 */
	private boolean isSubpathOf(PdfPath main, PdfPath sub) {

		Set<Point2D> points = new HashSet<Point2D>(main.points);

		for(Point2D point: sub.points) {
			if (!points.contains(point)){
				return false;
			}
		}

		return true;
	}

	private List<LayerContents> splitBySegmentKind(LayerContents layer, boolean closed, boolean single, boolean orthogonal)
	{
		if (!closed && !single) {
			return Collections.singletonList(layer);
		}

		OrthogonalShapesFilter of = new OrthogonalShapesFilter(10);

		List<PdfPath> singleSegmentPaths = new ArrayList<PdfPath>();
		List<PdfPath> multiSegmentPaths = new ArrayList<PdfPath>();
		List<PdfPath> closedPaths = new ArrayList<PdfPath>();
		List<PdfPath> orthogonalPaths = new ArrayList<PdfPath>();
		List<PdfPath> orthogonalClosedPaths = new ArrayList<PdfPath>();

		for(PdfPath path: layer.paths) {
			boolean pathOrthgonal = orthogonal && of.isOrthogonal(path);
			boolean pathUnclosed = !path.isClosed() && closed;
			boolean pathSingleSegment = path.points.size() <= 3 && single;

			if (pathSingleSegment) {
				singleSegmentPaths.add(path);
			}
			else if (pathUnclosed) {

				if (pathOrthgonal) {
					orthogonalPaths.add(path);
				}
				else {
					multiSegmentPaths.add(path);
				}
			}
			else {
				if (pathOrthgonal) {
					orthogonalClosedPaths.add(path);
				}
				else
				{
					closedPaths.add(path);
				}

			}
		}

		List<LayerContents> layers = new ArrayList<LayerContents>();

		if (multiSegmentPaths.size() > 0) {
			LayerContents l = new LayerContents();
			l.paths = multiSegmentPaths;
			l.info = layer.info.copy();

			layers.add(l);
		}

		if (singleSegmentPaths.size() > 0) {
			LayerContents l = new LayerContents();
			l.paths = singleSegmentPaths;
			l.info = layer.info.copy();
			layers.add(l);
		}


		if (orthogonalPaths.size() > 0) {
			LayerContents l = new LayerContents();
			l.paths = orthogonalPaths;
			l.info = layer.info.copy();
			layers.add(l);
		}

		if (orthogonalClosedPaths.size() > 0) {
			LayerContents l = new LayerContents();
			l.paths = orthogonalClosedPaths;
			l.info = layer.info.copy();
			layers.add(l);
		}

		if (closedPaths.size() > 0 || layer.multiPaths.size() > 0) {
			LayerContents l = new LayerContents();
			l.paths = closedPaths;
			l.info = layer.info.copy();
			l.multiPaths = layer.multiPaths;
			layers.add(l);
		}

		return layers;
	}

	private List<LayerContents> splitBySimilarGroups(LayerContents layer) {
		List<List<PdfPath>> subparts = new ArrayList<List<PdfPath>>();

		//split into similar parts
		for (PdfPath path: layer.paths) {
			List<PdfPath> sublayer = null;

			for(List<PdfPath> ll: subparts){
				if (this.pathsSimilar(ll.get(0).points, path.points))
				{
					sublayer = ll;
					break;
				}
			}

			if (sublayer == null) {
				sublayer = new ArrayList<PdfPath>();
				subparts.add(sublayer);
			}

			sublayer.add(path);
		}

		//get groups
		int minGroupTreshold = 10;

		List<PdfPath> independantPaths = new ArrayList<PdfPath>();
		List<LayerContents> result = new ArrayList<LayerContents>();

		for(List<PdfPath> list: subparts){
			if (list.size() >= minGroupTreshold) {
				LayerContents l = new LayerContents();
				l.paths = list;
				l.info = layer.info.copy();
				l.info.isGroup = true;
				l.multiPaths = Collections.EMPTY_LIST;
				result.add(l);
			}
			else
			{
				independantPaths.addAll(list);
			}
		}

		if (independantPaths.size() > 0 || layer.multiPaths.size() > 0) {
			LayerContents l = new LayerContents();
			l.paths = independantPaths;
			l.info = layer.info.copy();
			l.info.isGroup = false;
			l.multiPaths = layer.multiPaths;
			result.add(l);
		}


		return result;
	}



	private List<Point2D> tryMergeNodeLists(List<Point2D> nodes1, List<Point2D> nodes2) {

		boolean nodes1Closed = (nodes1.get(0) == nodes1.get(nodes1.size() - 1));
		boolean nodes2Closed = (nodes2.get(0) == nodes2.get(nodes2.size() - 1));

		if (nodes1Closed || nodes2Closed) {
			return null;
		}

		if (nodes1.get(nodes1.size() - 1) == nodes2.get(0)) {
			nodes1.remove(nodes1.size() -1);
			nodes1.addAll(nodes2);
			return nodes1;
		}
		else if (nodes1.get(nodes1.size() - 1) == nodes2.get(nodes2.size() -1)) {
			nodes1.remove(nodes1.size() -1);
			for (int pos = nodes2.size() - 1; pos >= 0; pos --) {
				nodes1.add(nodes2.get(pos));
			}

			return nodes1;
		}
		else if (nodes1.get(0) == nodes2.get(nodes2.size() - 1)) {
			nodes1.remove(0);
			nodes1.addAll(0, nodes2);
			return nodes1;
		}
		else if (nodes1.get(0) == nodes2.get(0)) {
			nodes1.remove(0);
			for (int pos = 0; pos < nodes2.size(); pos ++) {
				nodes1.add(0, nodes2.get(pos));
			}

			return nodes1;
		} else {
			return null;
		}
	}

	public List<LayerContents> getLayers() {
		return this.layers;
	}

	/**
	 * Test if paths are different only by offset.
	 * @return
	 */
	private boolean pathsSimilar(List<Point2D> path1, List<Point2D> path2) {
		if (path1.size() != path2.size()) {
			return false;
		}

		if (path1.size() < 3) {
			return false;
			//cannot judge so small paths
		}

		Point2D p1 = path1.get(0);
		Point2D p2 = path2.get(0);

		double offsetX = p1.getX() - p2.getX();
		double offsetY = p1.getY() - p2.getY();
		double tolerance = 1e-4;

		for(int pos = 0; pos < path1.size(); pos ++) {
			p1 = path1.get(pos);
			p2 = path2.get(pos);

			double errorX = p1.getX() - p2.getX() - offsetX;
			double errorY = p1.getY() - p2.getY() - offsetY;

			if (Math.abs(errorX) + Math.abs(errorY) > tolerance){
				return false;
			}
		}

		return true;
	}



}
