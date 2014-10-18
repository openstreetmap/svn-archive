// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.sncf.datasets;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;

public class EquipementsHandler extends SncfDataSetHandler {

    private class LambertIICsvHandler extends InternalCsvHandler {
        @Override
        public LatLon getCoor(EastNorth en, String[] fields) {
            // Lambert II coordinates offset by 2000000 (see https://fr.wikipedia.org/wiki/Projection_conique_conforme_de_Lambert#Projections_officielles_en_France_métropolitaine)
            return super.getCoor(new EastNorth(en.getX(), en.getY()-2000000), fields);
        }
    }
    
    public EquipementsHandler() {
        super("equipementsgares");
        setCsvHandler(new LambertIICsvHandler());
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
}
