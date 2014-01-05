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

import s57.S57map;
import s57.S57map.*;
import s57.S57dat;
import s57.S57dat.*;

public class Js57toosm {
	
	public static void main(String[] args) throws IOException {

		FileInputStream in = new FileInputStream("/Users/mherring/boatsw/oseam/openseamap/renderer/js57toosm/tst.000");
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
		S57map map = new S57map();;

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
				if (!ddr) switch (tag) {
				case "0001":
					out.println("Record: " + (long)S57dat.getSubf(record, fields+pos, S57field.I8RI, S57subf.I8RN));
					break;
				case "DSID":
					break;
				case "DSSI":
					break;
				case "DSPM":
					comf = (double)(long)S57dat.getSubf(record, fields+pos, S57field.DSPM, S57subf.COMF);
					somf = (double)(long)S57dat.getSubf(S57subf.SOMF);
					break;
				case "FRID":
					break;
				case "FOID":
					break;
				case "ATTF":
					break;
				case "NATF":
					break;
				case "FFPC":
					break;
				case "FFPT":
					break;
				case "FSPC":
					break;
				case "FSPT":
					break;
				case "VRID":
					name = (long)S57dat.getSubf(record, fields+pos, S57field.VRID, S57subf.RCNM) << 32;
					name += (long)S57dat.getSubf(record, fields+pos, S57field.VRID, S57subf.RCID);
					name <<= 16;
					break;
				case "ATTV":
					break;
				case "VRPC":
					break;
				case "VRPT":
					break;
				case "SGCC":
					break;
				case "SG2D":
					S57dat.setField(record, fields + pos, S57field.SG2D, len);
					while (S57dat.more()) {
						double lat = (double) ((long) S57dat.getSubf(S57subf.YCOO)) / comf;
						double lon = (double) ((long) S57dat.getSubf(S57subf.XCOO)) / comf;
						map.addNode(name++, lat, lon);
					}
					break;
				case "SG3D":
					S57dat.setField(record, fields + pos, S57field.SG3D, len);
					while (S57dat.more()) {
						double lat = (double) ((long) S57dat.getSubf(S57subf.YCOO)) / comf;
						double lon = (double) ((long) S57dat.getSubf(S57subf.XCOO)) / comf;
						double depth = (double) ((long) S57dat.getSubf(S57subf.VE3D)) / somf;
						map.addNode(name++, lat, lon, depth);
					}
					break;
				}
			}
		}
		int a = 0; int i = 0; int c = 0; int d = 0;
		for (Snode node : map.nodes.values()) {
			switch (node.flg) {
			case ANON: a++; break;
			case ISOL: i++; break;
			case CONN: c++; break;
			case DPTH: d++; break;
			}
		}
		out.println("Anon " + a);
		out.println("Isol " + i);
		out.println("Conn " + c);
		out.println("Dpth " + d);
		in.close();
	}

}
