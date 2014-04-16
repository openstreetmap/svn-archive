/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package jrender;

import java.io.IOException;

public class Jrender {

	public static class MapBB {
		public double minlat;
		public double minlon;
		public double maxlat;
		public double maxlon;
		public MapBB(double nt, double nn, double xt, double xn) {
			minlat = nt;
			minlon = nn;
			maxlat = xt;
			maxlon = xn;
		}
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length < 5) {
			System.err.println("Usage: java -jar jrender.jar osm_file minlat, minlon, maxlat, maxlon");
			System.exit(-1);
		}
		
		MapBB bb = new MapBB(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
		Tilegen.tileMap(Extract.extractData(args[0], bb), bb);

		System.err.println("Finished");
		System.exit(0);
	}
}
