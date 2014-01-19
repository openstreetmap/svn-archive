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
import s57.S57att.*;
import s57.S57val;
import s57.S57val.*;
import s57.S57dat;
import s57.S57dat.*;
import s57.S57map;
import s57.S57map.*;

public class Js57toosm {
	
	public static void main(String[] args) throws IOException {

		FileInputStream in = new FileInputStream("/Users/mherring/boatsw/oseam/josm/plugins/smed2/js57toosm/tst.000");
		PrintStream out = System.out;

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
		S57map.Pflag prim = S57map.Pflag.NOSP;
		long objl = 0;
		S57map map = new S57map();
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
							prim = S57map.Pflag.POINT;
							break;
						case 2:
							prim = S57map.Pflag.LINE;
							break;
						case 3:
							prim = S57map.Pflag.AREA;
							break;
						default:
							prim = S57map.Pflag.NOSP;
						}
						objl = (long)S57dat.getSubf(S57subf.OBJL);
						break;
					case "FOID":
						name = (long) S57dat.getSubf(record, fields + pos, S57field.FOID, S57subf.LNAM);
						map.newFeature(name, prim, objl);
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
						name = (long) S57dat.getSubf(record, fields + pos, S57field.FFPT, S57subf.LNAM);
						int rind = ((Long) S57dat.getSubf(S57subf.RIND)).intValue();
						map.newObj(name, rind);
						break;
					case "FSPT":
						S57dat.setField(record, fields + pos, S57field.FSPT, len);
						do {
							name = (Long) S57dat.getSubf(S57subf.NAME) << 16;
							map.newPrim(name, (long) S57dat.getSubf(S57subf.ORNT), (long) S57dat.getSubf(S57subf.USAG));
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
			if (feature.reln != Rflag.SLAVE) {
				if (feature.geom.prim == Pflag.POINT) {
					Snode node = map.nodes.get(feature.geom.elems.get(0).id);
					String type = S57obj.stringType(feature.type);
					out.format("  <node id='%d' lat='%f' lon='%f' version='1'>%n",-id,  Math.toDegrees(node.lat), Math.toDegrees(node.lon));
					out.format("    <tag k='seamark:type' v=\"%s\"/>%n", type);
					for (Map.Entry<Att, AttVal<?>> item : feature.atts.entrySet()) {
						out.format("    <tag k='seamark:%s:%s' v=\"%s\"/>%n", type, S57att.stringAttribute(item.getKey()), S57val.stringValue(item.getValue()));
					}
					for (Reln rel : feature.rels) {
						if (rel.reln == Rflag.SLAVE) {
							Feature slave = map.index.get(rel.id);
							type = S57obj.stringType(slave.type);
							for (Map.Entry<Att, AttVal<?>> item : slave.atts.entrySet()) {
								out.format("    <tag k='seamark:%s:%s' v=\"%s\"/>%n", type, S57att.stringAttribute(item.getKey()), S57val.stringValue(item.getValue()));
							}
						}
					}
					out.format("  </node>%n");
				}
			}
		}
/*		
		for (long id : map.nodes.keySet()) {
			Snode node = map.nodes.get(id);
			if (node.flg == S57map.Nflag.DPTH) {
				out.format("  <node id='%d' lat='%f' lon='%f' version='1'>%n", -id, Math.toDegrees(node.lat), Math.toDegrees(node.lon));
				out.format("    <tag k='seamark:type' v='sounding'/>%n");
				out.format("    <tag k='seamark:sounding:depth' v='%.1f'/>%n", ((Dnode)node).val);
				out.format("  </node>%n");
			} else {
				out.format("  <node id='%d' lat='%f' lon='%f' version='1'/>%n",-id,  Math.toDegrees(node.lat), Math.toDegrees(node.lon));
			}
		}
		
		for (long id : map.edges.keySet()) {
			Edge edge = map.edges.get(id);
			out.format("  <way id='%d' version='1'>%n", -id);
			out.format("    <nd ref='%d'/>%n", -edge.first);
			for (long anon : edge.nodes) {
				out.format("    <nd ref='%d'/>%n", -anon);
			}
			out.format("    <nd ref='%d'/>%n", -edge.last);
			out.format("  </way>%n");
		}
*/		
		out.println("</osm>\n");
	}

}
