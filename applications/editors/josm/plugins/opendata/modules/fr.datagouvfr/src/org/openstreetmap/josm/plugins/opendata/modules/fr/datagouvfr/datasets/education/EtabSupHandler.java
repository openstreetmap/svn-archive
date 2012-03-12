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

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class EtabSupHandler extends DataGouvDataSetHandler {

	public EtabSupHandler() {
		super("Etablissements-d'enseignement-supérieur-30382046", wgs84);
		setName("Établissements d'enseignement supérieur");
		setDownloadFileName("livraison ETALAB 28 11 2011.xls");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsXlsFilename(filename, "livraison ETALAB .. .. 20..(\\.xls-fr)?");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			replace(n, "NOM_ETABLISSEMENT", "name");
			n.put("amenity", "university");
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.portals.fr.datagouvfr.datasets.DataGouvDataSetHandler#getSpreadSheetCoor(org.openstreetmap.josm.data.coor.EastNorth, java.lang.String[])
	 */
	@Override
	public LatLon getSpreadSheetCoor(EastNorth en, String[] fields) {
		// X/Y sont inversees dans le fichier
		return wgs84.eastNorth2latlon(new EastNorth(en.north(), en.east()));
	}
}
