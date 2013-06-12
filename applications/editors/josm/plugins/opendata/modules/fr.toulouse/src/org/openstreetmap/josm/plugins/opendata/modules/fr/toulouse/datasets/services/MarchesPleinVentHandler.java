//    JOSM opendata plugin.
//    Copyright (C) 2011-2013 Don-vip
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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.services;

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class MarchesPleinVentHandler extends ToulouseDataSetHandler {

    public MarchesPleinVentHandler() {
        super(19640, "amenity=marketplace");
        setName("Marchés de plein vent");
        setCategory(CAT_SERVICES);
        setSingleProjection(wgs84);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsOdsFilename(filename, "MPVToulouse_OpenData");
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.remove("Adresse");
            n.remove("Code Postal");
            replace(n, "Nom", "name", new ValueReplacer() {
                @Override public String replace(String value) {
                    return WordUtils.capitalizeFully(value).replace(", ", "").replace("MarchÉ", "Marché");
                }});
            n.put("amenity", "marketplace");
            replace(n, "Type", "note");
            String hours = "";
            for (String[] day : new String[][]{
                    new String[]{"Lundi","Mo"},
                    new String[]{"mardi","Tu"},
                    new String[]{"mercredi","We"},
                    new String[]{"jeudi","Th"},
                    new String[]{"vendredi","Fr"},
                    new String[]{"samedi","Sa"},
                    new String[]{"dimanche","Su"}
            }) {
                String value = n.get(day[0]);
                if (value != null) {
                    if (!hours.isEmpty()) {
                        hours += "; ";
                    }
                    hours += day[1]+" "+value.replace(" ", "").replace('–','-').replace('h', ':').replace(":-", ":00-");
                    if (hours.endsWith(":")) {
                        hours += "00";
                    }
                    n.remove(day[0]);
                }
            }
            if (!hours.isEmpty()) {
                n.put("opening_hours", hours);
            }
        }
    }
}
