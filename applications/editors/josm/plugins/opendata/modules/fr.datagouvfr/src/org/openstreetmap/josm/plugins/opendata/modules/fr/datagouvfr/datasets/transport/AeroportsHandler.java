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
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.transport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.DefaultSpreadSheetHandler;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class AeroportsHandler extends DataGouvDataSetHandler {

	private static final Pattern COOR_PATTERN = Pattern.compile(
			"(-?\\p{Digit}+)°\\p{Space}*(\\p{Digit}+)'\\p{Space}*(\\p{Digit}+)\\p{Space}*((Nord|Sud|Est|Ouest)?)", Pattern.CASE_INSENSITIVE);
	
	public AeroportsHandler() {
		super("Aéroports-français-coordonnées-géographiques-30382044");
		setName("Aéroports");
		setDownloadFileName("coordonn_es g_ographiques a_roports fran_ais v2.xls");
		setSpreadSheetHandler(new InternalXlsHandler());
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsXlsFilename(filename, "coordonn_es g_ographiques a_roports fran_ais v2");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			n.put("aeroway", "aerodrome");
		}
	}
	
	protected class InternalXlsHandler extends DefaultSpreadSheetHandler {

		public InternalXlsHandler() {
			setLineNumber(4);
			setHandlesProjection(true);
		}

		@Override
		public LatLon getCoor(EastNorth en, String[] fields) {
			Matcher x = COOR_PATTERN.matcher(fields[getXCol()]);
			Matcher y = COOR_PATTERN.matcher(fields[getYCol()]);
			if (x.matches() && y.matches() && x.groupCount() >= 4 && y.groupCount() >= 4) {
				return new LatLon(convertDegreeMinuteSecond(y), convertDegreeMinuteSecond(x));
			}
			return null;
		}
		
		protected double convertDegreeMinuteSecond(Matcher m) {
			Double deg = Double.parseDouble(m.group(1));
			Double min = Double.parseDouble(m.group(2));
			Double sec = Double.parseDouble(m.group(3));
			Double sign = deg < 0 
					|| (m.groupCount() >= 5 && (m.group(4).equalsIgnoreCase("Sud") || m.group(4).equalsIgnoreCase("Ouest"))) 
					? -1.0 : +1.0;
			if (sign < 0) {
				if (deg > 0) {
					deg *= sign;
				}
				min *= sign;
				sec *= sign;
			}
			return OdUtils.convertDegreeMinuteSecond(deg, min, sec);
		}
	}
}
