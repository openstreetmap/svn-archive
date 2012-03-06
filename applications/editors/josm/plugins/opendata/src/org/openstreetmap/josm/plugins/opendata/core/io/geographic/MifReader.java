//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifDatum.Custom;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifDatum.Geodetic_Reference_System_1980_GRS_80;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifProjection.Hotine_Oblique_Mercator;
import static org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifProjection.Longitude_Latitude;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.projection.Epsg4326;
import org.openstreetmap.josm.data.projection.Lambert93;
import org.openstreetmap.josm.data.projection.LambertCC9Zones;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;

/**
 * MapInfo Interchange File (MIF) reader, based on this specification:
 * http://www.gissky.com/Download/Download/DataFormat/Mapinfo_Mif.pdf
 *
 */
public class MifReader extends AbstractMapInfoReader {

	private enum State {
		UNKNOWN,
		READING_COLUMNS,
		START_POLYGON,
		READING_POINTS,
		END_POLYGON,
		START_POLYLINE,
		END_POLYLINE
	}

	protected BufferedReader midReader;

	private Character delimiter = '\t';
	
	private State state = State.UNKNOWN;
	
	private Projection josmProj;
	private DataSet ds;
	private Relation region;
	private Way polygon;
	private Node node;
	private Way polyline;

	// CoordSys clause
	private MifProjection proj;
	private MifDatum datum;
	private String units;
	private Double originLon;
	private Double originLat;
	private Double stdP1;
	private Double stdP2;
	private Double azimuth;
	private Double scaleFactor;
	private Double falseEasting;
	private Double falseNorthing;
	private Double range;
	
	// Region clause
	private int numpolygons = -1;
	private int numpts = -1;
	
	public static DataSet parseDataSet(InputStream in, File file,
			AbstractDataSetHandler handler, ProgressMonitor instance) throws IOException {
		return new MifReader().parse(in, file, instance, Charset.forName(ISO8859_15));
	}

	private void parseDelimiter(String[] words) {
		delimiter = words[1].charAt(1);
	}

	private void parseUnique(String[] words) {
		// TODO
		System.err.println("TODO: "+line);
	}

	private void parseIndex(String[] words) {
		// TODO
		System.err.println("TODO: "+line);
	}

	private void parseCoordSys(String[] words) {
		for (int i = 0; i<words.length; i++) {
			words[i] = words[i].replace(",", "");
		}
		if (words[1].equalsIgnoreCase("Earth")) {
			proj = MifProjection.forCode(Integer.parseInt(words[3]));
			datum = MifDatum.forCode(Integer.parseInt(words[4]));

			// Custom datum: TODO: use custom decalage values
			int offset = datum == Custom ? 4 : 0;

			if (proj == Longitude_Latitude) {
				josmProj = new Epsg4326();
				return;
			}
			
			// Units
			units = words[5+offset];
			
			// Origin, longitude
			originLon = Double.parseDouble(words[6+offset]);
			
			// Origin, latitude
			switch(proj) {
			case Albers_Equal_Area_Conic:
			case Azimuthal_Equidistant_polar_aspect_only:
			case Equidistant_Conic_also_known_as_Simple_Conic:
			case Hotine_Oblique_Mercator:
			case Lambert_Azimuthal_Equal_Area_polar_aspect_only:
			case Lambert_Conformal_Conic:
			case Lambert_Conformal_Conic_modified_for_Belgium_1972:
			case New_Zealand_Map_Grid:
			case Stereographic:
			case Swiss_Oblique_Mercator:
			case Transverse_Mercator_also_known_as_Gauss_Kruger:
			case Transverse_Mercator_modified_for_Danish_System_34_Jylland_Fyn:
			case Transverse_Mercator_modified_for_Danish_System_45_Bornholm:
			case Transverse_Mercator_modified_for_Finnish_KKJ:
			case Transverse_Mercator_modified_for_Sjaelland:
			case Polyconic:
				originLat = Double.parseDouble(words[7+offset]);
				break;
			}
			
			// Standard Parallel 1
			switch (proj) {
			case Cylindrical_Equal_Area:
			case Regional_Mercator:
				stdP1 = Double.parseDouble(words[7+offset]);
				break;
			case Albers_Equal_Area_Conic:
			case Equidistant_Conic_also_known_as_Simple_Conic:
			case Lambert_Conformal_Conic:
			case Lambert_Conformal_Conic_modified_for_Belgium_1972:
				stdP1 = Double.parseDouble(words[8+offset]);
				break;
			}

			// Standard Parallel 2
			switch (proj) {
			case Albers_Equal_Area_Conic:
			case Equidistant_Conic_also_known_as_Simple_Conic:
			case Lambert_Conformal_Conic:
			case Lambert_Conformal_Conic_modified_for_Belgium_1972:
				stdP2 = Double.parseDouble(words[9+offset]);
				break;
			}
			
			// Azimuth
			if (proj == Hotine_Oblique_Mercator) {
				azimuth = Double.parseDouble(words[8+offset]);
			}

			// Scale Factor
			switch (proj) {
			case Hotine_Oblique_Mercator:
				scaleFactor = Double.parseDouble(words[9+offset]);
				break;
			case Stereographic:
			case Transverse_Mercator_also_known_as_Gauss_Kruger:
			case Transverse_Mercator_modified_for_Danish_System_34_Jylland_Fyn:
			case Transverse_Mercator_modified_for_Danish_System_45_Bornholm:
			case Transverse_Mercator_modified_for_Finnish_KKJ:
			case Transverse_Mercator_modified_for_Sjaelland:
				scaleFactor = Double.parseDouble(words[8+offset]);
				break;
			}
			
			// False Easting/Northing
			switch (proj) {
			case Albers_Equal_Area_Conic:
			case Equidistant_Conic_also_known_as_Simple_Conic:
			case Hotine_Oblique_Mercator:
			case Lambert_Conformal_Conic:
			case Lambert_Conformal_Conic_modified_for_Belgium_1972:
				falseEasting = Double.parseDouble(words[10+offset]);
				falseNorthing = Double.parseDouble(words[11+offset]);
				break;
			case Stereographic:
			case Transverse_Mercator_also_known_as_Gauss_Kruger:
			case Transverse_Mercator_modified_for_Danish_System_34_Jylland_Fyn:
			case Transverse_Mercator_modified_for_Danish_System_45_Bornholm:
			case Transverse_Mercator_modified_for_Finnish_KKJ:
			case Transverse_Mercator_modified_for_Sjaelland:
				falseEasting = Double.parseDouble(words[9+offset]);
				falseNorthing = Double.parseDouble(words[10+offset]);
				break;
			case New_Zealand_Map_Grid:
			case Swiss_Oblique_Mercator:
			case Polyconic:
				falseEasting = Double.parseDouble(words[8+offset]);
				falseNorthing = Double.parseDouble(words[9+offset]);
				break;
			}
										
			// Range
			switch (proj) {
			case Azimuthal_Equidistant_polar_aspect_only:
			case Lambert_Azimuthal_Equal_Area_polar_aspect_only:
				range = Double.parseDouble(words[8+offset]);
			}

			switch (proj) {
			case Lambert_Conformal_Conic:
				if ((datum == Geodetic_Reference_System_1980_GRS_80 || datum == Custom) && equals(originLon, 3.0)) {
					// This sounds good for Lambert 93 or Lambert CC 9
					if (equals(originLat, 46.5) && equals(stdP1, 44.0) && equals(stdP2, 49.0) && equals(falseEasting, 700000.0) && equals(falseNorthing, 6600000.0)) {
						josmProj = new Lambert93();
					} else if (equals(falseEasting, 1700000.0)) {
						for (int i=0; josmProj == null && i<9; i++) {
							if (equals(originLat, 42.0+i) && equals(stdP1, 41.25+i) && equals(stdP2, 42.75+i) && equals(falseNorthing, (i+1)*1000000.0 + 200000.0)) {
								josmProj = new LambertCC9Zones(i);
							}
						}
					}
				}
				break;
			default:
				// TODO
				System.err.println("TODO: "+line);
			}
			
		} else if (words[1].equalsIgnoreCase("Nonearth")) {
			// TODO
			System.err.println("TODO: "+line);
		} else if (words[1].equalsIgnoreCase("Layout")) {
			// TODO
			System.err.println("TODO: "+line);
		} else if (words[1].equalsIgnoreCase("Table")) {
			// TODO
			System.err.println("TODO: "+line);
		} else if (words[1].equalsIgnoreCase("Window")) {
			// TODO
			System.err.println("TODO: "+line);
		} else {
			System.err.println("Line "+lineNum+". Invalid CoordSys clause: "+line);
		}
	}

	private void parseTransform(String[] words) {
		// TODO
		System.err.println("TODO: "+line);
	}

	@Override
	protected void parseColumns(String[] words) {
		super.parseColumns(words);
		state = State.READING_COLUMNS;
	}

	private void parseData(String[] words) {
		if (ds == null) {
			ds = new DataSet();
		}
	}
	
	private void parsePoint(String[] words) throws IOException {
		readAttributes(createNode(words[1], words[2]));
	}

	private void parseLine(String[] words) {
		// TODO
		System.err.println("TODO: "+line);
	}
	
	private void parsePLine(String[] words) throws IOException {
		if (words.length > 2) {
			// TODO: pline with multiple sections
			polyline = new Way();
			ds.addPrimitive(polyline);
			readAttributes(polyline);
			numpts = Integer.parseInt(words[1]); // Not described in PDF but found in real files: PLINE XX, with XX = numpoints
			state = State.READING_POINTS;
		} else {
			numpts = -1;
			state = State.START_POLYLINE;
		}
	}

	private void parseRegion(String[] words) throws IOException {
		region = new Relation();
		region.put("type", "multipolygon");
		ds.addPrimitive(region);
		readAttributes(region);
		numpolygons = Integer.parseInt(words[1]);
		state = State.START_POLYGON;
	}

	private void parseArc(String[] words) {
		// TODO
		System.err.println("TODO: "+line);
	}

	private void parseText(String[] words) {
		// TODO
		System.err.println("TODO: "+line);
	}

	private void parseRect(String[] words) {
		// TODO
		System.err.println("TODO: "+line);
	}

	private void parseRoundRect(String[] words) {
		// TODO
		System.err.println("TODO: "+line);
	}

	private void parseEllipse(String[] words) {
		// TODO
		System.err.println("TODO: "+line);
	}

	private DataSet parse(InputStream in, File file, ProgressMonitor instance, Charset charset) throws IOException {
		try {
			headerReader = new BufferedReader(new InputStreamReader(in, charset));
			midReader = getDataReader(file, ".mid", charset);
			parseHeader();
			if (midReader != null) {
				midReader.close();
			}
			return ds;
		} catch (UnsupportedEncodingException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void parseHeaderLine(String[] words) throws IOException {
		if (words[0].equalsIgnoreCase("Version")) {
			parseVersion(words);
		} else if (words[0].equalsIgnoreCase("Charset")) {
			parseCharset(words);
		} else if (words[0].equalsIgnoreCase("Delimiter")) {
			parseDelimiter(words);
		} else if (words[0].equalsIgnoreCase("Unique")) {
			parseUnique(words);
		} else if (words[0].equalsIgnoreCase("Index")) {
			parseIndex(words);
		} else if (words[0].equalsIgnoreCase("CoordSys")) {
			parseCoordSys(words);
		} else if (words[0].equalsIgnoreCase("Transform")) {
			parseTransform(words);
		} else if (words[0].equalsIgnoreCase("Columns")) {
			parseColumns(words);
		} else if (words[0].equalsIgnoreCase("Data")) {
			parseData(words);
		} else if (ds != null) {
			if (state == State.START_POLYGON) {
				numpts = Integer.parseInt(words[0]);
				polygon = new Way();
				ds.addPrimitive(polygon);
				region.addMember(new RelationMember("outer", polygon));
				state = State.READING_POINTS;
				
			} else if (state == State.START_POLYLINE) {
				numpts = Integer.parseInt(words[0]);
				polyline = new Way();
				ds.addPrimitive(polyline);
				readAttributes(polyline);
				state = State.READING_POINTS;
				
			} else if (state == State.READING_POINTS && numpts > 0) {
				if (josmProj != null) {
					node = createNode(words[0], words[1]);
					if (polygon != null) {
						polygon.addNode(node);
					} else if (polyline != null) {
						polyline.addNode(node);
					}
				}
				if (--numpts == 0) {
					if (numpolygons > -1) {
						if (--numpolygons > 0) {
							state = State.START_POLYGON;
						} else {
							state = State.END_POLYGON;
							polygon = null;
						}
					} else if (polyline != null) {
						state = State.UNKNOWN;
						polyline = null;
					}
				}
			} else if (words[0].equalsIgnoreCase("Point")) {
				parsePoint(words);
			} else if (words[0].equalsIgnoreCase("Line")) {
				parseLine(words);
			} else if (words[0].equalsIgnoreCase("PLine")) {
				parsePLine(words);
			} else if (words[0].equalsIgnoreCase("Region")) {
				parseRegion(words);
			} else if (words[0].equalsIgnoreCase("Arc")) {
				parseArc(words);
			} else if (words[0].equalsIgnoreCase("Text")) {
				parseText(words);
			} else if (words[0].equalsIgnoreCase("Rect")) {
				parseRect(words);
			} else if (words[0].equalsIgnoreCase("RoundRect")) {
				parseRoundRect(words);
			} else if (words[0].equalsIgnoreCase("Ellipse")) {
				parseEllipse(words);
			} else if (words[0].equalsIgnoreCase("Pen")) {
				// Do nothing
			} else if (words[0].equalsIgnoreCase("Brush")) {
				// Do nothing
			} else if (words[0].equalsIgnoreCase("Center")) {
				// Do nothing
			} else if (words[0].equalsIgnoreCase("Symbol")) {
				// Do nothing
			} else if (words[0].equalsIgnoreCase("Font")) {
				// Do nothing
			} else if (!words[0].isEmpty()) {
				System.err.println("Line "+lineNum+". Unknown clause in data section: "+line);
			}
		} else if (state == State.READING_COLUMNS && numcolumns > 0) {
			columns.add(words[0]);
			if (--numcolumns == 0) {
				state = State.UNKNOWN;
			}
		} else if (!line.isEmpty()) {
			System.err.println("Line "+lineNum+". Unknown clause in header: "+line);
		}
	}
	
	protected void readAttributes(OsmPrimitive p) throws IOException {
		if (midReader != null) { 
			String midLine = midReader.readLine();
			if (midLine != null) {
				String[] fields = OdUtils.stripQuotes(midLine.split(delimiter.toString()), delimiter.toString());
				if (columns.size() != fields.length) {
					System.err.println("Error: Incoherence between MID and MIF files ("+columns.size()+" columns vs "+fields.length+" fields)");
				}
				for (int i=0; i<Math.min(columns.size(), fields.length); i++) {
					String field = fields[i].trim();
					/*if (field.startsWith("\"") && field.endsWith("\"")) {
						field = fields[i].substring(fields[i].indexOf('"')+1, fields[i].lastIndexOf('"'));
					}*/
					if (!field.isEmpty()) {
						p.put(columns.get(i), field);
					}
				}
			}
		}
	}
	
	protected final Node createNode(String x, String y) {
		Node node = new Node(josmProj.eastNorth2latlon(new EastNorth(Double.parseDouble(x), Double.parseDouble(y))));
		ds.addPrimitive(node);
		return node;
	}
	
	/** Compare two doubles within a default epsilon */
	public static boolean equals(Double a, Double b) {
		if (a==b) return true;
		// If the difference is less than epsilon, treat as equal.
		return Math.abs(a - b) < 0.0000001;
	}
}
