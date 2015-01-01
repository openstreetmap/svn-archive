/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package jharbour;

import java.io.*;
import java.util.*;

public class Jharbour {

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

	static HashMap<Long, Node> nodes = new HashMap<Long, Node>();
	static HashMap<Long, ArrayList<Long>> ways = new HashMap<Long, ArrayList<Long>>();

	public static void main(String[] args) throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		PrintStream out = System.out;

		ArrayList<String> tags = null;
		ArrayList<Long> refs = null;
		ArrayList<Long> outers = null;

		boolean inOsm = false;
		boolean inNode = false;
		boolean inWay = false;
		boolean inRel = false;
		boolean isHarbour = false;
		long id = 0;

		String ln;
		while ((ln = in.readLine()) != null) {
			if (inOsm) {
				if (ln.contains("</osm")) {
					inOsm = false;
					out.println("</harbours>");
				} else if (inNode) {
					if (ln.contains("</node")) {
						inNode = false;
						if (isHarbour) {
							out.println(String.format("  <harbour node=\"%d\" lat=\"%f\" lon=\"%f\">", id, nodes.get(id).lat, nodes.get(id).lon));
							for (String tag : tags) {
								out.println(tag);
							}
							out.println("  </harbour>");
						}
					} else if (ln.contains("<tag")) {
						tags.add(ln);
						if (ln.contains("seamark:type") && (ln.contains("harbour") || ln.contains("anchorage"))) {
							isHarbour = true;
						}
					}
				} else if (inWay) {
					if (ln.contains("</way")) {
						inWay = false;
						ways.put(id, refs);
						if (isHarbour) {
							Node node = findCentroid(refs);
							out.println(String.format("  <harbour way=\"%d\" lat=\"%f\" lon=\"%f\">", id, node.lat, node.lon));
							for (String tag : tags) {
								out.println(tag);
							}
							out.println("  </harbour>");
						}
					} else if (ln.contains("<nd")) {
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^ref=.+")) {
								refs.add(Long.parseLong(token.split("[\"\']")[1]));
							}
						}
					} else if (ln.contains("<tag")) {
						tags.add(ln);
						if (ln.contains("seamark:type") && (ln.contains("harbour") || ln.contains("anchorage"))) {
							isHarbour = true;
						}
					}
				} else if (inRel) {
					if (ln.contains("</relation")) {
						inRel = false;
						if (isHarbour) {
							refs = new ArrayList<Long>();
							long first = 0;
							long last = 0;
							int sweep = outers.size();
							while (!outers.isEmpty() && (sweep > 0)) {
								long way = outers.remove(0);
								if (refs.isEmpty()) {
									refs.addAll(ways.get(way));
									first = refs.get(0);
									last = refs.get(refs.size()-1);
								} else {
									ArrayList<Long> nway = ways.get(way);
									if (nway.get(0) == last) {
										refs.addAll(nway);
										last = refs.get(refs.size()-1);
										sweep = outers.size();
									} else if (nway.get(nway.size()-1) == last) {
										Collections.reverse(nway);
										refs.addAll(nway);
										last = refs.get(refs.size()-1);
										sweep = outers.size();
									} else {
										outers.add(way);
										sweep--;
									}
								}
								if (first == last) {
									Node node = findCentroid(refs);
									out.println(String.format("  <harbour rel=\"%d\" lat=\"%f\" lon=\"%f\">", id, node.lat, node.lon));
									for (String tag : tags) {
										out.println(tag);
									}
									out.println("  </harbour>");
									refs = new ArrayList<Long>();
									sweep = outers.size();
								}
							}
						}
					} else if (ln.contains("<member") && ln.contains("way") && ln.contains("outer")) {
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^ref=.+")) {
								outers.add(Long.parseLong(token.split("[\"\']")[1]));
							}
						}
					} else if (ln.contains("<tag")) {
						tags.add(ln);
						if (ln.contains("seamark:type") && (ln.contains("harbour") || ln.contains("anchorage"))) {
							isHarbour = true;
						}
					}
				} else if (ln.contains("<node")) {
					inNode = true;
					isHarbour = false;
					tags = new ArrayList<String>();
					Node node = new Node();
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
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
					isHarbour = false;
					tags = new ArrayList<String>();
					refs = new ArrayList<Long>();
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						}
					}
					if (ln.contains("/>")) {
						inWay = false;
					}
				} else if (ln.contains("<relation")) {
					inRel = true;
					isHarbour = false;
					tags = new ArrayList<String>();
					outers = new ArrayList<Long>();
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						}
					}
					if (ln.contains("/>")) {
						inRel = false;
					}
				}
			} else {
				if (ln.contains("<osm")) {
					inOsm = true;
					out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
					out.println("<harbours>");
				}
			}
		}
	}

	static Node findCentroid(ArrayList<Long> refs) {
		double lat, lon, slat, slon, llat, llon, sarc;
		boolean first = true;
		slat = slon = sarc = lat = lon = llat = llon = 0.0;
		if (refs.get(0).equals(refs.get(refs.size() - 1))) {
			for (Long ref : refs) {
				lat = nodes.get(ref).lat;
				lon = nodes.get(ref).lon;
				if (first) {
					first = false;
				} else {
					double arc = (Math.acos(Math.cos(lon - llon) * Math.cos(lat - llat)));
					slat += (lat * arc);
					slon += (lon * arc);
					sarc += arc;
				}
				llon = lon;
				llat = lat;
			}
			return new Node((sarc > 0.0 ? slat / sarc : 0.0), (sarc > 0.0 ? slon / sarc : 0.0));
		} else {
			for (Long ref : refs) {
				lat = nodes.get(ref).lat;
				lon = nodes.get(ref).lon;
				if (first) {
					first = false;
				} else {
					sarc += (Math.acos(Math.cos(lon - llon) * Math.cos(lat - llat)));
				}
				llat = lat;
				llon = lon;
			}
			double harc = sarc / 2;
			sarc = 0.0;
			first = true;
			for (Long ref : refs) {
				lat = nodes.get(ref).lat;
				lon = nodes.get(ref).lon;
				if (first) {
					first = false;
				} else {
					sarc = (Math.acos(Math.cos(lon - llon) * Math.cos(lat - llat)));
					if (sarc > harc)
						break;
				}
				harc -= sarc;
				llat = lat;
				llon = lon;
			}
			return new Node(llat + ((lat - llat) * harc / sarc), llon + ((lon - llon) * harc / sarc));
		}
	}

}
