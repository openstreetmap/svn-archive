/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package js57toosm;

import java.io.*;
import java.util.Map;

import s57.S57obj;
import s57.S57att;
import s57.S57obj.Obj;
import s57.S57att.*;
import s57.S57val;
import s57.S57val.*;
import s57.S57dat;
import s57.S57dat.*;
import s57.S57map;
import s57.S57map.*;

public class Js57toosm {
	
	static FileInputStream in;
	static PrintStream out;
	static S57map map;
	
	public static void main(String[] args) throws IOException {

		in = new FileInputStream("/Users/mherring/boatsw/oseam/josm/plugins/smed2/js57toosm/tst.000");
		out = System.out;
		map = new S57map();

		S57dat.rnum = 0;

		byte[] leader = new byte[24];
		boolean ddr = false;
		int length;
		int fields;
		int mapfl, mapfp, mapts, entry;
		String tag;
		int len;
		int pos;
		boolean inFeature = false;
		
		double comf = 1;
		double somf = 1;
		long name = 0;
		S57map.Nflag nflag = Nflag.ANON;
		S57map.Pflag pflag = S57map.Pflag.NOSP;
		long objl = 0;
		double minlat = 90, minlon = 180, maxlat = -90, maxlon = -180;

		while (in.read(leader) == 24) {
			length = Integer.parseInt(new String(leader, 0, 5)) - 24;
			ddr = (leader[6] == 'L');
			fields = Integer.parseInt(new String(leader, 12, 5)) - 24;
			mapfl = leader[20] - '0';
			mapfp = leader[21] - '0';
			mapts = leader[23] - '0';
			entry = mapfl + mapfp + mapts;
			byte[] record = new byte[length];
			if (in.read(record) != length)
				break;
			for (int idx = 0; idx < fields-1; idx += entry) {
				tag = new String(record, idx, mapts);
				len = Integer.parseInt(new String(record, idx+mapts, mapfl));
				pos = Integer.parseInt(new String(record, idx+mapts+mapfl, mapfp));
				if (!ddr) {
					switch (tag) {
					case "0001":
						int i8rn = ((Long) S57dat.getSubf(record, fields + pos, S57field.I8RI, S57subf.I8RN)).intValue();
						if (i8rn != ++S57dat.rnum) {
							out.println("Out of order record ID");
							in.close();
							System.exit(-1);
						}
						break;
					case "DSPM":
						comf = (double) (Long) S57dat.getSubf(record, fields + pos, S57field.DSPM, S57subf.COMF);
						somf = (double) (Long) S57dat.getSubf(S57subf.SOMF);
						break;
					case "FRID":
						inFeature = true;
						switch ((int)((long)S57dat.getSubf(record, fields + pos, S57field.FRID, S57subf.PRIM))) {
						case 1:
							pflag = S57map.Pflag.POINT;
							break;
						case 2:
							pflag = S57map.Pflag.LINE;
							break;
						case 3:
							pflag = S57map.Pflag.AREA;
							break;
						default:
							pflag = S57map.Pflag.NOSP;
						}
						objl = (long)S57dat.getSubf(S57subf.OBJL);
						break;
					case "FOID":
						name = (long) S57dat.getSubf(record, fields + pos, S57field.FOID, S57subf.LNAM);
						map.newFeature(name, pflag, objl);
						break;
					case "ATTF":
						S57dat.setField(record, fields + pos, S57field.ATTF, len);
						do {
							long attl = (long) S57dat.getSubf(S57subf.ATTL);
							String atvl = (String) S57dat.getSubf(S57subf.ATVL);
							map.newAtt(attl, atvl);
						} while (S57dat.more());
						break;
					case "FFPT":
						S57dat.setField(record, fields + pos, S57field.FFPT, len);
						do {
							name = (long) S57dat.getSubf(S57subf.LNAM);
							int rind = ((Long) S57dat.getSubf(S57subf.RIND)).intValue();
							S57dat.getSubf(S57subf.COMT);
							map.newObj(name, rind);
						} while (S57dat.more());
						break;
					case "FSPT":
						S57dat.setField(record, fields + pos, S57field.FSPT, len);
						do {
							name = (Long) S57dat.getSubf(S57subf.NAME) << 16;
							map.newPrim(name, (long) S57dat.getSubf(S57subf.ORNT), (long) S57dat.getSubf(S57subf.USAG));
							S57dat.getSubf(S57subf.MASK);
						} while (S57dat.more());
						break;
					case "VRID":
						inFeature = false;
						name = (long) S57dat.getSubf(record, fields + pos, S57field.VRID, S57subf.RCNM);
						switch ((int) name) {
						case 110:
							nflag = Nflag.ISOL;
							break;
						case 120:
							nflag = Nflag.CONN;
							break;
						default:
							nflag = Nflag.ANON;
							break;
						}
						name <<= 32;
						name += (Long) S57dat.getSubf(S57subf.RCID);
						name <<= 16;
						if (nflag == Nflag.ANON) {
							map.newEdge(name);
						}
						break;
					case "VRPT":
						S57dat.setField(record, fields + pos, S57field.VRPT, len);
						do {
							name = (Long) S57dat.getSubf(S57subf.NAME) << 16;
							int topi = ((Long) S57dat.getSubf(S57subf.TOPI)).intValue();
							map.addConn(name, topi);
							S57dat.getSubf(S57subf.MASK);
						} while (S57dat.more());
						break;
					case "SG2D":
						S57dat.setField(record, fields + pos, S57field.SG2D, len);
						do {
							double lat = (double) ((Long) S57dat.getSubf(S57subf.YCOO)) / comf;
							double lon = (double) ((Long) S57dat.getSubf(S57subf.XCOO)) / comf;
							if (nflag == Nflag.ANON) {
								map.newNode(++name, lat, lon, nflag);
							} else {
								map.newNode(name, lat, lon, nflag);
							}
							if (lat < minlat)
								minlat = lat;
							if (lat > maxlat)
								maxlat = lat;
							if (lon < minlon)
								minlon = lon;
							if (lon > maxlon)
								maxlon = lon;
						} while (S57dat.more());
						break;
					case "SG3D":
						S57dat.setField(record, fields + pos, S57field.SG3D, len);
						do {
							double lat = (double) ((Long) S57dat.getSubf(S57subf.YCOO)) / comf;
							double lon = (double) ((Long) S57dat.getSubf(S57subf.XCOO)) / comf;
							double depth = (double) ((Long) S57dat.getSubf(S57subf.VE3D)) / somf;
							map.newNode(name++, lat, lon, depth);
							if (lat < minlat)
								minlat = lat;
							if (lat > maxlat)
								maxlat = lat;
							if (lon < minlon)
								minlon = lon;
							if (lon > maxlon)
								maxlon = lon;
						} while (S57dat.more());
						break;
					}
				}
				if (inFeature) {
					map.endFeature();
					inFeature = false;
				}
			}
		}
		map.endFile();
		in.close();
		
		out.println("<?xml version='1.0' encoding='UTF-8'?>");
		out.println("<osm version='0.6' generator='js57toosm'>");
		out.println("<bounds minlat='" + minlat +"' minlon='" + minlon + "' maxlat='" + maxlat + "' maxlon='" + maxlon + "'/>");
		
		for (long id : map.index.keySet()) {
			Feature feature = map.index.get(id);
			String type = S57obj.stringType(feature.type);
			if (!type.isEmpty()) {
				if (feature.reln == Rflag.MASTER) {
					if (feature.geom.prim == Pflag.POINT) {
						for (Prim prim : feature.geom.elems) {
							long ref = prim.id;
							Snode node;
							while ((node = map.nodes.get(ref)) != null) {
								out.format("  <node id='%d' lat='%.8f' lon='%.8f' version='1'>%n", -ref, Math.toDegrees(node.lat), Math.toDegrees(node.lon));
								out.format("    <tag k='seamark:type' v=\"%s\"/>%n", type);
								if ((feature.type == Obj.SOUNDG) && (node.flg == S57map.Nflag.DPTH))
									out.format("    <tag k='seamark:sounding:depth' v='%.1f'/>%n", ((Dnode) node).val);
								writeAtts(feature, type);
								out.format("  </node>%n");
								map.nodes.remove(ref++);
							}
						}
					}
				}
			}
		}
		
//int i = 256;
		for (long id : map.index.keySet()) {
//if (i-- == 0) break;
			Feature feature = map.index.get(id);
			String type = S57obj.stringType(feature.type);
			if (!type.isEmpty()) {
				if (feature.reln == Rflag.MASTER) {
					if ((feature.geom.prim == Pflag.LINE) || ((feature.geom.prim == Pflag.AREA) && (feature.geom.outers == 1) && (feature.geom.inners == 0))) {
						GeomIterator git = map.new GeomIterator(feature.geom);
						while (git.hasMore()) {
							git.getMore();
							while (git.hasNext()) {
								long ref = git.nextRef();
								Snode node = map.nodes.get(ref);
								if (node != null) {
									out.format("  <node id='%d' lat='%.8f' lon='%.8f' version='1'/>%n", -ref, Math.toDegrees(node.lat), Math.toDegrees(node.lon));
									map.nodes.remove(ref);
								}
							}
						}
						git = map.new GeomIterator(feature.geom);
						while (git.hasMore()) {
							long way = git.getMore();
							out.format("  <way id='%d' version='1'>%n", -way);
							while (git.hasNext()) {
								long ref = git.nextRef();
								out.format("    <nd ref='%d'/>%n", -ref);
							}
							out.format("    <tag k='seamark:type' v=\"%s\"/>%n", type);
							writeAtts(feature, type);
							out.format("  </way>%n");
						}
					} else if (feature.geom.prim == Pflag.AREA) {

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
