package pdfimport;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PathOptimizer {

	public Map<Point2D, Point2D> uniquePointMap;
	private final Map<LayerInfo, LayerContents> layerMap;
	private List<LayerContents> layers;
	public Rectangle2D bounds;

	public PathOptimizer()
	{
		uniquePointMap = new HashMap<Point2D, Point2D>();
		layerMap = new HashMap<LayerInfo, LayerContents>();
		layers = new ArrayList<LayerContents>();
	}

	public Point2D getUniquePoint(Point2D point) {

		if (this.uniquePointMap.containsKey(point)){
			return this.uniquePointMap.get(point);
		}
		else {
			this.uniquePointMap.put(point, point);
			return point;
		}
	}

	public void addPath(LayerInfo info, PdfPath path)
	{
		LayerContents layer = this.getLayer(info);
		layer.paths.add(path);
	}

	public void addMultiPath(LayerInfo info, List<PdfPath> paths) {
		LayerContents layer = this.getLayer(info);
		PdfMultiPath p = new PdfMultiPath(paths);
		layer.multiPaths.add(p);
	}

	private LayerContents getLayer(LayerInfo info) {
		LayerContents layer;

		if (this.layerMap.containsKey(info))
		{
			layer = this.layerMap.get(info);
		}
		else
		{
			layer = new LayerContents();
			layer.info = info.copy();
			layer.info.nr = this.layers.size();
			this.layerMap.put(layer.info, layer);
			this.layers.add(layer);
		}

		return layer;
	}

	public void optimize()
	{
		for(LayerContents layer: this.layers) {
			this.concatenataPaths(layer);
		}


		List<LayerContents> newLayers = new ArrayList<LayerContents>();
		int nr = 0;

		for(LayerContents l: this.layers) {
			List<LayerContents> splitResult = splitBySegmentKind(l);

			for(LayerContents ll: splitResult) {
				ll.info.nr = nr;
				nr ++;
				newLayers.add(ll);
			}
		}

		this.layers = newLayers;

		for(LayerContents layer: this.layers) {
			finalizeLayer(layer);
		}
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

	/**
	 * This method merges together paths with common end nodes.
	 * @param layer the layer to process.
	 */
	private void concatenataPaths(LayerContents layer) {
		Map<Point2D, PdfPath> pathEndpoints = new HashMap<Point2D, PdfPath>();
		Set<PdfPath> mergedPaths = new HashSet<PdfPath>();

		for(PdfPath pp: layer.paths){

			PdfPath path = pp;
			boolean changed = true;

			while (changed && !path.isClosed()) {
				changed  = false;

				if (pathEndpoints.containsKey(path.firstPoint())) {
					PdfPath p1 = pathEndpoints.get(path.firstPoint());
					pathEndpoints.remove(p1.firstPoint());
					pathEndpoints.remove(p1.lastPoint());

					List<Point2D> newNodes = tryMergeNodeLists(path.points, p1.points);

					if (newNodes == null)
					{
						int a = 10;
						a++;
					}

					path.points = newNodes;
					mergedPaths.add(p1);
					changed = true;
				}

				if (pathEndpoints.containsKey(path.lastPoint())) {
					PdfPath p1 = pathEndpoints.get(path.lastPoint());
					pathEndpoints.remove(p1.firstPoint());
					pathEndpoints.remove(p1.lastPoint());

					List<Point2D> newNodes = tryMergeNodeLists(path.points, p1.points);
					path.points = newNodes;
					mergedPaths.add(p1);
					changed = true;
				}
			}

			if (!path.isClosed()){
				pathEndpoints.put(path.firstPoint(), path);
				pathEndpoints.put(path.lastPoint(), path);
			}
		}

		List<PdfPath> resultPaths = new ArrayList<PdfPath>();

		for(PdfPath path: layer.paths) {
			if (!mergedPaths.contains(path)){
				resultPaths.add(path);
			}
		}

		layer.paths = resultPaths;
	}

	private List<LayerContents> splitBySegmentKind(LayerContents layer)
	{
		List<PdfPath> singleSegmentPaths = new ArrayList<PdfPath>();
		List<PdfPath> multiSegmentPaths = new ArrayList<PdfPath>();
		List<PdfPath> closedPaths = new ArrayList<PdfPath>();

		for(PdfPath path: layer.paths) {
			if (path.points.size() <= 3) {
				singleSegmentPaths.add(path);
			}
			else if (path.isClosed()) {
				closedPaths.add(path);
			}
			else {
				multiSegmentPaths.add(path);
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

		if (closedPaths.size() > 0 || layer.multiPaths.size() > 0) {
			LayerContents l = new LayerContents();
			l.paths = closedPaths;
			l.info = layer.info.copy();
			l.multiPaths = layer.multiPaths;
			layers.add(l);
		}

		return layers;
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

}
