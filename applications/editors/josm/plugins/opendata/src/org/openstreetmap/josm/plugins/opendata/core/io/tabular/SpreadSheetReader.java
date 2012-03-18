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
package org.openstreetmap.josm.plugins.opendata.core.io.tabular;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.projection.LambertCC9Zones;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.ProjectionChooser;
import org.openstreetmap.josm.plugins.opendata.core.io.ProjectionPatterns;

public abstract class SpreadSheetReader extends AbstractReader implements OdConstants {
	
	private static final NumberFormat formatFrance = NumberFormat.getInstance(Locale.FRANCE);
	private static final NumberFormat formatUK = NumberFormat.getInstance(Locale.UK);
	
	protected static final LambertCC9Zones[] projLambCC9Zones = new LambertCC9Zones[9];
	static {
		for (int i=0; i<projLambCC9Zones.length; i++) {
			projLambCC9Zones[i] = new LambertCC9Zones(i);
		}
	}

	protected final SpreadSheetHandler handler;

	public SpreadSheetReader(SpreadSheetHandler handler) {
		this.handler = handler;
	}

	protected static double parseDouble(String value) throws ParseException {
		if (value.contains(",")) { 
			return formatFrance.parse(value.replace(" ", "")).doubleValue();
		} else {
			return formatUK.parse(value.replace(" ", "")).doubleValue();
		}
	}
	
	protected abstract void initResources(InputStream in, ProgressMonitor progressMonitor) throws IOException;
	
	protected abstract String[] readLine(ProgressMonitor progressMonitor) throws IOException;
	
	protected final int getSheetNumber() {
		return handler != null && handler.getSheetNumber() > -1 ? handler.getSheetNumber() : 0;
	}
	
	private class CoordinateColumns {
		public int xCol = -1;
		public int yCol = -1;
		public final boolean isOk() {
			return xCol > -1 && yCol > -1;
		}
	}
	
	public DataSet doParse(String[] header, ProgressMonitor progressMonitor) throws IOException {
		System.out.println("Header: "+Arrays.toString(header));
		
		Map<ProjectionPatterns, CoordinateColumns> projColumns = new HashMap<ProjectionPatterns, CoordinateColumns>();
		
		// TODO: faire une liste de coordonnees pour les cas ou plusieurs coordonnées dans la meme projection sont présentes (ex assainissement) 
		for (int i = 0; i<header.length; i++) {
			for (ProjectionPatterns pp : PROJECTIONS) {
				CoordinateColumns col = projColumns.get(pp);
				if (pp.getXPattern().matcher(header[i]).matches()) {
					if (col == null) {
						projColumns.put(pp, col = new CoordinateColumns());
					}
					col.xCol = i;
				} else if (pp.getYPattern().matcher(header[i]).matches()) {
					if (col == null) {
						projColumns.put(pp, col = new CoordinateColumns());
					}
					col.yCol = i;
				}
			}
		}

		Projection proj = null;
		CoordinateColumns columns = null;
		Collection<Integer> allProjIndexes = new ArrayList<Integer>();
		
		for (ProjectionPatterns pp : projColumns.keySet()) {
			CoordinateColumns col = projColumns.get(pp);
			if (col.isOk()) {
				if (proj == null) {
					proj = pp.getProjection(header[col.xCol], header[col.yCol]);
					columns = col;
				}
				allProjIndexes.add(col.xCol);
				allProjIndexes.add(col.yCol);
			}
		}

		final boolean handlerOK = handler != null && handler.handlesProjection();

		if (proj != null) {
			// projection identified, do nothing
		} else if (columns != null) {
			if (!handlerOK) {
				// TODO: filter proposed projections with min/max values ?
				ProjectionChooser dialog = (ProjectionChooser) new ProjectionChooser(progressMonitor.getWindowParent()).showDialog();
				if (dialog.getValue() != 1) {
					return null; // User clicked Cancel
				}
				proj = dialog.getProjection();
			}
			
		} else {
			throw new IllegalArgumentException(tr("No valid coordinates have been found."));
		}

		System.out.println("Loading data using projection "+proj+" ("+header[columns.xCol]+", "+header[columns.yCol]+")");
				
		final DataSet ds = new DataSet();
		int lineNumber = 1;
		
		String[] fields;
		while ((fields = readLine(progressMonitor)) != null) {
			lineNumber++;
			EastNorth en = new EastNorth(Double.NaN, Double.NaN);
			Node n = new Node();
			for (int i = 0; i<fields.length; i++) {
				try {
					if (i >= header.length) {
						throw new IllegalArgumentException(tr("Invalid file. Bad length on line {0}. Expected {1} columns, got {2}.", lineNumber, header.length, i+1));
					} else if (i == columns.xCol) {
						en.setLocation(parseDouble(fields[i]), en.north());
					} else if (i == columns.yCol) {
						en.setLocation(en.east(), parseDouble(fields[i]));
					} else if (!allProjIndexes.contains(i)) {
						if (!fields[i].isEmpty()) {
							n.put(header[i], fields[i]);
						}
					}
				} catch (ParseException e) {
					System.err.println("Warning: Parsing error on line "+lineNumber+": "+e.getMessage());
				}
			}
			if (en.isValid()) {
				n.setCoor(proj != null && !handlerOK ? proj.eastNorth2latlon(en) : handler.getCoor(en, fields));
			} else {
				System.err.println("Warning: Skipping line "+lineNumber+" because no valid coordinates have been found.");
			}
			if (n.getCoor() != null) {
				ds.addPrimitive(n);
			}
		}
		
		return ds;
	}
	
	public final DataSet parse(InputStream in, ProgressMonitor progressMonitor) throws IOException {
		
		initResources(in, progressMonitor);
		
		String[] header = null;
		int length = 0;
		
		while (header == null || length == 0) {
			header = readLine(progressMonitor);
			length = 0;
			if (header == null) {
				return null;
			} else for (String field : header) {
				length += field.length();
			}
		}
		
		return doParse(header, progressMonitor);
	}
}
