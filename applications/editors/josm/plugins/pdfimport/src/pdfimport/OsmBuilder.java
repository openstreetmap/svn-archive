package pdfimport;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.Projection;

public class OsmBuilder {

	public Projection projection = null;
	public double minX = 0;
	public double maxX = 1;
	public double minY = 0;
	public double maxY = 1;

	public double minEast = 0;
	public double maxEast = 10000;
	public double minNorth = 0;
	public double maxNorth = 10000;

	public OsmBuilder()
	{
	}

	public void setPdfBounds(double minX, double minY, double maxX, double maxY){
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	public void setEastNorthBounds(double minEast, double minNorth, double maxEast, double maxNorth) {
		this.minEast = minEast;
		this.maxEast = maxEast;
		this.minNorth = minNorth;
		this.maxNorth = maxNorth;
	}


	public Bounds getWorldBounds(PathOptimizer data) {
		LatLon min = tranformCoords(data.bounds.getMinX(), data.bounds.getMinY());
		LatLon max = tranformCoords(data.bounds.getMaxX(), data.bounds.getMaxY());
		return new Bounds(min, max);
	}

	public DataSet build(List<LayerContents> data, boolean full) {

		DataSet result = new DataSet();

		for (LayerContents layer: data) {
			this.addLayer(result, layer, full);
		}
		return result;
	}


	private void addLayer(DataSet target, LayerContents layer, boolean full) {
		Map<Point2D, Node> point2Node = new HashMap<Point2D, Node>();

		//insert nodes
		for(Point2D pt: layer.points) {
			Node node = new Node();
			node.setCoor(this.tranformCoords(pt.getX(), pt.getY()));

			target.addPrimitive(node);
			point2Node.put(pt, node);
		}

		//insert ways
		Map<PdfPath, Way> path2Way = new HashMap<PdfPath, Way>();

		for (PdfPath path: layer.paths){
			Way w = this.insertWay(path, point2Node, -1, full, false);
			target.addPrimitive(w);
			path2Way.put(path, w);
		}

		int pathId = 0;
		for (PdfMultiPath mpath: layer.multiPaths) {
			for (PdfPath path: mpath.paths){
				Way w = this.insertWay(path, point2Node, pathId, full, true);
				target.addPrimitive(w);
				path2Way.put(path, w);
			}
			pathId ++;
		}

		if (full) {
			//insert relations
			for (PdfMultiPath mpath: layer.multiPaths) {
				Relation rel = new Relation();

				Map<String, String> keys = new HashMap<String, String>();
				keys.put("type", "multipolygon");
				keys.put("area", "yes");
				rel.setKeys(keys);

				for (PdfPath path: mpath.paths){
					Way w = path2Way.get(path);
					rel.addMember(new RelationMember("", w));
				}

				target.addPrimitive(rel);
			}
		}
	}

	private Way insertWay(PdfPath path, Map<Point2D, Node> point2Node, int multipathId, boolean full, boolean multipolygon) {

		List<Node> nodes = new ArrayList<Node>(path.points.size());

		for (Point2D point: path.points) {
			Node node = point2Node.get(point);
			if (node == null) {
				throw new RuntimeException();
			}

			nodes.add(node);
		}

		Map<String, String> keys = new HashMap<String, String>();

		if (full) {
			keys.put("PDF_nr", "" + path.nr);
			keys.put("PDF_layer", "" + path.layer.info.nr);
			keys.put("PDF_closed", "" + path.isClosed());

			if (path.layer.info.fill){
				keys.put("PDF_fillColor", printColor(path.layer.info.fillColor));
			}

			if (path.layer.info.stroke) {
				keys.put("PDF_lineColor", printColor(path.layer.info.color));
			}

			if (multipathId != -1){
				keys.put("PDF_multipath", ""+ multipathId);
			}
			else if (path.layer.info.fill && !multipolygon) {
				keys.put("area", "yes");
			}
		}

		Way newWay = new Way();
		newWay.setNodes(nodes);
		newWay.setKeys(keys);
		return newWay;
	}

	private String printColor(Color col){
		return "#" + Integer.toHexString(col.getRGB() & 0xffffff);
	}


	private LatLon tranformCoords(double x, double y) {

		if (this.projection == null){
			return new LatLon(y/1000, x/1000);
		}
		else{
			x = (x - this.minX) * (this.maxEast - this.minEast) / (this.maxX - this.minX)  + this.minEast;
			y = (y - this.minY) * (this.maxNorth - this.minNorth) /  (this.maxY - this.minY) + this.minNorth;
			return this.projection.eastNorth2latlon(new EastNorth(x, y));
		}
	}

	public EastNorth reverseTransform(LatLon coor) {
		if (this.projection == null){
			return new EastNorth(coor.lon() * 1000, coor.lat() * 1000);
		}
		else{
			return null;
		}
	}
}
