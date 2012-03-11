package org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.datasets;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;

public class EquipementsHandler extends SncfDataSetHandler {

	public EquipementsHandler() {
		super("equipementsgares");
		setSingleProjection(lambert4Zones[1]); // Lambert II
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvXlsFilename(filename, "gare_20......");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			replace(n, "nom gare", "name");
			n.put("railway", "station");
		}
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchDataSetHandler#getSpreadSheetCoor(org.openstreetmap.josm.data.coor.EastNorth, java.lang.String[])
	 */
	@Override
	public LatLon getSpreadSheetCoor(EastNorth en, String[] fields) {
		// Lambert II coordinates offset by 2000000 (see http://fr.wikipedia.org/wiki/Projection_conique_conforme_de_Lambert#Projections_officielles_en_France_métropolitaine)
		return super.getSpreadSheetCoor(new EastNorth(en.getX(), en.getY()-2000000), fields);
	}
}
