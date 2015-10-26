/* Copyright 2015 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package josmtos57;

import java.io.*;
import java.util.Scanner;
import java.util.zip.CRC32;

import s57.S57enc;
import s57.S57map;
import s57.S57osm;

public class Josmtos57 {

	static byte[] header = {
		'0', '0', '2', '6', '2', '3', 'L', 'E', '1', ' ', '0', '9', '0', '0', '0', '7', '3', ' ', ' ', ' ', '6', '6', '0', '4', '0', '0', '0', '0', '0', '0', '0', '0',
		'1', '9', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '0', '0', '0', '0', '4', '8', '0', '0', '0', '0', '1', '9', 'C', 'A', 'T', 'D', '0', '0', '0', '1',
		'2', '2', '0', '0', '0', '0', '6', '7', 0x1e, '0', '0', '0', '0', ';', '&', ' ', ' ', ' ', 0x1f, '0', '0', '0', '1', 'C', 'A', 'T', 'D', 0x1e, '0', '1', '0', '0',
		';', '&', ' ', ' ', ' ', 'I', 'S', 'O', '/', 'I', 'E', 'C', ' ', '8', '2', '1', '1', ' ', 'R', 'e', 'c', 'o', 'r', 'd', ' ', 'I', 'd', 'e', 'n', 't', 'i', 'f',
		'i', 'e', 'r', 0x1f, 0x1f, '(', 'I', '(', '5', ')', ')', 0x1e, '1', '6', '0', '0', ';', '&', ' ', ' ', ' ', 'C', 'a', 't', 'a', 'l', 'o', 'g', 'u', 'e', ' ', 'D',
		'i', 'r', 'e', 'c', 't', 'o', 'r', 'y', ' ', 'F', 'i', 'e', 'l', 'd', 0x1f, 'R', 'C', 'N', 'M', '!', 'R', 'C', 'I', 'D', '!', 'F', 'I', 'L', 'E', '!', 'L', 'F',
		'I', 'L', '!', 'V', 'O', 'L', 'M', '!', 'I', 'M', 'P', 'L', '!', 'S', 'L', 'A', 'T', '!', 'W', 'L', 'O', 'N', '!', 'N', 'L', 'A', 'T', '!', 'E', 'L', 'O', 'N',
		'!', 'C', 'R', 'C', 'S', '!', 'C', 'O', 'M', 'T', 0x1f, '(', 'A', '(', '2', ')', ',', 'I', '(', '1', '0', ')', ',', '3', 'A', ',', 'A', '(', '3', ')', ',', '4',
		'R', ',', '2', 'A', ')', 0x1e
	};
	
	static byte[] entry = {
		//*** 0
		'0', '0', '1', '0', '1', ' ', 'D', ' ', ' ', ' ', ' ', ' ', '0', '0', '0', '5', '3', ' ', ' ', ' ', '5', '5', '0', '4', // Leader
		'0', '0', '0', '1', '0', '0', '0', '0', '6', '0', '0', '0', '0', '0',   'C', 'A', 'T', 'D', '0', '0', '0', '4', '2', '0', '0', '0', '0', '6', 0x1e, // Directory
		'0', '0', '0', '0', '0', 0x1e,
		//*** 
		'C', 'D', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', // Record name+number
		//***
		'C', 'A', 'T', 'A', 'L', 'O', 'G', '.', '0', '3', '1', 0x1f, // File name
		0x1f, // File long name
		'V', '0', '1', 'X', '0', '1', 0x1f, // Volume
		'A', 'S', 'C', // Implementation
		0x1f, 0x1f, 0x1f, 0x1f, // minlat, minlon, maxlat, maxlon
		0x1f, // CRC32
		0x1f, 0x1e // Comment
	};
	
	static BufferedReader in;
	static FileOutputStream out;
	static S57map map;
	static byte[] buf;
	
	public static void main(String[] args) throws IOException {

		map = new S57map(true);
		int idx = 0;
		
		if (args.length < 4) {
			System.err.println("Usage: java -jar josmtos57.jar OSM_filename meta_data_filename S57_ENC_ROOT_directory S57_filename");
			System.exit(-1);
		}
		try {
			Scanner min = new Scanner(new FileInputStream(args[1]));
			while (min.hasNext()) {
				min.next();
			}
			min.close();
		} catch (IOException e) {
			System.err.println("Meta data file: " + e.getMessage());
			System.exit(-1);
		}
		try {
			in = new BufferedReader(new FileReader(new File(args[0])));
			S57osm.OSMmap(in, map);
			in.close();
		} catch (IOException e) {
			System.err.println("Input file: " + e.getMessage());
			System.exit(-1);
		}
		
		try {
			buf = new byte[5242880];
			idx = S57enc.encodeChart(map, buf);
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Output file too big (limit 5 MB)");
			System.exit(-1);
		}
		
		CRC32 crc = new CRC32();
		crc.reset();
		crc.update(buf, 0, idx);
		try {
			File file = new File(args[2] + "/" + args[3]);
			if (file.exists()) file.delete();
			out = new FileOutputStream(file, false);
			out.write(buf, 0, idx);
		} catch (IOException e) {
			System.err.println("Output file: " + e.getMessage());
			System.exit(-1);
		}
		out.close();
		
		try {
			File file = new File(args[2] + "/CATALOG.031");
			if (file.exists()) file.delete();
			out = new FileOutputStream(file, false);
		} catch (IOException e) {
			System.err.println("Catalogue file: " + e.getMessage());
			System.exit(-1);
		}
		out.close();

		String[] dir = (new File(args[2]).list());
		for (String item : dir) {
			System.err.println(item);
		}
		
		System.err.println("Finished");
	}

}
