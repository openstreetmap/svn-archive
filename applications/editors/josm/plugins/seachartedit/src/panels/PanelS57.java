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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.UploadPolicy;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.spi.preferences.Config;

import s57.S57att;
import s57.S57att.Att;
import s57.S57dec;
import s57.S57map;
import s57.S57map.AttMap;
import s57.S57map.Feature;
import s57.S57map.GeomIterator;
import s57.S57map.ObjTab;
import s57.S57map.Pflag;
import s57.S57map.Prim;
import s57.S57map.Rflag;
import s57.S57map.Snode;
import s57.S57obj;
import s57.S57obj.Obj;
import s57.S57val;
import s57.S57val.AttVal;

public class PanelS57 extends JPanel {

    ArrayList<Obj> types = new ArrayList<>();
    S57map map;
    HashMap<Long, Long> uids = new HashMap<>();

    public PanelS57() {
        setLayout(null);
        setSize(new Dimension(480, 480));
        setVisible(false);
    }

    public void startImport(File inf) throws IOException {
        FileInputStream in = new FileInputStream(inf);
        PanelMain.setStatus("Select OSM types file", Color.yellow);
        JFileChooser ifc = new JFileChooser(Config.getPref().get("smed2plugin.typesfile"));
        ifc.setSelectedFile(new File(Config.getPref().get("smed2plugin.typesfile")));
        int returnVal = ifc.showOpenDialog(MainApplication.getMainFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            Config.getPref().put("smed2plugin.typesfile", ifc.getSelectedFile().getPath());
            Scanner tin = new Scanner(new FileInputStream(ifc.getSelectedFile()), "UTF-8");
            while (tin.hasNext()) {
                Obj type = S57obj.enumType(tin.next());
                if (type != Obj.UNKOBJ)
                    types.add(type);
            }
            tin.close();
        }
        map = new S57map(true);
        S57dec.decodeChart(in, map);

        in.close();

        DataSet data = new DataSet();
        data.setUploadPolicy(UploadPolicy.DISCOURAGED);

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
                                if (!uids.containsKey(ref)) {
                                    Node node = new Node(0, 1);
                                    node.setCoor((new LatLon(Math.toDegrees(snode.lat), Math.toDegrees(snode.lon))));
                                    data.addPrimitive(node);
                                    addKeys(node, feature, type);
                                    uids.put(ref, node.getUniqueId());
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
                    if ((feature.geom.prim == Pflag.LINE) || ((feature.geom.prim == Pflag.AREA)
                            && (feature.geom.outers == 1) && (feature.geom.inners == 0))) {
                        GeomIterator git = map.new GeomIterator(feature.geom);
                        while (git.hasComp()) {
                            git.nextComp();
                            while (git.hasEdge()) {
                                git.nextEdge();
                                while (git.hasNode()) {
                                    long ref = git.nextRef();
                                    Snode snode = map.nodes.get(ref);
                                    if (!uids.containsKey(ref)) {
                                        Node node = new Node(0, 1);
                                        node.setCoor((new LatLon(Math.toDegrees(snode.lat), Math.toDegrees(snode.lon))));
                                        data.addPrimitive(node);
                                        uids.put(ref, node.getUniqueId());
                                    }
                                }
                            }
                        }
                        git = map.new GeomIterator(feature.geom);
                        while (git.hasComp()) {
                            git.nextComp();
                            Way way = new Way(0, 1);
                            data.addPrimitive(way);
                            while (git.hasEdge()) {
                                git.nextEdge();
                                while (git.hasNode()) {
                                    long ref = git.nextRef();
                                    way.addNode((Node) data.getPrimitiveById(uids.get(ref), OsmPrimitiveType.NODE));
                                }
                            }
                            addKeys(way, feature, type);
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
                                    if (!uids.containsKey(ref)) {
                                        Node node = new Node(0, 1);
                                        node.setCoor((new LatLon(Math.toDegrees(snode.lat), Math.toDegrees(snode.lon))));
                                        data.addPrimitive(node);
                                        uids.put(ref, node.getUniqueId());
                                    }
                                }
                            }
                        }
                        git = map.new GeomIterator(feature.geom);
                        while (git.hasComp()) {
                            long ref = git.nextComp();
                            Way way = new Way(0, 1);
                            uids.put(ref, way.getUniqueId());
                            data.addPrimitive(way);
                            while (git.hasEdge()) {
                                git.nextEdge();
                                while (git.hasNode()) {
                                    ref = git.nextRef();
                                    way.addNode((Node) data.getPrimitiveById(uids.get(ref), OsmPrimitiveType.NODE));
                                }
                            }
                        }
                        Relation rel = new Relation(0, 1);
                        data.addPrimitive(rel);
                        git = map.new GeomIterator(feature.geom);
                        int outers = feature.geom.outers;
                        while (git.hasComp()) {
                            long ref = git.nextComp();
                            if (outers-- > 0) {
                                rel.addMember(new RelationMember("outer", data.getPrimitiveById(uids.get(ref), OsmPrimitiveType.WAY)));
                            } else {
                                rel.addMember(new RelationMember("inner", data.getPrimitiveById(uids.get(ref), OsmPrimitiveType.WAY)));
                            }
                        }
                        addKeys(rel, feature, type);
                    }
                }
            }
        }

        OsmDataLayer layer = new OsmDataLayer(data, "S-57 Import", null);
        MainApplication.getLayerManager().addLayer(layer);
        MainApplication.getMap().mapView.zoomTo(new Bounds(Math.toDegrees(map.bounds.minlat), Math.toDegrees(map.bounds.minlon),
                                           Math.toDegrees(map.bounds.maxlat), Math.toDegrees(map.bounds.maxlon)));
        PanelMain.setStatus("Import done", Color.green);
    }

    void addKeys(OsmPrimitive prim, Feature feature, String type) {
        HashMap<String, String> keys = new HashMap<>();
        if (prim instanceof Relation) {
            keys.put("type", "multipolygon");
        }
        keys.put("seamark:type", type);
        if (feature.type == Obj.SOUNDG) {
            Snode snode = map.nodes.get(feature.geom.elems.get(0).id);
            if (snode.flg == S57map.Nflag.DPTH) {
                keys.put("seamark:sounding:depth", ((Double) snode.val).toString());
            }
        }
        for (Map.Entry<Att, AttVal<?>> item : feature.atts.entrySet()) {
            String attstr = S57att.stringAttribute(item.getKey());
            String valstr = S57val.stringValue(item.getValue(), item.getKey());
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
                    String valstr = S57val.stringValue(item.getValue(), item.getKey());
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
