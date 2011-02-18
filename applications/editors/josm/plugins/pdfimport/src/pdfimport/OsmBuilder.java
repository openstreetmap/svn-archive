package pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;

public class OsmBuilder {

	enum Mode {Draft, Final, Debug};

	private final FilePlacement placement;

	private String layerName;
	private String fillName;
	private String lineName;
	private Mode mode;

	private ProgressMonitor monitor;
	private int monitorPos;
	private int monitorTotal;

	public OsmBuilder(FilePlacement placement)
	{
		this.placement = placement;
	}

	public DataSet build(List<LayerContents> data, Mode mode, ProgressMonitor monitor) {

		this.monitor = monitor;
		this.monitorPos = 0;
		this.mode = mode;
		DataSet result = new DataSet();

		//count total items for progress monitor.
		this.monitorTotal = 0;
		for (LayerContents layer: data) {
			this.monitorTotal += layer.paths.size();
			for(PdfMultiPath mp: layer.multiPaths){
				this.monitorTotal += mp.paths.size();
			}
		}

		monitor.beginTask(tr("Building JOSM layer."), this.monitorTotal);


		for (LayerContents layer: data) {
			this.addLayer(result, layer);
		}

		monitor.finishTask();
		return result;
	}


	private void addLayer(DataSet target, LayerContents layer) {
		Map<Point2D, Node> point2Node = new HashMap<Point2D, Node>();

		this.fillName = this.printColor(layer.info.fill);
		this.lineName = this.printColor(layer.info.stroke);
		this.layerName = "" + layer.info.nr;

		//insert nodes
		for(Point2D pt: layer.points) {
			Node node = new Node();
			node.setCoor(this.placement.tranformCoords(pt));

			target.addPrimitive(node);
			point2Node.put(pt, node);
		}

		//insert ways
		Map<PdfPath, Way> path2Way = new HashMap<PdfPath, Way>();

		for (PdfPath path: layer.paths){
			Way w = this.insertWay(path, point2Node, -1, false);
			target.addPrimitive(w);
			path2Way.put(path, w);
		}

		int pathId = 0;
		for (PdfMultiPath mpath: layer.multiPaths) {
			for (PdfPath path: mpath.paths){
				Way w = this.insertWay(path, point2Node, pathId, true);
				target.addPrimitive(w);
				path2Way.put(path, w);
			}
			pathId ++;
		}

		if (this.mode != Mode.Draft) {
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

	private Way insertWay(PdfPath path, Map<Point2D, Node> point2Node, int multipathId, boolean multipolygon) {

		if (this.monitorPos % 100 == 0) {
			monitor.setExtraText(tr(" "+this.monitorPos+"/"+this.monitorTotal));
			monitor.setTicks(this.monitorPos);
		}
		this.monitorPos ++;

		List<Node> nodes = new ArrayList<Node>(path.points.size());

		for (Point2D point: path.points) {
			Node node = point2Node.get(point);
			if (node == null) {
				throw new RuntimeException();
			}

			nodes.add(node);
		}

		Map<String, String> keys = new HashMap<String, String>();

		if (this.mode != Mode.Draft) {
			keys.put("PDF_nr", "" + path.nr);
			keys.put("PDF_layer", this.layerName);

			if (path.isClosed()){
				keys.put("PDF_closed", "yes");
			}

			if (this.fillName != null){
				keys.put("PDF_fillColor", this.fillName);
			}

			if (this.lineName != null) {
				keys.put("PDF_lineColor", this.lineName);
			}

			if (multipathId != -1){
				keys.put("PDF_multipath", ""+ multipathId);
			}
			else if (path.layer.info.fill != null && !multipolygon) {
				keys.put("area", "yes");
			}
		}

		if (this.mode == Mode.Debug) {

			keys.put("PDF_pathLayer", ""+path.layer.info.nr);
			keys.put("PDF_lineWidth", ""+path.layer.info.width);
			keys.put("PDF_lineDash", ""+path.layer.info.dash);
			keys.put("PDF_layerHash", ""+path.layer.info.hashCode());
			keys.put("PDF_layerDivider", ""+path.layer.info.divider);
		}

		Way newWay = new Way();
		newWay.setNodes(nodes);
		newWay.setKeys(keys);
		return newWay;
	}



	private String printColor(Color col){
		if (col == null){
			return null;
		}

		String s = Integer.toHexString(col.getRGB() & 0xffffff);
		while (s.length() < 6) {
			s = "0" + s;
		}

		return "#" + s;
	}
}
