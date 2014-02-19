/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package js57toosm;

import java.io.*;
import java.util.*;

import s57.S57obj;
import s57.S57obj.*;
import s57.S57att;
import s57.S57att.*;
import s57.S57val;
import s57.S57val.*;
import s57.S57map;
import s57.S57map.*;
import s57.S57dec;

public class Js57toosm {
	
	static FileInputStream in;
	static PrintStream out;
	static S57map map;
	
	public static void main(String[] args) throws IOException {

		ArrayList<Long> done = new ArrayList<Long>();

		if (args.length < 1) {
			System.err.println("Usage: java -jar js57toosm.jar S57_filename [types_filename]");
			System.exit(-1);
		}
		in = new FileInputStream(args[0]);
		out = System.out;
		ArrayList<Obj> types = new ArrayList<Obj>();
		if (args.length == 2) {
			Scanner tin = new Scanner(new FileInputStream(args[1]));
			while (tin.hasNext()) {
				types.add(S57obj.enumType(tin.next()));
			}
			tin.close();
		}
		
		map = new S57map();
		MapBounds bounds = S57dec.decodeFile(in, types, map);

		out.format("<?xml version='1.0' encoding='UTF-8'?>%n");
		out.format("<osm version='0.6' upload='false' generator='js57toosm'>%n");
		out.format("<bounds minlat='%.8f' minlon='%.8f' maxlat='%.8f' maxlon='%.8f'/>%n", bounds.minlat, bounds.minlon, bounds.maxlat, bounds.maxlon);

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
									out.format("  <node id='%d' lat='%.8f' lon='%.8f' version='1'>%n", -ref, Math.toDegrees(node.lat), Math.toDegrees(node.lon));
									out.format("    <tag k='seamark:type' v=\"%s\"/>%n", type);
									if ((feature.type == Obj.SOUNDG) && (node.flg == S57map.Nflag.DPTH))
										out.format("    <tag k='seamark:sounding:depth' v='%.1f'/>%n", ((Dnode) node).val);
									writeAtts(feature, type);
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
					if ((feature.geom.prim == Pflag.LINE) || ((feature.geom.prim == Pflag.AREA) && (feature.geom.outers == 1) && (feature.geom.inners == 0))) {
						GeomIterator git = map.new GeomIterator(feature.geom);
						while (git.hasComp()) {
							git.nextComp();
							while (git.hasEdge()) {
								git.nextEdge();
								while (git.hasNode()) {
									long ref = git.nextRef();
									Snode node = map.nodes.get(ref);
									if (!done.contains(ref)) {
										out.format("  <node id='%d' lat='%.8f' lon='%.8f' version='1'/>%n", -ref, Math.toDegrees(node.lat), Math.toDegrees(node.lon));
										done.add(ref);
									}
								}
							}
						}
						git = map.new GeomIterator(feature.geom);
						while (git.hasComp()) {
							long way = git.nextComp();
							out.format("  <way id='%d' version='1'>%n", -way);
							while (git.hasEdge()) {
								git.nextEdge();
								while (git.hasNode()) {
									long ref = git.nextRef();
									out.format("    <nd ref='%d'/>%n", -ref);
								}
								out.format("    <tag k='seamark:type' v=\"%s\"/>%n", type);
								writeAtts(feature, type);
							}
							out.format("  </way>%n");
							done.add(way);
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
										out.format("  <node id='%d' lat='%.8f' lon='%.8f' version='1'/>%n", -ref, Math.toDegrees(node.lat), Math.toDegrees(node.lon));
										done.add(ref);
									}
								}
							}
						}
						git = map.new GeomIterator(feature.geom);
						while (git.hasComp()) {
							git.nextComp();
							while (git.hasEdge()) {
								long way = git.nextEdge();
								if (!done.contains(way)) {
									out.format("  <way id='%d' version='1'>%n", -way);
									while (git.hasNode()) {
										long ref = git.nextRef(true);
										out.format("    <nd ref='%d'/>%n", -ref);
									}
									out.format("  </way>%n");
									done.add(way);
								}
							}
						}
						out.format("  <relation id='%d' version='1'>%n", -map.ref++);
						out.format("    <tag k='type' v='multipolygon'/>%n");
						git = map.new GeomIterator(feature.geom);
						int outers = feature.geom.refs.get(0).size;
						while (git.hasComp()) {
							git.nextComp();
							while (git.hasEdge()) {
								long way = git.nextEdge();
								if (outers-- > 0) {
									out.format("    <member type='way' ref='%d' role='outer'/>%n", -way);
								} else {
									out.format("    <member type='way' ref='%d' role='inner'/>%n", -way);
								}
							}
						}
						out.format("    <tag k='seamark:type' v=\"%s\"/>%n", type);
						writeAtts(feature, type);
						out.format("  </relation>%n");
					}
				}
			}
		}
		out.println("</osm>\n");
	}
	
	static void writeAtts(Feature feature, String type) {
		for (Map.Entry<Att, AttVal<?>> item : feature.atts.entrySet()) {
			String attstr = S57att.stringAttribute(item.getKey());
			String valstr = S57val.stringValue(item.getValue());
			if (!attstr.isEmpty() && !valstr.isEmpty())
				out.format("    <tag k='seamark:%s:%s' v=\"%s\"/>%n", type, attstr, valstr);
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
							out.format("    <tag k='seamark:%s:%s' v=\"%s\"/>%n", type, attstr, valstr);
						} else {
							out.format("    <tag k='seamark:%s:%d:%s' v=\"%s\"/>%n", type, ix + 1, attstr, valstr);
						}
					}
				}
			}
		}
	}

}
