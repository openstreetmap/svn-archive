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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class PMRHandler extends ToulouseDataSetHandler {

    public PMRHandler() {
        super(12538, "amenity=parking_space");
        setWikiPage("PMR");
        setCategory(CAT_TRANSPORT);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Emplacements_PMR");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.remove("name");
            n.put("amenity", "parking_space");
            n.put("access:disabled", "designated");
            n.put("wheelchair", "designated");
            replace(n, "nb_places", "capacity:disabled");
            n.remove("Lib_voie");
            n.remove("No");
            n.remove("commune");
            n.remove("code_insee");
            n.remove("color");
            replace(n, "id_PMR", "ref:grandtoulouse");
        }
    }
}
