// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.education;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.DefaultCsvHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class Etab1er2ndDegreHandler extends DataGouvDataSetHandler {

    private class EtabCsvHandler extends DefaultCsvHandler {
        
        public EtabCsvHandler() {
            setCharset(OdConstants.ISO8859_15);
            setHandlesProjection(true);
        }
        
        @Override
        public LatLon getCoor(EastNorth en, String[] fields) {
            return getLatLonByDptCode(en, fields[0].substring(0, 3), false);
        }
    }
    
    public Etab1er2ndDegreHandler() {
        super("Géolocalisation-des-établissements-d'enseignement-du-premier-degré-et-du-second-degré-du-ministère-d-30378093");
        setName("Établissements d'enseignement du premier degré et du second degré");
        setDownloadFileName("MENJVA_etab_geoloc.csv");
        setCsvHandler(new EtabCsvHandler());
    }
    
    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvFilename(filename, "MENJVA_etab_geoloc(\\.csv-fr)?");
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
}
