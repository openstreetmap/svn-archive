/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package jseas;

import java.io.*;
import java.util.*;

public class Jseas {

	static class Node {
		public double lat;
		public double lon;

		public Node() {
			lat = 0.0;
			lon = 0.0;
		}

		public Node(double iLat, double iLon) {
			lat = iLat;
			lon = iLon;
		}
	}
	
	static class Sea {
		public String name;
		public int level;
		public int buoyage;
		public boolean system;
		public double minlat;
		public double minlon;
		public double maxlat;
		public double maxlon;
		public ArrayList<Node> nodes;
		
		public Sea() {
			name = "";
			level = 0;
			buoyage = -1;
			system = true;
			minlat = 90.0;
			minlon = 180.0;
			maxlat = -90.0;
			maxlon = -180.0;
			nodes = new ArrayList<Node>();
		}
	}
	
	static class SeaInfo {
		public String name;
		public int buoyage;
		public boolean system;
		public boolean wmm;
		public double magvar;
		public SeaInfo() {
			name = "";
			buoyage = -1;
			system = true;
			wmm = false;
		}
	}
	
	static HashMap<Long, Node> nodes = new HashMap<Long, Node>();
	static ArrayList<Sea> seas = new ArrayList<Sea>();

	public static void main(String[] args) throws IOException {

		if (args.length < 1) {
			System.err.println("Usage: java -jar jseas.jar seas_OSM_file");
			System.exit(-1);
		}
		BufferedReader inf = new BufferedReader(new FileReader(args[0]));
		makeSeas(inf);
		inf.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));		
		PrintStream out = System.out;

		String ln;
		out.println("Enter lat,lon:");
		while (true) {
			ln = in.readLine();
			String token[] = ln.split(",");
			if ((token.length == 2) && !token[0].isEmpty() && !token[1].isEmpty()) {
				try {
					Double lat = Double.parseDouble(token[0]);
					Double lon = Double.parseDouble(token[1]);
					SeaInfo info = getSeaInfo(lat, lon);
					if (info.name.isEmpty())
						out.println("No match");
					else
						out.println(info.name);
					if (info.buoyage >= 0)
						out.println("Buoyage: " + info.buoyage);
					if (info.wmm) {
						System.out.println("Magnetic Var: " + info.magvar);
					}
				} catch (Exception e) {
					out.println("Error! Enter lat,lon:");
				}
			} else {
				out.println("Error! Enter lat,lon:");
			}
		}
	}
	
	public static void makeSeas(BufferedReader inf) throws NumberFormatException, IOException {
		Sea sea = null;
		boolean inOsm = false;
		boolean inNode = false;
		boolean inWay = false;

		String ln;
		while ((ln = inf.readLine()) != null) {
			if (inOsm) {
				if (ln.contains("</osm")) {
					inOsm = false;
				} else if (inNode) {
					if (ln.contains("</node")) {
						inNode = false;
					}
				} else if (inWay) {
					if (ln.contains("</way")) {
						inWay = false;
						seas.add(sea);
					} else if (ln.contains("<nd")) {
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^ref=.+")) {
								Long ref = Long.parseLong(token.split("[\"\']")[1]);
								Node node = nodes.get(ref);
								sea.nodes.add(node);
								if (node.lat < sea.minlat) sea.minlat = node.lat;
								if (node.lon < sea.minlon) sea.minlon = node.lon;
								if (node.lat > sea.maxlat) sea.maxlat = node.lat;
								if (node.lon > sea.maxlon) sea.maxlon = node.lon;
							}
						}
					} else if (ln.contains("<tag")) {
						String key = "";
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^k=.+")) {
								key = token.split("[\"\']")[1];
							} else if (token.matches("^v=.+")) {
								switch (key) {
								case "seamark:name":
									sea.name = ln.split("v=[\"\']")[1].split("[\"\']")[0];
									break;
								case "seamark:sea_area:tier":
									sea.level = Integer.parseInt(token.split("[\"\']")[1]);
									break;
								case "seamark:navigational_system:orientation":
									sea.buoyage = Integer.parseInt(token.split("[\"\']")[1]);
									break;
								case "seamark:navigational_system:system":
									String val = token.split("[\"\']")[1];
									switch (val) {
									case "iala-a":
									case "cevni":
									case "riwr":
										sea.system = true;
										break;
									case "iala-b":
									case "bniwr2":
									case "bniwr":
									case "ppwbc":
										sea.system = false;
										break;
										default:
											sea.system = true;
									}
									break;
								}
							}
						}
					}
				} else if (ln.contains("<node")) {
					inNode = true;
					Node node = new Node();
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							long id = Long.parseLong(token.split("[\"\']")[1]);
							nodes.put(id, node);
						} else if (token.matches("^lat=.+")) {
							node.lat = Double.parseDouble(token.split("[\"\']")[1]);
						} else if (token.matches("^lon=.+")) {
							node.lon = Double.parseDouble(token.split("[\"\']")[1]);
						}
					}
					if (ln.contains("/>")) {
						inNode = false;
					}
				} else if (ln.contains("<way")) {
					inWay = true;
					sea = new Sea();
					if (ln.contains("/>")) {
						inWay = false;
					}
				}
			} else {
				if (ln.contains("<osm")) {
					inOsm = true;
				}
			}
		}
	}
	
	public static SeaInfo getSeaInfo(double lat, double lon) {
		SeaInfo info = new SeaInfo();
		String name0 = "";
		String name1 = "";
		String name2 = "";
		for (Sea tsea : seas) {
			if ((lat >= tsea.minlat) && (lat <= tsea.maxlat) && (lon >= tsea.minlon) && (lon <= tsea.maxlon) && !tsea.name.isEmpty()) {
				int cross = 0;
				Node last = tsea.nodes.get(0);
				for (Node next : tsea.nodes) {
					if (((last.lon < lon) && (next.lon > lon)) || ((last.lon > lon) && (next.lon < lon)) && ((last.lat > lat) || (next.lat > lat))) {
						if ((last.lat + ((lon - last.lon) * ((next.lat - last.lat) / (next.lon - last.lon)))) > lat) cross++;
					}
					last = next;
				}
				if ((cross % 2) == 1) {
					switch (tsea.level) {
					case 0:
						name0 = tsea.name;
						break;
					case 1:
						name1 = tsea.name;
						break;
					case 2:
						name2 = tsea.name;
						 info.buoyage = tsea.buoyage;
						break;
					}
				}
			}
		}
		if (!name2.isEmpty()) {
			if (!name0.isEmpty())
				name0 = ", " + name0;
			name0 = name2 + name0;
		}
		if (!name1.isEmpty()) {
			if (!name0.isEmpty())
				name0 = ", " + name0;
			name0 = name1 + name0;
		}
		info.name = name0;
		try {
			GregorianCalendar cal = new GregorianCalendar();
			double date = cal.get(Calendar.YEAR) + (cal.get(Calendar.DAY_OF_YEAR) / 365.25);
			Process p = Runtime.getRuntime().exec("wmm" + " " + lat + " " + lon + " " + date);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s = stdInput.readLine();
			info.magvar = Double.parseDouble(s);
			info.wmm = true;
		} catch (Exception e) {
			// No mag var
		}
		return info;
	}

}
