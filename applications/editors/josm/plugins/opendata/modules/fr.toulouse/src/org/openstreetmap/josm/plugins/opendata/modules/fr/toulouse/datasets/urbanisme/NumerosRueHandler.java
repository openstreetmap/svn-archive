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

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.plugins.opendata.core.util.NamesFrUtils;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;

public class NumerosRueHandler extends ToulouseDataSetHandler {

	public NumerosRueHandler() {
		super(12673, "addr:housenumber");
		setWikiPage("Numéros de rue");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvKmzTabFilename(filename, "Numeros");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		Map<String, Relation> associatedStreets = new HashMap<String, Relation>();
		
		for (Node n : ds.getNodes()) {
			replace(n, "no", "addr:housenumber");
			n.remove("numero");
			replace(n, "lib_off", "addr:street");
			n.remove("mot_directeur");
			n.remove("name");
			n.remove("rivoli");
			n.remove("nrivoli");
			//n.remove("sti");
			n.remove("color");
			String streetName = NamesFrUtils.checkStreetName(n, "addr:street");
			Relation street = associatedStreets.get(n.get("sti"));
			if (street == null) {
				associatedStreets.put(n.get("sti"), street = new Relation());
				street.put("type", "associatedStreet");
				street.put("name", streetName);
				ds.addPrimitive(street);
			}
			street.addMember(new RelationMember("house", n));
		}
	}
}
