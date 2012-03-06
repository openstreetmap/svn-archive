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

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class VeloToulouseHandler extends ToulouseDataSetHandler {

	public VeloToulouseHandler() {
		super(12546, "amenity=bicycle_rental");
		setWikiPage("Vélô Toulouse");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvKmzTabFilename(filename, "Velo_Toulouse");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			n.put("amenity", "bicycle_rental");
			n.put("network", "VélôToulouse");
			n.put("operator", "JCDecaux");
			if (n.hasKey("M_en_S_16_nov_07") && n.get("M_en_S_16_nov_07").equals("O")) {
				n.put("start_date", "2007-11-16");
			}
			n.remove("M_en_S_16_nov_07");
			n.remove("Mot_Directeur");
			n.remove("No");
			n.remove("Nrivoli");
			n.remove("street");
			replace(n, "nb_bornettes", "capacity");
			replace(n, "num_station", "ref");
			replace(n, "nom", "name");
			n.put("name", WordUtils.capitalizeFully(n.get("name")));
			n.remove("code_insee");
			n.remove("commune");
			n.remove("color");
			if (n.hasKey("En_service") && n.get("En_service").equals("N")) {
				n.put("fixme", "Station pas en service");
			}
			n.remove("En_service");
		}
	}
}
