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
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.ecologie;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;
import org.openstreetmap.josm.tools.Pair;

public class InventaireForestierNationalHandler extends DataGouvDataSetHandler {

	private static final String FORET = "foret";
	private static final String PEUPLERAIE = "peupleraie";
	
	private static final String PATTERN = "placettes_("+FORET+"|"+PEUPLERAIE+")_(20..)";

	public InventaireForestierNationalHandler() {
		super();
		setName("Inventaire forestier national");
	}
	
	@Override
	public boolean acceptsFilename(String filename) {
		boolean result = acceptsCsvFilename(filename, PATTERN);
		if (result) {
			setNationalPortalPath(findPortalSuffix(filename));
		}
		return result;
	}

	@Override
	public boolean acceptsUrl(String url) {
		boolean result = super.acceptsUrl(url);
		if (result) {
			setNationalPortalPath(findPortalSuffix(url));
		}
		return result;
	}

	private final String findPortalSuffix(CharSequence filename) {
		Matcher m = Pattern.compile(".*"+PATTERN+"\\....").matcher(filename);
		if (m.matches()) {
			String type = m.group(1);
			if (type.equals(FORET)) {
				switch (Integer.parseInt(m.group(2))) {
					case 2010: return "Données-points-forêt-2010-30379029";
					case 2009: return "Données-points-forêt-2009-30378890";
					case 2008: return "Données-points-forêt-2008-30378754";
					case 2007: return "Données-points-forêt-2007-30378682";
					case 2006: return "Données-points-forêt-2006-30379198";
					case 2005: return "Données-points-forêt-2005-30378933";
					default: System.err.println("Unknown IFN year: "+m.group(2));
				}
			} else if (type.equals(PEUPLERAIE)) {
				switch (Integer.parseInt(m.group(2))) {
					case 2010: return "Données-points-peupleraie-2010-30379180";
					case 2009: return "Données-points-peupleraie-2009-30378848";
					case 2008: return "Données-points-peupleraie-2008-30378632";
					case 2007: return "Données-points-peupleraie-2007-30378611";
					case 2006: return "Données-points-peupleraie-2006-30378633";
					case 2005: return "Données-points-peupleraie-2005-30378717";
					default: System.err.println("Unknown IFN year: "+m.group(2));
				}
			} else {
				System.err.println("Unknown IFN type: "+type);
			}
		}
		return null;
	}
	
	@Override
	public void updateDataSet(DataSet ds) {
		// TODO
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getDataURLs()
	 */
	@Override
	public List<Pair<String, URL>> getDataURLs() {
		List<Pair<String, URL>> result = new ArrayList<Pair<String,URL>>();
		try {
			for (int year = 2010; year >= 2005; year--) {
				result.add(getIfnURL(year, "Peupleraies", PEUPLERAIE));
				result.add(getIfnURL(year, "Forêts", FORET));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private Pair<String, URL> getIfnURL(int year, String name, String type) throws MalformedURLException {
		return new Pair<String, URL>(name+" "+year, new URL("http://www.ifn.fr/spip/IMG/csv/placettes_"+type+"_"+year+".csv"));
	}
}
