// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.education;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class EtabSupHandler extends DataGouvDataSetHandler {

	private class EtabSupCsvHandler extends InternalCsvHandler {
		@Override
		public LatLon getCoor(EastNorth en, String[] fields) {
			// X/Y sont inversees dans le fichier
			return wgs84.eastNorth2latlon(new EastNorth(en.north(), en.east()));
		}
	}
	
	public EtabSupHandler() {
		super("Etablissements-d'enseignement-supérieur-30382046", wgs84);
		setName("Établissements d'enseignement supérieur");
		setDownloadFileName("livraison ETALAB 28 11 2011.xls");
		setCsvHandler(new EtabSupCsvHandler());
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
}
