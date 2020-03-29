// License: GPL. For details, see LICENSE file.
package s57;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import s57.S57att.Att;
import s57.S57map.Snode;
import s57.S57obj.Obj;
import s57.S57val.CatROD;
import s57.S57val.Conv;

/**
 * @author Malcolm Herring
 */
public final class S57osm { // OSM to S57 Object/Attribute and Object/Primitive conversions
    private S57osm() {
        // Hide default constructor for utilities classes
    }

    // CHECKSTYLE.OFF: LineLength

    static class KeyVal<V> {
        Obj obj;
        Att att;
        Conv conv;
        V val;
        KeyVal(Obj o, Att a, Conv c, V v) {
            obj = o;
            att = a;
            conv = c;
            val = v;
        }
    }

    private static final HashMap<String, KeyVal<?>> OSMtags = new HashMap<>();
    static {
        OSMtags.put("natural=coastline", new KeyVal<>(Obj.COALNE, Att.UNKATT, null, null)); OSMtags.put("natural=water", new KeyVal<>(Obj.LAKARE, Att.UNKATT, null, null));
        OSMtags.put("water=river", new KeyVal<>(Obj.RIVERS, Att.UNKATT, null, null)); OSMtags.put("water=canal", new KeyVal<>(Obj.CANALS, Att.UNKATT, null, null));
        OSMtags.put("waterway=riverbank", new KeyVal<>(Obj.RIVERS, Att.UNKATT, null, null)); OSMtags.put("waterway=dock", new KeyVal<>(Obj.HRBBSN, Att.UNKATT, null, null));
        OSMtags.put("waterway=lock", new KeyVal<>(Obj.HRBBSN, Att.UNKATT, null, null)); OSMtags.put("landuse=basin", new KeyVal<>(Obj.LAKARE, Att.UNKATT, null, null));
        OSMtags.put("wetland=tidalflat", new KeyVal<>(Obj.DEPARE, Att.DRVAL2, Conv.F, 0.0)); OSMtags.put("tidal=yes", new KeyVal<>(Obj.DEPARE, Att.DRVAL2, Conv.F, 0.0));
        OSMtags.put("natural=mud", new KeyVal<>(Obj.DEPARE, Att.UNKATT, null, null)); OSMtags.put("natural=sand", new KeyVal<>(Obj.DEPARE, Att.UNKATT, null, null));
        OSMtags.put("highway=motorway", new KeyVal<>(Obj.ROADWY, Att.CATROD, Conv.E, CatROD.ROD_MWAY)); OSMtags.put("highway=trunk", new KeyVal<>(Obj.ROADWY, Att.CATROD, Conv.E, CatROD.ROD_MAJR));
        OSMtags.put("highway=primary", new KeyVal<>(Obj.ROADWY, Att.CATROD, Conv.E, CatROD.ROD_MAJR)); OSMtags.put("highway=secondary", new KeyVal<>(Obj.ROADWY, Att.CATROD, Conv.E, CatROD.ROD_MINR));
        OSMtags.put("highway=tertiary", new KeyVal<>(Obj.ROADWY, Att.CATROD, Conv.E, CatROD.ROD_MINR)); OSMtags.put("highway=residential", new KeyVal<>(Obj.ROADWY, Att.UNKATT, null, null));
        OSMtags.put("highway=unclassified", new KeyVal<>(Obj.ROADWY, Att.UNKATT, null, null)); OSMtags.put("railway=rail", new KeyVal<>(Obj.RAILWY, Att.UNKATT, null, null));
        OSMtags.put("man_made=breakwater", new KeyVal<>(Obj.SLCONS, Att.UNKATT, null, null)); OSMtags.put("man_made=groyne", new KeyVal<>(Obj.SLCONS, Att.UNKATT, null, null));
        OSMtags.put("man_made=pier", new KeyVal<>(Obj.SLCONS, Att.UNKATT, null, null)); OSMtags.put("man_made=jetty", new KeyVal<>(Obj.SLCONS, Att.UNKATT, null, null));
        OSMtags.put("landuse=industrial", new KeyVal<>(Obj.BUAARE, Att.UNKATT, null, null)); OSMtags.put("landuse=commercial", new KeyVal<>(Obj.BUAARE, Att.UNKATT, null, null));
        OSMtags.put("landuse=retail", new KeyVal<>(Obj.BUAARE, Att.UNKATT, null, null)); OSMtags.put("landuse=residential", new KeyVal<>(Obj.BUAARE, Att.UNKATT, null, null));
        OSMtags.put("boundary_type=territorial_waters", new KeyVal<>(Obj.TESARE, Att.UNKATT, null, null));
    }

    public static void OSMtag(ArrayList<KeyVal<?>> osm, String key, String val) {
        KeyVal<?> kv = OSMtags.get(key + "=" + val);
        if (kv != null) {
            if (kv.conv == Conv.E) {
                ArrayList<Enum<?>> list = new ArrayList<>();
                list.add((Enum<?>) kv.val);
                osm.add(new KeyVal<>(kv.obj, kv.att, kv.conv, list));
            } else {
                osm.add(kv);
            }
        }
        KeyVal<?> kvl = null;
        KeyVal<?> kvd = null;
        boolean rc = false;
        boolean rcl = false;
        for (KeyVal<?> kvx : osm) {
            if (kvx.obj == Obj.LAKARE) {
                kvl = kvx;
            } else if ((kvx.obj == Obj.RIVERS) || (kvx.obj == Obj.CANALS)) {
                rc = true;
            }
            if (kvx.obj == Obj.DEPARE) {
                kvd = kvx;
            } else if ((kvx.obj == Obj.RIVERS) || (kvx.obj == Obj.CANALS) || (kvx.obj == Obj.LAKARE)) {
                rcl = true;
            }
        }
        if (rc && (kvl != null)) {
            osm.remove(kvl);
        }
        if (rcl && (kvd != null)) {
            osm.remove(kvd);
        }
        return;
    }

    public static void OSMmap(File in, S57map map, boolean bb) throws Exception {
        double lat = 0;
        double lon = 0;
        long id = 0;
        long ref = 0;
        String k = "";
        String v = "";
        String type = "";
        String role = "";

        map.nodes.put(1L, new Snode());
        map.nodes.put(2L, new Snode());
        map.nodes.put(3L, new Snode());
        map.nodes.put(4L, new Snode());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(in);
        doc.getDocumentElement().normalize();
        if (!doc.getDocumentElement().getNodeName().equals("osm")) {
            System.err.println("OSM file format error");
            System.exit(-1);
        }
		NodeList nList = doc.getElementsByTagName("bounds");
		NamedNodeMap nnmap;
		if (nList.getLength() != 0) {
			nnmap = nList.item(0).getAttributes();
			map.bounds.minlat = Math.toRadians(Double.parseDouble(nnmap.getNamedItem("minlat").getNodeValue()));
			map.nodes.get(2L).lat = map.bounds.minlat;
			map.nodes.get(3L).lat = map.bounds.minlat;
			map.bounds.minlon = Math.toRadians(Double.parseDouble(nnmap.getNamedItem("minlon").getNodeValue()));
			map.nodes.get(1L).lon = map.bounds.minlon;
			map.nodes.get(2L).lon = map.bounds.minlon;
			map.bounds.maxlat = Math.toRadians(Double.parseDouble(nnmap.getNamedItem("maxlat").getNodeValue()));
			map.nodes.get(1L).lat = map.bounds.maxlat;
			map.nodes.get(4L).lat = map.bounds.maxlat;
			map.bounds.maxlon = Math.toRadians(Double.parseDouble(nnmap.getNamedItem("maxlon").getNodeValue()));
			map.nodes.get(3L).lon = map.bounds.maxlon;
			map.nodes.get(4L).lon = map.bounds.maxlon;
		}

        nList = doc.getElementsByTagName("node");
        int nLen = nList.getLength();
        for (int i = 0; i < nLen; i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            	nnmap = nNode.getAttributes();
                id = Long.parseLong(nnmap.getNamedItem("id").getNodeValue());
                lat = Double.parseDouble(nnmap.getNamedItem("lat").getNodeValue());
                lon = Double.parseDouble(nnmap.getNamedItem("lon").getNodeValue());
                map.addNode(id, lat, lon);
                NodeList tList = ((Element)nNode).getElementsByTagName("tag");
                for (int j = 0; j < tList.getLength(); j++) {
                    NamedNodeMap ntmap = tList.item(j).getAttributes();
                    k = ntmap.getNamedItem("k").getNodeValue();
                    v = ntmap.getNamedItem("v").getNodeValue();
                    if (!k.isEmpty() && !v.isEmpty()) {
                        map.addTag(k, v);
                    }
                }
                map.tagsDone(id);
            }
        }

        nList = doc.getElementsByTagName("way");
        nLen = nList.getLength();
        for (int i = 0; i < nLen; i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                nnmap = nNode.getAttributes();
                id = Long.parseLong(nnmap.getNamedItem("id").getNodeValue());
                map.addEdge(id);
                NodeList rList = ((Element)nNode).getElementsByTagName("nd");
                for (int j = 0; j < rList.getLength(); j++) {
                    NamedNodeMap nrmap = rList.item(j).getAttributes();
                    ref = Long.parseLong(nrmap.getNamedItem("ref").getNodeValue());
                    try {
                        map.addToEdge(ref);
                    } catch (Exception e) {
                        System.err.println("Unknown node in way");
                        System.exit(-1);
                    }
                }
                NodeList tList = ((Element)nNode).getElementsByTagName("tag");
                for (int j = 0; j < tList.getLength(); j++) {
                    NamedNodeMap ntmap = tList.item(j).getAttributes();
                    k = ntmap.getNamedItem("k").getNodeValue();
                    v = ntmap.getNamedItem("v").getNodeValue();
                    if (!k.isEmpty() && !v.isEmpty()) {
                        map.addTag(k, v);
                    }
                }
                map.tagsDone(id);
            }
        }

        nList = doc.getElementsByTagName("relation");
        nLen = nList.getLength();
        for (int i = 0; i < nLen; i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                nnmap = nNode.getAttributes();
                id = Long.parseLong(nnmap.getNamedItem("id").getNodeValue());
                map.addArea(id);
                NodeList rList = ((Element)nNode).getElementsByTagName("member");
                for (int j = 0; j < rList.getLength(); j++) {
                    NamedNodeMap nrmap = rList.item(j).getAttributes();
                    type = nrmap.getNamedItem("type").getNodeValue();
                    ref = Long.parseLong(nrmap.getNamedItem("ref").getNodeValue());
                    role = nrmap.getNamedItem("role").getNodeValue();
                    if ((role.equals("outer") || role.equals("inner")) && type.equals("way"))
                        map.addToArea(ref, role.equals("outer"));
                }
                NodeList tList = ((Element)nNode).getElementsByTagName("tag");
                for (int j = 0; j < tList.getLength(); j++) {
                    NamedNodeMap ntmap = tList.item(j).getAttributes();
                    k = ntmap.getNamedItem("k").getNodeValue();
                    v = ntmap.getNamedItem("v").getNodeValue();
                    if (!k.isEmpty() && !v.isEmpty()) {
                        map.addTag(k, v);
                    }
                }
                map.tagsDone(id);
            }
        }

        map.mapDone();
        return;
    }

    public static void OSMmeta(S57map map) {
        map.addEdge(++map.xref);
        for (long ref = 0; ref <= 4; ref++) {
            map.addToEdge((ref == 0) ? 4 : ref);
        }
        map.addTag("seamark:type", "coverage");
        map.addTag("seamark:coverage:category", "coverage");
        map.tagsDone(map.xref);
    }

}
