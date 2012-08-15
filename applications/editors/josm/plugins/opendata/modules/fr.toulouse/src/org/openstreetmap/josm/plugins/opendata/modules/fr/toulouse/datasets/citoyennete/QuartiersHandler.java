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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.citoyennete;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class QuartiersHandler extends ToulouseDataSetHandler {

    public QuartiersHandler() {
        super(12574, "admin_level=11");
        setWikiPage("Quartiers de proximité");
        setCategory(CAT_CITOYENNETE);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsKmzTabFilename(filename, "Quartiers");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Relation r : ds.getRelations()) {
            r.remove("name");
            replace(r, "Quartier", "ref");
            replace(r, "Nom_Quartier", "description");
            r.put("type", "boundary");
            r.put("boundary", "administrative");
            r.put("admin_level", "11");
            r.remove("Adjoint_Secteur");
            r.remove("Elu_Referent_Quartier");
            r.remove("Id");
            r.remove("Nom_Secteur");
            r.remove("Secteur");
        }
    }
}
