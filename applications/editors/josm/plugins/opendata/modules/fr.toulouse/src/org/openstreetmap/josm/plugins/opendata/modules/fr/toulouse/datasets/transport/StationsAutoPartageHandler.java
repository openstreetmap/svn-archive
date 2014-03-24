// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class StationsAutoPartageHandler extends ToulouseDataSetHandler {

    public StationsAutoPartageHandler() {
        super(19163, "amenity=car_sharing");
        setName("Stations d'auto partage");
        setCategory(CAT_TRANSPORT);
        getCsvHandler().setCharset(ISO8859_15);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Autopartage");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("amenity", "car_sharing");
            n.remove("Lib_voie");
            n.remove("mot_dir");
            n.remove("No");
            n.remove("commune");
            n.remove("code_insee");
            replace(n, "id_AUTO", REF_TOULOUSE_METROPOLE);
            replace(n, "nb_places", "capacity");
            replace(n, "arrete", "bylaw");
            replace(n, "Societe", "operator");
            replace(n, "annee", "start_date");
            n.remove("photo");
            replace(n, "obervations", "note");
        }
    }
}
