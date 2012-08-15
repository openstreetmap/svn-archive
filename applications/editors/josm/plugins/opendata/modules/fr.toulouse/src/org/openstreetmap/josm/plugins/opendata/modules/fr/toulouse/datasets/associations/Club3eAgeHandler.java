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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.associations;

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class Club3eAgeHandler extends ToulouseDataSetHandler {

    public Club3eAgeHandler() {
        super(12587, "social_facility=outreach", "social_facility:for=senior");
        setWikiPage("Clubs du 3ème âge");
        setCategory(CAT_ASSOCIATIONS);
    }
    
    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Club_3E_AGE");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            replace(n, "Nom", "name");
            n.put("name", WordUtils.capitalizeFully(n.get("name")));
            n.put("social_facility", "outreach");
            n.put("social_facility:for", "senior");
            n.remove("Adresse");
            n.remove("CP");
            n.remove("Classe");
            n.remove("CodSTI");
            n.remove("Description");
            n.remove("Numero");
            n.remove("Ville");
            n.remove("color");
        }
    }
}
