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
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.transport;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchAdministrativeUnit;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;
import org.openstreetmap.josm.tools.Pair;

public class Route500Handler extends DataGouvDataSetHandler {

    private static final String OSMFR_PORTAL = "http://osm13.openstreetmap.fr/~cquest/route500/";
    
    private static final String ARCHIVE_PATTERN = "ROUTE500_1-1_SHP_LAMB93_D..._20..-..-..";

    private static final String SHP_PATTERN = "(EMPRISE|NOEUD_FERRE|TRONCON_VOIE_FERREE|ZONE_OCCUPATION_SOL|"+
            "COTE_FRONTIERE|TRONCON_HYDROGRAPHIQUE|COMMUNE|LIMITE_ADMINISTRATIVE|COMMUNICATION_RESTREINTE|NOEUD_COMMUNE|NOEUD_ROUTIER|TRONCON_ROUTE|AERODROME)";

    public Route500Handler() {
        super("572583");
        setName("Route 500");
        try {
            setLocalPortalURL("http://professionnels.ign.fr/route500");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return accepts7ZipFilename(filename, ARCHIVE_PATTERN)
                || acceptsShpFilename(filename, SHP_PATTERN);
    }

    @Override
    public void updateDataSet(DataSet ds) {
        // TODO
    }
    
    @Override
    public List<Pair<String, URL>> getDataURLs() {
        List<Pair<String, URL>> result = new ArrayList<Pair<String,URL>>();
        try {
            for (FrenchAdministrativeUnit dpt : FrenchAdministrativeUnit.allDepartments) {
                if (dpt.getCode().startsWith("0")) { // Skip DOM/TOM
                    result.add(getRoute500URL(dpt.getCode(), dpt.getName()));
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Pair<String, URL> getRoute500URL(String code, String name) throws MalformedURLException {
        return new Pair<String, URL>(name, new URL(OSMFR_PORTAL+"ROUTE500_1-1_SHP_LAMB93_D"+code+"_2012-11-21.7z"));
    }
}
