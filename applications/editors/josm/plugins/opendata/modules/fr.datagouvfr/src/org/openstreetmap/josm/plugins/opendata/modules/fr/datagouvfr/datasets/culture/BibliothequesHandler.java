// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.culture;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class BibliothequesHandler extends DataGouvDataSetHandler {

    public BibliothequesHandler() {
        super("Adresses-des-bibliothèques-municipales-30382179", lambert93);
        setName("Bibliothèques municipales");
        setDownloadFileName("lieux de lecture_geoloc.txt");
        getCsvHandler().setCharset(OdConstants.ISO8859_15);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvFilename(filename, "lieux de lecture_geoloc.txt-fr");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("amenity", "library");
        }
    }
}
