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
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.education;

import java.nio.charset.Charset;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class Etab1er2ndDegreHandler extends DataGouvDataSetHandler {

	public Etab1er2ndDegreHandler() {
		super("Géolocalisation-des-établissements-d'enseignement-du-premier-degré-et-du-second-degré-du-ministère-d-30378093");
	}
	
	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvFilename(filename, "MENJVA_etab_geoloc.csv-fr");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			n.put("amenity", "school");
			replace(n, "numero_uai", "ref:FR:UAI");
			replace(n, "appellation_officielle_uai", "name");
			add(n, "lib_nature", "school:FR", 
					new String[]{".*MATERNELLE.*", ".*ELEMENTAIRE.*", "COLLEGE.*", "LYCEE.*"}, 
					new String[]{"maternelle", "élémentaire", "college", "lycée"});
			n.remove("etat_etablissement"); // Toujours a 1
			n.remove("nature_uai"); // cle numerique associe au champ lib_nature, redondant, donc
			n.remove("patronyme_uai"); // deja dans le nom
			n.remove("sous_fic"); // cycle ? 1 pour ecoles, 3 pour colleges et lycees
			// Voir http://www.infocentre.education.fr/bcn/domaine/voir/id/31
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#handlesCsvProjection()
	 */
	@Override
	public boolean handlesSpreadSheetProjection() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getCsvCoor(org.openstreetmap.josm.data.coor.EastNorth, java.lang.String[])
	 */
	@Override
	public LatLon getSpreadSheetCoor(EastNorth en, String[] fields) {
		return getLatLonByDptCode(en, fields[0].substring(0, 3), false);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getCsvCharset()
	 */
	@Override
	public Charset getCsvCharset() {
		return Charset.forName(ISO8859_15);
	}
}
