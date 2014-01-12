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
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchAdministrativeUnit;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;
import org.openstreetmap.josm.tools.Pair;

public class ForetsPubliquesHandler extends DataGouvDataSetHandler {

	private static final String PATTERN = "for_publ_v20.._reg(..)";

	public ForetsPubliquesHandler() {
		setName("Forêts publiques");
	}
	
	@Override
	public boolean acceptsFilename(String filename) {
		boolean result = acceptsZipFilename(filename, PATTERN) || acceptsShpFilename(filename, PATTERN);
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
			String regionCode = m.group(1);
			if (regionCode.equals("42")) {
				return "Forêts-publiques-d’Alsace-30378695";
			} else if (regionCode.equals("72")) {
				return "Forêts-publiques-d'Aquitaine-30378994";
			} else if (regionCode.equals("83")) {
				return "Forêts-publiques-d'Auvergne-30379302";
			} else if (regionCode.equals("26")) {
				return "Forêts-publiques-de-Bourgogne-30379206";
			} else if (regionCode.equals("53")) {
				return "Forêts-publiques-de-Bretagne-30378874";
			} else if (regionCode.equals("24")) {
				return "Forêts-publiques-du-Centre-30379134";
			} else if (regionCode.equals("21")) {
				return "Forêts-publiques-de-Champagne-Ardenne-30378853";
			} else if (regionCode.equals("94")) {
				return "Forêts-publiques-de-Corse-30379118";
			} else if (regionCode.equals("43")) {
				return "Forêts-publiques-de-Franche-Comté-30378652";
			} else if (regionCode.equals("11")) {
				return "Forêts-publiques-d’Île-de-France-30378829";
			} else if (regionCode.equals("91")) {
				return "Forêts-publiques-de-Languedoc-Roussillon-30379312";
			} else if (regionCode.equals("74")) {
				return "Forêts-publiques-du-Limousin-30378844";
			} else if (regionCode.equals("41")) {
				return "Forêts-publiques-de-Lorraine-30378675";
			} else if (regionCode.equals("73")) {
				return "Forêts-publiques-de-Midi-Pyrénées-30378665";
			} else if (regionCode.equals("31")) {
				return "Forêts-publiques-du-Nord-Pas-de-Calais-30379095";
			} else if (regionCode.equals("25")) {
				return "Forêts-publiques-de-Basse-Normandie-30378962";
			} else if (regionCode.equals("23")) {
				return "Forêts-publiques-de-Haute-Normandie-30379164";
			} else if (regionCode.equals("52")) {
				return "Forêts-publiques-des-Pays-de-la-Loire-30378999";
			} else if (regionCode.equals("22")) {
				return "Forêts-publiques-de-Picardie-30379389";
			} else if (regionCode.equals("54")) {
				return "Forêts-publiques-de-Poitou-Charente-30378900";
			} else if (regionCode.equals("93")) {
				return "Forêts-publiques-de-PACA-30379322";
			} else if (regionCode.equals("82")) {
				return "Forêts-publiques-de-Rhône-Alpes-30378732";
			} else {
				System.err.println("Unknown French region code: "+regionCode);
			}
		}
		return null;
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (OsmPrimitive p : ds.allPrimitives()) {
			// For all identified objects (both closed ways and multipolygons)
			if (p.hasKey("IIDTN_FRT")) {
				p.put("landuse", "forest");
				replace(p, "LLIB_FRT", "name");
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getDataURLs()
	 */
	@Override
	public List<Pair<String, URL>> getDataURLs() {
		List<Pair<String, URL>> result = new ArrayList<Pair<String,URL>>();
		try {
			for (FrenchAdministrativeUnit region : FrenchAdministrativeUnit.allRegions) {
				if (!region.getCode().startsWith("0")) { // Skip DOM/TOM
					result.add(getForetURL(region.getCode(), region.getName()));
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return result;
	}

	private Pair<String, URL> getForetURL(String code, String name) throws MalformedURLException {
		return new Pair<String, URL>(name, new URL(FRENCH_PORTAL+"var/download/"+"for_publ_v2011_reg"+code+".zip"));
	}
}
