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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.urbanisme;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class SanisetteHandler extends ToulouseDataSetHandler {

	public SanisetteHandler() {
		super(12584, "amenity=toilets");
		setWikiPage("Sanisettes");
		setCategory(CAT_URBANISME);
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvKmzTabFilename(filename, "Sanisette");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			n.remove("name");
			n.put("amenity", "toilets");
			n.put("supervised", "no");
			n.put("unisex", "yes");
			n.put("fee", "no");
			n.put("operator", "JCDecaux");
			n.put("opening_hours", "24/7");
			replace(n, "numero", "ref:grandtoulouse");
			replace(n, "PMR", "wheelchair", new String[]{"true", "false"}, new String[]{"yes", "no"});
			String valide = n.get("emplacement_valide");
			if (valide != null && valide.equalsIgnoreCase("non")) {
				n.put("fixme", "L'emplacement semble invalide !");
			} else {
				n.remove("emplacement_valide");
			}
			n.remove("adresse");
			n.remove("INSEE");
			n.remove("color");
		}
	}
}
