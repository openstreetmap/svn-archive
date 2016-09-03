// License: GPL. For details, see LICENSE file.
package js57toosm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringEscapeUtils;

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

/**
 * @author Malcolm Herring
 */
public final class Js57toosm {
    private Js57toosm() {
        // Hide default constructor for utilities classes
    }

    static FileInputStream in;
    static PrintStream out;
    static S57map map;
    static final ArrayList<Att> typatts = new ArrayList<>(); static {
        typatts.add(Att.OBJNAM); typatts.add(Att.NOBJNM); typatts.add(Att.STATUS); typatts.add(Att.INFORM); typatts.add(Att.NINFOM);
        typatts.add(Att.PEREND); typatts.add(Att.PERSTA); typatts.add(Att.CONDTN); typatts.add(Att.CONRAD); typatts.add(Att.CONVIS);
    }

    public static void main(String[] args) throws IOException {

        ArrayList<Long> done = new ArrayList<>();

        if (args.length < 3) {
            System.err.println("Usage: java -jar js57toosm.jar S57_filename types_filename OSM_filename");
            System.exit(-1);
        }
        try {
            in = new FileInputStream(args[0]);
        } catch (IOException e) {
            System.err.println("Input file: " + e.getMessage());
            System.exit(-1);
        }
        try {
            out = new PrintStream(args[2]);
        } catch (IOException e) {
            System.err.println("Output file: " + e.getMessage());
            in.close();
            System.exit(-1);
        }
        ArrayList<Obj> types = new ArrayList<>();
        try {
            Scanner tin = new Scanner(new FileInputStream(args[1]));
            while (tin.hasNext()) {
                Obj type = S57obj.enumType(tin.next());
                if (type != Obj.UNKOBJ)
                    types.add(type);
            }
            tin.close();
        } catch (IOException e) {
            System.err.println("Types file: " + e.getMessage());
            in.close();
            out.close();
            System.exit(-1);
        }

        map = new S57map(true);
        S57dec.decodeChart(in, map);

        out.format("<?xml version='1.0' encoding='UTF-8'?>%n");
        out.format("<osm version='0.6' upload='false' generator='js57toosm'>%n");
        out.format("<bounds minlat='%.8f' minlon='%.8f' maxlat='%.8f' maxlon='%.8f'/>%n",
                Math.toDegrees(map.bounds.minlat), Math.toDegrees(map.bounds.minlon),
                Math.toDegrees(map.bounds.maxlat), Math.toDegrees(map.bounds.maxlon));

        for (long id : map.index.keySet()) {
            Feature feature = map.index.get(id);
            String type = S57obj.stringType(feature.type);
            if (!type.isEmpty() && (types.isEmpty() || types.contains(feature.type))) {
                if (feature.reln == Rflag.MASTER) {
                    if (feature.geom.prim == Pflag.POINT) {
                        for (Prim prim : feature.geom.elems) {
                            long ref = prim.id;
                            Snode node;
                            while ((node = map.nodes.get(ref)) != null) {
                                if (!done.contains(ref)) {
                                    out.format("  <node id='%d' lat='%.8f' lon='%.8f' version='1'>%n",
                                            -ref, Math.toDegrees(node.lat), Math.toDegrees(node.lon));
                                    out.format("    <tag k='seamark:type' v=\"%s\"/>%n", type);
                                    if ((feature.type == Obj.SOUNDG) && (node.flg == S57map.Nflag.DPTH))
                                        out.format("    <tag k='seamark:sounding:depth' v='%.1f'/>%n", node.val);
                                    writeAtts(feature);
                                    out.format("  </node>%n");
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
                    if ((feature.geom.prim == Pflag.LINE) ||
                       ((feature.geom.prim == Pflag.AREA) && (feature.geom.outers == 1) && (feature.geom.inners == 0))) {
                        GeomIterator git = map.new GeomIterator(feature.geom);
                        while (git.hasComp()) {
                            git.nextComp();
                            while (git.hasEdge()) {
                                git.nextEdge();
                                while (git.hasNode()) {
                                    long ref = git.nextRef();
                                    Snode node = map.nodes.get(ref);
                                    if (!done.contains(ref)) {
                                        out.format("  <node id='%d' lat='%.8f' lon='%.8f' version='1'/>%n",
                                                -ref, Math.toDegrees(node.lat), Math.toDegrees(node.lon));
                                        done.add(ref);
                                    }
                                }
                            }
                        }
                        git = map.new GeomIterator(feature.geom);
                        while (git.hasComp()) {
                            long edge = git.nextComp();
                            out.format("  <way id='%d' version='1'>%n", -edge);
                            while (git.hasEdge()) {
                                git.nextEdge();
                                while (git.hasNode()) {
                                    long ref = git.nextRef();
                                    out.format("    <nd ref='%d'/>%n", -ref);
                                }
                            }
                            out.format("    <tag k='seamark:type' v='%s'/>%n", type);
                            writeAtts(feature);
                            out.format("  </way>%n");
                        }
                    } else if (feature.geom.prim == Pflag.AREA) {
                        GeomIterator git = map.new GeomIterator(feature.geom);
                        while (git.hasComp()) {
                            git.nextComp();
                            while (git.hasEdge()) {
                                git.nextEdge();
                                while (git.hasNode()) {
                                    long ref = git.nextRef();
                                    Snode node = map.nodes.get(ref);
                                    if (!done.contains(ref)) {
                                        out.format("  <node id='%d' lat='%.8f' lon='%.8f' version='1'/>%n",
                                                -ref, Math.toDegrees(node.lat), Math.toDegrees(node.lon));
                                        done.add(ref);
                                    }
                                }
                            }
                        }
                        git = map.new GeomIterator(feature.geom);
                        while (git.hasComp()) {
                            long ref = git.nextComp();
                            out.format("  <way id='%d' version='1'>%n", -ref);
                            while (git.hasEdge()) {
                                git.nextEdge();
                                while (git.hasNode()) {
                                    ref = git.nextRef();
                                    out.format("    <nd ref='%d'/>%n", -ref);
                                }
                            }
                            out.format("  </way>%n");
                        }
                        out.format("  <relation id='%d' version='1'>%n", -map.xref++);
                        out.format("    <tag k='type' v='multipolygon'/>%n");
                        git = map.new GeomIterator(feature.geom);
                        int outers = feature.geom.outers;
                        while (git.hasComp()) {
                            long ref = git.nextComp();
                            if (outers-- > 0) {
                                out.format("    <member type='way' ref='%d' role='outer'/>%n", -ref);
                            } else {
                                out.format("    <member type='way' ref='%d' role='inner'/>%n", -ref);
                            }
                        }
                        out.format("    <tag k='seamark:type' v='%s'/>%n", type);
                        writeAtts(feature);
                        out.format("  </relation>%n");
                    }
                }
            }
        }
        out.println("</osm>\n");
        out.close();
        System.err.println("Finished");
    }

    static void writeAtts(Feature feature) {
        for (Map.Entry<Att, AttVal<?>> item : feature.atts.entrySet()) {
            String attstr = S57att.stringAttribute(item.getKey());
            String valstr = S57val.stringValue(item.getValue(), item.getKey());
            if (!attstr.isEmpty() && !valstr.isEmpty()) {
                if (typatts.contains(item.getKey())) {
                    out.format("    <tag k='seamark:%s' v='%s'/>%n", attstr, StringEscapeUtils.escapeXml10(valstr));
                } else {
                    out.format("    <tag k='seamark:%s:%s' v='%s'/>%n",
                            S57obj.stringType(feature.type), attstr, StringEscapeUtils.escapeXml10(valstr));
                }
            }
        }
        for (Obj obj : feature.objs.keySet()) {
            ObjTab tab = feature.objs.get(obj);
            for (int ix : tab.keySet()) {
                AttMap atts = tab.get(ix);
                for (Map.Entry<Att, AttVal<?>> item : atts.entrySet()) {
                    String attstr = S57att.stringAttribute(item.getKey());
                    String valstr = S57val.stringValue(item.getValue(), item.getKey());
                    if (!attstr.isEmpty() && !valstr.isEmpty()) {
                        if ((ix == 0) && (tab.size() == 1)) {
                            out.format("    <tag k='seamark:%s:%s' v='%s'/>%n",
                                    S57obj.stringType(obj), attstr, StringEscapeUtils.escapeXml10(valstr));
                        } else {
                            out.format("    <tag k='seamark:%s:%d:%s' v='%s'/>%n",
                                    S57obj.stringType(obj), ix + 1, attstr, StringEscapeUtils.escapeXml10(valstr));
                        }
                    }
                }
            }
        }
    }

}
