/* Copyright 2014 Malcolm Herring
*
* This is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, version 3 of the License.
*
* For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
*/

package panels;

import java.awt.Color;
import java.awt.Dimension;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

import s57.S57att;
import s57.S57dec;
import s57.S57obj;
import s57.S57map;
import s57.S57val;
import s57.S57att.Att;
import s57.S57map.*;
import s57.S57obj.*;
import s57.S57val.AttVal;

public class PanelS57 extends JPanel {

	ArrayList<Obj> types = new ArrayList<Obj>();
	S57map map;
	ArrayList<Long> done = new ArrayList<Long>();
	
	public PanelS57() {
		setLayout(null);
		setSize(new Dimension(480, 480));
		setVisible(false);
	}

	public void startImport(File inf) throws IOException {
		JFileChooser ifc = new JFileChooser(Main.pref.get("smed2plugin.file"));
		FileInputStream in = new FileInputStream(inf);
		PanelMain.setStatus("Select OSM types file", Color.yellow);
		ifc.setCurrentDirectory(inf);
		int returnVal = ifc.showOpenDialog(Main.parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Main.pref.put("smed2plugin.file", ifc.getSelectedFile().getPath());
			Scanner tin = new Scanner(new FileInputStream(ifc.getSelectedFile()));
			while (tin.hasNext()) {
				types.add(S57obj.enumType(tin.next()));
			}
			tin.close();
		}
		map = new S57map();
		MapBounds bounds = S57dec.decodeFile(in, types, map);

		in.close();
		
		DataSet data = new DataSet();
		data.setUploadDiscouraged(true);
		
		for (long id : map.index.keySet()) {
			Feature feature = map.index.get(id);
			String type = S57obj.stringType(feature.type);
			if (!type.isEmpty() && (types.isEmpty() || types.contains(feature.type))) {
				if (feature.reln == Rflag.MASTER) {
					if (feature.geom.prim == Pflag.POINT) {
						for (Prim prim : feature.geom.elems) {
							long ref = prim.id;
							Snode snode;
							while ((snode = map.nodes.get(ref)) != null) {
								if (!done.contains(ref)) {
									Node node = new Node(new LatLon(Math.toDegrees(snode.lat), Math.toDegrees(snode.lon)));
									node.setOsmId(ref, 1);
									data.addPrimitive(node);
									addKeys(node, feature, type);
									done.add(ref);
								}
								ref++;
							}
						}
					}
				}
			}
		}
		for (long id : map.index.keySet()) {
			Feature feature = map.index.get(id);
			String type = S57obj.stringType(feature.type);
			if (!type.isEmpty() && (types.isEmpty() || types.contains(feature.type))) {
				if (feature.reln == Rflag.MASTER) {
					if ((feature.geom.prim == Pflag.LINE) || ((feature.geom.prim == Pflag.AREA) && (feature.geom.outers == 1) && (feature.geom.inners == 0))) {
						GeomIterator git = map.new GeomIterator(feature.geom);
						while (git.hasComp()) {
							git.nextComp();
							while (git.hasEdge()) {
								git.nextEdge();
								while (git.hasNode()) {
									long ref = git.nextRef();
									Snode snode = map.nodes.get(ref);
									if (!done.contains(ref)) {
										Node node = new Node(new LatLon(Math.toDegrees(snode.lat), Math.toDegrees(snode.lon)));
										node.setOsmId(ref, 1);
										data.addPrimitive(node);
										done.add(ref);
									}
								}
							}
						}
						git = map.new GeomIterator(feature.geom);
						while (git.hasComp()) {
							long edge = git.nextComp();
							Way way = new Way(edge, 1);
							data.addPrimitive(way);
							while (git.hasEdge()) {
								git.nextEdge();
								while (git.hasNode()) {
									long ref = git.nextRef();
									way.addNode((Node)data.getPrimitiveById(ref, OsmPrimitiveType.NODE));
								}
								addKeys(way, feature, type);
							}
							done.add(edge);
						}
					} else if (feature.geom.prim == Pflag.AREA) {
						GeomIterator git = map.new GeomIterator(feature.geom);
						while (git.hasComp()) {
							git.nextComp();
							while (git.hasEdge()) {
								git.nextEdge();
								while (git.hasNode()) {
									long ref = git.nextRef();
									Snode snode = map.nodes.get(ref);
									if (!done.contains(ref)) {
										Node node = new Node(new LatLon(Math.toDegrees(snode.lat), Math.toDegrees(snode.lon)));
										node.setOsmId(ref, 1);
										data.addPrimitive(node);
										done.add(ref);
									}
								}
							}
						}
						git = map.new GeomIterator(feature.geom);
						while (git.hasComp()) {
							git.nextComp();
							while (git.hasEdge()) {
								long edge = git.nextEdge();
								if (!done.contains(edge)) {
									Way way = new Way(edge, 1);
									data.addPrimitive(way);
									while (git.hasNode()) {
										long ref = git.nextRef(true);
										way.addNode((Node)data.getPrimitiveById(ref, OsmPrimitiveType.NODE));
									}
									done.add(edge);
								}
							}
						}
						Relation rel = new Relation(map.ref++, 1);
						data.addPrimitive(rel);
						git = map.new GeomIterator(feature.geom);
						int outers = feature.geom.refs.get(0).size;
						while (git.hasComp()) {
							git.nextComp();
							while (git.hasEdge()) {
								long way = git.nextEdge();
								if (outers-- > 0) {
									rel.addMember(new RelationMember("outer", (Way)data.getPrimitiveById(way, OsmPrimitiveType.WAY)));
								} else {
									rel.addMember(new RelationMember("inner", (Way)data.getPrimitiveById(way, OsmPrimitiveType.WAY)));
								}
							}
						}
						addKeys(rel, feature, type);
					}
				}
			}
		}

		OsmDataLayer layer = new OsmDataLayer(data, "S-57 Import", null);
		Main.map.mapView.addLayer(layer);
		Main.map.mapView.zoomTo(new Bounds(bounds.minlat, bounds.minlon, bounds.maxlat, bounds.maxlon));
		PanelMain.setStatus("Import done", Color.green);
	}

	void addKeys(OsmPrimitive prim, Feature feature, String type) {
		HashMap<String,String> keys = new HashMap<String,String>();
		if (prim instanceof Relation) {
			keys.put("type", "multipolygon");
		}
		keys.put("seamark:type", type);
		if (feature.type == Obj.SOUNDG) {
			Snode snode = map.nodes.get(feature.geom.elems.get(0).id);
			if (snode.flg == S57map.Nflag.DPTH) {
				keys.put("seamark:sounding:depth", ((Double)((Dnode)snode).val).toString());
			}
		}
		for (Map.Entry<Att, AttVal<?>> item : feature.atts.entrySet()) {
			String attstr = S57att.stringAttribute(item.getKey());
			String valstr = S57val.stringValue(item.getValue());
			if (!attstr.isEmpty() && !valstr.isEmpty()) {
					keys.put(("seamark:" + type + ":" + attstr), valstr);
			}
		}
		for (Obj obj : feature.objs.keySet()) {
			ObjTab tab = feature.objs.get(obj);
			for (int ix : tab.keySet()) {
				type = S57obj.stringType(obj);
				AttMap atts = tab.get(ix);
				for (Map.Entry<Att, AttVal<?>> item : atts.entrySet()) {
					String attstr = S57att.stringAttribute(item.getKey());
					String valstr = S57val.stringValue(item.getValue());
					if (!attstr.isEmpty() && !valstr.isEmpty()) {
						if ((ix == 0) && (tab.size() == 1)) {
							keys.put(("seamark:" + type + ":" + attstr), valstr);
						} else {
							keys.put(("seamark:" + type + ":" + (ix+1) + ":" + attstr), valstr);
						}
					}
				}
			}
		}
		prim.setKeys(keys);
	}

	
	public void startExport(File outf) throws IOException {
		
	}
}
