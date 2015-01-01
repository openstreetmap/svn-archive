/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package jsearch;

import java.io.*;
import java.util.*;

import jsearch.Jsearch.MapBB;

public class Extract {

	public static ArrayList<String> extractData(String filename, MapBB box) throws IOException {

		HashMap<Long, Long> nodes = new HashMap<Long, Long>();
		HashMap<Long, Long> ways = new HashMap<Long, Long>();
		HashMap<Long, Long> rels = new HashMap<Long, Long>();

		ArrayList<String> buf = new ArrayList<String>();
		BufferedReader in;
		boolean inOsm = false;
		boolean inNode = false;
		boolean inWay = false;
		boolean inRel = false;
		
		double lat = 0;
		double lon = 0;
		long id = 0;
		ArrayList<Long> refs = null;
		boolean needed = false;
		String ln;

		in = new BufferedReader(new FileReader(filename));
		while ((ln = in.readLine()) != null) {
			if (inOsm) {
				if (inNode) {
					if (ln.contains("</node")) {
						inNode = false;
					}
				} else if (ln.contains("<node")) {
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						} else if (token.matches("^lat=.+")) {
							lat = Double.parseDouble(token.split("[\"\']")[1]);
						} else if (token.matches("^lon=.+")) {
							lon = Double.parseDouble(token.split("[\"\']")[1]);
						}
					}
					if ((lat > box.minlat) && (lat <= box.maxlat) && (lon > box.minlon) && (lon <= box.maxlon)) {
						nodes.put(id, null);
					}
					if (!ln.contains("/>")) {
						inNode = true;
					}
				} else if (inWay) {
					if (ln.contains("<nd")) {
						long ref = 0;
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^ref=.+")) {
								ref = Long.parseLong(token.split("[\"\']")[1]);
								refs.add(ref);
								if (nodes.containsKey(ref)) {
									needed = true;
								}
							}
						}
					}
					if (ln.contains("</way")) {
						inWay = false;
						if (needed) {
							for (Long nd : refs) {
								nodes.put(nd, null);
							}
							ways.put(id, null);
						}
					}
				} else if (ln.contains("<way")) {
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						}
					}
					refs = new ArrayList<Long>();
					needed = false;
					if (!ln.contains("/>")) {
						inWay = true;
					}
				} else if (inRel) {
					if (ln.contains("<member")) {
						String type = "";
						String role = "";
						long ref = 0;
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^ref=.+")) {
								ref = Long.parseLong(token.split("[\"\']")[1]);
							} else if (token.matches("^type=.+")) {
								type = (token.split("[\"\']")[1]);
							} else if (token.matches("^role=.+")) {
								role = (token.split("[\"\']")[1]);
							}
						}
						if ((role.equals("outer") || role.equals("inner")) && type.equals("way")) {
							needed = ways.containsKey(ref);
							refs.add(ref);
						}
					}
					if (ln.contains("</relation")) {
						inRel = false;
						if (needed) {
							for (Long way : refs) {
								ways.put(way, null);
							}
							rels.put(id, null);
						}
					}
				} else if (ln.contains("<relation")) {
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						}
					}
					refs = new ArrayList<Long>();
					needed = false;
					if (!ln.contains("/>")) {
						inRel = true;
					}
				} else if (ln.contains("</osm")) {
					inOsm = false;
					break;
				}
			} else if (ln.contains("<osm")) {
				inOsm = true;
			}
		}
		in.close();
		
		in = new BufferedReader(new FileReader(filename));
		while ((ln = in.readLine()) != null) {
			if (inOsm) {
				if (inWay) {
					if (ln.contains("<nd")) {
						long ref = 0;
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^ref=.+")) {
								ref = Long.parseLong(token.split("[\"\']")[1]);
								if (needed) {
									nodes.put(ref, null);
								}
							}
						}
					}
					if (ln.contains("</way")) {
						inWay = false;
					}
				} else if (ln.contains("<way")) {
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						}
					}
					if (!ln.contains("/>")) {
						needed = ways.containsKey(id);
						inWay = true;
					}
				} else if (ln.contains("</osm")) {
					inOsm = false;
					break;
				}
			} else if (ln.contains("<osm")) {
				inOsm = true;
			}
		}
		in.close();
		
		in = new BufferedReader(new FileReader(filename));
		buf.add("<?xml version='1.0' encoding='UTF-8'?>");
		buf.add("<osm version='0.6' upload='false' generator='Jrender'>");
		buf.add(String.format("<bounds minlat='%.8f' minlon='%.8f' maxlat='%.8f' maxlon='%.8f'/>", box.minlat, box.minlon, box.maxlat, box.maxlon));
		while ((ln = in.readLine()) != null) {
			if (inOsm) {
				if (inNode) {
					buf.add(ln);
					if (ln.contains("</node")) {
						inNode = false;
					}
				} else if (ln.contains("<node")) {
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						}
					}
					if (nodes.containsKey(id)) {
						buf.add(ln);
						if (!ln.contains("/>")) {
							inNode = true;
						}
					}
				} else if (inWay) {
					buf.add(ln);
					if (ln.contains("</way")) {
						inWay = false;
					}
				} else if (ln.contains("<way")) {
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						}
					}
					if (ways.containsKey(id)) {
						buf.add(ln);
						if (!ln.contains("/>")) {
							inWay = true;
						}
					}
				} else if (inRel) {
					buf.add(ln);
					if (ln.contains("</relation")) {
						inRel = false;
					}
				} else if (ln.contains("<relation")) {
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						}
					}
					if (rels.containsKey(id)) {
						buf.add(ln);
						if (!ln.contains("/>")) {
							inRel = true;
						}
					}
				} else if (ln.contains("</osm")) {
					inOsm = false;
					buf.add(ln);
					break;
				}
			} else {
				if (ln.contains("<osm")) {
					inOsm = true;
				}
			}
		}
		in.close();
		
		return buf;
	}
}
