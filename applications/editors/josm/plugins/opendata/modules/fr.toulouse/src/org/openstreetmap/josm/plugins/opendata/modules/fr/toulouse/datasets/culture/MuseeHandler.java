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
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.culture;

import org.apache.commons.lang3.text.WordUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class MuseeHandler extends ToulouseDataSetHandler {

	public MuseeHandler() {
		super(12426, "tourism=museum");
		setWikiPage("Musées");
		setCategory(CAT_CULTURE);
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvKmzTabFilename(filename, "Musee");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		for (Node n : ds.getNodes()) {
			replace(n, "NOMS", "name");
			replace(n, "SITE_INTERNET", "contact:website");
			n.put("tourism", "museum");
			n.remove("ADRESSES");
			n.remove("Num");
			n.remove("Index");
			replacePhone(n, "TELEPHONE");
			String name = WordUtils.capitalizeFully(n.get("name")).replace("Musee", "Musée").replace(" D ", " d'").replace(" L ", " l'").trim();
			int index = name.indexOf("Musée");
			if (index > 1) {
				name = name.substring(index) + " " + name.substring(0, index-1);
			}
			while (name.contains("  ")) {
				name = name.replace("  ", " ");
			}
			n.put("name", name);
		}
	}
}
