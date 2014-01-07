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

import s57.S57dat;
import s57.S57dat.*;
import s57.S57map;
import s57.S57map.*;

public class Js57toosm {
	
	public static int rnum = 0;

	public static void main(String[] args) throws IOException {

		FileInputStream in = new FileInputStream("/Users/mherring/boatsw/oseam/josm/plugins/smed2/js57toosm/tst.000");
		PrintStream out = System.out;

		byte[] leader = new byte[24];
		boolean ddr = false;
		int length;
		int fields;
		int mapfl, mapfp, mapts, entry;
		String tag;
		int len;
		int pos;
		
		double comf = 1;
		double somf = 1;
		long name = 0;
		S57map.Nflag nflag = Nflag.ANON;
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
    				if ("0001".equals(tag)) {
    					int i8rn = ((Long)S57dat.getSubf(record, fields+pos, S57field.I8RI, S57subf.I8RN)).intValue();
    					if (i8rn != ++rnum) {
    						out.println("Out of order record ID");
    						in.close();
    						System.exit(-1);
    					}
    				} else if ("DSID".equals(tag)) {
    				} else if ("DSSI".equals(tag)) {
    				} else if ("DSPM".equals(tag)) {
    					comf = (double)(Long)S57dat.getSubf(record, fields+pos, S57field.DSPM, S57subf.COMF);
    					somf = (double)(Long)S57dat.getSubf(S57subf.SOMF);
    				} else if ("FRID".equals(tag)) {
    				} else if ("FOID".equals(tag)) {
    				} else if ("ATTF".equals(tag)) {
    				} else if ("NATF".equals(tag)) {
    				} else if ("FFPC".equals(tag)) {
    				} else if ("FFPT".equals(tag)) {
    				} else if ("FSPC".equals(tag)) {
    				} else if ("FSPT".equals(tag)) {
    				} else if ("VRID".equals(tag)) {
    					name = (Long)S57dat.getSubf(record, fields+pos, S57field.VRID, S57subf.RCNM);
    					switch ((int)name) {
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
    					name += (Long)S57dat.getSubf(record, fields+pos, S57field.VRID, S57subf.RCID);
    					name <<= 16;
    					if (nflag == Nflag.ANON) {
    						map.newEdge(name);
    					}
    				} else if ("ATTV".equals(tag)) {
    				} else if ("VRPC".equals(tag)) {
    				} else if ("VRPT".equals(tag)) {
    					name = (Long)S57dat.getSubf(record, fields+pos, S57field.VRPT, S57subf.NAME) << 16;
    					int topi = ((Long)S57dat.getSubf(S57subf.TOPI)).intValue();
    					map.addConn(name, topi);
    					name = (Long)S57dat.getSubf(S57subf.NAME) << 16;
    					topi = ((Long)S57dat.getSubf(S57subf.TOPI)).intValue();
    					map.addConn(name, topi);
    				} else if ("SGCC".equals(tag)) {
    				} else if ("SG2D".equals(tag)) {
    					S57dat.setField(record, fields + pos, S57field.SG2D, len);
    					while (S57dat.more()) {
    						double lat = (double) ((Long) S57dat.getSubf(S57subf.YCOO)) / comf;
    						double lon = (double) ((Long) S57dat.getSubf(S57subf.XCOO)) / comf;
    						if (nflag == Nflag.ANON) {
    							map.newNode(++name, lat, lon, nflag);
    						} else {
    							map.newNode(name, lat, lon, nflag);
    						}
    						if (lat < minlat) minlat = lat;
    						if (lat > maxlat) maxlat = lat;
    						if (lon < minlon) minlon = lon;
    						if (lon > maxlon) maxlon = lon;
    					}
    				} else if ("SG3D".equals(tag)) {
    					S57dat.setField(record, fields + pos, S57field.SG3D, len);
    					while (S57dat.more()) {
    						double lat = (double) ((Long) S57dat.getSubf(S57subf.YCOO)) / comf;
    						double lon = (double) ((Long) S57dat.getSubf(S57subf.XCOO)) / comf;
    						double depth = (double) ((Long) S57dat.getSubf(S57subf.VE3D)) / somf;
    						map.newNode(name++, lat, lon, depth);
    						if (lat < minlat) minlat = lat;
    						if (lat > maxlat) maxlat = lat;
    						if (lon < minlon) minlon = lon;
    						if (lon > maxlon) maxlon = lon;
    					}
    				}
				}
			}
		}
		in.close();
		
		out.println("<?xml version='1.0' encoding='UTF-8'?>");
		out.println("<osm version='0.6' generator='js57toosm'>");
		out.println("<bounds minlat='" + minlat +"' minlon='" + minlon + "' maxlat='" + maxlat + "' maxlon='" + maxlon + "'/>");
		
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
		
		out.println("</osm>\n");
	}

}
