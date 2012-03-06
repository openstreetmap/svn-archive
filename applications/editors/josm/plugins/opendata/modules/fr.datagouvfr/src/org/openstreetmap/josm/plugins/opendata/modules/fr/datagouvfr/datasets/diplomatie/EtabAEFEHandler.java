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
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.diplomatie;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class EtabAEFEHandler extends DataGouvDataSetHandler {

	public EtabAEFEHandler() {
		super("Géolocalisation-des-établissements-du-réseau-d'enseignement-de-l'AEFE-30382449", wgs84);
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvFilename(filename, "ETALAB_MAEE_Extraction_LDAP_geoloc_AEFE_20..-..-..\\.csv-fr");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			n.put("amenity", "school");
			replace(n, "code_etab", "ref");
			replace(n, "ENTStructureNomCourant", "name:fr");
			replace(n, "adresse", "addr");
			replace(n, "ENTStructureSiteWeb", "website");
			replace(n, "ENTStructureEmail", "contact:email");
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getCsvSeparator()
	 */
	@Override
	public String getCsvSeparator() {
		return ",";
	}
}
