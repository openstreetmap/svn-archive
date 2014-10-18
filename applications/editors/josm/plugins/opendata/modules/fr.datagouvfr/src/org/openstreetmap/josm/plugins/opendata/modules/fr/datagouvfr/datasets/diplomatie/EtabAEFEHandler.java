// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.diplomatie;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class EtabAEFEHandler extends DataGouvDataSetHandler {

    public EtabAEFEHandler() {
        super("Géolocalisation-des-établissements-du-réseau-d'enseignement-de-l'AEFE-30382449", wgs84);
        setName("Établissements du réseau d'enseignement de l'AEFE");
        setDownloadFileName("ETALAB_MAEE_Extraction_LDAP_geoloc_AEFE_2011-10-13.csv");
        getCsvHandler().setSeparator(",");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvFilename(filename, "ETALAB_MAEE_Extraction_LDAP_geoloc_AEFE_20..-..-..(\\.csv-fr)?");
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
}
